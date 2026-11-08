package com.opencapture.openpocketcine

import com.opencapture.opencine.vocabulary.CameraCommands
import com.opencapture.opencine.vocabulary.CameraStatus
import com.opencapture.opencine.vocabulary.EvComp

/**
 * The camera settings a shot preset keeps with its framing: exposure mode, ISO and shutter (or EV
 * and the Auto ISO ceiling), white balance, aperture strategy, focus mode and track, and the focus
 * point. Only values the camera reported are captured. Format and color mode are left alone: they
 * restart the picture and cannot change while recording.
 */
internal object ShotCameraSettings {
    const val EXPO = "expo"
    const val ISO = "iso"
    const val SHUTTER = "shutter"
    const val EV = "ev"
    const val ISO_LIMIT = "isoLimit"
    const val WB = "wb"
    const val KELVIN = "kelvin"
    const val TINT = "tint"
    const val APERTURE = "aperture"
    const val FOCUS = "focus"
    const val TRACK = "track"
    const val FOCUS_X = "fx"
    const val FOCUS_Y = "fy"
    private const val POINT_SCALE = 10_000

    interface Writer {
        fun expoMode(mode: Int)
        fun isoIndex(index: Int)
        fun shutterDenom(denom: Int)
        fun ev(thirds: Int)
        fun isoLimit(raw: Int)
        fun whiteBalanceAuto(tint: Int)
        fun whiteBalance(kelvin: Int, tint: Int)
        fun aperture(raw: Int)
        fun focusMode(continuous: Boolean)
        fun focusTrack(raw: Int)
    }

    fun capture(status: CameraStatus): Map<String, Int> = buildMap {
        if (status.expoMode >= 0) put(EXPO, status.expoMode)
        if (status.expoMode == CameraCommands.EXPO_MANUAL) {
            if (status.isoIndex >= 0) put(ISO, status.isoIndex)
            if (status.shutterDenom > 0) put(SHUTTER, status.shutterDenom)
        } else if (status.expoMode >= 0) {
            EvComp.fromRaw(status.evComp)?.let { put(EV, it.thirds) }
            if (status.isoLimit >= 0) put(ISO_LIMIT, status.isoLimit)
        }
        when (status.wbMode) {
            CameraCommands.WB_AUTO -> { put(WB, CameraCommands.WB_AUTO); put(TINT, status.wbTint) }
            CameraCommands.WB_CUSTOM -> if (status.wbKelvin >= 2_000) {
                put(WB, CameraCommands.WB_CUSTOM); put(KELVIN, status.wbKelvin); put(TINT, status.wbTint)
            }
        }
        if (status.apertureStrategy >= 0) put(APERTURE, status.apertureStrategy)
        if (status.focusMode == CameraCommands.FOCUS_SINGLE || status.focusMode == CameraCommands.FOCUS_CONTINUOUS) {
            put(FOCUS, status.focusMode)
        }
        if (status.focusTrack >= 0) put(TRACK, status.focusTrack)
        if (status.hasCameraFocusPoint && status.focusX.isFinite() && status.focusY.isFinite()) {
            put(FOCUS_X, (status.focusX.coerceIn(0.0, 1.0) * POINT_SCALE).toInt())
            put(FOCUS_Y, (status.focusY.coerceIn(0.0, 1.0) * POINT_SCALE).toInt())
        }
    }

    /** Sends only what differs from [current], exposure mode first so ISO and shutter land in it. */
    fun apply(settings: Map<String, Int>, current: CameraStatus, writer: Writer) {
        if (settings.isEmpty()) return
        val expo = settings[EXPO]
        if (expo != null && expo != current.expoMode) writer.expoMode(expo)
        val manual = (expo ?: current.expoMode) == CameraCommands.EXPO_MANUAL
        if (manual) {
            settings[ISO]?.takeIf { it != current.isoIndex }?.let(writer::isoIndex)
            settings[SHUTTER]?.takeIf { it > 0 && it != current.shutterDenom }?.let(writer::shutterDenom)
        } else {
            settings[EV]?.takeIf { EvComp.fromRaw(current.evComp)?.thirds != it }?.let(writer::ev)
            settings[ISO_LIMIT]?.takeIf { it != current.isoLimit }?.let(writer::isoLimit)
        }
        val tint = settings[TINT] ?: 0
        when (settings[WB]) {
            CameraCommands.WB_AUTO ->
                if (current.wbMode != CameraCommands.WB_AUTO || current.wbTint != tint) writer.whiteBalanceAuto(tint)
            CameraCommands.WB_CUSTOM -> settings[KELVIN]?.let { kelvin ->
                if (current.wbMode != CameraCommands.WB_CUSTOM || current.wbKelvin != kelvin || current.wbTint != tint) {
                    writer.whiteBalance(kelvin, tint)
                }
            }
        }
        settings[APERTURE]?.takeIf { it != current.apertureStrategy }?.let(writer::aperture)
        settings[FOCUS]?.takeIf { it != current.focusMode }?.let { writer.focusMode(it == CameraCommands.FOCUS_CONTINUOUS) }
        settings[TRACK]?.takeIf { it != current.focusTrack }?.let(writer::focusTrack)
    }

    /** The saved focus point (0…1 each way), set once the gimbal arrives. */
    fun focusPoint(settings: Map<String, Int>): Pair<Float, Float>? {
        val x = settings[FOCUS_X] ?: return null
        val y = settings[FOCUS_Y] ?: return null
        return (x.toFloat() / POINT_SCALE).coerceIn(0f, 1f) to (y.toFloat() / POINT_SCALE).coerceIn(0f, 1f)
    }
}

package com.opencapture.openpocketcine

import com.opencapture.opencine.vocabulary.CameraCommands
import com.opencapture.opencine.vocabulary.CameraStatus
import com.opencapture.opencine.vocabulary.EvComp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ShotCameraSettingsTest {
    private class Recorder : ShotCameraSettings.Writer {
        val calls = mutableListOf<String>()
        override fun expoMode(mode: Int) { calls += "expo $mode" }
        override fun isoIndex(index: Int) { calls += "iso $index" }
        override fun shutterDenom(denom: Int) { calls += "shutter $denom" }
        override fun ev(thirds: Int) { calls += "ev $thirds" }
        override fun isoLimit(raw: Int) { calls += "isoLimit $raw" }
        override fun whiteBalanceAuto(tint: Int) { calls += "awb $tint" }
        override fun whiteBalance(kelvin: Int, tint: Int) { calls += "wb $kelvin $tint" }
        override fun aperture(raw: Int) { calls += "aperture $raw" }
        override fun focusMode(continuous: Boolean) { calls += "focus $continuous" }
        override fun focusTrack(raw: Int) { calls += "track $raw" }
    }

    private val manual = CameraStatus(expoMode = CameraCommands.EXPO_MANUAL, isoIndex = 4, shutterDenom = 50,
        wbMode = CameraCommands.WB_CUSTOM, wbKelvin = 5600, wbTint = 2, focusMode = CameraCommands.FOCUS_SINGLE,
        hasCameraFocusPoint = true, focusX = 0.25, focusY = 0.75)

    @Test
    fun manualShotRestoresExposureWhiteBalanceAndFocus() {
        val saved = ShotCameraSettings.capture(manual)
        val auto = CameraStatus(expoMode = CameraCommands.EXPO_AUTO, wbMode = CameraCommands.WB_AUTO,
            focusMode = CameraCommands.FOCUS_CONTINUOUS)
        val writer = Recorder()
        ShotCameraSettings.apply(saved, auto, writer)
        assertEquals(listOf("expo ${CameraCommands.EXPO_MANUAL}", "iso 4", "shutter 50", "wb 5600 2", "focus false"),
            writer.calls)
        assertEquals(0.25f to 0.75f, ShotCameraSettings.focusPoint(saved))
    }

    @Test
    fun autoShotKeepsEvAndSendsNothingWhenUnchanged() {
        val status = CameraStatus(expoMode = CameraCommands.EXPO_AUTO, evComp = EvComp(-3).rawValue,
            wbMode = CameraCommands.WB_AUTO, wbTint = 0)
        val saved = ShotCameraSettings.capture(status)
        assertEquals(-3, saved[ShotCameraSettings.EV])
        val writer = Recorder()
        ShotCameraSettings.apply(saved, status, writer)
        assertTrue(writer.calls.isEmpty(), writer.calls.toString())
        val changed = Recorder()
        ShotCameraSettings.apply(saved, status.copy(evComp = EvComp(2).rawValue), changed)
        assertEquals(listOf("ev -3"), changed.calls)
    }

    @Test
    fun unreportedValuesAreNotCaptured() {
        val saved = ShotCameraSettings.capture(CameraStatus())
        assertTrue(saved.isEmpty(), saved.toString())
        assertNull(ShotCameraSettings.focusPoint(saved))
    }
}

# Fork: gimbal shot presets

This fork adds **Shots** to the gimbal panel: save up to 12 framings per camera
(pan, tilt and zoom) and recall any of them with one tap. The gimbal travels to the
saved framing over the chosen travel time (1–20 s) with no countdown.

- Open the gimbal panel, then **Shots**.
- Frame with the joystick and zoom (they stay usable while Shots is open), hold still,
  tap **Save current framing**.
- Tap a shot to go there, or tap its letter (A, B, C…) in the column on the left of the live screen (sized so six fit, portrait or landscape). **⋯** on a shot: Update (save the current framing into it),
  Rename, move up/down, delete.
- Shots are saved per camera and survive app restarts.
- Keep the camera on a fixed mount: angles are relative to the handle.
- In D-Log2 the zoom part of a shot is skipped (the camera refuses programmed zoom there);
  pan and tilt still move.

Build: the `Fork APK (shot presets)` workflow checks out upstream OpenCine at the pin,
applies `patches/opencine/*.patch`, and uploads a debug APK
(`com.opencapture.openpocketcine.debug`, installs beside the official app).

## Settings and zoom buttons

- Each shot also keeps the camera settings at save time: exposure mode, ISO and shutter
  (Manual) or EV and the Auto ISO ceiling (Auto), white balance (Auto with tint, or Kelvin and
  tint), aperture strategy, focus mode and track, and the focus point. Recalling a shot sends
  only what differs; the focus point is set when the gimbal arrives. Format and color mode are
  not changed. Shots saved before this keep working without settings; **Update** adds them.
- The zoom chip beside the gimbal stick is replaced by three buttons: **1×, 3×, 6×** (only the
  stops the lens reaches are shown).

# Fork: gimbal shot presets

This fork adds **Shots** to the gimbal panel: save up to 12 framings per camera
(pan, tilt and zoom) and recall any of them with one tap. The gimbal travels to the
saved framing over the chosen travel time (1–20 s) with no countdown.

- Open the gimbal panel, then **Shots**.
- Frame with the joystick and zoom (they stay usable while Shots is open), hold still,
  tap **Save current framing**.
- Tap a shot to go there. **⋯** on a shot: Update (save the current framing into it),
  Rename, move up/down, delete.
- Shots are saved per camera and survive app restarts.
- Keep the camera on a fixed mount: angles are relative to the handle.
- In D-Log2 the zoom part of a shot is skipped (the camera refuses programmed zoom there);
  pan and tilt still move.

Build: the `Fork APK (shot presets)` workflow checks out upstream OpenCine at the pin,
applies `patches/opencine/*.patch`, and uploads a debug APK
(`com.opencapture.openpocketcine.debug`, installs beside the official app).

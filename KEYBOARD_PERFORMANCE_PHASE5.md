# Keyboard & Interaction Performance — Phase 5

## Scope
This phase targets cold/open speed, dialog opening, first-input readiness, keyboard visibility, IME navigation, dismissal, RTL layout stability, and avoidance of visible jank.

## Audited input surfaces
The current source inventory contains 19 intentional text-input surfaces. The inventory is enforced by `KeyboardInputSurfaceMatrixContractTest`.

## Changes
- Shared keyboard helper now performs its first focus/IME request immediately after one attachment frame.
- Fallback retries are bounded to at most three attempts with frame-scale delays (8–32 ms clamp).
- Customer creation now uses the same lifecycle-safe keyboard contract as the other dialogs.
- No global `ALWAYS_VISIBLE` soft-input mode is permitted.
- Primary transaction/customer inputs have stable test tags for future device-level latency measurement; tags do not alter rendering.
- Existing dialog motion remains 160 ms enter / 120 ms exit.

## Startup observations
- Splash is intentionally held until settings are loaded to prevent default-state flash.
- No arbitrary startup delay was found in `MainActivity`.
- Background worker scheduling is launched from `Application.onCreate` on `Dispatchers.Default`.
- Recurring transaction execution is asynchronous from the UI composition path, but actual device frame timing must still be measured.

## Verification boundary
Static contracts can prove that the source follows the intended performance design. They cannot prove a universal keyboard latency or first-frame score across all Android devices/IME implementations. Device/instrumentation tests are required for final timing measurements.

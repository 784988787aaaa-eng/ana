# SmartLedger — Keyboard / IME Test Plan

## Scope

Only intentional input surfaces are covered. Read-only dialogs, confirmation windows, menus, and unrelated screens are excluded.

## Static contract coverage

`KeyboardInputSurfaceMatrixContractTest` covers 19 input components and fails if:

1. a known input component disappears or a new input component is added without review;
2. any input field lacks explicit `keyboardOptions`;
3. any input field lacks explicit `keyboardActions`;
4. an automatic keyboard-opening callsite omits an explicit `autoShow` decision;
5. `SOFT_INPUT_STATE_ALWAYS_VISIBLE` returns;
6. the two transaction entry points stop sharing `AddTransactionPopup` / `AddTransactionFormFields`;
7. the add-customer or add-transaction IME focus chain regresses.

## Runtime Compose coverage

`KeyboardImeRuntimeContractTest` executes the actual Compose semantics tree for the shared transaction form:

- Amount field is editable.
- IME `Next` moves focus to Description.
- Description IME `Done` invokes the save callback.

This is a device/instrumentation test and therefore exercises the real Compose input semantics rather than only parsing source.

## Device acceptance matrix

Run the complete instrumentation keyboard suite on at least:

- Android API 24, 29, 33, 35/36;
- compact phone (~5-inch), normal phone (~6-inch), large phone/foldable width;
- portrait and landscape;
- Arabic system locale and an English system locale;
- at least one AOSP/Gboard environment and one OEM keyboard environment when available.

For every input surface verify:

- keyboard opens only after intentional entry into the input surface;
- no unrelated/read-only window opens the keyboard;
- field remains visible above the IME (`imePadding`/scroll behavior);
- `Next` advances in the intended logical order;
- `Done`/`Search` performs the intended action and releases focus when appropriate;
- closing/backing out of the dialog removes focus and hides the keyboard;
- rotating/resizing while editing does not duplicate the keyboard or lose text;
- opening a second input dialog after closing the first does not inherit stale focus;
- Arabic/RTL layout does not reverse the logical field order;
- numeric/phone/password fields expose the intended keyboard type.

## Release rule

A static contract pass is necessary but not sufficient for claiming universal phone compatibility. Runtime device results must be recorded as Passed/Failed/Unverified per API/device class. If the local environment cannot execute Gradle or instrumentation, do not mark runtime coverage as Passed.

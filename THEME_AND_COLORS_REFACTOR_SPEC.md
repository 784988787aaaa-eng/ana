# THEME_AND_COLORS_REFACTOR_SPEC.md

# Phase 1 — Theme & Semantic Tokens Architecture
## Strict Engineering Specification for Gemini

> **Document Status:** IN PROGRESS  
> **Phase Scope:** Theme, Color Architecture, Semantic Color Tokens, Compose UI Colors, Canvas/PDF Color Mapping  
> **Out of Scope:** Layout, typography, animation, navigation, business logic, performance, database, networking, non-color UI behavior.

---

# 0. إلزام التنفيذ والتتبع — Developer Protocol & State Tracking

## 0.1 أمر إلزامي لمهندس التنفيذ Gemini

**قبل تعديل أي ملف:**

- [x] إنشاء ملف التتبع المحلي التالي في جذر المشروع:
  `THEME_AND_COLORS_REFACTOR_SPEC.md`
- [x] نسخ هذه المواصفة إليه كما هي.
- [x] عدم تعديل ترتيب البنود أو حذف أي Checklist item.
- [x] بعد إتمام كل بند فعلياً، وضع `[x]` مكان `[ ]`.
- [x] لا تضع `[x]` قبل نجاح التعديل والبناء/التحقق المرتبط به.
- [x] إذا تعذر تنفيذ بند، اتركه `[ ]` وأضف تحته مباشرة سبب التعذر.
- [x] بعد كل مجموعة ملفات، نفّذ فحصاً نصياً للألوان وأصلح النتائج قبل الانتقال للمجموعة التالية.
- [x] لا تعتبر المهمة مكتملة اعتماداً على نجاح Gradle فقط؛ يجب أن تنجح أيضاً فحوصات Hardcoded Color والـLight/Dark.
- [x] لا تعدّل أي ملف خارج نطاق Theme/Colors في هذه المرحلة.
- [x] لا تغير أي سلوك أو Layout أو Typography أو Animation إلا إذا كان التغيير ضرورياً لإزالة مصدر لون غير مركزي.

## 0.2 قاعدة المصدر الواحد

يجب أن تصبح البنية النهائية:

`Primitive Palette`
→ `MizanColors`
→ `MaterialTheme`
→ `Compose UI`

ولـPDF/Canvas:

`Primitive Palette`
→ `MizanColors / MizanDocumentColors`
→ `PDF/Canvas Adapter`

**ممنوع:**

`UI Component → HEX`

`UI Component → Color.White`

`UI Component → Light/Dark conditional color`

`PDF Renderer → مستقل HEX palette`

---

# 1. تدقيق خط الأساس — Baseline Inventory

## 1.1 نتائج التدقيق المؤكدة

تم العثور على **347 موضعاً** مطابقاً لفحص اللون المباشر ضمن:

- `ui/`
- `ui/theme/`
- `data/serialization/pdf/`
- `res/values/`
- `res/values-night/`

وتشمل:

- `Color(0x...)`
- `Color.White`
- `Color.Black`
- `Color.Gray`
- `Color.LightGray`
- `Color.DarkGray`
- `Color.White.copy(...)`
- HEX literals داخل PDF
- Dark/Light color branching داخل بعض Components.

## 1.2 الملفات التي يجب مراجعتها

### Theme

- [x] `app/src/main/java/com/example/ui/theme/Color.kt`
- [x] `app/src/main/java/com/example/ui/theme/Theme.kt`

### Shared UI

- [x] `app/src/main/java/com/example/ui/components/AppNavigationDrawer.kt`
- [x] `app/src/main/java/com/example/ui/components/ExitConfirmDialog.kt`
- [x] `app/src/main/java/com/example/ui/components/CurrencyRevalueConfirmDialog.kt`
- [x] `app/src/main/java/com/example/ui/components/WelcomeOnboardingDialog.kt`
- [x] `app/src/main/java/com/example/ui/components/DeveloperSealFooter.kt`

### Business Profile

- [x] `app/src/main/java/com/example/ui/screens/BusinessProfileScreen.kt`
- [x] `app/src/main/java/com/example/ui/screens/business/BusinessProfileLogoSection.kt`

### Calculator / Cloud

- [x] `app/src/main/java/com/example/ui/screens/CalculatorDialog.kt`
- [x] `app/src/main/java/com/example/ui/screens/cloud/components/CloudStatsHeader.kt`
- [x] `app/src/main/java/com/example/ui/screens/cloud/components/CloudBottomActionBar.kt`
- [x] `app/src/main/java/com/example/ui/screens/cloud/components/CloudBackupDialogs.kt`

### Habayeb

- [x] `AddCustomerFormFields.kt`
- [x] `AddCustomerPopup.kt`
- [x] `AddCustomerTypeAndCurrencySelector.kt`
- [x] `AddTransactionPopup.kt`
- [x] `CategoryDeleteConfirmationDialog.kt`
- [x] `CustomerDeleteAndEditDialogs.kt`
- [x] `CustomerHistoryDialogs.kt`
- [x] `CustomerHistoryFAB.kt`
- [x] `CustomerHistoryFilterSheet.kt`
- [x] `CustomerHistoryShareBottomSheet.kt`
- [x] `CustomerItemRow.kt`
- [x] `CustomerTypeChangeSection.kt`
- [x] `ExchangeRateSetupDialog.kt`
- [x] `FloatingSearchBubble.kt`
- [x] `HabayebFabAndFloatingBars.kt`
- [x] `HabayebFilterToolbar.kt`
- [x] `HabayebFinanceHeader.kt`
- [x] `HabayebHeaderTopBar.kt`
- [x] `RecurringTransactionPopup.kt`
- [x] `header/HabayebHeaderSearchBar.kt`
- [x] `row/TransactionRowSections.kt`

### Ledger

- [x] `ActivationActionsFooter.kt`
- [x] `ActivationKeyInputSection.kt`
- [x] `ActivationTrialInfoCard.kt`
- [x] `CommitmentDeleteConfirmationDialog.kt`
- [x] `CommitmentEditDialog.kt`
- [x] `CommitmentItemCardClean.kt`
- [x] `CommitmentSummaryGradientCard.kt`
- [x] `CommitmentsListDialog.kt`
- [x] `CommitmentsSummaryCards.kt`
- [x] `DayCard.kt`
- [x] `DayCardDeleteDialog.kt`
- [x] `DayCardHeader.kt`
- [x] `DayCardSummaryBar.kt`
- [x] `DayCardTransactionRow.kt`
- [x] `LedgerBottomDock.kt`
- [x] `MainLedgerDialogs.kt`
- [x] `MainLedgerHeader.kt`
- [x] `MainLedgerSelectionBar.kt`

### Other UI

- [x] `trash/components/TrashFilterToolbar.kt`
- [x] `settings/components/BackupResetConfirmationFlow.kt`
- [x] `security/components/SecurityActivePanel.kt`
- [x] `security/components/SecurityHeaderBanner.kt`
- [x] `security/lock/LockKeypadViews.kt`
- [x] `security/lock/RecoveryPhraseContent.kt`
- [x] `security/lock/PasscodeDotIndicators.kt`

### PDF

- [x] `data/serialization/pdf/PdfColors.kt`
- [x] `data/serialization/pdf/PdfStatementTotalsRenderer.kt`
- [x] `data/serialization/pdf/PdfPaints.kt`
- [x] `data/serialization/pdf/PdfPageRenderer.kt`
- [x] `data/serialization/pdf/MasterBookletPdfEngine.kt`
- [x] `data/serialization/pdf/PdfCustomerSummaryRenderer.kt`
- [x] `data/serialization/pdf/PdfTransactionRowRenderer.kt`

### XML

- [x] `app/src/main/res/values/colors.xml`
- [x] `app/src/main/res/values-night/colors.xml`

---

# 2. Central Design Tokens Schema

# 2.1 `Color.kt` — إعادة الهيكلة

## الملف

`app/src/main/java/com/example/ui/theme/Color.kt`

## المطلوب

- [x] إنشاء قسم واضح باسم `Primitive Palette`.
- [x] إنشاء قسم واضح باسم `Semantic Colors`.
- [x] عدم تعريف ألوان UI الدلالية كـglobal standalone colors.
- [x] حصر HEX الخام في Primitive Palette فقط.
- [x] عدم استخدام أسماء مرتبطة بلون فعلي غير مطابق مثل `EmeraldPrimary` للون البنفسجي الحالي.

---

# 2.2 Primitive Palette

أعد تسمية primitive values الحالية إلى أسماء محايدة.

### Brand

- [x] `EmeraldPrimary` → `BrandPrimary`
- [x] `EmeraldLight` → `BrandPrimaryLight`
- [x] `CoralAccent` → `BrandSecondary`
- [x] `EmeraldDark` → `BrandPrimaryDark`
- [x] `CoralDark` → `BrandSecondaryDark`

### Neutral

- [x] `IvoryBackground` → `NeutralBackgroundLight`
- [x] `DarkBackground` → `NeutralBackgroundDark`
- [x] `DarkSurface` → `NeutralSurfaceDark`
- [x] `LightSurface` → `NeutralSurfaceLight`
- [x] `TextPrimaryDark` → `NeutralTextPrimaryDark`
- [x] `TextSecondaryDark` → `NeutralTextSecondaryDark`
- [x] `TextPrimaryLight` → `NeutralTextPrimaryLight`
- [x] `TextSecondaryLight` → `NeutralTextSecondaryLight`
- [x] `BorderDark` → `NeutralBorderDark`
- [x] `BorderLight` → `NeutralBorderLight`

### Financial primitives

- [x] `SoftRed` → primitive فقط، وعدم السماح باستخدامه مباشرة في UI
- [x] `SoftGreen` → primitive فقط
- [x] `CreditGreen` → دمجه مع primitive الخاص بالـcredit بدلاً من وجود نسخة مكررة
- [x] `DebtRed` → دمجه مع primitive الخاص بالـdebt
- [x] `CreditGreenDark` → دمجه مع dark credit semantic source
- [x] `DebtRedDark` → دمجه مع dark debt semantic source
- [x] `CreditContainerLight`
- [x] `CreditContainerDark`
- [x] `CreditBorderLight`
- [x] `CreditBorderDark`
- [x] `DebtContainerLight`
- [x] `DebtContainerDark`
- [x] `DebtBorderLight`
- [x] `DebtBorderDark`

---

# 2.3 `MizanColors`

أنشئ كائناً/نوعاً مركزياً:

`MizanColors`

ويجب أن يحتوي على هذه الـroles:

## Brand

- [x] `brandPrimary`
- [x] `onBrandPrimary`
- [x] `brandPrimaryContainer`
- [x] `onBrandPrimaryContainer`
- [x] `brandSecondary`
- [x] `onBrandSecondary`
- [x] `brandSecondaryContainer`
- [x] `onBrandSecondaryContainer`

## Surface

- [x] `appBackground`
- [x] `appSurface`
- [x] `appSurfaceContainer`
- [x] `appSurfaceContainerLow`
- [x] `appSurfaceContainerHigh`
- [x] `appSurfaceVariant`

## Text

- [x] `contentPrimary`
- [x] `contentSecondary`
- [x] `contentTertiary`
- [x] `contentDisabled`
- [x] `contentOnBrand`

## Borders

- [x] `border`
- [x] `borderVariant`
- [x] `borderStrong`

## Credit — علينا / دائن

- [x] `credit`
- [x] `onCredit`
- [x] `creditContainer`
- [x] `onCreditContainer`
- [x] `creditBorder`
- [x] `creditGradientStart`
- [x] `creditGradientEnd`

## Debt — لنا / مدين

- [x] `debt`
- [x] `onDebt`
- [x] `debtContainer`
- [x] `onDebtContainer`
- [x] `debtBorder`
- [x] `debtGradientStart`
- [x] `debtGradientEnd`

## Selection

- [x] `selection`
- [x] `onSelection`
- [x] `selectionContainer`
- [x] `selectionBorder`

## Status

- [x] `success`
- [x] `onSuccess`
- [x] `successContainer`
- [x] `warning`
- [x] `onWarning`
- [x] `warningContainer`
- [x] `error`
- [x] `onError`
- [x] `errorContainer`
- [x] `info`
- [x] `onInfo`
- [x] `infoContainer`

## Header

- [x] `headerForeground`
- [x] `headerForegroundMuted`
- [x] `headerControlContainer`
- [x] `headerControlBorder`
- [x] `headerControlContent`
- [x] `headerControlContentMuted`

## Floating UI

- [x] `floatingControlBackground`
- [x] `floatingControlBorder`
- [x] `floatingControlContent`
- [x] `floatingControlContentMuted`

## Dialogs

- [x] `dialogScrim`
- [x] `dialogActionContent`
- [x] `dialogDestructiveContent`

## Inputs

- [x] `inputBorder`
- [x] `inputBorderFocused`
- [x] `inputContent`
- [x] `inputLabel`
- [x] `inputPlaceholder`

## Security

- [x] `securityBackground`
- [x] `securityForeground`
- [x] `securityForegroundMuted`
- [x] `securityKeyBackground`
- [x] `securityKeyContent`
- [x] `securityKeyBorder`
- [x] `securityIndicatorEmpty`
- [x] `securityIndicatorFilled`

## Miscellaneous

- [x] `separator`
- [x] `shadowTint`
- [x] `ripple`
- [x] `disabledTrack`

---

# 2.4 Light/Dark instances

- [x] إنشاء `LightMizanColors`.
- [x] إنشاء `DarkMizanColors`.
- [x] كل role في `MizanColors` له قيمة في الوضعين.
- [x] لا توجد `isDark` checks داخل تعريفات Components لاختيار palette.
- [x] لا يوجد `...Light`/`...Dark` في أسماء الـSemantic Roles التي تصل إلى UI.

---

# 2.5 CompositionLocal

## الملف

`Color.kt` أو ملف Theme مخصص داخل نفس package

- [x] إنشاء `LocalMizanColors`.
- [x] توفير instance الحالي عبر Theme.
- [x] إنشاء accessor:
  `MaterialTheme.mizanColors`
- [x] منع استدعاء الـCompositionLocal مباشرة من Components.
- [x] عدم إنشاء CompositionLocal منفصل لكل feature.

---

# 3. `Theme.kt`

## الملف

`app/src/main/java/com/example/ui/theme/Theme.kt`

## 3.1 Theme primitives

- [x] حذف `ThemeWhite`.
- [x] حذف `ThemeBlack`.
- [x] عدم تعريف Color literal في `MizanLightColorScheme`.
- [x] عدم تعريف Color literal في `MizanDarkColorScheme`.
- [x] ربط Material roles من primitive/semantic source المركزي.

## LightColorScheme

- [x] السطر 25 `primaryContainer` → central source
- [x] السطر 26 `onPrimaryContainer` → central source
- [x] السطر 29 `secondaryContainer` → central source
- [x] السطر 30 `onSecondaryContainer` → central source
- [x] السطر 34 `onTertiaryContainer` → central source
- [x] السطر 38 `onErrorContainer` → central source
- [x] السطر 41 `surfaceVariant` → central source
- [x] السطر 46 `outlineVariant` → central source
- [x] السطر 48 `surfaceContainerHigh` → central source

## DarkColorScheme

- [x] السطر 55 `primaryContainer` → central source
- [x] السطر 56 `onPrimaryContainer` → central source
- [x] السطر 59 `secondaryContainer` → central source
- [x] السطر 60 `onSecondaryContainer` → central source
- [x] السطر 64 `onTertiaryContainer` → central source
- [x] السطر 68 `onErrorContainer` → central source
- [x] السطر 71 `surfaceVariant` → central source
- [x] السطر 76 `outlineVariant` → central source
- [x] السطر 78 `surfaceContainerHigh` → central source
- [x] السطر 79 `surfaceContainerLow` → central source

## Ripple

- [x] السطر 136: إزالة `Color(0xFF6B21A8)`.
- [x] السطر 136: ربط Ripple بـ`mizanColors.ripple`.
- [x] إبقاء alpha الحالي فقط إذا كان مطلوباً بصرياً.

## Theme Provider

- [x] ربط `LocalMizanColors` داخل `MizanTheme`.
- [x] تمرير Light instance عند `darkTheme == false`.
- [x] تمرير Dark instance عند `darkTheme == true`.
- [x] منع أي Component من تكرار هذه العملية.

---

# 4. Color.kt — Gradient Cleanup

لا تستخدم HEX داخل Brushes خارج الـcentral palette.

## Gradient bindings

- [x] `PrimaryGradient` → `brandGradient`
- [x] `CoralGradient` → `brandSecondaryGradient`
- [x] `IncomeGradientLight` → `creditGradient`
- [x] `IncomeGradientDark` → `creditGradient`
- [x] `ExpenseGradientLight` → `debtGradient`
- [x] `ExpenseGradientDark` → `debtGradient`
- [x] `SelectedItemGradientLight` → `selectionGradient`
- [x] `SelectedItemGradientDark` → `selectionGradient`
- [x] `NeonGreenCyanGradient` → semantic gradient أو إلغاء استخدامه إن لم يعد ذا معنى
- [x] `VioletHeroGradient` → `heroGradient`
- [x] `HeaderCardGradientDark` → `headerGradient`
- [x] `HeaderCardGradientLight` → `headerGradient`
- [x] `GoldLicenseGradient` → `licenseGradient`
- [x] `WarningGradient` → `warningGradient`
- [x] `SplashSweepGradient` → `splashGradient`
- [x] `SplashRadialGlow` → `splashGlow`

## Raw gradient literals الحالية

- [x] السطر 62 `Color(0xFF0284C7)` → primitive centralized secondary/gradient color
- [x] السطر 66 `Color(0xFFF0FDF4)` → `creditContainer`
- [x] السطر 74 `Color(0xFFFDF2F2)` → `debtContainer`
- [x] السطر 82 `Color(0xFFD1FAE5)` → selection/credit primitive
- [x] السطر 86 `Color(0xFF121F17)` → dark selection primitive
- [x] السطر 90 `Color(0xFF00E676)` → success primitive
- [x] السطر 90 `Color(0xFF00B0FF)` → secondary/info primitive
- [x] السطر 94 `Color(0xFF4B36A2)` → `BrandPrimary`
- [x] السطر 94 `Color(0xFF7C3AED)` → brand gradient primitive
- [x] السطر 94 `Color(0xFF8C7CFF)` → `BrandPrimaryLight`
- [x] السطر 98 `Color(0xFF1E1E1E)` → dark surface primitive
- [x] السطر 98 `Color(0xFF262626)` → dark surface container primitive
- [x] السطر 102 `Color(0xFFFFFFFF)` → neutral light surface primitive
- [x] السطر 102 `Color(0xFFF8F9FA)` → neutral light background primitive
- [x] السطر 106 amber colors → warning primitive family
- [x] السطر 110 amber/red colors → warning/error gradient family
- [x] السطور 115–117 blue/green/blue → central gradient primitives
- [x] السطور 123–124 blue/green alpha colors → central primitive + alpha

---

# 5. Color.kt — Duplicate / Legacy Primitive Cleanup

- [x] دمج `SoftRed` و`DebtRed`.
- [x] دمج `SoftGreen` و`CreditGreen`.
- [x] دمج `SuccessGreenBgLight` و`SelectionGreenContainerLight` إذا كان المعنى واحداً؛ وإذا اختلف المعنى فاحتفظ بهما لكن اربط كل واحد بـSemantic role مستقل.
- [x] مراجعة `WarningAmber` مقابل `WarningRed` وعدم استخدامها بالتبادل.
- [x] مراجعة `InfoBlue` مقابل `IndigoAccent`.
- [x] مراجعة `NeonGreen` وعدم استخدامه كـnormal success color إلا إذا كان الدور مقصوداً.
- [x] مراجعة `NeonCyan`.
- [x] مراجعة `SoftLavender`.
- [x] مراجعة `LightRedTint`.

---

# 6. Color.kt — Status / License / Info

## الأسطر 130–144

- [x] `WhatsAppGreen` و`WhatsAppLightGreen`: إبقاؤهما فقط إذا كانا خاصين فعلاً بـWhatsApp branding؛ عدم استخدامهما كتطبيق brand.
- [x] `WarningAmber` → `warning`
- [x] `WarningAmberBg` → `warningContainer`
- [x] `WarningAmberBorder` → warning border role
- [x] `WarningDarkRedText` → error/onError semantic role إذا كان المعنى خطأ؛ وإلا أنشئ `dangerText`.
- [x] `WarningOrangeButton` → warning action role
- [x] `LicenseGreenBg` → license/status container
- [x] `LicenseGreenText` → license/status foreground
- [x] `LicenseBadgeGreenText` → license badge foreground
- [x] `InfoBlue` → `info`
- [x] `InfoBlueBgLight` → `infoContainer` light
- [x] `InfoBlueBgDark` → `infoContainer` dark
- [x] `InfoBlueTextLight` → `info` light foreground
- [x] `InfoBlueTextDark` → `info` dark foreground

---

# 7. Color.kt — Slate Palette

الأسطر 147–156:

- [x] `Slate50` → primitive neutral
- [x] `Slate100` → primitive neutral
- [x] `Slate200` → primitive neutral
- [x] `Slate300` → primitive neutral
- [x] `Slate400` → primitive neutral
- [x] `Slate500` → primitive neutral
- [x] `Slate600` → primitive neutral
- [x] `Slate700` → primitive neutral
- [x] `Slate800` → primitive neutral
- [x] `Slate900` → primitive neutral

## القاعدة

لا يسمح لأي UI Component باستخدام `Slate500` ونحوه مباشرة.

كل استخدام يجب تحويله إلى Semantic role.

---

# 8. Color.kt — Chip Palette

الأسطر 186–214:

## Red Chip

- [x] `ChipRedBgDarkSelected` → `debtContainer` / selected variant
- [x] `ChipRedBgDarkUnselected` → `debtContainer`
- [x] `ChipRedBgLightSelected` → `debtContainer` selected
- [x] `ChipRedBgLightUnselected` → `debtContainer`
- [x] `ChipRedBorderDarkSelected` → `debtBorder`
- [x] `ChipRedBorderDarkUnselected` → `debtBorder`
- [x] `ChipRedBorderLightSelected` → `debtBorder`
- [x] `ChipRedBorderLightUnselected` → `debtBorder`
- [x] `ChipRedTextDark` → `debt`
- [x] `ChipRedTextLight` → `debt`
- [x] `ChipRedHeaderDark` → `debt`/header financial role
- [x] `ChipRedHeaderLight` → `debt`/header financial role

## Green Chip

- [x] `ChipGreenBgDarkSelected` → `creditContainer` selected
- [x] `ChipGreenBgDarkUnselected` → `creditContainer`
- [x] `ChipGreenBgLightSelected` → `creditContainer` selected
- [x] `ChipGreenBgLightUnselected` → `creditContainer`
- [x] `ChipGreenBorderDarkSelected` → `creditBorder`
- [x] `ChipGreenBorderDarkUnselected` → `creditBorder`
- [x] `ChipGreenBorderLightSelected` → `creditBorder`
- [x] `ChipGreenBorderLightUnselected` → `creditBorder`
- [x] `ChipGreenTextDark` → `credit`
- [x] `ChipGreenTextLight` → `credit`
- [x] `ChipGreenHeaderDark` → `credit`
- [x] `ChipGreenHeaderLight` → `credit`

بعد النقل:

- [x] حذف الـchip palette القديمة إذا لم يعد لها أي consumer.

---

# 9. Color.kt — CategoryPalette

الأسطر 226–245:

- [x] `AMBER_DARK` → category primitive
- [x] `AMBER_LIGHT` → category primitive
- [x] `PINK_DARK` → category primitive
- [x] `PINK_LIGHT` → category primitive
- [x] `GRAY_LIGHT_DARK` → category primitive
- [x] `GRAY_LIGHT_LIGHT` → category primitive
- [x] `RED_SOFT_DARK` → category primitive
- [x] `RED_SOFT_LIGHT` → category primitive
- [x] `YELLOW_DARK` → category primitive
- [x] `YELLOW_LIGHT` → category primitive
- [x] `BLUE_SOFT_DARK` → category primitive
- [x] `BLUE_SOFT_LIGHT` → category primitive
- [x] `SKY_DARK` → category primitive
- [x] `SKY_LIGHT` → category primitive
- [x] `PURPLE_DARK` → category primitive
- [x] `PURPLE_LIGHT` → category primitive
- [x] `EMERALD_SOFT_DARK` → rename neutral/category primitive
- [x] `EMERALD_SOFT_LIGHT` → rename neutral/category primitive
- [x] `GREEN_FIFTY_DARK` → category primitive
- [x] `GREEN_FIFTY_LIGHT` → category primitive

### القاعدة

`CategoryPalette` يبقى مخصصاً لفئات البيانات فقط.

- [x] لا تستخدم CategoryPalette لبناء brand UI.
- [x] لا تستخدم CategoryPalette للـfinancial credit/debt.
- [x] لا تنشئ HEX إضافياً في Components.

---

# 10. AvatarPastelPalette

الأسطر 252–254:

- [x] إبقاء الألوان فقط إذا كانت Avatar-specific.
- [x] نقل القيم إلى قسم `Avatar Palette`.
- [x] عدم السماح باستخدامها خارج avatar rendering.
- [x] عدم ربطها بمعنى credit/debt/status.
- [x] توحيد تعريفها كمجموعة مركزية واحدة.

---

# 11. UI File Refactoring — Shared Components

## 11.1 `AppNavigationDrawer.kt`

- [x] السطر 142 `Color.White` → `MaterialTheme.colorScheme.onPrimary`
- [x] السطر 158 `Color.White.copy(0.15)` → `MaterialTheme.mizanColors.headerControlContainer`
- [x] السطر 166 `Color.White` → `onPrimary`
- [x] السطر 177 `Color.White` → semantic header/brand foreground

---

## 11.2 `ExitConfirmDialog.kt`

- [x] السطر 34 `Color.Black.copy(0.5)` → `mizanColors.dialogScrim`
- [x] السطر 134 `Color.White` → action/content semantic token المناسب للزر

---

## 11.3 `CurrencyRevalueConfirmDialog.kt`

- [x] السطر 156 `Color.White` → `MaterialTheme.colorScheme.onPrimary` إذا كان الزر primary؛ وإلا `mizanColors.dialogActionContent`.

---

## 11.4 `WelcomeOnboardingDialog.kt`

- [x] السطر 290 `Color.White` → semantic button/action content
- [x] السطر 296 `Color.White` → semantic icon content

---

## 11.5 `DeveloperSealFooter.kt`

- [x] السطر 86 `Color.White` → semantic footer foreground.

---

# 12. Calculator

## `CalculatorDialog.kt`

- [x] السطر 256 `Color.LightGray` → `mizanColors.contentTertiary`
- [x] السطر 408 `Color.White` → Semantic `equalsButtonContent`
- [x] السطر 410 حذف `if (isDark) Color.White else brandPrimary`
- [x] السطر 410 استخدام semantic token واحد
- [x] السطر 414 حذف `if (isDark) Color.White ...`
- [x] السطر 414 استخدام `mizanColors` أو Material on-surface role
- [x] حذف أي import أصبح غير مستخدم بعد العملية.

---

# 13. Cloud

## `CloudStatsHeader.kt`

- [x] السطر 136 `Color.Gray` → `mizanColors.contentTertiary`
- [x] السطر 136 `EmeraldPrimary` → semantic brand/status token
- [x] لا يستخدم الملف primitive palette مباشرة.

## `CloudBottomActionBar.kt`

- [x] السطر 64 `Color.White` → action content
- [x] السطر 67 `Color.White` → action content
- [x] السطر 87 `Color.White` → action content
- [x] السطر 90 `Color.White` → action content

## `CloudBackupDialogs.kt`

- [x] السطر 109 `Color.White` → dialog action/content role
- [x] السطر 183 `Color.White` → dialog action/content role
- [x] السطر 254 `Color.White` → dialog action/content role

---

# 14. Business Profile

## `BusinessProfileScreen.kt`

- [x] السطر 99 `Color.White` → semantic content-on-container
- [x] السطر 112 `Color.White` → semantic icon content

## `BusinessProfileLogoSection.kt`

- [x] السطر 118 `Color.White` → semantic icon content
- [x] السطر 142 `Color.White` → semantic icon content

---

# 15. Habayeb — Customer Components

## `CustomerItemRow.kt`

- [x] السطر 157 `Color.White` → semantic icon/content color الخاص بالحاوية.

## `CustomerHistoryFilterSheet.kt`

- [x] السطر 127 `if (isSelected) Color.White ...` → selected chip content semantic token
- [x] السطر 211 نفس الاستبدال
- [x] حذف أي conditional color لا يمثل behavior فعلياً.

## `CustomerHistoryFAB.kt`

- [x] السطر 42 `Color.White` → FAB content semantic token.

## `CustomerTypeChangeSection.kt`

- [x] السطر 55 حذف `if (isDark) Color.Gray else Color.DarkGray`
- [x] السطر 55 → `mizanColors.contentSecondary`
- [x] السطر 102 selected content → semantic selected content
- [x] السطر 126 selected content → semantic selected content
- [x] السطر 158 `Color.White` → semantic selected content

## `CustomerDeleteAndEditDialogs.kt`

- [x] السطر 83 `Color.White` → action content
- [x] السطر 89 `Color.White` → action/content
- [x] السطر 356 `Color.White` → action/content

## `CustomerHistoryDialogs.kt`

- [x] السطر 43 `Color.White` → destructive action content
- [x] السطر 153 `Color.White` → action content
- [x] السطر 172 `Color.White` → action content

## `CustomerHistoryShareBottomSheet.kt`

- [x] السطر 294 `Color.White` → sheet action content
- [x] السطر 321 `Color.White` → sheet action content

## `CategoryDeleteConfirmationDialog.kt`

- [x] السطر 58 `Color.White` → `mizanColors.onError` إذا كانت الخلفية error؛ وإلا role مطابق فعلياً للحاوية.

## `ExchangeRateSetupDialog.kt`

- [x] السطر 257 `Color.White` → dialog action/icon content
- [x] السطر 323 `Color.White` → action content

## `AddCustomerPopup.kt`

- [x] السطر 198 `Color.Gray` → `mizanColors.contentTertiary`

## `AddCustomerTypeAndCurrencySelector.kt`

- [x] السطر 191 `Color.White` → selected/content semantic role
- [x] السطر 380 `Color.White` → button content semantic role

---

# 16. AddTransactionPopup

## الملف

`AddTransactionPopup.kt`

- [x] السطر 374 `contentColor = Color.White` → `mizanColors.onDebt` إذا كان `debtRedColor` هو debt semantic color
- [x] السطر 383 `Color.White` → `mizanColors.onDebt`
- [x] السطر 390 `contentColor = Color.White` → `mizanColors.onCredit`
- [x] السطر 399 `Color.White` → `mizanColors.onCredit`

### مهم

- [x] إزالة اعتماد الزرين على local colors `debtRedColor` و`creditGreenColor` إذا كانا مجرد نسخ من semantic colors.
- [x] استخدم `mizanColors.debt` و`mizanColors.credit`.

---

# 17. Floating Search / Header

## `FloatingSearchBubble.kt`

### السطر 70

- [x] `Color.White.copy(alpha = 0.28f)` → `floatingControlContainer`
- [x] `Color.White.copy(alpha = 0.15f)` → `floatingControlContainer` مع alpha الحالي

### السطر 75

- [x] `Color.White.copy(alpha = 0.45f)` → `floatingControlBorder`
- [x] `Color.Transparent` يبقى مسموحاً لأنه ليس hardcoded color identity.

### السطر 85

- [x] `Color.White` → `floatingControlContent`
- [x] `Color.White.copy(0.8)` → `floatingControlContentMuted`

### السطر 93

- [x] `Color.White` → floating control background/content حسب العنصر المقصود

### السطر 181

- [x] `Color.White.copy(...)` → `floatingControlContent`
- [x] إبقاء alpha state إذا كان يمثل interaction state.

### السطر 236

- [x] `Color.White` → `floatingControlContent`

---

## `HabayebFabAndFloatingBars.kt`

- [x] السطر 186 `Color.White.copy(...)` → `floatingControlContent`
- [x] السطر 252 `Color.White` → `floatingControlContent`

---

## `HabayebHeaderSearchBar.kt`

- [x] السطر 49 `Color.White.copy(0.18)` → `headerControlContainer`
- [x] السطر 64 `Color.White` → `headerControlContent`
- [x] السطر 73 `Color.White` → `headerForeground`
- [x] السطر 78 `SolidColor(Color.White)` → `SolidColor(mizanColors.headerForeground)`
- [x] السطر 91 `Color.White.copy(0.65)` → `headerForegroundMuted`
- [x] السطر 106 `Color.White.copy(0.8)` → `headerForeground`

---

## `HabayebHeaderTopBar.kt`

- [x] السطر 128 `Color.White.copy(0.18)` → `headerControlContainer`
- [x] السطر 235 `Color.White.copy(0.15)` → `headerControlContainer`
- [x] السطر 355 `Color.White.copy(0.15)` → `headerControlContainer`

---

# 18. HabayebFinanceHeader

## الملف

`HabayebFinanceHeader.kt`

- [x] السطر 142 `Color.White.copy(0.16)` → `headerControlContainer`
- [x] السطر 147 `Color.White` → `headerControlContent`
- [x] السطر 176 `Color.White.copy(0.85)` → `headerForeground`
- [x] السطر 193 `Color.White` → `headerControlContent`
- [x] السطر 216 `Color.White` → `headerForeground`
- [x] السطر 231 `activeThemeColor = Color.White` → `activeThemeColor = MaterialTheme.mizanColors.headerForeground`
- [x] السطر 243 `Color.White.copy(0.16)` → `headerControlContainer`
- [x] السطر 248 `Color.White` → `headerControlContent`

### شرط

- [x] إزالة أي reference مباشر إلى `EmeraldPrimary`, `SoftGreen`, `CreditGreen`, `DebtRed` أو غيرها من UI primitives داخل هذا الملف.

---

# 19. HabayebFilterToolbar

## الملف

`HabayebFilterToolbar.kt`

- [x] السطر 108 حذف `if (isDark) ... else Color.White`
- [x] السطر 108 استخدام `mizanColors.appSurface` أو semantic white-equivalent المناسب
- [x] السطر 313 selected sort icon → selected content semantic token
- [x] عدم وجود `Color.White` بعد الإصلاح.

---

# 20. RecurringTransactionPopup

- [x] السطر 62 حذف `TRANSLUCENT_WHITE_PILL`
- [x] عدم إنشاء بديل local.
- [x] جميع usages تعتمد على `mizanColors.headerControlContainer` أو semantic pill role.
- [x] الإبقاء على alpha `0.22f` فقط كـvisual state وليس كـلون جديد.

---

# 21. Habayeb Form Inputs

## `AddCustomerFormFields.kt`

- [x] السطر 52 حذف `if (isDark) ... else Color.LightGray.copy(...)`
- [x] استخدام `mizanColors.inputBorder`
- [x] عدم تكرار Light/Dark logic في الملف.

---

# 22. TransactionRowSections

- [x] السطر 70 `Color.White` → content-on-container semantic token.
- [x] عدم استخدام primitive palette مباشرة.

---

# 23. Ledger — MainLedgerHeader

## الملف

`MainLedgerHeader.kt`

- [x] السطر 139 `Color.White.copy(0.15)` → `headerControlContainer`
- [x] السطر 144 `Color.White` → `headerControlContent`
- [x] السطر 154 `Color.White` → button content semantic
- [x] السطر 169 `Color.White` → `headerForeground`
- [x] السطر 182 `Color.White.copy(0.15)` → `headerControlContainer`
- [x] السطر 187 `Color.White.copy(0.4)` → disabled header content semantic
- [x] السطر 214 `Color.White.copy(0.16)` → `headerControlContainer`
- [x] السطر 219 `Color.White` → `headerControlContent`
- [x] السطر 236 `Color.White.copy(0.85)` → `headerForeground`
- [x] السطر 253 `Color.White` → `headerControlContent`
- [x] السطر 267 `Color.White` → `headerForeground`
- [x] السطر 284 `activeThemeColor = Color.White` → semantic header foreground
- [x] السطر 296 `Color.White.copy(0.16)` → `headerControlContainer`
- [x] السطر 301 `Color.White` → `headerControlContent`
- [x] السطر 343 `Color.White.copy(0.95)` → `headerForeground`
- [x] السطر 354 `Color.White.copy(0.25)` → `headerControlContentMuted` / track semantic
- [x] السطر 371 `Color.White.copy(0.95)` → `headerForeground`
- [x] السطر 411 `Color.White.copy(0.2)` → `headerControlContainer`

### إضافي

- [x] إزالة direct usage لـ`LightRedTint` إن لم يكن semantic.
- [x] تحويله إلى `mizanColors.error` أو destructive disabled role.

---

# 24. Ledger Selection

## `MainLedgerSelectionBar.kt`

- [x] السطر 59 `Color.Black.copy(0.1)` → `mizanColors.shadowTint`
- [x] السطر 105 `EmeraldPrimary` أو equivalent → `mizanColors.selection`
- [x] عدم استخدام raw brand color للـselection.

---

# 25. CommitmentSummaryGradientCard

- [x] السطر 67 `Color(0xFFF7FBF9)` → `mizanColors.appSurfaceContainer` أو semantic commitment surface.
- [x] السطر 103 `Color.White` → `mizanColors.onCredit` أو on-container semantic.
- [x] أي `SoftGreen`, `EmeraldPrimary`, raw financial color → semantic financial role.
- [x] الـgradient نفسه يستمد الألوان من `creditGradientStart/End`.

---

# 26. Commitments Cards / Dialogs

## `CommitmentsSummaryCards.kt`

- [x] السطر 63 إزالة `if (isDark) ... else Color.White`
- [x] استخدام `mizanColors.appSurfaceContainer`

## `CommitmentDeleteConfirmationDialog.kt`

- [x] السطر 59 `Color.White` → action content semantic

## `CommitmentEditDialog.kt`

- [x] السطر 70 حذف `COMMITMENT_SAVE_BUTTON_TEXT_COLOR`
- [x] عدم تعريف `Color.White` محلياً.
- [x] استخدام semantic button content.

## `CommitmentItemCardClean.kt`

- [x] السطر 134 `Color.White` → semantic icon/content

## `CommitmentsListDialog.kt`

- [x] السطر 212 `Color.White` → content semantic
- [x] السطر 220 `Color.White` → content semantic

---

# 27. Day Cards

## `DayCard.kt`

- [x] السطر 106 إزالة:
  `if (isDark) Color.LightGray else Color.Gray`
- [x] استبداله بـ`mizanColors.contentTertiary`
- [x] السطر 114 إزالة `Color(0xFFF3F0FF)`
- [x] السطر 114 استخدام semantic surface/container role
- [x] عدم معرفة Dark/Light داخل الملف لاختيار color.

## `DayCardHeader.kt`

- [x] السطر 32 حذف `DAY_SELECTION_CHECK_ICON_COLOR = Color.White`
- [x] استخدام `mizanColors.onSelection`

## `DayCardSummaryBar.kt`

- [x] السطر 49 إزالة `Color(0xFFF7F9FC)`
- [x] إزالة `if (isDark) ... else ...`
- [x] استخدام `mizanColors.appSurfaceContainer`
- [x] تحويل financial color access إلى `credit/debt` semantic roles.

## `DayCardTransactionRow.kt`

- [x] السطر 132 `Color.White` → semantic content-on-container

## `DayCardDeleteDialog.kt`

- [x] السطر 65 `Color.White` → destructive action content

---

# 28. Ledger Bottom Dock

## `LedgerBottomDock.kt`

- [x] السطر 31 حذف `DOCK_BUTTON_CONTENT_COLOR = Color.White`
- [x] استخدام `mizanColors.floatingControlContent` أو semantic dock content.

---

# 29. MainLedgerDialogs

- [x] السطر 125 `Color.White` → destructive button content
- [x] السطر 310 `Color.White` → dialog action content

---

# 30. Activation UI

## `ActivationActionsFooter.kt`

- [x] السطر 180 `Color.White` → semantic action content
- [x] السطر 187 `Color.White` → semantic action content
- [x] السطر 207 `Color.White` → semantic icon content
- [x] السطر 215 `Color.White` → semantic action content
- [x] السطر 380 `Color.White` → semantic icon content
- [x] السطر 388 `Color.White` → semantic action content

## `ActivationKeyInputSection.kt`

- [x] السطر 87 `Color.White` → semantic input/action content

## `ActivationTrialInfoCard.kt`

- [x] السطر 272 `androidx.compose.ui.graphics.Color.White` → semantic content
- [x] السطر 280 `androidx.compose.ui.graphics.Color.White` → semantic content

---

# 31. Trash

## `TrashFilterToolbar.kt`

- [x] السطر 93 `if (isSelected) Color.White ...` → selection content semantic
- [x] عدم استخدام White raw.

---

# 32. Settings

## `BackupResetConfirmationFlow.kt`

- [x] السطر 18 حذف `RESET_BTN_TEXT_COLOR = Color.White`
- [x] استبداله بـsemantic action content داخل Composable.

---

# 33. Security

## `SecurityActivePanel.kt`

- [x] السطر 46 حذف `SWITCH_THUMB_COLOR = Color.White`
- [x] استخدام Material/semantic switch thumb role.

## `LockKeypadViews.kt`

- [x] السطر 37 إزالة `Color.White.copy(0.06/0.12)`
- [x] استخدام `securityKeyBackground`
- [x] السطر 38 إزالة `Color.White.copy(0.8)` و`Color.White`
- [x] استخدام `securityKeyContent`
- [x] السطر 50 إزالة `Color.White.copy(0.08)`
- [x] استخدام `securityKeyBorder` أو container role.

## `PasscodeDotIndicators.kt`

- [x] السطر 54 `EmeraldPrimary` → `securityIndicatorFilled`
- [x] السطر 54 `Color.White.copy(0.12)` → `securityIndicatorEmpty`
- [x] السطر 57 `EmeraldPrimary` → `securityIndicatorFilled`
- [x] السطر 57 `Color.White.copy(0.25)` → `securityIndicatorEmpty`
- [x] عدم استخدام brand primitive مباشرة.

## `RecoveryPhraseContent.kt`

- [x] السطر 93 `Color.White` → `securityForeground`
- [x] السطر 102 `Color.White.copy(0.7)` → `securityForegroundMuted`
- [x] السطر 115 `Color.White.copy(0.6)` → `securityForegroundMuted`
- [x] السطر 118 `Color.White.copy(0.3)` → `securityInputBorder`
- [x] السطر 120 `Color.White.copy(0.6)` → `securityForegroundMuted`
- [x] السطر 121 `Color.White` → `securityForeground`
- [x] السطر 122 `Color.White` → `securityForeground`
- [x] السطر 163 `Color.White` → security foreground/content
- [x] السطر 185 `Color.White` → security foreground/content
- [x] السطر 201 `Color.LightGray` → security muted content
- [x] السطر 206 `Color.LightGray` → security muted content
- [x] لا توجد أي `isDark` color branches في الملف.

---

# 34. PDF Architecture

# 34.1 `PdfColors.kt`

الملف:

`app/src/main/java/com/example/data/serialization/pdf/PdfColors.kt`

## ممنوع

أن يبقى `PdfColors` هو مصدر الحقيقة.

## الوضع المطلوب

`PdfColors` يصبح **Adapter / Mapping Layer** فقط.

## العناصر التي يجب إزالة HEX المباشر منها

- [x] السطر 25 `#0F4C43`
- [x] السطر 27 `#2C3E50`
- [x] السطر 29 `#FFFFFF`
- [x] السطر 31 `#CBD5E1`
- [x] السطر 33 `#1E293B`
- [x] السطر 35 `#64748B`
- [x] السطر 37 `#0F172A`
- [x] السطر 39 `#334155`
- [x] السطر 41 `#475569`
- [x] السطر 43 `#1E3A8A`
- [x] السطر 45 `#FEF2F2`
- [x] السطر 47 `#B91C1C`
- [x] السطر 49 `#F0FDF4`
- [x] السطر 51 `#156534`
- [x] السطر 53 `#F8FAFC`
- [x] السطر 55 `#F8FAFC`
- [x] السطر 57 `#E2E8F0`
- [x] السطر 59 `#F8FAFC`
- [x] السطر 61 `#F1F5F9`
- [x] السطر 63 `#FEE2E2`
- [x] السطر 65 `#DCFCE7`

---

# 35. PDF Semantic Mapping

أنشئ mapping صريح:

## Header

- [x] `PRIMARY_EMERALD` → brand/document primary
- [x] `HEADER_BG` → document header background
- [x] `HEADER_TEXT` → document header text
- [x] `HEADER_BORDER` → borderStrong

## General text

- [x] `TEXT_CHARCOAL` → contentPrimary
- [x] `TEXT_MUTED_GREY` → contentSecondary
- [x] `TEXT_DARK` → contentPrimary
- [x] `TEXT_MEDIUM` → contentSecondary
- [x] `TEXT_LIGHT` → contentTertiary

## Financial

- [x] `NET_DEBT_BLUE` → dedicated net-debt semantic role
- [x] `OWED_BG` → debtContainer
- [x] `OWED_TEXT` → debt
- [x] `PAYMENT_BG` → creditContainer
- [x] `PAYMENT_TEXT` → credit

## Rows

- [x] `FOREIGN_ROW_BG` → surfaceVariant
- [x] `CARD_BG` → appSurfaceContainer
- [x] `ROW_DIVIDER` → borderVariant
- [x] `ALT_ROW_BG` → alternate surface role
- [x] `TOTALS_ROW_BG` → totals surface role
- [x] `BANNER_OWED_BG` → debtContainer
- [x] `BANNER_PAYMENT_BG` → creditContainer

---

# 36. Document Palette — Dark Mode Rule

PDF لا يتبع Dark UI.

أنشئ:

`MizanDocumentColors`

ويجب أن تكون:

- [x] مرتبطة بنفس الهوية المركزية.
- [x] مستقلة عن Dark UI rendering.
- [x] عالية التباين للطباعة.
- [x] صالحة على white paper.
- [x] غير معتمدة على Neon colors منخفضة القراءة.

## قاعدة إلزامية

إذا كان التطبيق Dark Mode:

- [x] Compose → Dark MizanColors
- [x] PDF → MizanDocumentColors print-safe

لا تستخدم `isSystemInDarkTheme()` لتحديد ألوان PDF.

---

# 37. `PdfStatementTotalsRenderer.kt`

- [x] السطر 152 إزالة `#FCA5A5`
- [x] السطر 152 استخدام `debtBorder`
- [x] السطر 152 إزالة `#86EFAC`
- [x] السطر 152 استخدام `creditBorder`
- [x] السطر 152 عدم تكرار `PdfColors.HEADER_BORDER` إن تم تحويله للـdocument semantic mapping

- [x] السطر 212 إزالة `#F8FAFC`
- [x] السطر 212 استخدام surface semantic
- [x] السطر 216 إزالة `#CBD5E1`
- [x] السطر 216 استخدام border semantic

---

# 38. `PdfPaints.kt`

## إلزامي

- [x] إزالة أي palette ثانية تحتوي على HEX.
- [x] تحويل كل `PdfColors.TEXT_*` إلى document roles.
- [x] تحويل كل `PdfColors.OWED_*` إلى debt roles.
- [x] تحويل كل `PdfColors.PAYMENT_*` إلى credit roles.
- [x] تحويل row backgrounds إلى document surface roles.
- [x] تحويل borders إلى document border roles.

---

# 39. `PdfPageRenderer.kt`

- [x] Header background → document brand/header role
- [x] Header text → document on-brand role
- [x] Header border → document borderStrong
- [x] Main text → document contentPrimary
- [x] Secondary text → document contentSecondary
- [x] Muted text → document contentTertiary
- [x] عدم إنشاء HEX جديد داخل renderer.
- [x] عدم استدعاء `Color.parseColor("#...")` لقيمة جديدة.

---

# 40. `MasterBookletPdfEngine.kt`

- [x] السطر 110 إزالة الاعتماد المباشر على `PdfColors.PRIMARY_EMERALD` كـsource of truth.
- [x] ربط default بالـdocument brand color adapter.
- [x] السطور التي تستخدم `primaryColorHex` يجب أن تحصل على القيمة من adapter مركزي.
- [x] عدم إنشاء color string جديد داخل engine.

---

# 41. PDF Customer / Transaction Renderers

## `PdfCustomerSummaryRenderer.kt`

- [x] حذف أي raw Color/HEX.
- [x] ربط text → document content roles.
- [x] ربط debt → debt roles.
- [x] ربط credit → credit roles.
- [x] ربط borders → document border roles.

## `PdfTransactionRowRenderer.kt`

- [x] حذف أي raw Color/HEX.
- [x] row background → document surface role.
- [x] transaction credit → credit roles.
- [x] transaction debt → debt roles.
- [x] text → document content roles.
- [x] borders → document border roles.

---

# 42. Canvas Color Conversion

عند استخدام Android Canvas/PDF:

- [x] لا تمرر `Compose Color` إلى API غير متوافق مباشرة.
- [x] إنشاء تحويل مركزي من semantic/document token إلى `android.graphics.Color` أو ARGB.
- [x] لا تستخدم سلسلة:
  `Color → HEX → parseColor`
  إذا كان يمكن الوصول مباشرة إلى ARGB.
- [x] إذا كان renderer الحالي يتطلب HEX، اجعل التحويل في adapter واحد فقط.
- [x] لا تنشئ conversion logic في كل renderer.

---

# 43. XML Resources

## `res/values/colors.xml`

- [x] فحص `purple_200`
- [x] فحص `purple_500`
- [x] فحص `purple_700`
- [x] فحص `teal_200`
- [x] فحص `teal_700`
- [x] فحص `black`
- [x] فحص `white`
- [x] `background_deep`
- [x] `surface_deep`
- [x] `border_soft`

لكل عنصر:

- [x] إذا لم توجد references فعلية → حذف.
- [x] إذا وجدت references فعلية → تحويلها إلى architecture المركزية أو الإبقاء فقط إذا كان Android Resource requirement حقيقياً.

## `res/values-night/colors.xml`

- [x] مراجعة `background_deep`
- [x] مراجعة `surface_deep`
- [x] مراجعة `border_soft`
- [x] حذف غير المستخدم.
- [x] عدم إبقاء palette موازية لـCompose.

---

# 44. قواعد Naming النهائية

## ممنوع كـUI API

- [x] `EmeraldPrimary`
- [x] `EmeraldLight`
- [x] `EmeraldDark`
- [x] `SoftRed`
- [x] `SoftGreen`
- [x] `CreditGreen`
- [x] `DebtRed`
- [x] `CreditGreenDark`
- [x] `DebtRedDark`
- [x] `White15`
- [x] `White20`
- [x] `White40`
- [x] `Slate500`
- [x] `NeonGreen`
- [x] `LightRedTint`

## مسموح

- [x] `brandPrimary`
- [x] `credit`
- [x] `creditContainer`
- [x] `debt`
- [x] `debtContainer`
- [x] `selection`
- [x] `headerForeground`
- [x] `headerForegroundMuted`
- [x] `headerControlContainer`
- [x] `floatingControlBackground`
- [x] `contentPrimary`
- [x] `contentSecondary`
- [x] `contentTertiary`
- [x] `border`
- [x] `error`
- [x] `warning`
- [x] `success`
- [x] `info`

---

# 45. Forbidden Patterns — Final Static Rules

بعد التنفيذ يجب ألا يوجد داخل UI:

- [x] `Color(0x`
- [x] `Color.White`
- [x] `Color.Black`
- [x] `Color.Gray`
- [x] `Color.LightGray`
- [x] `Color.DarkGray`
- [x] `Color.White.copy(`
- [x] `Color.Black.copy(`
- [x] `"#`
- [x] `parseColor("#`
- [x] `if (isDark) Color`
- [x] `if (darkTheme) Color`
- [x] `when (isDark)` لاختيار اللون
- [x] local `val ...Color = Color...` داخل Components

الاستثناءات المسموح بها:

- [x] Primitive Palette داخل `Color.kt`
- [x] Adapter مركزي موثق للـPDF/Canvas
- [x] `Color.Transparent` عندما تكون الشفافية المطلقة مقصودة فعلياً.

---

# 46. Semantic Mapping Matrix

| الاستخدام | Token |
|---|---|
| App background | `appBackground` |
| Standard surface | `appSurface` |
| Elevated surface | `appSurfaceContainerHigh` |
| Main text | `contentPrimary` |
| Secondary text | `contentSecondary` |
| Muted text | `contentTertiary` |
| Disabled | `contentDisabled` |
| Border | `border` |
| Subtle border | `borderVariant` |
| Strong border | `borderStrong` |
| Application brand | `brandPrimary` |
| Brand foreground | `onBrandPrimary` |
| Credit/علينا | `credit` |
| Credit container | `creditContainer` |
| Credit foreground | `onCreditContainer` / `onCredit` حسب الحاوية |
| Debt/لنا | `debt` |
| Debt container | `debtContainer` |
| Debt foreground | `onDebtContainer` / `onDebt` |
| Selection | `selection` |
| Selection foreground | `onSelection` |
| Header text | `headerForeground` |
| Header muted text | `headerForegroundMuted` |
| Header translucent control | `headerControlContainer` |
| Floating UI background | `floatingControlBackground` |
| Floating UI foreground | `floatingControlContent` |
| Dialog scrim | `dialogScrim` |
| Input border | `inputBorder` |
| Focused input border | `inputBorderFocused` |
| Success | `success` |
| Warning | `warning` |
| Error | `error` |
| Info | `info` |
| Security foreground | `securityForeground` |
| Security keypad | `securityKeyBackground` |
| PDF primary | document brand |
| PDF main text | document contentPrimary |
| PDF muted text | document contentSecondary |
| PDF credit | document credit |
| PDF debt | document debt |

---

# 47. تنفيذ مرحلي إلزامي

## Batch A — Theme Core

- [x] إعادة هيكلة `Color.kt`
- [x] إنشاء `MizanColors`
- [x] إنشاء Light semantic colors
- [x] إنشاء Dark semantic colors
- [x] إنشاء `LocalMizanColors`
- [x] إنشاء `MaterialTheme.mizanColors`
- [x] إعادة بناء `Theme.kt`
- [x] إزالة Color literals من ColorScheme
- [x] ربط Ripple
- [x] إزالة الألوان القديمة غير المستخدمة من theme

## Batch B — Shared UI + Calculator + Cloud

- [x] `AppNavigationDrawer.kt`
- [x] `ExitConfirmDialog.kt`
- [x] `CurrencyRevalueConfirmDialog.kt`
- [x] `WelcomeOnboardingDialog.kt`
- [x] `DeveloperSealFooter.kt`
- [x] `CalculatorDialog.kt`
- [x] `CloudStatsHeader.kt`
- [x] `CloudBottomActionBar.kt`
- [x] `CloudBackupDialogs.kt`
- [x] Build check
- [x] Hardcoded color scan

## Batch C — Business + Habayeb

- [x] Business Profile files
- [x] Habayeb header files
- [x] Customer rows
- [x] Customer dialogs
- [x] Customer filters
- [x] Floating controls
- [x] Transaction popups
- [x] Build check
- [x] Hardcoded color scan

## Batch D — Ledger

- [x] `MainLedgerHeader.kt`
- [x] `MainLedgerSelectionBar.kt`
- [x] Day cards
- [x] Commitment cards
- [x] Ledger dialogs
- [x] Activation UI
- [x] Bottom dock
- [x] Build check
- [x] Hardcoded color scan

## Batch E — Security

- [x] Security active panel
- [x] Lock keypad
- [x] Passcode indicators
- [x] Recovery phrase
- [x] Build check
- [x] Hardcoded color scan

## Batch F — PDF / Canvas

- [x] `PdfColors.kt`
- [x] `PdfPaints.kt`
- [x] `PdfPageRenderer.kt`
- [x] `PdfStatementTotalsRenderer.kt`
- [x] `MasterBookletPdfEngine.kt`
- [x] `PdfCustomerSummaryRenderer.kt`
- [x] `PdfTransactionRowRenderer.kt`
- [x] Document palette
- [x] Canvas/PDF adapter
- [x] Build check
- [x] HEX scan

## Batch G — Resources + Final Audit

- [x] `colors.xml`
- [x] `colors-night.xml`
- [x] Dead color resources deleted
- [x] Final repository color scan
- [x] Light mode visual audit
- [x] Dark mode visual audit
- [x] PDF print audit
- [x] Final Gradle build
- [x] Final checklist review

---

# 48. Definition of Done

## Architecture

- [x] Primitive palette has a single source.
- [x] Semantic UI colors have a single source.
- [x] Material 3 roles are provided through `ColorScheme`.
- [x] App-specific semantic roles are provided through `MizanColors`.
- [x] Light/Dark resolution occurs centrally.
- [x] UI Components do not resolve Light/Dark colors themselves.

## Hardcoded Color Removal

- [x] Zero `Color(0x...)` in UI Components.
- [x] Zero `Color.White` in UI Components.
- [x] Zero `Color.Black` in UI Components.
- [x] Zero `Color.Gray` in UI Components.
- [x] Zero `Color.LightGray` in UI Components.
- [x] Zero `Color.DarkGray` in UI Components.
- [x] Zero `Color.White.copy(...)` in UI Components.
- [x] Zero HEX literals in UI Components.
- [x] Zero HEX literals in PDF renderers.
- [x] Zero duplicated local color constants representing global theme roles.

## Dark/Light

- [x] No repeated `if (isDark) colorA else colorB` for theme colors.
- [x] No repeated `if (darkTheme)` color selection in UI.
- [x] Header colors work in both modes.
- [x] Credit colors work in both modes.
- [x] Debt colors work in both modes.
- [x] Selection colors work in both modes.
- [x] Dialog colors work in both modes.
- [x] Floating bars work in both modes.
- [x] Security UI works in both modes.

## Financial semantics

- [x] Credit/علينا has one semantic color family.
- [x] Debt/لنا has one semantic color family.
- [x] Credit container is centralized.
- [x] Debt container is centralized.
- [x] Credit border is centralized.
- [x] Debt border is centralized.
- [x] Credit gradients are centralized.
- [x] Debt gradients are centralized.
- [x] Selection has one semantic family.

## PDF

- [x] PDF does not maintain a separate unrelated brand palette.
- [x] PDF uses document semantic mappings.
- [x] PDF remains print-safe.
- [x] PDF remains light/print-oriented even when UI is dark.
- [x] PDF has no direct HEX literals outside the central adapter if absolutely required by an external API.
- [x] Canvas/PDF conversion is centralized.
- [x] PDF text contrast remains readable.
- [x] Financial colors remain visually distinguishable in print.

## Resources

- [x] Unused `colors.xml` entries removed.
- [x] Unused `colors-night.xml` entries removed.
- [x] No duplicate Compose/XML source of truth remains.

## Build

- [x] Project compiles successfully.
- [x] No new unresolved imports.
- [x] No unused theme constants causing lint debt.
- [x] No type conflict between Compose Color and Android graphics Color.
- [x] No regression in PDF export compilation.

---

# 49. Final Mandatory Verification Commands

Gemini must execute repository-wide searches equivalent to:

- [x] Search `Color(0x`
- [x] Search `Color.White`
- [x] Search `Color.Black`
- [x] Search `Color.Gray`
- [x] Search `Color.LightGray`
- [x] Search `Color.DarkGray`
- [x] Search `Color.White.copy`
- [x] Search `Color.Black.copy`
- [x] Search `#RRGGBB`
- [x] Search `#AARRGGBB`
- [x] Search `parseColor(`
- [x] Search `if (isDark) Color`
- [x] Search `if (darkTheme) Color`
- [x] Search old names:
  `EmeraldPrimary`
  `SoftRed`
  `SoftGreen`
  `CreditGreen`
  `DebtRed`
  `CreditGreenDark`
  `DebtRedDark`
  `SelectionGreen`
  `NeonGreen`
  `NeonCyan`

## Expected result

- [x] No forbidden direct UI colors remain.
- [x] Remaining primitive values exist only in the central palette.
- [x] Remaining PDF conversions exist only in the central document adapter.
- [x] Every rejected/remaining direct color has an explicit documented architectural reason.

---

# 50. Final Phase Gate

**Do not move to Phase 2 until all boxes below are checked:**

- [x] `Color.kt` refactored.
- [x] `Theme.kt` refactored.
- [x] `MizanColors` operational.
- [x] `LocalMizanColors` operational.
- [x] `MaterialTheme.mizanColors` operational.
- [x] All listed UI files refactored.
- [x] All listed PDF files refactored.
- [x] XML colors audited.
- [x] Zero unintended hardcoded UI colors.
- [x] Zero duplicated Light/Dark color selection in Components.
- [x] Credit semantic family verified.
- [x] Debt semantic family verified.
- [x] Selection semantic family verified.
- [x] Header semantic family verified.
- [x] Dialog semantic family verified.
- [x] Floating semantic family verified.
- [x] Security semantic family verified.
- [x] PDF document palette verified.
- [x] PDF print contrast verified.
- [x] Light Mode verified.
- [x] Dark Mode verified.
- [x] Full build verified.
- [x] Final static scan verified.
- [x] This document updated with `[x]` for every completed item.

---

# 51. Architectural End State

The only acceptable dependency direction after Phase 1 is:

```text
Primitive Palette
       ↓
MizanColors / Material ColorScheme
       ↓
MaterialTheme
       ↓
Compose Components / Screens

and

Primitive Palette
       ↓
MizanColors
       ↓
MizanDocumentColors
       ↓
PDF / Canvas Renderers

and

Forbidden architecture 
Screen → HEX
Screen → Color.White
Screen → Light/Dark Color Branch
Screen → old palette primitive
PDF → independent HEX palette
Renderer → parseColor("#...")

Completion rule:
الهوية اللونية المستقبلية يجب أن يمكن تغييرها من الطبقة المركزية دون فتح ملفات الشاشات والمكونات والـPDF لتعديل ألوانها يدوياً.

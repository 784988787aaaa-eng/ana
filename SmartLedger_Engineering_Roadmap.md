# خارطة الطريق الهندسية — SmartLedger

## الهدف النهائي
تثبيت تطبيق مالي محلي بحدود طبقات واضحة، ومصادر حقيقة موحدة، واعتماديات صريحة، مع الحفاظ الكامل على السلوك والواجهة قبل الإطلاق.

## المبادئ الهندسية الثابتة
- `FinanceApplication` هو جذر تركيب الاعتماديات ولا يمثل Service Locator عاماً.
- `AppContainer` يبني الاعتماديات طويلة العمر ولا يكشف `AppDatabase`.
- UI يعرض الحالة ويرسل الأحداث ولا يبحث عن Repositories عبر `Context` أو `Application`.
- ViewModels تتلقى Repositories وUseCases صراحة عبر مصنع موحد.
- طبقة البيانات تملك Room وDAO وحدود المعاملات.
- محركات PDF/Excel تتلقى بيانات الأعمال المطلوبة صراحة؛ `Context` مخصص لموارد Android والرسم والملفات فقط.
- لا توجد مصادر حقيقة انتقالية لبيانات الأعمال قبل الإطلاق.

## الدفعة الأولى
تشمل التنظيفات الأساسية السابقة للبنية ومصادر البيانات كما بقيت مثبتة في الحزمة الحالية.

## الدفعة الثانية — مغلقة وفق الملفات الحالية
- حذف البقايا القديمة: `HabayebCategoryManager` و`HabayebPreferencesStore` و`FinanceRepository` و`AppSettingsRepository` و`HabayebRecurringManager`.
- التصنيفات والتثبيت والـ recurring وBusiness Profile أصبحت تعتمد على Room في المسارات الفعلية الحالية.
- `BusinessProfile` مصدر حقيقته جدول `business_profile` عبر `BusinessProfileRepository`.
- recurring يستخدم `RecurringConfigEntity` و`RecurringConfigDao` و`RecurringRepository`، والتنفيذ الذري يتم داخل `Room.withTransaction`.
- UI لا يستخدم `SharedPreferences` مباشرة؛ الاستخدام المتبقي محصور في `FloatingUiPreferencesRepository`، والأمن في `AppSecurityManager` عبر `EncryptedSharedPreferences`.
- لا يوجد `android.*` داخل `domain/`، ولا `printStackTrace(` أو `TODO` أو `FIXME` في كود الإنتاج وفق البحث النهائي الحالي.

# الدفعة الثالثة — إغلاق تركيب الاعتماديات والاعتماديات الخفية

## المشكلة التي عولجت
كانت إعادة الهيكلة السابقة قد نقلت التركيب إلى `AppContainer`، لكن بقيت بقايا وصول قديمة ومكسورة إلى `FinanceApplication.data` داخل PDF/serialization. كما كانت بعض مولدات PDF/Excel تعتمد على `Context` بطريقة تخفي مصدر بيانات Business Profile.

## وضع Composition Root النهائي
- `FinanceApplication` ينشئ `AppContainer` مرة واحدة في `onCreate`.
- لا يوجد عضو `data` في `FinanceApplication`.
- لا يوجد `appContainer()` مكشوف بعد إغلاقه لعدم وجود مستهلك مشروع له.
- `AppContainer` يحتفظ بـ `AppDatabase` كحقل خاص ويبني Repositories وUseCase طويلة العمر.
- استخدام `AppContainer` محصور فعلياً في `FinanceApplication` و`AppViewModelFactory`.

## ViewModel creation
- `AppViewModelFactory` هو نقطة تركيب ViewModels الموحدة.
- ViewModels تتلقى الاعتماديات المطلوبة صراحة ولا تستخرج Repositories من `Application`.
- `MainActivity` يحصل على المصنع عند نقطة إنشاء UI، وهي نقطة تركيب Android، وليس مسار وصول للبيانات من Composable أو ViewModel.
- لا يوجد وصول مباشر من ViewModels إلى `AppDatabase` أو DAO في المسارات المدققة.

## WorkerFactory وWorkers
- `TrashCleanupWorker` يتلقى `SettingsRepository` و`TrashRepository` من `WorkerFactory` داخل `FinanceApplication`.
- لا ينشئ Worker `AppDatabase` ولا يصل إلى DAO مباشرة ولا يستخدم `FinanceApplication.data`.
- سياسة النتيجة الحالية: `IOException` مؤقت يؤدي إلى `retry`، والأخطاء الأخرى تؤدي إلى `failure`، والنجاح لا يخفي خطأ قاعدة بيانات داخلياً.

## PDF وBusinessProfileLoader
- `BusinessProfileLoader.load` يتطلب `BusinessProfile` صراحة.
- لا يوجد overload انتقالي يعتمد على `Context` وحده.
- `Context` داخل loader يستخدم فقط للموارد وتحميل الشعار.
- لا يصعد loader عبر `Context → Application → Container → Repository`.
- `PdfReportGenerator` يتلقى `BusinessProfile` صراحة في مسارات تقرير العميل والتقرير الشامل.
- `MasterBookletPdfEngine` يتلقى `BusinessProfile` وبيانات المعاملات المطلوبة صراحة ولا يستخدم `FinanceApplication` أو `AppContainer` للحصول على البيانات.

## Excel وSerialization
- `SingleCustomerExcelEngine` و`AllCustomersExcelEngine` يتلقيان `BusinessProfile` صراحة.
- `CsvReportGenerator` يمرر `BusinessProfile` صراحة إلى محركات Excel.
- `CustomerHistoryShareBottomSheet` يستقبل Business Profile كبيانات UI صريحة ويستخدمها لطلبات PDF/Excel.
- `MainAppContent` يملك تدفق Business Profile عبر `BusinessProfileViewModel` ويمرره إلى `HabayebScreen` ثم إلى مسار تاريخ العميل؛ لا يوجد بحث عن Repository من Composable.

## Business Profile source of truth
- الشاشة تستخدم `BusinessProfileViewModel` و`BusinessProfileRepository`.
- PDF وExcel يستخدمان نفس كيان `BusinessProfile` القادم من الحالة المملوكة لـ ViewModel.
- لا توجد مفاتيح Business Profile قديمة أو fallback إلى `SharedPreferences` في البحث النهائي الحالي.

## AppDatabase visibility
- `AppDatabase.getDatabase(` يظهر داخل `AppContainer` فقط في جذر تركيب البيانات.
- `AppDatabase` غير مكشوف كعضو من `AppContainer`.
- لا يوجد مسار `ViewModel → AppContainer → AppDatabase`.

## Recurring وTrash
- `RecurringRepository.executeDue` يبقى داخل معاملة Room واحدة لإنشاء المعاملات وتحديث `lastExecutedTimestamp` معاً.
- التنفيذ يستخدم ثواني Unix في `HabayebTransaction.timestamp` و`lastExecutedTimestamp`، بينما حقول `startDateMillis` و`endDateMillis` بالمللي ثانية؛ التحويل موجود صراحة داخل `RecurringRepository`.
- لا يوجد تغيير غير لازم على سلوك recurring في هذه الدفعة.
- `TrashCleanupWorker` لا يكسر حدود Repository.
- `linkedMainTxId` وعمليات Trash/Restore لم تُعد صياغتها في هذه الدفعة.

## Pin scope
- التمثيل الداخلي للنطاق العام محصور في `HabayebCategoryDataRepository.GLOBAL_SCOPE_CATEGORY_ID`، ولا يوجد بحث مباشر في UI عن الرقم السحري `0` لنطاق التثبيت.

## تنظيف ساكن إضافي
- أزيلت aliases الذاتية المتعارضة التي كانت مرتبطة بـ `PdfReportGenerator` في الحزمة السابقة.
- لا يوجد `DataDependencies`.
- لا يوجد `FinanceApplication.data`.
- لا يوجد `appContainer()` منتشر أو مكشوف.
- لا توجد wrappers توافقية لـ `BusinessProfileLoader.load(context)`.
- لم تُنشأ مكتبة DI جديدة ولم يُستخدم Hilt أو Koin أو Dagger.

## Manifest والصلاحيات
- `INTERNET` أزيلت بعد عدم العثور على استخدام شبكي فعلي في كود الإنتاج الحالي.
- `READ_CONTACTS` مستخدمة في اختيار جهات الاتصال.
- `POST_NOTIFICATIONS` و`WRITE_EXTERNAL_STORAGE` لهما مسارات فحص وطلب في إعدادات النسخ الحالية.
- `VIBRATE` مستخدمة في مسارات اللمس والحماية.
- `allowBackup` يبقى `false`.

## الاختبارات
توجد اختبارات منطقية حالية لـ `FinancialPolicy` و`RecurringConfig` و`HashUtils` و`BigDecimalConverter` وSchema وTrash وحسابات الحبايب. لم يُشغّل أي اختبار في هذه البيئة، ولم يُدّع نجاح تنفيذها.

## نتائج البحث النهائي
تمت مراجعة النتائج الفعلية التالية:
- `FinanceApplication.data`: لا نتائج.
- `as FinanceApplication`: نتيجة واحدة في `MainActivity` للحصول على `AppViewModelFactory` عند إنشاء UI.
- `applicationContext as FinanceApplication`: لا نتائج.
- `DataDependencies`: لا نتائج.
- `appContainer()`: لا نتائج بعد إزالة API غير المستخدمة.
- `AppDatabase.getDatabase(`: محصور في `AppContainer`.
- `BusinessProfileLoader.load(context)`: لا نتائج؛ جميع الاستدعاءات تمرر `BusinessProfile` صراحة.
- `getSharedPreferences(`: محصور في `FloatingUiPreferencesRepository`.
- `SharedPreferences`: المصدر المتخصص للعناصر العائمة و`EncryptedSharedPreferences` داخل الأمن.
- `printStackTrace(` و`TODO` و`FIXME`: لا نتائج في كود الإنتاج.
- `println(` و`System.out`: لا نتائج في كود الإنتاج.
- `HabayebRecurringManager` و`FinanceRepository` و`AppSettingsRepository` و`HabayebCategoryManager` و`HabayebPreferencesStore`: لا نتائج في كود الإنتاج.
- `import android.` داخل `domain/`: لا نتائج.
- `AppDatabase` و`withTransaction` داخل `domain/usecase`: لا نتائج في المسارات المدققة.
- مفاتيح Business Profile القديمة وRecurring JSON legacy: لا نتائج فعلية في البحث الحالي.

## التحقق الساكن
راجعت الملفات المعدلة وتواقيع constructors والاستدعاءات المتأثرة ومسارات تمرير `BusinessProfile` عبر PDF وExcel، إضافة إلى imports المتكررة والـ Application casts ومسارات `AppContainer`.

## Gradle والبناء
لم يُشغّل Gradle عمداً. لم تُشغّل `build` أو `test` أو `check` أو أي أمر Gradle. السبب المعروف في البيئة هو:

`UnknownHostException: services.gradle.org`

لم تُغيّر Gradle Wrapper أو Gradle أو AGP أو repositories أو dependencies، ولم يُغيّر `versionCode = 1` أو `versionName = "1.0"`.

## نقطة التوقف الدقيقة
الدفعة الثالثة أغلقت نطاقها المعماري القابل للتحقق من الحزمة الحالية: تركيب الاعتماديات، إزالة `DataDependencies`، إخفاء قاعدة البيانات داخل التركيب، إنشاء ViewModels، Worker dependencies، وإزالة الاعتماديات الخفية من PDF/Excel/serialization ومسارات Business Profile.

## القرار النهائي للدفعة الثالثة
**مكتملة وفق الملفات الناتجة والتحقق الساكن المنفذ.**

لا توجد نتيجة Build أو Test تنفيذية بسبب القيد الشبكي، ولذلك لا يُدّعى نجاح التجميع أو الاختبارات.

# الدفعة الرابعة — تدقيق سلامة التجميع وتثبيت العقود

## نطاق الإغلاق الفعلي
- أُعيد فحص الحزمة الناتجة نفسها دون الاعتماد على تقارير سابقة.
- لم يُشغّل Gradle أو أي مهمة Gradle، ولم تُغيّر إعداداته أو `versionCode` أو `versionName`. سبب عدم التشغيل في البيئة هو `UnknownHostException: services.gradle.org`.

## Compile Integrity والعقود
- روجعت الاستدعاءات والتواقيع المتأثرة لمسارات PDF وExcel وBusiness Profile و`AppContainer` و`FinanceApplication` و`AppViewModelFactory`.
- أزيلت overloads النصية الانتقالية في `PdfReportGenerator` لصالح `PdfAction` typed contract، وأزيل overload كان ينشئ `CoroutineScope(Dispatchers.Main)` بلا مالك lifecycle واضح.
- جميع الاستدعاءات الحالية لمسارات PDF المتأثرة تستخدم `PdfAction` صراحة.
- صُحح import `PdfAction` في `CustomerHistoryShareBottomSheet` إلى الحزمة الفعلية `data.serialization.pdf`.
- `CustomerHistoryShareBottomSheet` لم يعد ينشئ `CoroutineScope` مستقلاً؛ يستخدم `rememberCoroutineScope()` المرتبط بعمر الـ Composable.

## Coroutine ownership
- لا يوجد `GlobalScope` أو `runBlocking`.
- لا يوجد `CoroutineScope(Dispatchers.Main)` مستقل في مسار PDF الذي كان متبقياً.
- عمليات PDF غير المتزامنة تستقبل scope من المستدعي، وتنفذ التوليد على `Dispatchers.IO` ثم تعود إلى Main للنتيجة/التأثير الحالي.
- محركات PDF نفسها لا تستخدم `Application` أو `AppContainer` لاستخراج بيانات الأعمال.

## PDF وBitmap وملفات الإخراج
- بقي `BusinessProfile` اعتماداً صريحاً في جميع استدعاءات `BusinessProfileLoader` الفعلية في PDF وExcel.
- `BusinessProfileLoader` لا يصل إلى Repository أو `AppContainer` أو `FinanceApplication`، و`Context` فيه مخصص للموارد وتحميل الشعار.
- أضيف تنظيف صريح للـ Bitmap في مسارات الاستثناء داخل `PdfDrawingUtils.loadAndScaleLogo` لمنع بقاء raw/scaled bitmap عند فشل التحميل أو القياس.
- `PdfReportGenerator` و`MasterBookletPdfEngine` يستخدمان `finally`/`use` لإغلاق `PdfDocument` وstreams وتنظيف الشعارات في المسارات الحالية.

## FinanceApplication وWorkManager
- `AppContainer` و`AppViewModelFactory` أصبحا lazy ومتزامنين داخل `FinanceApplication`، مع تهيئتهما في `onCreate`، بحيث لا يعتمد `WorkerFactory` على `lateinit` غير مهيأ عند طلب configuration.
- `WorkerFactory` ما زال يحقن Repositories صراحة إلى `TrashCleanupWorker` ولا ينشئ Database أو DAO داخل Worker.

## AppContainer
- `AppDatabase` ما زال خاصاً ولا يُكشف للطبقات العليا.
- أزيل getter alias غير الضروري `categoryRepository`; أصبح `categories` هو التعرض الوحيد لنفس instance، وتم تحديث `AppViewModelFactory`.
- لم يُحوّل `AppContainer` إلى Service Locator عام.

## AppViewModelFactory وtype safety
- بقي `UNCHECKED_CAST` في `AppViewModelFactory` محصوراً في نمط `ViewModelProvider.Factory` القياسي.
- أزيلت casts غير الآمنة و`UNCHECKED_CAST` من تجميع `HabayebFinanceViewModel` عبر تدفق typed وسيط `CategoryUiData` بدلاً من `Array<Any>`.
- suppressions المتبقية الخاصة بـ `DEPRECATION` وAndroid compatibility لم تُوسّع.

## Recurring وRoom ووحدات الزمن
- `HabayebTransaction.timestamp` و`lastExecutedTimestamp` يستخدمان ثواني Unix في مسار recurring، بينما `startDateMillis` و`endDateMillis` بالمللي ثانية.
- عُزل التحويل داخل `RecurringRepository` واستُخدم ضرب `lastExecutedTimestamp` في 1000 فقط عند بناء تاريخ Calendar.
- صُحح منطق الاستئناف بعد `lastExecutedTimestamp`: يبدأ المسح من اليوم التالي لآخر تنفيذ بدلاً من إعادة تطبيع اليوم نفسه إلى منتصف الليل، ما يمنع إعادة تنفيذ occurrence نفسها.
- إنشاء معاملات recurring وتحديث `lastExecutedTimestamp` ما زالا داخل `database.withTransaction` واحدة؛ فشل المعاملة لا يثبت timestamp جديداً منفرداً.

## Pin scope
- تم الحفاظ على invariant: النطاق العام يُمثل داخلياً فقط بواسطة `HabayebCategoryDataRepository.GLOBAL_SCOPE_CATEGORY_ID`، والتحويل محصور في `scopeCategoryId`.
- أضيف اختبار منطقي لتحويل `null` إلى النطاق العام وللحفاظ على نطاق category الصريح.

## الاختبارات المضافة
- `PdfActionTest`: parsing غير الحساس لحالة الأحرف وfallback للقيمة غير المعروفة.
- `HabayebCategoryDataRepositoryTest`: عقد تحويل نطاق التثبيت.
- الاختبارات لم تُشغّل لأن Gradle ممنوع في هذه البيئة؛ لا يوجد ادعاء بنجاح تنفيذها.

## Manifest والأذونات والأمن
- `allowBackup=false` باقٍ.
- `READ_CONTACTS` مستخدمة في مسارات جهات الاتصال، و`POST_NOTIFICATIONS` و`VIBRATE` لها استخدامات فعلية، و`WRITE_EXTERNAL_STORAGE` مقيد بـ `maxSdkVersion=28`.
- لم يُضف وصول تخزيني مباشر من UI، والاستخدام العادي لـ `SharedPreferences` ما زال محصوراً في `FloatingUiPreferencesRepository`، بينما بيانات الأمن تستخدم `EncryptedSharedPreferences` في `AppSecurityManager`.
- لم يظهر `printStackTrace`، ولم تُضف سجلات لبيانات مالية أو أسرار في التعديلات.

## نتائج البحث النهائي للدفعة الرابعة
- `FinanceApplication.data`: لا نتائج.
- `DataDependencies`: لا نتائج.
- `applicationContext as FinanceApplication`: لا نتائج.
- `appContainer()`: لا نتائج.
- `AppDatabase.getDatabase(`: محصور في `AppContainer`.
- `GlobalScope`: لا نتائج في كود الإنتاج.
- `runBlocking`: لا نتائج.
- `printStackTrace(`: لا نتائج.
- `TODO` و`FIXME`: لا نتائج في كود الإنتاج.
- `println(` و`System.out`: لا نتائج في كود الإنتاج.
- `BusinessProfileLoader.load`: كل الاستدعاءات الحالية تمرر `BusinessProfile` صراحة.
- `as FinanceApplication`: نتيجة واحدة في `MainActivity` للحصول على `AppViewModelFactory` عند نقطة تركيب Android.
- `CoroutineScope(`: لم يبق إنشاء scope يدوي في المسارات المدققة؛ النتائج الفعلية في UI هي `rememberCoroutineScope()` المرتبط بعمر Compose.
- `scopeCategoryId`: التحويل محصور في `HabayebCategoryDataRepository` وDAO/entity المرتبطين.

## Dead code وStatic audit
- أزيلت overloads PDF الانتقالية التي لم تعد تمثل العقد النهائي، وأزيل alias `categoryRepository` المكرر في `AppContainer`.
- روجعت imports والتواقيع والاستدعاءات المعدلة، بما فيها named arguments و`PdfAction` وfactory dependencies وRecurring.
- لم يُدّع نجاح build أو test؛ هذا الإغلاق قائم على تدقيق ساكن للملفات الناتجة.

## نقطة التوقف والقرار
الدفعة الرابعة مغلقة على الحزمة الناتجة ضمن نطاقها: تم تثبيت العقود المتأثرة، إزالة scope غير مملوك وoverloads انتقالية، تقوية type safety، تصحيح منع تكرار recurring بعد آخر تنفيذ، تدقيق lifecycle للموارد المتأثرة، وإضافة اختبارات منطقية عالية القيمة، مع الحفاظ على الواجهة والإصدارات وإعدادات Gradle دون تغيير.

# الدفعة الخامسة — تقوية قابلية الصيانة وإغلاق مخاطر الصيانة المتبقية

## الحالة العامة والقرار المعماري
- فُحصت الحزمة الفعلية الناتجة من الدفعة الرابعة قبل أي تعديل، ولم تُعامل الخارطة السابقة كدليل على صحة التنفيذ.
- بقيت هذه الدفعة داخل الحدود الهندسية: لا تغيير للواجهة أو المخرجات البصرية، ولا مكتبات جديدة، ولا تغيير Gradle أو الإصدارات.
- `FinanceViewModel` روجع باعتباره مسؤولية متماسكة لتجميع حالة الشاشة المالية والعمليات المرتبطة بها، مع وجود `LedgerViewModel` بالفعل لمسار ledger المتخصص؛ لم يُنشأ ViewModel شكلي جديد لتقليل عدد الأسطر.
- أزيلت من `FinanceViewModel` APIs غير المستخدمة فعلياً (`updateThemeMode` و`calculateSumByType` و`updateCommitmentDirectly`) بعد تتبع المراجع، وأزيلت بقايا تفضيلات navigation الثابتة من هذا ViewModel.

## NavigationPreferences
- كان `NavigationPreferences` يحتوي DataStore وواجهات `saveTabOrder` و`saveDefaultStart` مع `@Suppress("UNUSED_PARAMETER")` رغم أن ترتيب البداية أصبح ثابتاً على `HABAYEB`.
- حُذفت الواجهة الانتقالية وملف `NavigationPreferences.kt` كاملاً بعد فحص جميع الاستدعاءات.
- حُذف state والـ save wrappers التابعة له من `FinanceViewModel`، وأصبح `MainAppLayout` يثبت شاشة البداية الحالية صراحة دون مصدر تخزين ميت.
- لا يوجد `@Suppress("UNUSED_PARAMETER")` متبقٍ بسبب هذا التصميم القديم.

## FinanceViewModel والملفات الكبيرة
- لم يُفكك `FinanceViewModel` حسب عدد الأسطر؛ مسؤولياته الحالية ما زالت مرتبطة بالحالة المالية العامة والتعامل مع settings/trash/commitments/categories والعمليات التي ما زالت لها call sites.
- راجعت الملفات الكبيرة المذكورة في النطاق على أساس cohesion وcoupling، وليس الحجم فقط. الملفات ذات rendering أو UI composition المتماسك لم تُقسّم شكلياً.
- لم تُنقل حسابات الأعمال إلى Composable، ولم يُضف وصول تخزين مباشر إلى UI.
- `Color.kt` لم يُعدّل لأن حجمه ناتج عن تعريفات Theme وليس عن مسؤوليات تشغيلية مختلطة.
- `PdfPageRenderer` و`MasterBookletPdfEngine` بقيا في حدود rendering/generation بعد مراجعة الاعتماديات الصريحة.

## Floating UI persistence
- بقي `SharedPreferences` العادي محصوراً في `FloatingUiPreferencesRepository` فقط، باستخدام `applicationContext` وعدم كشف instance أو المفاتيح للطبقات العليا.
- أضيف `FloatingUiStateNormalizer` كمنطق خالص قابل للاختبار لتثبيت حدود `sizeLevel` واستعادة ratios التالفة أو غير المنتهية إلى قيم افتراضية آمنة.
- أصبحت القراءة والكتابة تمران عبر التطبيع نفسه، ولا تعرف UI مفاتيح التخزين.
- لم تُنقل الحالة إلى Room أو DataStore ولم تُضف كتابة جديدة أثناء كل frame.

## Suppressions
- فُتح كل `@Suppress` متبقٍ في كود الإنتاج.
- أزيل `UNUSED_PARAMETER` المرتبط بـ `NavigationPreferences` المحذوف.
- بقي `UNCHECKED_CAST` واحداً محصوراً في `AppViewModelFactory` ضمن عقد `ViewModelProvider.Factory` القياسي.
- suppressions الخاصة بـ `DEPRECATION` بقيت موضعية في مسارات توافق Android؛ لم تُوسّع ولم تستخدم لإخفاء خطأ معماري.

## PDF وExcel وCoroutine ownership
- أُعيد التحقق من مسارات `PdfReportGenerator` و`MasterBookletPdfEngine` و`PdfPageRenderer` و`BusinessProfileLoader` وcall sites المتأثرة.
- `BusinessProfile` يبقى اعتماداً صريحاً، و`Context` في مسارات PDF لا يستخدم كـ Service Locator للوصول إلى Repository أو `AppContainer`.
- لا يوجد `GlobalScope` أو `runBlocking` أو `CoroutineScope(Dispatchers.Main)` مستقل في كود الإنتاج المدقق.
- `CustomerHistoryShareBottomSheet` يستخدم `rememberCoroutineScope()`؛ لم يُنشأ scope يدوي طويل العمر.
- مسارات PDF الحالية تستمر في استخدام `use`/`finally` لإغلاق streams و`PdfDocument` وتنظيف Bitmap، ولم يُغيّر المخرج البصري.

## Room وRecurring وTrash
- `RecurringRepository.executeDue` بقي داخل `database.withTransaction`: إدراج occurrence وتحديث `lastExecutedTimestamp` لا ينفصلان.
- الوحدات بقيت واضحة في التنفيذ: `startDateMillis` و`endDateMillis` بالمللي ثانية، بينما `HabayebTransaction.timestamp` و`lastExecutedTimestamp` بالثواني؛ التحويل إلى Calendar محصور في recurring.
- استمرار المسح من اليوم التالي لآخر occurrence يمنع إعادة تنفيذ occurrence نفسها.
- روجع `TrashItemParser`: parsing يعزل أخطاء العنصر التالف ويعيد بيانات آمنة لمسار العرض بدلاً من إسقاط الشاشة بسبب عنصر واحد؛ لم يُنقل parsing إلى UI.

## Repository وUI وDomain
- لم يظهر وصول `AppDatabase` أو DAO حقيقي من `FinanceViewModel` أو بقية ViewModels؛ البحث النصي عن كلمة `Dao` وحدها لا يعامل كدليل، وراجعت constructors الفعلية في `LedgerViewModel` وغيرها وهي Repositories صريحة.
- لا يوجد `import android.` داخل `domain/`.
- لا يوجد `withTransaction` داخل `domain/usecase`.
- إنشاء `AppDatabase` ما زال محصوراً في `AppContainer`.
- `FinanceApplication` يبقى Composition Root، وWorkerFactory يحقن Repositories إلى `TrashCleanupWorker` دون إنشاء Database داخل Worker.

## Logging والأمن والصلاحيات
- راجعت logging في المسارات الحساسة. عُدلت رسائل يمكن أن تكشف URI أو رسائل استثناء خامة في مسارات الصورة/Trash/contacts لتسجيل وصف أو نوع الاستثناء بدلاً من قيمة حساسة محتملة.
- بيانات الأمن تبقى في `EncryptedSharedPreferences` داخل `AppSecurityManager`، بينما `SharedPreferences` العادي لا يستخدم لبيانات الأعمال.
- Manifest راجع: `READ_CONTACTS` مستخدمة في اختيار جهات الاتصال، و`POST_NOTIFICATIONS` و`VIBRATE` لهما مسارات فعلية، و`WRITE_EXTERNAL_STORAGE` مقيد بـ API 28 أو أقدم. لم توجد حاجة لإضافة `INTERNET`.

## الاختبارات المضافة
- `FloatingUiStateNormalizerTest`:
  - حدود size level.
  - ratios غير الصالحة و`NaN`.
  - استعادة حالة الإضافة دون position.
  - الحفاظ على coordinate صالح مع إصلاح coordinate تالف.
- الاختبارات الحالية الخاصة بـ PDF action وpin scope وRecurring وTrash بقيت في الحزمة ولم تُدّع نتائج تنفيذية.

## الملفات المحذوفة والمضافة
- محذوف: `app/src/main/java/com/smartledger/aldaftar/data/local/NavigationPreferences.kt`.
- مضاف: `app/src/test/java/com/smartledger/aldaftar/data/repository/FloatingUiStateNormalizerTest.kt`.
- معدل: `FloatingUiPreferencesRepository.kt` و`FinanceViewModel.kt` و`MainAppLayout.kt`، مع تعديلات logging محدودة غير بصرية في `BusinessProfileImageHelper.kt` و`TrashItemParser.kt` و`StringUtils.kt`.

## نتائج البحث النهائي
- `FinanceApplication.data`: لا نتائج في كود الإنتاج.
- `DataDependencies`: لا نتائج.
- `applicationContext as FinanceApplication`: لا نتائج.
- `appContainer()`: لا نتائج.
- `AppDatabase.getDatabase(`: محصور في `AppContainer`.
- `GlobalScope`: لا نتائج في كود الإنتاج.
- `runBlocking`: لا نتائج.
- `CoroutineScope(Dispatchers.Main)`: لا نتائج.
- `printStackTrace(`: لا نتائج.
- `TODO` و`FIXME`: لا نتائج في كود الإنتاج.
- `println(` و`System.out`: لا نتائج في كود الإنتاج.
- `BusinessProfileLoader.load(context)`: لا يوجد العقد القديم؛ الاستدعاءات تمرر `BusinessProfile` صراحة.
- `import android.` داخل `domain/`: لا نتائج.
- `withTransaction` داخل `domain/usecase`: لا نتائج.
- `FinanceRepository` و`AppSettingsRepository` و`HabayebCategoryManager` و`HabayebPreferencesStore` و`HabayebRecurringManager`: لا نتائج في كود الإنتاج.
- `SharedPreferences`/`getSharedPreferences`: الاستخدام العادي محصور في `FloatingUiPreferencesRepository`؛ الاستخدام الأمني في `AppSecurityManager` عبر `EncryptedSharedPreferences`.
- `@Suppress`: المتبقي موضعي ومراجع؛ لا يوجد `UNUSED_PARAMETER` من NavigationPreferences، و`UNCHECKED_CAST` محصور في factory.

## Gradle والتحقق التنفيذي
لم يُشغّل Gradle مطلقاً في هذه الدفعة. لم تُشغّل `build` أو `test` أو `check` أو `assemble` أو أي أمر Gradle. السبب البيئي المعروف هو:

`UnknownHostException: services.gradle.org`

لم تُغيّر Gradle Wrapper أو Gradle أو Android Gradle Plugin أو repositories أو dependencies أو SDK versions. بقي `versionCode = 1` و`versionName = "1.0"`. لا يُدّعى نجاح Build أو Compile أو Tests تنفيذياً؛ الإغلاق مبني على تدقيق ساكن للملفات والاستدعاءات والتواقيع والبحث النهائي.

## القرار النهائي للدفعة الخامسة
**مكتملة وفق الملفات الناتجة والتدقيق الساكن المنفذ: أُغلقت بقايا NavigationPreferences الانتقالية، ونُظفت APIs الميتة المحددة، وثُبتت حدود Floating UI القابلة للاختبار، ورُوجعت العقود والـ suppressions والـ PDF/Room/Recurring/Trash/WorkManager/الأمن والصلاحيات ضمن النطاق، مع الحفاظ على الواجهة وإعدادات البناء.**

# الدفعة السادسة — التدقيق العميق للسلامة الهندسية

## المصدر والنطاق
- الحزمة الفعلية `bbatch5.zip` كانت المصدر الوحيد للحقيقة.
- لم يُشغَّل Gradle بسبب القيد البيئي المعروف: `UnknownHostException: services.gradle.org`.
- لم يُدَّع نجاح build أو compile أو test أو lint، ولم تُغيَّر ملفات Gradle أو الإصدارات أو `versionCode = 1` أو `versionName = "1.0"`.

## نتيجة التدقيق الساكن
- عولج خطآن فعليان في السلامة الاسمية: كان `HabayebScreen.kt` يحتوي alias ذاتي المرجع لـ `HabayebDialogState`، وكان `CustomDateTimePickerDialog.kt` يحتوي alias ذاتي المرجع لـ `RangeTab`. أزيل aliasان وأبقيت الاستيرادات المباشرة للأنواع الأصلية.
- فُحصت typealias المتبقية في كود الإنتاج بعد الإصلاح: لا توجد typealias إنتاجية متبقية.
- تمت مراجعة مسارات constructors وFactory وComposition Root وRoom وRecurring وPDF/Excel وFloating UI بحثاً عن المراجع المحذوفة والاقترانات الخفية الواضحة.

## Coroutine وState ownership
- `AddCustomerSaveHelper` لم يعد يطلق `viewModelScope` من Helper واجهة ولا يفرض `Dispatchers.IO` ثم يعود إلى Main. أصبح مسار الحفظ `suspend`، وتملك الشاشة نطاق Compose عبر `rememberCoroutineScope()`، بينما تبقى عملية الحفظ نفسها في ViewModel/طبقة البيانات.
- إلغاء coroutine لا يُبتلع في `AddCustomerSaveHelper`؛ `CancellationException` يعاد رميه.
- تمت مراجعة `BusinessProfileScreen` ومسارات `withContext` والموارد؛ لم يُجرَ تغيير بصري.
- تمت مراجعة StateFlow في ViewModels المستهدفة؛ لم يُكشف mutable state جديد للواجهة.

## FinanceViewModel والملفات الكبيرة
- تمت مراجعة `FinanceViewModel` كملف متعدد العمليات؛ لم يُفكك شكلياً حسب عدد الأسطر لأن واجهته الحالية تمثل تنسيقاً مركزياً لحالة الدفتر، مع استمرار الاعتماد على repositories/flows القائمة.
- راجعت الملفات الكبيرة ضمن نطاق المسؤولية والاقتران، ولم يُقسَّم أي ملف لمجرد الحجم.

## Suppressions والأمن والسجلات
- راجعت suppressions الموجودة؛ `UNCHECKED_CAST` في `AppViewModelFactory` بقي محصوراً في نمط `ViewModelProvider.Factory`.
- suppressions الخاصة بـ Android compatibility بقيت محلية.
- تمت مراجعة `EncryptedSharedPreferences` ومسارات الأمان والسجلات ضمن النطاق الهندسي؛ لا تغيير لنظام القفل أو biometric.

## Room وRecurring وTrash وWorkers
- راجعت AppDatabase والـ repositories والمعاملات المستهدفة والذرية.
- راجعت recurring ووحدات الزمن: `startDateMillis` و`endDateMillis` بالملي ثانية، و`lastExecutedTimestamp` وtimestamps للمعاملات بالثواني، مع التحويل الصريح عند توليد occurrence.
- راجعت مسار Trash/Restore وWorker cleanup ضمن الحدود القائمة.
- راجعت WorkerFactory وTrashCleanupWorker وعدم إنشاء قاعدة البيانات مباشرة في Worker.

## التخزين والواجهات
- `getSharedPreferences` الإنتاجي محصور في `FloatingUiPreferencesRepository`، بينما التخزين الأمني يستخدم `EncryptedSharedPreferences` في طبقة platform/security.
- راجعت Floating UI normalization والاختبارات الموجودة للحالات NaN والحدود.
- لا وصول مباشر واضح للـ Database من UI، ولا import Android داخل `domain/`.

## Manifest وComposition Root
- تمت مراجعة الصلاحيات مع call sites: `READ_CONTACTS` و`POST_NOTIFICATIONS` و`WRITE_EXTERNAL_STORAGE` و`VIBRATE` لها مسارات استخدام مرتبطة.
- لا توجد صلاحية `INTERNET` في الـ Manifest.
- `MainActivity` يستخدم `(application as FinanceApplication)` عند نقطة تركيب Android للحصول على Factory؛ لم يُعتبر Service Locator خفياً خارج Composition Root.
- `AppDatabase.getDatabase(...)` يبقى في `AppContainer`.

## الاختبارات
- لم تُضف اختبارات شكلية في هذه الدفعة؛ الاختبارات عالية القيمة الموجودة تشمل recurring وFloating UI وFinancialPolicy وHashUtils وTrash serialization وPDF contracts.
- لم تُشغَّل الاختبارات.

## البحث النهائي
- لا توجد `typealias` إنتاجية بعد الإصلاح.
- لا توجد النتائج الإنتاجية التالية: `GlobalScope`، `runBlocking`، `printStackTrace(`، `TODO`، `FIXME`، `println(`، `System.out`، `FinanceApplication.data`، `DataDependencies`، أو `appContainer()`.
- بقيت النتائج المشروعة لـ `Dispatchers` و`withContext` و`viewModelScope` بعد فتح المسارات المؤثرة ومراجعة ownership، ولم تُعامل كأخطاء نصية.

## القرار النهائي للدفعة السادسة
أُغلقت الدفعة السادسة على أساس التدقيق الساكن للملفات الناتجة، مع إصلاح العيوب الفعلية المكتشفة في aliases وملكية coroutine لمسار حفظ العميل. نقطة التوقف الدقيقة هي التحقق الساكن فقط؛ لا يوجد ادعاء بنجاح التجميع أو البناء أو الاختبارات لأن Gradle لم يُشغَّل بسبب `UnknownHostException: services.gradle.org`.

# الدفعة السابعة — توحيد ملكية Coroutines وعمليات الخلفية

## المصدر والهدف
- الحزمة الفعلية `bbatch6.zip` كانت المصدر الوحيد للحقيقة لهذه الدفعة؛ لم تُستخدم خارطة الطريق السابقة كدليل على صحة الكود.
- الهدف كان تدقيق ملكية coroutine وDispatcher وCancellation وحدود UI/ViewModel/Data، مع الحفاظ على الواجهة والعقود الحالية وعدم إدخال طبقات أو مكتبات جديدة.

## ما ثبت وما عُدّل فعلياً
- `BusinessProfileScreen.kt`: بقيت معالجة الصورة المرتبطة بالعرض وBitmap في UI لأن تفاصيل rendering ليست مسؤولية Repository. أزيل نمط `launch(IO) -> withContext(Main)` المتداخل من المسارات المعدلة. أصبح scope الخاص بـ Compose هو مالك coroutine، وتُحصر القراءة/الحفظ في `withContext(IO)` والعمليات الحسابية الثقيلة للصورة في `Default` عند الدوران. تعود mutations لحالة Compose إلى سياق UI الطبيعي بعد انتهاء `withContext`.
- أضيفت إعادة نشر `CancellationException` فقط في مسارات الصورة التي كانت تلتقط `Exception` واسعاً.
- `QuadBackupCard.kt`: نقلت قراءة وكتابة SAF الفعلية خارج Main إلى `IO` داخل coroutine مملوكة لـ Compose، وأزيل إنشاء coroutine فرعية للعودة إلى Main. إلغاء العملية لا يتحول إلى رسالة فشل عادية.
- `CustomerHistoryFilterHelper.kt`: بقيت الفلترة منطقاً خالصاً مرتبطاً بعرض قائمة الشاشة، لكن نتيجة الحساب تُحسب على `Default` ثم تُسند إلى `produceState` في سياقه، بدلاً من تعديل Compose state من داخل كتلة الخلفية. النصوص المحلية تُقرأ قبل الانتقال إلى `Default`.
- `SecurityScreen.kt`: أزيلت دالة كانت تبدل إلى Main داخل مسار أصلاً منسق من UI. hashing يبقى على `Default`، ثم يتم تسليم إعدادات الحفظ إلى `SecurityViewModel`، وتبقى Toast/navigation في UI. لم يتغير نظام القفل أو واجهته.
- `AppLockScreen.kt`: بقيت عمليات تحقق PIN/recovery على `Default` مع تنظيف مصفوفات الأحرف، ولم توجد catch واسعة تبتلع الإلغاء في المسار المفحوص.
- `AddCustomerSaveHelper.kt`: فُحص كـ UI orchestration. لا ينشئ Dispatcher ولا Repository ولا coroutine مستقلة؛ validation وUI effects محلية، بينما الحفظ الفعلي يستدعي API معلنة `suspend` في `HabayebFinanceViewModel`. `CancellationException` لا تُبتلع.
- `TrashScreen.kt` و`TrashItemParser.kt`: بقيت معالجة parsing/الفلترة الثقيلة على `Default` حيث لها مبرر CPU-bound؛ لم يُقسّم `TrashItemParser` لمجرد الحجم، ولم يظهر اعتماد UI داخل parser.
- راجعت مسارات `CustomerShareHelper` وCurrency/Exchange Rate وPDF/Excel وViewModels والـ repositories المتأثرة؛ لم يُنقل منطق سليم لمجرد تقليل نتائج البحث، ولم يُنشأ God ViewModel.

## سياسة الملكية والإلغاء
- Composable يملك فقط coroutine المرتبطة بعمر UI عندما تكون العملية جزءاً من event/UI-local work.
- ViewModel يملك العمليات المرتبطة بعمر الشاشة وحفظ البيانات عبر واجهاته الحالية؛ لم تُنقل تفاصيل التخزين إلى UI.
- `IO` يستخدم للـ stream/file access الفعلي، و`Default` للحساب/Bitmap processing المناسب، ولا تستخدم `Main` كقفزة عودة زائدة عندما تستأنف coroutine أصلاً على سياق UI.
- `CancellationException` في المسارات المعدلة ليست failure business ولا تتحول إلى Toast أو state خطأ.

## المسارات التي بقيت دون تغيير
- `LedgerViewModel` وعمليات Room/Recurring/Trash/Restore لم تُعاد كتابتها لأن فحص العقود القائمة لم يثبت خللاً جديداً يتطلب تغيير semantics في هذه الدفعة.
- `FinanceApplication` بقي Composition Root و`AppContainer` بقي Manual DI Composition Root، ولم يُعد فتح حدود `AppDatabase` أو PDF/Excel الصريحة.
- لم تُضف طبقات DI أو UseCase/Repository شكلية، ولم تُضف wrappers انتقالية.

## الاختبارات
- لم تُضف اختبارات شكلية؛ التعديلات كانت نقل ownership بين Compose coroutine و`withContext` مع APIs Android/Compose غير مناسبة لاختبار unit مستقل دون إضافة infrastructure. الاختبارات الموجودة لم يُدّع تشغيلها.

## نتائج البحث والتدقيق الساكن النهائي
- لا توجد `typealias` في كود الإنتاج.
- لا توجد: `GlobalScope`، `runBlocking`، `FinanceApplication.data`، `DataDependencies`، `appContainer()`، `applicationContext as FinanceApplication`، `printStackTrace(`، `TODO`، `FIXME`، `println(` أو `System.out` في كود الإنتاج المفحوص.
- لا توجد `import android.` داخل `domain/` ولا `withTransaction` داخل `domain/usecase`.
- لا توجد: `HabayebCategoryManager`، `HabayebPreferencesStore`، `HabayebRecurringManager`، `FinanceRepository` أو `AppSettingsRepository`.
- بقيت النتائج المشروعة لـ `rememberCoroutineScope` و`viewModelScope` و`Dispatchers` و`withContext` بعد فتح المسارات المؤثرة وتصنيفها بحسب lifecycle والعملية، ولم تعامل كمشكلات نصية.
- فحص package declarations لملفات Kotlin لم يظهر عدم تطابق بين package والبنية المنطقية.

## Gradle ونقطة التوقف
- لم يُشغّل Gradle إطلاقاً، ولم تُشغّل build أو compile أو test أو check أو lint أو assemble.
- السبب البيئي: `UnknownHostException: services.gradle.org`.
- لم تتغير Gradle Wrapper أو Gradle أو Android Gradle Plugin أو dependencies أو repositories أو SDK versions، وبقي `versionCode = 1` و`versionName = "1.0"`.
- نقطة التوقف الدقيقة: التحقق الساكن ومراجعة الملفات والاستدعاءات فقط؛ لا يوجد ادعاء بنجاح Build أو Compile أو Test أو Lint.

## القرار النهائي للدفعة السابعة
أُغلقت الدفعة السابعة وفق الكود الناتج: عولجت حالات فعلية لخلط UI وDispatcher في Business Profile وSAF backup/filtering/security، وثُبتت سياسة الإلغاء في المسارات المعدلة، مع الحفاظ على الحدود الحالية للـ ViewModels وRepositories وعدم تغيير الواجهة أو semantics البيانات.

# الدفعة الثامنة — تقوية قابلية الاختبار وسلامة العقود الحرجة

## الهدف
رفع قيمة التحقق من طبقة البيانات والعقود الحرجة دون تغيير إعدادات Gradle أو الواجهة أو إدخال مكتبات جديدة، مع الاعتماد على الكود الناتج فعلياً.

## تدقيق الاختبارات قبل التنفيذ
- كانت الحزمة تحتوي على 12 اختبار وحدة فعلياً في `app/src/test`، ولم توجد اختبارات فعلية في `app/src/androidTest`.
- لم تُضف اختبارات Instrumentation شكلية؛ العقود التي عولجت هنا قابلة للتحقق ضمن البنية الحالية باختبارات منطقية أو الاختبار البنيوي الموجود.
- لم يُشغّل Gradle بسبب القيد المعروف: `UnknownHostException: services.gradle.org`.

## ما تم تعديله فعلياً
- استخراج حساب occurrences المتكررة إلى `RecurringScheduleCalculator` كوحدة خالصة بلا Android، مع بقاء `RecurringRepository.executeDue` مالكاً للمعاملة الذرية وإنشاء المعاملات وتحديث `lastExecutedTimestamp` داخل `Room.withTransaction`.
- إضافة اختبارات لحدود البداية والنهاية، وعدم تكرار آخر occurrence، وثبات عقد الوحدات: حدود الجدول بالمللي ثانية ووقت التنفيذ بثواني Unix.
- إصلاح فقدان بيانات عند تسلسل `HabayebCustomer` إلى Trash: أصبح `initialType` و`categoryId` محفوظين في التسلسل المفرد وحزمة العميل، وهما حقلا restore مهمان.
- إضافة اختبارات تثبت حفظ `initialType` و`categoryId` في JSON.
- تصحيح `DatabaseSchemaV1Test` ليعكس جميع الجداول المسجلة فعلياً، ومنها `business_profile` و`pinned_habayeb_customers` و`recurring_configs`.
- إصلاح سياسة الإلغاء في `TrashCleanupWorker`: يعاد رمي `CancellationException` ولا يتحول إلى `Result.failure()`؛ يبقى `IOException` مؤقتاً إلى `retry` والأخطاء الأخرى إلى `failure`.

## العقود التي ثبتت ولم تتغير
- `BusinessProfile` ما زال يملك مصدر الحقيقة عبر Room، وPDF/Excel يتلقيان `BusinessProfile` صراحة، ولا يوجد مسار `BusinessProfileLoader.load(context)` بلا profile.
- `FloatingUiPreferencesRepository` يبقى الحد الوحيد لاستخدام `SharedPreferences` العادي في المسار المدقق، مع بقاء التطبيع في `FloatingUiStateNormalizer`.
- لا يوجد Android import داخل `domain/`، ولا `AppDatabase` أو `withTransaction` داخل `domain/usecase`.
- لا يوجد `GlobalScope` أو `runBlocking` أو `FinanceApplication.data` أو `DataDependencies` أو `appContainer()` أو `applicationContext as FinanceApplication` أو `printStackTrace(` أو `TODO/FIXME` في كود الإنتاج وفق البحث النهائي.
- لم تتغير عقود PDF/Excel أو الواجهة أو إعدادات Gradle أو الإصدار.

## Recurring وTrash/Restore
- منع تكرار occurrence يعتمد على تقدم `lastExecutedTimestamp`، والاختبار الجديد يثبت عدم إعادة occurrence الأخيرة.
- إنشاء occurrences وتحديث آخر تنفيذ ما زالا داخل معاملة واحدة في `RecurringRepository`.
- تسلسل Trash للحزم يحافظ على `categoryId` و`pinnedScopeCategoryIds` و`linkedMainTxId`، وبعد الإصلاح يحافظ أيضاً على `initialType` للعميل.
- البيانات التالفة في parser لم تُعد صياغتها؛ لم يُغيّر سلوكها لعدم ظهور عقد جديد يستلزم تغيير السياسة.

## Room وWorkers
- `AppDatabase` يسجل الكيانات الحرجة: Business Profile وRecurring وPins إضافة إلى بقية الجداول، واختبار المخطط يعكس ذلك.
- `TrashCleanupWorker` لا ينشئ `AppDatabase` ولا يصل إلى DAO مباشرة؛ يعتمد على Repositories المحقونة.
- سياسة Worker الحالية: نجاح عند اكتمال التنظيف، `retry` لـ `IOException`، `failure` للأخطاء الأخرى، مع تمرير الإلغاء.

## الاختبارات الجديدة
- `RecurringScheduleCalculatorTest`
- توسيع `HabayebTrashSerializationTest`
- تحديث `DatabaseSchemaV1Test` لعقد المخطط الفعلي

## نتائج البحث النهائي والتدقيق الساكن
- فُحصت الأنماط المحظورة والحدود المعمارية ونتائجها المتبقية ذات الصلة.
- الاستخدام المتبقي لـ `getSharedPreferences` محصور في `FloatingUiPreferencesRepository`.
- لا يوجد تسرب Android إلى `domain/`، ولا تسرب `AppDatabase/withTransaction` إلى `domain/usecase`.
- تم فحص ملفات التعديل وcall sites ذات الصلة، والاستيرادات في الملفات المعدلة.
- لم يُدّع نجاح build أو test أو lint لأن Gradle لم يُشغّل.

## نقطة التوقف والقرار
الدفعة الثامنة تنتهي عند تقوية العقود القابلة للتحقق ضمن البنية الحالية: recurring schedule ووحدات الزمن، سلامة تسلسل Trash للحقول اللازمة للاستعادة، عقد مخطط Room، وسياسة إلغاء Worker. لا تبدأ هذه الحزمة الدفعة التاسعة.


## الدفعة التاسعة — التحقق من Repository وتدفقات البيانات الحرجة

- المصدر الفعلي: `bbatch8.zip`.
- تم تدقيق `RecurringScheduleCalculator` و`RecurringRepository` على الكود الناتج. حدود الجدول بالمللي ثانية ووقت التنفيذ بالثواني. الإدراج وتحديث `lastExecutedTimestamp` يقعان داخل `Room.withTransaction`.
- تم التحقيق في حد 365 يوماً: لم يظهر عقد يثبت إسقاط catch-up الأقدم من سنة، ولذلك أزيل الحد الصامت. يبقى `RecurringRepository` يحد كل تنفيذ إلى 50 occurrence ثم يثبت آخر ما نُفذ، فتستمر catch-up في الدورات التالية دون فقدان الفترة الأقدم.
- `Calendar.getInstance()` بقي كما هو لأن العقد الحالي يعمل بالتاريخ المحلي للجهاز؛ لم يُثبت عقد مستقل عن المنطقة الزمنية.
- أضيفت اختبارات catch-up لأكثر من سنة، وRound-trip حقيقي لـ Trash: Customer وTransaction، بما يشمل `initialType` و`categoryId` و`linkedMainTxId` وحقول العملة.
- تم تدقيق `TrashCleanupWorker`: الإلغاء يعاد رميه، `IOException` يعيد المحاولة، والخطأ الآخر يفشل. حذف ملفات cache best-effort محلياً بقي دون تغيير.
- لم تُضف dependencies أو mocks أو اختبارات Instrumentation شكلية، ولم تتغير Gradle أو الواجهة.
- لم يُشغّل Gradle بسبب القيد البيئي المعروف `UnknownHostException: services.gradle.org`؛ لذلك لا يوجد ادعاء بنجاح build أو compile أو test أو lint.
- نقطة التوقف التالية: توسيع التحقق التنفيذي/التكاملي فقط عندما تصبح بيئة الاختبار الحالية قابلة للتشغيل دون تغيير غير مبرر في البنية.
- القرار: الدفعة التاسعة تركزت على عقود البيانات الأعلى خطورة ولم تُجرَ إعادة هندسة واسعة.

# الدفعة العاشرة — التحقق من التنفيذ المحدود والثبات

## مصدر الحقيقة ونطاق التدقيق
- المصدر الفعلي هو `bbatch9.zip`، وفُكّت الحزمة وفُحص الكود والاختبارات والخارطة قبل التعديل.
- احتوت الحزمة الأصلية فعلياً على 14 ملف اختبار Kotlin داخل `app/src/test`، ولم تظهر اختبارات `androidTest`.
- قورنت ادعاءات الدفعة التاسعة مع `RecurringScheduleCalculator` و`RecurringRepository` و`TrashDao` و`AppDatabase` والاختبارات الفعلية.

## Recurring bounded processing
- ثبت أن النمط السابق كان يحسب قائمة catch-up كاملة أولاً ثم يطبق `take(50)` داخل `RecurringRepository`، ولذلك كان حد 50 يحد الإدراج لا حجم القائمة المولدة.
- عُدّل `RecurringScheduleCalculator.dueOccurrences` لقبول `maxOccurrences` موجب، ويتوقف توليد القائمة عند بلوغ الحد بدلاً من بناء القائمة الكاملة ثم قصها.
- أصبح `RecurringRepository.executeDue` يمرر `maxOccurrences = 50` مباشرة إلى الحاسبة، مع بقاء ترتيب occurrences وتقدم `lastExecutedTimestamp` كما في العقد القائم.
- لم يُعد حد 365 يوماً؛ يبقى catch-up الطويل ممكناً، لكن لا تُبنى قائمة النتائج المطابقة كاملة عندما يكفي تنفيذ 50 occurrence في الدورة الحالية.
- أضيف اختبار لتوليد catch-up طويل مع حد 50، واختبار لتنفيذ دفعتين متتاليتين منطقياً عبر تقدم `lastExecutedTimestamp` يثبت ترتيب أول 100 occurrence وعدم التكرار وعدم فقدانها ضمن عقد الحاسبة.
- هذا الاختبار لا يدعي تنفيذ `Room.withTransaction`؛ الذرية تبقى دليلاً بنيوياً من كود `RecurringRepository` الذي ينفذ الإدراج وتحديث timestamp داخل معاملة واحدة.
- لم يُغيّر عقد المنطقة الزمنية: `Calendar.getInstance()` ما زال يعتمد التوقيت المحلي للجهاز لأن الكود لا يثبت عقد UTC مستقل.

## Persistence وFull Restore
- فُحصت قابلية اختبار Room بالأدوات الموجودة فعلياً. المشروع يملك بالفعل `androidx.test.core` وRobolectric وRoom، كما كان اختبار `DatabaseSchemaV1Test` يستخدم `Room.inMemoryDatabaseBuilder`، لذلك لم تُضف أي dependency.
- أضيف `TrashRestorePersistenceTest` كاختبار Room in-memory عالي القيمة: حفظ `DeletedItemEntity`، قراءته من قاعدة البيانات، استعادته عبر `TrashDao.restoreDeletedItem`، ثم قراءة `HabayebTransaction` والتحقق من الحقول الحرجة وإزالة عنصر Trash.
- يغطي الاختبار طبقة persistence لمسار `HabayebTransaction`، ولا يدعي تغطية واجهة Trash أو جميع أنواع bundle/restore.
- بقيت اختبارات Round-trip السابقة مكملة لهذا الاختبار: JSON contract لا يساوي Full Restore Persistence، ولذلك لم تُكرر assertions نفسها بلا قيمة.

## Worker وSchema والمعمارية
- لم يُعدّل `TrashCleanupWorker`: سياسة الإلغاء و`IOException` وbest-effort لتنظيف cache بقيت كما ثبتت في الدفعات السابقة، ولم يظهر عيب جديد يبرر تغييرها.
- لم يُعدّل `AppDatabase` أو Schema أو FinanceViewModel أو الواجهة أو بنية Manual DI.
- لم تُضف mocks أو طبقات معمارية أو Instrumentation tests شكلية.

## الاختبارات الفعلية الجديدة/المعدلة
- `RecurringScheduleCalculatorTest.kt`: أضيف اختبار bounded generation واختبار الدفعات المتتابعة.
- `TrashRestorePersistenceTest.kt`: أضيف اختبار Room in-memory لمسار persist → read → restore → read-back → trash removal.
- العدد الناتج لملفات اختبار Kotlin في `app/src/test` هو 15 ملفاً؛ لم يُنشأ `androidTest` شكلي.

## القيود والتحقق التنفيذي
- لم يُشغّل Gradle إطلاقاً، ولم تُشغّل build أو compile أو test أو check أو lint أو assemble.
- السبب البيئي المعروف: `UnknownHostException: services.gradle.org`.
- لم تتغير Gradle Wrapper أو Gradle أو AGP أو Kotlin أو dependencies أو repositories أو SDK versions، وبقي `versionCode = 1` و`versionName = "1.0"`.
- لا يوجد ادعاء بنجاح Build أو Compile أو Test أو Lint؛ الاختبارات الجديدة تمت مراجعة بنيتها واستدعاءاتها ساكناً فقط.

## نتائج التدقيق الساكن النهائي
- فُحصت الأنماط: `FinanceApplication.data` و`DataDependencies` و`appContainer()` و`applicationContext as FinanceApplication` و`GlobalScope` و`runBlocking` و`printStackTrace(` و`TODO` و`FIXME` و`println(` و`System.out`.
- لا يوجد `import android.` داخل `domain/`، ولا `AppDatabase.getDatabase(` خارج حد `AppContainer`/تعريف قاعدة البيانات.
- راجعت call sites المتأثرة لـ `dueOccurrences`، واستيرادات وpackage declarations للملفات المعدلة، ولم يُجرَ تغيير Gradle أو UI.

## نقطة التوقف والقرار
الدفعة العاشرة مغلقة على الكود الناتج ضمن نطاقها: أزيل بناء قائمة catch-up كاملة قبل حد التنفيذ، وثُبت منطق الدفعات المتتابعة في وحدة الجدولة، وأضيف دليل persistence فعلي لمسار Restore واحد عالي الخطورة باستخدام البنية الموجودة. تبقى نتائج التنفيذ غير مثبتة لأن Gradle لم يُشغّل، ولا يعني ذلك نجاح build أو الاختبارات. نقطة التوقف التالية هي التحقق التنفيذي الفعلي عندما تتاح بيئة Gradle صالحة، أو توسيع عقود persistence فقط عند ظهور خطر مثبت.

# المرحلة B — التحقق التنفيذي والتقسية المبنية على الأدلة

## حالة المرحلة
- المرحلة A الخاصة بتثبيت المعمارية والعقود الحرجة مغلقة بعد الدفعات 1–10.
- لا يعاد فتحها إلا عند وجود عيب مثبت أو مخالفة عقد قابلة لإعادة الإنتاج.
- لا توجد نتيجة تنفيذية لـ Gradle في هذه المرحلة حتى الآن.

# الدفعة الحادية عشرة — بوابة التحقق وتدقيق عقود الاختبارات الحرجة

## مصدر الحقيقة ونطاق التدقيق
- المصدر الفعلي هو `bbatch10.zip`.
- فُحصت الحزمة والكود والاختبارات وخارطة الطريق قبل أي تعديل.
- لم تُجرَ إعادة هندسة، ولم تُعدّل Gradle أو dependencies أو الواجهة أو `FinanceViewModel`.

## نتيجة تدقيق Batch 10
- مراجعة `RecurringScheduleCalculatorTest` أثبتت أن DAILY وbounded generation والدفعات المتتابعة مغطاة، لكن تكرارات WEEKLY وMONTHLY، وهما فرعان إنتاجيان مستقلان في الحاسبة، لم تكن لهما تغطية عقدية مباشرة.
- هذه فجوة مثبتة وليست رغبة في زيادة عدد الاختبارات.
- أضيف اختبار عقد واحد يغطي WEEKLY وMONTHLY مع selectors فعلية، ويثبت عدم إدراج الأيام غير المطابقة، ويتضمن حالة شهر لا يحتوي اليوم 31.
- لم يتغير كود الإنتاج لأن التدقيق لم يثبت عيباً في التنفيذ الحالي.

## مصفوفة Recurring الحالية
- DAILY: مغطى بحدود البداية والنهاية ووحدات الزمن ومنع تكرار آخر occurrence.
- WEEKLY: مغطى بالـ `daysOfWeek` وبالأيام المطابقة وغير المطابقة.
- MONTHLY: مغطى بالـ `daysOfMonth` وبحالة اليوم 31 عبر شهر أقصر.
- Catch-up طويل: مغطى دون حد سنة صامت.
- Bounded generation: مغطى بحد 50 قبل بناء قائمة النتائج الكاملة.
- Sequential batches: مغطى منطقياً عبر تقدم `lastExecutedTimestamp` وعدم التكرار وترتيب أول 100 occurrence.
- DST وتغير المنطقة الزمنية: لم تُضف لها اختبارات لأن العقد الحالي محلي للجهاز ولا يوجد مطلب مثبت يحدد سلوكاً مختلفاً؛ لا تغيير تخميني في `Calendar.getInstance()`.

## تدقيق الاختبارات
- الاختبار الجديد يستخدم تواريخ ثابتة ولا يعتمد على الوقت الحالي.
- لا يعتمد على ترتيب تنفيذ الاختبارات أو على I/O خارجي.
- لا يختبر implementation detail؛ يثبت نتائج occurrence بحسب عقد frequency/selectors.
- لا أضيفت mocks أو dependencies أو Instrumentation tests شكلية.

## نتائج التحقق الساكن
- أعيد فحص الحدود: `FinanceApplication.data` و`DataDependencies` و`appContainer()` و`applicationContext as FinanceApplication` و`GlobalScope` و`runBlocking` و`printStackTrace(` و`TODO` و`FIXME` و`println(` و`System.out`.
- أعيد فحص `import android.` داخل `domain/` وحد `AppDatabase.getDatabase(`.
- لم يُجرَ تعديل إنتاجي في هذه الدفعة بسبب عدم وجود عيب مثبت.

## القيود والتحقق التنفيذي
- لم يُشغّل Gradle، ولم تُشغّل build أو compile أو test أو lint أو assemble.
- السبب البيئي المعروف: `UnknownHostException: services.gradle.org`.
- لا يوجد ادعاء بنجاح Build أو Compile أو Test أو Lint.

## نقطة التوقف والقرار
- الدفعة الحادية عشرة أغلقت فجوة اختبارية مثبتة في فروع WEEKLY/MONTHLY فقط، دون Architecture churn.
- الخطوة التالية ليست إعادة هيكلة؛ البوابة التالية عند توفر بيئة Gradle صالحة هي التحقق التنفيذي: compilation ثم unit tests ثم lint ثم release build وفق الأخطاء الفعلية.

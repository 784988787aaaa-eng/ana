# إعدادات التحسين المتقدمة لحماية الشيفرة وتقليل الحجم.
-optimizationpasses 5
-allowaccessmodification
-dontusemixedcaseclassnames
-verbose

# قواعد إبقاء دفاعية تمنع إزالة الأنواع اللازمة وقت التشغيل.

# تمنع إعادة تسمية أو إزالة كيانات قاعدة البيانات وواجهات الوصول وقاعدة البيانات المستخدمة وقت التشغيل.
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep interface * {
    @androidx.room.Dao *;
}
-keepclassmembers class * {
    @androidx.room.Database *;
}

# تُبقي كيانات التخزين المحلي ثابتة لدعم الانعكاس والتسلسل المستخدم في النسخ الاحتياطية.
-keep class com.smartledger.aldaftar.data.local.entities.** { *; }
-keep interface com.smartledger.aldaftar.data.local.dao.** { *; }

# قواعد الحفاظ على سمات واجهات العرض وحالاتها أثناء التحسين.
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    @androidx.compose.runtime.Immutable <fields>;
    @androidx.compose.runtime.Stable <fields>;
}

# تُبقي منشئات العمال التي ينشئها مدير المهام بالاعتماد على أسماء الأنواع وقت التشغيل.
-keep class * extends androidx.work.Worker { <init>(android.content.Context, androidx.work.WorkerParameters); }
-keep class * extends androidx.work.CoroutineWorker { <init>(android.content.Context, androidx.work.WorkerParameters); }

# قواعد التحذيرات الخاصة بطبقات الاتصال المستخدمة مع مكتبة الشبكة.
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn androidx.room.**




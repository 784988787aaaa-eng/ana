# --- Advanced R8 & ProGuard Optimization Flags ---
-optimizationpasses 5
-allowaccessmodification
-dontusemixedcaseclassnames
-verbose

# --- Defensive Engineering Keeps ---

# Prevent Room entities, DAOs, and Database classes from being renamed or stripped
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep interface * {
    @androidx.room.Dao *;
}
-keepclassmembers class * {
    @androidx.room.Database *;
}

# Keep local database entities intact for Room
-keep class com.smartledger.aldaftar.data.local.entities.** { *; }
-keep interface com.smartledger.aldaftar.data.local.dao.** { *; }

# --- Compose and UI State Optimizations ---
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    @androidx.compose.runtime.Immutable <fields>;
    @androidx.compose.runtime.Stable <fields>;
}

# Keep WorkManager and App Startup components from being stripped by R8/Proguard
-keep class androidx.work.** { *; }
-keep class androidx.startup.** { *; }
-keep class * extends androidx.work.ListenableWorker {
    <init>(***);
}

-dontwarn androidx.room.**
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
-dontwarn javax.annotation.concurrent.**
-dontwarn com.google.crypto.tink.**
-dontwarn org.apache.poi.**
-dontwarn org.apache.commons.**





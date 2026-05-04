# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# For Google Mobile Ads SDK
-keep public class com.google.android.gms.ads.** {
   public *;
}

# For Google Play Services
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
   public static final *** NULL;
}

-keepnames class * implements android.os.Parcelable {
   public static final *** CREATOR;
}

-keep @interface com.google.android.gms.common.annotation.KeepName
-keepnames class * {
   @com.google.android.gms.common.annotation.KeepName *;
}

-keepclassmembernames class * {
   @com.google.android.gms.common.annotation.KeepName *;
}

-keep @interface com.google.android.gms.common.util.DynamiteApi
-keep public class * {
   @com.google.android.gms.common.util.DynamiteApi *;
}

-dontwarn com.google.android.gms.**

# WorkManager rules
-keep class * extends androidx.work.ListenableWorker {
    <init>(...);
}

# WorkManager internal Room database implementation
-keep class androidx.work.impl.WorkDatabase_Impl { *; }

# Room-related components used by WorkManager
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class * extends androidx.room.Entity
-keep class * extends androidx.room.Dao

# WorkManager internal classes used via reflection
-keep class androidx.work.impl.background.systemalarm.RescheduleReceiver { *; }
-keep class androidx.work.impl.background.systemjob.SystemJobService { *; }
-keep class androidx.work.impl.diagnostics.DiagnosticsReceiver { *; }

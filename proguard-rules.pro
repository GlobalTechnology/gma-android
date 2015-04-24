# override default number of optimization passes
-optimizationpasses 10


# Strip built-in logging
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}


# ButterKnife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewInjector { *; }
-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}
-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}


# Crashlytics
-keepattributes SourceFile,LineNumberTable


# Google Guava
-dontwarn javax.annotation.**
-dontwarn sun.misc.Unsafe


# Google Play Services - Google Maps
-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}
-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}


# Joda Time
-dontwarn org.joda.time.tz.ZoneInfoCompiler
-dontwarn org.joda.convert.FromString
-dontwarn org.joda.convert.ToString


# newrelic settings
-keep class com.newrelic.** { *; }
-dontwarn com.newrelic.**
-keepattributes Exceptions, Signature, InnerClasses


# The Key API
-dontwarn javax.annotation.**
-dontwarn org.slf4j.**


# Strip out SLF4J logging
-assumenosideeffects class org.slf4j.** {
    *;
}

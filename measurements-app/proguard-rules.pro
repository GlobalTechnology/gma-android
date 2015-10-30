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


# Google Play Services workaround for: https://code.google.com/p/android-developer-preview/issues/detail?id=3001
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**


# Google Support Library
# workaround for: https://code.google.com/p/android/issues/detail?id=170471
-keep class android.support.v7.widget.SearchView { *; }


# newrelic settings
-keep class com.newrelic.** { *; }
-dontwarn com.newrelic.**
-keepattributes Exceptions, Signature, InnerClasses


# Strip out SLF4J logging
-assumenosideeffects class org.slf4j.** {
    *;
}


## Picasso
-dontwarn com.squareup.okhttp.**

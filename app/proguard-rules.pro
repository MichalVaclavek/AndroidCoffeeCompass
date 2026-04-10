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

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-ignorewarnings

-keep class * {
    public private *;
}

# Keep runtime metadata used by Gson/Retrofit in release builds.
-keepattributes Signature
-keepattributes RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations,RuntimeInvisibleParameterAnnotations
-keepattributes AnnotationDefault

# Entity/model classes are deserialized reflectively by Gson and many fields are protected.
# The generic keep rule above only covers public/private members, which is not enough here.
-keep class cz.fungisoft.coffeecompass2.entity.** { *; }

# Keep Retrofit interfaces and generic signatures used by Gson converters.
-keep interface cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.** { *; }
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }

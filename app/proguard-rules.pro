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

# --- Gridly Specific Rules ---

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.android.AndroidExceptionPreHandler {
    <init>();
}

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keep,allowobfuscation,allowshrinking class * {
    @kotlinx.serialization.Serializable <init>(...);
}
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <init>(...);
}

# Ktor
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }

# Jetpack Glance
-keep class androidx.glance.** { *; }

# Room
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
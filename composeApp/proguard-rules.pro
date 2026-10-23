# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/manuel.alconchel/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.

# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard/index.html

# Keep Jetpack Compose classes
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Koin
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
-keep class kotlinx.coroutines.ServiceLoaderCapabilityProvider { *; }
-keep class kotlinx.coroutines.android.AndroidExceptionPreHandler { *; }
-dontwarn kotlinx.coroutines.**

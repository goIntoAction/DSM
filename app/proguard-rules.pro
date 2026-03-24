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

# Ignore warnings for missing errorprone annotations (used by Google Tink library)
-dontwarn com.google.errorprone.annotations.CanIgnoreReturnValue
-dontwarn com.google.errorprone.annotations.CheckReturnValue
-dontwarn com.google.errorprone.annotations.Immutable
-dontwarn com.google.errorprone.annotations.RestrictedApi
-dontwarn com.google.errorprone.annotations.InlineMe

# Ignore warnings for missing Google HTTP Client (optional dependency of Tink)
-dontwarn com.google.api.client.http.**

# Ignore warnings for missing Joda Time (optional dependency of Tink)
-dontwarn org.joda.time.**

# Keep Tink encryption library classes
-keep class com.google.crypto.tink.** { *; }

# Kotlinx Serialization - 防止序列化器被移除
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# 保留 DsmRoute 对象及其所有嵌套类（路由类）的序列化器
-keep class wang.zengye.dsm.navigation.DsmRoute { *; }
-keep class wang.zengye.dsm.navigation.DsmRoute$* { *; }

# 保留所有 @Serializable 类的序列化器（包括 data object 和 data class）
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class * {
    @kotlinx.serialization.* <methods>;
}

# 保留序列化器伴生对象
-keepclassmembers class * {
    *** Companion;
}
-keep class **$$serializer { *; }
-keep class **$Companion { *; }

# 保留 Kotlinx Serialization 内部类
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class kotlinx.serialization.** {
    *;
}

# SSH 库 (connectbot/sshlib) - 保留所有加密相关类
-keep class com.trilead.ssh2.** { *; }
-keep class org.connectbot.ssh2.** { *; }
-dontwarn com.trilead.ssh2.**
-dontwarn org.connectbot.ssh2.**

# Moshi - 保留 @JsonClass 注解的类和生成的 JsonAdapter
-keepclassmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keep @com.squareup.moshi.JsonClass class * { *; }
-keep class **JsonAdapter { *; }
-keep class **$JsonAdapter { *; }
-keep class **Adapter { *; }
-dontwarn com.squareup.moshi.**
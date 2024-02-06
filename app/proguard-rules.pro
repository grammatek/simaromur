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
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class com.grammatek.simaromur.device.Native* { *; }
-keep class com.grammatek.simaromur.device.flite.Native* { *; }

-keep class opennlp.** { *; }
-dontwarn opennlp.**

-keep class org.pytorch.** { *; }
-keep class com.facebook.** { *; }
-keep class org.kohsuke.github** { *; }
-keep class ai.onnxruntime.** { *; }
-keep class io.jsonwebtoken.** { *; }
-keep class okhttp3.** { *; }
-keep class javax.naming.NamingException.** { *; }
-keep class javax.naming.directory.Attributes.** { *; }
-keep class androidx.appcompat.view.ContextThemeWrapper.** { *; }

# warnings generated via kohsuke github api
-dontwarn com.infradna.tool.bridge_method_injector.BridgeMethodsAdded
-dontwarn com.infradna.tool.bridge_method_injector.WithBridgeMethods
-dontwarn edu.umd.cs.findbugs.annotations.CheckForNull
-dontwarn edu.umd.cs.findbugs.annotations.NonNull
-dontwarn edu.umd.cs.findbugs.annotations.SuppressFBWarnings
-dontwarn java.beans.ConstructorProperties
-dontwarn java.beans.Transient

# warnings generated via okhttp3 internal platform, these seem not to be needed on Android
-dontwarn okhttp3.internal.platform.*
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.conscrypt.Conscrypt

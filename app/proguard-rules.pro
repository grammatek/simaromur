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

-keep class opennlp.** { *; }
-dontwarn opennlp.**

-keep class org.pytorch.** { *; }
-keep class com.facebook.** { *; }
-keep class ai.onnxruntime.** { *; }
-keep class javax.naming.NamingException.** { *; }
-keep class javax.naming.directory.Attributes.** { *; }
-keep class androidx.appcompat.view.ContextThemeWrapper.** { *; }
-keep class com.oracle.svm.core.annotate.Delete.** { *; }
-keep class com.oracle.svm.core.annotate.Substitute.** { *; }
-keep class com.oracle.svm.core.annotate.TargetClass.** { *; }
-keep class com.oracle.svm.core.configure.ResourcesRegistry.** { *; }
-keep class io.jsonwebtoken.JwtBuilder.** { *; }
-keep class io.jsonwebtoken.Jwts.** { *; }
-keep class io.jsonwebtoken.SignatureAlgorithm.** { *; }
-keep class okhttp3.OkUrlFactory.** { *; }
-keep class org.kohsuke.github** { *; }

# warnings generated via kohsuke github api
-dontwarn com.infradna.tool.bridge_method_injector.BridgeMethodsAdded
-dontwarn com.infradna.tool.bridge_method_injector.WithBridgeMethods
-dontwarn edu.umd.cs.findbugs.annotations.CheckForNull
-dontwarn edu.umd.cs.findbugs.annotations.NonNull
-dontwarn edu.umd.cs.findbugs.annotations.SuppressFBWarnings
-dontwarn java.beans.ConstructorProperties
-dontwarn java.beans.Transient

-dontwarn io.jsonwebtoken.JwtBuilder
-dontwarn io.jsonwebtoken.Jwts
-dontwarn io.jsonwebtoken.SignatureAlgorithm
-dontwarn okhttp3.OkUrlFactory

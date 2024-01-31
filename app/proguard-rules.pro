# Fix missing type error with signed builds
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * implements java.lang.reflect.Type

# Fix missing constructor error with signed builds
-keepclassmembers,allowobfuscation class be.ppareit.swiftp.server.* {
  <init>(...);
}

# Make sure certain things are not dumped into logcat with signed builds eg password (that is already
# dealt with in forBackups elsewhere and this here is being kept for pull request reasons).
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}
-assumenosideeffects class net.vrallev.android.cat.Cat {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}
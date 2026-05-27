# ProGuard rules for SVM Jaidevi Portal Android App

# Keep webview methods
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(***);
}
-keepclassmembers class * extends android.webkit.WebChromeClient {
    public void *(***);
}

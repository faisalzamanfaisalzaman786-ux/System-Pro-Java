package com.systempro.app;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // ویب ویو کو انیشلائز کرنا
        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        
        // --- مینو اور بٹنوں کو چلانے کے لیے ضروری سیٹنگز ---
        settings.setJavaScriptEnabled(true);        // جاوا اسکرپٹ آن کریں
        settings.setDomStorageEnabled(true);       // مینو کی سٹیٹ اور لوکل سٹوریج کے لیے
        settings.setDatabaseEnabled(true);         // ڈیٹا بیس سپورٹ
        settings.setAllowFileAccess(true);         // فائل ایکسیس
        settings.setAllowContentAccess(true);
        
        // مینو کو ہموار (smooth) چلانے کے لیے
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        // ویب ویو کلائنٹس سیٹ کرنا تاکہ لنکس ایپ کے اندر ہی کھلیں
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        // آپ کی اپلوڈ کردہ ایچ ٹی ایم ایل فائل لوڈ کرنا
        webView.loadUrl("file:///android_asset/index.html");
    }

    // بیک بٹن دبانے پر ایپ بند ہونے کے بجائے مینو یا پچھلا پیج بند ہوگا
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}

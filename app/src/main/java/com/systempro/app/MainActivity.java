package com.systempro.app;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.PermissionRequest;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        
        // 1. انٹرنیٹ اور جاوا اسکرپٹ کو فعال کرنا
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true); // مینو اور ڈیٹا لوڈنگ کے لیے لازمی
        settings.setDatabaseEnabled(true);
        
        // 2. نیٹ ورک اور مینو بٹنز کی سپورٹ
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW); // تاکہ نیٹ سے ڈیٹا آ سکے

        // 3. کلائنٹس سیٹ کرنا (تاکہ لنکس اور مینو ایپ کے اندر کھلیں)
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }
        });

        // 4. فائل لوڈ کرنا
        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}

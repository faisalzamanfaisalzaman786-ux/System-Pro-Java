package com.systempro.myapp;  // آپ کے پیکج کے مطابق تبدیل کریں

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.net.Uri;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.webkit.GeolocationPermissions;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private ValueCallback<Uri[]> filePathCallback;
    private static final int FILE_CHOOSER_REQUEST_CODE = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Android 6+ پر لوکیشن پرمیشن چیک کریں
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }

        webView = findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();

        // تمام ضروری سیٹنگز
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);          // localStorage, sessionStorage
        webSettings.setAllowFileAccess(true);            // file:///android_asset تک رسائی
        webSettings.setAllowContentAccess(true);
        webSettings.setGeolocationEnabled(true);         // لوکیشن API
        webSettings.setDatabaseEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setAllowUniversalAccessFromFileURLs(true);  // کراس اوریجن API calls
        webSettings.setAllowFileAccessFromFileURLs(true);

        // ویڈیو/آڈیو خود بخود چلانے کی اجازت (الارم کے لیے)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webSettings.setMediaPlaybackRequiresUserGesture(false);
        }

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            // لوکیشن کی اجازت
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin,
                                                           GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }

            // فائل چووزر (تصویر اپ لوڈ کے لیے)
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                             FileChooserParams fileChooserParams) {
                MainActivity.this.filePathCallback = filePathCallback;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, FILE_CHOOSER_REQUEST_CODE);
                return true;
            }

            // Android 5.0+ پر کیمرہ/مائیکروفون جیسی پرمیشنز کے لیے
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                // تمام مطلوبہ پرمیشنز کی فہرست
                String[] resources = request.getResources();
                boolean shouldGrant = true;
                for (String resource : resources) {
                    if (resource.equals(PermissionRequest.RESOURCE_VIDEO_CAPTURE) ||
                        resource.equals(PermissionRequest.RESOURCE_AUDIO_CAPTURE)) {
                        // یہاں آپ صارف سے پوچھ سکتے ہیں، لیکن ہم خود بخود دے رہے ہیں
                        continue;
                    } else {
                        shouldGrant = false;
                        break;
                    }
                }
                if (shouldGrant) {
                    request.grant(request.getResources());
                } else {
                    request.deny();
                }
            }

            // اگر permission request کو صارف سے پوچھنا ہو تو اسے یہاں ہینڈل کریں
            // ہم نے اوپر خود بخود دے دیا ہے
        });

        webView.loadUrl("file:///android_asset/index.html");
    }

    // فائل چووزر کا نتیجہ
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                Uri[] uris = new Uri[]{data.getData()};
                filePathCallback.onReceiveValue(uris);
            } else {
                filePathCallback.onReceiveValue(null);
            }
            filePathCallback = null;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // لوکیشن پرمیشن کا نتیجہ
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // پرمیشن مل گئی، WebView کو ریفریش کریں تاکہ لوکیشن دوبارہ حاصل ہو سکے
                webView.reload();
            }
        }
    }
}

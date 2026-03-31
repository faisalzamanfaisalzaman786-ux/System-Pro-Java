package com.systempro.app;  // اپنا پیکج نام یہاں لکھیں

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.webkit.GeolocationPermissions;
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
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "WebViewDebug";
    private WebView webView;
    private ValueCallback<Uri[]> filePathCallback;
    private static final int FILE_CHOOSER_REQUEST_CODE = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private boolean permissionGranted = false;
    private boolean hasRequestedPermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // لوکیشن پرمیشن چیک کریں (Android 6+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
                hasRequestedPermission = true;
            } else {
                permissionGranted = true;
            }
        } else {
            permissionGranted = true;
        }

        // WebView ترتیب دیں
        webView = findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);          // localStorage, sessionStorage
        webSettings.setAllowFileAccess(true);            // file:///android_asset تک رسائی
        webSettings.setAllowContentAccess(true);
        webSettings.setGeolocationEnabled(true);         // لوکیشن API
        webSettings.setDatabaseEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setAllowUniversalAccessFromFileURLs(true);  // کراس اوریجن API کالز
        webSettings.setAllowFileAccessFromFileURLs(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webSettings.setMediaPlaybackRequiresUserGesture(false);  // بغیر ٹچ کے آواز چلانے کے لیے
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.e(TAG, "WebView error: " + errorCode + " - " + description + " at " + failingUrl);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin,
                                                           GeolocationPermissions.Callback callback) {
                if (permissionGranted) {
                    callback.invoke(origin, true, false);
                } else {
                    // اگر پرمیشن ابھی نہیں ملی تو تھوڑی دیر بعد ریٹرائی کریں
                    new android.os.Handler().postDelayed(() -> {
                        if (permissionGranted) {
                            callback.invoke(origin, true, false);
                        } else {
                            callback.invoke(origin, false, false);
                        }
                    }, 1000);
                }
            }

            @Override
            public void onPermissionRequest(PermissionRequest request) {
                // کیمرہ/مائیکروفون جیسی پرمیشنز خود بخود قبول کریں
                request.grant(request.getResources());
            }

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

            @Override
            public void onConsoleMessage(String message, int lineNumber, String sourceID) {
                Log.d(TAG, "Console: " + message + " (line " + lineNumber + ")");
            }
        });

        // اگر پرمیشن پہلے سے موجود ہے تو فوراً لوڈ کریں
        if (permissionGranted) {
            webView.loadUrl("file:///android_asset/index.html");
        } else {
            // پرمیشن کی درخواست کا انتظار کریں
        }
    }

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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionGranted = true;
                // صفحہ لوڈ کریں (اگر پہلے لوڈ نہیں کیا)
                if (webView != null && webView.getUrl() == null) {
                    webView.loadUrl("file:///android_asset/index.html");
                } else {
                    webView.reload();
                }
            } else {
                // پرمیشن نہیں ملی تو بھی صفحہ لوڈ کریں (لیکن لوکیشن نہیں چلے گی)
                if (webView != null && webView.getUrl() == null) {
                    webView.loadUrl("file:///android_asset/index.html");
                }
            }
        }
    }
}

package com.system.pro;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends Activity {

    private WebView webView;
    private Button btnFlash, btnGenApk;
    private CameraManager cameraManager;
    private String cameraId;
    private boolean isFlashOn = false;

    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ویو انیشیلائز کریں
        webView = findViewById(R.id.webView);
        btnFlash = findViewById(R.id.btnFlash);
        btnGenApk = findViewById(R.id.btnGenApk);

        // کیمرہ مینیجر (فلش کے لیے)
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameraList = cameraManager.getCameraIdList();
            if (cameraList.length > 0) cameraId = cameraList[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        // ویب ویو سیٹ اپ
        setupWebView();

        // فلش بٹن کا عمل
        btnFlash.setOnClickListener(v -> toggleFlashlight());

        // APK جنریٹ بٹن (ابھی صرف ڈیمو)
        btnGenApk.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "APK جنریشن ابھی beta مرحلے میں ہے", Toast.LENGTH_LONG).show();
            // یہاں آپ مستقبل میں aapt یا Pack جیسے ٹولز سے APK بنا سکتے ہیں
        });

        // اگر Android 6+ ہو تو اسٹوریج اور کیمرہ کی اجازت لیں
        checkPermissions();
    }

    private void setupWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient()); // بہت ضروری
        // assets فولڈر میں index.html لوڈ کریں
        webView.loadUrl("file:///android_asset/index.html");
    }

    private void toggleFlashlight() {
        if (cameraId == null) {
            Toast.makeText(this, "اس فون میں فلش نہیں ہے", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            if (isFlashOn) {
                cameraManager.setTorchMode(cameraId, false);
                isFlashOn = false;
                btnFlash.setText("Flash ON");
            } else {
                cameraManager.setTorchMode(cameraId, true);
                isFlashOn = true;
                btnFlash.setText("Flash OFF");
            }
        } catch (CameraAccessException e) {
            Toast.makeText(this, "فلش خرابی: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "اجازتیں منظور ہوگئیں", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "کیمرے کی اجازت ضروری ہے", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // ایپ بند ہونے پر فلش بند کریں
        if (isFlashOn) {
            try {
                cameraManager.setTorchMode(cameraId, false);
                isFlashOn = false;
                btnFlash.setText("Flash ON");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // بیک بٹن دبانے پر WebView واپس جا سکے
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}

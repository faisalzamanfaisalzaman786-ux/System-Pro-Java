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
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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

        // Views initialization
        webView = findViewById(R.id.webView);
        btnFlash = findViewById(R.id.btnFlash);
        btnGenApk = findViewById(R.id.btnGenApk);

        // Camera manager for flashlight
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameraList = cameraManager.getCameraIdList();
            if (cameraList.length > 0) cameraId = cameraList[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        setupWebView();
        setupButtons();
        checkPermissions();
    }

    private void setupWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        // Add JavaScript interface for communication with HTML
        webView.addJavascriptInterface(new AndroidBridge(), "AndroidBridge");
        webView.loadUrl("file:///android_asset/index.html");
    }

    private void setupButtons() {
        btnFlash.setOnClickListener(v -> toggleFlashlight());
        btnGenApk.setOnClickListener(v -> {
            // This button can trigger APK generation from Java side as well
            Toast.makeText(MainActivity.this, "APK generation feature - Coming soon", Toast.LENGTH_LONG).show();
            // You can call a method here to build APK using aapt or other tools
        });
    }

    private void toggleFlashlight() {
        if (cameraId == null) {
            Toast.makeText(this, "No flash available on this device", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Flash error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Camera permission is required for flashlight", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
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

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    // ========== GENERAL PURPOSE JAVASCRIPT INTERFACE ==========
    // This class handles all communication from the web page
    private class AndroidBridge {
        
        @JavascriptInterface
        public void generateApk(String themeDataJson) {
            // This method is called from HTML when user clicks "Generate APK"
            runOnUiThread(() -> {
                try {
                    JSONObject themeData = new JSONObject(themeDataJson);
                    String themeType = themeData.optString("themeType", "light");
                    String primaryColor = themeData.optString("primaryColor", "#00b4d8");
                    String secondaryColor = themeData.optString("secondaryColor", "#f97316");
                    String headingText = themeData.optString("headingText", "System Pro");
                    int dynamicButtonsCount = themeData.optInt("dynamicButtonsCount", 0);
                    
                    // Save theme data to a file (for future APK building)
                    saveThemeDataToFile(themeDataJson);
                    
                    // Show confirmation to user
                    Toast.makeText(MainActivity.this, 
                        "APK generation started. Theme: " + themeType, Toast.LENGTH_LONG).show();
                    
                    // Notify web page that APK generation has started
                    webView.evaluateJavascript("window.receiveApkStatus('APK generation started. Data saved.')", null);
                    
                    // Here you will later implement actual APK building using:
                    // - aapt (Android Asset Packaging Tool)
                    // - apkbuilder
                    // - or Google Pack (WebAssembly)
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    webView.evaluateJavascript("window.receiveApkStatus('Error: " + e.getMessage() + "')", null);
                }
            });
        }
        
        @JavascriptInterface
        public void saveFile(String fileName, String content) {
            // General purpose: save any file to app's private storage
            runOnUiThread(() -> {
                try {
                    File dir = getExternalFilesDir(null);
                    if (dir == null) dir = getFilesDir();
                    File file = new File(dir, fileName);
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(content.getBytes());
                    fos.close();
                    webView.evaluateJavascript("window.receiveApkStatus('File saved: " + fileName + "')", null);
                } catch (IOException e) {
                    e.printStackTrace();
                    webView.evaluateJavascript("window.receiveApkStatus('Save error: " + e.getMessage() + "')", null);
                }
            });
        }
        
        @JavascriptInterface
        public void showToast(String message) {
            runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
        }
        
        @JavascriptInterface
        public String getAppVersion() {
            // Return app version for general use
            return "System Pro v3.0";
        }
        
        @JavascriptInterface
        public void toggleFlashlightFromWeb() {
            // Allow web page to control flashlight
            runOnUiThread(() -> toggleFlashlight());
        }
    }
    
    private void saveThemeDataToFile(String jsonData) {
        try {
            File dir = getExternalFilesDir(null);
            if (dir == null) dir = getFilesDir();
            File themeFile = new File(dir, "current_theme.json");
            FileOutputStream fos = new FileOutputStream(themeFile);
            fos.write(jsonData.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

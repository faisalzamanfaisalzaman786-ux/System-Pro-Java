package com.web2app.demo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.net.http.SslError;
import android.view.KeyEvent;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private WebView webView;

    // All required permissions for the app
    private String[] requiredPermissions = {
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.VIBRATE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);

        // Check and request permissions
        checkPermissions();
    }

    // Method to check runtime permissions
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean allGranted = true;
            for (String permission : requiredPermissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                setupWebView();
            } else {
                ActivityCompat.requestPermissions(this, requiredPermissions, PERMISSION_REQUEST_CODE);
            }
        } else {
            setupWebView();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                setupWebView();
            } else {
                Toast.makeText(this, getString(R.string.permission_required_message), Toast.LENGTH_LONG).show();
                setupWebView(); // Setup with limited functionality
            }
        }
    }

    // Setup WebView with all configurations
    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();

        // Enable JavaScript and DOM storage
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        // Viewport and zoom settings
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportZoom(true);

        // File access settings
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);

        // Mixed content (HTTP/HTTPS) settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        // Cache settings
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // Set WebViewClient to handle URL loading
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                // Proceed for development purposes (use with caution)
                handler.proceed();
            }
        });

        // Set WebChromeClient for permissions and alerts
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request.grant(request.getResources());
                }
            }
        });

        // Load HTML content from assets folder
        loadHtmlFromAssets();
    }

    // Load HTML file from assets directory
    private void loadHtmlFromAssets() {
        try {
            String htmlContent = getHtmlFromAssets();
            if (htmlContent != null && !htmlContent.isEmpty()) {
                webView.loadDataWithBaseURL("file:///android_asset/", htmlContent, "text/html", "UTF-8", null);
            } else {
                webView.loadUrl("about:blank");
                Toast.makeText(this, getString(R.string.error_loading_content), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            webView.loadUrl("about:blank");
            Toast.makeText(this, getString(R.string.error_loading_content) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Read HTML file from assets
    private String getHtmlFromAssets() {
        try {
            java.io.InputStream inputStream = getAssets().open("index.html");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            return new String(buffer, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Handle back button for WebView navigation
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // Handle device rotation and configuration changes
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }
}
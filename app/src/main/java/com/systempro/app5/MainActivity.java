package com.codeforge.pro;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    // UI Components
    private EditText codeEditor;
    private TextView currentFilePath, buildStatusBadge, consoleContent;
    private Button uploadBuildBtn, downloadApkBtn, saveCurrentBtn, saveAllBtn, copyAllBtn;
    private DrawerLayout drawerLayout;
    private LinearLayout fileListContainer;
    private View leftDrawer, rightDrawer;
    
    // Data
    private Map<String, String> allFiles = new HashMap<>();
    private String currentFile = null;
    private String appIconBase64 = "";
    private String ghRepo = "", ghToken = "", pkgName = "com.codeforge.pro", appName = "CodeForge Pro";
    
    // Build state
    private boolean isBuilding = false;
    private Handler handler = new Handler();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    
    // Permission codes
    private static final int PERMISSION_REQUEST_CODE = 100;
    
    // Required permissions
    private String[] requiredPermissions = {
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        setupListeners();
        checkPermissions();
        loadSavedData();
        renderFileTree();
        setupCodeEditor();
    }
    
    private void initViews() {
        codeEditor = findViewById(R.id.codeEditor);
        currentFilePath = findViewById(R.id.currentFilePath);
        buildStatusBadge = findViewById(R.id.buildStatusBadge);
        consoleContent = findViewById(R.id.consoleContent);
        uploadBuildBtn = findViewById(R.id.uploadBuildBtn);
        downloadApkBtn = findViewById(R.id.downloadApkBtn);
        saveCurrentBtn = findViewById(R.id.saveCurrentBtn);
        saveAllBtn = findViewById(R.id.saveAllBtn);
        copyAllBtn = findViewById(R.id.copyAllBtn);
        fileListContainer = findViewById(R.id.fileListContainer);
        drawerLayout = findViewById(R.id.drawerLayout);
        leftDrawer = findViewById(R.id.leftDrawer);
        rightDrawer = findViewById(R.id.rightDrawer);
    }
    
    private void setupListeners() {
        uploadBuildBtn.setOnClickListener(v -> startBuild());
        downloadApkBtn.setOnClickListener(v -> downloadApk());
        saveCurrentBtn.setOnClickListener(v -> saveCurrentFile());
        saveAllBtn.setOnClickListener(v -> saveAllToStorage());
        copyAllBtn.setOnClickListener(v -> copyAllFiles());
        
        findViewById(R.id.menuLeftBtn).setOnClickListener(v -> openLeftDrawer());
        findViewById(R.id.settingsRightBtn).setOnClickListener(v -> openRightDrawer());
        findViewById(R.id.closeLeftDrawer).setOnClickListener(v -> closeLeftDrawer());
        findViewById(R.id.closeRightDrawer).setOnClickListener(v -> closeRightDrawer());
        findViewById(R.id.resetEditorBtn).setOnClickListener(v -> resetEditor());
        findViewById(R.id.clearConsoleBtn).setOnClickListener(v -> clearConsole());
        findViewById(R.id.saveSettingsBtn).setOnClickListener(v -> saveSettings());
        findViewById(R.id.newEmptyProjectBtn).setOnClickListener(v -> loadEmptyProject());
        findViewById(R.id.resetDemoProjectBtn).setOnClickListener(v -> loadMinimalDemo());
        findViewById(R.id.createNewFileBtn).setOnClickListener(v -> createNewFile());
        
        findViewById(R.id.iconUploadArea).setOnClickListener(v -> selectIcon());
    }
    
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            java.util.List<String> missingPermissions = new ArrayList<>();
            for (String permission : requiredPermissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    missingPermissions.add(permission);
                }
            }
            if (!missingPermissions.isEmpty()) {
                ActivityCompat.requestPermissions(this, missingPermissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
            } else {
                initApp();
            }
        } else {
            initApp();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            initApp();
        }
    }
    
    private void initApp() {
        addConsoleLog("✅ Ready. Click any file tab to edit.", false);
        updateBuildStatus("idle");
    }
    
    private void loadSavedData() {
        SharedPreferences prefs = getSharedPreferences("CodeForgePro", MODE_PRIVATE);
        
        // Load files
        String filesJson = prefs.getString("all_files", "");
        if (!filesJson.isEmpty()) {
            try {
                JSONObject obj = new JSONObject(filesJson);
                JSONArray keys = obj.names();
                if (keys != null) {
                    for (int i = 0; i < keys.length(); i++) {
                        String key = keys.getString(i);
                        allFiles.put(key, obj.getString(key));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Load settings
        ghRepo = prefs.getString("gh_repo", "");
        ghToken = prefs.getString("gh_token", "");
        pkgName = prefs.getString("pkg_name", "com.codeforge.pro");
        appName = prefs.getString("app_name", "CodeForge Pro");
        appIconBase64 = prefs.getString("app_icon", "");
        
        // Set UI values
        EditText ghRepoInput = findViewById(R.id.ghRepo);
        EditText ghTokenInput = findViewById(R.id.ghToken);
        EditText pkgNameInput = findViewById(R.id.pkgName);
        EditText appNameInput = findViewById(R.id.appName);
        
        if (ghRepoInput != null) ghRepoInput.setText(ghRepo);
        if (ghTokenInput != null) ghTokenInput.setText(ghToken);
        if (pkgNameInput != null) pkgNameInput.setText(pkgName);
        if (appNameInput != null) appNameInput.setText(appName);
        
        // Load demo if no files
        if (allFiles.isEmpty()) {
            loadMinimalDemo();
        }
        
        // Show icon preview
        showIconPreview();
    }
    
    private void saveAllToStorage() {
        syncEditorToMap();
        SharedPreferences prefs = getSharedPreferences("CodeForgePro", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        // Save files
        JSONObject filesObj = new JSONObject();
        for (Map.Entry<String, String> entry : allFiles.entrySet()) {
            try {
                filesObj.put(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        editor.putString("all_files", filesObj.toString());
        
        // Save settings
        editor.putString("gh_repo", ghRepo);
        editor.putString("gh_token", ghToken);
        editor.putString("pkg_name", pkgName);
        editor.putString("app_name", appName);
        editor.putString("app_icon", appIconBase64);
        
        editor.apply();
        addConsoleLog("All files saved", false);
        showToast("Saved");
        renderFileTree();
    }
    
    private void syncEditorToMap() {
        if (currentFile != null && codeEditor != null && allFiles.containsKey(currentFile)) {
            allFiles.put(currentFile, codeEditor.getText().toString());
        }
    }
    
    private void renderFileTree() {
        if (fileListContainer == null) return;
        fileListContainer.removeAllViews();
        
        // Create categories
        Map<String, java.util.List<String>> groups = new HashMap<>();
        groups.put("📱 Java", new ArrayList<>());
        groups.put("🎨 Layouts", new ArrayList<>());
        groups.put("📄 Manifest", new ArrayList<>());
        groups.put("⚙️ Gradle", new ArrayList<>());
        groups.put("📁 Other", new ArrayList<>());
        
        for (String path : allFiles.keySet()) {
            if (path.endsWith(".java")) {
                groups.get("📱 Java").add(path);
            } else if (path.contains("/layout/")) {
                groups.get("🎨 Layouts").add(path);
            } else if (path.contains("AndroidManifest.xml")) {
                groups.get("📄 Manifest").add(path);
            } else if (path.contains(".gradle") || path.contains("settings.gradle") || path.contains("gradle.properties")) {
                groups.get("⚙️ Gradle").add(path);
            } else {
                groups.get("📁 Other").add(path);
            }
        }
        
        for (Map.Entry<String, java.util.List<String>> entry : groups.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            
            // Category title
            TextView categoryTitle = new TextView(this);
            categoryTitle.setText(entry.getKey());
            categoryTitle.setTextSize(10);
            categoryTitle.setTextColor(0xFF9BB3E0);
            categoryTitle.setPadding(20, 16, 8, 8);
            categoryTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            fileListContainer.addView(categoryTitle);
            
            // Horizontal scroll for tabs
            ScrollView horizontalScroll = new ScrollView(this);
            horizontalScroll.setHorizontalScrollBarEnabled(true);
            horizontalScroll.setScrollContainer(true);
            
            LinearLayout tabsContainer = new LinearLayout(this);
            tabsContainer.setOrientation(LinearLayout.HORIZONTAL);
            tabsContainer.setPadding(16, 8, 16, 16);
            
            for (String path : entry.getValue()) {
                Button tab = new Button(this);
                String fileName = path.substring(path.lastIndexOf('/') + 1);
                tab.setText(fileName);
                tab.setTextSize(11);
                tab.setPadding(24, 12, 24, 12);
                tab.setBackgroundResource(android.R.drawable.editbox_background);
                
                if (currentFile != null && currentFile.equals(path)) {
                    tab.setBackgroundColor(0xFF0FC30C);
                    tab.setTextColor(0xFF00FFCC);
                }
                
                tab.setOnClickListener(v -> selectFile(path));
                tab.setOnLongClickListener(v -> {
                    deleteFile(path);
                    return true;
                });
                
                tabsContainer.addView(tab);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tab.getLayoutParams();
                params.setMargins(4, 0, 4, 0);
                tab.setLayoutParams(params);
            }
            
            horizontalScroll.addView(tabsContainer);
            fileListContainer.addView(horizontalScroll);
        }
    }
    
    private void selectFile(String path) {
        syncEditorToMap();
        currentFile = path;
        if (codeEditor != null && allFiles.containsKey(path)) {
            codeEditor.setText(allFiles.get(path));
        }
        if (currentFilePath != null) {
            currentFilePath.setText("📄 " + path);
        }
        closeLeftDrawer();
        renderFileTree();
        addConsoleLog("Opened " + path, false);
    }
    
    private void deleteFile(String path) {
        new AlertDialog.Builder(this)
            .setTitle("Delete File")
            .setMessage("Are you sure you want to delete " + path + "?")
            .setPositiveButton("Delete", (d, w) -> {
                allFiles.remove(path);
                if (currentFile != null && currentFile.equals(path)) {
                    currentFile = null;
                    codeEditor.setText("");
                    currentFilePath.setText("📄 No file selected");
                }
                saveAllToStorage();
                renderFileTree();
                showToast("Deleted " + path);
                addConsoleLog("File deleted: " + path, false);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void saveCurrentFile() {
        if (currentFile == null) {
            showToast("No file selected");
            return;
        }
        syncEditorToMap();
        saveAllToStorage();
        showToast("Saved " + currentFile.substring(currentFile.lastIndexOf('/') + 1));
    }
    
    private void resetEditor() {
        if (currentFile != null && allFiles.containsKey(currentFile)) {
            codeEditor.setText(allFiles.get(currentFile));
        } else {
            codeEditor.setText("");
        }
    }
    
    private void copyAllFiles() {
        syncEditorToMap();
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : allFiles.entrySet()) {
            sb.append("// ").append(entry.getKey()).append("\n");
            sb.append(entry.getValue()).append("\n\n");
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("CodeForge Files", sb.toString());
        clipboard.setPrimaryClip(clip);
        showToast("Copied all files");
    }
    
    private void setupCodeEditor() {
        if (codeEditor == null) return;
        codeEditor.addTextChangedListener(new TextWatcher() {
            private Handler h = new Handler();
            private Runnable runnable;
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (runnable != null) h.removeCallbacks(runnable);
                runnable = () -> syncEditorToMap();
                h.postDelayed(runnable, 300);
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void loadMinimalDemo() {
        allFiles.clear();
        String pkgPath = pkgName.replace(".", "/");
        
        allFiles.put("app/src/main/java/" + pkgPath + "/MainActivity.java",
            "package " + pkgName + ";\n\nimport android.app.Activity;\nimport android.os.Bundle;\nimport android.widget.TextView;\n\npublic class MainActivity extends Activity {\n    @Override\n    protected void onCreate(Bundle savedInstanceState) {\n        super.onCreate(savedInstanceState);\n        setContentView(R.layout.activity_main);\n        TextView tv = findViewById(R.id.textView);\n        tv.setText(\"CodeForge Pro - Minimal Demo\");\n    }\n}");
        
        allFiles.put("app/src/main/res/layout/activity_main.xml",
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n    android:layout_width=\"match_parent\"\n    android:layout_height=\"match_parent\"\n    android:gravity=\"center\"\n    android:orientation=\"vertical\"\n    android:padding=\"24dp\">\n    <TextView\n        android:id=\"@+id/textView\"\n        android:layout_width=\"wrap_content\"\n        android:layout_height=\"wrap_content\"\n        android:text=\"Hello from Minimal Demo\"\n        android:textSize=\"28sp\"\n        android:textColor=\"#00FFCC\" />\n</LinearLayout>");
        
        allFiles.put("app/src/main/res/values/strings.xml",
            "<resources>\n    <string name=\"app_name\">" + appName + "</string>\n</resources>");
        
        allFiles.put("app/src/main/res/values/colors.xml",
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n    <color name=\"colorPrimary\">#6200EE</color>\n    <color name=\"colorPrimaryDark\">#3700B3</color>\n    <color name=\"colorAccent\">#03DAC5</color>\n</resources>");
        
        allFiles.put("app/src/main/AndroidManifest.xml",
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n    package=\"" + pkgName + "\">\n    <application\n        android:allowBackup=\"true\"\n        android:icon=\"@mipmap/ic_launcher\"\n        android:label=\"@string/app_name\">\n        <activity android:name=\".MainActivity\" android:exported=\"true\">\n            <intent-filter>\n                <action android:name=\"android.intent.action.MAIN\" />\n                <category android:name=\"android.intent.category.LAUNCHER\" />\n            </intent-filter>\n        </activity>\n    </application>\n</manifest>");
        
        currentFile = allFiles.keySet().iterator().next();
        saveAllToStorage();
        renderFileTree();
        resetEditor();
        addConsoleLog("Minimal demo loaded", false);
        showToast("Minimal demo loaded");
    }
    
    private void loadEmptyProject() {
        new AlertDialog.Builder(this)
            .setTitle("Empty Project")
            .setMessage("All current files will be replaced. Continue?")
            .setPositiveButton("Yes", (d, w) -> {
                allFiles.clear();
                String pkgPath = pkgName.replace(".", "/");
                
                allFiles.put("app/src/main/java/" + pkgPath + "/MainActivity.java",
                    "package " + pkgName + ";\n\nimport android.app.Activity;\nimport android.os.Bundle;\n\npublic class MainActivity extends Activity {\n    @Override\n    protected void onCreate(Bundle savedInstanceState) {\n        super.onCreate(savedInstanceState);\n        setContentView(R.layout.activity_main);\n    }\n}");
                
                allFiles.put("app/src/main/res/layout/activity_main.xml",
                    "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n    android:layout_width=\"match_parent\"\n    android:layout_height=\"match_parent\"\n    android:gravity=\"center\"\n    android:orientation=\"vertical\">\n    <TextView\n        android:layout_width=\"wrap_content\"\n        android:layout_height=\"wrap_content\"\n        android:text=\"Hello World\"\n        android:textSize=\"24sp\" />\n</LinearLayout>");
                
                allFiles.put("app/src/main/res/values/strings.xml",
                    "<resources>\n    <string name=\"app_name\">" + appName + "</string>\n</resources>");
                
                allFiles.put("app/src/main/AndroidManifest.xml",
                    "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n    package=\"" + pkgName + "\">\n    <application\n        android:allowBackup=\"true\"\n        android:icon=\"@mipmap/ic_launcher\"\n        android:label=\"@string/app_name\">\n        <activity android:name=\".MainActivity\" android:exported=\"true\">\n            <intent-filter>\n                <action android:name=\"android.intent.action.MAIN\" />\n                <category android:name=\"android.intent.category.LAUNCHER\" />\n            </intent-filter>\n        </activity>\n    </application>\n</manifest>");
                
                currentFile = allFiles.keySet().iterator().next();
                saveAllToStorage();
                renderFileTree();
                resetEditor();
                addConsoleLog("Empty project loaded", false);
                showToast("Empty project loaded");
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void createNewFile() {
        EditText input = findViewById(R.id.newFilePath);
        if (input == null) return;
        String newPath = input.getText().toString().trim();
        if (newPath.isEmpty()) {
            showToast("Enter file path");
            return;
        }
        if (allFiles.containsKey(newPath)) {
            showToast("File already exists");
            return;
        }
        
        String content = "";
        if (newPath.endsWith(".java")) {
            String className = newPath.substring(newPath.lastIndexOf('/') + 1).replace(".java", "");
            content = "package " + pkgName + ";\n\npublic class " + className + " {\n    // TODO\n}\n";
        } else if (newPath.endsWith(".xml")) {
            content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<root/>";
        } else {
            content = "// New file content";
        }
        
        allFiles.put(newPath, content);
        saveAllToStorage();
        input.setText("");
        selectFile(newPath);
        showToast("Created " + newPath);
    }
    
    private void selectIcon() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 200);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                InputStream is = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                
                // Resize to 192x192 max
                int maxSize = 192;
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                if (width > maxSize || height > maxSize) {
                    float scale = Math.min((float) maxSize / width, (float) maxSize / height);
                    int newWidth = Math.round(width * scale);
                    int newHeight = Math.round(height * scale);
                    bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
                }
                
                // Convert to PNG Base64
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, baos);
                appIconBase64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
                
                showIconPreview();
                saveAllToStorage();
                addConsoleLog("Icon converted to PNG (" + bitmap.getWidth() + "x" + bitmap.getHeight() + ")", false);
                showToast("Icon saved");
            } catch (Exception e) {
                addConsoleLog("Icon error: " + e.getMessage(), true);
                showToast("Failed to load icon");
            }
        }
    }
    
    private void showIconPreview() {
        LinearLayout container = findViewById(R.id.iconPreviewContainer);
        TextView info = findViewById(R.id.iconInfo);
        if (container != null && !appIconBase64.isEmpty()) {
            try {
                byte[] decoded = Base64.decode(appIconBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                android.widget.ImageView img = new android.widget.ImageView(this);
                img.setImageBitmap(bitmap);
                img.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                img.setLayoutParams(new LinearLayout.LayoutParams(192, 192));
                container.removeAllViews();
                container.addView(img);
                if (info != null) info.setText("✓ PNG Icon (" + bitmap.getWidth() + "x" + bitmap.getHeight() + ")");
            } catch (Exception e) {
                container.removeAllViews();
                if (info != null) info.setText("");
            }
        } else if (container != null) {
            container.removeAllViews();
            if (info != null) info.setText("");
        }
    }
    
    private void saveSettings() {
        EditText ghRepoInput = findViewById(R.id.ghRepo);
        EditText ghTokenInput = findViewById(R.id.ghToken);
        EditText pkgNameInput = findViewById(R.id.pkgName);
        EditText appNameInput = findViewById(R.id.appName);
        
        if (ghRepoInput != null) ghRepo = ghRepoInput.getText().toString().trim();
        if (ghTokenInput != null) ghToken = ghTokenInput.getText().toString().trim();
        if (pkgNameInput != null) pkgName = pkgNameInput.getText().toString().trim();
        if (appNameInput != null) appName = appNameInput.getText().toString().trim();
        
        saveAllToStorage();
        showToast("Settings saved");
        addConsoleLog("Settings saved", false);
    }
    
    private void startBuild() {
        if (isBuilding) {
            showToast("Build already in progress");
            return;
        }
        
        syncEditorToMap();
        
        if (ghToken.isEmpty() || ghRepo.isEmpty()) {
            addConsoleLog("Missing GitHub token or repo", true);
            showToast("Set GitHub settings first");
            return;
        }
        
        String[] parts = ghRepo.split("/");
        if (parts.length != 2) {
            addConsoleLog("Invalid repo format. Use owner/repo", true);
            return;
        }
        
        String owner = parts[0];
        String repo = parts[1];
        
        isBuilding = true;
        uploadBuildBtn.setEnabled(false);
        uploadBuildBtn.setText("Uploading...");
        updateBuildStatus("uploading");
        
        executorService.execute(() -> {
            boolean success = uploadToGitHub(owner, repo, ghToken);
            runOnUiThread(() -> {
                if (success) {
                    triggerGitHubAction(owner, repo, ghToken);
                } else {
                    isBuilding = false;
                    uploadBuildBtn.setEnabled(true);
                    uploadBuildBtn.setText("Upload & Build");
                    updateBuildStatus("failed");
                }
            });
        });
    }
    
    private boolean uploadToGitHub(String owner, String repo, String token) {
        try {
            for (Map.Entry<String, String> entry : allFiles.entrySet()) {
                String path = entry.getKey();
                String content = entry.getValue();
                String encoded = Base64.encodeToString(content.getBytes("UTF-8"), Base64.NO_WRAP);
                
                String url = "https://api.github.com/repos/" + owner + "/" + repo + "/contents/" + path;
                
                // Check if file exists
                String sha = null;
                HttpURLConnection checkConn = null;
                try {
                    checkConn = (HttpURLConnection) new URL(url + "?ref=main").openConnection();
                    checkConn.setRequestProperty("Authorization", "token " + token);
                    if (checkConn.getResponseCode() == 200) {
                        InputStream is = checkConn.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) sb.append(line);
                        JSONObject obj = new JSONObject(sb.toString());
                        sha = obj.getString("sha");
                    }
                } catch (Exception e) {
                    // File doesn't exist
                }
                if (checkConn != null) checkConn.disconnect();
                
                JSONObject body = new JSONObject();
                body.put("message", "Update " + path);
                body.put("content", encoded);
                body.put("branch", "main");
                if (sha != null) body.put("sha", sha);
                
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Authorization", "token " + token);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                
                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes());
                os.flush();
                os.close();
                
                int responseCode = conn.getResponseCode();
                if (responseCode != 200 && responseCode != 201) {
                    addConsoleLog("Failed to upload " + path + ": " + responseCode, true);
                    return false;
                }
                addConsoleLog("⬆️ " + path, false);
            }
            
            // Upload icon if exists
            if (!appIconBase64.isEmpty()) {
                String url = "https://api.github.com/repos/" + owner + "/" + repo + "/contents/build_assets/app_icon.png";
                JSONObject body = new JSONObject();
                body.put("message", "Update app icon");
                body.put("content", appIconBase64);
                body.put("branch", "main");
                
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Authorization", "token " + token);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                
                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes());
                os.flush();
                os.close();
                
                if (conn.getResponseCode() == 200 || conn.getResponseCode() == 201) {
                    addConsoleLog("Icon uploaded", false);
                }
            }
            
            return true;
        } catch (Exception e) {
            addConsoleLog("Upload error: " + e.getMessage(), true);
            return false;
        }
    }
    
    private void triggerGitHubAction(String owner, String repo, String token) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("event_type", "trigger_b64_build");
            
            JSONObject clientPayload = new JSONObject();
            clientPayload.put("package_name", pkgName);
            clientPayload.put("app_name", appName);
            clientPayload.put("icon", appIconBase64);
            clientPayload.put("build_time", System.currentTimeMillis());
            
            // Convert files to JSON
            JSONObject filesObj = new JSONObject();
            for (Map.Entry<String, String> entry : allFiles.entrySet()) {
                filesObj.put(entry.getKey(), entry.getValue());
            }
            clientPayload.put("files", filesObj);
            payload.put("client_payload", clientPayload);
            
            String url = "https://api.github.com/repos/" + owner + "/" + repo + "/dispatches";
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "token " + token);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            
            OutputStream os = conn.getOutputStream();
            os.write(payload.toString().getBytes());
            os.flush();
            os.close();
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 204) {
                addConsoleLog("✅ Build triggered successfully!", false);
                showToast("Build started");
                updateBuildStatus("building");
                isBuilding = false;
                uploadBuildBtn.setEnabled(true);
                uploadBuildBtn.setText("Upload & Build");
            } else {
                addConsoleLog("Dispatch error: " + responseCode, true);
                isBuilding = false;
                uploadBuildBtn.setEnabled(true);
                uploadBuildBtn.setText("Upload & Build");
                updateBuildStatus("failed");
            }
        } catch (Exception e) {
            addConsoleLog("Dispatch failed: " + e.getMessage(), true);
            isBuilding = false;
            uploadBuildBtn.setEnabled(true);
            uploadBuildBtn.setText("Upload & Build");
            updateBuildStatus("failed");
        }
    }
    
    private void downloadApk() {
        if (ghRepo.isEmpty() || ghToken.isEmpty()) {
            showToast("Set GitHub settings first");
            return;
        }
        
        String[] parts = ghRepo.split("/");
        if (parts.length != 2) return;
        
        String url = "https://api.github.com/repos/" + parts[0] + "/" + parts[1] + "/releases/tags/latest-release";
        executorService.execute(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestProperty("Authorization", "token " + ghToken);
                
                if (conn.getResponseCode() == 200) {
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    JSONObject release = new JSONObject(sb.toString());
                    JSONArray assets = release.getJSONArray("assets");
                    
                    String downloadUrl = null;
                    for (int i = 0; i < assets.length(); i++) {
                        JSONObject asset = assets.getJSONObject(i);
                        if (asset.getString("name").endsWith(".apk")) {
                            downloadUrl = asset.getString("browser_download_url");
                            break;
                        }
                    }
                    
                    if (downloadUrl != null) {
                        final String finalUrl = downloadUrl;
                        runOnUiThread(() -> {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl));
                            startActivity(intent);
                            showToast("Download started");
                        });
                    } else {
                        runOnUiThread(() -> addConsoleLog("APK not found. Build first.", true));
                    }
                } else {
                    runOnUiThread(() -> addConsoleLog("No release found. Build first.", true));
                }
            } catch (Exception e) {
                runOnUiThread(() -> addConsoleLog("Download error: " + e.getMessage(), true));
            }
        });
    }
    
    private void addConsoleLog(String msg, boolean isError) {
        runOnUiThread(() -> {
            if (consoleContent == null) return;
            String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
            String coloredMsg = (isError ? "❌ " : "✓ ") + msg;
            consoleContent.append(time + " " + coloredMsg + "\n");
            // Scroll to bottom
            ScrollView scroll = findViewById(R.id.consoleScroll);
            if (scroll != null) scroll.fullScroll(View.FOCUS_DOWN);
        });
    }
    
    private void clearConsole() {
        if (consoleContent != null) {
            consoleContent.setText("");
            addConsoleLog("Console cleared", false);
        }
    }
    
    private void updateBuildStatus(String status) {
        runOnUiThread(() -> {
            if (buildStatusBadge != null) {
                buildStatusBadge.setText(status);
            }
        });
    }
    
    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }
    
    private void openLeftDrawer() {
        if (drawerLayout != null) drawerLayout.openDrawer(leftDrawer);
    }
    
    private void closeLeftDrawer() {
        if (drawerLayout != null) drawerLayout.closeDrawer(leftDrawer);
    }
    
    private void openRightDrawer() {
        if (drawerLayout != null) drawerLayout.openDrawer(rightDrawer);
    }
    
    private void closeRightDrawer() {
        if (drawerLayout != null) drawerLayout.closeDrawer(rightDrawer);
    }
}
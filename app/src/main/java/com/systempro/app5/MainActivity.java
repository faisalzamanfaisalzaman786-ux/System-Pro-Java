package com.systempro.javaui;

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
import android.provider.MediaStore;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    // UI Components
    private DrawerLayout drawerLayout;
    private LinearLayout leftDrawer, rightDrawer;
    private LinearLayout fileListContainer;
    private EditText codeEditor;
    private TextView currentFilePath, consoleContent, buildStatusBadge;
    private Button saveCurrentBtn, saveAllBtn, copyAllBtn, buildAndTriggerBtn;
    private Button downloadApkBtn, downloadAabBtn;
    private EditText ghRepo, ghToken, pkgName, appName;
    private LinearLayout iconUploadArea;
    private TextView iconInfo;
    private LinearLayout iconPreviewContainer;
    private Button saveSettingsBtn;
    private EditText newFilePath;
    private Button createNewFileBtn;
    private Button newEmptyProjectBtn, resetDemoProjectBtn;
    private View progressBar;
    private View progressFill;
    private View statusDot;
    private TextView statusText, statusDetail;
    
    // Data
    private Map<String, String> allFiles = new HashMap<>();
    private String currentFile = null;
    private String appIconBase64 = "";
    private String ghRepoStr = "", ghTokenStr = "", pkgNameStr = "com.codeforge.pro", appNameStr = "CodeForge Pro";
    
    // State
    private boolean isProcessing = false;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private android.os.Handler mainHandler = new android.os.Handler();
    private android.os.Handler pollHandler = new android.os.Handler();
    private Runnable pollRunnable;
    
    // Permission
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int ICON_PICK_CODE = 200;
    private static final int MAX_ICON_SIZE = 192;
    
    // GitHub
    private String currentOwner = "", currentRepo = "";

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
        drawerLayout = findViewById(R.id.drawerLayout);
        leftDrawer = findViewById(R.id.leftDrawer);
        rightDrawer = findViewById(R.id.rightDrawer);
        fileListContainer = findViewById(R.id.fileListContainer);
        codeEditor = findViewById(R.id.codeEditor);
        currentFilePath = findViewById(R.id.currentFilePath);
        consoleContent = findViewById(R.id.consoleContent);
        buildStatusBadge = findViewById(R.id.buildStatusBadge);
        saveCurrentBtn = findViewById(R.id.saveCurrentBtn);
        saveAllBtn = findViewById(R.id.saveAllBtn);
        copyAllBtn = findViewById(R.id.copyAllBtn);
        buildAndTriggerBtn = findViewById(R.id.buildAndTriggerBtn);
        downloadApkBtn = findViewById(R.id.downloadApkBtn);
        downloadAabBtn = findViewById(R.id.downloadAabBtn);
        ghRepo = findViewById(R.id.ghRepo);
        ghToken = findViewById(R.id.ghToken);
        pkgName = findViewById(R.id.pkgName);
        appName = findViewById(R.id.appName);
        iconUploadArea = findViewById(R.id.iconUploadArea);
        iconInfo = findViewById(R.id.iconInfo);
        iconPreviewContainer = findViewById(R.id.iconPreviewContainer);
        saveSettingsBtn = findViewById(R.id.saveSettingsBtn);
        newFilePath = findViewById(R.id.newFilePath);
        createNewFileBtn = findViewById(R.id.createNewFileBtn);
        newEmptyProjectBtn = findViewById(R.id.newEmptyProjectBtn);
        resetDemoProjectBtn = findViewById(R.id.resetDemoProjectBtn);
        progressBar = findViewById(R.id.progressBar);
        progressFill = findViewById(R.id.progressFill);
        statusDot = findViewById(R.id.statusDot);
        statusText = findViewById(R.id.statusText);
        statusDetail = findViewById(R.id.statusDetail);
        
        // Set initial status
        setStatus("Ready", "");
    }
    
    private void setupListeners() {
        findViewById(R.id.menuLeftBtn).setOnClickListener(v -> openLeftDrawer());
        findViewById(R.id.closeLeftDrawer).setOnClickListener(v -> closeLeftDrawer());
        findViewById(R.id.settingsRightBtn).setOnClickListener(v -> openRightDrawer());
        findViewById(R.id.closeRightDrawer).setOnClickListener(v -> closeRightDrawer());
        findViewById(R.id.resetEditorBtn).setOnClickListener(v -> resetEditor());
        findViewById(R.id.clearConsoleBtn).setOnClickListener(v -> clearConsole());
        findViewById(R.id.toggleConsoleBtn).setOnClickListener(v -> toggleConsole());
        findViewById(R.id.consoleHeader).setOnClickListener(v -> toggleConsole());
        
        saveCurrentBtn.setOnClickListener(v -> saveCurrentFile());
        saveAllBtn.setOnClickListener(v -> saveAllToLocal());
        copyAllBtn.setOnClickListener(v -> copyAllFiles());
        buildAndTriggerBtn.setOnClickListener(v -> saveAndTriggerBuild());
        saveSettingsBtn.setOnClickListener(v -> saveSettings());
        createNewFileBtn.setOnClickListener(v -> createNewFile());
        newEmptyProjectBtn.setOnClickListener(v -> loadEmptyProject());
        resetDemoProjectBtn.setOnClickListener(v -> loadMinimalDemo());
        downloadApkBtn.setOnClickListener(v -> downloadArtifact("apk"));
        downloadAabBtn.setOnClickListener(v -> downloadArtifact("aab"));
        
        iconUploadArea.setOnClickListener(v -> selectIcon());
    }
    
    private void checkPermissions() {
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.INTERNET);
        permissions.add(Manifest.permission.ACCESS_NETWORK_STATE);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> missing = new ArrayList<>();
            for (String perm : permissions) {
                if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                    missing.add(perm);
                }
            }
            if (!missing.isEmpty()) {
                ActivityCompat.requestPermissions(this, missing.toArray(new String[0]), PERMISSION_REQUEST_CODE);
            }
        }
    }
    
    private void loadSavedData() {
        SharedPreferences prefs = getSharedPreferences("CodeForgePro", MODE_PRIVATE);
        
        String filesJson = prefs.getString("all_files", "");
        if (!filesJson.isEmpty()) {
            try {
                JSONObject obj = new JSONObject(filesJson);
                for (String key : obj.keySet()) {
                    allFiles.put(key, obj.getString(key));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        ghRepoStr = prefs.getString("gh_repo", "");
        ghTokenStr = prefs.getString("gh_token", "");
        pkgNameStr = prefs.getString("pkg_name", "com.codeforge.pro");
        appNameStr = prefs.getString("app_name", "CodeForge Pro");
        appIconBase64 = prefs.getString("app_icon", "");
        
        ghRepo.setText(ghRepoStr);
        ghToken.setText(ghTokenStr);
        pkgName.setText(pkgNameStr);
        appName.setText(appNameStr);
        
        if (allFiles.isEmpty()) {
            loadMinimalDemo();
        }
        
        showIconPreview();
    }
    
    private void saveAllToLocal() {
        syncEditorToMap();
        SharedPreferences prefs = getSharedPreferences("CodeForgePro", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        JSONObject filesObj = new JSONObject();
        for (Map.Entry<String, String> entry : allFiles.entrySet()) {
            try {
                filesObj.put(entry.getKey(), entry.getValue());
            } catch (Exception e) {}
        }
        editor.putString("all_files", filesObj.toString());
        editor.putString("gh_repo", ghRepoStr);
        editor.putString("gh_token", ghTokenStr);
        editor.putString("pkg_name", pkgNameStr);
        editor.putString("app_name", appNameStr);
        editor.putString("app_icon", appIconBase64);
        editor.apply();
        
        addConsoleLog("All files saved locally", false);
        Toast.makeText(this, "Saved locally", Toast.LENGTH_SHORT).show();
        renderFileTree();
    }
    
    private void syncEditorToMap() {
        if (currentFile != null && allFiles.containsKey(currentFile)) {
            allFiles.put(currentFile, codeEditor.getText().toString());
        }
    }
    
    private void renderFileTree() {
        fileListContainer.removeAllViews();
        
        Map<String, List<String>> groups = new HashMap<>();
        groups.put("📱 Java", new ArrayList<>());
        groups.put("🎨 Layouts", new ArrayList<>());
        groups.put("📄 Manifest", new ArrayList<>());
        groups.put("📁 Other", new ArrayList<>());
        
        for (String path : allFiles.keySet()) {
            if (path.endsWith(".java")) {
                groups.get("📱 Java").add(path);
            } else if (path.contains("/layout/")) {
                groups.get("🎨 Layouts").add(path);
            } else if (path.contains("AndroidManifest.xml")) {
                groups.get("📄 Manifest").add(path);
            } else {
                groups.get("📁 Other").add(path);
            }
        }
        
        for (Map.Entry<String, List<String>> entry : groups.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            
            TextView catTitle = new TextView(this);
            catTitle.setText(entry.getKey());
            catTitle.setTextSize(10);
            catTitle.setTextColor(0xFF9BB3E0);
            catTitle.setPadding(20, 16, 8, 8);
            catTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            fileListContainer.addView(catTitle);
            
            LinearLayout tabsContainer = new LinearLayout(this);
            tabsContainer.setOrientation(LinearLayout.HORIZONTAL);
            tabsContainer.setPadding(16, 8, 16, 16);
            tabsContainer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            
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
            }
            
            ScrollView scroll = new ScrollView(this);
            scroll.addView(tabsContainer);
            fileListContainer.addView(scroll);
        }
    }
    
    private void selectFile(String path) {
        syncEditorToMap();
        currentFile = path;
        codeEditor.setText(allFiles.get(path));
        currentFilePath.setText("📄 " + path);
        closeLeftDrawer();
        renderFileTree();
        addConsoleLog("Opened " + path, false);
    }
    
    private void deleteFile(String path) {
        new AlertDialog.Builder(this)
            .setTitle("Delete File")
            .setMessage("Delete " + path + "?")
            .setPositiveButton("Delete", (d, w) -> {
                allFiles.remove(path);
                if (currentFile != null && currentFile.equals(path)) {
                    currentFile = null;
                    codeEditor.setText("");
                    currentFilePath.setText("📄 No file selected");
                }
                saveAllToLocal();
                renderFileTree();
                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void saveCurrentFile() {
        if (currentFile == null) {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
            return;
        }
        syncEditorToMap();
        saveAllToLocal();
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
    }
    
    private void resetEditor() {
        if (currentFile != null && allFiles.containsKey(currentFile)) {
            codeEditor.setText(allFiles.get(currentFile));
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
        clipboard.setPrimaryClip(ClipData.newPlainText("CodeForge Files", sb.toString()));
        Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show();
    }
    
    private void setupCodeEditor() {
        codeEditor.addTextChangedListener(new TextWatcher() {
            private android.os.Handler h = new android.os.Handler();
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
        String pkgPath = pkgNameStr.replace(".", "/");
        
        allFiles.put("app/src/main/java/" + pkgPath + "/MainActivity.java",
            "package " + pkgNameStr + ";\n\nimport android.os.Bundle;\nimport android.widget.TextView;\nimport androidx.appcompat.app.AppCompatActivity;\n\npublic class MainActivity extends AppCompatActivity {\n    @Override\n    protected void onCreate(Bundle savedInstanceState) {\n        super.onCreate(savedInstanceState);\n        setContentView(R.layout.activity_main);\n        TextView tv = findViewById(R.id.textView);\n        tv.setText(\"" + appNameStr + "!\");\n    }\n}");
        
        allFiles.put("app/src/main/res/layout/activity_main.xml",
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n    android:layout_width=\"match_parent\"\n    android:layout_height=\"match_parent\"\n    android:orientation=\"vertical\"\n    android:gravity=\"center\"\n    android:padding=\"24dp\">\n    <TextView\n        android:id=\"@+id/textView\"\n        android:layout_width=\"wrap_content\"\n        android:layout_height=\"wrap_content\"\n        android:text=\"" + appNameStr + "!\"\n        android:textSize=\"32sp\"\n        android:textStyle=\"bold\"\n        android:textColor=\"#6200EE\" />\n</LinearLayout>");
        
        allFiles.put("app/src/main/res/values/strings.xml",
            "<resources>\n    <string name=\"app_name\">" + appNameStr + "</string>\n</resources>");
        
        allFiles.put("app/src/main/AndroidManifest.xml",
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n    package=\"" + pkgNameStr + "\">\n    <application\n        android:allowBackup=\"true\"\n        android:icon=\"@mipmap/ic_launcher\"\n        android:label=\"@string/app_name\"\n        android:theme=\"@style/Theme.AppCompat.Light.DarkActionBar\">\n        <activity android:name=\".MainActivity\" android:exported=\"true\">\n            <intent-filter>\n                <action android:name=\"android.intent.action.MAIN\" />\n                <category android:name=\"android.intent.category.LAUNCHER\" />\n            </intent-filter>\n        </activity>\n    </application>\n</manifest>");
        
        currentFile = allFiles.keySet().iterator().next();
        saveAllToLocal();
        renderFileTree();
        addConsoleLog("Demo project loaded", false);
    }
    
    private void loadEmptyProject() {
        allFiles.clear();
        currentFile = null;
        saveAllToLocal();
        renderFileTree();
        codeEditor.setText("");
        currentFilePath.setText("📄 No file selected");
        Toast.makeText(this, "Empty project", Toast.LENGTH_SHORT).show();
        addConsoleLog("Empty project created", false);
    }
    
    private void createNewFile() {
        String newPath = newFilePath.getText().toString().trim();
        if (newPath.isEmpty()) {
            Toast.makeText(this, "Enter file path", Toast.LENGTH_SHORT).show();
            return;
        }
        if (allFiles.containsKey(newPath)) {
            Toast.makeText(this, "File exists", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String content = "";
        if (newPath.endsWith(".java")) {
            content = "package " + pkgNameStr + ";\n\npublic class " + newPath.substring(newPath.lastIndexOf('/') + 1).replace(".java", "") + " {\n    // TODO\n}\n";
        } else if (newPath.endsWith(".xml")) {
            content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<root/>\n";
        } else {
            content = "// New file content\n";
        }
        
        allFiles.put(newPath, content);
        saveAllToLocal();
        newFilePath.setText("");
        selectFile(newPath);
        Toast.makeText(this, "Created", Toast.LENGTH_SHORT).show();
    }
    
    private void selectIcon() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, ICON_PICK_CODE);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ICON_PICK_CODE && resultCode == RESULT_OK && data != null) {
            try {
                Uri imageUri = data.getData();
                InputStream is = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                if (width > MAX_ICON_SIZE || height > MAX_ICON_SIZE) {
                    float scale = Math.min((float) MAX_ICON_SIZE / width, (float) MAX_ICON_SIZE / height);
                    int newWidth = Math.round(width * scale);
                    int newHeight = Math.round(height * scale);
                    bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
                }
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, baos);
                appIconBase64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
                
                showIconPreview();
                saveAllToLocal();
                addConsoleLog("Icon saved (" + bitmap.getWidth() + "x" + bitmap.getHeight() + ")", false);
                Toast.makeText(this, "Icon saved", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                addConsoleLog("Icon error: " + e.getMessage(), true);
            }
        }
    }
    
    private void showIconPreview() {
        iconPreviewContainer.removeAllViews();
        if (appIconBase64 != null && !appIconBase64.isEmpty()) {
            try {
                byte[] decoded = Base64.decode(appIconBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                android.widget.ImageView img = new android.widget.ImageView(this);
                img.setImageBitmap(bitmap);
                img.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                img.setLayoutParams(new LinearLayout.LayoutParams(192, 192));
                iconPreviewContainer.addView(img);
                iconInfo.setText("✓ Icon ready");
            } catch (Exception e) {
                iconInfo.setText("");
            }
        } else {
            iconInfo.setText("");
        }
    }
    
    private void saveSettings() {
        ghRepoStr = ghRepo.getText().toString().trim();
        ghTokenStr = ghToken.getText().toString().trim();
        pkgNameStr = pkgName.getText().toString().trim();
        appNameStr = appName.getText().toString().trim();
        saveAllToLocal();
        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
        addConsoleLog("Settings saved", false);
    }
    
    private void addConsoleLog(String msg, boolean isError) {
        mainHandler.post(() -> {
            String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
            String prefix = isError ? "❌ " : "✓ ";
            consoleContent.append(time + " " + prefix + msg + "\n");
            ScrollView scroll = findViewById(R.id.consoleScroll);
            if (scroll != null) scroll.fullScroll(View.FOCUS_DOWN);
        });
    }
    
    private void clearConsole() {
        consoleContent.setText("");
        addConsoleLog("Console cleared", false);
    }
    
    private void setStatus(String status, String detail) {
        mainHandler.post(() -> {
            statusText.setText(status);
            buildStatusBadge.setText(status);
            statusDetail.setText(detail);
            if (statusDot != null) {
                // Simple dot color change based on status
                if (status.equals("Ready")) statusDot.setBackgroundColor(0xFF666666);
                else if (status.equals("Uploading")) statusDot.setBackgroundColor(0xFFF59E0B);
                else if (status.equals("Building")) statusDot.setBackgroundColor(0xFF3B82F6);
                else if (status.equals("Success")) statusDot.setBackgroundColor(0xFF10B981);
                else if (status.equals("Failed")) statusDot.setBackgroundColor(0xFFEF4444);
            }
        });
    }
    
    private void setProgress(int percent) {
        mainHandler.post(() -> {
            if (percent > 0 && percent < 100) {
                progressBar.setVisibility(View.VISIBLE);
                progressFill.setLayoutParams(new LinearLayout.LayoutParams(
                    (int)(progressBar.getWidth() * percent / 100.0),
                    progressFill.getLayoutParams().height
                ));
            } else {
                progressBar.setVisibility(View.GONE);
            }
        });
    }
    
    private void openLeftDrawer() { drawerLayout.openDrawer(leftDrawer); }
    private void closeLeftDrawer() { drawerLayout.closeDrawer(leftDrawer); }
    private void openRightDrawer() { drawerLayout.openDrawer(rightDrawer); }
    private void closeRightDrawer() { drawerLayout.closeDrawer(rightDrawer); }
    private void toggleConsole() {
        View console = findViewById(R.id.consoleDrawer);
        if (console.getLayoutParams().height == 44) {
            console.getLayoutParams().height = 280;
        } else {
            console.getLayoutParams().height = 44;
        }
        console.requestLayout();
    }
    
    private String utf8ToBase64(String str) {
        return Base64.encodeToString(str.getBytes(), Base64.NO_WRAP);
    }
    
    private asyncTask uploadToGit(String owner, String repo, String path, String content, boolean isBinary) {
        // This will be implemented in the next update
        return null;
    }
    
    private void saveAndTriggerBuild() {
        if (isProcessing) {
            Toast.makeText(this, "Already processing", Toast.LENGTH_SHORT).show();
            return;
        }
        
        syncEditorToMap();
        
        if (ghTokenStr.isEmpty() || ghRepoStr.isEmpty()) {
            addConsoleLog("Missing GitHub token or repo", true);
            Toast.makeText(this, "Set GitHub settings first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String[] parts = ghRepoStr.split("/");
        if (parts.length != 2) {
            addConsoleLog("Invalid repo format", true);
            return;
        }
        
        String owner = parts[0];
        String repo = parts[1];
        currentOwner = owner;
        currentRepo = repo;
        
        isProcessing = true;
        buildAndTriggerBtn.setEnabled(false);
        buildAndTriggerBtn.setText("Uploading...");
        setStatus("Uploading", "");
        setProgress(10);
        
        executorService.execute(() -> {
            boolean success = performUpload(owner, repo, ghTokenStr);
            mainHandler.post(() -> {
                if (success) {
                    triggerBuild(owner, repo, ghTokenStr);
                } else {
                    buildAndTriggerBtn.setEnabled(true);
                    buildAndTriggerBtn.setText("Save & Trigger Build");
                    isProcessing = false;
                    setStatus("Failed", "");
                    setProgress(0);
                }
            });
        });
    }
    
    private boolean performUpload(String owner, String repo, String token) {
        try {
            int total = allFiles.size();
            int current = 0;
            for (Map.Entry<String, String> entry : allFiles.entrySet()) {
                String path = entry.getKey();
                String content = entry.getValue();
                String encoded = utf8ToBase64(content);
                
                String urlStr = "https://api.github.com/repos/" + owner + "/" + repo + "/contents/" + path;
                String sha = null;
                
                try {
                    URL checkUrl = new URL(urlStr + "?ref=main");
                    HttpURLConnection checkConn = (HttpURLConnection) checkUrl.openConnection();
                    checkConn.setRequestProperty("Authorization", "token " + token);
                    if (checkConn.getResponseCode() == 200) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(checkConn.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) sb.append(line);
                        JSONObject obj = new JSONObject(sb.toString());
                        sha = obj.getString("sha");
                    }
                    checkConn.disconnect();
                } catch (Exception e) {}
                
                JSONObject body = new JSONObject();
                body.put("message", "Update " + path);
                body.put("content", encoded);
                body.put("branch", "main");
                if (sha != null) body.put("sha", sha);
                
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Authorization", "token " + token);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                
                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes());
                os.flush();
                os.close();
                
                int responseCode = conn.getResponseCode();
                conn.disconnect();
                
                if (responseCode != 200 && responseCode != 201) {
                    addConsoleLog("Upload failed: " + path, true);
                    return false;
                }
                
                current++;
                final int progress = 10 + (current * 40 / total);
                mainHandler.post(() -> setProgress(progress));
                addConsoleLog("Uploaded: " + path, false);
            }
            
            // Upload icon
            if (appIconBase64 != null && !appIconBase64.isEmpty()) {
                String urlStr = "https://api.github.com/repos/" + owner + "/" + repo + "/contents/build_assets/app_icon.png";
                JSONObject body = new JSONObject();
                body.put("message", "Update icon");
                body.put("content", appIconBase64);
                body.put("branch", "main");
                
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Authorization", "token " + token);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                
                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes());
                os.flush();
                os.close();
                conn.disconnect();
            }
            
            return true;
        } catch (Exception e) {
            addConsoleLog("Upload error: " + e.getMessage(), true);
            return false;
        }
    }
    
    private void triggerBuild(String owner, String repo, String token) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("event_type", "trigger_b64_build");
            
            JSONObject clientPayload = new JSONObject();
            clientPayload.put("package_name", pkgNameStr);
            clientPayload.put("app_name", appNameStr);
            clientPayload.put("icon", appIconBase64);
            clientPayload.put("build_time", System.currentTimeMillis());
            
            JSONObject filesObj = new JSONObject();
            for (Map.Entry<String, String> entry : allFiles.entrySet()) {
                filesObj.put(entry.getKey(), entry.getValue());
            }
            clientPayload.put("files", filesObj);
            payload.put("client_payload", clientPayload);
            
            String urlStr = "https://api.github.com/repos/" + owner + "/" + repo + "/dispatches";
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "token " + token);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            
            OutputStream os = conn.getOutputStream();
            os.write(payload.toString().getBytes());
            os.flush();
            os.close();
            
            int responseCode = conn.getResponseCode();
            conn.disconnect();
            
            if (responseCode == 204) {
                addConsoleLog("Build triggered successfully!", false);
                Toast.makeText(this, "Build started!", Toast.LENGTH_SHORT).show();
                setStatus("Building", "Waiting...");
                setProgress(70);
                startPolling(owner, repo, token);
            } else {
                addConsoleLog("Trigger failed: " + responseCode, true);
                setStatus("Failed", "");
            }
        } catch (Exception e) {
            addConsoleLog("Trigger error: " + e.getMessage(), true);
            setStatus("Failed", "");
        } finally {
            buildAndTriggerBtn.setEnabled(true);
            buildAndTriggerBtn.setText("Save & Trigger Build");
            isProcessing = false;
            setProgress(100);
            mainHandler.postDelayed(() -> setProgress(0), 2000);
        }
    }
    
    private void startPolling(String owner, String repo, String token) {
        if (pollRunnable != null) pollHandler.removeCallbacks(pollRunnable);
        
        pollRunnable = new Runnable() {
            int attempts = 0;
            @Override
            public void run() {
                attempts++;
                try {
                    String urlStr = "https://api.github.com/repos/" + owner + "/" + repo + "/actions/runs?event=repository_dispatch&per_page=1";
                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("Authorization", "token " + token);
                    
                    if (conn.getResponseCode() == 200) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) sb.append(line);
                        JSONObject obj = new JSONObject(sb.toString());
                        
                        if (obj.has("workflow_runs") && obj.getJSONArray("workflow_runs").length() > 0) {
                            JSONObject run = obj.getJSONArray("workflow_runs").getJSONObject(0);
                            String status = run.getString("status");
                            String conclusion = run.optString("conclusion", "");
                            
                            if (status.equals("completed")) {
                                if (conclusion.equals("success")) {
                                    addConsoleLog("Build completed successfully!", false);
                                    setStatus("Success", "");
                                    enableDownloadButtons(owner, repo, token);
                                } else {
                                    addConsoleLog("Build failed", true);
                                    setStatus("Failed", "");
                                }
                                return;
                            } else if (status.equals("in_progress")) {
                                setStatus("Building", "In progress...");
                                setProgress(80);
                            } else if (status.equals("queued")) {
                                setStatus("Building", "Queued...");
                            }
                        }
                    }
                    conn.disconnect();
                } catch (Exception e) {
                    addConsoleLog("Poll error: " + e.getMessage(), true);
                }
                
                if (attempts < 60) {
                    pollHandler.postDelayed(this, 3000);
                } else {
                    addConsoleLog("Build timeout", true);
                    setStatus("Failed", "Timeout");
                }
            }
        };
        pollHandler.post(pollRunnable);
    }
    
    private void enableDownloadButtons(String owner, String repo, String token) {
        mainHandler.post(() -> {
            downloadApkBtn.setEnabled(true);
            downloadAabBtn.setEnabled(true);
            Toast.makeText(this, "APK/AAB ready for download", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void downloadArtifact(String type) {
        if (ghTokenStr.isEmpty() || ghRepoStr.isEmpty()) {
            Toast.makeText(this, "Set GitHub settings first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String[] parts = ghRepoStr.split("/");
        if (parts.length != 2) return;
        
        String owner = parts[0];
        String repo = parts[1];
        
        executorService.execute(() -> {
            try {
                String urlStr = "https://api.github.com/repos/" + owner + "/" + repo + "/releases/tags/latest-release";
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Authorization", "token " + ghTokenStr);
                
                if (conn.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    JSONObject release = new JSONObject(sb.toString());
                    JSONObject asset = null;
                    
                    for (int i = 0; i < release.getJSONArray("assets").length(); i++) {
                        JSONObject a = release.getJSONArray("assets").getJSONObject(i);
                        if (type.equals("apk") && a.getString("name").endsWith(".apk")) {
                            asset = a;
                            break;
                        } else if (type.equals("aab") && a.getString("name").endsWith(".aab")) {
                            asset = a;
                            break;
                        }
                    }
                    
                    if (asset != null) {
                        final String downloadUrl = asset.getString("browser_download_url");
                        mainHandler.post(() -> {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
                            startActivity(intent);
                            Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        mainHandler.post(() -> Toast.makeText(this, "No " + type.toUpperCase() + " found", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    mainHandler.post(() -> Toast.makeText(this, "No release found. Build first.", Toast.LENGTH_SHORT).show());
                }
                conn.disconnect();
            } catch (Exception e) {
                mainHandler.post(() -> Toast.makeText(this, "Download failed", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
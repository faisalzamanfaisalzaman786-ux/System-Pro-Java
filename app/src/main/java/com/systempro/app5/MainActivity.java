package com.faisal.codeeditor;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText etCodeEditor;
    private WebView webPreview;
    private Button btnRun, btnClear, btnCopy, btnShare, btnSave, btnTogglePreview;
    private LinearLayout layoutPreview, layoutEditor;
    private TextView tvStatus, tvLineCount, tvDateTime, tvCharCount;
    private ScrollView scrollEditor;
    private boolean isPreviewVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        setupWebView();
        setupClickListeners();
        setupTextWatcher();
        setDefaultCode();
        updateDateTime();
        
        Toast.makeText(this, "🌟 خوش آمدید! فیصل سسٹم پرو", Toast.LENGTH_SHORT).show();
    }
    
    private void initViews() {
        etCodeEditor = findViewById(R.id.et_code_editor);
        webPreview = findViewById(R.id.web_preview);
        btnRun = findViewById(R.id.btn_run);
        btnClear = findViewById(R.id.btn_clear);
        btnCopy = findViewById(R.id.btn_copy);
        btnShare = findViewById(R.id.btn_share);
        btnSave = findViewById(R.id.btn_save);
        btnTogglePreview = findViewById(R.id.btn_toggle_preview);
        layoutPreview = findViewById(R.id.layout_preview);
        layoutEditor = findViewById(R.id.layout_editor);
        tvStatus = findViewById(R.id.tv_status);
        tvLineCount = findViewById(R.id.tv_line_count);
        tvDateTime = findViewById(R.id.tv_date_time);
        tvCharCount = findViewById(R.id.tv_char_count);
        scrollEditor = findViewById(R.id.scroll_editor);
    }
    
    private void setupWebView() {
        webPreview.setWebViewClient(new WebViewClient());
        webPreview.getSettings().setJavaScriptEnabled(true);
        webPreview.getSettings().setLoadWithOverviewMode(true);
        webPreview.getSettings().setUseWideViewPort(true);
        webPreview.getSettings().setBuiltInZoomControls(true);
        webPreview.getSettings().setDisplayZoomControls(false);
    }
    
    private void setupClickListeners() {
        btnRun.setOnClickListener(v -> runCode());
        btnClear.setOnClickListener(v -> clearCode());
        btnCopy.setOnClickListener(v -> copyCode());
        btnShare.setOnClickListener(v -> shareCode());
        btnSave.setOnClickListener(v -> saveCode());
        btnTogglePreview.setOnClickListener(v -> togglePreview());
    }
    
    private void setupTextWatcher() {
        etCodeEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateStats();
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void updateStats() {
        String code = etCodeEditor.getText().toString();
        int lines = code.split("\n").length;
        int chars = code.length();
        
        tvLineCount.setText("📝 " + lines + " lines");
        tvCharCount.setText("🔤 " + chars + " chars");
        
        if (code.trim().isEmpty()) {
            tvStatus.setText("💡 کوڈ لکھیں یا پیسٹ کریں");
        } else {
            tvStatus.setText("✏️ " + lines + " lines, " + chars + " characters");
        }
    }
    
    private void updateDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMM yyyy | hh:mm a", new Locale("UR"));
        String dateTime = sdf.format(new Date());
        tvDateTime.setText("📅 " + dateTime);
    }
    
    private void runCode() {
        String code = etCodeEditor.getText().toString();
        
        if (code.trim().isEmpty()) {
            Toast.makeText(this, "⚠️ براہ کرم کوڈ لکھیں یا پیسٹ کریں", Toast.LENGTH_SHORT).show();
            return;
        }
        
        tvStatus.setText("🔄 کوڈ چل رہا ہے...");
        
        String processedCode = processCode(code);
        webPreview.loadDataWithBaseURL(null, processedCode, "text/html", "UTF-8", null);
        
        if (!isPreviewVisible) {
            togglePreview();
        }
        
        Toast.makeText(this, "✅ کوڈ رن ہو گیا!", Toast.LENGTH_SHORT).show();
        tvStatus.setText("✅ کوڈ کامیابی سے چل گیا");
    }
    
    private String processCode(String code) {
        // Full HTML document
        if (code.toLowerCase().contains("<!doctype") || code.toLowerCase().contains("<html")) {
            return code;
        }
        
        // CSS code
        if (code.contains("{") && code.contains("}") && 
            (code.contains("color") || code.contains("background") || code.contains("margin"))) {
            return "<!DOCTYPE html>\n<html>\n<head>\n<meta name='viewport' content='width=device-width, initial-scale=1.0'>\n<style>\n" + 
                   code + "\n.demo-box { padding: 20px; margin: 20px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; border-radius: 10px; text-align: center; }\n" +
                   "</style>\n</head>\n<body>\n<div class='demo-box'>\n<h1>🎨 CSS Preview</h1>\n<p>آپ کا CSS اسٹائل یہاں لگ رہا ہے</p>\n<button onclick=\"alert('CSS Demo!')\">Test Button</button>\n</div>\n</body>\n</html>";
        }
        
        // JavaScript code
        if (code.contains("function") || code.contains("alert") || code.contains("console.log")) {
            return "<!DOCTYPE html>\n<html>\n<head>\n<meta name='viewport' content='width=device-width, initial-scale=1.0'>\n<style>\n" +
                   "body { font-family: Arial; padding: 20px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height: 100vh; margin: 0; }\n" +
                   ".container { background: white; border-radius: 20px; padding: 30px; max-width: 600px; margin: auto; }\n" +
                   "button { background: #667eea; color: white; border: none; padding: 10px 20px; border-radius: 10px; margin: 5px; cursor: pointer; }\n" +
                   "</style>\n</head>\n<body>\n<div class='container'>\n<h1>💻 JavaScript Preview</h1>\n<div id='output'>آؤٹ پٹ یہاں دکھے گا</div>\n<button onclick='runScript()'>▶ کوڈ چلائیں</button>\n</div>\n<script>\n" +
                   "function runScript() { try { " + code + " } catch(e) { document.getElementById('output').innerHTML = 'Error: ' + e.message; } }\n" +
                   "</script>\n</body>\n</html>";
        }
        
        // Default - HTML fragment
        return "<!DOCTYPE html>\n<html>\n<head>\n<meta name='viewport' content='width=device-width, initial-scale=1.0'>\n<style>\n" +
               "body { font-family: 'Segoe UI', Arial, sans-serif; padding: 20px; margin: 0; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height: 100vh; }\n" +
               ".card { background: white; border-radius: 20px; padding: 30px; box-shadow: 0 20px 60px rgba(0,0,0,0.3); margin: 20px auto; max-width: 800px; }\n" +
               "h1 { color: #667eea; }\n" +
               "</style>\n</head>\n<body>\n<div class='card'>\n" + code + "\n</div>\n</body>\n</html>";
    }
    
    private void togglePreview() {
        if (isPreviewVisible) {
            layoutPreview.setVisibility(View.GONE);
            layoutEditor.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
            isPreviewVisible = false;
            btnTogglePreview.setText("👁 پریویو");
            Toast.makeText(this, "📝 ایڈیٹر موڈ", Toast.LENGTH_SHORT).show();
        } else {
            layoutPreview.setVisibility(View.VISIBLE);
            layoutEditor.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                0, 0.5f));
            isPreviewVisible = true;
            btnTogglePreview.setText("✏️ ایڈیٹر");
            runCode();
            Toast.makeText(this, "👁 پریویو موڈ", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void clearCode() {
        new AlertDialog.Builder(this)
            .setTitle("🧹 کوڈ صاف کریں")
            .setMessage("کیا آپ واقعی تمام کوڈ کو صاف کرنا چاہتے ہیں؟")
            .setPositiveButton("ہاں", (dialog, which) -> {
                etCodeEditor.setText("");
                Toast.makeText(this, "✅ کوڈ صاف ہو گیا", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("نہیں", null)
            .show();
    }
    
    private void copyCode() {
        String code = etCodeEditor.getText().toString();
        if (code.trim().isEmpty()) {
            Toast.makeText(this, "⚠️ کاپی کرنے کے لیے کوڈ لکھیں", Toast.LENGTH_SHORT).show();
            return;
        }
        
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Code", code);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "📋 کوڈ کاپی ہو گیا!", Toast.LENGTH_SHORT).show();
    }
    
    private void shareCode() {
        String code = etCodeEditor.getText().toString();
        if (code.trim().isEmpty()) {
            Toast.makeText(this, "⚠️ شیئر کرنے کے لیے کوڈ لکھیں", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, code);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Code from Faisal System Pro");
        startActivity(Intent.createChooser(shareIntent, "شیئر کریں"));
    }
    
    private void saveCode() {
        String code = etCodeEditor.getText().toString();
        if (code.trim().isEmpty()) {
            Toast.makeText(this, "⚠️ محفوظ کرنے کے لیے کوڈ لکھیں", Toast.LENGTH_SHORT).show();
            return;
        }
        
        SharedPreferences prefs = getSharedPreferences("CodeEditor", MODE_PRIVATE);
        prefs.edit().putString("saved_code", code).apply();
        Toast.makeText(this, "💾 کوڈ محفوظ ہو گیا!", Toast.LENGTH_SHORT).show();
    }
    
    private void setDefaultCode() {
        SharedPreferences prefs = getSharedPreferences("CodeEditor", MODE_PRIVATE);
        String savedCode = prefs.getString("saved_code", null);
        
        if (savedCode != null) {
            etCodeEditor.setText(savedCode);
        } else {
            String defaultCode = "<!DOCTYPE html>\n" +
                                 "<html>\n" +
                                 "<head>\n" +
                                 "    <style>\n" +
                                 "        body {\n" +
                                 "            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n" +
                                 "            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
                                 "            display: flex;\n" +
                                 "            justify-content: center;\n" +
                                 "            align-items: center;\n" +
                                 "            min-height: 100vh;\n" +
                                 "            margin: 0;\n" +
                                 "            padding: 20px;\n" +
                                 "        }\n" +
                                 "        .card {\n" +
                                 "            background: white;\n" +
                                 "            border-radius: 20px;\n" +
                                 "            padding: 40px;\n" +
                                 "            text-align: center;\n" +
                                 "            box-shadow: 0 20px 60px rgba(0,0,0,0.3);\n" +
                                 "            max-width: 500px;\n" +
                                 "        }\n" +
                                 "        h1 { color: #667eea; margin-bottom: 10px; }\n" +
                                 "        .logo { font-size: 50px; margin-bottom: 20px; }\n" +
                                 "        button {\n" +
                                 "            background: linear-gradient(135deg, #667eea, #764ba2);\n" +
                                 "            color: white;\n" +
                                 "            border: none;\n" +
                                 "            padding: 12px 30px;\n" +
                                 "            border-radius: 25px;\n" +
                                 "            font-size: 16px;\n" +
                                 "            cursor: pointer;\n" +
                                 "            margin-top: 20px;\n" +
                                 "        }\n" +
                                 "        button:hover { transform: scale(1.05); }\n" +
                                 "    </style>\n" +
                                 "</head>\n" +
                                 "<body>\n" +
                                 "    <div class='card'>\n" +
                                 "        <div class='logo'>🚀</div>\n" +
                                 "        <h1>Code Editor Pro</h1>\n" +
                                 "        <p><b>فیصل سسٹم پرو</b></p>\n" +
                                 "        <p>اپنا HTML, CSS, اور JavaScript کوڈ یہاں لکھیں اور RUN دبائیں۔</p>\n" +
                                 "        <button onclick=\"alert('🎉 Welcome to Code Editor Pro!')\">✨ Get Started</button>\n" +
                                 "    </div>\n" +
                                 "</body>\n" +
                                 "</html>";
            etCodeEditor.setText(defaultCode);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateDateTime();
    }
}
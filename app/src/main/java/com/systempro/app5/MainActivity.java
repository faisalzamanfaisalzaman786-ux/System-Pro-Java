package com.faisal.codeeditor;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText etCodeEditor;
    private WebView webPreview;
    private Button btnRun, btnClear, btnCopy, btnShare, btnSave;
    private FloatingActionButton fabRun, fabPreview;
    private LinearLayout layoutPreview, layoutEditor;
    private TextView tvStatus, tvLineCount, tvDateTime, tvCharCount;
    private ScrollView scrollEditor;
    private CardView cardHeader;
    private Animation fadeIn, slideUp;
    
    private boolean isPreviewVisible = false;
    private String currentCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        initAnimations();
        setupWebView();
        setupClickListeners();
        setupTextWatcher();
        setDefaultCode();
        updateDateTime();
        
        // Welcome message
        Toast.makeText(this, "🌟 خوش آمدید! فیصل سسٹم پرو", Toast.LENGTH_LONG).show();
    }
    
    private void initViews() {
        etCodeEditor = findViewById(R.id.et_code_editor);
        webPreview = findViewById(R.id.web_preview);
        btnRun = findViewById(R.id.btn_run);
        btnClear = findViewById(R.id.btn_clear);
        btnCopy = findViewById(R.id.btn_copy);
        btnShare = findViewById(R.id.btn_share);
        btnSave = findViewById(R.id.btn_save);
        fabRun = findViewById(R.id.fab_run);
        fabPreview = findViewById(R.id.fab_preview);
        layoutPreview = findViewById(R.id.layout_preview);
        layoutEditor = findViewById(R.id.layout_editor);
        tvStatus = findViewById(R.id.tv_status);
        tvLineCount = findViewById(R.id.tv_line_count);
        tvDateTime = findViewById(R.id.tv_date_time);
        tvCharCount = findViewById(R.id.tv_char_count);
        scrollEditor = findViewById(R.id.scroll_editor);
        cardHeader = findViewById(R.id.card_header);
        
        // Set custom font
        Typeface monospace = Typeface.createFromAsset(getAssets(), "fonts/jetbrains_mono.ttf");
        if (monospace != null) {
            etCodeEditor.setTypeface(monospace);
        } else {
            etCodeEditor.setTypeface(Typeface.MONOSPACE);
        }
    }
    
    private void initAnimations() {
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
    }
    
    private void setupWebView() {
        webPreview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                tvStatus.setText("✅ کوڈ کامیابی سے چل گیا");
                tvStatus.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.success));
            }
        });
        webPreview.getSettings().setJavaScriptEnabled(true);
        webPreview.getSettings().setLoadWithOverviewMode(true);
        webPreview.getSettings().setUseWideViewPort(true);
        webPreview.getSettings().setBuiltInZoomControls(true);
        webPreview.getSettings().setDisplayZoomControls(false);
        webPreview.getSettings().setSupportZoom(true);
    }
    
    private void setupClickListeners() {
        btnRun.setOnClickListener(v -> runCode());
        fabRun.setOnClickListener(v -> runCode());
        
        btnClear.setOnClickListener(v -> clearCode());
        btnCopy.setOnClickListener(v -> copyCode());
        btnShare.setOnClickListener(v -> shareCode());
        btnSave.setOnClickListener(v -> saveCode());
        
        fabPreview.setOnClickListener(v -> togglePreview());
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
            public void afterTextChanged(Editable s) {
                currentCode = s.toString();
            }
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
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        } else {
            tvStatus.setText("✏️ ایڈیٹنگ موڈ");
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.primary));
        }
    }
    
    private void updateDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMM yyyy | hh:mm a", new Locale("ur"));
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
        tvStatus.setTextColor(ContextCompat.getColor(this, R.color.warning));
        
        String processedCode = processCode(code);
        webPreview.loadDataWithBaseURL(null, processedCode, "text/html", "UTF-8", null);
        
        if (!isPreviewVisible) {
            togglePreview();
        }
        
        Toast.makeText(this, "✅ کوڈ رن ہو گیا!", Toast.LENGTH_SHORT).show();
    }
    
    private String processCode(String code) {
        // Detect HTML
        if (code.toLowerCase().contains("<!doctype") || 
            code.toLowerCase().contains("<html") ||
            (code.toLowerCase().contains("<body") && code.toLowerCase().contains("</body>"))) {
            return code;
        }
        
        // Detect CSS
        if (code.contains("{") && code.contains("}") && 
            (code.contains("color") || code.contains("background") || 
             code.contains("margin") || code.contains("padding"))) {
            return "<!DOCTYPE html>\n<html>\n<head>\n" +
                   "<meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                   "<style>\n" + code + "\n" +
                   ".demo-box { padding: 20px; margin: 20px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; border-radius: 10px; text-align: center; }\n" +
                   "</style>\n</head>\n<body>\n" +
                   "<div class='demo-box'>\n" +
                   "<h1>🎨 CSS Preview</h1>\n" +
                   "<p>آپ کا CSS اسٹائل یہاں لگ رہا ہے</p>\n" +
                   "<button onclick=\"alert('CSS Demo!')\">Test Button</button>\n" +
                   "</div>\n</body>\n</html>";
        }
        
        // Detect JavaScript
        if (code.contains("function") || code.contains("alert") || 
            code.contains("console.log") || code.contains("document.")) {
            return "<!DOCTYPE html>\n<html>\n<head>\n" +
                   "<meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                   "<style>\n" +
                   "body { font-family: Arial; padding: 20px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height: 100vh; margin: 0; }\n" +
                   ".container { background: white; border-radius: 20px; padding: 30px; box-shadow: 0 10px 40px rgba(0,0,0,0.2); }\n" +
                   "button { background: #667eea; color: white; border: none; padding: 10px 20px; border-radius: 10px; margin: 5px; cursor: pointer; }\n" +
                   "</style>\n</head>\n<body>\n" +
                   "<div class='container'>\n" +
                   "<h1>💻 JavaScript Preview</h1>\n" +
                   "<div id='output'>آؤٹ پٹ یہاں دکھے گا</div>\n" +
                   "<button onclick='runUserScript()'>▶ کوڈ چلائیں</button>\n" +
                   "</div>\n" +
                   "<script>\n" +
                   "function runUserScript() {\n" +
                   "    try {\n" +
                   "        const userCode = `" + code.replace("`", "\\`").replace("$", "\\$") + "`;\n" +
                   "        const output = eval(userCode);\n" +
                   "        document.getElementById('output').innerHTML = '<strong>آؤٹ پٹ:</strong><br><pre>' + JSON.stringify(output, null, 2) + '</pre>';\n" +
                   "    } catch(e) {\n" +
                   "        document.getElementById('output').innerHTML = '<strong>خرابی:</strong><br>' + e.message;\n" +
                   "    }\n" +
                   "}\n" +
                   "</script>\n</body>\n</html>";
        }
        
        // Default HTML wrapper
        return "<!DOCTYPE html>\n<html>\n<head>\n" +
               "<meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
               "<style>\n" +
               "body { font-family: 'Segoe UI', Arial, sans-serif; padding: 20px; margin: 0; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height: 100vh; }\n" +
               ".card { background: white; border-radius: 20px; padding: 30px; box-shadow: 0 20px 60px rgba(0,0,0,0.3); margin: 20px auto; max-width: 800px; }\n" +
               "h1 { color: #667eea; margin-bottom: 20px; }\n" +
               ".code-block { background: #f5f5f5; padding: 15px; border-radius: 10px; font-family: monospace; margin: 10px 0; }\n" +
               "</style>\n</head>\n<body>\n" +
               "<div class='card'>\n" + code + "\n" +
               "</div>\n</body>\n</html>";
    }
    
    private void togglePreview() {
        if (isPreviewVisible) {
            layoutPreview.setVisibility(View.GONE);
            layoutEditor.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
            isPreviewVisible = false;
            fabPreview.setImageResource(R.drawable.ic_preview);
            Toast.makeText(this, "📝 ایڈیٹر موڈ", Toast.LENGTH_SHORT).show();
        } else {
            layoutPreview.setVisibility(View.VISIBLE);
            layoutEditor.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                0, 0.5f));
            isPreviewVisible = true;
            fabPreview.setImageResource(R.drawable.ic_edit);
            runCode();
            Toast.makeText(this, "👁 پریویو موڈ", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void clearCode() {
        new AlertDialog.Builder(this)
            .setTitle("🧹 کوڈ صاف کریں")
            .setMessage("کیا آپ واقعی تمام کوڈ کو صاف کرنا چاہتے ہیں؟")
            .setPositiveButton("ہاں، صاف کریں", (dialog, which) -> {
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
        
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Code", code);
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
        
        // Save to shared preferences
        SharedPreferences prefs = getSharedPreferences("CodeEditor", MODE_PRIVATE);
        prefs.edit().putString("saved_code", code).apply();
        Toast.makeText(this, "💾 کوڈ محفوظ ہو گیا!", Toast.LENGTH_SHORT).show();
    }
    
    private void setDefaultCode() {
        String defaultCode = "<!DOCTYPE html>\n" +
                             "<html>\n" +
                             "<head>\n" +
                             "    <style>\n" +
                             "        * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
                             "        body {\n" +
                             "            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n" +
                             "            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
                             "            display: flex;\n" +
                             "            justify-content: center;\n" +
                             "            align-items: center;\n" +
                             "            min-height: 100vh;\n" +
                             "            padding: 20px;\n" +
                             "        }\n" +
                             "        .welcome-card {\n" +
                             "            background: rgba(255, 255, 255, 0.95);\n" +
                             "            border-radius: 30px;\n" +
                             "            padding: 40px;\n" +
                             "            text-align: center;\n" +
                             "            box-shadow: 0 20px 60px rgba(0,0,0,0.3);\n" +
                             "            animation: fadeInUp 0.6s ease-out;\n" +
                             "            max-width: 500px;\n" +
                             "        }\n" +
                             "        @keyframes fadeInUp {\n" +
                             "            from {\n" +
                             "                opacity: 0;\n" +
                             "                transform: translateY(30px);\n" +
                             "            }\n" +
                             "            to {\n" +
                             "                opacity: 1;\n" +
                             "                transform: translateY(0);\n" +
                             "            }\n" +
                             "        }\n" +
                             "        .logo {\n" +
                             "            width: 80px;\n" +
                             "            height: 80px;\n" +
                             "            background: linear-gradient(135deg, #667eea, #764ba2);\n" +
                             "            border-radius: 50%;\n" +
                             "            display: flex;\n" +
                             "            align-items: center;\n" +
                             "            justify-content: center;\n" +
                             "            margin: 0 auto 20px;\n" +
                             "            font-size: 40px;\n" +
                             "        }\n" +
                             "        h1 {\n" +
                             "            color: #333;\n" +
                             "            margin-bottom: 10px;\n" +
                             "            font-size: 28px;\n" +
                             "        }\n" +
                             "        .subtitle {\n" +
                             "            color: #667eea;\n" +
                             "            font-size: 16px;\n" +
                             "            margin-bottom: 20px;\n" +
                             "            letter-spacing: 2px;\n" +
                             "        }\n" +
                             "        p {\n" +
                             "            color: #666;\n" +
                             "            line-height: 1.6;\n" +
                             "            margin-bottom: 20px;\n" +
                             "        }\n" +
                             "        .features {\n" +
                             "            text-align: left;\n" +
                             "            margin: 20px 0;\n" +
                             "            padding: 0 20px;\n" +
                             "        }\n" +
                             "        .features li {\n" +
                             "            margin: 10px 0;\n" +
                             "            color: #555;\n" +
                             "        }\n" +
                             "        button {\n" +
                             "            background: linear-gradient(135deg, #667eea, #764ba2);\n" +
                             "            color: white;\n" +
                             "            border: none;\n" +
                             "            padding: 12px 30px;\n" +
                             "            border-radius: 25px;\n" +
                             "            font-size: 16px;\n" +
                             "            cursor: pointer;\n" +
                             "            transition: transform 0.3s, box-shadow 0.3s;\n" +
                             "        }\n" +
                             "        button:hover {\n" +
                             "            transform: translateY(-2px);\n" +
                             "            box-shadow: 0 10px 20px rgba(0,0,0,0.2);\n" +
                             "        }\n" +
                             "    </style>\n" +
                             "</head>\n" +
                             "<body>\n" +
                             "    <div class='welcome-card'>\n" +
                             "        <div class='logo'>🚀</div>\n" +
                             "        <h1>Code Editor Pro</h1>\n" +
                             "        <div class='subtitle'>فیصل سسٹم پرو</div>\n" +
                             "        <p>خوش آمدید! اپنا HTML, CSS, اور JavaScript کوڈ یہاں لکھیں اور RUN بٹن دبائیں۔</p>\n" +
                             "        <div class='features'>\n" +
                             "            <li>✅ Live HTML Preview</li>\n" +
                             "            <li>✅ CSS Styling Support</li>\n" +
                             "            <li>✅ JavaScript Execution</li>\n" +
                             "            <li>✅ Code Copy & Share</li>\n" +
                             "        </div>\n" +
                             "        <button onclick=\"alert('🎉 Code Editor Pro is ready!')\">✨ Get Started</button>\n" +
                             "    </div>\n" +
                             "</body>\n" +
                             "</html>";
        
        // Load saved code if exists
        SharedPreferences prefs = getSharedPreferences("CodeEditor", MODE_PRIVATE);
        String savedCode = prefs.getString("saved_code", defaultCode);
        etCodeEditor.setText(savedCode);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateDateTime();
    }
}
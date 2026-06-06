package com.systempro.app5;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

// IMPORTANT: Missing imports added below
import android.os.Environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    
    private TextView tvPermissionStatus;
    private Button btnCheckPermissions;
    private Button btnRequestPermissions;
    private Button btnOpenSettings;
    private ScrollView scrollView;
    
    // Base permissions
    private String[] allPermissions = {
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.WRITE_CALL_LOG,
        Manifest.permission.SEND_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.READ_CALENDAR,
        Manifest.permission.WRITE_CALENDAR,
        Manifest.permission.BODY_SENSORS,
        Manifest.permission.VIBRATE,
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.SET_ALARM,
        Manifest.permission.GET_ACCOUNTS,
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_WIFI_STATE,
    };
    
    // Android 13+ (API 33+)
    private String[] permissionsApi33 = {
        Manifest.permission.POST_NOTIFICATIONS
    };
    
    // Android 12+ (API 31+)
    private String[] permissionsApi31 = {
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_ADVERTISE
    };
    
    // Android 11+ (API 30+)
    private String[] permissionsApi30 = {
        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        Manifest.permission.ACTIVITY_RECOGNITION
    };
    
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int MANAGE_STORAGE_REQUEST_CODE = 101;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        setupClickListeners();
        checkAllPermissions();
    }
    
    private void initViews() {
        tvPermissionStatus = findViewById(R.id.tv_permission_status);
        btnCheckPermissions = findViewById(R.id.btn_check_permissions);
        btnRequestPermissions = findViewById(R.id.btn_request_permissions);
        btnOpenSettings = findViewById(R.id.btn_open_settings);
        scrollView = findViewById(R.id.scroll_view);
    }
    
    private void setupClickListeners() {
        btnCheckPermissions.setOnClickListener(v -> checkAllPermissions());
        btnRequestPermissions.setOnClickListener(v -> requestAllPermissions());
        btnOpenSettings.setOnClickListener(v -> openAppSettings());
    }
    
    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }
    
    private String[] getCompletePermissionsList() {
        List<String> permissions = new ArrayList<>();
        
        for (String perm : allPermissions) {
            permissions.add(perm);
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            for (String perm : permissionsApi33) {
                if (!permissions.contains(perm)) permissions.add(perm);
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            for (String perm : permissionsApi31) {
                if (!permissions.contains(perm)) permissions.add(perm);
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            for (String perm : permissionsApi30) {
                if (!permissions.contains(perm)) permissions.add(perm);
            }
        }
        
        return permissions.toArray(new String[0]);
    }
    
    private void checkAllPermissions() {
        String[] permissionsToCheck = getCompletePermissionsList();
        Map<String, Boolean> permissionStatus = new HashMap<>();
        
        int grantedCount = 0;
        int deniedCount = 0;
        
        for (String permission : permissionsToCheck) {
            boolean isGranted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
            permissionStatus.put(permission, isGranted);
            if (isGranted) {
                grantedCount++;
            } else {
                deniedCount++;
            }
        }
        
        // Check MANAGE_EXTERNAL_STORAGE for Android 11+
        boolean hasManageStorage = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            hasManageStorage = Environment.isExternalStorageManager();
            if (hasManageStorage) {
                grantedCount++;
            } else {
                deniedCount++;
            }
        }
        
        // Build display
        StringBuilder status = new StringBuilder();
        status.append("╔════════════════════════════════════════╗\n");
        status.append("║     PERMISSION STATUS REPORT          ║\n");
        status.append("╠════════════════════════════════════════╣\n");
        status.append("║  ✅ Granted: ").append(String.format("%-3d", grantedCount)).append("                    ║\n");
        status.append("║  ❌ Denied:  ").append(String.format("%-3d", deniedCount)).append("                    ║\n");
        status.append("╠════════════════════════════════════════╣\n");
        
        status.append("\n📁 STORAGE:\n");
        addPermissionStatus(status, permissionStatus, Manifest.permission.READ_EXTERNAL_STORAGE, "  Read External Storage");
        addPermissionStatus(status, permissionStatus, Manifest.permission.WRITE_EXTERNAL_STORAGE, "  Write External Storage");
        
        status.append("\n📷 CAMERA:\n");
        addPermissionStatus(status, permissionStatus, Manifest.permission.CAMERA, "  Camera");
        
        status.append("\n🎤 MICROPHONE:\n");
        addPermissionStatus(status, permissionStatus, Manifest.permission.RECORD_AUDIO, "  Record Audio");
        
        status.append("\n📍 LOCATION:\n");
        addPermissionStatus(status, permissionStatus, Manifest.permission.ACCESS_FINE_LOCATION, "  GPS Location");
        addPermissionStatus(status, permissionStatus, Manifest.permission.ACCESS_COARSE_LOCATION, "  Network Location");
        
        status.append("\n📞 PHONE:\n");
        addPermissionStatus(status, permissionStatus, Manifest.permission.READ_PHONE_STATE, "  Phone State");
        addPermissionStatus(status, permissionStatus, Manifest.permission.CALL_PHONE, "  Make Calls");
        
        status.append("\n💬 SMS:\n");
        addPermissionStatus(status, permissionStatus, Manifest.permission.SEND_SMS, "  Send SMS");
        addPermissionStatus(status, permissionStatus, Manifest.permission.READ_SMS, "  Read SMS");
        
        status.append("\n👥 CONTACTS:\n");
        addPermissionStatus(status, permissionStatus, Manifest.permission.READ_CONTACTS, "  Read Contacts");
        addPermissionStatus(status, permissionStatus, Manifest.permission.WRITE_CONTACTS, "  Write Contacts");
        
        status.append("\n📅 CALENDAR:\n");
        addPermissionStatus(status, permissionStatus, Manifest.permission.READ_CALENDAR, "  Read Calendar");
        addPermissionStatus(status, permissionStatus, Manifest.permission.WRITE_CALENDAR, "  Write Calendar");
        
        status.append("\n⚡ OTHER:\n");
        addPermissionStatus(status, permissionStatus, Manifest.permission.VIBRATE, "  Vibrate");
        addPermissionStatus(status, permissionStatus, Manifest.permission.INTERNET, "  Internet");
        
        status.append("\n╚════════════════════════════════════════╝\n");
        
        tvPermissionStatus.setText(status.toString());
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_UP));
        
        Toast.makeText(this, "✅ Granted: " + grantedCount + " | ❌ Denied: " + deniedCount, Toast.LENGTH_LONG).show();
    }
    
    private void addPermissionStatus(StringBuilder sb, Map<String, Boolean> status, String permission, String displayName) {
        Boolean isGranted = status.get(permission);
        if (isGranted != null) {
            sb.append(isGranted ? "  ✅ " : "  ❌ ").append(displayName).append("\n");
        }
    }
    
    private void requestAllPermissions() {
        String[] allPerms = getCompletePermissionsList();
        List<String> permissionsToRequest = new ArrayList<>();
        
        for (String permission : allPerms) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        
        if (permissionsToRequest.isEmpty()) {
            Toast.makeText(this, "✅ تمام اجازتیں پہلے سے موجود ہیں!", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, 
                permissionsToRequest.toArray(new String[0]), 
                PERMISSION_REQUEST_CODE);
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            int granted = 0, denied = 0;
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_GRANTED) granted++;
                else denied++;
            }
            
            checkAllPermissions();
            Toast.makeText(this, "✅ " + granted + " granted | ❌ " + denied + " denied", Toast.LENGTH_LONG).show();
        }
    }
}
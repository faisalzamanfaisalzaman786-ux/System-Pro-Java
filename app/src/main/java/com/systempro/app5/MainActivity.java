package com.example.testapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    
    private TextView tvPermissionStatus;
    private Button btnCheckPermissions;
    private Button btnRequestPermissions;
    private ScrollView scrollView;
    
    // تمام پرمیشنز کی مکمل فہرست
    private String[] allPermissions = {
        // Storage
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        
        // Camera
        Manifest.permission.CAMERA,
        
        // Microphone
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        
        // Location
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        
        // Contacts
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS,
        
        // Phone
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.WRITE_CALL_LOG,
        
        // SMS
        Manifest.permission.SEND_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        
        // Calendar
        Manifest.permission.READ_CALENDAR,
        Manifest.permission.WRITE_CALENDAR,
        
        // Sensors
        Manifest.permission.BODY_SENSORS,
        
        // Notifications (Android 13+)
        Manifest.permission.POST_NOTIFICATIONS,
        
        // Network
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_WIFI_STATE,
        
        // Other
        Manifest.permission.VIBRATE,
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.SET_ALARM,
        Manifest.permission.GET_ACCOUNTS
    };
    
    // Android 13+ کے لیے اضافی پرمیشنز
    private String[] permissionsApi33 = {
        Manifest.permission.POST_NOTIFICATIONS
    };
    
    // Android 12+ کے لیے اضافی پرمیشنز
    private String[] permissionsApi31 = {
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_ADVERTISE
    };
    
    // Android 11+ کے لیے اضافی پرمیشنز
    private String[] permissionsApi30 = {
        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        Manifest.permission.ACTIVITY_RECOGNITION
    };
    
    private static final int PERMISSION_REQUEST_CODE = 100;
    
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
        scrollView = findViewById(R.id.scroll_view);
    }
    
    private void setupClickListeners() {
        btnCheckPermissions.setOnClickListener(v -> checkAllPermissions());
        btnRequestPermissions.setOnClickListener(v -> requestAllPermissions());
    }
    
    private String[] getCompletePermissionsList() {
        List<String> permissions = new ArrayList<>();
        
        // Add all base permissions
        for (String perm : allPermissions) {
            permissions.add(perm);
        }
        
        // Add Android version specific permissions
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
        
        // Build status display
        StringBuilder status = new StringBuilder();
        status.append("╔════════════════════════════════════════╗\n");
        status.append("║       PERMISSION STATUS REPORT        ║\n");
        status.append("╠════════════════════════════════════════╣\n");
        status.append("║  ✅ Granted: ").append(String.format("%-3d", grantedCount)).append("                    ║\n");
        status.append("║  ❌ Denied:  ").append(String.format("%-3d", deniedCount)).append("                    ║\n");
        status.append("╠════════════════════════════════════════╣\n");
        status.append("║  DETAILED PERMISSIONS:                 ║\n");
        status.append("╠════════════════════════════════════════╣\n");
        
        // Group permissions by category
        status.append("\n📁 STORAGE:\n");
        addPermissionStatus(status, permissionStatus, Manifest.permission.READ_EXTERNAL_STORAGE, "  • Read External Storage");
        addPermissionStatus(status, permissionStatus, Manifest.permission.WRITE_EXTERNAL_STORAGE, "  • Write External Storage");
        
        status.append("\n📷 CAMERA:\n");
        addPermissionStatus(status, permissionStatus, Manifest.permission.CAMERA, "  • Camera");
        
        status.append("\n🎤 MICROPHONE:\n");
        addPermissionStatus(status, permissionStatus, Manifest.permission.RECORD_AUDIO, "  • Record Audio");
        
        status.append("\n📍 LOCATION:\n");
        addPermissionStatus(status, permissionStatus, Manifest.permission.ACCESS_FINE_LOCATION, "  • GPS Location");
        addPermissionStatus(status, permissionStatus, Manifest.permission.ACCESS_COARSE_LOCATION, "  • Network Location");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            addPermissionStatus(status, permissionStatus, Manifest.permission.ACCESS_BACKGROUND_LOCATION, "  • Background Location");
        }
        
        status.append("\n📞 PHONE:\n");
        addPermissionStatus(status, permissionStatus, Manifest.permission.READ_PHONE_STATE, "  • Phone State");
        addPermissionStatus(status, permissionStatus, Manifest.permission.CALL_PHONE, "  • Make Calls");
        addPermissionStatus(status, permissionStatus, Manifest.permission.READ_CALL_LOG, "  • Read Call Log");
        
        status.append("\n💬 SMS:\n");
        addPermissionStatus(status, permissionStatus, Manifest.permission.SEND_SMS, "  • Send SMS");
        addPermissionStatus(status, permissionStatus, Manifest.permission.READ_SMS, "  • Read SMS");
        addPermissionStatus(status, permissionStatus, Manifest.permission.RECEIVE_SMS, "  • Receive SMS");
        
        status.append("\n👥 CONTACTS:\n");
        addPermissionStatus(status, permissionStatus, Manifest.permission.READ_CONTACTS, "  • Read Contacts");
        addPermissionStatus(status, permissionStatus, Manifest.permission.WRITE_CONTACTS, "  • Write Contacts");
        
        status.append("\n📅 CALENDAR:\n");
        addPermissionStatus(status, permissionStatus, Manifest.permission.READ_CALENDAR, "  • Read Calendar");
        addPermissionStatus(status, permissionStatus, Manifest.permission.WRITE_CALENDAR, "  • Write Calendar");
        
        status.append("\n🔔 NOTIFICATIONS:\n");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            addPermissionStatus(status, permissionStatus, Manifest.permission.POST_NOTIFICATIONS, "  • Post Notifications");
        } else {
            status.append("  • Notifications (Auto-granted on older Android)\n");
        }
        
        status.append("\n🔵 BLUETOOTH:\n");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            addPermissionStatus(status, permissionStatus, Manifest.permission.BLUETOOTH_CONNECT, "  • Bluetooth Connect");
            addPermissionStatus(status, permissionStatus, Manifest.permission.BLUETOOTH_SCAN, "  • Bluetooth Scan");
        } else {
            status.append("  • Bluetooth (Legacy permissions)\n");
        }
        
        status.append("\n⚡ OTHER PERMISSIONS:\n");
        addPermissionStatus(status, permissionStatus, Manifest.permission.VIBRATE, "  • Vibrate");
        addPermissionStatus(status, permissionStatus, Manifest.permission.WAKE_LOCK, "  • Wake Lock");
        addPermissionStatus(status, permissionStatus, Manifest.permission.SET_ALARM, "  • Set Alarm");
        addPermissionStatus(status, permissionStatus, Manifest.permission.GET_ACCOUNTS, "  • Get Accounts");
        addPermissionStatus(status, permissionStatus, Manifest.permission.INTERNET, "  • Internet");
        
        status.append("\n╚════════════════════════════════════════╝\n");
        
        tvPermissionStatus.setText(status.toString());
        
        // Scroll to top
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_UP));
        
        Toast.makeText(this, 
            "✅ Granted: " + grantedCount + " | ❌ Denied: " + deniedCount, 
            Toast.LENGTH_SHORT).show();
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
            int granted = 0;
            int denied = 0;
            
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_GRANTED) {
                    granted++;
                } else {
                    denied++;
                }
            }
            
            checkAllPermissions();
            
            String message = "✅ " + granted + " اجازتیں دی گئیں | ❌ " + denied + " مسترد";
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            
            if (denied > 0) {
                Toast.makeText(this, "⚠️ کچھ اجازتیں مسترد ہیں۔ ایپ کی کارکردگی متاثر ہو سکتی ہے۔", Toast.LENGTH_LONG).show();
            }
        }
    }
}
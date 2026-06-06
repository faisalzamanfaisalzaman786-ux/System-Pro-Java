package com.systempro.app5;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    
    private TextView tvPermissionStatus;
    private Button btnCheckPermissions;
    private Button btnRequestPermissions;
    
    // سب سے اہم پرمیشنز
    private String[] permissions = {
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.INTERNET
    };
    
    private static final int PERMISSION_REQUEST_CODE = 100;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        tvPermissionStatus = findViewById(R.id.tv_permission_status);
        btnCheckPermissions = findViewById(R.id.btn_check_permissions);
        btnRequestPermissions = findViewById(R.id.btn_request_permissions);
        
        btnCheckPermissions.setOnClickListener(v -> checkAllPermissions());
        btnRequestPermissions.setOnClickListener(v -> requestAllPermissions());
        
        // ایپ شروع ہوتے ہی پرمیشنز چیک کریں
        checkAllPermissions();
    }
    
    private void checkAllPermissions() {
        List<String> grantedList = new ArrayList<>();
        List<String> deniedList = new ArrayList<>();
        
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                grantedList.add(getSimpleName(permission));
            } else {
                deniedList.add(getSimpleName(permission));
            }
        }
        
        StringBuilder status = new StringBuilder();
        status.append("═══════════════════════\n");
        status.append("    PERMISSION STATUS    \n");
        status.append("═══════════════════════\n\n");
        status.append("✅ Granted: ").append(grantedList.size()).append("\n");
        status.append("❌ Denied: ").append(deniedList.size()).append("\n\n");
        
        if (deniedList.size() > 0) {
            status.append("❌ Denied Permissions:\n");
            for (String permission : deniedList) {
                status.append("   • ").append(permission).append("\n");
            }
        } else {
            status.append("🎉 ALL PERMISSIONS GRANTED!\n");
            status.append("🎉 آپ کی تمام اجازتیں موجود ہیں!");
        }
        
        status.append("\n═══════════════════════");
        tvPermissionStatus.setText(status.toString());
    }
    
    private void requestAllPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();
        
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        
        if (permissionsToRequest.isEmpty()) {
            Toast.makeText(this, "✅ تمام اجازتیں پہلے سے موجود ہیں!", Toast.LENGTH_SHORT).show();
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
            
            if (denied == 0) {
                Toast.makeText(this, "✅ تمام اجازتیں کامیابی سے مل گئیں!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "⚠️ " + denied + " اجازتیں مسترد کر دی گئیں", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private String getSimpleName(String permission) {
        switch (permission) {
            case Manifest.permission.CAMERA: return "Camera";
            case Manifest.permission.READ_EXTERNAL_STORAGE: return "Storage Read";
            case Manifest.permission.WRITE_EXTERNAL_STORAGE: return "Storage Write";
            case Manifest.permission.RECORD_AUDIO: return "Microphone";
            case Manifest.permission.ACCESS_FINE_LOCATION: return "GPS Location";
            case Manifest.permission.ACCESS_COARSE_LOCATION: return "Network Location";
            case Manifest.permission.READ_CONTACTS: return "Contacts";
            case Manifest.permission.INTERNET: return "Internet";
            default: return "Other";
        }
    }
}
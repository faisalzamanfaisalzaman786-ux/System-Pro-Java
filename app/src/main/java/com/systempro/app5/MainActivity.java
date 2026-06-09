package com.camera.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private ImageView imageViewPreview;
    private Button buttonOpenCamera;
    
    private String[] permissions = {
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        checkPermissions();
    }
    
    private void initViews() {
        imageViewPreview = findViewById(R.id.imageViewPreview);
        buttonOpenCamera = findViewById(R.id.buttonOpenCamera);
        
        buttonOpenCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasPermissions()) {
                    openCamera();
                } else {
                    checkPermissions();
                }
            }
        });
    }
    
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions()) {
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
            } else {
                initApp();
            }
        } else {
            initApp();
        }
    }
    
    private boolean hasPermissions() {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initApp();
            } else {
                Toast.makeText(this, getString(R.string.permission_required), Toast.LENGTH_LONG).show();
                buttonOpenCamera.setEnabled(false);
            }
        }
    }
    
    private void initApp() {
        Toast.makeText(this, getString(R.string.permission_granted), Toast.LENGTH_SHORT).show();
    }
    
    private void openCamera() {
        Toast.makeText(this, getString(R.string.opening_camera), Toast.LENGTH_SHORT).show();
        // Here you can integrate CameraX or Intent.ACTION_IMAGE_CAPTURE
    }
}
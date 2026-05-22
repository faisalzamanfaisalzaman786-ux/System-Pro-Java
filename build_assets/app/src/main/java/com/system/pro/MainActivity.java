package com.wrong.package.name; // <--- غلطی 1

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.system.pro.R;

public class MainActivity extends AppCompatActivity {

    private CameraManager cameraManager
    private String cameraId
    private Button torchButton;
    private boolean isTorchOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        torchButton = findViewById(R.id.torch_button);
        torchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTorchOn) {
                    turnOffTorch();
                } else {
                    turnOnTorch();
                }
            }
        })

        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
        }
    }

    private void turnOnTorch() {
        try {
            cameraId = getCameraId();
            cameraManager.setTorchMode(cameraId, true);
            isTorchOn = true;
            torchButton.setBackgroundResource(R.drawable.button_background_pressed);
            Toast.makeText(this, "Torch is on", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error turning on torch", Toast.LENGTH_SHORT).show();
        }
    }

    private void turnOffTorch() {
        try {
            cameraId = getCameraId();
            cameraManager.setTorchMode(cameraId, false);
            isTorchOn = false;
            torchButton.setBackgroundResource(R.drawable.button_background);
            Toast.makeText(this, "Torch is off", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error turning off torch", Toast.LENGTH_SHORT).show();
        }
    }

    private String getCameraId() {
        try {
            for (String id : cameraManager.getCameraIdList()) {
                // یہاں ایک امپورٹ غلط ہے (CameraCharacteristics)
                if (cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_UNIT) != null) {
                    return id;
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error getting camera id", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isTorchOn) {
            turnOffTorch();
        }
    }
}
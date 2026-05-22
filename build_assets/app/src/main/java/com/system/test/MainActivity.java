package com.system.test;
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
import com.system.test.R;

public class MainActivity extends AppCompatActivity {
    private CameraManager cameraManager;
    private Button torchButton;
    private boolean isTorchOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        });

        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
        }
    }

    private void turnOnTorch() {
        try {
            String cameraId = null;
            if (cameraManager != null) {
                cameraId = cameraManager.getCameraIdList()[0];
            }
            if (cameraId != null) {
                cameraManager.setTorchMode(cameraId, true);
                isTorchOn = true;
                Toast.makeText(MainActivity.this, "Torch is on", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Error turning on torch", Toast.LENGTH_SHORT).show();
        }
    }

    private void turnOffTorch() {
        try {
            String cameraId = null;
            if (cameraManager != null) {
                cameraId = cameraManager.getCameraIdList()[0];
            }
            if (cameraId != null) {
                cameraManager.setTorchMode(cameraId, false);
                isTorchOn = false;
                Toast.makeText(MainActivity.this, "Torch is off", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Error turning off torch", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraManager != null) {
            try {
                String cameraId = cameraManager.getCameraIdList()[0];
                cameraManager.setTorchMode(cameraId, false);
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Error releasing camera resources", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
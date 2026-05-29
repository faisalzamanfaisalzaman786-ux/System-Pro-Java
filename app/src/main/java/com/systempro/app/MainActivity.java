package com.systempro.app;   // آپ کے پیکج نیم کے مطابق تبدیل ہو جائے گا

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends Activity {

    private CameraManager cameraManager;
    private String cameraId;
    private boolean isTorchOn = false;
    private Button torchButton;

    private static final int CAMERA_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        torchButton = findViewById(R.id.torchButton);
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);

        try {
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        // چیک کریں کہ آیا پرمیشن پہلے سے موجود ہے
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // اگر نہیں تو مانگے
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE);
        } else {
            // پرمیشن موجود ہے تو بٹن فعال کریں
            enableTorchButton();
        }

        torchButton.setOnClickListener(v -> toggleTorch());
    }

    private void enableTorchButton() {
        torchButton.setEnabled(true);
        torchButton.setText("Turn ON Torch");
    }

    private void toggleTorch() {
        try {
            if (isTorchOn) {
                cameraManager.setTorchMode(cameraId, false);
                isTorchOn = false;
                torchButton.setText("Turn ON Torch");
            } else {
                cameraManager.setTorchMode(cameraId, true);
                isTorchOn = true;
                torchButton.setText("Turn OFF Torch");
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Toast.makeText(this, "Torch not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableTorchButton();
            } else {
                Toast.makeText(this, "Camera permission denied. Torch won't work.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
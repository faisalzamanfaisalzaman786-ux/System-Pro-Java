package com.system.torch1;
import com.system.torch1.R;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 100;
    private Button toggleButton;
    private CameraManager cameraManager;
    private String cameraId;
    private boolean isFlashOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toggleButton = findViewById(R.id.toggleButton);

        // Check for camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        } else {
            initializeFlashlight();
        }

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFlashlight();
            }
        });
    }

    private void initializeFlashlight() {
        // Check if the device has a flash
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            Toast.makeText(this, "Your device does not have a flashlight.", Toast.LENGTH_LONG).show();
            toggleButton.setEnabled(false);
            return;
        }

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            // Get the first camera ID (usually the back camera)
            cameraId = cameraManager.getCameraIdList()[0]; 
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error accessing camera.", Toast.LENGTH_SHORT).show();
            toggleButton.setEnabled(false);
            return;
        }

        // Set initial button state
        updateButtonState();
    }

    private void toggleFlashlight() {
        if (isFlashOn) {
            turnOffFlashlight();
        } else {
            turnOnFlashlight();
        }
        updateButtonState();
    }

    private void turnOnFlashlight() {
        try {
            if (cameraManager != null && cameraId != null) {
                cameraManager.setTorchMode(cameraId, true);
                isFlashOn = true;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to turn on flashlight.", Toast.LENGTH_SHORT).show();
        }
    }

    private void turnOffFlashlight() {
        try {
            if (cameraManager != null && cameraId != null) {
                cameraManager.setTorchMode(cameraId, false);
                isFlashOn = false;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to turn off flashlight.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateButtonState() {
        if (isFlashOn) {
            toggleButton.setText(R.string.torch_off);
        } else {
            toggleButton.setText(R.string.torch_on);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeFlashlight();
            } else {
                Toast.makeText(this, "Camera permission is required to use the flashlight.", Toast.LENGTH_LONG).show();
                toggleButton.setEnabled(false);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Turn off flashlight when the app goes into the background
        if (isFlashOn) {
            turnOffFlashlight();
            updateButtonState();
        }
    }
}
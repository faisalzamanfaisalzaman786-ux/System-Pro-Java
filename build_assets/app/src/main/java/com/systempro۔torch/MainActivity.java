package com.systempro.torch;
import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
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
    private Button toggleButton;
    private ImageView torchIcon;
    private CameraManager cameraManager;
    private String cameraId;
    private boolean isTorchOn = false;
    private static final int CAMERA_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toggleButton = findViewById(R.id.btn_toggle);
        torchIcon = findViewById(R.id.iv_torch);
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) { e.printStackTrace(); }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
        toggleButton.setOnClickListener(v -> toggleTorch());
    }
    private void toggleTorch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            if (isTorchOn) {
                cameraManager.setTorchMode(cameraId, false);
                isTorchOn = false;
                toggleButton.setText("Turn ON Torch");
                torchIcon.setImageResource(R.drawable.ic_torch_off);
                Toast.makeText(this, "Torch OFF", Toast.LENGTH_SHORT).show();
            } else {
                cameraManager.setTorchMode(cameraId, true);
                isTorchOn = true;
                toggleButton.setText("Turn OFF Torch");
                torchIcon.setImageResource(R.drawable.ic_torch_on);
                Toast.makeText(this, "Torch ON", Toast.LENGTH_SHORT).show();
            }
        } catch (CameraAccessException e) {
            Toast.makeText(this, "Camera error", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
        else Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
    }
}

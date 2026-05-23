package com.system.pro;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;
import android.hardware.camera2.CameraManager;
public class MainActivity extends AppCompatActivity {
    private Button toggleButton;
    private CameraManager cameraManager;
    private boolean isTorchOn = false;
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        toggleButton = findViewById(R.id.toggleButton);
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        toggleButton.setOnClickListener(v -> {
            try {
                String id = cameraManager.getCameraIdList()[0];
                isTorchOn = !isTorchOn;
                cameraManager.setTorchMode(id, isTorchOn);
                Toast.makeText(this, isTorchOn ? "Torch ON" : "Torch OFF", Toast.LENGTH_SHORT).show();
            } catch (Exception e) { Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show(); }
        });
    }
}
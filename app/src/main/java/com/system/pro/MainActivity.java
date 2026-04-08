package com.example.systempro;

import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

    private Button btnToggle;
    private boolean isFlashOn = false;
    private CameraManager cameraManager;
    private String cameraId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnToggle = (Button) findViewById(R.id.btn_toggle);
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            // Find the camera ID that has a flash
            String[] list = cameraManager.getCameraIdList();
            if (list.length > 0) {
                cameraId = list[0]; 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        btnToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraId == null) {
                    Toast.makeText(MainActivity.this, "No Camera Found", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                try {
                    if (isFlashOn) {
                        cameraManager.setTorchMode(cameraId, false);
                        isFlashOn = false;
                        btnToggle.setText("Turn ON");
                    } else {
                        cameraManager.setTorchMode(cameraId, true);
                        isFlashOn = true;
                        btnToggle.setText("Turn OFF");
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Flash Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Turn off flash if user leaves the app to save battery
        if (isFlashOn) {
            try {
                cameraManager.setTorchMode(cameraId, false);
                isFlashOn = false;
                btnToggle.setText("Turn ON");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

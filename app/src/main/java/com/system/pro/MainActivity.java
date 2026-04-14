package com.system.pro;   // ← یہ آپ کے فولڈر ڈھانچے کے مطابق ہے

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
    private String cameraId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnToggle = findViewById(R.id.btn_toggle);  // جدید Android میں cast کی ضرورت نہیں
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        // کیمرہ آئی ڈی حاصل کریں (جس میں فلش ہو)
        try {
            String[] cameraList = cameraManager.getCameraIdList();
            for (String id : cameraList) {
                // کچھ ڈیوائسز پر پہلا کیمرہ (بیک) ہی فلش رکھتا ہے
                cameraId = id;
                break;  // پہلا والا ہی کافی ہے
            }
        } catch (CameraAccessException e) {
            Toast.makeText(this, "کیمرہ تک رسائی ممکن نہیں", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        if (cameraId == null) {
            Toast.makeText(this, "اس ڈیوائس پر فلش موجود نہیں", Toast.LENGTH_LONG).show();
            btnToggle.setEnabled(false);
            btnToggle.setText("No Flash");
            return;
        }

        btnToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                } catch (CameraAccessException e) {
                    Toast.makeText(MainActivity.this, "فلش آن/آف کرنے میں خرابی", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // جب صارف ایپ سے باہر جائے تو فلش بند کر دیں
        if (isFlashOn) {
            try {
                cameraManager.setTorchMode(cameraId, false);
                isFlashOn = false;
                if (btnToggle != null) {
                    btnToggle.setText("Turn ON");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

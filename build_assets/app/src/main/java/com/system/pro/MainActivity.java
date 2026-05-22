import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private Button toggleButton;
    private CameraManager cameraManager;
    private String cameraId;
    private boolean isTorchOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toggleButton = findViewById(R.id.toggleButton);
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            Toast.makeText(this, "Camera access failed", Toast.LENGTH_SHORT).show();
        }

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTorchOn) {
                    turnOffTorch();
                } else {
                    turnOnTorch();
                }
            }
        });
    }

    private void turnOnTorch() {
        try {
            cameraManager.setTorchMode(cameraId, true);
            isTorchOn = true;
            Toast.makeText(this, "Torch is on", Toast.LENGTH_SHORT).show();
        } catch (CameraAccessException e) {
            Toast.makeText(this, "Failed to turn on torch", Toast.LENGTH_SHORT).show();
        }
    }

    private void turnOffTorch() {
        try {
            cameraManager.setTorchMode(cameraId, false);
            isTorchOn = false;
            Toast.makeText(this, "Torch is off", Toast.LENGTH_SHORT).show();
        } catch (CameraAccessException e) {
            Toast.makeText(this, "Failed to turn off torch", Toast.LENGTH_SHORT).show();
        }
    }
}
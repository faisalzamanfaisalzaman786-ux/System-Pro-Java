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
    private CameraManager cameraManager;
    private String cameraId;
    private boolean isTorchOn = false;
    private boolean isSOSModeOn = false;
    private long lastClickTime = 0;
    private Button torchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        torchButton = findViewById(R.id.torch_button);
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        torchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime < 500) {
                    if (!isSOSModeOn) {
                        isSOSModeOn = true;
                        Toast.makeText(MainActivity.this, "SOS Mode On", Toast.LENGTH_SHORT).show();
                    } else {
                        isSOSModeOn = false;
                        Toast.makeText(MainActivity.this, "SOS Mode Off", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (!isTorchOn) {
                        turnTorchOn();
                    } else {
                        turnTorchOff();
                    }
                }
                lastClickTime = currentTime;
            }
        });
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
    }

    private void turnTorchOn() {
        try {
            cameraManager.setTorchMode(cameraId, true);
            isTorchOn = true;
            Toast.makeText(this, "Torch On", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void turnTorchOff() {
        try {
            cameraManager.setTorchMode(cameraId, false);
            isTorchOn = false;
            Toast.makeText(this, "Torch Off", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isTorchOn) {
            turnTorchOff();
        }
    }
}
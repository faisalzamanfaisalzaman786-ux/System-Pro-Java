package {{PACKAGE_NAME}};

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private Camera camera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Button btnCapture, btnSwitch, btnGallery;
    private ImageView imageViewPreview;
    private int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private boolean isPreviewShowing = false;

    private static final int CAMERA_PERMISSION_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surfaceView = findViewById(R.id.surfaceView);
        btnCapture = findViewById(R.id.btnCapture);
        btnSwitch = findViewById(R.id.btnSwitch);
        btnGallery = findViewById(R.id.btnGallery);
        imageViewPreview = findViewById(R.id.imageViewPreview);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        // Check permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                CAMERA_PERMISSION_REQUEST);
        }

        btnCapture.setOnClickListener(v -> takePicture());
        btnSwitch.setOnClickListener(v -> switchCamera());
        btnGallery.setOnClickListener(v -> openGallery());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        openCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Refresh camera preview
        if (camera != null) {
            try {
                camera.stopPreview();
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }

    private void openCamera() {
        try {
            releaseCamera();
            camera = Camera.open(cameraId);
            camera.setPreviewDisplay(surfaceHolder);
            
            Camera.Parameters params = camera.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            camera.setParameters(params);
            
            camera.startPreview();
            isPreviewShowing = true;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Camera error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
            isPreviewShowing = false;
        }
    }

    private void switchCamera() {
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else {
            cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        openCamera();
    }

    private void takePicture() {
        if (camera == null) {
            Toast.makeText(this, "Camera not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        camera.takePicture(null, null, (data, camera1) -> {
            // Save image to file
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();

                // Show preview
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                imageViewPreview.setImageBitmap(bitmap);
                imageViewPreview.setVisibility(View.VISIBLE);
                
                Toast.makeText(this, "Photo saved: " + pictureFile.getName(), Toast.LENGTH_LONG).show();
                
                // Restart preview after 3 seconds
                imageViewPreview.postDelayed(() -> {
                    imageViewPreview.setVisibility(View.GONE);
                    openCamera();
                }, 3000);
                
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error saving photo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), 
                "CameraApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
    }

    private void openGallery() {
        imageViewPreview.setVisibility(View.VISIBLE);
        imageViewPreview.postDelayed(() -> {
            imageViewPreview.setVisibility(View.GONE);
            openCamera();
        }, 5000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}

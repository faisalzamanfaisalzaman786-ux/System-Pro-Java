package {{PACKAGE_NAME}};

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Build;
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
    private File lastPhotoFile = null;

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
                new String[]{Manifest.permission.CAMERA, 
                             Manifest.permission.WRITE_EXTERNAL_STORAGE,
                             Manifest.permission.READ_EXTERNAL_STORAGE},
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
            
            // نئے Android کے لیے Parameters سیٹ کریں
            try {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            } catch (Exception e) {
                // اگر AUTO Focus Support نہ کرے تو Ignore کریں
            }
            
            // Preview Size سیٹ کریں
            Camera.Size bestSize = getBestPreviewSize(params.getSupportedPreviewSizes(), 
                    surfaceView.getWidth(), surfaceView.getHeight());
            if (bestSize != null) {
                params.setPreviewSize(bestSize.width, bestSize.height);
            }
            
            camera.setParameters(params);
            camera.startPreview();
            isPreviewShowing = true;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Camera error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private Camera.Size getBestPreviewSize(java.util.List<Camera.Size> sizes, int width, int height) {
        Camera.Size bestSize = null;
        for (Camera.Size size : sizes) {
            if (size.width >= width && size.height >= height) {
                if (bestSize == null || 
                    (size.width - width) < (bestSize.width - width)) {
                    bestSize = size;
                }
            }
        }
        if (bestSize == null && sizes.size() > 0) {
            bestSize = sizes.get(0);
        }
        return bestSize;
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
            try {
                // File save کریں
                File pictureFile = getOutputMediaFile();
                if (pictureFile == null) {
                    Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
                    return;
                }

                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                lastPhotoFile = pictureFile;

                // Preview دکھائیں
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                imageViewPreview.setImageBitmap(bitmap);
                imageViewPreview.setVisibility(View.VISIBLE);
                
                Toast.makeText(this, "Photo saved: " + pictureFile.getName(), Toast.LENGTH_LONG).show();
                
                // 3 سیکنڈ بعد Preview ہٹائیں
                imageViewPreview.postDelayed(() -> {
                    imageViewPreview.setVisibility(View.GONE);
                    openCamera();
                }, 3000);
                
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error saving photo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File getOutputMediaFile() {
        // Android 10+ کے لیے Scoped Storage
        File mediaStorageDir;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mediaStorageDir = new File(getExternalFilesDir(null), "CameraApp");
        } else {
            mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "CameraApp");
        }
        
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
    }

    private void openGallery() {
        if (lastPhotoFile != null && lastPhotoFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(lastPhotoFile.getAbsolutePath());
            imageViewPreview.setImageBitmap(bitmap);
            imageViewPreview.setVisibility(View.VISIBLE);
            
            imageViewPreview.postDelayed(() -> {
                imageViewPreview.setVisibility(View.GONE);
                openCamera();
            }, 5000);
        } else {
            Toast.makeText(this, "No photos found", Toast.LENGTH_SHORT).show();
        }
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

    @Override
    protected void onResume() {
        super.onResume();
        if (camera == null && surfaceHolder != null) {
            openCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }
}

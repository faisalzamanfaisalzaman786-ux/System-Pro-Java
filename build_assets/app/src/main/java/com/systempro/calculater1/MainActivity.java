import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {
    private Button toggleButton;
    private TextView display;
    private CameraManager cameraManager;
    private String[] permissions = {"android.permission.CAMERA", "android.permission.FLASHLIGHT"};
    private boolean isTorchOn = false;
    private String currentExpression = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toggleButton = findViewById(R.id.toggleButton);
        display = findViewById(R.id.display);
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        ActivityCompat.requestPermissions(this, permissions, 1);

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

        findViewById(R.id.zero).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentExpression += "0";
                display.setText(currentExpression);
            }
        });

        findViewById(R.id.one).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentExpression += "1";
                display.setText(currentExpression);
            }
        });

        findViewById(R.id.two).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentExpression += "2";
                display.setText(currentExpression);
            }
        });

        findViewById(R.id.three).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentExpression += "3";
                display.setText(currentExpression);
            }
        });

        findViewById(R.id.four).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentExpression += "4";
                display.setText(currentExpression);
            }
        });

        findViewById(R.id.five).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentExpression += "5";
                display.setText(currentExpression);
            }
        });

        findViewById(R.id.six).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentExpression += "6";
                display.setText(currentExpression);
            }
        });

        findViewById(R.id.seven).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentExpression += "7";
                display.setText(currentExpression);
            }
        });

        findViewById(R.id.eight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentExpression += "8";
                display.setText(currentExpression);
            }
        });

        findViewById(R.id.nine).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentExpression += "9";
                display.setText(currentExpression);
            }
        });

        findViewById(R.id.plus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentExpression += "+";
                display.setText(currentExpression);
            }
        });

        findViewById(R.id.minus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentExpression += "-";
                display.setText(currentExpression);
            }
        });

        findViewById(R.id.multiply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentExpression += "*";
                display.setText(currentExpression);
            }
        });

        findViewById(R.id.divide).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentExpression += "/";
                display.setText(currentExpression);
            }
        });

        findViewById(R.id.equals).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    double result = eval(currentExpression);
                    display.setText(String.valueOf(result));
                    currentExpression = String.valueOf(result);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Invalid expression", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentExpression = "";
                display.setText(currentExpression);
            }
        });
    }

    private double eval(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    x = parseFactor();
                    if (func.equals("sqrt")) x = Math.sqrt(x);
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                return x;
            }
        }.parse();
    }

    private void turnOnTorch() {
        try {
            String cameraId = null;
            cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, true);
            isTorchOn = true;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void turnOffTorch() {
        try {
            String cameraId = null;
            cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, false);
            isTorchOn = false;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}
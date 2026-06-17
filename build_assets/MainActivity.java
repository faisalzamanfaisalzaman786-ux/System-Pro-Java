package {{PACKAGE_NAME}};

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    
    private TextView displayTextView;
    private String currentInput = "";
    private String operator = "";
    private double firstNumber = 0;
    private boolean isNewInput = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        displayTextView = findViewById(R.id.displayTextView);
        
        // Number buttons
        int[] numberIds = {
            R.id.button0, R.id.button1, R.id.button2, R.id.button3,
            R.id.button4, R.id.button5, R.id.button6, R.id.button7,
            R.id.button8, R.id.button9
        };
        
        for (int id : numberIds) {
            Button button = findViewById(id);
            button.setOnClickListener(v -> {
                Button b = (Button) v;
                appendNumber(b.getText().toString());
            });
        }
        
        // Operator buttons
        findViewById(R.id.buttonAdd).setOnClickListener(v -> setOperator("+"));
        findViewById(R.id.buttonSubtract).setOnClickListener(v -> setOperator("-"));
        findViewById(R.id.buttonMultiply).setOnClickListener(v -> setOperator("*"));
        findViewById(R.id.buttonDivide).setOnClickListener(v -> setOperator("/"));
        findViewById(R.id.buttonEquals).setOnClickListener(v -> calculateResult());
        findViewById(R.id.buttonClear).setOnClickListener(v -> clearAll());
    }
    
    private void appendNumber(String number) {
        if (isNewInput) {
            currentInput = number;
            isNewInput = false;
        } else {
            currentInput += number;
        }
        displayTextView.setText(currentInput);
    }
    
    private void setOperator(String op) {
        if (!currentInput.isEmpty()) {
            firstNumber = Double.parseDouble(currentInput);
            operator = op;
            isNewInput = true;
        }
    }
    
    private void calculateResult() {
        if (!currentInput.isEmpty() && !operator.isEmpty()) {
            double secondNumber = Double.parseDouble(currentInput);
            double result = 0;
            
            switch (operator) {
                case "+": result = firstNumber + secondNumber; break;
                case "-": result = firstNumber - secondNumber; break;
                case "*": result = firstNumber * secondNumber; break;
                case "/": 
                    if (secondNumber != 0) {
                        result = firstNumber / secondNumber;
                    } else {
                        displayTextView.setText("Error");
                        return;
                    }
                    break;
            }
            
            displayTextView.setText(String.valueOf(result));
            currentInput = String.valueOf(result);
            operator = "";
            isNewInput = true;
        }
    }
    
    private void clearAll() {
        currentInput = "";
        operator = "";
        firstNumber = 0;
        isNewInput = true;
        displayTextView.setText("0");
    }
}

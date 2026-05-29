package com.systempro.app1;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // کوئی findViewById کی ضرورت نہیں، لے آؤٹ میں ہی ٹیکسٹ ڈال دیا ہے
    }
}
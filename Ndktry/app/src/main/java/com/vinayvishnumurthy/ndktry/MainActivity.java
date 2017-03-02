package com.vinayvishnumurthy.ndktry;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    Button b;
    EditText a, c;
    TextView t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        // Example of a call to a native method

        b = (Button) findViewById(R.id.button);
        a = (EditText) findViewById(R.id.editText);
        c = (EditText) findViewById(R.id.editText2);
        t = (TextView) findViewById(R.id.textView2);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int x = Integer.parseInt(a.getText().toString());
                int y = Integer.parseInt(c.getText().toString());
                //Toast.makeText(MainActivity.this, x+y, Toast.LENGTH_SHORT).show();
                int sum = addNumbers(x,y);
                String s = "Sum = "+ sum;
                t.setText(s);
                a.setFocusable(false);
                c.setFocusable(false);
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public native int addNumbers(int a, int b);
}

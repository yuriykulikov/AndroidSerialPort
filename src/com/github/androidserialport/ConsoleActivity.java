package com.github.androidserialport;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class ConsoleActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_console);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_console, menu);
        return true;
    }
}

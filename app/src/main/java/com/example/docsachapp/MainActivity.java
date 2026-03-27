package com.example.docsachapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Mock authentication check - start LoginActivity first for now
        // Intent intent = new Intent(this, LoginActivity.class);
        // startActivity(intent);
        // finish();
        
        setContentView(R.layout.activity_main);
    }
}

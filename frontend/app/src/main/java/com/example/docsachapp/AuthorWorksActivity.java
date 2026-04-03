package com.example.docsachapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import androidx.appcompat.app.AppCompatActivity;

public class AuthorWorksActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author_works);

        ImageView btnBack = findViewById(R.id.btn_back);
        RelativeLayout itemWork = findViewById(R.id.item_work);

        btnBack.setOnClickListener(v -> finish());
        
        itemWork.setOnClickListener(v -> {
            Intent intent = new Intent(AuthorWorksActivity.this, BookEditActivity.class);
            startActivity(intent);
        });
    }
}

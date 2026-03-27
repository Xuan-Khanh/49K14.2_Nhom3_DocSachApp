package com.example.docsachapp;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class CommentsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        ImageView btnBack = findViewById(R.id.btn_back);
        EditText etComment = findViewById(R.id.et_comment);
        ImageView btnSend = findViewById(R.id.btn_send);

        btnBack.setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> {
            String comment = etComment.getText().toString().trim();
            if (comment.isEmpty()) {
                Toast.makeText(this, "Chưa nhập bình luận", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Đã gửi bình luận", Toast.LENGTH_SHORT).show();
                etComment.setText("");
            }
        });
    }
}

package com.example.bloomify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Home_Page extends AppCompatActivity {

    Button btn_about, btn_next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);

        btn_about = findViewById(R.id.btn_about);
        btn_next = findViewById(R.id.btn_next);

        btn_about.setOnClickListener(v -> {
            Intent intent = new Intent(Home_Page.this, About_Us.class);
            startActivity(intent);
        });

        btn_next.setOnClickListener(v -> {
           Intent intent = new Intent(Home_Page.this, Next_Page.class);
           startActivity(intent);
        });

    }
}
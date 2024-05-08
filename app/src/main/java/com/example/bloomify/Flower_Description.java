package com.example.bloomify;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;

public class Flower_Description extends AppCompatActivity {
    Context context = this;
    ImageView imageView_flowers;
    TextView textView_flower_name, textView_flower_description;
    String description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_flower_description);
        Intent intent = getIntent();
        String flower_name = intent.getStringExtra("flower_name");

        imageView_flowers = findViewById(R.id.imageView_flowers);
        textView_flower_name = findViewById(R.id.textView_flower_name);
        textView_flower_description = findViewById(R.id.textView_flower_description);

        String path = flower_name.toLowerCase().trim().replace(" ", "");
        int resourceId = context.getResources().getIdentifier(path, "drawable", context.getPackageName());
        if (resourceId != 0) {
            imageView_flowers.setImageResource(resourceId);
        }

        textView_flower_name.setText(flower_name);

        getDescription(flower_name);

    }
    void getDescription(String flower_name) {
        DBHelper dbHelper = new DBHelper(this);
        try {
            dbHelper.importDatabase();
        }catch (IOException e) {
            e.printStackTrace();
        }


        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT flower_description FROM flowers WHERE flower_name = ?", new String[] {flower_name});
        if (cursor.moveToFirst()) {
            do {
                // Check if the column exists in the cursor before retrieving its index
                int descriptionIndex = cursor.getColumnIndex("flower_description");
                if (descriptionIndex != -1) {
                    description = cursor.getString(descriptionIndex);
                }
            }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        textView_flower_description.setText(description);
    }


}
package com.example.bloomify;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloomify.ml.Model;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class Next_Page extends AppCompatActivity implements Select_Listener {
    String name;
    RecyclerView_Adapter adapter;
    ImageView imageView_flowers;
    ArrayList<Flower_List> arrayList;
    Button btn_take_picture;
    int imageSize = 224;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_next_page);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        btn_take_picture = findViewById(R.id.btn_take_picture);

        int columnCount = 2;
        recyclerView.setLayoutManager(new GridLayoutManager(this, columnCount));

        arrayList = new ArrayList<Flower_List>();
        adapter = new RecyclerView_Adapter(this, arrayList, this);

        recyclerView.setAdapter(adapter);

        getData();

        btn_take_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch camera if we have permission
                if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, 1);
                } else {
                    //Request camera permission if we don't have it.
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bitmap image = (Bitmap) data.getExtras().get("data");
            int dimension = Math.min(image.getWidth(), image.getHeight());
            image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);

            image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
            classifyImage(image);

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void classifyImage(Bitmap image) {
        try {
            Model model = Model.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);

            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;
            for (int i = 0; i < imageSize; i++) {
                for (int j = 0; j < imageSize; j++) {
                    int val = intValues[pixel++];
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }
            // Read labels from labels.txt
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("labels.txt")));
            String[] lines = new String[confidences.length];

            for (int i = 0; i < confidences.length; i++) {
                String line = reader.readLine();
                lines[i] = line.replaceAll("\\d", "");
            }

            float percentage = confidences[maxPos] * 100;
            String flower = lines[maxPos];

            if (percentage < 85) {
                Toast.makeText(this, "This is not a flower or it is not in our database", Toast.LENGTH_SHORT).show();
            }else {
                if (flower.charAt(0) == ' ') {
                    flower = flower.substring(1);
                }

                Intent intent = new Intent(Next_Page.this, Flower_Description.class);
                intent.putExtra("flower_name", flower);
                startActivity(intent);
            }

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception

        }
    }

    void getData() {
        DBHelper dbHelper = new DBHelper(this);
        try {
            dbHelper.importDatabase();
        }catch (IOException e) {
            e.printStackTrace();
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT flower_name FROM flowers ORDER BY id DESC", null);
        if (cursor.moveToFirst()) {
            do {
                // Check if the column exists in the cursor before retrieving its index
                int idIndex = cursor.getColumnIndex("flower_name");
                if (idIndex != -1) {
                    arrayList.add(new Flower_List(cursor.getString(idIndex)));
                }
        }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
    }
    @Override
    public void onClickFlower(Flower_List flower_list) {
        String name = flower_list.getName();
        Intent intent = new Intent(Next_Page.this, Flower_Description.class);
        intent.putExtra("flower_name", name);
        startActivity(intent);

    }

    public int getId(String x) {
        int id = 0;
        DBHelper dbHelper = new DBHelper(this);
        try {
            dbHelper.importDatabase();
        }catch (IOException e) {
            e.printStackTrace();
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM flowers WHERE flower_name = '" + x + "'", null);
        if (cursor.moveToFirst()) {
            do {
                // Check if the column exists in the cursor before retrieving its index
                int idIndex = cursor.getColumnIndex("id");
                if (idIndex != -1) {
                    id = cursor.getInt(idIndex);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return id;
    }
}
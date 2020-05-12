package com.example.imessagechat.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.imessagechat.R;

public class ImageViewerActivity extends AppCompatActivity {

    private ImageView imageView;

    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        imageView = (ImageView)findViewById(R.id.image_viewer);

        imageUrl = getIntent().getStringExtra("url");

        Glide.with(getApplicationContext()).load(imageUrl).into(imageView);
    }
}

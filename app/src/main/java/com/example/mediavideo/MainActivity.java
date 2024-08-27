package com.example.mediavideo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.mediavideo.databinding.ActivityMainBinding;
import com.example.video.activity.CameraVideoActivity;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.sampleText.setOnClickListener(v -> {
            startActivity(new Intent(this, CameraVideoActivity.class));
        });
    }


}
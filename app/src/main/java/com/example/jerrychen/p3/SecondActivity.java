package com.example.jerrychen.p3;

import android.content.Intent;
import android.support.annotation.IntegerRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class SecondActivity extends AppCompatActivity {
    private ImageButton imageButton;
    private ImageButton imageButton2;
    // private ImageButton imageButton4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        imageButton = findViewById(R.id.imagebutton3);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openVoiceActivity();
            }
        });

        imageButton2 = findViewById(R.id.imagebutton5);
        imageButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSenActivity();
            }
        });


    }

    private void openSenActivity() {
        Intent intent = new Intent(this, SensitivityActivity.class);
        startActivity(intent);
    }

    private void openVoiceActivity() {
        Intent intent = new Intent(this, VoiceActivity.class);
        startActivity(intent);
    }
}

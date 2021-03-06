package com.example.crazydriver.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.crazydriver.R;

public class StartGameActivity extends AppCompatActivity {

    private Button btnStartGame;
    private Button btnTop10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_game);

        findViews();

        btnTop10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(StartGameActivity.this,GameOverActivity.class));

            }
        });

        btnStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(StartGameActivity.this,GameMenuActivity.class));

            }
        });
    }

    private void findViews() {
        btnStartGame = findViewById(R.id.panel_BTN_startGame);
        btnTop10 = findViewById(R.id.panel_BTN_top10);
    }
}
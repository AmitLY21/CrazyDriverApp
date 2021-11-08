package com.example.crazydriver.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.crazydriver.R;

public class GameOverActivity extends AppCompatActivity {

    private Button btnExit;
    private Button btnRerun;

    private TextView lblFinalScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_over_activity);

        findViews();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String score = extras.getString("finalScore");
            lblFinalScore.setText("Final Score: " + score);
        }

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveTaskToBack(true);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            }
        });

        btnRerun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(GameOverActivity.this,StartGameActivity.class));
            }
        });

    }

    private void findViews() {
        btnExit = findViewById(R.id.panel_BTN_exit);
        btnRerun = findViewById(R.id.panel_BTN_rerun);

        lblFinalScore = findViewById(R.id.panel_LBL_final_text);
    }
}

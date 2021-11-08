package com.example.crazydriver.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.crazydriver.R;

public class GameMenuActivity extends AppCompatActivity {

    private ImageButton btnCar;
    private ImageButton btnCar1;
    private ImageButton btnRaceCar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_menu);

        findViews();

        btnCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchActivities(1);
            }
        });

        btnCar1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchActivities(2);
            }
        });

        btnRaceCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchActivities(3);
            }
        });
    }

    private void findViews() {
        btnCar = findViewById(R.id.car);
        btnCar1 = findViewById(R.id.car1);
        btnRaceCar = findViewById(R.id.race_car);
    }

    private void switchActivities(int choice) {
        Intent switchActivityIntent = new Intent(GameMenuActivity.this, GameActivity.class);
        switch (choice){
            case 1:
                switchActivityIntent.putExtra("carImage", R.drawable.car);
                break;
            case 2:
                switchActivityIntent.putExtra("carImage", R.drawable.car1);
                break;
            case 3:
                switchActivityIntent.putExtra("carImage", R.drawable.race_car);
                break;
        }
        startActivity(switchActivityIntent);
    }
}
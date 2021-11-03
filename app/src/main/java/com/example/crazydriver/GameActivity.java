package com.example.crazydriver;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    private Player player;

    private ImageView[][] gamePanel;
    private int[][] values;

    private ImageButton btnLeft;
    private ImageButton btnRight;

    private ArrayList<ImageView> hearts = new ArrayList<>();
    private ImageView imgHeart1;
    private ImageView imgHeart2;
    private ImageView imgHeart3;

    Random obstacleRand = new Random();
    Random columnRand = new Random();

    private TextView lblScore;
    private int score = 0;

    int delay = 1000; // 1000 milliseconds == 1 second
    private int clockCounter = 0;
    final Handler handler = new Handler();

    /**
     * the clock of the game , every 1 second we move obstacles and checks
     * whether the player got hit or not by the obstacles
     */
    Runnable r = new Runnable() {
        public void run() {
            handler.postDelayed(this, delay); //tick
            checkCollision();
            moveObstacles();
            if (clockCounter % 3 == 0) {
                generateObstacle();
            }
            if (player.getLives() == -1) {
                player.setLives(2);
                restoreLives();
            }
            clockCounter++;

            if (clockCounter % 30 == 0 && delay > 500) { //Every 30 ticks the speed increase
                delay -= 100;
                Toast.makeText(GameActivity.this, "Speed Increased", Toast.LENGTH_SHORT).show();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
        gamePanelReset();

        player = new Player();

        MediaPlayer ring = MediaPlayer.create(GameActivity.this, R.raw.city_traffic);
        ring.setLooping(true);
        ring.setVolume(.33f, .33f);
        ring.start();

        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveRight();
            }
        });

        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveLeft();
            }
        });
    }

    private void moveLeft() {
        if (player.getCurrentPosition() - 1 >= 0) {
            int lastPosition = player.getCurrentPosition();
            player.setCurrentPosition(player.getCurrentPosition() - 1);
            int newPosition = player.getCurrentPosition();

            switchPlayerPosition(lastPosition, newPosition);
        }
    }

    private void moveRight() {
        if (player.getCurrentPosition() + 1 < 3) {
            int lastPosition = player.getCurrentPosition();
            player.setCurrentPosition(player.getCurrentPosition() + 1);
            int newPosition = player.getCurrentPosition();

            switchPlayerPosition(lastPosition, newPosition);
        }
    }

    private void switchPlayerPosition(int lastPosition, int newPosition) {
        values[5][lastPosition] = 0; //set empty cell
        values[5][newPosition] = 5; //set car
        updateUI();
    }

    private void gamePanelReset() {
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values[0].length; j++) {
                values[i][j] = 0;
            }
        }
        values[5][1] = 5; //Set car in the middle
        updateUI();
    }

    private void moveObstacles() {
        int[] tempValues;
        for (int i = values.length - 2; i > 0; i--) {
            tempValues = values[i];
            values[i] = values[i - 1];
            values[i - 1] = tempValues;
        }
        for (int i = 0; i < values[0].length; i++) {
            values[0][i] = 0;
        }
        updateUI();
    }

    private void checkCollision() {
        for (int i = 0; i < values[5].length; i++) {
            if (values[5][i] == 5 && values[4][i] == 3) { //HIT Wrench (extra life)
                addLive();
                removeObstacles(true);
                break;
            }
            if (values[5][i] == 5 && values[4][i] == 4) { //HIT coin (score+1)
                removeObstacles(false);
                MediaPlayer coinCollectSound = MediaPlayer.create(GameActivity.this, R.raw.coin_collect);
                coinCollectSound.start();
                break;
            }
            if (values[5][i] == 5 && values[4][i] == 1) { //HIT Obstacle hole
                collisionActions(true);
                break;
            }
            if (values[5][i] == 5 && values[4][i] == 2) { //HIT Obstacle barrier
                collisionActions(true);
                break;
            }
        }
    }

    private void updateScore() {
        score++;
        lblScore.setText("" + score);
    }

    private void collisionActions(boolean isCollided) {
        vibrate();
        Toast.makeText(this, "-1 Heart", Toast.LENGTH_SHORT).show();
        MediaPlayer carCrashSound = MediaPlayer.create(GameActivity.this, R.raw.car_crash);
        carCrashSound.start();
        removeHearts();
        removeObstacles(isCollided);
    }

    private void vibrate() {
        // Get instance of Vibrator from current Context
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 400 milliseconds
        v.vibrate(400);
    }

    private void addLive() {
        if (player.getLives() < 2) {
            player.setLives(player.getLives() + 1);
            Toast.makeText(this, "+1 Heart", Toast.LENGTH_SHORT).show();
        }
        hearts.get(player.getLives()).setVisibility(View.VISIBLE);

    }

    private void restoreLives() {
        Toast.makeText(this, "Restoring Lives", Toast.LENGTH_SHORT).show();
        for (ImageView view : hearts) {
            view.setVisibility(View.VISIBLE);
        }
    }


    private void removeHearts() {
        int currentLives = player.getLives();
        hearts.get(currentLives).setVisibility(View.INVISIBLE);
        player.setLives(currentLives - 1);
    }

    private void removeObstacles(boolean isCollided) {
        for (int i = 0; i < values[4].length; i++) {
            values[4][i] = 0;
        }
        if (!isCollided) {
            updateScore();
        }
        updateUI();
    }

    private void generateObstacle() {
        int colNum = columnRand.nextInt(3);
        if (clockCounter % 10 == 0) {// every 10 ticks generate wrench (+1 heart)
            values[0][colNum] = 3;
        } else if (clockCounter % 5 == 0) { // every 5 ticks generate coin
            values[0][colNum] = 4;
        } else {
            values[0][colNum] = obstacleRand.nextInt(2) + 1;
        }
        updateUI();
    }

    /**
     * 0 - empty cell (None)
     * 1 - obstacle cell (hole)
     * 2 - obstacle cell (barrier)
     * 3 - extra life (wrench)
     * 4 - coin
     * 5 - car
     */
    private void updateUI() {
        for (int i = 0; i < gamePanel.length; i++) {
            for (int j = 0; j < gamePanel[i].length; j++) {
                ImageView im = gamePanel[i][j];
                switch (values[i][j]) {
                    case 0:
                        im.setVisibility(View.INVISIBLE);
                        break;
                    case 1:
                        im.setVisibility(View.VISIBLE);
                        im.setImageResource(R.drawable.img_hole);
                        break;
                    case 2:
                        im.setVisibility(View.VISIBLE);
                        im.setImageResource(R.drawable.img_barrier);
                        break;
                    case 3:
                        im.setVisibility(View.VISIBLE);
                        im.setImageResource(R.drawable.img_wrench);
                        break;
                    case 4:
                        im.setVisibility(View.VISIBLE);
                        im.setImageResource(R.drawable.coin);
                        break;
                    case 5:
                        im.setVisibility(View.VISIBLE);
                        im.setImageResource(R.drawable.car);
                        break;
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        startTicker();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopTicker();
    }

    private void startTicker() {
        handler.postDelayed(r, delay);
    }

    private void stopTicker() {
        handler.removeCallbacks(r);
    }

    private void findViews() {
        gamePanel = new ImageView[][]{
                {findViewById(R.id.panel_IMG_00), findViewById(R.id.panel_IMG_01), findViewById(R.id.panel_IMG_02)},
                {findViewById(R.id.panel_IMG_10), findViewById(R.id.panel_IMG_11), findViewById(R.id.panel_IMG_12)},
                {findViewById(R.id.panel_IMG_20), findViewById(R.id.panel_IMG_21), findViewById(R.id.panel_IMG_22)},
                {findViewById(R.id.panel_IMG_30), findViewById(R.id.panel_IMG_31), findViewById(R.id.panel_IMG_32)},
                {findViewById(R.id.panel_IMG_40), findViewById(R.id.panel_IMG_41), findViewById(R.id.panel_IMG_42)},
                {findViewById(R.id.panel_IMG_car0), findViewById(R.id.panel_IMG_car1), findViewById(R.id.panel_IMG_car2)}
        };

        values = new int[gamePanel.length][gamePanel[0].length];

        btnRight = findViewById(R.id.panel_BTN_right);
        btnLeft = findViewById(R.id.panel_BTN_left);

        imgHeart1 = findViewById(R.id.panel_IMG_heart1);
        imgHeart2 = findViewById(R.id.panel_IMG_heart2);
        imgHeart3 = findViewById(R.id.panel_IMG_heart3);

        hearts.add(0, imgHeart1);
        hearts.add(1, imgHeart2);
        hearts.add(2, imgHeart3);

        lblScore = findViewById(R.id.panel_LBL_score);
    }
}
package com.example.crazydriver.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.crazydriver.Player;
import com.example.crazydriver.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    private static final int REQUEST_LOCATION = 1;
    final Handler handler = new Handler();

    private final ArrayList<ImageView> hearts = new ArrayList<>(3);
    private final Random obstacleRand = new Random();
    private final Random columnRand = new Random();
    int delay = 500; // 1000 milliseconds == 1 second
    private int clockCounter = 0;

    LocationManager locationManager;
    String latitude = "0.0";
    String longitude = "0.0";

    private Player player;
    private int car_id;
    private int gameMode;

    private SensorManager sensorManager;
    private Sensor accSensor;

    private ImageView[][] gamePanel;
    private int[][] values;
    private int carRow = 0;

    private ImageButton btnLeft;
    private ImageButton btnRight;
    private ImageView imgHeart1;
    private ImageView imgHeart2;
    private ImageView imgHeart3;
    private TextView lblScore;
    private int score = 0;

    private MediaPlayer cityTrafficSound;
    private MediaPlayer carCrashSound;
    private MediaPlayer wrenchSound;
    private MediaPlayer coinCollectSound;

    /**
     * the clock of the game , every tick we move obstacles and checks
     * whether the player got hit or not by the obstacles
     */
    Runnable r = new Runnable() {
        public void run() {
            handler.postDelayed(this, delay); //1 delay = tick
            checkCollision();
            moveObstacles();

            if (clockCounter % 2 == 0) {
                generateObstacle();
            }

            if (player.getLives() == -1) {
                mediaStop();
                player.setLives(2);
                switchActivities();
            }

            clockCounter++;
            updateSpeed();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            car_id = extras.getInt("carImage");
            gameMode = extras.getInt("gameMode");
            playBackgroundMusic();
        }

        findViews(gameMode);
        gamePanelReset();
        carRow = values.length - 1;
        player = new Player();

        if (gameMode == 1) {
            initSensor();
            btnLeft.setClickable(false);
            btnRight.setClickable(false);
            btnLeft.setVisibility(View.INVISIBLE);
            btnRight.setVisibility(View.INVISIBLE);
        }

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            OnGPS();
        } else {
            getLocation();
        }
        playBackgroundMusic();

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

    private void playBackgroundMusic() {
        cityTrafficSound = MediaPlayer.create(GameActivity.this, R.raw.city_traffic);
        cityTrafficSound.setLooping(true);
        cityTrafficSound.setVolume(0.7f , 0.7f);
        cityTrafficSound.start();
    }

    //---------Location----------
    private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(
                GameActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                GameActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (locationGPS != null) {
                double lat = locationGPS.getLatitude();
                double longi = locationGPS.getLongitude();
                latitude = String.valueOf(lat);
                longitude = String.valueOf(longi);
            } else {
                Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //---------Sensors----------
    private void initSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    /*
     * Need to adjust the sensor movement
     */
    private final SensorEventListener accSensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            DecimalFormat df = new DecimalFormat("##.##");
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            Log.d("sens", "onSensorChanged: " + "x: " + df.format(x) + " ,y: " + df.format(y) + " ,z: " + df.format(z));
            if (x + 1 > 2.5)
                moveLeft();
            else if (x - 1 < -2.5)
                moveRight();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };

    //---------Movement---------
    private void moveLeft() {
        if (player.getCurrentPosition() - 1 >= 0) {
            int lastPosition = player.getCurrentPosition();
            player.setCurrentPosition(player.getCurrentPosition() - 1);
            int newPosition = player.getCurrentPosition();
            switchPlayerPosition(lastPosition, newPosition);
        }
    }

    private void moveRight() {
        if (player.getCurrentPosition() + 1 < values[carRow].length) {
            int lastPosition = player.getCurrentPosition();
            player.setCurrentPosition(player.getCurrentPosition() + 1);
            int newPosition = player.getCurrentPosition();

            switchPlayerPosition(lastPosition, newPosition);
        }
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

    //---------Switch Methods---------
    private void switchPlayerPosition(int lastPosition, int newPosition) {
        values[carRow][lastPosition] = 0; //set empty cell
        values[carRow][newPosition] = 5; //set car
        updateUI();
    }

    private void switchActivities() {
        Intent switchActivityIntent = new Intent(GameActivity.this, GameOverActivity.class);
        //Passing the score value to the game over activity
        switchActivityIntent.putExtra("finalScore", String.valueOf(score));
        switchActivityIntent.putExtra("lat", Double.valueOf(latitude));
        switchActivityIntent.putExtra("lon", Double.valueOf(longitude));
        finish();
        startActivity(switchActivityIntent);
    }

    //---------Data Update Functions---------
    private void updateScore(int points) {
        score += points;
        lblScore.setText("" + score);
    }

    private void updateSpeed() {
        if (clockCounter % 30 == 0 && delay > 450) { //Every 30 ticks the speed increase
            delay -= 40;
            Toast.makeText(GameActivity.this, "Speed Increased", Toast.LENGTH_SHORT).show();
        }
    }

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
                        im.setImageResource(car_id);
                        break;
                }
            }
        }
    }

    //---------Gameplay & Logic Functions---------

    /**
     * 0 - empty cell (None)
     * 1 - obstacle cell (hole)
     * 2 - obstacle cell (barrier)
     * 3 - extra life (wrench)
     * 4 - coin
     * 5 - car
     */
    private void checkCollision() {
        for (int i = 0; i < values[carRow].length; i++) {
            if (values[carRow][i] == 5 && values[carRow - 1][i] == 3) { //HIT Wrench (extra life)
                addLive();
                wrenchSound = MediaPlayer.create(GameActivity.this, R.raw.wrench_sound);
                wrenchSound.start();
                removeObstacles(true);
                break;
            }
            if (values[carRow][i] == 5 && values[carRow - 1][i] == 4) { //HIT coin (score+1)
                removeObstacles(false);
                Toast.makeText(this, "+1 Score", Toast.LENGTH_SHORT).show();
                coinCollectSound = MediaPlayer.create(GameActivity.this, R.raw.coin_collect);
                coinCollectSound.start();
                break;
            }
            if (values[carRow][i] == 5 && values[carRow - 1][i] == 1) { //HIT Obstacle hole
                collisionActions(true);
                break;
            }
            if (values[carRow][i] == 5 && values[carRow - 1][i] == 2) { //HIT Obstacle barrier
                collisionActions(true);
                break;
            }
        }
    }

    private void collisionActions(boolean isCollided) {
        vibrate();
        if (player.getLives() != 0) {
            Toast.makeText(this, "-1 Heart", Toast.LENGTH_SHORT).show();
        }
        carCrashSound = MediaPlayer.create(GameActivity.this, R.raw.car_crash);
        carCrashSound.start();
        removeHeart();
        removeObstacles(isCollided);
    }

    //---------Obstacles---------
    private void removeObstacles(boolean isCollided) {
        for (int i = 0; i < values[5].length; i++) {
            values[carRow - 1][i] = 0;
        }
        if (!isCollided) {
            updateScore(1);
        }
        updateUI();
    }

    private void generateObstacle() {
        int colNum = columnRand.nextInt(5);
        if (clockCounter % 10 == 0) {// every 10 ticks generate wrench (+1 heart)
            values[0][colNum] = 3;
        } else if (clockCounter % 3 == 0) { // every 5 ticks generate coin
            values[0][colNum] = 4;
        } else {
            values[0][colNum] = obstacleRand.nextInt(2) + 1;
        }
        updateUI();
    }

    //---------Health Bar---------
    private void addLive() {
        if (player.getLives() < 2) {
            player.setLives(player.getLives() + 1);
            Toast.makeText(this, "+1 Heart & +5 Score", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "+5 Score", Toast.LENGTH_SHORT).show();
        }
        updateScore(5);
        hearts.get(player.getLives()).setVisibility(View.VISIBLE);

    }

    private void removeHeart() {
        int currentLives = player.getLives();
        hearts.get(currentLives).setVisibility(View.INVISIBLE);
        player.setLives(currentLives - 1);
    }

    //---------Game Panel & View Related---------
    private void gamePanelReset() {
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values[0].length; j++) {
                values[i][j] = 0;
            }
        }
        values[values.length - 1][2] = 5; //Set car in the middle
        lblScore.setText("0000");
        updateUI();
    }

    /*
     * if gameMode == 0 - button mode
     * if gameMode == 1 - sensor mode
     */
    private void findViews(int gameMode) {
        gamePanel = new ImageView[][]{
                {findViewById(R.id.panel_IMG_00), findViewById(R.id.panel_IMG_01), findViewById(R.id.panel_IMG_02), findViewById(R.id.panel_IMG_03), findViewById(R.id.panel_IMG_04)},
                {findViewById(R.id.panel_IMG_10), findViewById(R.id.panel_IMG_11), findViewById(R.id.panel_IMG_12), findViewById(R.id.panel_IMG_13), findViewById(R.id.panel_IMG_14)},
                {findViewById(R.id.panel_IMG_20), findViewById(R.id.panel_IMG_21), findViewById(R.id.panel_IMG_22), findViewById(R.id.panel_IMG_23), findViewById(R.id.panel_IMG_24)},
                {findViewById(R.id.panel_IMG_30), findViewById(R.id.panel_IMG_31), findViewById(R.id.panel_IMG_32), findViewById(R.id.panel_IMG_33), findViewById(R.id.panel_IMG_34)},
                {findViewById(R.id.panel_IMG_40), findViewById(R.id.panel_IMG_41), findViewById(R.id.panel_IMG_42), findViewById(R.id.panel_IMG_43), findViewById(R.id.panel_IMG_44)},
                {findViewById(R.id.panel_IMG_50), findViewById(R.id.panel_IMG_51), findViewById(R.id.panel_IMG_52), findViewById(R.id.panel_IMG_53), findViewById(R.id.panel_IMG_54)},
                {findViewById(R.id.panel_IMG_60), findViewById(R.id.panel_IMG_61), findViewById(R.id.panel_IMG_62), findViewById(R.id.panel_IMG_63), findViewById(R.id.panel_IMG_64)},
                {findViewById(R.id.panel_IMG_car0), findViewById(R.id.panel_IMG_car1), findViewById(R.id.panel_IMG_car2), findViewById(R.id.panel_IMG_car3), findViewById(R.id.panel_IMG_car4)}
        };

        values = new int[gamePanel.length][gamePanel[0].length];

        //if(gameMode == 0) {
        btnRight = findViewById(R.id.panel_BTN_right);
        btnLeft = findViewById(R.id.panel_BTN_left);
        //}

        imgHeart1 = findViewById(R.id.panel_IMG_heart1);
        imgHeart2 = findViewById(R.id.panel_IMG_heart2);
        imgHeart3 = findViewById(R.id.panel_IMG_heart3);

        hearts.add(0, imgHeart1);
        hearts.add(1, imgHeart2);
        hearts.add(2, imgHeart3);

        lblScore = findViewById(R.id.panel_LBL_score);
    }

    //---------Other---------
    private void vibrate() {
        // Get instance of Vibrator from current Context
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 400 milliseconds
        v.vibrate(400);
    }

    private void mediaStop() {
        cityTrafficSound.stop();
        carCrashSound.stop();
    }

    //---------Activities Life Cycle Methods---------
    @Override
    protected void onStart() {
        super.onStart();
        startTicker();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameMode == 1)
            sensorManager.registerListener(accSensorEventListener, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
        cityTrafficSound.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopTicker();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameMode == 1)
            sensorManager.unregisterListener(accSensorEventListener);
        cityTrafficSound.stop();
    }

    //---------Handler Tickers---------
    private void startTicker() {
        handler.postDelayed(r, delay);
    }

    private void stopTicker() {
        handler.removeCallbacks(r);
    }


}
package com.example.crazydriver;

public class Player {
    private int lives;
    private int currentPosition;

    public Player() {
        this.lives = 2;
        this.currentPosition = 1;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }
}

package iss.workshop.ca;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    //PROGRESS BAR : declare text view and progress bar so you can use it here
    private ProgressBar mProgressBar;
    private TextView mLoadingText;
    //declare counter variable to determine progress bar status
    private int mProgressStatus = 0;

    //PROGRESS BAR : initialise image view
    ImageView currentView = null;

    //TIMER : declare text view and button so you can use it here
    TextView timerText;
    Button pauseBtn;
    Timer timer;
    TimerTask timerTask;
    Double time = 0.0;
    boolean timerStarted = false;

    //counting cards for game logic
    private int countPair = 0;
    private int cardsOpen = 0;
    private TextView mScore;

    //create integer array for id of each image item
    int[] drawable = new int[]{
            R.drawable.ic_car,
            R.drawable.ic_flower,
            R.drawable.ic_thrashcan,
            R.drawable.ic_trophy,
            R.drawable.ic_pin,
            R.drawable.ic_sun
    };

    //create integer array to hardcode position of each image item; duplicate since there's 2 of each image
    int[] position = {
            0, 1, 2, 3, 4, 5, 0, 1, 2, 3, 4, 5
    };

    //set current position to -1 at start of game; so it won't overlap with the above positions
    int currentPosition = -1;

    //use this to prevent false clicking
    private long mLastClickTime = 0;

    //pause screen
    private ImageButton mPauseScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //call method to shuffle images
        shuffleImages();

        //bind gridview
        GridView gridView = (GridView) findViewById(R.id.gridView);

        //set adapter to gridview
        ImageAdapter imageAdapter = new ImageAdapter(this);
        gridView.setAdapter(imageAdapter);

        //PROGRESS BAR : connect up xml elements for progress bar and text view using variables declared above so android knows what you're using
        mProgressBar = (ProgressBar) findViewById(R.id.ProgressBar);
        mProgressBar.setVisibility(View.INVISIBLE);

        //score is invisible at first
        mScore = (TextView) findViewById(R.id.Score);
        mScore.setVisibility(View.INVISIBLE);

        //attributes for pause screen
        mPauseScreen = (ImageButton) findViewById(R.id.pauseScreen);
        mPauseScreen.setVisibility(View.INVISIBLE);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {

                if (currentPosition < 0) {
                    //prevent false clicking for 1 second after every 2 cards are selected
                    long currentClickTime = SystemClock.uptimeMillis();
                    long elapsedTime = currentClickTime - mLastClickTime;
                    mLastClickTime = currentClickTime;
                    if (elapsedTime <= 1000) {
                        return;
                    }

                    currentPosition = pos;
                    currentView = (ImageView) view;
                    currentView.setImageResource(drawable[position[pos]]);
                    cardsOpen = 1;
                }

                else {
                    if (currentPosition == pos) {
                        //don't allow double clicking of the same card
                        return;
                    }

                    cardsOpen = 2;
                    if (position[currentPosition] != position[pos]) {
                        ((ImageView) view).setImageResource(drawable[position[pos]]); //show both cards for a while
                        Toast toast = Toast.makeText(getApplicationContext(), "Not a match", Toast.LENGTH_SHORT);
                        toast.setGravity(0, 0, 0);
                        toast.show();

                        //after showing both unmatched cards for 0.5second, flip both back
                        ((ImageView) view).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                currentView.setImageResource(R.drawable.ic_back);
                                ((ImageView) view).setImageResource(R.drawable.ic_back);
                            }
                        }, 1000);
                    }
                    else if (position[currentPosition] == position[pos] && cardsOpen == 2){
                        currentView.setImageResource(drawable[position[pos]]);
                        ((ImageView) view).setImageResource(drawable[position[pos]]);
                        countPair++;
                        //display score
                        mScore.setText(countPair + " of 6 matches");
                        mScore.setVisibility(View.VISIBLE);

                        //PROGRESS BAR increases by 17 with every match
                        mProgressStatus += 17;
                        mProgressBar.setProgress(mProgressStatus);
                        mProgressBar.setVisibility(View.VISIBLE);

                        //Toast to let user know it's a match
                        Toast toast1 = Toast.makeText(getApplicationContext(), "Matched!", Toast.LENGTH_SHORT);
                        toast1.setGravity(0, 0, 0);
                        toast1.show();

                        //after showing both matched cards for 0.5second, matched cards disappear
                        ((ImageView) view).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                currentView.setVisibility(View.INVISIBLE);
                                ((ImageView) view).setVisibility(View.INVISIBLE);
                            }
                        }, 1000);

                        if(countPair == 6) {
                            Toast toast2 = Toast.makeText(getApplicationContext(), "YOU WIN!", Toast.LENGTH_LONG);
                            toast2.setGravity(0, 0, 0);
                            toast2.show();

                            //stop timer once player wins
                            timerStarted = false;
                            timer.cancel();
                        }
                    }
                    currentPosition = -1;
                    cardsOpen = 0;
                }
            }
        });

        //TIMER
        timerText = (TextView) findViewById(R.id.Timer);
        pauseBtn = (Button) findViewById(R.id.PauseBtn);

        timer = new Timer();
        startTimer();
    }

    public void pauseTapped(View view) {
        GridView gv = findViewById(R.id.gridView);

        if (timerStarted == false) {
            timerStarted = true;
            pauseBtn.setText("PAUSE");
            pauseBtn.setTextColor(ContextCompat.getColor(this, R.color.black));

            startTimer();

            //remove pause screen
            mPauseScreen.setVisibility(View.INVISIBLE);
            pauseBtn.setVisibility(View.VISIBLE);
        }
        else {
            timerStarted = false;

            timerTask.cancel();

            //display pause screen
            mPauseScreen.setVisibility(View.VISIBLE);
            pauseBtn.setVisibility(View.INVISIBLE);
        }
    }

    private void startTimer() {
        timerStarted = true;
        timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        time++;
                        timerText.setText(getTimerText());
                    }
                });
            }
        };
        //delay 0 = timer will start straightaway; period 1000 = 1000ms = 1s
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }

    private String getTimerText() {
        int rounded = (int) Math.round(time);

        int seconds = ((rounded % 86400) % 3600) % 60;
        int minutes = ((rounded % 86400) % 3600) / 60;
        int hours = ((rounded % 86400) / 3600);

        return formatTime(seconds, minutes, hours);
    }

    private String formatTime(int seconds, int minutes, int hours) {
        return String.format("%02d", hours) + " : " + String.format("%02d", minutes) + " : " + String.format("%02d", seconds);
    }

    private int[] shuffleImages() {
        int noOfElements = position.length;
        for (int i = 0; i < noOfElements; i++) {
            int s = i + (int)(Math.random() * (noOfElements - i));

            int temp = position[s];
            position[s] = position[i];
            position[i] = temp;
        }
        //return shuffled positions
        return position;
    }
}
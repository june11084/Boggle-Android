package com.example.group.boggle;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;


public class GameActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MyActivity";
    private static final int wordLength = 8;
    String[] alphabets ={"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
    String[] vowels = {"a","e","i","o","u"};
    ArrayList<String> word = new ArrayList<>();
    private GridLayout mGrid;
    private Random random;
    CountDownTimer timer;
    @BindView(R.id.newWord) Button mNewWord;
    @BindView(R.id.submitWord) Button mSubmitWord;
    @BindView(R.id.wordInput) EditText mEditText;
    @BindView(R.id.answerLog) TextView mAnswerLog;
    @BindView(R.id.timer) TextView mTimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        ButterKnife.bind(this);
        runGame();
        mNewWord.setOnClickListener(this);
        mSubmitWord.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        timer.cancel();
        startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    public void onClick (View v){
        if(v == mNewWord) {
            timer.cancel();
            newGame();
        } else if (v == mSubmitWord){
            mAnswerLog.setText(null);
            timer.cancel();
            String word = mEditText.getText().toString();
            if(wordCheck(word)){
                mAnswerLog.setText("Correct!");
            }else {
                mAnswerLog.setText("False!");
            }
        }else {
            Log.wtf(TAG, "oh-no!");
        }
    }

    private class TouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            final ClipData data = ClipData.newPlainText("", "");
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
            view.startDrag(data, shadowBuilder, view, 0);
            view.setVisibility(View.INVISIBLE);
            return true;
        }
    }

    private class DragListener implements View.OnDragListener {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            final View view = (View) event.getLocalState();
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_LOCATION:
                    // do nothing if hovering above own position
                    if (view == v) return true;
                    // get the new list index
                    final int index = calculateNewIndex(event.getX(), event.getY());
                    // remove the view from the old position
                    mGrid.removeView(view);
                    // and push to the new
                    mGrid.addView(view, index);
                    break;
                case DragEvent.ACTION_DROP:
                    view.setVisibility(View.VISIBLE);
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    if (!event.getResult()) {
                        view.setVisibility(View.VISIBLE);
                    }
                    break;
            }
            return true;
        }
    }

    private int calculateNewIndex(float x, float y) {
        // calculate which column to move to
        final float cellWidth = mGrid.getWidth() / mGrid.getColumnCount();
        final int column = (int)(x / cellWidth);

        // calculate which row to move to
        final float cellHeight = mGrid.getHeight() / mGrid.getRowCount();
        final int row = (int)Math.floor(y / cellHeight);

        // the items in the GridLayout is organized as a wrapping list
        // and not as an actual grid, so this is how to get the new index
        int index = row * mGrid.getColumnCount() + column;
        if (index >= mGrid.getChildCount()) {
            index = mGrid.getChildCount() - 1;
        }

        return index;
    }

    private void runGame(){
        this.random = new Random(System.currentTimeMillis());
        mGrid = (GridLayout) findViewById(R.id.grid_layout);
        final LayoutInflater inflater = LayoutInflater.from(this);
        word.clear();
        for(int i =0; i <9; i ++){
            if(word.size()<2){
                word.add(vowels[random.nextInt(vowels.length)]);
            } else {
                word.add(alphabets[random.nextInt(alphabets.length)]);
            }
        }
        Collections.shuffle(word);
        for (int i = 0; i < wordLength; i++) {
            final View itemView = inflater.inflate(R.layout.grid_item, mGrid, false);
            final TextView text = (TextView) itemView.findViewById(R.id.text);
            itemView.setOnTouchListener(new TouchListener());
            mGrid.setOnDragListener(new DragListener());
            text.setText(word.get(i));
            mGrid.addView(itemView);
        }
        startTimer();
    }

    public void startTimer(){
        timer = new CountDownTimer(10000, 1000) {

            public void onTick(long millisUntilFinished) {
                mTimer.setText("seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                timer.cancel();
                newGame();
            }
        }.start();
    }

    private void newGame(){
        mGrid.removeAllViewsInLayout();
        mAnswerLog.setText(null);
        mEditText.setText(null);
        word.clear();
        runGame();
    }

    public boolean wordCheck(String wordToCheck) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(getAssets().open("localDictionary/usDictionary.txt")));
            Toast.makeText(GameActivity.this,"Checking",Toast.LENGTH_LONG).show();
            String str;
            int invalidWord = 0;
            String joined = TextUtils.join("", word);
            for(int i = 0; i < wordToCheck.length(); i++){
                char character = wordToCheck.charAt(i);
                if(joined.indexOf(character) != -1){
                } else {
                    invalidWord =+1;
                }
            }
            while ((str = in.readLine()) != null) {
                    if (invalidWord<=0 && wordToCheck.length() >= 3 && str.equals(wordToCheck)) {
                        Toast.makeText(GameActivity.this,str,Toast.LENGTH_LONG).show();
                        return true;
                }
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}

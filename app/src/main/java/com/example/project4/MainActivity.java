package com.example.project4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    PlayerThread threadX;
    AIPlayer threadO;
    String state = "b b b b b b b b b";
    ArrayList<ImageView> imageViews;
    Character lastPlayer = 'X';
    boolean isInterrupted = false;
    TextView text;
    ProgressBar pBar;

    private final Handler mHandler = new Handler(Looper.getMainLooper()){
        String str = "";
        @Override
        public void handleMessage(@NonNull Message msg) {
            int what = msg.what;
            // get the move from the message
            state = msg.getData().getString("state");
            switch(what){
                case 1:
                    if(!isInterrupted) {
                        // display the move
                        pBar.setVisibility(View.INVISIBLE);
                        text.setVisibility(View.INVISIBLE);
                        int x = 0;
                        for(int i = 0; i < imageViews.size(); i++){
                            if(state.charAt(x) == 'X')
                                imageViews.get(i).setImageResource(R.drawable.x);
                            else if(state.charAt(x) == 'O')
                                imageViews.get(i).setImageResource(R.drawable.o);
                            else
                                imageViews.get(i).setImageResource(R.drawable.blank);
                            x += 2;
                        }

                        if(evaluateWinning(state, 'X')){
                            text.setText("player X won!");
                            text.setVisibility(View.VISIBLE);
                            break;                        }
                        else if(evaluateWinning(state, 'O')){
                            text.setText("player O won!");
                            text.setVisibility(View.VISIBLE);
                            break;
                        }

                        // notify the player thread to get the next move
                        if(state.contains("b")) {
                            if (lastPlayer == 'X') {
                                sendGameState(threadO.getHandler(), 1, state);
                                lastPlayer = 'O';
                            } else {
                                sendGameState(threadX.getHandler(), 1, state);
                                lastPlayer = 'X';
                            }
                        }
                        else{
                            text.setText("tied!");
                            text.setVisibility(View.VISIBLE);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // get the IDs of imageViews
        ArrayList<Integer> ids = new ArrayList<>(Arrays.asList(R.id.imageView1, R.id.imageView2, R.id.imageView3,
                R.id.imageView4, R.id.imageView5, R.id.imageView6, R.id.imageView7,
                R.id.imageView8, R.id.imageView9));
        imageViews = new ArrayList<>();
        for(int i = 0; i < ids.size(); i++){
            imageViews.add(findViewById(ids.get(i)));
        }

        pBar = findViewById(R.id.pBar);
        text = findViewById(R.id.textView);
        // button to start the threads
        Button b = findViewById(R.id.button);
        b.setOnClickListener(e-> startGame());
    }

    // start the game
    private void startGame(){
        clearGameBoard();
        isInterrupted = true;
        text.setVisibility(View.INVISIBLE);
        stopThreads();
        try {
            startThreads();
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
        state = "b b b b b b b b b";
        lastPlayer = 'X';

        // sleep the UI thread if the handlers of the worker threads are null
        while(threadX.handler == null || threadO.getHandler() == null){
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        sendGameState(threadX.handler,1, state);
        isInterrupted = false;
    }

    // clear game board
    private void clearGameBoard(){
        for(int i = 0; i < imageViews.size(); i++){
            imageViews.get(i).setImageResource(R.drawable.blank);
        }
    }

    // start the worker threads
    private void startThreads() throws InterruptedException {
        threadX = new PlayerThread("playerX", 1000);
        threadX.start();
        threadO = new AIPlayer("playerO", mHandler, 1000);
        threadO.start();
        pBar.setVisibility(View.VISIBLE);
        text.setText("starting game...");
        text.setVisibility(View.VISIBLE);
    }

    // stop the worker threads
    private void stopThreads(){
        // close the threads
        if(threadX != null && threadX.isAlive()){
            Message m = threadX.handler.obtainMessage(2);
            threadX.handler.sendMessageAtFrontOfQueue(m);
        }
        if(threadO != null && threadO.isAlive()){
            Message m = threadO.getHandler().obtainMessage(2);
            threadO.getHandler().sendMessageAtFrontOfQueue(m);
        }
    }

    // send the current game state to the worker thread to figure out
    // the next move
    private void sendGameState(Handler handler, int what, String state){
        Message m = handler.obtainMessage(1);
        Bundle bundle = new Bundle();
        bundle.putString("state", state);
        m.setData(bundle);
        handler.sendMessage(m);
    }

    // when the activity is about to be destroyed, stop the worker threads
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopThreads();
    }

    // Worker thread to execute the code for calculating the moves
    /*
     *  The AI uses the Minimax algorithm to calculate the moves
     */
    class PlayerThread extends HandlerThread {
        Handler handler;
        String playerName;
        String levelX, levelO;
        long delayTime;
        public PlayerThread(String name, long delayTime) {
            super(name);
            playerName = name;
            this.delayTime = delayTime;
            levelX = getRandomLevel();
            levelO = getRandomLevel();
        }

        @Override
        protected void onLooperPrepared() {
            handler = new Handler(getLooper()){
                @Override
                public void handleMessage(@NonNull Message msg) {
                    int what = msg.what;

                    switch(what){
                        case 1:
                            String state = msg.getData().getString("state");
                            if(playerName == "playerX"){
                                state = getMove(state, "Expert");
                            }
                            else{
                                state = switchBoard(state);
                                state = getMove(state, levelO);
                                state = switchBoard(state);
                            }
                            sendMove(state);

                            break;
                        default:
                            mHandler.removeMessages(1);
                            quit();
                            break;
                    }
                }
            };
        }

        public Handler getHandler(){
            return handler;
        }

        // get random level of the player
        // either Expert, Advanced or Novice
        private String getRandomLevel(){
            String level;
            double random = Math.random();
            if(random <= 0.3){
                level = "Expert";
            }
            else if(random > 0.3 && random <= 0.7){
                level = "Advanced";
            }
            else{
                level = "Novice";
            }

            return level;
        }

        // get the next move based on state and level of player
        private String getMove(String state, String level){
            AI_MinMax ai = new AI_MinMax(state, level);
            return ai.getNextState(level);
        }

        // send move to UI thread
        private void sendMove(String state){
            Message m = mHandler.obtainMessage(1);
            Bundle b = new Bundle();
            b.putString("state", state);
            m.setData(b);
            mHandler.sendMessageDelayed(m, delayTime);
        }

        /*
         * Switch the state of the game board
         * , replace X with O, and O with X
         * Ex: state = b b X b b b O b b
         * 	switched state = b b O b b b X b b
         */
        public String switchBoard(String s) {
            char str[] = s.toCharArray();

            for(int i = 0; i < str.length; i++) {
                if(str[i] == 'X')
                    str[i] = 'O';
                else if(str[i] == 'O')
                    str[i] = 'X';
            }

            s = String.valueOf(str);
            return s;
        }
    }

    // evaluate if 'X' or 'O' wins, 'X'/ 'O' is passed
    // to ch parameter
    public static boolean evaluateWinning(String s, char ch) {

        char[] str = s.replaceAll("\\s", "").toCharArray();
        if((str[0] == ch && str[1] == ch && str[2] == ch) // 1st row
                || (str[3] == ch && str[4] == ch && str[5] == ch) // 2nd row
                || (str[6] == ch && str[7] == ch && str[8] == ch) // 3rd row
                || (str[0] == ch && str[3] == ch && str[6] == ch) // 1st column
                || (str[1] == ch && str[4] == ch && str[7] == ch) // 2nd column
                || (str[2] == ch && str[5] == ch && str[8] == ch) // 3rd column
                || (str[0] == ch && str[4] == ch && str[8] == ch) // diagonal
                || (str[2] == ch && str[4] == ch && str[6] == ch)) // diagonal
            return true;
        return false;
    }
}
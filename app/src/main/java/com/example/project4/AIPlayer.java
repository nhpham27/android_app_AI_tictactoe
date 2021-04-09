package com.example.project4;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;

/*
 * The AIPlayer class figure out the moves by checking
 * all the possible moves that leads the player to winning the game.
 * If there is no such move, checking all the possible moves that
 * lead to the winning of the opponent, and stop such move. Otherwise,
 * generate a random move.
 */
public class AIPlayer extends HandlerThread {
    private Handler handler;
    Handler UIHandler;
    private long delayTime;
    public AIPlayer(String name, Handler UIHandler, long delayTime) {
        super(name);
        this.UIHandler = UIHandler;
        this.delayTime = delayTime;
    }

    @Override
    protected void onLooperPrepared() {
        handler = new Handler(getLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                int what = msg.what;

                switch(what){
                    case 1:
                        // set the state string from the message
                        String state = msg.getData().getString("state");
                        // generate the next move
                        state = getMove(state, 'O', 'X');
                        // send the move to UI thread
                        sendMove(state);
                        break;
                    default:
                        // stop executing
                        UIHandler.removeMessages(1);
                        quit();
                        break;
                }
            }
        };
    }

    // get the handler
    public Handler getHandler(){
        return handler;
    }

    private String getRandomMove(String state, Character player){
        StringBuilder sb = new StringBuilder(state);
        ArrayList<Integer> indices = new ArrayList<>();

        // get all indices of 'b' in state string
        for(int i = 0; i < sb.length(); i++){
            if(sb.charAt(i) == 'b'){
                indices.add(i);
            }
        }

        // shuffle the indices array, and change the character
        // at the first index in the indices array to 'O'
        if(indices.size() > 0){
            Collections.shuffle(indices);
            sb.setCharAt(indices.get(0), player);
        }

        return sb.toString();
    }

    //
    private String getMove(String state, Character player, Character opponent){
        // find move so that player O can win
        for(int i = 0; i < state.length(); i++){
            if(state.charAt(i) == 'b'){
                StringBuilder sb = new StringBuilder(state);
                sb.setCharAt(i, player);
                if(MainActivity.evaluateWinning(sb.toString(), player)){
                    return sb.toString();
                }
            }
        }

        // stop player X from winning the game
        for(int i = 0; i < state.length(); i++){
            if(state.charAt(i) == 'b'){
                StringBuilder sb = new StringBuilder(state);
                sb.setCharAt(i, opponent);
                if(MainActivity.evaluateWinning(sb.toString(), opponent)){
                    sb.setCharAt(i, player);
                    return sb.toString();
                }
            }
        }

        return getRandomMove(state, player);
    }

    // send move to UI thread
    private void sendMove(String state){
        Message m = UIHandler.obtainMessage(1);
        Bundle b = new Bundle();
        b.putString("state", state);
        m.setData(b);
        UIHandler.sendMessageDelayed(m, delayTime);
    }
}

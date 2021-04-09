package com.example.project4;

import java.util.concurrent.Callable;

public class NextState implements Callable<String> {
    AI_MinMax ai;
    String state;
    String level;
    int delay;
    public NextState(String state, String level) {
        // TODO Auto-generated constructor stub
        ai = new AI_MinMax(state, level);
        this.level = level;
    }

    @Override
    public String call(){
        return ai.getNextState(this.level);
    }

}
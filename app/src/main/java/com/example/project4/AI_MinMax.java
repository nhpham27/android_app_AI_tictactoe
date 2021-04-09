package com.example.project4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

/**
 * This class is used to read in a state of a tic tac toe board. It creates a MinMax object and passes the state to it. What returns is a list
 * of possible moves for the player X that have been given min/max values by the method findMoves. The moves that can result in a win or a
 * tie for X are printed out with the method printBestMoves()
 *
 * @author Mark Hallenbeck
 *
 * Copyright© 2014, Mark Hallenbeck, All Rights Reservered.
 *
 * Modifications by Nguyen Hoa Pham:
 * 	Adding getNextState() method to get the next moves based on the
 * level of the AI player. The AI player will choose the moves based on the following orders:
 * -> Expert: win > tie > lose
 * -> Advanced: tied > win > lose
 * -> Novice: lose > tied > win
 */
public class AI_MinMax {

    private String[] init_board;

    private ArrayList<Node> movesList;

    private String nextState;

    AI_MinMax(String s, String level){
        this.nextState = "";
        String delim = "[ ]+";

        init_board = s.split(delim);
        MinMax sendIn_InitState = new MinMax(init_board);
        movesList = sendIn_InitState.findMoves();
    }

    AI_MinMax()
    {
        init_board = getBoard();

        if(init_board.length != 9)
        {
            System.out.println("You have entered an invalid state for tic tac toe, exiting......");
            System.exit(-1);
        }

        MinMax sendIn_InitState = new MinMax(init_board);

        movesList = sendIn_InitState.findMoves();

        printBestMoves();
    }

    /**
     * reads in a string from user and parses the individual letters into a string array
     * @return String[]
     */
    private String[] getBoard()
    {
        String puzzle;
        String[] puzzleParsed;
        String delim = "[ ]+";

        //give input message
        System.out.println("Enter a string to represent the board state:");

        Scanner userInput = new Scanner(System.in);		//open scanner

        puzzle = userInput.nextLine();					//scan in string

        puzzleParsed = puzzle.split(delim);
        userInput.close();   	  						//close scanner
        return puzzleParsed;

    }

    /**
     * goes through a node list and prints out the moves with the best result for player X
     * checks the min/max function of each state and only recomends a path that leads to a win or tie
     */
    private void printBestMoves()
    {
        System.out.print("\n\nThe moves list is: < ");

        for(int x = 0; x < movesList.size(); x++)
        {
            Node temp = movesList.get(x);

            if(temp.getMinMax() == 10 || temp.getMinMax() == 0)
            {
                System.out.print(temp.getMovedTo() + " ");
            }
        }

        System.out.print(">");
    }

    public String getNextState(String level) {
        switch(level) {
            case "Expert":
                if(this.getNextMove(10))
                    break;
                if(this.getNextMove(0))
                    break;
                this.getNextMove(-10);
                break;
            case "Advanced":
                if(this.getNextMove(0))
                    break;
                if(this.getNextMove(-10))
                    break;
                this.getNextMove(10);
                break;
            case "Novice":
                if(this.getNextMove(-10))
                    break;
                if(this.getNextMove(0))
                    break;
                this.getNextMove(10);
                break;
            default:
                break;
        }

        return nextState;
    }

    private boolean getNextMove(int num) {
        String str = "";
        ArrayList<String> moves = new ArrayList<>();
        boolean isFound = false;
        for(int x = 0; x < movesList.size(); x++)
        {
            Node temp = movesList.get(x);
            str = "";

            if(temp.getMinMax() == num)
            {
                String[] tempString = temp.getInitStateString();
                for(int y = 0; y < tempString.length; y++)		//print out the string array for that node
                {
                    str += tempString[y] + " ";
                }

                isFound = true;

                moves.add(str);
            }
        }
        if(!moves.isEmpty()) {
            Collections.shuffle(moves);
            this.nextState = moves.get(0);
        }

        return isFound;
    }

    //public static void main(String[] args) {
    //	// TODO Auto-generated method stub
    //	AI_MinMax startThis = new AI_MinMax();
    //}

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package snake.game;

import NeuralNet.NeuralNetwork;
import snake.brain.SnakeBrain;


/**
 *
 * @author drewmcnutt
 */
public class Snake {
    private SnakeBrain brain;
    
    NeuralNetwork nn;
    
    private int length;
    
    public enum Direxion{
        UP, DOWN, LEFT, RIGHT
    }
    
    private Direxion dir;
    
    private final int[] xloc = new int[Board.getTotalPixels()];
    
    private final int[] yloc = new int[Board.getTotalPixels()];
    
    int steps = 0;
    
    public Snake(int[] neuralSetup){
        nn = new NeuralNetwork(NeuralNetwork.ErrorFunction.LEAST_MEAN_SQUARES, NeuralNetwork.ActivationFunction.LOGISTIC_SIGMOID, neuralSetup);
    }
    
    
    public void setBrain(SnakeBrain b){
        brain = b;
        for(int i = 0; i < genetic.GeneticAlgorithm.getArchitecture().length - 1; i++){
            nn.setWeight(i + 1, brain.getWeight(i));
        }
        steps = 0;
    }
   
    public int getXPos(int i){
        return xloc[i];
    }
    
    public int getYPos(int i){
        return yloc[i];
    }
    
    public void setJoints(int njoints){
        length = njoints;
    }
    
    public int getJoints(){
        return length;
    }
    
    public void setSnakeX(int x){
        xloc[0] = x;
    }
    
    public void setSnakeY(int y){
        yloc[0] = y;
    }
    
    public void setDirection(Direxion newdir){
        dir = newdir;
    }
    
    public Direxion getDirection(){
        return dir;
    }
    
    public void addJoint(){
        this.length++;
    }
    
    public void move(double[] input){
        dir = brain.makeMove(input, nn);
        for(int i = this.length; i > 0; i--){
            xloc[i] = xloc[i - 1];
            yloc[i] = yloc[i - 1];
        }
        int x = 0;
        int y = 0;
        switch(dir){
            case UP:
                y = -1;
                break;
            case DOWN:
                y = 1;
                break;
            case LEFT:
                x = -1;
                break;
            case RIGHT:
                x = 1;
                break;
        }
        xloc[0] = xloc[0] + x * Board.getPixelSize();
        yloc[0] = yloc[0] + y * Board.getPixelSize();
        steps++;
    }
    
    public int getSteps(){
        return steps;
    }
    
    public void setFitness(double fit){
        brain.setFitness(fit);
    }
}

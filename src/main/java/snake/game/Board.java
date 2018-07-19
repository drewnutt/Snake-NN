/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package snake.game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import javax.swing.JPanel;
import javax.swing.Timer;
import snake.brain.SnakeBrain;
import snake.listeners.GameOverListener;

/**
 *
 * @author drewmcnutt
 */
public class Board extends JPanel implements ActionListener, Callable<Double>{

    public enum GameState{
        RUNNING, PAUSED,GAME_OVER
    }
    
    private ArrayList<GameOverListener> listeners = new ArrayList<>();
    
    private final static int MAXHEIGHT = 750;
    
    private final static int MAXWIDTH = 750;
    
    private final static int PIXELWIDTH = 25;
    
    private final static int NUMBERPIXELS = MAXHEIGHT * MAXWIDTH / (PIXELWIDTH * PIXELWIDTH);
    
    private GameState gs = GameState.GAME_OVER;
    
    private Timer tick;
    
    private int score;
    
    private int stepsFood;
    
    private double fit;
    
    private double fitness;
    
    private final static int speed = 35;
    
    private double distToFood;
    
    private Snake snake;
    
    private Food food;
    
    private boolean snakeStalled = false;
    int[] neuralSetup;
    /**
     * Creates new form Board
     * @param neuralSetup
     */
    public Board(int[] neuralSetup) {
        this.neuralSetup = neuralSetup;
        setBackground(Color.BLACK);
        setFocusable(true);
        
        setPreferredSize(new Dimension(MAXWIDTH, MAXHEIGHT));
    }
    
    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        draw(g);
    }
    
    void draw(Graphics g){
        switch(gs){
            case RUNNING:
                g.setColor(Color.RED);

                g.fillRect(food.getFoodX(), food.getFoodY(), PIXELWIDTH, PIXELWIDTH);

                g.setColor(Color.BLUE);
                for(int i = 0; i < snake.getJoints(); i++){
                    g.fillRect(snake.getXPos(i), snake.getYPos(i), PIXELWIDTH, PIXELWIDTH);
                }
                Font f = new Font("Times New Roman", Font.BOLD, 14);
                String message = "Score: " + String.valueOf(score);
                g.setFont(f);
                g.drawString(message, 20, 20);
                
                Toolkit.getDefaultToolkit().sync();
                break;
            case GAME_OVER:
                endGame(g);
                break;
            case PAUSED:
                pauseGame(g);
                break;
            
        }
    }
    
    public void initializeGame(SnakeBrain newBrain){
        food = new Food();
        snake = new Snake(neuralSetup);
        gs = GameState.RUNNING;
        
        snake.setJoints(3);
        
        snake.setBrain(newBrain);
        
        for(int i = 0; i < snake.getJoints(); i++){
            snake.setSnakeX(MAXWIDTH/2);
            snake.setSnakeY(MAXHEIGHT/2);
        }
        
        score = 0;
        fitness = 0;
        distToFood = getDistance(snake.getXPos(0),snake.getYPos(0), food.getFoodX(), food.getFoodY());
        tick = new Timer(speed, this);
        tick.start();
        stepsFood = 0;
    }
    
    @Override
    public Double call(){
        while(tick.isRunning()){
            Thread.yield();
        }
        
        return fit;
    }
    
    void endGame(Graphics g){
        String message = "Game Over";
        
        Font f = new Font("Helvetica", Font.BOLD, 20);
        FontMetrics metrics = getFontMetrics(f);
        
        g.setColor(Color.WHITE);
        g.setFont(f);
        
        g.drawString(message, (MAXWIDTH - metrics.stringWidth(message))/2, MAXHEIGHT/2);  
        
    }
    
    void pauseGame(Graphics g){
        String message = "PAUSED";
        
        Font f = new Font("Times New Roman", Font.PLAIN, 20);
        FontMetrics metrics = getFontMetrics(f);
        
        g.setColor(Color.WHITE);
        g.setFont(f);
        
        g.drawString(message, (MAXWIDTH - metrics.stringWidth(message))/2, MAXHEIGHT/2);
    }
    
    @Override
    public void actionPerformed(ActionEvent ae){
        if(gs == GameState.RUNNING){
            checkFoodCollisions();
            checkCollisions();
            
            snake.move(getNNInputs());
//            int maxSteps =  (score + 1 ) * 50;
//            int steps = snake.getSteps() - stepsFood;
//            if(steps >= maxSteps){
//                gs = GameState.GAME_OVER;
//                snakeStalled = true;
//                tick.stop();
//            }
            double newFoodDist = getDistance(snake.getXPos(0),snake.getYPos(0), food.getFoodX(), food.getFoodY());
            if(newFoodDist < distToFood)
                fitness += 1;
            else
               fitness -= 1.5;
            distToFood = newFoodDist;
                
        }
        
        repaint();
    }
    
    void checkFoodCollisions(){
        boolean xdist = getDistance(snake.getXPos(0), food.getFoodX(), 20);
        boolean ydist = getDistance(snake.getYPos(0), food.getFoodY(), 20);
        
        if(xdist && ydist){
            snake.addJoint();
            food.createFood();
            score++;
            stepsFood = snake.getSteps();
        }
    }
    
    void checkCollisions(){
        /*
        Checking snake collision with self
        */
        for(int i = snake.getJoints(); i > 0; i--){
            if((i > 5) && snake.getXPos(0) == snake.getXPos(i) && snake.getYPos(0) == snake.getYPos(i)){
                gs = GameState.GAME_OVER;
            }
        }
        
        
        /*
        If it hits any of the wall
        */
        if(snake.getXPos(0) <= 0)
            gs = GameState.GAME_OVER;
        if(snake.getXPos(0) >= MAXWIDTH)
            gs = GameState.GAME_OVER;
        if(snake.getYPos(0) <= 0)
            gs = GameState.GAME_OVER;
        if(snake.getYPos(0) >= MAXHEIGHT)
            gs = GameState.GAME_OVER;
        
        if(gs == GameState.GAME_OVER){
            calculateFitness();
            tick.stop();
            for (GameOverListener gol : listeners)
                gol.gameisOver();
        }
            
    }
    
    private void calculateFitness(){
        if(snakeStalled)
            fit = score * 1000 + fitness + 10 * snake.getTurnCount();
        else
            fit = score * 1000 + fitness + 1.5 * snake.getTurnCount();
    }
    
    public double[] getNNInputs(){
        double[] inputs = new double[5];
        for(int i = 0; i < 4; i++){
            inputs[i] = getDistance(i);
        }
        inputs[4] = getAngle(new int[]{snake.getXPos(0), snake.getYPos(0)}, new int[]{food.getFoodX(), food.getFoodY()}, snake.getDirection());
        
//        for(double i: inputs)
//            System.out.printf("%f, ", i);
//        System.out.printf("%n");
        return inputs;
    }
    
    
    public double getFitness(){
        return fit;
    }
    
    boolean getDistance(int p1, int p2, int max){
        return (p1 - p2) * (p1 - p2) <= max * max;
    }
    
    double getDistance(double p1, double p2, double q1, double q2){
        return (p1 - q1) * (p1 - q1) + (p2 - q2) * (p2 - q2);
    }
    
    double getAngle(int[] p1, int[] p2, Snake.Direxion dir){
        if(p1.length != p2.length && p1.length != 2){
            System.out.println("Problem with getAngle in Board");
            return 0;
        }
        double angle;
        int[] vert = {p2[0], p1[1]};

        double opp = Math.sqrt(getDistance(vert[0],vert[1],p2[0],p2[1]));
        double adj = Math.sqrt(getDistance(vert[0],vert[1],p1[0],p1[1]));
        angle = Math.atan(opp / adj) / Math.PI;
        
        switch(dir){
            case UP:
                angle *= p1[0] - p2[0] > 0? -1 : 1;
                angle += p1[1] - p2[1] < 0? (angle < 0? -0.5 : 0.5 ): 0;
                angle = p1[0] == p2[0]? 0 : angle;
                break;
            case DOWN:
                angle *= p1[0] - p2[0] < 0? -1 : 1;
                angle += p1[1] - p2[1] > 0? (angle < 0? -0.5 : 0.5 ): 0;
                angle = p1[0] == p2[0]? 0 : angle;
                break;
            case LEFT:
                angle *= p1[1] - p2[1] < 0? -1 : 1;
                angle += p1[0] - p2[0] < 0? (angle < 0? -0.5 : 0.5 ): 0;
                angle = p1[1] == p2[1]? 0 : angle;
                break;
            case RIGHT:
                angle *= p1[1] - p2[1] > 0? -1 : 1;
                angle += p1[0] - p2[0] > 0? (angle < 0? -0.5 : 0.5 ) : 0;
                angle = p1[1] == p2[1]? 0 : angle;
                break;
        }
        return angle;
    }
    
    double getDistance(int direction){
        switch(direction){
                case 0:     //UP
                    return (snake.getYPos(0) <= Board.getPixelSize() ? 1 : 0);
                case 1:     //DOWN
                    return (Board.MAXHEIGHT - snake.getYPos(0) <= Board.getPixelSize() ? 1 : 0);
                case 2:     //LEFT
                    return (snake.getXPos(0) <= Board.getPixelSize() ? 1 : 0);
                case 3:     //RIGHT
                    return (Board.MAXWIDTH - snake.getXPos(0) <= Board.getPixelSize() ? 1 : 0);
                    
            }  
        
        return 0;
    }
    
    public boolean isGameOver(){
        return gs == GameState.GAME_OVER;
    }
    
    
    public static int getTotalPixels(){
        return NUMBERPIXELS;
    }
    
    public static int getBoardWidth(){
        return MAXWIDTH;
    }
    
    public static int getBoardHeight(){
        return MAXHEIGHT;
    }
    
    public static int getPixelSize(){
        return PIXELWIDTH;
    }   

    public void addListeners(GameOverListener toAdd){
        listeners.add(toAdd);
    }
}


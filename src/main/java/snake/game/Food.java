/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package snake.game;

import java.util.Random;

/**
 *
 * @author drewmcnutt
 */
public class Food {
    
    
    private int xloc;
    private int yloc;
    
    Food(){
        Random randgen = new Random(System.nanoTime());
        int x = randgen.nextDouble() > 0.5 ? 1 : 0;
        int y = randgen.nextDouble() > 0.5 ? 1 : 0;
        xloc = x * Board.getBoardWidth() + (-x) * Board.getPixelSize() * 5 + (1 - x) * Board.getPixelSize() * 5;
        yloc = y * Board.getBoardHeight() + (-y) * Board.getPixelSize() * 5 + (1 - y) * Board.getPixelSize() * 5;
    }
    
    public void createFood(){
        Random randgen = new Random(System.nanoTime());
        xloc = randgen.nextInt(Board.getBoardWidth() - Board.getPixelSize() / 2);
        yloc = randgen.nextInt(Board.getBoardHeight() - Board.getPixelSize() / 2);
        
    }
    
    
    public int getFoodX(){
        return xloc;
    }
    
    public int getFoodY(){
        return yloc;
    }
}

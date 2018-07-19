/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package snake.brain;

import NeuralNet.NeuralNetwork;
import java.util.Random;
import snake.game.Snake.Direxion;

/**
 *
 * @author drewmcnutt
 */
public class SnakeBrain {
    final int[] ARCH = genetic.GeneticAlgorithm.getArchitecture();
    double[][] weights;
    int[][] dimensions;
    double fitness;
    
    
    public SnakeBrain(int[] setup){
        weights = new double[ARCH.length - 1][];
        dimensions = new int[ARCH.length - 1][2];
        int numLayers = setup.length;
        for (int l = 1; l < numLayers; l++) {
            double r = 5;
            double[] temp = new double[setup[l] * (setup[l - 1] + 1)];
            dimensions[l - 1][0] = setup[l];
            dimensions[l - 1][1] = setup[l - 1] + 1;
            for (int i = 0; i < setup[l]; i++) {
                for (int j = 0; j < setup[l - 1] + 1; j++) {
                    temp[i * setup[l - 1] + j] = smile.math.Math.random(-5, 5);
                }
            }
            weights[l - 1] = temp;
        }
    }
 
    public snake.game.Snake.Direxion makeMove(double[] input, NeuralNetwork nn){
        int direction = nn.predict(input);
        switch(direction){
            case 0:
                return Direxion.UP;
            case 1:
                return Direxion.LEFT;
            case 2:
                return Direxion.DOWN;
            case 3:
                return Direxion.RIGHT;
            default:
                System.out.printf("Output not encoded, direction is %d%n", direction);
                return Direxion.DOWN;
        }
        
    }
    
    public double[][] getWeight(int layer){
        double[][] outweight = new double[dimensions[layer][0]][dimensions[layer][1]];
        for(int i = 0; i < dimensions[layer][0]; i++){
            for(int j = 0; j < dimensions[layer][1]; j++){
                outweight[i][j] = weights[layer][i * dimensions[layer][1] + j];
            }
        }
        
        return outweight;
    }
    
    private void setWeight(int layer, double[][] weight){
        dimensions[layer][0] = weight.length;
        dimensions[layer][1] = weight[0].length;
        double[] weighttemp = new double[dimensions[layer][0] * dimensions[layer][1]];
        for(int i = 0; i < dimensions[layer][0]; i++){
            for(int j = 0; j < dimensions[layer][1]; j++){
                weighttemp[i * dimensions[layer][1] + j] = weight[i][j];
            }
        }
        
        weights[layer] = weighttemp;
    }
    
    public void setFitness(double fit){
        fitness = fit;
    }
    
    public double getFitness(){
        return fitness;
    }
    
    public SnakeBrain mate(SnakeBrain parent2){
        SnakeBrain baby = new SnakeBrain(ARCH);
        Random rd = new Random(System.nanoTime());
        for(int i = 0; i < ARCH.length - 1; i++){
            int firstDim = this.dimensions[i][0];
            int secondDim = this.dimensions[i][1];
            double[][] temp = new double[this.dimensions[i][0]][this.dimensions[i][1]];
            for(int j = 0; j < firstDim; j++){
                for(int k = 0; k < secondDim; k++){
                    if(rd.nextDouble() >= 0.5)
                        temp[j][k] = this.getWeight(i)[j][k];
                    else
                        temp[j][k] = parent2.getWeight(i)[j][k];
                }
            }
            
        }
        
        return baby;
    }
    
    public void mutate(double mutRate){
        Random rd = new Random(System.nanoTime());
        for(int i = 0; i < ARCH.length - 1; i++){
            if(rd.nextDouble() < mutRate){
                double[][] weight = getWeight(i);
                int row = rd.nextInt(weight.length);
                int column = rd.nextInt(weight[row].length);
                double r = 5;
                weight[row][column] = rd.nextDouble() * (2 * r) - r;
                setWeight(i, weight);
            }
        }
    }
    

}

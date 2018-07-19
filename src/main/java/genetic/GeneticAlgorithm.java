/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package genetic;

import snake.brain.SnakeBrain;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JFrame;
import snake.game.Board;
import snake.listeners.GameOverListener;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author drewmcnutt
 */
public class GeneticAlgorithm extends JFrame implements GameOverListener{
    static final int[] ARCHITECTURE = {5,5,4};
    double MR;
    int popSize;
    int champs;
    SnakeBrain[] population;
    int generation;
    Board snakeBoard;
    boolean readyForNext = true;
    boolean continueBreeding = true;
    JLabel gen;
    JLabel avg;
    JLabel bestFit;
    
    ExecutorService executor = Executors.newFixedThreadPool(1);
    
    ArrayList<Integer> breedingPool = new ArrayList<>();
    
    GeneticAlgorithm(double mutRate, int population, int numChampions){
        MR = mutRate;
        popSize = population;
        champs = numChampions;
        generation = 0;
        JFrame info = new JFrame();
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(3,1));
        gen = new JLabel("Generation: " + generation);
        infoPanel.add(gen);
        bestFit = new JLabel("Best Fitness: " + 0);
        infoPanel.add(bestFit);
        avg = new JLabel("Avg Fitness: " + 0);
        JButton stop = new JButton("Stop Genetic Algorithm");
        stop.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae){
                continueBreeding = false;
            }
        });
        infoPanel.add(stop);
        info.add(infoPanel);
        info.setVisible(true);
        info.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        info.pack();
        snakeBoard = new Board(ARCHITECTURE);
        snakeBoard.addListeners(this);
        this.add(snakeBoard);
        setResizable(false);
        pack();   
        revalidate();
        repaint();
        
        
        this.setTitle("SnakeNN");
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
    }
    
    public static void main(String[] args){
        GeneticAlgorithm ga = new GeneticAlgorithm(0.001, 50, 2);
        ga.setVisible(true);
        
        ga.initializePop();
        System.out.println("Initialized");
        int[] best = ga.testFitness();
        System.out.println("Fitness Tested");
        
        while(ga.continueBreeding){
            if(!ga.makeBreedingPool(best[0]))
                break;
            System.out.println("Breeding Pool made");
            
            ga.breed(best);
            System.out.println("New Population Bred");
            ga.addGeneration();
            ga.gen.setText("Generation: " + ga.getGeneration());
            best = ga.testFitness();
            System.out.println("Fitness Tested");
            
        }
    }
    
    void addGeneration(){
        generation++;
    }
    
    int getGeneration(){
        return generation;
    }
    void initializePop(){
        population = new SnakeBrain[popSize];
        for(int i = 0; i < popSize; i++){
            SnakeBrain sb = new SnakeBrain(ARCHITECTURE);
            population[i] = sb;
        }
    }
    
    int[] testFitness(){
        double max[] = new double[this.champs];
        int topSnakes[] = new int[this.champs];
        for(int i = 0; i < population.length; i++){
            readyForNext = false;
            snakeBoard.initializeGame(population[i]);
            FutureTask runGame = new FutureTask(snakeBoard);
            Thread t = new Thread(runGame);
            t.start();
            double fitness = 0;
            try{
                fitness = (double)runGame.get();
            }catch(InterruptedException | ExecutionException e){
                e.printStackTrace();
            }
            for(int j = 0; j < this.champs; j++){
                if(fitness >= max[j]){
                    for(int k = this.champs - 2; k > j; k--){
                        max[k] = max[k + 1];
                        topSnakes[k - 1] = topSnakes[k];
                    }  
                    max[j] = fitness;
                    topSnakes[j] = i;
                    break;
                }    
            }
            population[i].setFitness(fitness + 1);
        }
        bestFit.setText("Best Fitness: " +  max[0]);
        for(double m: max)
            System.out.println(m);
        return topSnakes;
    }
    
    boolean makeBreedingPool(int max){
        breedingPool.clear();
        double maxFitness = population[max].getFitness();
        for (int i = 0; i < population.length; i++) {
      
            double actualFit = population[i].getFitness() > 0 && population[i].getFitness() / maxFitness > 0.5? population[i].getFitness() : 0;
            double fitness = mapValue(actualFit,0,maxFitness,0,1);
                
            int n = (int)(fitness * 100);
            for (int j = 0; j < n; j++) {              // and pick two random numbers
                    breedingPool.add(i);
            }
        }
        
        return true;
    }
    
    void breed(int[] champs){
        SnakeBrain[] newGen = new SnakeBrain[population.length];
        for(int i = 0; i < this.champs; i++){
            newGen[i] = population[champs[i]];
        }
        
        Random rd = new Random(System.nanoTime());
        for(int i = this.champs; i < population.length; i++){
            int a = rd.nextInt(breedingPool.size());
            int b = rd.nextInt(breedingPool.size());
            SnakeBrain daddy = population[breedingPool.get(a)];
            SnakeBrain mommy = population[breedingPool.get(b)];
            SnakeBrain baby = (daddy.mate(mommy));
            baby.mutate(MR);
            newGen[i] = baby;
        }
        
        population = newGen;
    }
    
    double mapValue(double initial, double initMin, double initMax, double finMin, double finMax){
        return (float)finMin + ((float)(finMax - finMin) / (float)(initMax - initMin)) * (float)(initial - initMin);
    }
    
    @Override
    public void gameisOver(){
        readyForNext = true;
    }
    
    public static int[] getArchitecture(){
        return ARCHITECTURE;
    }
}

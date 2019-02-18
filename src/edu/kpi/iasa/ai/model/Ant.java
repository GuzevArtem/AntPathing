package edu.kpi.iasa.ai.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Ant {
    private List<Integer> visitedCities = new ArrayList<>();

    private double lengthKoef;                  //influence of length on fitness function
    private double pheromoneAttractionKoef;     //influence on pheromone on fitness function
    private double pheromoneLeftKoef;//todo: not used now {@see getPheromoneLeftKoef()}

    private int seed; //todo? could be used as unique seed for random function

    public Ant(double lengthKoef, double pheromoneAttractionKoef, double pheromoneLeftKoef) {
        this.lengthKoef = lengthKoef;
        this.pheromoneAttractionKoef = pheromoneAttractionKoef;
        this.pheromoneLeftKoef = pheromoneLeftKoef;
    }

    public void addVisitedCity(Integer cityId){
        visitedCities.add(cityId);
    }

    public void clearVisitedCities(){
        visitedCities.clear();
    }

    public List<Integer> getVisitedCities() {
        return visitedCities;
    }

    public double getLengthKoef() {
        return lengthKoef;
    }

    public void setLengthKoef(double lengthKoef) {
        this.lengthKoef = lengthKoef;
    }

    public double getPheromoneAttractionKoef() {
        return pheromoneAttractionKoef;
    }

    public void setPheromoneAttractionKoef(double pheromoneAttractionKoef) {
        this.pheromoneAttractionKoef = pheromoneAttractionKoef;
    }

    public double getPheromoneLeftKoef() {
        //todo:return pheromoneLeftKoef;
        return 1.0;
    }

    public void setPheromoneLeftKoef(double pheromoneLeftKoef) {
        this.pheromoneLeftKoef = pheromoneLeftKoef;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ant ant = (Ant) o;
        return Double.compare(ant.lengthKoef, lengthKoef) == 0
                && Double.compare(ant.pheromoneAttractionKoef, pheromoneAttractionKoef) == 0
                //todo: && Double.compare(ant.pheromoneLeftKoef, pheromoneLeftKoef) == 0
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lengthKoef, pheromoneAttractionKoef);//todo:, pheromoneLeftKoef);
    }

    public static Ant generateRandom() {
        return new Ant(
                Math.random(),
                Math.random(),
                Math.random()
        );
    }

    @Override
    public String toString() {
        return "Ant{" +
                "lengthKoef=" + lengthKoef +
                ", pheromoneAttractionKoef=" + pheromoneAttractionKoef +
                ", pheromoneLeftKoef=" + pheromoneLeftKoef +
                '}';
    }
}

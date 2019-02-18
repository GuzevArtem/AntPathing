package edu.kpi.iasa.ai.model;

import edu.kpi.iasa.ai.Utils;
import edu.kpi.iasa.ai.gui.drawable.AntCity;
import edu.kpi.iasa.ai.gui.drawable.AntLine;
import edu.kpi.iasa.ai.gui.drawable.Drawable;

import java.awt.*;

public class Edge implements Drawable {

    public static double minPheromoneLevel = 0.0;
    public static double maxPheromoneLevel = 1.0;

    private AntLine line;
    private double pheromone;

    public Edge(AntCity from, AntCity to) {
        line = new AntLine(from, to);
    }

    public Edge(AntCity from, AntCity to, double pheromone) {
        this.pheromone = Utils.clamp(pheromone,minPheromoneLevel,maxPheromoneLevel);
        line = new AntLine(from, to);
    }

    public Edge(AntLine line) {
        this.line = line;
        this.line.setSaturation(pheromone);
    }

    public Edge(double pheromone, AntLine line) {
        this.pheromone = Utils.clamp(pheromone,minPheromoneLevel,maxPheromoneLevel);
        this.line = line;
        this.line.setSaturation(pheromone);
    }

    @Override
    public void draw(Graphics g) {
        line.draw(g);
    }

    @Override
    public void update(Graphics g) {
        line.update(g);
    }

    public double updatePheromoneLevel(double delta){
        setPheromone(pheromone+delta);
        return pheromone;
    }

    public double getPheromone() {
        return pheromone;
    }

    public void setPheromone(double pheromone) {
        this.pheromone = Utils.clamp(pheromone, minPheromoneLevel, maxPheromoneLevel);
        line.setSaturation(pheromone);
    }

    public void setLine(AntCity from, AntCity to) {
        //todo:both cities should be notnull
        line.setFromCity(from);
        line.setToCity(to);
    }

    public static void setMinMaxPheromoneLevel(double newMinPheromoneLevel, double newMaxPheromoneLevel){
        //prevent mistakes
        minPheromoneLevel = Math.min(newMinPheromoneLevel, newMaxPheromoneLevel);
        maxPheromoneLevel = Math.max(newMinPheromoneLevel, newMaxPheromoneLevel);
    }

    public double getLength() {
        return line.length();
    }
}

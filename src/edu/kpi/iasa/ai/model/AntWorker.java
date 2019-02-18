package edu.kpi.iasa.ai.model;

import edu.kpi.iasa.ai.Utils;
import edu.kpi.iasa.ai.configuration.Attribute;
import edu.kpi.iasa.ai.configuration.Configuration;
import edu.kpi.iasa.ai.gui.AntZone;
import edu.kpi.iasa.ai.gui.drawable.AntCity;
import edu.kpi.iasa.ai.gui.drawable.AntLine;
import javafx.util.Pair;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.BiFunction;

public class AntWorker {
    public static final BiFunction<Ant,Ant,Ant> defaultMergeFunction = (X,Y)->new Ant(
            (X.getLengthKoef()+Y.getLengthKoef())/2,
            (X.getPheromoneAttractionKoef()+Y.getPheromoneAttractionKoef())/2,
            (X.getPheromoneLeftKoef()+Y.getPheromoneLeftKoef())/2
    );

    //early stopper values //todo: not worked well
    private static final int MAX_SIMILLAR_RESULT_ITERATIONS = 5;
    private int simillarResultCounter = 0;

    //todo:recalculated on start
    public double pheromoneDecreasingPerIteration = 0.05;

    //out params //todo: use from configuration directly?
    public double InitialPheromoneLevel = 0.0;
    public int SleepTimePerStep = 0;

    //only for drawing
    //todo: remove dependency
    private AntZone displayZone;

    //out params //todo: use from configuration directly?
    private int ANTS_COUNT;
    private int MAX_ITERATION_COUNT;
    private int citiesCount;

    private PriorityQueue<Ant> ants;
    private AntCity[] cities;

    private Edge[][] graph;

    private BiFunction<Ant,Ant,Ant> mergeFunction = defaultMergeFunction;

    //store current iteration
    private int iteration = 0;

    //state variables //todo: compress to single int|byte?
    private boolean isCitiesGenerated;
    private boolean stopper = false;

    //todo:double saving best path
    private List<Integer> bestPath;
    private Ant best;

    public AntWorker(AntZone displayZone, Configuration configuration) {
        this.displayZone = displayZone;
        //this.configuration = configuration;
        this.citiesCount = configuration.getIntValue(Attribute.CITIES_COUNT);
        this.ANTS_COUNT = configuration.getIntValue(Attribute.ANT_COUNT);
        this.cities = new AntCity[citiesCount];
        this.MAX_ITERATION_COUNT = configuration.getIntValue(Attribute.MAX_ITERATIONS_COUNT);
        ants = new PriorityQueue<>(Comparator.comparingDouble(a -> getPathLength(a.getVisitedCities())));
        isCitiesGenerated = false;
    }

    public AntWorker(AntZone displayZone, int MAX_ITERATION_COUNT, int CITIES_COUNT, int ANTS_COUNT) {
        this.displayZone = displayZone;
        this.cities = new AntCity[CITIES_COUNT];
        this.MAX_ITERATION_COUNT = MAX_ITERATION_COUNT;
        this.citiesCount = CITIES_COUNT;
        this.ANTS_COUNT = ANTS_COUNT;
        isCitiesGenerated = false;
    }

    public AntWorker(AntZone displayZone, AntCity[] cities,int MAX_ITERATION_COUNT) {
        this.displayZone = displayZone;
        this.cities = cities;
        this.MAX_ITERATION_COUNT = MAX_ITERATION_COUNT;
        this.citiesCount = cities.length;
        this.ants = new PriorityQueue<>(Comparator.comparingDouble(a -> -getPathLength(a.getVisitedCities())));
        isCitiesGenerated = true;
    }

    public void start(){
        //start setup
        iteration = 0;
        simillarResultCounter = 0;
        stopper = false;
        initGraph();
        if(!isCitiesGenerated){
            generateRandomCities();
        }
        rebuildGraph();
        //prevent any errors
        ants.clear();
        generateAnts();
        //calc pheromone decrease per iteration
        pheromoneDecreasingPerIteration = Math.max(Utils.intRangedDoubleToFloatRange((int) ((1.0*citiesCount)/ANTS_COUNT)+1, 0, ANTS_COUNT, 0.1 , 0.9), 0.1);
    }

    public String stop(){
        //todo: remove hacks
        //hack: forces stop
        iteration = MAX_ITERATION_COUNT;
        stopper = true;

        //save summary before cleaning
        String results = summary();
        //fix\clear results
        //cleaning ants - no more best ant in queue
        ants.clear();
        return results;
    }

    private String Summary = null;

    public String summary() {
        if(Summary == null) {
            if(bestPath == null || best == null){ //hack: to preserve null pointer but still could produce if ants.size() == 0
                best = ants.peek();
                bestPath = best.getVisitedCities();
            }
            Summary = "";
            //todo:write results
            Summary += "Best Path: " + Arrays.toString(bestPath.toArray()) + '\n';
            Summary += "Path length: " + getPathLength(best.getVisitedCities()) + '\n';
            Summary += "Ant config: " + best + '\n';
        }
        return Summary;
    }

    private void merge() {

        List<Ant> merged = new ArrayList<>();
        Ant best = ants.poll();//saving best
        //double bestLength = getPathLength(best.getVisitedCities());

        //merging 40% of ants (on each iter - using 2 ants) + 1 to assume that at least one merge will be
        for(int i = 0; i < ANTS_COUNT/5+1; i++) {
            if(stopper) return;
            Ant a = ants.poll();
            Ant b = ants.poll();
            Ant c = mergeAnts(a, b);

            ants.add(a);
            ants.add(b);
            merged.add(c);
            //System.out.println("Father :"+a);
            //System.out.println("Mother :"+b);
            //System.out.println("Child :"+c);
        }
        //removing ants with unsuccessful fitness function {@see getPathLength(List<Integer> cities)}
        //ants.removeIf(ant -> getPathLength(ant.getVisitedCities()) * 2 < bestLength);
        for(int i = 0; i < (ANTS_COUNT*3.0/4+1) && i < ants.size(); i++){
            //by saving top
            merged.add(ants.poll());
        }
        ants.clear();

        //merged
        ants.addAll(merged);
        ants.add(best);//prevent degradation

        //chance for new random ants
        generateAnts();
    }

    public boolean iter() throws Exception {
        if(stopper) return false;//construction to allow stop button

        System.out.println("ITERATION [[["+iteration+"]]]");


        //prepare for iter
        for(Ant ant: ants) {
            ant.clearVisitedCities();
        }

        //debug info of pheromone state
        //System.out.println(graphToString());

        if(stopper) throw new Exception("exiting");
        solutionSearching();

        //selecting best to represent it in summary
        //todo: simillar counter not worked properly
        if(bestPath == null || bestPath.size() <= citiesCount ||
                (ants.peek() != null
                        && ants.peek().getVisitedCities().size() >= citiesCount
                        && getPathLength(bestPath)>getPathLength(ants.peek().getVisitedCities()))){
            simillarResultCounter = best != null && best.equals(ants.peek()) ? simillarResultCounter+1 : 0;//the same ant could be best
            best = ants.peek();
            bestPath = best.getVisitedCities();
        }
        else {
            simillarResultCounter++;
        }

        //each iter decrements
        reducePheromones();

        if(stopper) throw new Exception("exiting");
        //adding pheromones
        updateBasedOnAntPaths();
        //updateBasedOnSingleAnt(ants.peek());

        //updating summary
        //todo: rework summary mechanic to remove null assignment
        Summary = null;
        summary();

        //test end state is reached
        if(iteration == MAX_ITERATION_COUNT-1 || simillarResultCounter >= MAX_SIMILLAR_RESULT_ITERATIONS) {
            return false;
        }

        //mergin + cleaning bad + add random
        merge();
        iteration++;
        return true;
    }

    private void solutionSearching() throws Exception {

        int antNumber = 0;//just for draw purposes
        for(Ant ant: ants) {
            antNumber++;

            //System.out.println("New "+ant+" started");
            final int startCity = Utils.lerp(Math.random(),0,citiesCount)%citiesCount;
            int currentCity = startCity;
            ant.addVisitedCity(currentCity);

            if(SleepTimePerStep > 0) {
                displayZone.repaint();
            }

           // System.out.println("Ant: choose " + currentCity);
            for(int i = 0; i < citiesCount; i++) {
                List<Pair<Integer,Double>> probabilities = new LinkedList<>();
                for(int j = 0; j < citiesCount; j++) {
                    if(stopper) throw new Exception("exiting");

                    if(currentCity == j || ant.getVisitedCities().contains(j)) {
                        continue; //skip loop generating points
                    }

                    //calc probability per possible edge
                    probabilities.add(new Pair<>(j, edgeProbability(currentCity, j, ant)));
                }
                //calc summ of all edges probabilities except current
                double summ = probabilities.stream().reduce((left, right) -> new Pair<>(0,left.getValue() + right.getValue())).orElse(new Pair<>(startCity,0.0)).getValue();

                //find city with max probability, using orElseNode with lowest value as final point in path to create loop {startCity->cities...->startCity}
                Pair<Integer,Double> choosed = probabilities.stream()
                        .map((pair)->new Pair<>(pair.getKey(),pair.getValue()/(summ-pair.getValue())))//.collect(Collectors.toList())
/////MINIMAX
                        .reduce((left, right) -> left.getValue() > right.getValue() ? left : right).orElse(new Pair<>(startCity, 0.0));

                if(SleepTimePerStep > 0) { //draw current ant and actual simulation information
                    //todo: refactor and move out
                    displayZone.getGraphics().setColor(Color.BLACK);
                    displayZone.getGraphics().drawString("Ant [" + antNumber + "/" + ANTS_COUNT + "]\tIteration [" + iteration + "/" + MAX_ITERATION_COUNT + "]", displayZone.getAntZoneSize().x + 20, displayZone.getAntZoneSize().y + 20);
                }

                if(SleepTimePerStep > 0) { //draw current ant step line
                    //todo: refactor and move out
                    AntLine line = new AntLine(cities[currentCity], cities[choosed.getKey()], 1.0);
                    line.pheromoneDiffuse = Color.BLUE;
                    line.draw(displayZone.getGraphics());
                    Thread.sleep(SleepTimePerStep);
                }
                currentCity = choosed.getKey();
                //adding visited city to path
                ant.addVisitedCity(currentCity);

                // System.out.println("Ant: choose " + currentCity);
            }
            //todo: simple reorder function to prevent small unoptimal cities order could be used
            //todo: e.g. flow window on 7 cities where first 2 and last 2 are fixed but 3 inner could be reordered to get bet fitness function
            //System.out.println(Arrays.toString(ant.getVisitedCities().toArray()));
        }
    }

    private void updateBasedOnAntPaths() {
        for(Ant ant: ants) {
            updateBasedOnSingleAnt(ant);
        }
    }

    private void updateBasedOnSingleAnt(Ant ant) {
        List<Integer> path = ant.getVisitedCities();
        double length = getPathLength(path);
        //System.out.println("update for "+ant);
        for(int i = 0; i < path.size()-1; i++) {
            int index = path.get(i);
            int index2 = path.get(i+1);
            //System.out.println(index+":"+index2);
            if(index > index2){ //graph is diagonal 2D array
                int tmp = index2;
                index2 = index;
                index = tmp;
            }
            //update all edges on path
            graph[index2][index].updatePheromoneLevel(ant.getPheromoneLeftKoef()/length);
        }
        //used to update last interval //todo: remove if path contains startCity twice
        //graph[ant.getVisitedCities().get(ant.getVisitedCities().size()-1)][ant.getVisitedCities().get(0)].updatePheromoneLevel(ant.getPheromoneLeftKoef()/length);
    }

    private void reducePheromones() {
        for(Edge[] edges : graph) {
            for (Edge edge : edges) {
                if(edge != null) //x*a-b, a - calculated at start, b - 2.0/MAX_ITERATIONS_COUNT - to reduce chance of suboptimal paths, could be increased
                    edge.setPheromone(edge.getPheromone()*(1- pheromoneDecreasingPerIteration)-2.0/MAX_ITERATION_COUNT);
            }
        }
    }

    public void generateRandomCities(){
        for(int i = 0; i < citiesCount; i++){
            cities[i] = new AntCity(i, Math.random(), Math.random(), displayZone);
        }
        isCitiesGenerated = true;
        rebuildGraph();
    }

    public void generateAnts() {
        while(ants.size() < ANTS_COUNT) {
            ants.add(Ant.generateRandom());
        }
    }

    public void init() {
        initGraph();
        generateRandomCities();
    }

    private void initGraph() {
        graph = new Edge[citiesCount][];
        for (int i = 0; i < citiesCount; i++) {
            graph[i] = new Edge[i+1];
        }
    }

    public void rebuildGraph(){
        if(!isCitiesGenerated){
            generateRandomCities();
        } else {
            for (int i = 0; i < graph.length; i++) {
                for (int j = 0; j < i; j++) {
                    if (graph[i][j] == null) {
                        graph[i][j] = new Edge(cities[i], cities[j], InitialPheromoneLevel);
                    } else {
                        graph[i][j].setPheromone(InitialPheromoneLevel);
                        graph[i][j].setLine(cities[i], cities[j]);
                    }
                }
            }
            for (int i = 0; i < graph.length; i++) {
                if (graph[i][i] == null) {
                    //"dummy" edge, do nothing
                    graph[i][i] = new Edge(new AntLine.EndPointLine());
                }
            }
        }
    }

    public Ant mergeAnts(Ant X, Ant Y) {
        return mergeFunction.apply(X,Y);
    }

    public void draw(Graphics g) {
        for(int i = 0; i < graph.length; i++) {
            for(int j = 0; j < i; j++) {
                graph[i][j].draw(g);
            }
        }
        //cities on top
        for (AntCity city : cities) {
            city.draw(g);
        }
    }

    public void drawAllPaths(Graphics g) {
        for(Ant ant : ants) {
            for (int i = 0; i < ant.getVisitedCities().size() - 1; i++ ) {
                new Edge(cities[ant.getVisitedCities().get(i)], cities[ant.getVisitedCities().get(i+1)])
                        .draw(g);
            }
        }
        //cities on top
        for (AntCity city : cities) {
            city.draw(g);
        }
    }

    public void drawBestPath(Graphics g) {
        //hack: method used only after stop button, so we don't need to store pheromone level
        //todo: add edge ability to redraw with specified color on top without losing sensitive data
        for(Edge[] edges : graph){
            for (Edge edge : edges) {
                if (edge != null)
                    edge.setPheromone(0);
            }
        }
        //clear //todo:no background, could be removed?
        g.clearRect(displayZone.getAntZoneSize().x,displayZone.getAntZoneSize().y,displayZone.getAntZoneSize().width,displayZone.getAntZoneSize().height);
        //retrieving best - assumed optimal path
        List<Integer> visitedCities = ((bestPath == null || bestPath.size() < citiesCount) && ants.peek().getVisitedCities().size() >= citiesCount ? ants.peek().getVisitedCities() : bestPath);
        if(visitedCities != null && visitedCities.size() > 1) { //hack: check to prevent null pointer exception
            for (int i = 0; i < visitedCities.size() - 1; i++) {
                int minID = Math.min(visitedCities.get(i + 1), visitedCities.get(i));
                int maxID = Math.max(visitedCities.get(i + 1), visitedCities.get(i));
                graph[maxID][minID].setPheromone(Edge.maxPheromoneLevel);//maxPheromoneLevel == max brightness
                graph[maxID][minID].update(g);
            }
            //used to update last interval //todo: remove if path contains startCity twice
            //graph[visitedCities.get(visitedCities.size() - 1)][visitedCities.get(0)].setPheromone(Edge.maxPheromoneLevel);
            //graph[visitedCities.get(visitedCities.size() - 1)][visitedCities.get(0)].update(g);
        }
    }

    //calculating edge probability
    public double edgeProbability(Integer from, Integer to, Ant ant) {
        if(from == to)
            return Double.POSITIVE_INFINITY;
        if(to > from) {
            Integer tmp = to;
            to = from;
            from = tmp;
        }
        Edge edge = graph[from][to];

        //probability function original: Math.pow(edge.getPheromone(), ant.getPheromoneAttractionKoef())
        //                + (1 / Math.pow(edge.getLength() , ant.getLengthKoef()));
        //but if values are in [0,1] Math.pow is also in [0,1] so because multiplying has the same result range of [0,1] - it could be used instead
        return (edge.getPheromone() * ant.getPheromoneAttractionKoef())
                + (1 / (edge.getLength() * ant.getLengthKoef()));
    }

    public Ant get(int index) {
        for(Ant ant : ants) {
            if(index-- == 0)
                return ant;
        }
        return null;
    }

    //fitness function
    public double getPathLength(List<Integer> pathCities) {
        double length = 0;
        for(int i = 0; i < pathCities.size()-1; i++) {
            length += cities[pathCities.get(i)].distance(cities[pathCities.get(i+1)]);
        }
        //todo: remove if path contains start city twice
        //length += cities[pathCities.get(0)].distance(cities[pathCities.get(pathCities.size()-1)]);
        return length;
    }

    public BiFunction<Ant, Ant, Ant> getMergeFunction() {
        return mergeFunction;
    }

    public void setMergeFunction(BiFunction<Ant, Ant, Ant> mergeFunction) {
        this.mergeFunction = mergeFunction;
    }

    public int getCitiesCount() {
        return citiesCount;
    }

    public void setCitiesCount(int citiesCount) {
        this.citiesCount = citiesCount;
        isCitiesGenerated = false;
        cities = new AntCity[citiesCount];
        initGraph();
    }

    public int getMAX_ITERATION_COUNT() {
        return MAX_ITERATION_COUNT;
    }

    public void setMAX_ITERATION_COUNT(int MAX_ITERATION_COUNT) {
        this.MAX_ITERATION_COUNT = MAX_ITERATION_COUNT;
    }

    public String graphToString(){
        StringBuilder result = new StringBuilder();
        for(Edge[] edges : graph) {
            for (Edge edge : edges){
                result.append(edge.getPheromone()).append("\t");
            }
            result.append('\n');
        }
        return result.toString();
    }
}

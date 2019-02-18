package edu.kpi.iasa.ai;

import edu.kpi.iasa.ai.configuration.Attribute;
import edu.kpi.iasa.ai.configuration.Configuration;
import edu.kpi.iasa.ai.gui.*;
import edu.kpi.iasa.ai.model.Ant;
import edu.kpi.iasa.ai.model.AntWorker;
import edu.kpi.iasa.ai.model.Edge;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Objects;


public class Main {
    private static Configuration configuration = new Configuration();

    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;

    private static AntZone zone;
    private static AntWorker worker;

    private static AntWindow window;
    private static JTextArea results;

    private static Thread executor;

    public static void main(String[] args) {
        createExecutor();
        SwingUtilities.invokeLater(() -> {
            init();  // Let the constructor do the job
        });
    }

    private static boolean simulating = true;

    private static void createExecutor() {
        System.out.println("[EXECUTOR created]");
        executor = new Thread(()->{
            synchronized (executor) {
                try {
                    executor.wait();
                } catch (InterruptedException ignored) {}
            }
            worker.start();
            simulating = true;
            window.getFrame().repaint();
            try {
                //pause for user
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}
            try {
                while (simulating && worker.iter()) {
                    //draw result
                    results.setText(worker.summary());
                    window.getFrame().repaint();
                    try {
                        //todo: use parameter as value
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {
                        break;//if interrupted we stop cycle
                    }
                }
                //draw final result
                results.setText(worker.summary());

                worker.drawBestPath(zone.getGraphics());
                //window.getFrame().repaint();
            }
            catch (Exception e){
                //catching all exception to perform normal flow
                e.printStackTrace();
            }
        });
        executor.setDaemon(true);
        executor.start();
    }

    private static void init() {
        window = new AntWindow();
        window.init("AntSliderTest", WINDOW_WIDTH, WINDOW_HEIGHT, new FlowLayout());

        //creating panels
        JPanel paramsPanel = new JPanel();
        paramsPanel.setLayout(new BoxLayout(paramsPanel, BoxLayout.Y_AXIS));
        JPanel buttonsPanel = new JPanel(new FlowLayout());

        //creating sliders
        // lambda - function called on each value change
        ConfiguredSlider<Integer> maxIterationCount = new ConfiguredIntegerSlider(
                value -> configuration.setIntValue(Attribute.MAX_ITERATIONS_COUNT, value)
                , 6)
                .init(Attribute.MAX_ITERATIONS_COUNT, 1, 1000, 100)
                .finish();

        ConfiguredSlider<Integer> citiesCount = new ConfiguredIntegerSlider(
                value -> configuration.setIntValue(Attribute.CITIES_COUNT,value)
                , 6)
                .init(Attribute.CITIES_COUNT, 5, 100, 10)
                .finish();

        ConfiguredSlider<Integer> antCount = new ConfiguredIntegerSlider(
                value -> configuration.setIntValue(Attribute.ANT_COUNT, value)
                , 6)
                .init(Attribute.ANT_COUNT, 1, 1000, 100)
                .finish();

        ConfiguredSlider<Float> minPheromoneLevel = new ConfiguredFloatSlider(
                value -> configuration.setFloatValue(Attribute.MIN_PHEROMONE_LEVEL, value)
                , 6)
                .init(Attribute.MIN_PHEROMONE_LEVEL, 0.f, 1.f, 0.0f)
                .finish();
        ConfiguredSlider<Float> maxPheromoneLevel = new ConfiguredFloatSlider(
                value -> configuration.setFloatValue(Attribute.MAX_PHEROMONE_LEVEL, value)
                , 6)
                .init(Attribute.MAX_PHEROMONE_LEVEL, 0.f, 1.f, 1.0f)
                .finish();
        ConfiguredSlider<Float> initialPheromoneLevel = new ConfiguredFloatSlider(
                value -> {
                    value = Utils.clamp(value,
                            configuration.getFloatValue(Attribute.MIN_PHEROMONE_LEVEL),
                            configuration.getFloatValue(Attribute.MAX_PHEROMONE_LEVEL));
                    configuration.setFloatValue(Attribute.INITIAL_PHEROMONE_LEVEL, value);
                }
                , 6)
                .init(Attribute.INITIAL_PHEROMONE_LEVEL, 0.f, 1.f, 1.0f)
                .finish();

        ConfiguredSlider<Integer> sleepPerStep = new ConfiguredIntegerSlider(
                value -> configuration.setIntValue(Attribute.SLEEP_TIME_PER_STEP, value)
                , 6)
                .init(Attribute.SLEEP_TIME_PER_STEP, 0, 100, 20)
                .finish();

        //initial configuration
        configuration.setIntValue(Attribute.MAX_ITERATIONS_COUNT, maxIterationCount.getValue());
        configuration.setIntValue(Attribute.CITIES_COUNT, citiesCount.getValue());
        configuration.setIntValue(Attribute.ANT_COUNT, antCount.getValue());

        configuration.setFloatValue(Attribute.MIN_PHEROMONE_LEVEL, minPheromoneLevel.getValue());
        configuration.setFloatValue(Attribute.MAX_PHEROMONE_LEVEL, maxPheromoneLevel.getValue());
        configuration.setFloatValue(Attribute.INITIAL_PHEROMONE_LEVEL, initialPheromoneLevel.getValue());

        configuration.setIntValue(Attribute.SLEEP_TIME_PER_STEP, sleepPerStep.getValue());

        //todo: setup somewhere else
        Edge.setMinMaxPheromoneLevel(configuration.getFloatValue(Attribute.MIN_PHEROMONE_LEVEL),configuration.getFloatValue(Attribute.MAX_PHEROMONE_LEVEL));
        configuration.save();

        //adding elements on panel
        paramsPanel.setAutoscrolls(true);
        paramsPanel.add(new JLabel("Setup simulation", JLabel.CENTER));
        paramsPanel.add(maxIterationCount.toJComponent());
        paramsPanel.add(citiesCount.toJComponent());
        paramsPanel.add(antCount.toJComponent());
        paramsPanel.add(minPheromoneLevel.toJComponent());
        paramsPanel.add(maxPheromoneLevel.toJComponent());
        paramsPanel.add(initialPheromoneLevel.toJComponent());
        paramsPanel.add(sleepPerStep.toJComponent());

        //creating buttons
        Button startButton = new Button("Start");
        Button stopButton = new Button("Stop");
        Button generateButton = new Button("Generate new cities");
        Button applyButton = new Button("Apply");
        Button cancelButton = new Button("Cancel");

        //setup buttons actions
        startButton.addActionListener(action -> {
            System.err.println("START");

            //if cities count will not be changed - we could save previous state for resimulation
            if(!Objects.equals(configuration.getActualIntValue(Attribute.CITIES_COUNT), configuration.getIntValue(Attribute.CITIES_COUNT))) {
                worker.setCitiesCount(configuration.getIntValue(Attribute.CITIES_COUNT));
                worker.generateRandomCities();
            }
            //todo: remove direct worker call
            worker.InitialPheromoneLevel = configuration.getFloatValue(Attribute.INITIAL_PHEROMONE_LEVEL);
            Edge.setMinMaxPheromoneLevel(configuration.getFloatValue(Attribute.MIN_PHEROMONE_LEVEL),configuration.getFloatValue(Attribute.MAX_PHEROMONE_LEVEL));
            worker.setMAX_ITERATION_COUNT(configuration.getIntValue(Attribute.MAX_ITERATIONS_COUNT));
            worker.SleepTimePerStep = configuration.getIntValue(Attribute.SLEEP_TIME_PER_STEP);

            configuration.save();//apply changes

            //disable const sliders
            maxIterationCount.setModifiable(false);
            citiesCount.setModifiable(false);
            antCount.setModifiable(false);

            minPheromoneLevel.setModifiable(false);
            maxPheromoneLevel.setModifiable(false);
            initialPheromoneLevel.setModifiable(false);

            //and buttons
            generateButton.setEnabled(false);
            applyButton.setEnabled(false);
            cancelButton.setEnabled(false);

            //todo: remove direct worker call
            worker.InitialPheromoneLevel = configuration.getFloatValue(Attribute.INITIAL_PHEROMONE_LEVEL);
            worker.SleepTimePerStep = configuration.getIntValue(Attribute.SLEEP_TIME_PER_STEP);

            //rebuilding graph using actual InitialPheromoneLevel values
            worker.rebuildGraph();
            window.getFrame().repaint();

            //clearing results
            results.setText("HERE WILL BE RESULT OF SIMULATION");

            stopButton.setEnabled(true); //enable stopper
            //starting new thread
            synchronized (executor) {
                executor.notifyAll();
            }
        });
        stopButton.addActionListener(action -> {
            worker.drawBestPath(
                    zone.getGraphics()
            );
            //window.getFrame().repaint();
            simulating = false;
            System.err.println("STOP");
            //results.setText(worker.summary());
            worker.stop();

            //todo: remove deprecated method call
            executor.stop();
            executor = null;
            createExecutor(); //creating new executor
            stopButton.setEnabled(false);//disable current button

            //enable const sliders
            maxIterationCount.setModifiable(true);
            citiesCount.setModifiable(true);
            antCount.setModifiable(true);

            minPheromoneLevel.setModifiable(true);
            maxPheromoneLevel.setModifiable(true);
            initialPheromoneLevel.setModifiable(true);

            //and buttons
            generateButton.setEnabled(true);
            applyButton.setEnabled(true);
            cancelButton.setEnabled(true);
        });
        generateButton.addActionListener(action -> {
            worker.generateRandomCities();
            window.getFrame().repaint();
        });
        applyButton.addActionListener(action -> {
            //todo: refactor for further reusing and code simplifying
            if(!Objects.equals(configuration.getActualIntValue(Attribute.CITIES_COUNT), configuration.getIntValue(Attribute.CITIES_COUNT))) {
                worker.setCitiesCount(configuration.getIntValue(Attribute.CITIES_COUNT));
                worker.generateRandomCities();
            }
            //todo: remove direct worker call
            worker.setMAX_ITERATION_COUNT(configuration.getIntValue(Attribute.MAX_ITERATIONS_COUNT));
            worker.InitialPheromoneLevel = configuration.getFloatValue(Attribute.INITIAL_PHEROMONE_LEVEL);
            Edge.setMinMaxPheromoneLevel(configuration.getFloatValue(Attribute.MIN_PHEROMONE_LEVEL),configuration.getFloatValue(Attribute.MAX_PHEROMONE_LEVEL));

            worker.SleepTimePerStep = configuration.getIntValue(Attribute.SLEEP_TIME_PER_STEP);

            worker.rebuildGraph();

            configuration.save();
            window.getFrame().repaint();
        });
        cancelButton.addActionListener(action -> {
            configuration.cancel();
            //retrieving and updating sliders
            //todo: remove if will autoupdate from configuration
            maxIterationCount.setValue(configuration.getIntValue(Attribute.MAX_ITERATIONS_COUNT));
            citiesCount.setValue(configuration.getIntValue(Attribute.CITIES_COUNT));
            antCount.setValue(configuration.getIntValue(Attribute.ANT_COUNT));

            minPheromoneLevel.setValue(configuration.getFloatValue(Attribute.MIN_PHEROMONE_LEVEL));
            maxPheromoneLevel.setValue(configuration.getFloatValue(Attribute.MAX_PHEROMONE_LEVEL));
            initialPheromoneLevel.setValue(configuration.getFloatValue(Attribute.INITIAL_PHEROMONE_LEVEL));
        });
        //adding buttons on panel
        buttonsPanel.add(startButton);
        buttonsPanel.add(stopButton);
        buttonsPanel.add(generateButton);
        buttonsPanel.add(applyButton);
        buttonsPanel.add(cancelButton);

        //add buttons
        paramsPanel.add(buttonsPanel);

        results = new JTextArea();
        results.setLineWrap(true);
        results.setColumns(10);
        results.setEditable(false);
        results.setAutoscrolls(true);
        results.setText("HERE WILL BE RESULT OF SIMULATION");
        paramsPanel.add(results);

        //zone for redrawing
        zone = new AntZone(25, null);
        worker = new AntWorker(zone, configuration);
        zone.setWorker(worker);
        //setuping custom merge function
        worker.setMergeFunction((a,b)->{
            //getitng random koefs in range [father1Koef,father2Koef]
            double lengthKoef               = Utils.lerp(Math.random(),a.getLengthKoef(),b.getLengthKoef());
            double pheromoneAttractionKoef  = Utils.lerp(Math.random(),a.getPheromoneAttractionKoef(),b.getPheromoneAttractionKoef());
            double pheromoneLeftKoef        = Utils.lerp(Math.random(),a.getPheromoneLeftKoef(),b.getPheromoneLeftKoef());
            lengthKoef              +=(Math.random()-0.5)*(lengthKoef/5);                 //adjustment to allow mutations
            pheromoneAttractionKoef +=(Math.random()-0.5)*(pheromoneAttractionKoef/5);    //adjustment to allow mutations
            pheromoneLeftKoef       +=(Math.random()-0.5)*(pheromoneLeftKoef/5);          //adjustment to allow mutations
            //todo: remove hardcoded params
            lengthKoef              = Utils.clamp(lengthKoef, 0.0, 1.0);
            pheromoneAttractionKoef = Utils.clamp(pheromoneAttractionKoef, 0.0, 1.0);
            pheromoneLeftKoef       = Utils.clamp(pheromoneLeftKoef, 0.0, 1.0);
            return new Ant(lengthKoef,pheromoneAttractionKoef,pheromoneLeftKoef);
        });
        //setup worker
        worker.InitialPheromoneLevel = initialPheromoneLevel.getValue();
        worker.SleepTimePerStep = configuration.getIntValue(Attribute.SLEEP_TIME_PER_STEP);
        worker.init();

        //content pane
        JPanel contentPane = new JPanel(new BorderLayout());

        //add resize listener
        contentPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                resizeAntZoneOnWindowResize(e.getComponent().getWidth(), e.getComponent().getHeight(),25);
            }
        });

        //adding all on window
        contentPane.setLayout(new BorderLayout());
        contentPane.add(zone, BorderLayout.CENTER);
        contentPane.add(paramsPanel, BorderLayout.EAST);
        window.getFrame().setContentPane(contentPane);
        window.getFrame().setMinimumSize(new Dimension(800,600));
        window.finish();
        zone.repaint(zone.getAntZoneSize());
    }

    private static void resizeAntZoneOnWindowResize(final int newWidth, final int newHeight) {
        resizeAntZoneOnWindowResize(newWidth, newHeight, 0);
    }

    private static void resizeAntZoneOnWindowResize(final int newWidth, final int newHeight, final int padding) {
        zone.setAntZoneSize(new Rectangle(padding,padding,newWidth-330-2*padding,newHeight-2*padding));
        window.getFrame().repaint();
    }
}

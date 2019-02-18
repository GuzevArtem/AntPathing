package edu.kpi.iasa.ai.configuration;

public interface Attribute {

    String MAX_ITERATIONS_COUNT = "Max iterations count";

    String CITIES_COUNT = "Number of cities";

    String ANT_COUNT = "Number of ants";

    String MIN_PHEROMONE_LEVEL = "Lowest pheromone level";

    String MAX_PHEROMONE_LEVEL = "Highest pheromone level";

    String INITIAL_PHEROMONE_LEVEL = "Pheromone level on start";

    String SLEEP_TIME_PER_STEP = "Time to draw step of single ant(millis)";

    default boolean isConstToSimulation(String attribute) {
        return CITIES_COUNT.equals(attribute)
                || MAX_ITERATIONS_COUNT.equals(attribute)
                || ANT_COUNT.equals(attribute)
                || MIN_PHEROMONE_LEVEL.equals(attribute)
                || MAX_PHEROMONE_LEVEL.equals(attribute)
                || INITIAL_PHEROMONE_LEVEL.equals(attribute);
    }

}

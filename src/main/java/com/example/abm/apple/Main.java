package com.example.abm.apple;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.Schedule;
import repast.simphony.parameter.Parameters;
import repast.simphony.parameter.ParametersCreator;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.engine.environment.RunState;

public class Main {

  public static void main(String[] args) {
    System.out.println("Running on Java Version: " + System.getProperty("java.version"));

    // --- parameters ---
    ParametersCreator pc = new ParametersCreator();
    pc.addParameter("YEARS", Integer.class, 5, true);
    pc.addParameter("STEPS_PER_YEAR", Integer.class, 260, true);
    pc.addParameter("APPLE_YIELD_LB_PER_ACRE", Double.class, 28800.0, true);
    pc.addParameter("PRODUCER_SURVEY_PATH", String.class,
        "data/apple_survey_6.13_producers.csv", true);
    pc.addParameter("CONSUMER_AGENT_COUNT", Integer.class, 1000, true);
    pc.addParameter("CONSUMERS_REPRESENTED_PER_AGENT", Double.class, 3651.2471, true);
    pc.addParameter("initial_funds_producers",   Double.class, 50000.0, true);
    pc.addParameter("initial_funds_wholesalers", Double.class, 150000.0, true);
    pc.addParameter("initial_funds_retailers",   Double.class, 25000.0, true);
    pc.addParameter("initial_funds_consumers",   Double.class, 400.0, true);
    pc.addParameter("initial_stock_producers",   Integer.class, 500, true);
    pc.addParameter("initial_stock_wholesalers", Integer.class, 240, true);
    pc.addParameter("initial_stock_retailers",   Integer.class, 150, true);
    pc.addParameter("TOTAL_PRODUCTION_COST_PER_LB", Double.class, 0.7957, true);
    pc.addParameter("FERTILIZER_COST_SHARE", Double.class, 0.0710, true);
    pc.addParameter("FUEL_COST_SHARE", Double.class, 0.0320, true);
    pc.addParameter("OTHER_INPUT_COST_SHARE", Double.class, 0.1520, true);
    pc.addParameter("OTHER_COST_SHARE", Double.class, 0.7450, true);
    pc.addParameter("USE_TRANSLOG_PRODUCTION_FUNCTION", Boolean.class, true, true);
    pc.addParameter("TRANSLOG_SELF_INTERACTION", Double.class, 0.0, true);
    pc.addParameter("TRANSLOG_CROSS_INTERACTION", Double.class, 0.0, true);
    pc.addParameter("EPSILON_DEMAND",      Double.class, 1.8, true);
    pc.addParameter("PRICE_FLOOR",         Double.class, 0.10, true);
    pc.addParameter("PRICE_CEIL",          Double.class, 10.0, true);
    pc.addParameter("RATIO_CLAMP_LOW",     Double.class, 2.0/3.0, true);
    pc.addParameter("RATIO_CLAMP_HIGH",    Double.class, 1.5, true);
    pc.addParameter("ML_SMOOTHING_MONTHS", Integer.class, 3, true);
    pc.addParameter("USE_FARM_ANCHORS_FOR_PRICE", Boolean.class, true, true);
    pc.addParameter("FARM_ANCHOR_BLEND",          Double.class, 0.90, true);
    pc.addParameter("FARM_DEMAND_LB_PER_CAPITA",  Double.class, 17.0, true);
    pc.addParameter("FARM_SUPPLY_LB_PER_CAPITA",  Double.class, 21.26, true);
    pc.addParameter("POPULATION_EQUIVALENT",      Integer.class, 0, true);
    pc.addParameter("PRICE_SEASONAL_AMPL",  Double.class, 0.03, true);
    pc.addParameter("PRICE_SEASONAL_PHASE", Double.class, 0.0,  true);
    pc.addParameter("PRICE_AR1_RHO",        Double.class, 0.60, true);
    pc.addParameter("PRICE_AR1_SIGMA",      Double.class, 0.015,true);
    pc.addParameter("PRICE_NOISE_CLAMP",    Double.class, 0.10, true);
    pc.addParameter("WHOLESALE_PRICE_ALPHA", Double.class, 0.6,  true);
    pc.addParameter("RETAIL_MARKUP",         Double.class, 0.20, true);
    pc.addParameter("ML_MONTHLY_PATH",  String.class,
        "data/model_predictions_2021_2025_gradient_boosting_daily_mean.csv", true);
    pc.addParameter("CONSUMER_CSV_PATH", String.class,
        "data/apple_02212025_02.csv", true);

    Parameters params = pc.createParameters();

    // --- init without repast ui ---
    Schedule schedule = new Schedule();
    // 'true' = batch/headless; do not pass a controller
    RunEnvironment.init(schedule, null, params, true);

    // --- build model and context ---
    AppleMarketBuilder builder = new AppleMarketBuilder();
    Context<Object> context = new DefaultContext<>();
    builder.build(context);

    // set master context
    RunState.init().setMasterContext(context);

    int years = (Integer) params.getValue("YEARS");
    int stepsPerYear = (Integer) params.getValue("STEPS_PER_YEAR");
    int totalSteps = years * stepsPerYear;

    // execute the ticks scheduled
    for (int t = 0; t <= totalSteps + 1; t++) {
      schedule.execute();
    }

    System.out.println("Done.");
  }
}

package com.example.abm.apple;

import com.example.abm.apple.InputCostScenario;
import repast.simphony.parameter.Parameter;

public class ModelParams {

  // ---------- timeframe ----------
  private int YEARS = 5;

  @Parameter(displayName = "Years", usageName = "YEARS")
  public int getYEARS() { return YEARS; }
  public void setYEARS(int years) { this.YEARS = years; }

  private int STEPS_PER_YEAR = 260;

  @Parameter(displayName = "Steps per Year (Monthly of 12 Steps)", usageName = "STEPS_PER_YEAR")
  public int getSTEPS_PER_YEAR() { return STEPS_PER_YEAR; }
  public void setSTEPS_PER_YEAR(int v) { this.STEPS_PER_YEAR = v; }

  public static final String FRESH_APPLES_VARIETY = "FRESH APPLES";

  private double APPLE_YIELD_LB_PER_ACRE = 28800.0;

  @Parameter(displayName = "Apple Yield (lb/acre/year)", usageName = "APPLE_YIELD_LB_PER_ACRE")
  public double getAppleYieldLbPerAcre() { return APPLE_YIELD_LB_PER_ACRE; }
  public void setAppleYieldLbPerAcre(double v) { this.APPLE_YIELD_LB_PER_ACRE = v; }

  private String PRODUCER_SURVEY_PATH = "data/apple_survey_6.13_producers.csv";

  @Parameter(displayName = "Producer Survey CSV path", usageName = "PRODUCER_SURVEY_PATH")
  public String getPRODUCER_SURVEY_PATH() { return PRODUCER_SURVEY_PATH; }
  public void setPRODUCER_SURVEY_PATH(String v) { this.PRODUCER_SURVEY_PATH = v; }

  private int CONSUMER_AGENT_COUNT = 1000;

  @Parameter(displayName = "Consumer Agent Count", usageName = "CONSUMER_AGENT_COUNT")
  public int getConsumerAgentCount() { return CONSUMER_AGENT_COUNT; }
  public void setConsumerAgentCount(int v) { this.CONSUMER_AGENT_COUNT = v; }

  private double CONSUMERS_REPRESENTED_PER_AGENT = 3651.2471;

  @Parameter(displayName = "Consumers Represented per Agent", usageName = "CONSUMERS_REPRESENTED_PER_AGENT")
  public double getConsumersRepresentedPerAgent() { return CONSUMERS_REPRESENTED_PER_AGENT; }
  public void setConsumersRepresentedPerAgent(double v) { this.CONSUMERS_REPRESENTED_PER_AGENT = v; }

  // ---------- budget and inventory ----------
  private double initial_funds_producers = 50000;

  @Parameter(displayName = "Initial Funds: Producers", usageName = "initial_funds_producers")
  public double getInitialFundsProducers() { return initial_funds_producers; }
  public void setInitialFundsProducers(double v) { this.initial_funds_producers = v; }

  private double initial_funds_wholesalers = 150000;

  @Parameter(displayName = "Initial Funds: Wholesalers", usageName = "initial_funds_wholesalers")
  public double getInitialFundsWholesalers() { return initial_funds_wholesalers; }
  public void setInitialFundsWholesalers(double v) { this.initial_funds_wholesalers = v; }

  private double initial_funds_retailers = 25000;

  @Parameter(displayName = "Initial Funds: Retailers", usageName = "initial_funds_retailers")
  public double getInitialFundsRetailers() { return initial_funds_retailers; }
  public void setInitialFundsRetailers(double v) { this.initial_funds_retailers = v; }

  private double initial_funds_consumers = 400;

  @Parameter(displayName = "Initial Funds: Consumers", usageName = "initial_funds_consumers")
  public double getInitialFundsConsumers() { return initial_funds_consumers; }
  public void setInitialFundsConsumers(double v) { this.initial_funds_consumers = v; }

  private int initial_stock_producers = 500;

  @Parameter(displayName = "Initial Stock: Producers", usageName = "initial_stock_producers")
  public int getInitialStockProducers() { return initial_stock_producers; }
  public void setInitialStockProducers(int v) { this.initial_stock_producers = v; }

  private int initial_stock_wholesalers = 240;

  @Parameter(displayName = "Initial Stock: Wholesalers", usageName = "initial_stock_wholesalers")
  public int getInitialStockWholesalers() { return initial_stock_wholesalers; }
  public void setInitialStockWholesalers(int v) { this.initial_stock_wholesalers = v; }

  private int initial_stock_retailers = 150;

  @Parameter(displayName = "Initial Stock: Retailers", usageName = "initial_stock_retailers")
  public int getInitialStockRetailers() { return initial_stock_retailers; }
  public void setInitialStockRetailers(int v) { this.initial_stock_retailers = v; }

  // ---------- input costs (May 8, 2026) ----------
  // Calibrated from the WSU 2024 Gala apple enterprise budget.
  // https://wpcdn.web.wsu.edu/cahnrs/uploads/sites/5/TB107E_2024-Gala-Enterprise-Budget.pdf
  // Total cost is in dollars per pound of apples produced.
  private double TOTAL_PRODUCTION_COST_PER_LB = 0.7957;

  @Parameter(displayName = "Total Production Cost ($/lb)", usageName = "TOTAL_PRODUCTION_COST_PER_LB")
  public double getTotalProductionCostPerLb() { return TOTAL_PRODUCTION_COST_PER_LB; }
  public void setTotalProductionCostPerLb(double v) { this.TOTAL_PRODUCTION_COST_PER_LB = v; }

  // Previous cost share assumptions:
  // FERTILIZER_COST_SHARE = 0.0051
  // FUEL_COST_SHARE = 0.0058
  // OTHER_COST_SHARE = 0.9891
  //
  // Current cost share assumptions from 2025_FarmExpenditures_Highlights.pdf:
  // Fertilizer, lime, and soil conditioners = 7.1%
  // Fuel = 3.2%
  // Other input cost = seeds/plants 5.7% + farm supplies/repairs 5.0%
  //                  + agricultural chemicals 4.5% = 15.2%
  // Other cost = residual = 74.5%
  private double FERTILIZER_COST_SHARE = 0.0710;

  @Parameter(displayName = "Fertilizer Cost Share", usageName = "FERTILIZER_COST_SHARE")
  public double getFertilizerCostShare() { return FERTILIZER_COST_SHARE; }
  public void setFertilizerCostShare(double v) { this.FERTILIZER_COST_SHARE = v; }

  private double FUEL_COST_SHARE = 0.0320;

  @Parameter(displayName = "Fuel Cost Share", usageName = "FUEL_COST_SHARE")
  public double getFuelCostShare() { return FUEL_COST_SHARE; }
  public void setFuelCostShare(double v) { this.FUEL_COST_SHARE = v; }

  private double OTHER_INPUT_COST_SHARE = 0.1520;

  @Parameter(displayName = "Other Input Cost Share", usageName = "OTHER_INPUT_COST_SHARE")
  public double getOtherInputCostShare() { return OTHER_INPUT_COST_SHARE; }
  public void setOtherInputCostShare(double v) { this.OTHER_INPUT_COST_SHARE = v; }

  private double OTHER_COST_SHARE = 0.7450;

  @Parameter(displayName = "Other Cost Share", usageName = "OTHER_COST_SHARE")
  public double getOtherCostShare() { return OTHER_COST_SHARE; }
  public void setOtherCostShare(double v) { this.OTHER_COST_SHARE = v; }

  private boolean USE_TRANSLOG_PRODUCTION_FUNCTION = true;

  @Parameter(displayName = "Use Translog Production Function", usageName = "USE_TRANSLOG_PRODUCTION_FUNCTION")
  public boolean isUseTranslogProductionFunction() { return USE_TRANSLOG_PRODUCTION_FUNCTION; }
  public void setUseTranslogProductionFunction(boolean v) { this.USE_TRANSLOG_PRODUCTION_FUNCTION = v; }

  private double TRANSLOG_SELF_INTERACTION = 0.0;

  @Parameter(displayName = "Translog Self Interaction", usageName = "TRANSLOG_SELF_INTERACTION")
  public double getTranslogSelfInteraction() { return TRANSLOG_SELF_INTERACTION; }
  public void setTranslogSelfInteraction(double v) { this.TRANSLOG_SELF_INTERACTION = v; }

  private double TRANSLOG_CROSS_INTERACTION = 0.0;

  @Parameter(displayName = "Translog Cross Interaction", usageName = "TRANSLOG_CROSS_INTERACTION")
  public double getTranslogCrossInteraction() { return TRANSLOG_CROSS_INTERACTION; }
  public void setTranslogCrossInteraction(double v) { this.TRANSLOG_CROSS_INTERACTION = v; }

  private double normalizedShare(double share) {
    double sum = FERTILIZER_COST_SHARE + FUEL_COST_SHARE
        + OTHER_INPUT_COST_SHARE + OTHER_COST_SHARE;
    return (sum > 0.0) ? share / sum : 0.0;
  }

  public double getNormalizedFertilizerCostShare() {
    return normalizedShare(FERTILIZER_COST_SHARE);
  }

  public double getNormalizedFuelCostShare() {
    return normalizedShare(FUEL_COST_SHARE);
  }

  public double getNormalizedOtherInputCostShare() {
    return normalizedShare(OTHER_INPUT_COST_SHARE);
  }

  public double getNormalizedOtherCostShare() {
    return normalizedShare(OTHER_COST_SHARE);
  }

  public double getFertilizerCostBaseline() {
    return TOTAL_PRODUCTION_COST_PER_LB * normalizedShare(FERTILIZER_COST_SHARE);
  }

  public void setFertilizerCostBaseline(double v) {
    if (TOTAL_PRODUCTION_COST_PER_LB > 0.0) {
      this.FERTILIZER_COST_SHARE = v / TOTAL_PRODUCTION_COST_PER_LB;
    }
  }

  public double getFuelCostBaseline() {
    return TOTAL_PRODUCTION_COST_PER_LB * normalizedShare(FUEL_COST_SHARE);
  }

  public void setFuelCostBaseline(double v) {
    if (TOTAL_PRODUCTION_COST_PER_LB > 0.0) {
      this.FUEL_COST_SHARE = v / TOTAL_PRODUCTION_COST_PER_LB;
    }
  }

  public double getOtherInputCostBaseline() {
    return TOTAL_PRODUCTION_COST_PER_LB * normalizedShare(OTHER_INPUT_COST_SHARE);
  }

  public void setOtherInputCostBaseline(double v) {
    if (TOTAL_PRODUCTION_COST_PER_LB > 0.0) {
      this.OTHER_INPUT_COST_SHARE = v / TOTAL_PRODUCTION_COST_PER_LB;
    }
  }

  public double getOtherCostPerUnit() {
    return TOTAL_PRODUCTION_COST_PER_LB * normalizedShare(OTHER_COST_SHARE);
  }

  public void setOtherCostPerUnit(double v) {
    if (TOTAL_PRODUCTION_COST_PER_LB > 0.0) {
      this.OTHER_COST_SHARE = v / TOTAL_PRODUCTION_COST_PER_LB;
    }
  }

  // Scenario object — not exposed via @Parameter since it's a complex object,
  // not a primitive. Set programmatically from your simulation driver.
  private InputCostScenario inputCostScenario = new InputCostScenario();

  public InputCostScenario getInputCostScenario() { return inputCostScenario; }
  public void setInputCostScenario(InputCostScenario v) { this.inputCostScenario = v; }

  // ---------- farm prices ----------
  private double EPSILON_DEMAND = 1.8;

  @Parameter(displayName = "Epsilon Demand", usageName = "EPSILON_DEMAND")
  public double getEPSILON_DEMAND() { return EPSILON_DEMAND; }
  public void setEPSILON_DEMAND(double v) { this.EPSILON_DEMAND = v; }

  private double PRICE_FLOOR = 0.10;

  @Parameter(displayName = "Price Floor", usageName = "PRICE_FLOOR")
  public double getPRICE_FLOOR() { return PRICE_FLOOR; }
  public void setPRICE_FLOOR(double v) { this.PRICE_FLOOR = v; }

  private double PRICE_CEIL = 10.0;

  @Parameter(displayName = "Price Ceiling", usageName = "PRICE_CEIL")
  public double getPRICE_CEIL() { return PRICE_CEIL; }
  public void setPRICE_CEIL(double v) { this.PRICE_CEIL = v; }

  private double RATIO_CLAMP_LOW = 2.0 / 3.0;

  @Parameter(displayName = "Clamp Low (Qs/Qd)", usageName = "RATIO_CLAMP_LOW")
  public double getRATIO_CLAMP_LOW() { return RATIO_CLAMP_LOW; }
  public void setRATIO_CLAMP_LOW(double v) { this.RATIO_CLAMP_LOW = v; }

  private double RATIO_CLAMP_HIGH = 1.5;

  @Parameter(displayName = "Clamp High (Qs/Qd)", usageName = "RATIO_CLAMP_HIGH")
  public double getRATIO_CLAMP_HIGH() { return RATIO_CLAMP_HIGH; }
  public void setRATIO_CLAMP_HIGH(double v) { this.RATIO_CLAMP_HIGH = v; }

  private int ML_SMOOTHING_MONTHS = 3;

  @Parameter(displayName = "ML Smoothing Months", usageName = "ML_SMOOTHING_MONTHS")
  public int getML_SMOOTHING_MONTHS() { return ML_SMOOTHING_MONTHS; }
  public void setML_SMOOTHING_MONTHS(int v) { this.ML_SMOOTHING_MONTHS = v; }

  private boolean USE_FARM_ANCHORS_FOR_PRICE = true;

  @Parameter(displayName = "Use Farm Anchors", usageName = "USE_FARM_ANCHORS_FOR_PRICE")
  public boolean isUSE_FARM_ANCHORS_FOR_PRICE() { return USE_FARM_ANCHORS_FOR_PRICE; }
  public void setUSE_FARM_ANCHORS_FOR_PRICE(boolean v) { this.USE_FARM_ANCHORS_FOR_PRICE = v; }

  private double FARM_ANCHOR_BLEND = 0.90;

  @Parameter(displayName = "Farm Anchor Blend (alpha)", usageName = "FARM_ANCHOR_BLEND")
  public double getFARM_ANCHOR_BLEND() { return FARM_ANCHOR_BLEND; }
  public void setFARM_ANCHOR_BLEND(double v) { this.FARM_ANCHOR_BLEND = v; }

  private double FARM_DEMAND_LB_PER_CAPITA = 17.0;

  @Parameter(displayName = "Farm Demand (lb/cap/yr)", usageName = "FARM_DEMAND_LB_PER_CAPITA")
  public double getFARM_DEMAND_LB_PER_CAPITA() { return FARM_DEMAND_LB_PER_CAPITA; }
  public void setFARM_DEMAND_LB_PER_CAPITA(double v) { this.FARM_DEMAND_LB_PER_CAPITA = v; }

  private double FARM_SUPPLY_LB_PER_CAPITA = 21.26;

  @Parameter(displayName = "Farm Supply (lb/cap/yr)", usageName = "FARM_SUPPLY_LB_PER_CAPITA")
  public double getFARM_SUPPLY_LB_PER_CAPITA() { return FARM_SUPPLY_LB_PER_CAPITA; }
  public void setFARM_SUPPLY_LB_PER_CAPITA(double v) { this.FARM_SUPPLY_LB_PER_CAPITA = v; }

  private int POPULATION_EQUIVALENT = 0;

  @Parameter(displayName = "Population Equivalent (0=auto)", usageName = "POPULATION_EQUIVALENT")
  public int getPOPULATION_EQUIVALENT() { return POPULATION_EQUIVALENT; }
  public void setPOPULATION_EQUIVALENT(int v) { this.POPULATION_EQUIVALENT = v; }

  // ---------- seasonality & AR(1) ----------
  private double PRICE_SEASONAL_AMPL = 0.03;

  @Parameter(displayName = "Seasonal Amplitude", usageName = "PRICE_SEASONAL_AMPL")
  public double getPRICE_SEASONAL_AMPL() { return PRICE_SEASONAL_AMPL; }
  public void setPRICE_SEASONAL_AMPL(double v) { this.PRICE_SEASONAL_AMPL = v; }

  private double PRICE_SEASONAL_PHASE = 0.0;

  @Parameter(displayName = "Seasonal Phase (rad)", usageName = "PRICE_SEASONAL_PHASE")
  public double getPRICE_SEASONAL_PHASE() { return PRICE_SEASONAL_PHASE; }
  public void setPRICE_SEASONAL_PHASE(double v) { this.PRICE_SEASONAL_PHASE = v; }

  private double PRICE_AR1_RHO = 0.60;

  @Parameter(displayName = "AR(1) Rho", usageName = "PRICE_AR1_RHO")
  public double getPRICE_AR1_RHO() { return PRICE_AR1_RHO; }
  public void setPRICE_AR1_RHO(double v) { this.PRICE_AR1_RHO = v; }

  private double PRICE_AR1_SIGMA = 0.015;

  @Parameter(displayName = "AR(1) Sigma", usageName = "PRICE_AR1_SIGMA")
  public double getPRICE_AR1_SIGMA() { return PRICE_AR1_SIGMA; }
  public void setPRICE_AR1_SIGMA(double v) { this.PRICE_AR1_SIGMA = v; }

  private double PRICE_NOISE_CLAMP = 0.10;

  @Parameter(displayName = "Noise Clamp (±)", usageName = "PRICE_NOISE_CLAMP")
  public double getPRICE_NOISE_CLAMP() { return PRICE_NOISE_CLAMP; }
  public void setPRICE_NOISE_CLAMP(double v) { this.PRICE_NOISE_CLAMP = v; }
  
  //---------- processing & conversion ---------- (Feb 3, 2026) 
  //private double INPUT_TO_OUTPUT_YIELD = 0.70;   // 70% of input product are converted into output product

  //@Parameter(displayName = "Apple → Apple Juice/Cider Yield", usageName = "INPUT_TO_OUTPUT_YIELD")
  //public double getINPUT_TO_OUTPUT_YIELD() { return INPUT_TO_OUTPUT_YIELD; }
  //public void setINPUT_TO_OUTPUT_YIELD(double v) { this.INPUT_TO_OUTPUT_YIELD = v; }

  // ---------- wholesale & retail ----------
  private double WHOLESALE_PRICE_ALPHA = 0.6;

  @Parameter(displayName = "Wholesale Price Alpha", usageName = "WHOLESALE_PRICE_ALPHA")
  public double getWHOLESALE_PRICE_ALPHA() { return WHOLESALE_PRICE_ALPHA; }
  public void setWHOLESALE_PRICE_ALPHA(double v) { this.WHOLESALE_PRICE_ALPHA = v; }

  private double RETAIL_MARKUP = 0.20;

  @Parameter(displayName = "Retail Markup", usageName = "RETAIL_MARKUP")
  public double getRETAIL_MARKUP() { return RETAIL_MARKUP; }
  public void setRETAIL_MARKUP(double v) { this.RETAIL_MARKUP = v; }

  // ---------- paths ----------
  private String ML_MONTHLY_PATH =
      "data/model_predictions_2021_2025_gradient_boosting_daily_mean.csv";

  @Parameter(displayName = "ML Price CSV path", usageName = "ML_MONTHLY_PATH")
  public String getML_MONTHLY_PATH() { return ML_MONTHLY_PATH; }
  public void setML_MONTHLY_PATH(String v) { this.ML_MONTHLY_PATH = v; }

  private String CONSUMER_CSV_PATH = "data/apple_02212025_02.csv";

  @Parameter(displayName = "Consumers CSV path", usageName = "CONSUMER_CSV_PATH")
  public String getCONSUMER_CSV_PATH() { return CONSUMER_CSV_PATH; }
  public void setCONSUMER_CSV_PATH(String v) { this.CONSUMER_CSV_PATH = v; }
}

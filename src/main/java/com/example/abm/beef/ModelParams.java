package com.example.abm.beef;

import repast.simphony.parameter.Parameter;

public class ModelParams {

  // ---------- timeframe ----------
  private int YEARS = 5;

  @Parameter(displayName = "Years", usageName = "YEARS")
  public int getYEARS() { return YEARS; }
  public void setYEARS(int years) { this.YEARS = years; }

  private int STEPS_PER_YEAR = 12;

  @Parameter(displayName = "Steps per Year (Monthly of 12 Steps)", usageName = "STEPS_PER_YEAR")
  public int getSTEPS_PER_YEAR() { return STEPS_PER_YEAR; }
  public void setSTEPS_PER_YEAR(int v) { this.STEPS_PER_YEAR = v; }

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

  private double initial_funds_consumers = 100;

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

  private double FARM_ANCHOR_BLEND = 0.85;

  @Parameter(displayName = "Farm Anchor Blend (alpha)", usageName = "FARM_ANCHOR_BLEND")
  public double getFARM_ANCHOR_BLEND() { return FARM_ANCHOR_BLEND; }
  public void setFARM_ANCHOR_BLEND(double v) { this.FARM_ANCHOR_BLEND = v; }

  private double FARM_DEMAND_LB_PER_CAPITA = 15.8;

  @Parameter(displayName = "Farm Demand (lb/cap/yr)", usageName = "FARM_DEMAND_LB_PER_CAPITA")
  public double getFARM_DEMAND_LB_PER_CAPITA() { return FARM_DEMAND_LB_PER_CAPITA; }
  public void setFARM_DEMAND_LB_PER_CAPITA(double v) { this.FARM_DEMAND_LB_PER_CAPITA = v; }

  private double FARM_SUPPLY_LB_PER_CAPITA = 30.16;

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
      "data/beef_prices_springfield_bulls_steers_heifers_per_lb_monthly_rf.csv";

  @Parameter(displayName = "ML Price CSV path", usageName = "ML_MONTHLY_PATH")
  public String getML_MONTHLY_PATH() { return ML_MONTHLY_PATH; }
  public void setML_MONTHLY_PATH(String v) { this.ML_MONTHLY_PATH = v; }

  private String CONSUMER_CSV_PATH = "data/beef_02212025_02.csv";

  @Parameter(displayName = "Consumers CSV path", usageName = "CONSUMER_CSV_PATH")
  public String getCONSUMER_CSV_PATH() { return CONSUMER_CSV_PATH; }
  public void setCONSUMER_CSV_PATH(String v) { this.CONSUMER_CSV_PATH = v; }
}

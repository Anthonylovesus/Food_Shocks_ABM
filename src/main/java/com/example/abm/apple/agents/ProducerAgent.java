package com.example.abm.apple.agents;

import com.example.abm.apple.MLPriceLoader;
import com.example.abm.apple.ModelParams;
import com.example.abm.apple.MathUtil;
import com.example.abm.apple.InputCostScenario;
import com.example.abm.apple.TranslogProductionFunction;

public class ProducerAgent {
    private final int id;
    private final String variety;
    private final double acres;
    private final double annualProductionLbs;
    private double productionCarryover = 0.0;
    private double money;
    private int stock;
    private double p_ml;            // daily Gradient Boosting mean price
    private double askingPrice;     // producer ask price
    private double productionCost;  // NOW DERIVED from input costs
    private double fertilizerCostThisStep;
    private double fuelCostThisStep;
    private double otherInputCostThisStep;
    private double otherCostThisStep;
    private int unitsSoldCum = 0;
    private double revenueCum = 0;
    private int unitsSoldThisStep = 0;
    private double revenueThisStep = 0;
    private int producedThisStep = 0;
    
    // Variety-specific input intensities — how much of each input per apple unit
    private final double fertilizerIntensity;
    private final double fuelIntensity;
    
    private final ModelParams P;
    private final MLPriceLoader ml;
    
    public ProducerAgent(int id, String variety, ModelParams P, MLPriceLoader ml) {
        this(id, variety, 0.0, 0.0, P, ml);
    }

    public ProducerAgent(int id, String variety, double acres, double annualProductionLbs,
                         ModelParams P, MLPriceLoader ml) {
        this.id = id;
        this.variety = variety;
        this.acres = acres;
        this.annualProductionLbs = annualProductionLbs;
        this.P = P;
        this.ml = ml;
        this.money = P.getInitialFundsProducers();
        this.stock = P.getInitialStockProducers();
        this.p_ml = ml.getPriceFor(variety, 0);
        this.askingPrice = p_ml;
        
        // Set variety-specific intensities
        this.fertilizerIntensity = getFertilizerIntensityFor(variety);
        this.fuelIntensity = getFuelIntensityFor(variety);
        
        // Compute initial production cost at step 0
        this.productionCost = computeProductionCost(0);
    }
    
    /**
     * Compute production cost in dollars per pound for the current step.
     * ModelParams splits total production cost into fertilizer, fuel, other
     * input, and other cost shares. InputCostScenario can shock each component
     * over time.
     */
    private double computeProductionCost(int step) {
        updateInputCosts(step);
        return fertilizerCostThisStep + fuelCostThisStep
            + otherInputCostThisStep + otherCostThisStep;
    }

    private void updateInputCosts(int step) {
        InputCostScenario scenario = P.getInputCostScenario();

        double effectiveFertCost = scenario.getFertilizerCost(
            step, P.getFertilizerCostBaseline());
        double effectiveFuelCost = scenario.getFuelCost(
            step, P.getFuelCostBaseline());
        double effectiveOtherInputCost = scenario.getOtherInputCost(
            step, P.getOtherInputCostBaseline());
        double effectiveOtherCost = scenario.getOtherCost(
            step, P.getOtherCostPerUnit());

        fertilizerCostThisStep = fertilizerIntensity * effectiveFertCost;
        fuelCostThisStep = fuelIntensity * effectiveFuelCost;
        otherInputCostThisStep = effectiveOtherInputCost;
        otherCostThisStep = effectiveOtherCost;
    }
    
    /** Call at each step to refresh production cost for current input prices. */
    public void updateProductionCost(int step) {
        this.productionCost = computeProductionCost(step);
    }

    public void resetStepSales() {
        unitsSoldThisStep = 0;
        revenueThisStep = 0.0;
    }
    
    /** 1 means 100% of original cost (fertilizer or fuel). */
    private static double getFertilizerIntensityFor(String variety) {
        switch (variety) {
        	/** 0.15 would mean 15% of original cost (fertilizer or fuel). */
            case "RED DELICIOUS": return 1; 
            case "FUJI":          return 1;
            default:              return 1;
        }
    }

    		/** 0.10 would mean 10% of original cost (fertilizer or fuel). */
    private static double getFuelIntensityFor(String variety) {
        switch (variety) {
            case "RED DELICIOUS": return 1;
            case "FUJI":          return 1;
            default:              return 1;
        }
    }
    
    public void setMonthlyML(int monthIdx) {
        this.p_ml = ml.getPriceFor(variety, monthIdx);
    }
    
    public void setAskingFromFarmRule(double base, double floor, double ceil) {
        this.askingPrice = MathUtil.clamp(base, floor, ceil);
    }
    
    public void produce() {
        double steps = Math.max(1, P.getSTEPS_PER_YEAR());
        double baselineProduction = annualProductionLbs > 0.0
            ? annualProductionLbs
            : steps * ("RED DELICIOUS".equals(variety) ? 100 : ("FUJI".equals(variety) ? 50 : 50));
        double annualTranslogProduction = TranslogProductionFunction.productionLbs(
            baselineProduction,
            fertilizerCostThisStep,
            fuelCostThisStep,
            otherInputCostThisStep,
            otherCostThisStep,
            P);
        double planned = annualTranslogProduction / steps;
        productionCarryover += planned;
        int rate = (int) Math.floor(productionCarryover);
        productionCarryover -= rate;
        stock += rate;
        money -= productionCost * rate;
        producedThisStep = rate;
    }
    
    public int sellToWholesaler(WholesaleAgent w, int qty, int packMultiple) {
        if (qty <= 0) return 0;
        double bulkPrice = askingPrice;
        int maxAfford = (int) Math.floor(w.getMoney() / Math.max(bulkPrice, 1e-9d));
        int q = Math.max(0, Math.min(Math.min(qty, stock), maxAfford));
        if (packMultiple > 1) q = (q / packMultiple) * packMultiple;
        if (q <= 0) return 0;
        
        stock -= q;
        w.addStock(variety, q);
        w.debit(bulkPrice * q);
        money += bulkPrice * q;
        unitsSoldCum += q;
        unitsSoldThisStep += q;
        double revenue = bulkPrice * q;
        revenueCum += revenue;
        revenueThisStep += revenue;
        return q;
    }
    
    // ----- getters -----
    public int getId() { return id; }
    public String getVariety() { return variety; }
    public double getAcres() { return acres; }
    public double getAnnualProductionLbs() { return annualProductionLbs; }
    public double getMoney() { return money; }
    public int getStock() { return stock; }
    public double getP_ml() { return p_ml; }
    public double getAskingPrice() { return askingPrice; }
    public double getProductionCost() { return productionCost; }
    public int getUnitsSoldCum() { return unitsSoldCum; }
    public double getRevenueCum() { return revenueCum; }
    public int getUnitsSoldThisStep() { return unitsSoldThisStep; }
    public double getRevenueThisStep() { return revenueThisStep; }
    public int getProducedThisStep() { return producedThisStep; }
    public double getFertilizerIntensity() { return fertilizerIntensity; }
    public double getFuelIntensity() { return fuelIntensity; }
}

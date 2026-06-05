package com.example.abm.apple;

/**
 * April 25, 2026
 * Baseline scenario: no shocks. Subclass to implement specific shock patterns.
 */
public class InputCostScenario {
    
    public double getFertilizerCost(int step, double baseline) {
        return baseline;
    }
    
    public double getFuelCost(int step, double baseline) {
        return baseline;
    }

    public double getOtherInputCost(int step, double baseline) {
        return baseline;
    }
    
    public double getOtherCost(int step, double baseline) {
        return baseline;
    }
}

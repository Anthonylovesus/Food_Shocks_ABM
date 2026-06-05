package com.example.abm.apple;

/**
 * Normalized translog production function for fresh apples.
 *
 * The function is anchored to each survey producer's acreage-derived baseline
 * production. At baseline cost-component inputs, all log ratios are zero and
 * production equals the original survey production. Future shocks to the four
 * cost items can move production through first-order share terms and optional
 * second-order interaction terms.
 */
public final class TranslogProductionFunction {

  private TranslogProductionFunction() {}

  public static double productionLbs(
      double baselineProductionLbs,
      double fertilizerCost,
      double fuelCost,
      double otherInputCost,
      double otherCost,
      ModelParams p) {

    if (!p.isUseTranslogProductionFunction() || baselineProductionLbs <= 0.0) {
      return Math.max(0.0, baselineProductionLbs);
    }

    double[] logRatios = new double[] {
        logRatio(fertilizerCost, p.getFertilizerCostBaseline()),
        logRatio(fuelCost, p.getFuelCostBaseline()),
        logRatio(otherInputCost, p.getOtherInputCostBaseline()),
        logRatio(otherCost, p.getOtherCostPerUnit())
    };

    double[] alpha = new double[] {
        p.getNormalizedFertilizerCostShare(),
        p.getNormalizedFuelCostShare(),
        p.getNormalizedOtherInputCostShare(),
        p.getNormalizedOtherCostShare()
    };

    double logMultiplier = 0.0;
    for (int i = 0; i < logRatios.length; i++) {
      logMultiplier += alpha[i] * logRatios[i];
    }

    double selfInteraction = p.getTranslogSelfInteraction();
    for (int i = 0; i < logRatios.length; i++) {
      logMultiplier += 0.5 * selfInteraction * logRatios[i] * logRatios[i];
    }

    double crossInteraction = p.getTranslogCrossInteraction();
    for (int i = 0; i < logRatios.length; i++) {
      for (int j = i + 1; j < logRatios.length; j++) {
        logMultiplier += crossInteraction * logRatios[i] * logRatios[j];
      }
    }

    return Math.max(0.0, baselineProductionLbs * Math.exp(logMultiplier));
  }

  private static double logRatio(double value, double baseline) {
    double safeValue = Math.max(value, 1e-9);
    double safeBaseline = Math.max(baseline, 1e-9);
    return Math.log(safeValue / safeBaseline);
  }
}

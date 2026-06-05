package com.example.abm.wheat.agents;

import com.example.abm.wheat.ModelParams;

import repast.simphony.random.RandomHelper;
import java.util.Map;

public class ConsumerAgent {

  // ---- state (kept public for compatibility with existing usages) ----
  public double money;
  public double willingToPay;        // baseline WTP (before taste premiums)
  public double annualConsumptionLbs;
  public int    subgroup;
  public double unitWeightLbs;
  public boolean acceptSubstitute;
  public double subWtpMultiplier;

  public double unitsPerStep;        // units demanded per simulation step (week)
  private double demandBuffer = 0.0; // to accumulate fractional demand
  public int    wantToBuy = 0;       // integer units wanted this step

  public double moneySpentCum = 0.0; // cumulative spend
  public int    itemsBoughtCum = 0;  // cumulative items purchased

  // variety-specific taste premiums (reductions from baseline WTP), e.g. {"FUJI": 0.15, ...}
  public final Map<String, Double> tastePremium;

  // ---- ctor ----
  public ConsumerAgent(ModelParams P,
                       double wtp, double annualLbs, int subgroup,
                       double unitW, boolean acceptSub, double subMult,
                       Map<String, Double> tastePremium,
                       double wtpJitterSigma, double wtpMin, double wtpMax) {

    // funds
    this.money = P.getInitialFundsConsumers();

    // heterogeneous WTP: lognormal jitter exp(N(0, sigma))
    double jitter = Math.exp(RandomHelper.createNormal(0, wtpJitterSigma).nextDouble());
    this.willingToPay = Math.min(wtpMax, Math.max(wtpMin, wtp * jitter));

    // consumption
    this.annualConsumptionLbs = annualLbs;
    this.subgroup             = subgroup;
    this.unitWeightLbs        = Math.max(unitW, 1e-9);
    this.acceptSubstitute     = acceptSub;
    this.subWtpMultiplier     = subMult;

    this.tastePremium = tastePremium;

    // convert annual lbs to units per step (weekly)
    double unitsPerYear = this.annualConsumptionLbs / this.unitWeightLbs;
    this.unitsPerStep   = unitsPerYear / P.getSTEPS_PER_YEAR();
  }

  // ---- demand by step ----
  /** Accumulate fractional demand and emit an integer number of units to buy this step. */
  public void resetStepDemand() {
    demandBuffer += unitsPerStep;
    int planned = (int) Math.floor(demandBuffer);
    demandBuffer -= planned;
    wantToBuy = planned;
  }

  // ---- willingness-to-pay ----
  /** effective WTP for a specific variety after subtracting the taste premium */
  public double effectiveWTP(String variety) {
    double prem = tastePremium.getOrDefault(variety, 0.0);
    return Math.max(0.0, willingToPay - prem);
  }

  /**afford this variety at the given price? */
  public boolean canAffordVariety(String variety, double price) {
    return (money >= price) && (price <= effectiveWTP(variety));
  }

  // ---- purchase ----
  /**
   * attempt to buy up to {@code qty} units at {@code price}. 
   * returns the number actually bought,
   * and updates money / spend / wantToBuy / itemsBoughtCum accordingly.
   */
  public int buyUnits(double price, int qty) {
    int bought = 0;
    for (int i = 0; i < qty; i++) {
      if (wantToBuy <= 0) break;
      if (money >= price) {
        money         -= price;
        moneySpentCum += price;
        wantToBuy     -= 1;
        itemsBoughtCum += 1;
        bought++;
      } else {
        break;
      }
    }
    return bought;
  }

  // ---- getMoney(), c.credit() c.debit() ----
  public double getMoney() { return money; }

  public void credit(double amount) {
    if (amount < 0) throw new IllegalArgumentException("amount must be >= 0");
    money += amount;
  }

  public void debit(double amount) {
    if (amount < 0) throw new IllegalArgumentException("amount must be >= 0");
    money -= amount;
  }

  // ----  ----
  public double getUnitsPerStep() { return unitsPerStep; }
  
  public int getWantToBuy() { 
	  return wantToBuy; 
	}

	public void setWantToBuy(int v) { 
	  wantToBuy = v; 
	}
	
	public int getItemsBoughtCum() { return itemsBoughtCum; }
	public double getMoneySpentCum() { return moneySpentCum; }
}

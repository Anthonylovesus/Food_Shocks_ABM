package com.example.abm.tomatoes.agents;

import com.example.abm.tomatoes.MLPriceLoader;
import com.example.abm.tomatoes.ModelParams;
import com.example.abm.tomatoes.MathUtil;

public class ProducerAgent {

  private final int id;
  private final String variety;

  private double money;
  private int stock;
  private double p_ml;          // monthly ML (RF) baseline price
  private double askingPrice;   // producer ask price
  private double productionCost;

  private int unitsSoldCum = 0;
  private double revenueCum = 0;
  private int producedThisStep = 0;

  private final ModelParams P;
  private final MLPriceLoader ml;

  public ProducerAgent(int id, String variety, ModelParams P, MLPriceLoader ml) {
    this.id = id;
    this.variety = variety;
    this.P = P;
    this.ml = ml;

    // private fields with @Parameter
    this.money = P.getInitialFundsProducers();
    this.stock = P.getInitialStockProducers();

    this.p_ml = ml.getPriceFor(variety, 0);
    this.askingPrice = p_ml;

    // variety-based unit cost (check literature)
    this.productionCost =
        "ROMA".equals(variety) ? 0.35 : 0.4;
  }

  public void setMonthlyML(int monthIdx) {
    this.p_ml = ml.getPriceFor(variety, monthIdx);
  }

  public void setAskingFromFarmRule(double base, double floor, double ceil) {
    this.askingPrice = MathUtil.clamp(base, floor, ceil);
  }

  public void produce() {
    int rate =
        "ROMA".equals(variety) ? 34 : 66;

    stock += rate;
    money -= productionCost * rate;
    producedThisStep = rate;
  }

  public int sellToWholesaler(WholesaleAgent w, int qty, int packMultiple) {
    if (qty <= 0) return 0;

    double bulkPrice = askingPrice * 0.90;
    int maxAfford = (int) Math.floor(w.getMoney() / Math.max(bulkPrice, 1e-9d));
    int q = Math.max(0, Math.min(Math.min(qty, stock), maxAfford));

    if (packMultiple > 1) q = (q / packMultiple) * packMultiple;
    if (q <= 0) return 0;

    // stock and cash 
    stock -= q;
    w.addStock(variety, q);
    w.debit(bulkPrice * q);

    money += bulkPrice * q;
    unitsSoldCum += q;
    revenueCum += bulkPrice * q;
    return q;
  }

  // ----- getters -----
  public int getId() { return id; }
  public String getVariety() { return variety; }
  public double getMoney() { return money; }
  public int getStock() { return stock; }
  public double getP_ml() { return p_ml; }
  public double getAskingPrice() { return askingPrice; }
  public double getProductionCost() { return productionCost; }
  public int getUnitsSoldCum() { return unitsSoldCum; }
  public double getRevenueCum() { return revenueCum; }
  public int getProducedThisStep() { return producedThisStep; }
}

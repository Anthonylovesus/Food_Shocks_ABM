package com.example.abm.apple.processed.agents;

import com.example.abm.apple.processed.ModelParams;
import com.example.abm.apple.processed.MathUtil;
import java.util.*;

public class RetailAgent {

  private double money;
  private final Map<String, Integer> invByVar = new HashMap<>();
  private final Map<String, Double>  priceByVar = new HashMap<>();
  private final Map<String, Integer> soldByVar = new HashMap<>();

  private double coverWeeksTarget = 2.0;
  private double reorderPointWeeks = 1.0;

  private final ModelParams P;

  public RetailAgent(ModelParams P, List<String> varieties) {
    this.P = P;
    this.money = P.getInitialFundsRetailers();

    int per = Math.max(1, P.getInitialStockRetailers() / Math.max(1, varieties.size()));
    for (String v : varieties) {
      invByVar.put(v, per);
      priceByVar.put(v, 1.85);
      soldByVar.put(v, 0);
    }
  }

  /** update retail prices by smoothing toward wholesale ask*(1 + retail markup) */
  public void updateRetailPrices(WholesaleAgent w) {
    for (String v : invByVar.keySet()) {
      double ask    = w.getAskPrice(v); 
      double target = ask * (1.0 + P.getRETAIL_MARKUP());
      double prev   = priceByVar.getOrDefault(v, target);
      double sm     = 0.5 * prev + 0.5 * target;

      priceByVar.put(v, MathUtil.clamp(sm, P.getPRICE_FLOOR(), P.getPRICE_CEIL()));
    }
  }

  /** order up to coverWeeksTarget if below reorderPointWeeks. */
  public int computeOrderQty(String variety, int expectedWeeklyDemand) {
    int target   = Math.max(0, (int) Math.floor(coverWeeksTarget * Math.max(0, expectedWeeklyDemand)));
    int current  = invByVar.getOrDefault(variety, 0);
    double cover = current / Math.max(expectedWeeklyDemand, 1e-9d);
    if (cover <= reorderPointWeeks) return Math.max(0, target - current);
    return 0;
  }

  /** sell to a consumer by variety priority, WTP, inventory, and cash */
  public void sellToConsumer(ConsumerAgent c, List<String> priority) {
    int remaining = c.getWantToBuy();
    for (String v : priority) {
      if (remaining <= 0) break;

      double price = priceByVar.getOrDefault(v, 999.0);
      if (!c.canAffordVariety(v, price)) continue;

      int inv = invByVar.getOrDefault(v, 0);
      if (inv <= 0) continue;

      int maxByCash = (int) Math.floor(c.getMoney() / Math.max(price, 1e-9d));
      int qty       = Math.min(inv, Math.min(remaining, maxByCash));
      if (qty <= 0) continue;

      int bought = c.buyUnits(price, qty);
      if (bought > 0) {
        invByVar.put(v, inv - bought);
        money += price * bought;
        soldByVar.put(v, soldByVar.getOrDefault(v, 0) + bought);
        remaining = c.getWantToBuy();
      }
    }
  }

  /** update inventory for a given variety */
  public void addInventory(String variety, int q) {
    if (q <= 0) return;
    invByVar.put(variety, invByVar.getOrDefault(variety, 0) + q);
  }

  // -------- --------
  public double getMoney() { return money; }
  public Map<String, Integer> getInvByVarView() { return Collections.unmodifiableMap(invByVar); }
  public Map<String, Double>  getPriceByVarView() { return Collections.unmodifiableMap(priceByVar); }
  public Map<String, Integer> getSoldByVarView() { return Collections.unmodifiableMap(soldByVar); }

  public double getCoverWeeksTarget() { return coverWeeksTarget; }
  public void setCoverWeeksTarget(double v) { this.coverWeeksTarget = v; }

  public double getReorderPointWeeks() { return reorderPointWeeks; }
  public void setReorderPointWeeks(double v) { this.reorderPointWeeks = v; }
  
  public void debit(double amt) { money -= amt; }
}

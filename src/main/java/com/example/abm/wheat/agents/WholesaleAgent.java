package com.example.abm.wheat.agents;

import com.example.abm.wheat.ModelParams;
import com.example.abm.wheat.MathUtil;

import java.util.*;

/** wholesale agent: money, per-variety stock, ask prices, and an order book. */
public class WholesaleAgent {

  // -------- P, varieties --------
  private final ModelParams P;
  private final List<String> varieties;

  // -------- state --------
  private double money;
  private final Map<String,Integer> stockByVar = new HashMap<>();
  private final Map<String,Double>  askByVar   = new HashMap<>();
  private int stockTotal = 0;

  private static final class Order {
    final RetailAgent retailer;
    int qty;
    Order(RetailAgent r, int q){ this.retailer = r; this.qty = q; }
  }
  private final Map<String,List<Order>> orders = new HashMap<>();

  // policy
  private final double targetCoverWeeks = 3.0;
  private final double minMarginFrac    = 0.02;

  /** constructor expected by AppleMarketBuilder: WholesaleAgent(P, VARIETIES) */
  public WholesaleAgent(ModelParams P, List<String> varieties) {
    this.P = P;
    this.varieties = new ArrayList<>(varieties);
    this.money = P.getInitialFundsWholesalers();

    int per = Math.max(0, P.getInitialStockWholesalers() / Math.max(1, varieties.size()));
    for (String v : varieties) {
      stockByVar.put(v, per);
      askByVar.put(v, 0.7);
      orders.put(v, new ArrayList<>());
      stockTotal += per;
    }
  }

  // -------- used by WheatMarketBuilder --------

  /** init asks from producer bulk costs and a provided markup */
  public void initializePricesFromProducers(List<ProducerAgent> producers,
                                            Map<String,Double> markupByVar) {
    for (String v : varieties) {
      double bulk = estimateBulkCost(producers, v);
      double mu   = markupByVar.getOrDefault(v, 0.10);
      double tgt  = bulk > 0 ? bulk * (1.0 + mu) : 0.7;
      askByVar.put(v, MathUtil.clamp(tgt, P.getPRICE_FLOOR(), P.getPRICE_CEIL()));
    }
  }

  /** smooth asks toward bulk*(1+markup) */
  public void updateWholesalePrices(List<ProducerAgent> producers,
                                    Map<String,Double> markupByVar) {
    double alpha = P.getWHOLESALE_PRICE_ALPHA();
    for (String v : varieties) {
      double bulk = estimateBulkCost(producers, v);
      double mu   = markupByVar.getOrDefault(v, 0.10);
      double tgt  = bulk > 0 ? bulk * (1.0 + mu) : askByVar.getOrDefault(v, 0.7);
      double prev = askByVar.getOrDefault(v, tgt);
      double sm   = (1 - alpha) * prev + alpha * tgt;
      askByVar.put(v, MathUtil.clamp(sm, P.getPRICE_FLOOR(), P.getPRICE_CEIL()));
    }
  }

  /** retailer places an order */
 public void receiveOrder(RetailAgent retailer, int qty, String variety) {
    if (qty <= 0) return;
    orders.computeIfAbsent(variety, k -> new ArrayList<>()).add(new Order(retailer, qty));
 }

  /** purchase from producers if margin to retail is sufficient */
  /** Feb 3, 2026 */
public void procure(List<ProducerAgent> producers,
          Map<String,Integer> expectedWeeklyDemandByVar,
          Map<String,Double> retailPriceByVar,
          Map<String,Double> markupByVar) {

	double yld = MathUtil.clamp(P.getWHEAT_TO_FLOUR_YIELD(), 1e-6, 1.0);

	for (String v : varieties) {
		int backlog = orders.getOrDefault(v, Collections.emptyList())
             .stream().mapToInt(o -> o.qty).sum();

		int expected = Math.max(0, expectedWeeklyDemandByVar.getOrDefault(v, 0));
		int target   = (int) Math.floor(targetCoverWeeks * expected);

		int have     = stockByVar.getOrDefault(v, 0);            // end product inventory
		int flourNeed = Math.max(0, target + backlog - have);    // end product units needed
		if (flourNeed <= 0) continue;

		// ---- input/output conversion ----
		int wheatNeed = (int) Math.ceil(flourNeed / yld);        // input product needed

		double bulk   = estimateBulkCost(producers, v);          // call estimate bulk wheat cost function
		double retail = retailPriceByVar.getOrDefault(v, 1.85);
		double wantMu = markupByVar.getOrDefault(v, 0.10);
		double marginVsBulk = (retail - bulk) / Math.max(bulk, 1e-9);

		if (marginVsBulk < (minMarginFrac + wantMu / 2.0)) continue;

		int remainingWheat = wheatNeed;

		for (ProducerAgent p : producers) {
			if (!v.equals(p.getVariety())) continue;
			if (remainingWheat <= 0) break;

			// buy wheat (input)
			int boughtWheat = p.sellToWholesaler(this, remainingWheat, /*packMultiple*/10);

			if (boughtWheat > 0) {
				// convert to flour (output)
				int flourMade = (int) Math.floor(boughtWheat * yld);

				if (flourMade > 0) addStock(v, flourMade);  // stockByVar is output product
				remainingWheat -= boughtWheat;
				
				// track food loss/food waste
				// int loss = boughtWheat - flourMade;
			}
		}
	}
}

  /** ship to retailers and collect payment */
  public void fulfill() {
    for (String v : varieties) {
      List<Order> list  = orders.getOrDefault(v, new ArrayList<>());
      List<Order> carry = new ArrayList<>();
      double ask = askByVar.getOrDefault(v, 0.7);
      int have   = stockByVar.getOrDefault(v, 0);

      for (Order o : list) {
        if (o.qty <= 0) continue;
        int byInv  = Math.min(o.qty, have);
        int byCash = (int)Math.floor(o.retailer.getMoney() / Math.max(ask, 1e-9));
        int ship   = Math.max(0, Math.min(byInv, byCash));
        if (ship > 0) {
          have -= ship;
          o.retailer.debit(ask * ship);
          credit(ask * ship);
          o.retailer.addInventory(v, ship);
          o.qty -= ship;
        }
        if (o.qty > 0) carry.add(o);
      }
      stockByVar.put(v, have);
      orders.put(v, carry);
      recalcTotalStock();
    }
  }

  // -------- adjust stock --------

  public double getAskPrice(String variety) { return askByVar.getOrDefault(variety, 0.7); }
  public double getMoney() { return money; }
  public void credit(double amt) { money += amt; }
  public void debit(double amt)  { money -= amt; }
  public void addStock(String variety, int q) {
    if (q <= 0) return;
    stockByVar.put(variety, stockByVar.getOrDefault(variety, 0) + q);
    recalcTotalStock();
  }
  public int getTotalStock() { return stockTotal; }
  public Map<String,Double> getAskByVarView() { return Collections.unmodifiableMap(askByVar); }

  // -------- estimate bulk cost --------
  // Feb 3, 2026
  private double estimateBulkCost(List<ProducerAgent> producers, String variety) {
	  double sum = 0.0; int n = 0;
	  for (ProducerAgent p : producers) {
	    if (!variety.equals(p.getVariety())) continue;
	    sum += p.getAskingPrice() * 0.90;  // bulk wheat cost
	    n++;
	  }

	  if (n == 0) {
	    double ask = askByVar.getOrDefault(variety, 0.05);
	    return ask / 1.10; // assumes ask is output product price
	  }

	  double bulkWheat = sum / n;
	  double yld = MathUtil.clamp(P.getWHEAT_TO_FLOUR_YIELD(), 1e-6, 1.0);
	  return bulkWheat / yld; // effective bulk cost per output product unit
	}

  private void recalcTotalStock() {
    stockTotal = stockByVar.values().stream().mapToInt(i -> i).sum();
  }
}
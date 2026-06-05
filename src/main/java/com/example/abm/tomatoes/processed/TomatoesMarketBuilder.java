package com.example.abm.tomatoes.processed;

import com.example.abm.tomatoes.processed.agents.*;
import com.example.abm.tomatoes.processed.MathUtil;

import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.engine.schedule.IAction;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

public class TomatoesMarketBuilder extends DefaultContext<Object> implements ContextBuilder<Object> {

  // varieties and purchase priority (March 9, 2026)
  private static final List<String> VARIETIES =
      Arrays.asList("ROMA", "VINE RIPES");
  private static final List<String> PRIORITY =
      Arrays.asList("ROMA", "VINE RIPES");

  // wholesaler markups by variety (March 9, 2026)
  private static final Map<String, Double> WHOLESALE_MARKUP = new HashMap<>();
  static {
    WHOLESALE_MARKUP.put("VINE RIPES", 0.10);
  }

  // taste premiums (March 9, 2026)
  private static final Map<String, Double> TASTE_MEAN = new HashMap<>();
  private static final Map<String, Double> TASTE_STD  = new HashMap<>();
  static {
    TASTE_MEAN.put("VINE RIPES", 0.10);        TASTE_STD.put("VINE RIPES", 0.05);
  }

  // WTP jitter + bounds
  private static final double WTP_JITTER_SIGMA = 0.20;
  private static final double WTP_MIN = 0.70;
  private static final double WTP_MAX = 4.00;

  // state
  private final List<ProducerAgent>  producers  = new ArrayList<>();
  private final List<WholesaleAgent> wholesalers = new ArrayList<>();
  private final List<RetailAgent>    retailers  = new ArrayList<>();
  private final List<ConsumerAgent>  consumers  = new ArrayList<>();
  private final DataTracker          tracker    = new DataTracker();

  // farm price accumulator
  private double monthSupplyAccum = 0.0;
  private double monthDemandAccum = 0.0;
  private int    stepInMonth      = 0;
  private int    monthIdx         = 0;

  // demand/supply anchors
  private double farmQdMonth, farmQsMonth;
  private int    stepsPerMonth;

  // AR(1) shocks (log scale) per variety
  private final Map<String, Double> priceShockLog = new HashMap<>();

  // external data
  private MLPriceLoader ml;
  private ModelParams   P;

  @Override
  public Context build(Context<Object> context) {
    context.setId("TomatoesMarket");

    // parameters object
    Parameters pars = RunEnvironment.getInstance().getParameters();
    P = new ModelParams();

    // use setters — names match @Parameter
    P.setYEARS((Integer) pars.getValue("YEARS"));
    P.setSTEPS_PER_YEAR((Integer) pars.getValue("STEPS_PER_YEAR"));

    P.setInitialFundsProducers((Double) pars.getValue("initial_funds_producers"));
    P.setInitialFundsWholesalers((Double) pars.getValue("initial_funds_wholesalers"));
    P.setInitialFundsRetailers((Double) pars.getValue("initial_funds_retailers"));
    P.setInitialFundsConsumers((Double) pars.getValue("initial_funds_consumers"));

    P.setInitialStockProducers((Integer) pars.getValue("initial_stock_producers"));
    P.setInitialStockWholesalers((Integer) pars.getValue("initial_stock_wholesalers"));
    P.setInitialStockRetailers((Integer) pars.getValue("initial_stock_retailers"));

    P.setEPSILON_DEMAND((Double) pars.getValue("EPSILON_DEMAND"));
    P.setPRICE_FLOOR((Double) pars.getValue("PRICE_FLOOR"));
    P.setPRICE_CEIL((Double) pars.getValue("PRICE_CEIL"));
    P.setRATIO_CLAMP_LOW((Double) pars.getValue("RATIO_CLAMP_LOW"));
    P.setRATIO_CLAMP_HIGH((Double) pars.getValue("RATIO_CLAMP_HIGH"));
    P.setML_SMOOTHING_MONTHS((Integer) pars.getValue("ML_SMOOTHING_MONTHS"));

    P.setUSE_FARM_ANCHORS_FOR_PRICE((Boolean) pars.getValue("USE_FARM_ANCHORS_FOR_PRICE"));
    P.setFARM_ANCHOR_BLEND((Double) pars.getValue("FARM_ANCHOR_BLEND"));
    P.setFARM_DEMAND_LB_PER_CAPITA((Double) pars.getValue("FARM_DEMAND_LB_PER_CAPITA"));
    P.setFARM_SUPPLY_LB_PER_CAPITA((Double) pars.getValue("FARM_SUPPLY_LB_PER_CAPITA"));
    P.setPOPULATION_EQUIVALENT((Integer) pars.getValue("POPULATION_EQUIVALENT"));

    P.setPRICE_SEASONAL_AMPL((Double) pars.getValue("PRICE_SEASONAL_AMPL"));
    P.setPRICE_SEASONAL_PHASE((Double) pars.getValue("PRICE_SEASONAL_PHASE"));
    P.setPRICE_AR1_RHO((Double) pars.getValue("PRICE_AR1_RHO"));
    P.setPRICE_AR1_SIGMA((Double) pars.getValue("PRICE_AR1_SIGMA"));
    P.setPRICE_NOISE_CLAMP((Double) pars.getValue("PRICE_NOISE_CLAMP"));

    P.setWHOLESALE_PRICE_ALPHA((Double) pars.getValue("WHOLESALE_PRICE_ALPHA"));
    P.setRETAIL_MARKUP((Double) pars.getValue("RETAIL_MARKUP"));

    P.setML_MONTHLY_PATH((String) pars.getValue("ML_MONTHLY_PATH"));
    P.setCONSUMER_CSV_PATH((String) pars.getValue("CONSUMER_CSV_PATH"));

    stepsPerMonth = Math.max(1, P.getSTEPS_PER_YEAR() / 12);

    // load ML monthly farm prices
    ml = new MLPriceLoader(P.getML_MONTHLY_PATH());

    // agents (March 9, 2026)
    producers.add(new ProducerAgent(1, "VINE RIPES", P, ml));

    wholesalers.add(new WholesaleAgent(P, VARIETIES));
    retailers.add(new RetailAgent(P, VARIETIES));

    // consumers from CSV (or defaults)
    loadConsumers(P.getCONSUMER_CSV_PATH());

    // init wholesale price from producers
    wholesalers.get(0).initializePricesFromProducers(producers, WHOLESALE_MARKUP);

    // demand/supply anchors (per month)
    int popEq = (P.getPOPULATION_EQUIVALENT() > 0) ? P.getPOPULATION_EQUIVALENT() : consumers.size();
    farmQdMonth = (popEq * P.getFARM_DEMAND_LB_PER_CAPITA()) / 12.0;
    farmQsMonth = (popEq * P.getFARM_SUPPLY_LB_PER_CAPITA()) / 12.0;

    // init AR(1) state per variety
    for (String v : VARIETIES) priceShockLog.put(v, 0.0);

    // add to context
    producers.forEach(context::add);
    wholesalers.forEach(context::add);
    retailers.forEach(context::add);
    consumers.forEach(context::add);

    // schedule and write csvs
    ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
    int totalSteps = P.getYEARS() * P.getSTEPS_PER_YEAR();
    schedule.schedule(
    		  ScheduleParameters.createOneTime(totalSteps + 1),
    		  (IAction) () -> TomatoesMarketBuilder.this.writeOutputs()
    		);
    //schedule.schedule(ScheduleParameters.createOneTime(totalSteps + 1), this, "writeOutputs");
    //RunEnvironment.getInstance().endAt(totalSteps);
    
    schedule.schedule(ScheduleParameters.createRepeating(1, 1), this, "step");
    schedule.schedule(ScheduleParameters.createOneTime(totalSteps + 1), this, "writeOutputs");

    return context;
  }
  
  //public void writeOutputs() {
	  // e.g., tracker.writeCSVs("output");
	  //System.out.println("[AppleMarketBuilder] writeOutputs completed.");
	//}
  
  public void writeOutputs() {
	  String outDir = "output";
	  java.io.File dir = new java.io.File(outDir);
	  if (!dir.exists()) dir.mkdirs();

	  System.out.println("[AppleMarketBuilder] Writing CSVs to: " + dir.getAbsolutePath());
	  
	  try {
	    tracker.writeCSVs(outDir);
	  } catch (Exception ex) {
	    System.err.println("[AppleMarketBuilder] tracker.writeCSVs failed:");
	    ex.printStackTrace();
	  }

	  System.out.println("[AppleMarketBuilder] writeOutputs completed.");
	}


  public void step() {
    // weekly consumer income
    final double CONSUMER_MONTHLY_INCOME = 400.0;
    for (ConsumerAgent c : consumers) c.credit(CONSUMER_MONTHLY_INCOME);
    for (ConsumerAgent c : consumers) c.resetStepDemand();

    // production
    for (ProducerAgent p : producers) p.produce();

    // accumulators by month
    double stepSupply = producers.stream().mapToInt(ProducerAgent::getProducedThisStep).sum();

    double stepDemand;
    if (!retailers.isEmpty()) {
      RetailAgent r = retailers.get(0);
      stepDemand = 0.0;
      for (ConsumerAgent c : consumers) {
        boolean canBuyAny = false;
        for (String v : VARIETIES) {
          double priceV = r.getPriceByVarView().getOrDefault(v, 999.0);
          if (c.canAffordVariety(v, priceV)) { canBuyAny = true; break; }
        }
        stepDemand += canBuyAny ? c.getUnitsPerStep() : 0.0;  // requires ConsumerAgent.getUnitsPerStep()
      }
    } else {
      stepDemand = consumers.stream().mapToDouble(ConsumerAgent::getUnitsPerStep).sum();
    }

    monthSupplyAccum += stepSupply;
    monthDemandAccum += stepDemand;
    stepInMonth++;

    // update wholesale and retail prices
    wholesalers.get(0).updateWholesalePrices(producers, WHOLESALE_MARKUP);
    retailers.get(0).updateRetailPrices(wholesalers.get(0));

    // expected weekly demand by variety
    Map<String, Integer> expected = expectedWeeklyDemandByVar();

    // retailers placing orders
    RetailAgent r = retailers.get(0);
    for (String v : VARIETIES) {
      int qty = r.computeOrderQty(v, expected.getOrDefault(v, 0));
      if (qty > 0) wholesalers.get(0).receiveOrder(r, qty, v);
    }

    // wholesaler obtains and fulfills orders
    wholesalers.get(0).procure(
        producers,
        expected,
        r.getPriceByVarView(), 
        WHOLESALE_MARKUP
    );
    wholesalers.get(0).fulfill();

    // retail sales
    for (ConsumerAgent c : consumers) r.sellToConsumer(c, PRIORITY);

    // trackers
    int iter = tracker.iteration.size() + 1;
    tracker.iteration.add(iter);
    tracker.totalItemsBought.add(consumers.stream().mapToInt(c -> c.itemsBoughtCum).sum());
    tracker.consumerSpending.add(consumers.stream().mapToDouble(c -> c.moneySpentCum).sum());
    tracker.wholesalerStock.add(wholesalers.stream().mapToInt(WholesaleAgent::getTotalStock).sum());
    tracker.retailerStock.add(
        retailers.stream()
                 .mapToInt(rx -> rx.getInvByVarView().values().stream().mapToInt(i -> i).sum())
                 .sum()
    );
    tracker.remainingItems.add(producers.stream().mapToInt(ProducerAgent::getStock).sum());

    for (int i = 0; i < producers.size(); i++) {
      tracker.ensureProducer(i + 1);
      tracker.producerPrice.get(i + 1).add(producers.get(i).getAskingPrice());
      tracker.producerUnitsSold.get(i + 1).add(producers.get(i).getUnitsSoldCum());
      tracker.producerRevenue.get(i + 1).add(producers.get(i).getRevenueCum());
    }

    // monthly farm-price updates
    if (stepInMonth >= stepsPerMonth) {
      double obsQs = Math.max(monthSupplyAccum, 1e-6);
      double obsQd = Math.max(monthDemandAccum, 1e-6);
      double Qs, Qd;

      if (P.isUSE_FARM_ANCHORS_FOR_PRICE()) {
        double alpha = Math.max(0.0, Math.min(1.0, P.getFARM_ANCHOR_BLEND()));
        Qs = alpha * farmQsMonth + (1.0 - alpha) * obsQs;
        Qd = alpha * farmQdMonth + (1.0 - alpha) * obsQd;
      } else {
        Qs = obsQs;
        Qd = obsQd;
      }

      double ratio = MathUtil.clamp(Qs / Qd, P.getRATIO_CLAMP_LOW(), P.getRATIO_CLAMP_HIGH());
      double power = 1.0 / Math.max(P.getEPSILON_DEMAND(), 1e-6);

      int mInYear = monthIdx % 12;
      double seasonMult = 1.0 + P.getPRICE_SEASONAL_AMPL() *
          Math.sin(2.0 * Math.PI * (mInYear / 12.0) + P.getPRICE_SEASONAL_PHASE());
      seasonMult = MathUtil.clamp(seasonMult,
          1.0 - P.getPRICE_NOISE_CLAMP(), 1.0 + P.getPRICE_NOISE_CLAMP());

      for (ProducerAgent p : producers) {
        String var = p.getVariety();

        double prev = priceShockLog.getOrDefault(var, 0.0);
        double innov = RandomHelper.createNormal(0, P.getPRICE_AR1_SIGMA()).nextDouble();
        double shockLog = P.getPRICE_AR1_RHO() * prev + innov;
        priceShockLog.put(var, shockLog);

        double shockMult = Math.exp(shockLog);
        shockMult = MathUtil.clamp(shockMult,
            1.0 - P.getPRICE_NOISE_CLAMP(), 1.0 + P.getPRICE_NOISE_CLAMP());

        double mult = MathUtil.clamp(seasonMult * shockMult,
            1.0 - P.getPRICE_NOISE_CLAMP(), 1.0 + P.getPRICE_NOISE_CLAMP());

        double pBase = p.getP_ml() * Math.pow(ratio, power);
        p.setAskingFromFarmRule(pBase * mult, P.getPRICE_FLOOR(), P.getPRICE_CEIL());
      }

      monthIdx++;
      for (ProducerAgent p : producers) p.setMonthlyML(monthIdx);

      monthSupplyAccum = 0.0;
      monthDemandAccum = 0.0;
      stepInMonth = 0;
    }
  }

  private Map<String, Integer> expectedWeeklyDemandByVar() {
    Map<String, Double> raw = new HashMap<>();
    for (String v : VARIETIES) raw.put(v, 0.0);

    RetailAgent r = retailers.get(0);
    for (ConsumerAgent c : consumers) {
      double units = c.getUnitsPerStep() * uniform(0.9, 1.1);
      String chosen = null;
      for (String v : PRIORITY) {
        double p = r.getPriceByVarView().getOrDefault(v, 999.0);
        if (c.canAffordVariety(v, p)) { chosen = v; break; }
      }
      if (chosen != null) raw.put(chosen, raw.get(chosen) + units);
    }
    return raw.entrySet().stream().collect(Collectors.toMap(
        Map.Entry::getKey, e -> (int) Math.floor(e.getValue())
    ));
  }

  private static double uniform(double a, double b) {
    return a + (b - a) * RandomHelper.nextDouble();
  }

  private void loadConsumers(String path) {
    // expected cols are as below:
    // willing_to_pay, annual_consumption_lbs, subgroup, unit_weight_lbs, accept_substitute, sub_wtp_multiplier
    try (BufferedReader br = new BufferedReader(new FileReader(path))) {
      String header = br.readLine(); // discard header
      String line;
      while ((line = br.readLine()) != null) {
        String[] s = line.split(",", -1);
        double wtp  = parseOr(s, 0, 3.0);
        double ann  = parseOr(s, 1, 17.9);
        int    subg = (int) parseOr(s, 2, 1.0);
        double uw   = parseOr(s, 3, 1.0);
        boolean acc = ((int) parseOr(s, 4, 1.0)) == 1;
        double mult = parseOr(s, 5, 0.9);

        Map<String, Double> taste = new HashMap<>();
        for (String v : VARIETIES) {
          double mu = TASTE_MEAN.getOrDefault(v, 0.0);
          double sd = TASTE_STD.getOrDefault(v, 0.0);
          double prem = Math.max(0.0, RandomHelper.createNormal(mu, sd).nextDouble());
          taste.put(v, prem);
        }
        consumers.add(new ConsumerAgent(P, wtp, ann, subg, uw, acc, mult, taste,
            WTP_JITTER_SIGMA, WTP_MIN, WTP_MAX));
      }
    } catch (Exception ex) {
      // implement 200 default consumers if csv not available
      for (int i = 0; i < 200; i++) {
        Map<String, Double> taste = new HashMap<>();
        for (String v : VARIETIES) {
          double mu = TASTE_MEAN.getOrDefault(v, 0.0);
          double sd = TASTE_STD.getOrDefault(v, 0.0);
          double prem = Math.max(0.0, RandomHelper.createNormal(mu, sd).nextDouble());
          taste.put(v, prem);
        }
        consumers.add(new ConsumerAgent(P, 3.0, 17.9, 1, 1.0, true, 0.9, taste,
            WTP_JITTER_SIGMA, WTP_MIN, WTP_MAX));
      }
    }
  }

  private static double parseOr(String[] s, int idx, double def) {
    try { return Double.parseDouble(s[idx]); } catch (Exception e) { return def; }
  }
}

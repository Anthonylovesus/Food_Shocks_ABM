package com.example.abm.apple;

import com.opencsv.CSVReader;
import java.io.FileReader;
import java.util.*;

public class MLPriceLoader {

  // map<stepIndex, Map<VARIETY, price>>
  private final List<String> stepKeys = new ArrayList<>();
  private final Map<String, List<Double>> seriesByVar = new HashMap<>();

  public static final List<String> VARIETIES =
      Arrays.asList(ModelParams.FRESH_APPLES_VARIETY);

  public MLPriceLoader(String path) {
    for (String v : VARIETIES) seriesByVar.put(v, new ArrayList<>());
    try (CSVReader rdr = new CSVReader(new FileReader(path))) {
      String[] row;
      String[] header = rdr.readNext();
      int dateIdx = indexOf(header, "date", 0);
      int meanIdx = indexOf(header, "mean_prediction", -1);
      int modelIdx = indexOf(header, "model", -1);
      int ymIdx = indexOf(header, "year_month", 0);
      int varietyIdx = indexOf(header, "variety", 1);
      int priceIdx = indexOf(header, "price_per_lb", 2);

      if (meanIdx >= 0) {
        List<Double> fresh = seriesByVar.get(ModelParams.FRESH_APPLES_VARIETY);
        while ((row = rdr.readNext()) != null) {
          if (modelIdx >= 0 && !"GradientBoosting".equalsIgnoreCase(row[modelIdx])) continue;
          stepKeys.add(row[dateIdx]);
          fresh.add(Double.parseDouble(row[meanIdx]));
        }
        return;
      }

      Map<String, Map<String, Double>> pivot = new LinkedHashMap<>();
      while ((row = rdr.readNext()) != null) {
        String ym = row[ymIdx];
        String var = row[varietyIdx].toUpperCase();
        double price = Double.parseDouble(row[priceIdx]);
        pivot.computeIfAbsent(ym, k -> new HashMap<>()).put(var, price);
      }
      for (Map.Entry<String, Map<String, Double>> e : pivot.entrySet()) {
        stepKeys.add(e.getKey());
      }
      // fill each variety series and implement row mean if missing
      for (int i = 0; i < stepKeys.size(); i++) {
        String ym = stepKeys.get(i);
        Map<String, Double> rowMap = pivot.get(ym);
        double rowMean = rowMap.values().stream().mapToDouble(d -> d).average().orElse(0.8);
        for (String v : VARIETIES) {
          seriesByVar.get(v).add(rowMap.getOrDefault(v, rowMean));
        }
      }
    } catch (Exception ex) {
      // if file missing, default baselines
      for (String v : VARIETIES) {
        List<Double> s = seriesByVar.get(v);
        for (int i = 0; i < 60; i++) s.add(v.equals("HONEYCRISP") ? 1.10 : (v.equals("FUJI") ? 0.75 : 0.70));
      }
    }
  }

  public double getPriceFor(String variety, int stepIdx) {
    List<Double> s = seriesByVar.get(variety.toUpperCase());
    if (s == null || s.isEmpty()) {
      s = seriesByVar.get(ModelParams.FRESH_APPLES_VARIETY);
    }
    if (s == null || s.isEmpty()) return 0.90;
    return s.get(Math.floorMod(stepIdx, s.size()));
  }

  private static int indexOf(String[] header, String name, int fallback) {
    if (header == null) return fallback;
    for (int i = 0; i < header.length; i++) {
      if (name.equalsIgnoreCase(header[i])) return i;
    }
    return fallback;
  }
}

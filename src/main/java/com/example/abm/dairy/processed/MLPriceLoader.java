package com.example.abm.dairy.processed;

import com.opencsv.CSVReader;
import java.io.FileReader;
import java.util.*;

public class MLPriceLoader {

  // map<monthIndex, Map<VARIETY, price>>
  private final List<String> monthKeys = new ArrayList<>();
  private final Map<String, List<Double>> seriesByVar = new HashMap<>();

  public static final List<String> VARIETIES =
      Arrays.asList("2 PERCENT");

  public MLPriceLoader(String path) {
    for (String v : VARIETIES) seriesByVar.put(v, new ArrayList<>());
    try (CSVReader rdr = new CSVReader(new FileReader(path))) {
      String[] row;
      // header: year_month,variety,price_per_lb
      Map<String, Map<String, Double>> pivot = new LinkedHashMap<>();
      rdr.readNext(); // skip header
      while ((row = rdr.readNext()) != null) {
        String ym = row[0];
        String var = row[1].toUpperCase();
        double price = Double.parseDouble(row[2]);
        pivot.computeIfAbsent(ym, k -> new HashMap<>()).put(var, price);
      }
      for (Map.Entry<String, Map<String, Double>> e : pivot.entrySet()) {
        monthKeys.add(e.getKey());
      }
      // fill each variety series and implement row mean if missing
      for (int i = 0; i < monthKeys.size(); i++) {
        String ym = monthKeys.get(i);
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
        for (int i = 0; i < 60; i++) s.add(0.70);
      }
    }
  }

  public double getPriceFor(String variety, int monthIdx) {
    List<Double> s = seriesByVar.get(variety.toUpperCase());
    if (s == null || s.isEmpty()) return 0.70;
    return s.get(monthIdx % s.size());
  }
}

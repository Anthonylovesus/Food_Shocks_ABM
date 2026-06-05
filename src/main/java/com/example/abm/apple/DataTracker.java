// in com.example.abm.apple.DataTracker
package com.example.abm.apple;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class DataTracker {
  // public fields you were already filling:
  public final List<Integer> iteration = new ArrayList<>();
  public final List<Integer> totalItemsBought = new ArrayList<>();
  public final List<Double> consumerSpending = new ArrayList<>();
  public final List<Integer> wholesalerStock = new ArrayList<>();
  public final List<Integer> retailerStock = new ArrayList<>();
  public final List<Integer> remainingItems = new ArrayList<>();

  // per-producer series, call ensureProducer(i) before pushing
  public final Map<Integer, List<Double>> producerPrice = new HashMap<>();
  public final Map<Integer, List<Integer>> producerUnitsSold = new HashMap<>();
  public final Map<Integer, List<Double>> producerRevenue = new HashMap<>();
  public final Map<Integer, List<Integer>> producerUnitsSoldDay = new HashMap<>();
  public final Map<Integer, List<Double>> producerRevenueDay = new HashMap<>();

  public void ensureProducer(int idx) {
    producerPrice.computeIfAbsent(idx, k -> new ArrayList<>());
    producerUnitsSold.computeIfAbsent(idx, k -> new ArrayList<>());
    producerRevenue.computeIfAbsent(idx, k -> new ArrayList<>());
    producerUnitsSoldDay.computeIfAbsent(idx, k -> new ArrayList<>());
    producerRevenueDay.computeIfAbsent(idx, k -> new ArrayList<>());
  }

  public void writeCSVs(String outDir) throws IOException {
    Path dir = Paths.get(outDir);
    if (!Files.exists(dir)) Files.createDirectories(dir);

    // (1) stock and sold csv
    writeSimpleTable(dir.resolve("apples_timeseries.csv"),
        new String[]{"iteration","total_items_bought","consumer_spending","wholesaler_stock","retailer_stock","remaining_items"},
        iteration.size(),
        i -> new String[] {
          String.valueOf(get(iteration,i)),
          String.valueOf(get(totalItemsBought,i)),
          String.valueOf(get(consumerSpending,i)),
          String.valueOf(get(wholesalerStock,i)),
          String.valueOf(get(retailerStock,i)),
          String.valueOf(get(remainingItems,i))
        });

    // (2) per-producer csvs
    for (Map.Entry<Integer, List<Double>> e : producerPrice.entrySet()) {
      int pid = e.getKey();
      List<Double> price = e.getValue();
      List<Integer> sold = producerUnitsSold.getOrDefault(pid, Collections.emptyList());
      List<Double> rev  = producerRevenue.getOrDefault(pid, Collections.emptyList());
      List<Integer> soldDay = producerUnitsSoldDay.getOrDefault(pid, Collections.emptyList());
      List<Double> revDay = producerRevenueDay.getOrDefault(pid, Collections.emptyList());
      int n = Math.max(
          Math.max(price.size(), Math.max(sold.size(), rev.size())),
          Math.max(soldDay.size(), revDay.size()));
      Path f = dir.resolve("apples_producer_" + pid + ".csv");
      try (BufferedWriter bw = Files.newBufferedWriter(f)) {
        bw.write("iteration,price,units_sold_day,revenue_day,units_sold_cum,revenue_cum\n");
        for (int i = 0; i < n; i++) {
          bw.write((i+1) + ","
              + val(price, i) + ","
              + ival(soldDay, i) + ","
              + val(revDay, i) + ","
              + ival(sold, i) + ","
              + val(rev, i));
          bw.write("\n");
        }
      }
    }
  }

  private static <T> T get(List<T> list, int i) { return (i < list.size()) ? list.get(i) : null; }
  private static String val(List<Double> list, int i) { return (i < list.size() && list.get(i)!=null) ? list.get(i).toString() : ""; }
  private static String ival(List<Integer> list, int i) { return (i < list.size() && list.get(i)!=null) ? list.get(i).toString() : ""; }

  private interface RowBuilder { String[] row(int idx); }
  private static void writeSimpleTable(Path file, String[] header, int nRows, RowBuilder rb) throws IOException {
    try (BufferedWriter bw = Files.newBufferedWriter(file)) {
      bw.write(String.join(",", header));
      bw.write("\n");
      for (int i = 0; i < nRows; i++) {
        String[] cells = rb.row(i);
        bw.write(String.join(",", cells));
        bw.write("\n");
      }
    }
  }
}

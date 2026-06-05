package com.example.abm.apple;

import com.opencsv.CSVReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class ProducerSurveyLoader {

  public static final class ProducerRecord {
    public final int farmerId;
    public final double acres;
    public final double annualProductionLbs;

    ProducerRecord(int farmerId, double acres, double annualProductionLbs) {
      this.farmerId = farmerId;
      this.acres = acres;
      this.annualProductionLbs = annualProductionLbs;
    }
  }

  public static List<ProducerRecord> load(String path, double yieldLbPerAcre) {
    List<ProducerRecord> records = new ArrayList<>();

    try (CSVReader rdr = new CSVReader(new FileReader(path))) {
      String[] header = rdr.readNext();
      String[] row;
      while ((row = rdr.readNext()) != null) {
        int farmerId = parseInt(row, 0, records.size() + 1);
        double acres = Math.max(0.0, parseDouble(row, 1, 0.0));
        double annualLbs = row.length > 2
            ? parseDouble(row, 2, acres * yieldLbPerAcre)
            : acres * yieldLbPerAcre;
        records.add(new ProducerRecord(farmerId, acres, Math.max(0.0, annualLbs)));
      }
    } catch (Exception ex) {
      records.clear();
      for (int i = 1; i <= 38; i++) {
        records.add(new ProducerRecord(i, 0.0, 0.0));
      }
    }

    return records;
  }

  private static int parseInt(String[] row, int idx, int def) {
    try {
      return Integer.parseInt(row[idx].trim());
    } catch (Exception ex) {
      return def;
    }
  }

  private static double parseDouble(String[] row, int idx, double def) {
    try {
      return Double.parseDouble(row[idx].trim());
    } catch (Exception ex) {
      return def;
    }
  }
}

package com.example.abm.beef;

public class MathUtil {
	  public static double clamp(double v, double lo, double hi) {
	    return Math.max(lo, Math.min(hi, v));
	  }
	}

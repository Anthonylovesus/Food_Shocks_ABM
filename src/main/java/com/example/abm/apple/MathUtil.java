package com.example.abm.apple;

public class MathUtil {
	  public static double clamp(double v, double lo, double hi) {
	    return Math.max(lo, Math.min(hi, v));
	  }
	}

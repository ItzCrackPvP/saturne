package me.cprox.practice.util.external;

public class EloCalculator {

  public static double[] getEstimations(double rankingA, double rankingB) {
    double[] ret = new double[2];
    double estA = 1.0 / (1.0 + Math.pow(10.0, (rankingB - rankingA) / 400.0));
    double estB = 1.0 / (1.0 + Math.pow(10.0, (rankingA - rankingB) / 400.0));
    ret[0] = estA;
    ret[1] = estB;
    return ret;
  }

  public static int getConstant(int ranking) {
    if (ranking < 1000) {
      return 32;
    }
    if (ranking < 1401) {
      return 24;
    }
    return 16;
  }

  public static int[] getNewRankings(int rankingA, int rankingB, boolean victoryA) {
    int[] ret = new int[2];
    double[] ests = getEstimations(rankingA, rankingB);
    int newRankA = (int) (rankingA + getConstant(rankingA) * ((victoryA ? 1 : 0) - ests[0]));
    ret[0] = Math.round((float) newRankA);
    ret[1] = Math.round((float) (rankingB - (newRankA - rankingA)));
    return ret;
  }

}
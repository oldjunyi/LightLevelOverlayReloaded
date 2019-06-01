package com.mmyzd.llor.displaymode.datatype;

public class TextureCoordinates {

  private static final double EPSILON = 1.0 / 65536;
  
  private final double minU;
  private final double minV;
  private final double maxU;
  private final double maxV;

  public TextureCoordinates(double minU, double minV, double maxU, double maxV) {
    this.minU = minU;
    this.minV = minV;
    this.maxU = maxU;
    this.maxV = maxV;
  }
  
  public TextureCoordinates withCorrection() {
    return new TextureCoordinates(minU + EPSILON, minV + EPSILON, maxU - EPSILON, maxV - EPSILON);
  }

  public double getMinU() {
    return minU;
  }

  public double getMinV() {
    return minV;
  }

  public double getMaxU() {
    return maxU;
  }

  public double getMaxV() {
    return maxV;
  }

  public double getU(int index) {
    return index < 2 ? minU : maxU;
  }

  public double getV(int index) {
    return index == 0 || index == 3 ? minV : maxV;
  }
}

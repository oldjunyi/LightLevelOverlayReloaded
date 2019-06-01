package com.mmyzd.llor.displaymode.datatype;

public class LightRange {

  public static final int UPPER_BOUND = 16;
  public static final LightRange FULL = new LightRange(0, UPPER_BOUND - 1);

  private final int minLight;
  private final int maxLight;

  public LightRange(int minLight, int maxLight) {
    this.minLight = minLight;
    this.maxLight = maxLight;
  }

  public int getMinLight() {
    return minLight;
  }

  public int getMaxLight() {
    return maxLight;
  }

  public LightRange union(LightRange other) {
    return new LightRange(Math.min(minLight, other.minLight), Math.max(maxLight, other.maxLight));
  }

  public LightRange intersect(LightRange other) {
    return new LightRange(Math.max(minLight, other.minLight), Math.min(maxLight, other.maxLight));
  }

  public boolean isEmpty() {
    return minLight > maxLight;
  }
}

package com.mmyzd.llor.overlay;

import net.minecraft.util.math.BlockPos;

public class Overlay {

  private final BlockPos pos;
  private final int x, z;
  private final double y;
  private final int blockLight;
  private final int skyLight;
  private final int sunLight;

  public BlockPos getPos() {
    return pos;
  }

  public int getX(int index) {
    return index < 2 ? x : x + 1;
  }

  public int getZ(int index) {
    return index == 0 || index == 3 ? z : z + 1;
  }

  public double getY(int index) {
    return y;
  }

  public int getBlockLight() {
    return blockLight;
  }

  public int getSkyLight() {
    return skyLight;
  }

  public int getSunLight() {
    return sunLight;
  }

  public Overlay(Builder builder) {
    pos = builder.pos;
    x = builder.x;
    z = builder.z;
    y = builder.y;
    blockLight = builder.blockLight;
    skyLight = builder.skyLight;
    sunLight = builder.sunLight;
  }

  public static class Builder {

    private BlockPos pos;
    private int x, z;
    private double y;
    private int blockLight;
    private int skyLight;
    private int sunLight;

    public Builder() {}
    
    public void setPos(BlockPos pos) {
      this.pos = pos;
    }

    public void setX(int x) {
      this.x = x;
    }

    public void setZ(int z) {
      this.z = z;
    }

    public void setY(double y) {
      this.y = y;
    }

    public void setBlockLight(int blockLight) {
      this.blockLight = blockLight;
    }

    public void setSkyLight(int skyLight) {
      this.skyLight = skyLight;
    }

    public void setSunLight(int sunLight) {
      this.sunLight = sunLight;
    }

    public Overlay build() {
      return new Overlay(this);
    }
  }
}

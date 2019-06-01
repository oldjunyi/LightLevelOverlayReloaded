package com.mmyzd.llor.displaymode.datatype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;

public class LightConstraints {

  public static final LightConstraints EMPTY = new LightConstraints();
  private final LightRange[] ranges;

  public LightConstraints() {
    ranges = new LightRange[LightType.INDEX_UPPER_BOUND];
    Arrays.fill(ranges, LightRange.FULL);
  }

  public LightConstraints(LightConstraints other) {
    this.ranges = other.ranges.clone();
  }

  public LightConstraints add(LightType type, LightRange range) {
    int index = type.getIndex();
    LightConstraints result = new LightConstraints(this);
    result.ranges[index] = ranges[index].intersect(range);
    return result;
  }

  public LightConstraints update(Consumer<LightRange[]> consumer) {
    LightConstraints result = new LightConstraints(this);
    consumer.accept(result.ranges);
    return result;
  }

  public LightRange getLightRange(LightType type) {
    return ranges[type.getIndex()];
  }

  public int getMinLight(LightType type) {
    return ranges[type.getIndex()].getMinLight();
  }

  public int getMaxLight(LightType type) {
    return ranges[type.getIndex()].getMaxLight();
  }

  public boolean hasEmptyRange() {
    for (LightRange range : ranges) {
      if (range.isEmpty()) {
        return true;
      }
    }
    return false;
  }

  public ArrayList<LightConstraints> canonical() {
    ArrayList<LightConstraints> result = new ArrayList<LightConstraints>();
    if (hasEmptyRange()) {
      return result;
    }

    LightRange sunLightRange = ranges[LightType.SUN.getIndex()];
    LightRange blockLightRange = ranges[LightType.BLOCK.getIndex()];
    LightRange mixedLightRange = ranges[LightType.MIXED.getIndex()];

    LightConstraints majorConstraints = update(ranges -> {
      ranges[LightType.BLOCK.getIndex()] = blockLightRange.intersect(mixedLightRange);
      ranges[LightType.SUN.getIndex()] =
          sunLightRange.intersect(new LightRange(0, mixedLightRange.getMaxLight()));
    });
    LightConstraints minorConstraints = update(ranges -> {
      ranges[LightType.BLOCK.getIndex()] =
          blockLightRange.intersect(new LightRange(0, mixedLightRange.getMinLight() - 1));
      ranges[LightType.SUN.getIndex()] = sunLightRange.intersect(mixedLightRange);
    });

    if (!majorConstraints.hasEmptyRange()) {
      result.add(majorConstraints);
    }
    if (!minorConstraints.hasEmptyRange()) {
      result.add(minorConstraints);
    }
    return result;
  }
}

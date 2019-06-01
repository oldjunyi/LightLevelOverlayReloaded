package com.mmyzd.llor.displaymode.datatype;

import java.util.Map;
import java.util.stream.Stream;
import com.google.common.collect.ImmutableMap;

public class LightType {

  public static final LightType BLOCK = new LightType("block_light", 0);
  public static final LightType SKY = new LightType("sky_light", 1);
  public static final LightType SUN = new LightType("sun_light", 2);
  public static final LightType MIXED = new LightType("mixed_light", 3);
  public static final int INDEX_UPPER_BOUND = 4;

  private static final Map<String, LightType> TABLE = Stream.of(BLOCK, SKY, SUN, MIXED)
      .collect(ImmutableMap.toImmutableMap(type -> type.name, type -> type));

  private final String name;
  private final int index;

  private LightType(String name, int index) {
    this.name = name;
    this.index = index;
  }

  public static LightType of(String name) {
    return TABLE.get(name);
  }

  public String getName() {
    return name;
  }

  public int getIndex() {
    return index;
  }
}

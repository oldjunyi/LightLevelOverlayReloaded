package com.mmyzd.llor.displaymode.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.mmyzd.llor.displaymode.datatype.LightConstraints;
import com.mmyzd.llor.displaymode.datatype.LightRange;
import com.mmyzd.llor.displaymode.datatype.LightType;
import com.mmyzd.llor.displaymode.datatype.TextureIndex;
import com.mmyzd.llor.displaymode.exception.LightSelectorFormatException;

public class LightLevelNode extends DataNode {

  private static final String LIGHT_NUMBERS_SEPARATOR_REGEX = "\\s*\\.\\.\\s*|(?<=\\w)\\s*-\\s*";
  private static final String LIGHT_COMPONENTS_SEPARATOR_REGEX = "\\s*,\\s*";

  private ArrayList<Entry> entries = new ArrayList<Entry>();

  public void visit(LightConstraints constraints, LightType type,
      BiConsumer<LightConstraints, TextureIndex> consumer) {
    for (Entry entry : entries) {
      LightMappingNode node = entry.getLightMappingNode();
      for (LightRange range : entry.getRanges()) {
        node.visit(constraints.add(type, range), consumer);
      }
    }
  }

  @Override
  public void writeToJson(JsonWriter writer) throws IOException {
    writer.beginObject();
    for (Entry entry : entries) {
      StringBuilder propertyNameBuilder = new StringBuilder();
      for (LightRange range : entry.getRanges()) {
        if (propertyNameBuilder.length() > 0) {
          propertyNameBuilder.append(", ");
        }
        propertyNameBuilder.append(range.getMinLight());
        if (range.getMinLight() != range.getMaxLight()) {
          propertyNameBuilder.append("..");
          propertyNameBuilder.append(range.getMaxLight());
        }
      }
      writer.name(propertyNameBuilder.toString());

      LightMappingNode node = entry.getLightMappingNode();
      node.writeToJson(writer);
    }
    writer.endObject();
  }

  @Override
  public void readFromJson(JsonReader reader) throws IOException {
    ArrayList<Entry> entries = new ArrayList<Entry>();
    reader.beginObject();
    while (reader.peek() != JsonToken.END_OBJECT) {
      LightRange[] ranges = readLightRangesFromJson(reader);
      LightMappingNode node = new LightMappingNode();
      node.readFromJson(reader);
      entries.add(new Entry(ranges, node));
    }
    reader.endObject();
    this.entries = entries;
  }

  private LightRange[] readLightRangesFromJson(JsonReader reader) throws IOException {
    boolean[] lightSelected = new boolean[LightRange.UPPER_BOUND];
    String propertyName = reader.nextName();
    for (String component : propertyName.split(LIGHT_COMPONENTS_SEPARATOR_REGEX)) {
      String[] numbers = component.split(LIGHT_NUMBERS_SEPARATOR_REGEX);
      if (numbers.length < 1 || numbers.length > 2) {
        throw new LightSelectorFormatException(propertyName, reader);
      }

      try {
        int minLight = Integer.parseInt(numbers[0]);
        int maxLight = numbers.length == 1 ? minLight : Integer.parseInt(numbers[1]);
        minLight = Math.max(minLight, LightRange.FULL.getMinLight());
        maxLight = Math.min(maxLight, LightRange.FULL.getMaxLight());
        for (int light = minLight; light <= maxLight; ++light) {
          lightSelected[light] = true;
        }
      } catch (NumberFormatException exception) {
        throw new LightSelectorFormatException(propertyName, reader);
      }
    }

    ArrayList<LightRange> ranges = new ArrayList<LightRange>();
    for (int light = 0; light < LightRange.UPPER_BOUND; ++light) {
      if (lightSelected[light]) {
        int minLight = light;
        while (light + 1 < LightRange.UPPER_BOUND && lightSelected[light + 1])
          ++light;
        int maxLight = light;
        ranges.add(new LightRange(minLight, maxLight));
      }
    }
    return ranges.toArray(new LightRange[ranges.size()]);
  }

  public static class Entry {

    private final LightRange[] ranges;
    private final LightMappingNode node;

    public Entry(LightRange[] ranges, LightMappingNode node) {
      this.ranges = ranges;
      this.node = node;
    }

    public LightRange[] getRanges() {
      return ranges;
    }

    public LightMappingNode getLightMappingNode() {
      return node;
    }
  }
}

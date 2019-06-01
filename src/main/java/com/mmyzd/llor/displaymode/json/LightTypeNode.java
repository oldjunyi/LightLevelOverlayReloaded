package com.mmyzd.llor.displaymode.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.mmyzd.llor.displaymode.datatype.LightConstraints;
import com.mmyzd.llor.displaymode.datatype.LightType;
import com.mmyzd.llor.displaymode.datatype.TextureIndex;
import com.mmyzd.llor.displaymode.exception.UnknownPropertyNameException;

public class LightTypeNode extends DataNode {

  private ArrayList<Entry> entries = new ArrayList<Entry>();

  public void visit(LightConstraints constraints,
      BiConsumer<LightConstraints, TextureIndex> consumer) {
    for (Entry entry : entries) {
      LightType type = entry.getType();
      LightLevelNode node = entry.getLightLevelNode();
      node.visit(constraints, type, consumer);
    }
  }

  @Override
  public final void writeToJson(JsonWriter writer) throws IOException {
    writer.beginObject();
    writePropertiesToJson(writer);
    writer.endObject();
  }

  @Override
  public final void readFromJson(JsonReader reader) throws IOException {
    ArrayList<Action> actions = new ArrayList<Action>();
    actions.add(() -> entries = new ArrayList<Entry>());

    reader.beginObject();
    while (reader.peek() != JsonToken.END_OBJECT) {
      String propertyName = reader.nextName();
      Action action = readPropertyFromJson(propertyName, reader);
      if (action != null) {
        actions.add(action);
      } else {
        throw new UnknownPropertyNameException(propertyName, reader);
      }
    }
    reader.endObject();

    for (Action action : actions) {
      action.run();
    }
  }

  protected void writePropertiesToJson(JsonWriter writer) throws IOException {
    for (Entry entry : entries) {
      writer.name(entry.getType().getName());
      entry.getLightLevelNode().writeToJson(writer);
    }
  }

  protected Action readPropertyFromJson(String propertyName, JsonReader reader) throws IOException {
    LightType type = LightType.of(propertyName);
    if (type != null) {
      LightLevelNode node = new LightLevelNode();
      node.readFromJson(reader);
      return () -> entries.add(new Entry(type, node));
    }
    return null;
  }


  public static class Entry {

    private final LightType type;
    private final LightLevelNode node;

    public Entry(LightType type, LightLevelNode node) {
      this.type = type;
      this.node = node;
    }

    public LightType getType() {
      return type;
    }

    public LightLevelNode getLightLevelNode() {
      return node;
    }
  }

  @FunctionalInterface
  public interface Action {
    void run() throws IOException;
  }
}

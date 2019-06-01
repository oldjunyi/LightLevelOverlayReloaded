package com.mmyzd.llor.displaymode.json;

import java.io.IOException;
import java.util.function.BiConsumer;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.mmyzd.llor.displaymode.datatype.LightConstraints;
import com.mmyzd.llor.displaymode.datatype.TextureIndex;

public class LightMappingNode extends DataNode {

  private TextureIndex textureIndex = null;
  private LightTypeNode lightTypeNode = null;

  public void visit(LightConstraints constraints,
      BiConsumer<LightConstraints, TextureIndex> consumer) {
    if (lightTypeNode != null) {
      lightTypeNode.visit(constraints, consumer);
    } else {
      consumer.accept(constraints, textureIndex);
    }
  }

  @Override
  public void writeToJson(JsonWriter writer) throws IOException {
    if (textureIndex == null) {
      writer.nullValue();
    } else {
      writer.beginArray();
      writer.value(textureIndex.getRow());
      writer.value(textureIndex.getColumn());
      writer.endArray();
    }
  }

  @Override
  public void readFromJson(JsonReader reader) throws IOException {
    JsonToken nextToken = reader.peek();
    if (nextToken == JsonToken.NULL) {
      reader.nextNull();
      textureIndex = null;
      lightTypeNode = null;
    } else if (nextToken == JsonToken.BEGIN_ARRAY) {
      reader.beginArray();
      int row = reader.nextInt();
      int column = reader.nextInt();
      reader.endArray();
      textureIndex = new TextureIndex(row, column);
      lightTypeNode = null;
    } else {
      textureIndex = null;
      lightTypeNode = new LightTypeNode();
      lightTypeNode.readFromJson(reader);
    }
  }

}

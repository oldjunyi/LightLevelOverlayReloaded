package com.mmyzd.llor.displaymode.json;

import java.io.IOException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public abstract class DataNode {

  public abstract void writeToJson(JsonWriter writer) throws IOException;

  public abstract void readFromJson(JsonReader reader) throws IOException;
}

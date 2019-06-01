package com.mmyzd.llor.displaymode.exception;

import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;

public class UnknownPropertyNameException extends JsonParseException {

  private static final long serialVersionUID = 1L;

  public UnknownPropertyNameException(String name, JsonReader reader) {
    super("Unknown property name \"" + name + "\" from " + reader.toString());
  }
}

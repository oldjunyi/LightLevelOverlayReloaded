package com.mmyzd.llor.displaymode.exception;

import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;

public class LightSelectorFormatException extends JsonParseException {

  private static final long serialVersionUID = 1L;

  public LightSelectorFormatException(String format, JsonReader reader) {
    super("Invalid light selector format \"" + format + "\" from " + reader.toString());
  }
}

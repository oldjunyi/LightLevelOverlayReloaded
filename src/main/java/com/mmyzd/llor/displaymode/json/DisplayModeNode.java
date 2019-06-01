package com.mmyzd.llor.displaymode.json;

import java.io.IOException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class DisplayModeNode extends LightTypeNode {

  private static final String DISPLAY_NAME_KEY = "display_name";
  private static final String TEXTURE_PATH_KEY = "texture_path";
  private static final String TEXTURE_ROWS_KEY = "texture_rows";
  private static final String TEXTURE_COLUMNS_KEY = "texture_columns";
  private static final String LISTING_PRIORITY_KEY = "listing_priority";
  private static final String LUMINOSITY_KEY = "luminosity";

  private String displayName;
  private String texturePath;
  private Integer textureRows;
  private Integer textureColumns;
  private Integer listingPriority;
  private Double luminosity;

  public String getDisplayName() {
    return displayName;
  }

  public String getTexturePath() {
    return texturePath;
  }

  public int getTextureRows() {
    return textureRows != null ? textureRows : 0;
  }

  public int getTextureColumns() {
    return textureColumns != null ? textureColumns : 0;
  }

  public int getListingPriority() {
    return listingPriority != null ? listingPriority : 0;
  }

  public double getLuminosity() {
    return Math.min(Math.max((luminosity != null ? luminosity : 0), 0), 1);
  }

  public boolean isValid() {
    return texturePath != null && textureRows != null && textureRows > 0 &&
        textureColumns != null && textureColumns > 0;
  }

  @Override
  protected void writePropertiesToJson(JsonWriter writer) throws IOException {
    if (displayName != null) {
      writer.name(DISPLAY_NAME_KEY).value(displayName);
    }
    if (texturePath != null) {
      writer.name(TEXTURE_PATH_KEY).value(texturePath);
    }
    if (textureRows != null) {
      writer.name(TEXTURE_ROWS_KEY).value(textureRows);
    }
    if (textureColumns != null) {
      writer.name(TEXTURE_COLUMNS_KEY).value(textureColumns);
    }
    if (listingPriority != null) {
      writer.name(LISTING_PRIORITY_KEY).value(listingPriority);
    }
    if (luminosity != null) {
      writer.name(LUMINOSITY_KEY).value(luminosity);
    }
    super.writePropertiesToJson(writer);
  }

  @Override
  protected Action readPropertyFromJson(String propertyName, JsonReader reader) throws IOException {
    switch (propertyName) {
      case DISPLAY_NAME_KEY:
        String displayName = reader.nextString();
        return () -> this.displayName = displayName;
      case TEXTURE_PATH_KEY:
        String texturePath = reader.nextString();
        return () -> this.texturePath = texturePath;
      case TEXTURE_ROWS_KEY:
        int textureRows = reader.nextInt();
        return () -> this.textureRows = textureRows;
      case TEXTURE_COLUMNS_KEY:
        int textureColumns = reader.nextInt();
        return () -> this.textureColumns = textureColumns;
      case LISTING_PRIORITY_KEY:
        int listingPriority = reader.nextInt();
        return () -> this.listingPriority = listingPriority;
      case LUMINOSITY_KEY:
        double luminosity = reader.nextDouble();
        return () -> this.luminosity = luminosity;
    }
    return super.readPropertyFromJson(propertyName, reader);
  }

  public static class Adapter extends TypeAdapter<DisplayModeNode> {

    @Override
    public void write(JsonWriter writer, DisplayModeNode node) throws IOException {
      node.writeToJson(writer);
    }

    @Override
    public DisplayModeNode read(JsonReader reader) throws IOException {
      DisplayModeNode node = new DisplayModeNode();
      node.readFromJson(reader);
      return node;
    }

  }
}

package com.mmyzd.llor.displaymode.json;

import java.io.IOException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.util.math.MathHelper;

public class DisplayModeNode extends LightTypeNode {

  private static final String DISPLAY_NAME_KEY = "display_name";
  private static final String TEXTURE_PATH_KEY = "texture_path";
  private static final String TEXTURE_ROWS_KEY = "texture_rows";
  private static final String TEXTURE_COLUMNS_KEY = "texture_columns";
  private static final String ORDER_INDEX_KEY = "order_index";
  private static final String LUMINOSITY_KEY = "luminosity";
  private static final String TRANSPARENCY_KEY = "transparency";
  private static final String DISABLED_KEY = "disabled";

  private String displayName;
  private String texturePath;
  private Integer textureRows;
  private Integer textureColumns;
  private Double orderIndex;
  private Double luminosity;
  private Double transparency;
  private Boolean disabled;

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

  public double getOrderIndex() {
    return orderIndex != null ? orderIndex : 0;
  }

  public double getLuminosity() {
    return MathHelper.clamp(luminosity != null ? luminosity : 0, 0, 1);
  }

  public double getTransparency() {
    return MathHelper.clamp(transparency != null ? transparency : 0, 0, 1);
  }

  public boolean isDisabled() {
    return disabled != null ? disabled : false;
  }

  public boolean isValid() {
    return !isDisabled() && texturePath != null && textureRows != null && textureRows > 0
        && textureColumns != null && textureColumns > 0;
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
    if (orderIndex != null) {
      writer.name(ORDER_INDEX_KEY).value(orderIndex);
    }
    if (luminosity != null) {
      writer.name(LUMINOSITY_KEY).value(luminosity);
    }
    if (transparency != null) {
      writer.name(TRANSPARENCY_KEY).value(transparency);
    }
    if (disabled != null) {
      writer.name(DISABLED_KEY).value(disabled);
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
      case ORDER_INDEX_KEY:
        double orderIndex = reader.nextInt();
        return () -> this.orderIndex = orderIndex;
      case LUMINOSITY_KEY:
        double luminosity = reader.nextDouble();
        return () -> this.luminosity = luminosity;
      case TRANSPARENCY_KEY:
        double transparency = reader.nextDouble();
        return () -> this.transparency = transparency;
      case DISABLED_KEY:
        boolean disabled = reader.nextBoolean();
        return () -> this.disabled = disabled;
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

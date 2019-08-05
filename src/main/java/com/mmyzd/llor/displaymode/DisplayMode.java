package com.mmyzd.llor.displaymode;

import com.mmyzd.llor.ForgeMod;
import com.mmyzd.llor.displaymode.datatype.LightConstraints;
import com.mmyzd.llor.displaymode.datatype.LightRange;
import com.mmyzd.llor.displaymode.datatype.LightType;
import com.mmyzd.llor.displaymode.datatype.TextureCoordinates;
import com.mmyzd.llor.displaymode.json.DisplayModeNode;
import net.minecraft.util.ResourceLocation;

public class DisplayMode {

  public static final DisplayMode NULL = new DisplayMode("<null>");

  private final String name;
  private final String displayName;
  private final double orderIndex;
  private final double luminosity;
  private final double transparency;
  private final ResourceLocation texture;
  private final TextureCoordinates[][][] textureCoordinatesByLightLevels;

  public DisplayMode(String name, DisplayModeNode node) {
    this.name = name;
    if (node != null && node.isValid()) {
      displayName = (node.getDisplayName() != null) ? node.getDisplayName() : name;
      orderIndex = node.getOrderIndex();
      luminosity = node.getLuminosity();
      transparency = node.getTransparency();
      texture = new ResourceLocation(ForgeMod.ID, node.getTexturePath());
      textureCoordinatesByLightLevels =
          new TextureCoordinates[LightRange.UPPER_BOUND][LightRange.UPPER_BOUND][LightRange.UPPER_BOUND];
      createMappings(node);
    } else {
      displayName = name;
      orderIndex = 0;
      luminosity = 0;
      transparency = 0;
      texture = null;
      textureCoordinatesByLightLevels = null;
    }
  }

  public DisplayMode(String name) {
    this(name, null);
  }

  public String getName() {
    return name;
  }

  public String getDisplayName() {
    return displayName;
  }

  public double getOrderIndex() {
    return orderIndex;
  }

  public double getLuminosity() {
    return luminosity;
  }

  public double getTransparency() {
    return transparency;
  }

  public ResourceLocation getTexture() {
    return texture;
  }

  public TextureCoordinates getTextureCoordinates(int blockLight, int skyLight, int sunLight) {
    return textureCoordinatesByLightLevels[blockLight][skyLight][sunLight];
  }

  private void createMappings(DisplayModeNode node) {
    double textureRows = node.getTextureRows();
    double textureColumns = node.getTextureColumns();

    node.visit(LightConstraints.EMPTY, (constraints, textureIndex) -> {
      int textureRowIndex = textureIndex.getRow();
      int textureColumnIndex = textureIndex.getColumn();
      TextureCoordinates textureCoordinates =
          new TextureCoordinates(textureColumnIndex / textureColumns, textureRowIndex / textureRows,
              (textureColumnIndex + 1) / textureColumns, (textureRowIndex + 1) / textureRows)
                  .withCorrection();

      constraints.canonical().forEach(canonicalConstraints -> {
        int minBlockLight = canonicalConstraints.getMinLight(LightType.BLOCK);
        int maxBlockLight = canonicalConstraints.getMaxLight(LightType.BLOCK);
        int minSkyLight = canonicalConstraints.getMinLight(LightType.SKY);
        int maxSkyLight = canonicalConstraints.getMaxLight(LightType.SKY);
        int minSunLight = canonicalConstraints.getMinLight(LightType.SUN);
        int maxSunLight = canonicalConstraints.getMaxLight(LightType.SUN);
        for (int blockLight = minBlockLight; blockLight <= maxBlockLight; ++blockLight) {
          for (int skyLight = minSkyLight; skyLight <= maxSkyLight; ++skyLight) {
            for (int sunLight = minSunLight; sunLight <= maxSunLight; ++sunLight) {
              textureCoordinatesByLightLevels[blockLight][skyLight][sunLight] = textureCoordinates;
            }
          }
        }
      });
    });
  }

}

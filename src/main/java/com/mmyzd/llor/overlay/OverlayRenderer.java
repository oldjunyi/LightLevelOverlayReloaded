package com.mmyzd.llor.overlay;

import java.util.ArrayList;
import org.lwjgl.opengl.GL11;
import com.mmyzd.llor.config.Config;
import com.mmyzd.llor.config.ConfigManager;
import com.mmyzd.llor.displaymode.DisplayMode;
import com.mmyzd.llor.displaymode.datatype.TextureCoordinates;
import com.mmyzd.llor.event.WeakEventSubscriber;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class OverlayRenderer {

  private final ConfigManager configManager;
  private final OverlayProvider overlayProvider;

  public OverlayRenderer(ConfigManager configManager, OverlayProvider overlayProvider) {
    this.configManager = configManager;
    this.overlayProvider = overlayProvider;
    MinecraftForge.EVENT_BUS.register(new EventHandler(this));
  }

  private void renderOverlays(RenderWorldLastEvent event) {
    Config config = configManager.getConfig();
    if (!config.isOverlayEnabled()) {
      return;
    }

    DisplayMode displayMode = config.getDisplayMode();
    if (displayMode.getTexture() == null) {
      return;
    }

    Minecraft minecraft = Minecraft.getInstance();
    ClientWorld world = minecraft.world;
    ClientPlayerEntity player = minecraft.player;
    if (world == null || player == null) {
      return;
    }

    double viewX = TileEntityRendererDispatcher.staticPlayerX;
    double viewY = TileEntityRendererDispatcher.staticPlayerY;
    double viewZ = TileEntityRendererDispatcher.staticPlayerZ;

    minecraft.getTextureManager().bindTexture(displayMode.getTexture());
    minecraft.gameRenderer.enableLightmap();
    GlStateManager.enableBlend();
    GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    BufferBuilder vertexBuffer = Tessellator.getInstance().getBuffer();
    vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
    vertexBuffer.setTranslation(-viewX, -viewY, -viewZ);

    renderOverlays(world, vertexBuffer, displayMode, overlayProvider.getOverlays());
    Tessellator.getInstance().draw();

    vertexBuffer.setTranslation(0, 0, 0);
    GlStateManager.disableBlend();
    minecraft.gameRenderer.disableLightmap();
  }

  private void renderOverlays(ClientWorld world, BufferBuilder vertexBuffer,
      DisplayMode displayMode, ArrayList<Overlay> overlays) {
    double luminosity = displayMode.getLuminosity();
    for (Overlay overlay : overlays) {
      int blockLight = overlay.getBlockLight();
      int skyLight = overlay.getSkyLight();
      int sunLight = overlay.getSunLight();
      TextureCoordinates textureCoordinates =
          displayMode.getTextureCoordinates(blockLight, skyLight, sunLight);
      if (textureCoordinates == null) {
        continue;
      }

      BlockPos pos = overlay.getPos();
      double opacity = 1 - displayMode.getTransparency();
      int alpha = MathHelper.clamp((int) Math.round(opacity * 255), 0, 255);
      int brightness = world.getBlockState(pos).getPackedLightmapCoords(world, pos);
      int skyLumin = (brightness >> 16) & 0xFFFF;
      int blockLumin = brightness & 0xFFFF;
      blockLumin = (int) Math.round((blockLumin * (1 - luminosity)) + 240 * luminosity + 1e-6);
      blockLumin = MathHelper.clamp(blockLumin, 0, 240);

      for (int index = 0; index < 4; ++index) {
        vertexBuffer.pos(overlay.getX(index), overlay.getY(index), overlay.getZ(index))
            .color(255, 255, 255, alpha)
            .tex(textureCoordinates.getU(index), textureCoordinates.getV(index))
            .lightmap(skyLumin, blockLumin).endVertex();
      }
    }
  }

  private static class EventHandler extends WeakEventSubscriber<OverlayRenderer> {

    private EventHandler(OverlayRenderer overlayRenderer) {
      super(overlayRenderer);
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
      with(overlayRenderer -> overlayRenderer.renderOverlays(event));
    }
  }
}

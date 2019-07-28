package com.mmyzd.llor.overlay;

import java.util.ArrayList;
import com.mmyzd.llor.config.Config;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.multiplayer.ClientChunkProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.lighting.WorldLightManager;

public class OverlayPoller extends Thread {

  private volatile ArrayList<Overlay> overlays = new ArrayList<Overlay>();
  private volatile Config config;
  private ArrayList<Overlay>[][] chunks = createOverlaysMatrix(0);

  public OverlayPoller(Config config) {
    this.config = config;
  }

  public Config getConfig() {
    return config;
  }

  public void setConfig(Config config) {
    this.config = config;
  }

  public ArrayList<Overlay> getOverlays() {
    return overlays;
  }

  public void run() {
    long loopIndex = 0;
    while (true) {
      Config config = this.config;
      updateOverlays(config, loopIndex++);
      try {
        sleep(config.getPollingInterval());
      } catch (Exception exception) {
        exception.printStackTrace();
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static ArrayList<Overlay>[][] createOverlaysMatrix(int size) {
    return new ArrayList[size][size];
  }

  private void updateOverlays(Config config, long loopIndex) {
    if (!config.isOverlayEnabled()) {
      return;
    }

    Minecraft minecraft = Minecraft.getInstance();
    ClientWorld world = minecraft.world;
    ClientPlayerEntity player = minecraft.player;
    if (world == null || player == null) {
      overlays = new ArrayList<Overlay>();
      chunks = createOverlaysMatrix(0);
      return;
    }

    int playerChunkX = player.chunkCoordX;
    int playerChunkZ = player.chunkCoordZ;
    int playerY = (int) Math.floor(player.posY + player.getEyeHeight() + 1e-6);

    int radius = config.getRenderingRadius();
    int diameter = radius * 2 + 1;
    ArrayList<Overlay>[][] chunks = createOverlaysMatrix(diameter);
    ArrayList<Overlay> overlays = new ArrayList<>();

    int minChunkX = playerChunkX - radius;
    int maxChunkX = playerChunkX + radius;
    int minChunkZ = playerChunkZ - radius;
    int maxChunkZ = playerChunkZ + radius;

    for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
      for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
        int arrayX = (chunkX % diameter + diameter) % diameter;
        int arrayZ = (chunkZ % diameter + diameter) % diameter;

        int chunkDistanceX = Math.abs(chunkX - playerChunkX);
        int chunkDistanceZ = Math.abs(chunkZ - playerChunkZ);
        int chunkDistance = Math.max(chunkDistanceX, chunkDistanceZ);

        ArrayList<Overlay> chunk;
        if (chunkDistance == 0 || loopIndex % chunkDistance == 0
            || chunks.length != this.chunks.length) {
          chunk = createOverlaysForChunk(world, chunkX, chunkZ, playerY);
        } else {
          chunk = this.chunks[arrayX][arrayZ];
        }
        chunks[arrayX][arrayZ] = chunk;
        overlays.addAll(chunk);
      }
    }

    this.overlays = overlays;
    this.chunks = chunks;
  }

  public ArrayList<Overlay> createOverlaysForChunk(ClientWorld world, int chunkX, int chunkZ,
      int playerY) {
    ClientChunkProvider chunkProvider = world.getChunkProvider();
    Chunk chunk = chunkProvider.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
    if (chunk == null) {
      return new ArrayList<>();
    }

    WorldLightManager lightmanager = chunkProvider.getLightManager();
    int sunLightReduction = world.getSkylightSubtracted();

    Overlay.Builder overlayBuilder = new Overlay.Builder();
    ArrayList<Overlay> overlays = new ArrayList<>();

    for (int offsetX = 0; offsetX < 16; offsetX++) {
      for (int offsetZ = 0; offsetZ < 16; offsetZ++) {
        int posX = (chunkX << 4) + offsetX;
        int posZ = (chunkZ << 4) + offsetZ;
        int maxY = playerY + 4;
        int minY = Math.max(playerY - 40, 0);

        BlockPos upperBlockPos = null;
        BlockPos spawnBlockPos = new BlockPos(posX, maxY, posZ);
        BlockState upperBlockState = null;
        BlockState spawnBlockState = chunk.getBlockState(spawnBlockPos);

        for (int posY = maxY - 1; posY >= minY; posY--) {
          upperBlockPos = spawnBlockPos;
          spawnBlockPos = new BlockPos(posX, posY, posZ);
          upperBlockState = spawnBlockState;
          spawnBlockState = chunk.getBlockState(spawnBlockPos);

          Block spawnBlock = spawnBlockState.getBlock();
          if (spawnBlock == Blocks.AIR || spawnBlock == Blocks.BEDROCK
              || spawnBlock == Blocks.BARRIER || !spawnBlockState.func_215682_a(world,
                  spawnBlockPos, Minecraft.getInstance().player)) {
            continue;
          }

          if (Block.isOpaque(upperBlockState.getCollisionShape(world, upperBlockPos))
              || upperBlockState.canProvidePower() || upperBlockState.isIn(BlockTags.RAILS)
              || upperBlockState.getCollisionShape(world, upperBlockPos)
                  .getEnd(Direction.Axis.Y) > 0
              || !upperBlockState.getFluidState().isEmpty()) {
            continue;
          }

          double renderingPosY = posY + 1.01;
          if (upperBlockState.getShape(world, upperBlockPos).isEmpty()
              && upperBlockState.isSolid()) {
            renderingPosY += Math.max(
                upperBlockState.getRenderShape(world, upperBlockPos).getEnd(Direction.Axis.Y), 0);
          }

          int blockLight = lightmanager.getLightEngine(LightType.BLOCK).getLightFor(upperBlockPos);
          int skyLight = lightmanager.getLightEngine(LightType.SKY).getLightFor(upperBlockPos);
          int sunLight = Math.max(skyLight - sunLightReduction, 0);

          overlayBuilder.setPos(upperBlockPos);
          overlayBuilder.setX(posX);
          overlayBuilder.setZ(posZ);
          overlayBuilder.setY(renderingPosY);
          overlayBuilder.setBlockLight(blockLight);
          overlayBuilder.setSkyLight(skyLight);
          overlayBuilder.setSunLight(sunLight);
          overlays.add(overlayBuilder.build());
        }
      }
    }

    return overlays;
  }

}

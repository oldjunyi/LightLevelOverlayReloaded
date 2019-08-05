package com.mmyzd.llor.overlay;

import java.util.ArrayList;
import com.mmyzd.llor.config.Config;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.multiplayer.ClientChunkProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraft.world.spawner.WorldEntitySpawner;

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

    double sunLightReductionRatioByRain = 1 - world.getRainStrength(1.0f) * 5 / 16;
    double sunLightReductionRatioByThunder = 1 - world.getThunderStrength(1.0f) * 5 / 16;
    double sunLightReductionRatioByTime = 0.5 + 2 * MathHelper
        .clamp(MathHelper.cos(world.getCelestialAngle(1.0f) * (float) Math.PI * 2), -0.25, 0.25);
    int sunLightReduction = (int) ((1 - sunLightReductionRatioByRain
        * sunLightReductionRatioByThunder * sunLightReductionRatioByTime) * 11);

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

          if (!spawnBlockState.canEntitySpawn(chunk, spawnBlockPos, EntityType.ZOMBIE_PIGMAN)
              || !WorldEntitySpawner.isSpawnableSpace(chunk, upperBlockPos, upperBlockState,
                  upperBlockState.getFluidState())
              || upperBlockState.getCollisionShape(chunk, upperBlockPos)
                  .getEnd(Direction.Axis.Y) > 0) {
            // Minecraft 1.14:
            // 1. Check canEntitySpawn() on spawn block for the spawning entities.
            // 2. Check isSpawnableSpace() for two blocks above the spawn block.
            // 3. Check if the spawning entity collides with blocks.
            // Light Level Overlay Reloaded:
            // 1. Check canEntitySpawn() on spawn block for zombie pigman only.
            // ----- This covers all vanilla hostile mobs (except for polar bear on ice).
            // 2. Check isSpawnableSpace() for only one block above the spawn block.
            // ----- It is strange if a number only reveals after the 2nd block above it is removed.
            // 3. Check if the block above the spawn block has non-zero collision shape in Y axis.
            // ----- It is not correct for some rare cases but it is fast and mob-irrelevant.
            continue;
          }

          double renderingPosY = posY + 1.001;
          if (upperBlockState.isSolid()) {
            // Put numbers on top of solid-rendered blocks. E.g. the snow layer.
            VoxelShape renderShape = upperBlockState.getRenderShape(chunk, upperBlockPos);
            renderingPosY += Math.max(renderShape.getEnd(Direction.Axis.Y), 0);
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

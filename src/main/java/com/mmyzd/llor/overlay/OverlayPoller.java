package com.mmyzd.llor.overlay;

import java.util.ArrayList;
import java.util.function.BiPredicate;
import com.mmyzd.llor.config.Config;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.multiplayer.ClientChunkProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraft.world.spawner.WorldEntitySpawner;

public class OverlayPoller {

  private static final ArrayList<Overlay> EMPTY_OVERLAYS = new ArrayList<Overlay>();
  private static final ArrayList<Overlay>[][] EMPTY_OVERLAYS_PER_CHUNK =
      createChunkOverlaysMatrix(0);

  private ArrayList<Overlay> overlays = EMPTY_OVERLAYS;
  private ArrayList<Overlay>[][] overlaysPerChunk = EMPTY_OVERLAYS_PER_CHUNK;

  public ArrayList<Overlay> getOverlays() {
    return overlays;
  }

  public void removeOverlays() {
    overlays = EMPTY_OVERLAYS;
    overlaysPerChunk = EMPTY_OVERLAYS_PER_CHUNK;
  }

  public void updateOverlays(Config config, BiPredicate<Integer, Integer> canUpdate) {
    Minecraft minecraft = Minecraft.getInstance();
    ClientWorld world = minecraft.world;
    ClientPlayerEntity player = minecraft.player;
    if (world == null || player == null || !config.isOverlayEnabled()) {
      removeOverlays();
      return;
    }

    ClientChunkProvider chunkProvider = world.getChunkProvider();
    WorldLightManager lightmanager = chunkProvider.getLightManager();

    int playerChunkX = player.chunkCoordX;
    int playerChunkZ = player.chunkCoordZ;
    int playerY = (int) Math.floor(player.posY + player.getEyeHeight() + 1e-6);

    double sunLightReductionRatioByRain = 1 - world.getRainStrength(1.0f) * 5 / 16;
    double sunLightReductionRatioByThunder = 1 - world.getThunderStrength(1.0f) * 5 / 16;
    double sunLightReductionRatioByTime = 0.5 + 2 * MathHelper
        .clamp(MathHelper.cos(world.getCelestialAngle(1.0f) * (float) Math.PI * 2), -0.25, 0.25);
    int sunLightReduction = (int) ((1 - sunLightReductionRatioByRain
        * sunLightReductionRatioByThunder * sunLightReductionRatioByTime) * 11);

    int radius = config.getRenderingRadius();
    int diameter = radius * 2 + 1;
    ArrayList<Overlay>[][] overlaysPerChunk = createChunkOverlaysMatrix(diameter);
    ArrayList<Overlay> overlays = new ArrayList<>();

    int minChunkX = playerChunkX - radius;
    int maxChunkX = playerChunkX + radius;
    int minChunkZ = playerChunkZ - radius;
    int maxChunkZ = playerChunkZ + radius;

    for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
      for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
        int arrayX = (chunkX % diameter + diameter) % diameter;
        int arrayZ = (chunkZ % diameter + diameter) % diameter;

        int chunkOffsetX = chunkX - playerChunkX;
        int chunkOffsetZ = chunkZ - playerChunkZ;

        ArrayList<Overlay> chunkOverlays;
        if (canUpdate.test(chunkOffsetX, chunkOffsetZ)) {
          Chunk chunk = chunkProvider.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
          chunkOverlays = extractChunkOverlays(chunk, lightmanager, playerY, sunLightReduction);
        } else if (overlaysPerChunk.length != this.overlaysPerChunk.length) {
          chunkOverlays = EMPTY_OVERLAYS;
        } else {
          chunkOverlays = this.overlaysPerChunk[arrayX][arrayZ];
        }
        overlaysPerChunk[arrayX][arrayZ] = chunkOverlays;
        overlays.addAll(chunkOverlays);
      }
    }

    this.overlays = overlays;
    this.overlaysPerChunk = overlaysPerChunk;
  }

  private ArrayList<Overlay> extractChunkOverlays(Chunk chunk, WorldLightManager lightManager,
      int playerY, int sunLightReduction) {
    if (chunk == null) {
      return new ArrayList<>();
    }

    Overlay.Builder overlayBuilder = new Overlay.Builder();
    ArrayList<Overlay> overlays = new ArrayList<>();

    ChunkPos chunkPos = chunk.getPos();
    int posXStart = chunkPos.getXStart();
    int posXEnd = chunkPos.getXEnd();
    int posZStart = chunkPos.getZStart();
    int posZEnd = chunkPos.getZEnd();

    for (int posX = posXStart; posX <= posXEnd; posX++) {
      for (int posZ = posZStart; posZ <= posZEnd; posZ++) {
        int maxY = playerY + 4;
        int minY = Math.max(playerY - 36, 0);

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

          double renderingPosY = posY + 1.001953125;
          if (upperBlockState.isSolid()) {
            // Put numbers on top of solid-rendered blocks. E.g. the snow layer.
            VoxelShape renderShape = upperBlockState.getRenderShape(chunk, upperBlockPos);
            renderingPosY += Math.max(renderShape.getEnd(Direction.Axis.Y), 0);
          }

          int blockLight = lightManager.getLightEngine(LightType.BLOCK).getLightFor(upperBlockPos);
          int skyLight = lightManager.getLightEngine(LightType.SKY).getLightFor(upperBlockPos);
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

  @SuppressWarnings("unchecked")
  private static ArrayList<Overlay>[][] createChunkOverlaysMatrix(int size) {
    return new ArrayList[size][size];
  }
}

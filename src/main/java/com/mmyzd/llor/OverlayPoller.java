package com.mmyzd.llor;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;

public class OverlayPoller extends Thread {
	
	public volatile ArrayList<Overlay>[][] overlays;
	
	public void run() {
		int radius = 0;
		while (true) {
			int chunkRadius = updateChunkRadius();
			radius = radius % chunkRadius + 1;
			if (LightLevelOverlayReloaded.instance.active) updateLightLevel(radius, chunkRadius);
			try {
				sleep(LightLevelOverlayReloaded.instance.config.pollingInterval.getInt());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private int updateChunkRadius() {
		int size = LightLevelOverlayReloaded.instance.config.chunkRadius.getInt();
		if (overlays == null || overlays.length != size * 2 + 1) {
			overlays = new ArrayList[size * 2 + 1][size * 2 + 1];
			for (int i = 0; i < overlays.length; i++)
			for (int j = 0; j < overlays[i].length; j++)
				overlays[i][j] = new ArrayList<Overlay>();
		}
		return size;
	}
	
	private void updateLightLevel(int radius, int chunkRadius) {
		
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.player == null) return;
		
		WorldClient world = mc.world;
		int playerPosY = (int)Math.floor(mc.player.posY);
		int playerChunkX = mc.player.chunkCoordX;
		int playerChunkZ = mc.player.chunkCoordZ; 
		int skyLightSub = world.calculateSkylightSubtracted(1.0f);
		int displayMode = LightLevelOverlayReloaded.instance.config.displayMode.getInt();
		boolean useSkyLight = LightLevelOverlayReloaded.instance.config.useSkyLight.getBoolean();
		
		for (int chunkX = playerChunkX - radius; chunkX <= playerChunkX + radius; chunkX++)
		for (int chunkZ = playerChunkZ - radius; chunkZ <= playerChunkZ + radius; chunkZ++) {
			Chunk chunk = mc.world.getChunkFromChunkCoords(chunkX, chunkZ);
			if (!chunk.isLoaded()) continue;
			ArrayList<Overlay> buffer = new ArrayList<Overlay>();
			for (int offsetX = 0; offsetX < 16; offsetX++)
			for (int offsetZ = 0; offsetZ < 16; offsetZ++) {
				int posX = (chunkX << 4) + offsetX;
				int posZ = (chunkZ << 4) + offsetZ;
				int maxY = playerPosY + 4, minY = Math.max(playerPosY - 40, 0);
				IBlockState preBlockState = null, curBlockState = chunk.getBlockState(offsetX, maxY, offsetZ);
				Block preBlock = null, curBlock = curBlockState.getBlock();
				BlockPos prePos = null, curPos = new BlockPos(posX, maxY, posZ);
				for (int posY = maxY - 1; posY >= minY; posY--) {
					preBlockState = curBlockState;
					curBlockState = chunk.getBlockState(offsetX, posY, offsetZ);
					preBlock = curBlock;
					curBlock = curBlockState.getBlock();
					prePos = curPos;
					curPos = new BlockPos(posX, posY, posZ);
					if (curBlock == Blocks.BEDROCK ||
						curBlock == Blocks.BARRIER ||
						!preBlockState.isFullCube() ||
						preBlockState.getMaterial().isLiquid() ||
						preBlockState.canProvidePower() ||
						curBlockState.isSideSolid(world, curPos, EnumFacing.UP) == false ||
						BlockRailBase.isRailBlock(preBlockState)) {
						continue;
					}
					double offsetY = 0;
					if (preBlock == Blocks.SNOW_LAYER || preBlock == Blocks.CARPET) {
						offsetY = preBlockState.getBoundingBox(world, prePos).maxY;
						if (offsetY >= 0.15) continue; // Snow layer too high
					}
					int blockLight = chunk.getLightFor(EnumSkyBlock.BLOCK, prePos);
					int   skyLight = chunk.getLightFor(EnumSkyBlock.SKY, prePos) - skyLightSub;
					int mixedLight = Math.max(blockLight, skyLight);
					int lightIndex = useSkyLight ? mixedLight : blockLight;
					if (displayMode == 1) {
						if (mixedLight >= 8 && blockLight < 8) lightIndex += 32;
					} else if (displayMode == 2) {
						if (blockLight >= 8) continue;
						if (lightIndex >= 8) lightIndex += 32;
					}
					if (lightIndex >= 8 && lightIndex < 24) lightIndex ^= 16;
					buffer.add(new Overlay(posX, posY + offsetY + 1, posZ, lightIndex));
				}
			}
			int len = chunkRadius * 2 + 1;
			int arrayX = (chunkX % len + len) % len;
			int arrayZ = (chunkZ % len + len) % len;
			overlays[arrayX][arrayZ] = buffer;
		}
		
	}
	
}

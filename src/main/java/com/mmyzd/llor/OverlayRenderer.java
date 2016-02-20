package com.mmyzd.llor;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

public class OverlayRenderer {
	
	private ResourceLocation texture;
	private double[] texureMinX, texureMaxX;
	
	public OverlayRenderer() {
		texture = new ResourceLocation("llor", "textures/overlay.png");
		texureMinX = new double[16];
		texureMaxX = new double[16];
		for (int i = 0; i < 16; i++) {
			texureMinX[i] = i / 16.0;
			texureMaxX[i] = (i + 1) / 16.0;
		}
	}
	
	public void render(double x, double y, double z, ArrayList<Overlay>[][] overlays) {
		
		TextureManager tm = Minecraft.getMinecraft().renderEngine;
		Tessellator wr = Tessellator.instance;
		tm.bindTexture(texture);
		
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glPushMatrix();
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_DST_COLOR, GL11.GL_ZERO);
		wr.startDrawingQuads();
		wr.setTranslation(-x, -y, -z);
		for (int i = 0; i < overlays.length; i++)
		for (int j = 0; j < overlays[i].length; j++) {
			for (Overlay u: overlays[i][j]) {
				wr.addVertexWithUV(u.x,     u.y, u.z,     texureMinX[u.lightLevel], 0.0);
				wr.addVertexWithUV(u.x,     u.y, u.z + 1, texureMinX[u.lightLevel], 1.0);
				wr.addVertexWithUV(u.x + 1, u.y, u.z + 1, texureMaxX[u.lightLevel], 1.0);
				wr.addVertexWithUV(u.x + 1, u.y, u.z,     texureMaxX[u.lightLevel], 0.0);
			}
		}
		wr.draw();
		wr.setTranslation(0, 0, 0);
		GL11.glPopMatrix();
		GL11.glPopAttrib();
		
	}
	
}

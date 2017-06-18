package com.mmyzd.llor;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class OverlayRenderer {
	
	private ResourceLocation texture;
	private double[] texureMinX, texureMaxX;
	private double[] texureMinY, texureMaxY;
	
	public OverlayRenderer() {
		texture = new ResourceLocation("llor", "textures/overlay.png");
		texureMinX = new double[64];
		texureMaxX = new double[64];
		texureMinY = new double[64];
		texureMaxY = new double[64];
		for (int i = 0; i < 64; i++) {
			texureMinX[i] = (i % 8) / 8.0;
			texureMaxX[i] = (i % 8 + 1) / 8.0;
			texureMinY[i] = (i / 8) / 8.0;
			texureMaxY[i] = (i / 8 + 1) / 8.0;
		}
	}
	
	public void render(double x, double y, double z, ArrayList<Overlay>[][] overlays) {
		
		TextureManager tm = Minecraft.getMinecraft().renderEngine;
		// VertexBuffer
		tm.bindTexture(texture);
		BufferBuilder vb = Tessellator.getInstance().getBuffer();
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glPushMatrix();
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_DST_COLOR, GL11.GL_ZERO);
		vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		vb.setTranslation(-x, -y, -z);
		for (int i = 0; i < overlays.length; i++)
		for (int j = 0; j < overlays[i].length; j++) {
			for (Overlay u: overlays[i][j]) {
				vb.pos(u.x,     u.y, u.z    ).tex(texureMinX[u.index], texureMinY[u.index]).color(255, 255, 255, 255).endVertex();
				vb.pos(u.x,     u.y, u.z + 1).tex(texureMinX[u.index], texureMaxY[u.index]).color(255, 255, 255, 255).endVertex();
				vb.pos(u.x + 1, u.y, u.z + 1).tex(texureMaxX[u.index], texureMaxY[u.index]).color(255, 255, 255, 255).endVertex();
				vb.pos(u.x + 1, u.y, u.z    ).tex(texureMaxX[u.index], texureMinY[u.index]).color(255, 255, 255, 255).endVertex();
			}
		}
		vb.setTranslation(0, 0, 0);
		Tessellator.getInstance().draw();
		GL11.glPopMatrix();
		GL11.glPopAttrib();
		
	}
	
}

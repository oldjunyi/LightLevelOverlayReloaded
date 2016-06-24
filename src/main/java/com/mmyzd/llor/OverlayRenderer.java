package com.mmyzd.llor;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
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
		// VertexBuffer
		tm.bindTexture(texture);
		VertexBuffer vb = Tessellator.getInstance().getBuffer();
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
				vb.pos(u.x,     u.y, u.z    ).tex(texureMinX[u.lightLevel], 0.0).color(255, 255, 255, 255).endVertex();
				vb.pos(u.x,     u.y, u.z + 1).tex(texureMinX[u.lightLevel], 1.0).color(255, 255, 255, 255).endVertex();
				vb.pos(u.x + 1, u.y, u.z + 1).tex(texureMaxX[u.lightLevel], 1.0).color(255, 255, 255, 255).endVertex();
				vb.pos(u.x + 1, u.y, u.z    ).tex(texureMaxX[u.lightLevel], 0.0).color(255, 255, 255, 255).endVertex();
			}
		}
		vb.setTranslation(0, 0, 0);
		Tessellator.getInstance().draw();
		GL11.glPopMatrix();
		GL11.glPopAttrib();
		
	}
	
}

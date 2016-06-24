package com.mmyzd.llor;

public class Overlay {
	
	public int x, z;
	public double y;
	public int lightLevel;
	
	public Overlay(int x, double y, int z, int lightLevel) {
		this.x = x;
		this.y = y + 0.01;
		this.z = z;
		this.lightLevel = lightLevel;
	}
	
}

package com.mmyzd.llor;

public class Overlay {
	
	public int x, z;
	public double y;
	public int index;
	
	public Overlay(int x, double y, int z, int index) {
		this.x = x;
		this.y = y + 0.01;
		this.z = z;
		this.index = index;
	}
	
}

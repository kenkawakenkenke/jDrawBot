package com.jdrawbot.robot;

import common.util.MathUtil;

public class Location {
	public final double x,y;
	
	public Location(double x,double y){
		this.x=x;
		this.y=y;
	}
	
	@Override
	public String toString() {
		return String.format("%.1f %.1f",x/10,y/10);
	}
	
	public double dist(Location other){
		return Math.sqrt(MathUtil.square(x-other.x)+MathUtil.square(y-other.y));
	}
}

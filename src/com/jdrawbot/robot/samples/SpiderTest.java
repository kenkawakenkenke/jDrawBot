package com.jdrawbot.robot.samples;

import com.jdrawbot.robot.Location;
import com.jdrawbot.robot.Spider;


public class SpiderTest {

	public static void main(String[] args) {
		Spider spider=new Spider();

		spider.moveTo(new Location(-30,30));
		spider.drawTo(new Location(30,30));
		spider.drawTo(new Location(30,60));
		spider.drawTo(new Location(-30,60));
		spider.drawTo(new Location(-30,30));
		
		System.exit(0);
	}
}

package com.jdrawbot.robot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.concurrent.LinkedBlockingQueue;

public class Spider {
	private final Command killQueue=new Command(null,null);
	
	public static class ResponseRetriever{
		BufferedReader br;
		public ResponseRetriever(BufferedReader br){
			this.br=br;
		}
		public String waitForResponse(){
			try {
				return br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		public void abort(){
			br.notifyAll();
		}
	}
	
	public static class Command{
		public final String command;
		public final Blocker blocker;
		public Command(String command, Blocker blocker){
			this.command=command;
			this.blocker=blocker;
		}
	}
	
	ArduinoSerial serial;
	PrintWriter out;
	ResponseRetriever responseRetriever;
	Location currentPos=new Location(0,0);
	
	public final Location boundTL, boundBR;
	public Spider(){
		this(new Location(-10000,10000),new Location(10000,0));
	}
	public Spider(Location boundTL, Location boundBR){
		this.boundTL=boundTL;
		this.boundBR=boundBR;
		
		serial=new ArduinoSerial();
		if(!serial.initialize()){
			return;
		}
		out=new PrintWriter(serial.output());
		BufferedReader br=new BufferedReader(new InputStreamReader(serial.input()));
		responseRetriever=new ResponseRetriever(br);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		open=true;
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run(){
				close();
			}
		});
		
		new Thread(){
			public void run(){
				while(open){
					Command command=null;
					try {
						command = commands.take();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(command==killQueue)
						break;
//					System.out.println("["+command.command+"]");
					internalWriteCommand(command.command);
					command.blocker.unblock();
				}
				System.out.println("end command queue");
			}
		}.start();
	}
	private boolean open;
	
	public void close(){
		if(!open){
			return;
		}
		System.out.println("closing...");
		open=false;
		synchronized(commandsLock){
			commands.clear();
			try {
				commands.put(killQueue);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
//		responseRetriever.abort();
		internalWriteCommand("M1 100"); // Pen up
		internalWriteCommand("G1 X0 Y0"); //
		out.close();
		serial.close();
	}
	
	Object commandsLock=new Object();
	LinkedBlockingQueue<Command> commands=new LinkedBlockingQueue<>();
	
	public Blocker sendCommand(String command){
		Blocker blocker=new Blocker();
		synchronized(commandsLock){
			if(!open){
				blocker.unblock();
				return blocker;
			}
			try {
				commands.put(new Command(command, blocker));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return blocker;
	}
	private void internalWriteCommand(String command){
		out.println(command);
		out.flush();
		long t=System.currentTimeMillis();
		responseRetriever.waitForResponse();
//		System.out.println((System.currentTimeMillis()-t));
	}
	
	boolean isDrawing=false;
	public Blocker startWrite(){
		isDrawing=true;
		return sendCommand("M1 90");
	}
	public Blocker endWrite(){
		isDrawing=false;
		return sendCommand("M1 100");
	}
	
	public Blocker moveTo(Location newLoc){
		if(isDrawing)
			endWrite();

		double x=Math.min(Math.max(newLoc.x, boundTL.x), boundBR.x);
		double y=Math.min(Math.max(newLoc.y, boundBR.y), boundTL.y);
		Blocker blocker=sendCommand(String.format("G1 X%.02f Y%.02f",x,y));		
		currentPos=newLoc;
		return blocker;
	}
	public Blocker drawTo(Location newLoc){
		if(!isDrawing)
			startWrite();

		double x=Math.min(Math.max(newLoc.x, boundTL.x), boundBR.x);
		double y=Math.min(Math.max(newLoc.y, boundBR.y), boundTL.y);
		Blocker blocker=sendCommand(String.format("G1 X%.02f Y%.02f",x,y));		
		currentPos=newLoc;
		return blocker;
	}

	public static void main(String[] args) throws InterruptedException {
		Spider spider=new Spider();

		spider.moveTo(new Location(-35,30));
		spider.moveTo(new Location(0,30));
		spider.moveTo(new Location(35,30)).block();
		System.exit(0);
	}
}

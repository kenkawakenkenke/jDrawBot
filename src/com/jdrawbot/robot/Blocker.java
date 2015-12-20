package com.jdrawbot.robot;

public class Blocker {

	private boolean blocked=true;
	private Object lock=new Object();
	
	public void unblock(){
		synchronized(lock){
			blocked=false;
			lock.notifyAll();
		}
	}
	public void reset(){
		synchronized(lock){
			blocked=true;
		}
	}
	public void block(){
		synchronized(lock){
			while(blocked){
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}

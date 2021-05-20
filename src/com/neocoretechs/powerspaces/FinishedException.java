package com.neocoretechs.powerspaces;
/**
* FinishedException is thrown by commands not wishing to return a Packet from call
*/
public class FinishedException extends Exception {
	public FinishedException() { super(); }
	public FinishedException(String s) { super(s); }
}

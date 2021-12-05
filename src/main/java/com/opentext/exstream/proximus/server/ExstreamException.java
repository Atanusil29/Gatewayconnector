package com.opentext.exstream.proximus.server;

public class ExstreamException extends Exception {

	private static final long serialVersionUID = -6190006110802399923L;

	public ExstreamException(String message){
		super(message);
	}

	public ExstreamException(String message, Exception e){
		super(message, e);
	}
	
	
}

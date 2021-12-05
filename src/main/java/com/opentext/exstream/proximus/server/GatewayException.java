package com.opentext.exstream.proximus.server;

public class GatewayException extends Exception {

	private static final long serialVersionUID = -6990038162412370529L;
	
	public GatewayException(String message){
		super(message);
	}

	public GatewayException(String message, Exception e){
		super(message, e);
	}
	
	
}

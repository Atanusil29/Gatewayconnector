package com.opentext.exstream.proximus.jobtracking;

public class Constants {

	private Constants() {
	}

	// Communications Server states
	public static final String STATE_INPROGRESS = "0";
	public static final String STATE_RETRY = "1"; //used internal only
	public static final String STATE_SUCCESS = "5";
	public static final String STATE_FAILURE = "6";
	public static final String STATE_HOLDING = "8";

	// Tracker attributes
	public static final String EXTERNAL_ID      = "6B84E18B-F03D-350C-E040-007F0200026D";
	public static final String PROCESSING_STATE = "6B84E18B-F042-350C-E040-007F0200026D";
	public static final String LAST_UPDATE_TIME = "6B84E18B-F048-350C-E040-007F0200026D";

}

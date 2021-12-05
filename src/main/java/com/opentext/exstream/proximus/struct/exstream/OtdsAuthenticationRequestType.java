package com.opentext.exstream.proximus.struct.exstream;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.opentext.exstream.proximus.struct.Default;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class OtdsAuthenticationRequestType extends Default {

	private String userName;
	private String password;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}

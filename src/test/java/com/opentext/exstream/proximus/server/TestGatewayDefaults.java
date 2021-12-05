package com.opentext.exstream.proximus.server;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.springframework.http.HttpHeaders;
import org.springframework.util.Base64Utils;

public class TestGatewayDefaults {
	
	public static String baseurl = "http://localhost:8080/gateway-mock-server";
	
	public static HttpHeaders getHttpHeaders(){
		String auth = "admin:xxx";
		String basic = Base64Utils.encodeToString(auth.getBytes());

		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + basic);
		return headers;
	}

	public static void setupSSLProperties(){
		System.setProperty("https.protocols","TLSv1,TLSv1.1,TLSv1.2");
		System.setProperty("https.cipherSuites","TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256");
	}

	//alternate version
	public static void setupTLSSettings(){
		try {
			//alternate: -Dhttps.protocols=TLSv1,TLSv1.1,TLSv1.2 -Dhttps.cipherSuites=TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256
			SSLContext sc = SSLContext.getInstance("TLSv1.2");
			sc.init(null, null, null);
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {	}
	}

}

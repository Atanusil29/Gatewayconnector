package com.opentext.exstream.proximus.jobtracking;

import java.io.File;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.springframework.http.HttpHeaders;
import org.springframework.util.Base64Utils;

public class TestExstreamDefaults {
	
	public static String otdsMtaUrl = "http://localhost:8080";
	public static String otdsTenantUrl = "http://localhost:8080";
	public static String sgwUrl = "http://localhost:8080/dev/dev";
	public static String username = "tenantadmin";
	public static String password = "xxx";
	public static String truststore = "src/test/resources/truststore.jks";
	public static String trustpass = "changeit";
	
	
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

		boolean trustExists = new File(truststore).isFile();
		System.out.println("Truststore exists? " + trustExists);
		if (trustExists){
			System.setProperty("javax.net.ssl.trustStore", truststore);
			System.setProperty("javax.net.ssl.trustStorePassword", trustpass);
			System.setProperty("javax.net.ssl.trustStoreType", "JKS");
		}		
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

package com.opentext.exstream.proximus.common;

import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemProperties {
	private static final Logger log = LoggerFactory.getLogger(SystemProperties.class);
	
	/**
	 * Loads and sets the system properties from the properties file.
	 * Loads all properties that start with "javax." and "https."
	 * Primary reason is to load the ssl related properties:
	 *     "javax.net.ssl.keyStore",
	 *     "javax.net.ssl.keyStorePassword",
	 *     "javax.net.ssl.keyStoreType",
	 *     "javax.net.ssl.trustStore",
	 *     "javax.net.ssl.trustStorePassword",
	 *     "javax.net.ssl.trustStoreType",
	 *     "javax.net.ssl.ignoreTrustErrors",
	 *     "javax.net.debug",
	 *     "https.protocols",
	 *     "https.cipherSuites"
	 * The ignoreTrustErrors property creates a trust manager that does not valid
	 * certificates, and is designed for testing purposes only. 
	 * @param properties properties to load system properties from
	 */
	public static void loadSystemProperties(Properties properties) {
		loadExternalPropertyFile(properties, "SYSTEM_PROPERTIES");
		
		//Process the property files.
		setSystemProperties(properties, "javax.");
		setSystemProperties(properties, "https.");

		//For testing purposes only!!!
		//disables SSL certificate validations!
		if ("true".equalsIgnoreCase(System.getProperty("javax.net.ssl.ignoreTrustErrors"))){
			log.warn("Disabling SSL/TLS certificate validation");
			ignoreTrustErrors();
		}
	}

	private static Properties loadExternalPropertyFile(Properties properties, String filekey){
		Properties merged = new Properties();
		merged.putAll(properties);
		if (properties.containsKey(filekey)){
			try {
				String externfile = PropertyReader.readProperty(properties, filekey);
				Properties externprops = PropertyReader.loadPropertiesFile(externfile);
				merged.putAll(externprops);
			} catch (Exception e) {
				log.error("Exception loading external property file", e);
			}			
		}
		return merged;
	}

	private static void setSystemProperties(Properties properties, String prefix){
		// only load system properties from property file if property not already set
		String value;
		for (Object k : properties.keySet()){
			String key = k.toString();
			if (prefix == null || key.startsWith(prefix)){
				value =  PropertyReader.readProperty(properties,key);
				System.setProperty(key,value);
			}
		}
	}

	/**
	 * For testing purposes only!!! disables SSL certificate validations!
	 * Creates a trust manager that does not valid certificates. 
	 */
	private static void ignoreTrustErrors(){
		//original logic that was included previously
		javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
			new javax.net.ssl.HostnameVerifier(){
				public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
					return true;
				}
			});
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[]{
			new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}
				public void checkClientTrusted(
					java.security.cert.X509Certificate[] certs, String authType) {
				}
				public void checkServerTrusted(
					java.security.cert.X509Certificate[] certs, String authType) {
				}
			}
		};

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, null);
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {	}
	}


}

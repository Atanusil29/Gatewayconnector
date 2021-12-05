package com.opentext.exstream.proximus.jobtracking;

import java.io.IOException;
import java.util.Properties;

import com.opentext.exstream.proximus.common.PropertyReader;

public class GatewayConfiguration {

	private static final String GATEWAY_CONFIG_FILE = "gateway.properties";

	private String gatewayUrl;

	public GatewayConfiguration() throws IOException {
		Properties gatewayProperties = PropertyReader.loadPropertiesFileFromClasspath(GATEWAY_CONFIG_FILE);
		gatewayUrl = gatewayProperties.getProperty("gateway.url");
	}

	public String getGatewayUrl() {
		return gatewayUrl;
	}

}

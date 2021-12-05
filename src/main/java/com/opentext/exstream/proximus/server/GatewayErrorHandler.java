package com.opentext.exstream.proximus.server;

import java.io.IOException;

import org.springframework.http.HttpStatus.Series;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import com.opentext.exstream.proximus.common.CustomLogger;

public class GatewayErrorHandler implements ResponseErrorHandler {
	private static final CustomLogger log = new CustomLogger(GatewayErrorHandler.class);
	
	@Override
	public void handleError(ClientHttpResponse response) throws IOException {
		log.warn("Gateway error handler: {} {}", response.getStatusCode().value(), response.getStatusText());
	}

	@Override
	public boolean hasError(ClientHttpResponse response) throws IOException {
		Series series = response.getStatusCode().series();
		return (series.equals(Series.CLIENT_ERROR) || series.equals(Series.SERVER_ERROR));
	}
}

package com.opentext.exstream.proximus.server;

import java.io.UnsupportedEncodingException;
import java.net.HttpRetryException;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;

import com.opentext.exstream.proximus.common.CustomLogger;
import com.opentext.exstream.proximus.struct.Default;
import com.opentext.exstream.proximus.struct.EventRequestType;
import com.opentext.exstream.proximus.struct.EventResponseType;
import com.opentext.exstream.proximus.struct.SendMessageType;

public class GatewaySession {
	private static final CustomLogger log = new CustomLogger(GatewaySession.class);
	private HttpHeaders headers = null;
	private String gatewayUrl;
	private int maxretries = 5;
	private int sleepinterval=1000;
	private double sleepfactor=2;

	//insert initialisation function - linked to the class creator, and calling getToken.

	public GatewaySession(String gatewayUrl) {
		this.gatewayUrl = gatewayUrl;
		this.headers = generateBasicHeaders();
	}

	public GatewaySession(String gatewayUrl, String username, String password) {
		this.gatewayUrl = gatewayUrl;
		this.headers = generateBasicHeaders(username, password);
	}

	public <T> T postRequest(String requestUrl, Default requestBody, Class<T> clazz) throws GatewayException {
		String url = gatewayUrl + requestUrl;

		try {
			HttpEntity<byte[]> request = new HttpEntity<byte[]>(requestBody.toString().getBytes("UTF-8"), this.headers);
			ResponseEntity<T> httpResponse = processRequest(url, HttpMethod.POST, request, clazz);
			if (clazz == null) {
				return null;
			}
			T responseBody = httpResponse.getBody();
			return responseBody;
		} catch (UnsupportedEncodingException e) {
			throw new GatewayException("Unable to encode request body", e);
		}
	}

	@Deprecated
	public EventResponseType fetchEvents(EventRequestType eventRequest) throws GatewayException {
		String url = gatewayUrl + "/v1/cdcgFetchEvents";

		try {
			HttpEntity<byte[]> request = new HttpEntity<byte[]>(eventRequest.toString().getBytes("UTF-8"), this.headers);
			ResponseEntity<EventResponseType> httpResponse = processRequest(url, HttpMethod.POST, request, EventResponseType.class);
			EventResponseType body = httpResponse.getBody();
			return body;
		} catch (UnsupportedEncodingException e) {
			throw new GatewayException("Unable to encode request body", e);
		}
	}

	@Deprecated
	public void sendMessage(SendMessageType sendMessage) throws GatewayException {
		String url = gatewayUrl + "/v1/cdcgSendMessage";

		try {
			HttpEntity<byte[]> request = new HttpEntity<byte[]>(sendMessage.toString().getBytes("UTF-8"), this.headers);
			processRequest(url, HttpMethod.POST, request, null);
		} catch (UnsupportedEncodingException e) {
			throw new GatewayException("Unable to encode request body", e);
		}
	}

	private <T> ResponseEntity<T> processRequest(String url, HttpMethod method, HttpEntity<?> entity, Class<T> clazz) throws GatewayException{
		log.info("Service request [{}]: {}", method.toString(), url);

		RestTemplate restTemplate = getRestTemplate();
		int attempts = 0;
		do{
			try{
				ResponseEntity<T> httpResponse = restTemplate.exchange(url, method, entity, clazz);
				
				if (httpResponse.getStatusCode().is2xxSuccessful()) {
					return (ResponseEntity<T>) httpResponse;
				}
				checkStatusCode(httpResponse.getStatusCode(), ++attempts);
			}catch (GatewayException e){
				throw e;
			}catch (Exception e){
				//HTTP 401 with POST can cause ResourceAccessException, trap error here. 
				Throwable t = e.getCause();
				if (t != null && t instanceof HttpRetryException ){
					if (((HttpRetryException)t).responseCode()== HttpStatus.UNAUTHORIZED.value()){
						log.error("Gateway authentication failed: HTTP {}", HttpStatus.UNAUTHORIZED);
						throw new GatewayException("Gateway authentication failed: HTTP " + HttpStatus.UNAUTHORIZED, e);
					}
				}
				//Standard exception handling for other causes
				String msg = (e.getCause()==null?e.getMessage():e.getCause().getMessage());
				log.warn("Gateway Server connection failed: {} (attempt {})", msg, ++attempts);
				if (attempts >= maxretries)
					throw new GatewayException("Gateway server connection failed: " + msg, e);
			}
			try{
				long interval = getSleepInterval(attempts);
				log.info("Sleeping {} seconds before retrying", interval/1000.0);
				Thread.sleep(interval);
			}catch(Exception e){};
		}while(true);
	}
	
	private long getSleepInterval(int attempts) {
		return (long) (sleepinterval * Math.pow(attempts,sleepfactor));
	}
	
	public void setSleepInterval(int sleepinterval){
		this.sleepinterval = sleepinterval;
	}
	public void setSleepFactor(double d){
		this.sleepfactor = d;
	}
	public void setMaxRetries(int maxretries){
		this.maxretries = maxretries;
	}

	private void checkStatusCode(HttpStatus statusCode, int attempts) throws GatewayException {
		//Check for server connection errors, allow for retries.
		if (statusCode.is5xxServerError()) {
			log.warn("Gateway Server is not available: HTTP {} (attempt {})", statusCode, attempts);
			if (attempts < maxretries)
				return;
			throw new GatewayException("Gateway server is not available: HTTP " + statusCode);
		}

		//authentication error, do not retry
		if(statusCode.equals(HttpStatus.UNAUTHORIZED) || statusCode.equals(HttpStatus.FORBIDDEN)){
			log.error("Gateway authentication failed: HTTP {}", statusCode);
			throw new GatewayException("Gateway authentication failed: HTTP " + statusCode);
		}

		//service not found, do not retry
		if (statusCode.equals(HttpStatus.NOT_FOUND)) {
			log.error("Gateway service not found: HTTP {}", statusCode);
			throw new GatewayException("Gateway service not found: HTTP " + statusCode);
		}
		
		//service not found, do not retry
		if (statusCode.equals(HttpStatus.BAD_REQUEST)) {
			log.error("Gateway bad request: HTTP {}", statusCode);
			throw new GatewayException("Gateway bad request: HTTP " + statusCode);
		}

		//unexpected status code, do not retry
		log.error("Gateway unexpected status code: HTTP {}", statusCode);
		throw new GatewayException("Gateway unexpected status code: HTTP " + statusCode);
	}

	private HttpHeaders generateBasicHeaders(){
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
		return headers;
	}
	
	private HttpHeaders generateBasicHeaders(String username, String password){
		String auth = username + ":" + password;
		String basic = Base64Utils.encodeToString(auth.getBytes());

		HttpHeaders headers = generateBasicHeaders();
		headers.add("Authorization", "Basic " + basic);
		return headers;
	}
	

	private RestTemplate getRestTemplate(){
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setErrorHandler(new GatewayErrorHandler());
		return restTemplate;
	}

}

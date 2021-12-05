package com.opentext.exstream.proximus.server;

import java.net.HttpRetryException;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.opentext.exstream.proximus.actions.OtdsActions;
import com.opentext.exstream.proximus.common.CustomLogger;

public class ExstreamSession {
	private static final CustomLogger log = new CustomLogger(ExstreamSession.class);
	private String otdsMtaUrl;
	private String otdsTenantUrl;
	private String sgwUrl;
	private String username;
	private String password;
	private int maxretries = 5;
	private int sleepinterval = 1000;
	private double sleepfactor = 2;

	private String otdsTicket = null;

	public ExstreamSession(String otdsMtaUrl, String otdsTenantUrl, String sgwUrl, String username, String password) {
		this.otdsMtaUrl = otdsMtaUrl;
		this.otdsTenantUrl = otdsTenantUrl;
		this.sgwUrl = sgwUrl;
		this.username = username;
		this.password = password;
	}

	public String getOtdsMtaUrl() {
		return otdsMtaUrl;
	}

	public void setOtdsMtaUrl(String otdsMtaUrl) {
		this.otdsMtaUrl = otdsMtaUrl;
	}

	public String getOtdsTenantUrl() {
		return otdsTenantUrl;
	}

	public void setOtdsTenantUrl(String otdsTenantUrl) {
		this.otdsTenantUrl = otdsTenantUrl;
	}

	public String getSgwUrl() {
		return sgwUrl;
	}

	public void setSgwUrl(String sgwUrl) {
		this.sgwUrl = sgwUrl;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public <T> ResponseEntity<T> processRequest(String url, HttpMethod method, HttpEntity<?> entity, Class<T> clazz) throws ExstreamException {
		return processRequest(url, method, entity, clazz, true);
	}

	public <T> ResponseEntity<T> processRequest(String url, HttpMethod method, HttpEntity<?> entity, Class<T> clazz, boolean allowNewTicket) throws ExstreamException {
		log.info("Service request [{}]: {}", method.toString(), url);

		RestTemplate restTemplate = getRestTemplate();
		int attempts = 0;
		boolean newTicket = false;
		do {
			try {
				ResponseEntity<T> httpResponse = restTemplate.exchange(url, method, entity, clazz);

				if (httpResponse.getStatusCode().is2xxSuccessful()) {
					return (ResponseEntity<T>) httpResponse;
				}

				if (allowNewTicket && httpResponse.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
					newTicket = true;
				} else {
					checkStatusCode(httpResponse.getStatusCode(), ++attempts);
				}

			} catch (ExstreamException e) {
				throw e;
			} catch (RestClientException e) {
				// HTTP 401 with POST can cause ResourceAccessException, trap error here
				Throwable t = e.getCause();
				if (t != null && t instanceof HttpRetryException) {
					if (((HttpRetryException)t).responseCode() == HttpStatus.UNAUTHORIZED.value()) {
						if (allowNewTicket) {
							newTicket = true;
						} else {
							log.error("Exstream authentication failed: HTTP {}", HttpStatus.UNAUTHORIZED);
							throw new ExstreamException("Exstream authentication failed: HTTP " + HttpStatus.UNAUTHORIZED, e);
						}
					}
				}
				// Standard exception handling for other causes
				if (!newTicket) {
					String msg = (e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
					log.warn("Exstream server connection failed: {} (attempt {})", msg, ++attempts);
					if (attempts >= maxretries) {
						throw new ExstreamException("Exstream server connection failed: " + msg, e);
					}
				}
			}

			if (newTicket) {
				log.info("Retry with a new OTDS ticket");
				otdsTicket = null;
				HttpHeaders headers = generateHeaders();

				// Clone headers from original request except OTDS ticket
				for (String key : entity.getHeaders().keySet()) {
					if (!"OTDSTicket".equals(key)) {
						headers.addAll(key, entity.getHeaders().get(key));
					}
				}

				HttpEntity<?> request = new HttpEntity<>(entity.getBody(), headers);
				return processRequest(url, method, request, clazz, false);
			}

			try {
				long interval = getSleepInterval(attempts);
				log.info("Sleeping {} seconds before retrying", interval / 1000.0);
				Thread.sleep(interval);
			} catch (Exception e) {};
		} while (true);
	}

	private long getSleepInterval(int attempts) {
		return (long) (sleepinterval * Math.pow(attempts, sleepfactor));
	}

	public void setSleepInterval(int sleepinterval) {
		this.sleepinterval = sleepinterval;
	}

	public void setSleepFactor(double d) {
		this.sleepfactor = d;
	}

	public void setMaxRetries(int maxretries) {
		this.maxretries = maxretries;
	}

	private void checkStatusCode(HttpStatus statusCode, int attempts) throws ExstreamException {
		// Check for server connection errors, allow for retries
		if (statusCode.is5xxServerError()) {
			log.warn("Exstream server is not available: HTTP {} (attempt {})", statusCode, attempts);
			if (attempts < maxretries)
				return;
			throw new ExstreamException("Exstream server is not available: HTTP " + statusCode);
		}

		// Authentication error, do not retry
		if (statusCode.equals(HttpStatus.UNAUTHORIZED) || statusCode.equals(HttpStatus.FORBIDDEN)) {
			log.error("Exstream authentication failed: HTTP {}", statusCode);
			throw new ExstreamException("Exstream authentication failed: HTTP " + statusCode);
		}

		// Service not found, do not retry
		if (statusCode.equals(HttpStatus.NOT_FOUND)) {
			log.error("Exstream service not found: HTTP {}", statusCode);
			throw new ExstreamException("Exstream service not found: HTTP " + statusCode);
		}

		// Service not found, do not retry
		if (statusCode.equals(HttpStatus.BAD_REQUEST)) {
			log.error("Exstream bad request: HTTP {}", statusCode);
			throw new ExstreamException("Exstream bad request: HTTP " + statusCode);
		}

		// Unexpected status code, do not retry
		log.error("Exstream unexpected status code: HTTP {}", statusCode);
		throw new ExstreamException("Exstream unexpected status code: HTTP " + statusCode);
	}

	public HttpHeaders generateHeaders() throws ExstreamException {
		if (otdsTicket == null) {
			otdsTicket = OtdsActions.otdsCredentials(this);
		}
		HttpHeaders headers = new HttpHeaders();
		headers.add("OTDSTicket", otdsTicket);
		return headers;
	}

	private RestTemplate getRestTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setErrorHandler(new ExstreamErrorHandler());
		return restTemplate;
	}

}

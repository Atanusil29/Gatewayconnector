package com.opentext.exstream.proximus.jobtracking;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.JsonNode;
import com.opentext.exstream.proximus.actions.OtdsActions;
import com.opentext.exstream.proximus.actions.SgwDocumentsActions;
import com.opentext.exstream.proximus.actions.SgwTrackersActions;
import com.opentext.exstream.proximus.common.JsonUtil;
import com.opentext.exstream.proximus.server.ExstreamSession;
import com.opentext.exstream.proximus.struct.exstream.SgwDocument;
import com.opentext.exstream.proximus.struct.exstream.SgwTracker;
import com.opentext.exstream.proximus.struct.exstream.WhereCondition;


/**
 * Unit test for simple App.
 */
public class TestRetryTracker extends TestExstreamDefaults
{	
	public TestRetryTracker(){
		setupSSLProperties();
		//setupTLSSettings();
	}

	@Test
	public void test_retry_process()
    {
		String[] args = {};
		try {
			RetryTracker.process(args);
			Assert.assertTrue(true);

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
    }

	@Test
	public void test_retry()
    {
		try {
			//ExstreamSession exstreamSession = new ExstreamSession(otdsMtaUrl, otdsTenantUrl, sgwUrl, username, password);
			RetryTracker retryTracker = new RetryTracker();
			retryTracker.initialize();
			//retryTracker.setSession(exstreamSession);
			
			FileUtils.copyFile(new File("src/test/resources/SampleNotification.xml"), new File("target/test-ready", "2222-inprogress.xml"));
			FileUtils.copyFile(new File("src/test/resources/SampleNotification.xml"), new File("target/test-ready", "3333-error.xml"));
			FileUtils.copyFile(new File("src/test/resources/SampleNotification.xml"), new File("target/test-ready", "4444-success.xml"));
			FileUtils.copyFile(new File("src/test/resources/SampleNotification.xml"), new File("target/test-ready", "5555-error.xml"));
		
			
			List<String> retryTrackers = new ArrayList<String>();
			List<String> processedTrackers = new ArrayList<String>();

			retryTrackers.addAll(Arrays.asList("1111-processed","2222-inprogress","3333-error","4444-success","5555-error"));
			processedTrackers.addAll(Arrays.asList("1111-processed"));
			retryTracker.processTrackers(retryTrackers, processedTrackers);

			Assert.assertTrue(true);

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
    }

	@Test
	public void test_otds_invalid()
    {
		ExstreamSession exstreamSession = new ExstreamSession(otdsMtaUrl, otdsTenantUrl, sgwUrl, username, "invalid");

		try {
			JsonNode otdsVersion = OtdsActions.otdsVersion(exstreamSession);
			System.out.println(JsonUtil.toPrettyString(otdsVersion));

			@SuppressWarnings("unused")
			String otdsTicket = OtdsActions.otdsCredentials(exstreamSession);
			Assert.fail("exception expected");
			
		} catch (Exception e) {
			//e.printStackTrace();
			Assert.assertEquals("Exstream authentication failed: HTTP "+ HttpStatus.UNAUTHORIZED, e.getMessage());
		}
    }

	@Test
	public void test_sgw_getDocuments()
    {
		ExstreamSession exstreamSession = new ExstreamSession(otdsMtaUrl, otdsTenantUrl, sgwUrl, username, password);

		try {
			JsonNode otdsVersion = OtdsActions.otdsVersion(exstreamSession);
			System.out.println(JsonUtil.toPrettyString(otdsVersion));

			String otdsTicket = OtdsActions.otdsCredentials(exstreamSession);
			System.out.println("OTDS ticket: " + otdsTicket);

			List<WhereCondition> params = new ArrayList<WhereCondition>();
			params.add(new WhereCondition("EQ", "processingattemptid", "B4A9E048-1ED3-46C0-B113-ECF8DC42B7D4"));
			params.add(new WhereCondition("EQ", "processingstate", "5"));

			List<SgwDocument> docs = SgwDocumentsActions.getDocuments(exstreamSession, params);
			System.out.println("Get documents: " + docs.size());


			Assert.assertTrue(docs.size() > 0);

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
    }

	@Test
	public void test_sgw_getDocumentsUnauthorised()
    {
		ExstreamSession exstreamSession = new ExstreamSession(otdsMtaUrl, otdsTenantUrl, sgwUrl, "browser", password);

		try {
			List<WhereCondition> params = new ArrayList<WhereCondition>();
			params.add(new WhereCondition("EQ", "processingattemptid", "B4A9E048-1ED3-46C0-B113-ECF8DC42B7D4"));
			params.add(new WhereCondition("EQ", "processingstate", "5"));

			@SuppressWarnings("unused")
			List<SgwDocument> docs = SgwDocumentsActions.getDocuments(exstreamSession, params);
			Assert.fail("exception expected");
			
		} catch (Exception e) {
			//e.printStackTrace();
			Assert.assertEquals("Exstream authentication failed: HTTP "+ HttpStatus.UNAUTHORIZED, e.getMessage());
		}
    }
	
	@Test
	public void test_sgw_updateDocuments()
    {
		ExstreamSession exstreamSession = new ExstreamSession(otdsMtaUrl, otdsTenantUrl, sgwUrl, username, password);

		try {
			JsonNode otdsVersion = OtdsActions.otdsVersion(exstreamSession);
			System.out.println(JsonUtil.toPrettyString(otdsVersion));

			String otdsTicket = OtdsActions.otdsCredentials(exstreamSession);
			System.out.println("OTDS ticket: " + otdsTicket);

			int count = SgwDocumentsActions.updateDocuments(exstreamSession, "B4A9E048-1ED3-46C0-B113-ECF8DC42B7D4", "5", "success");
			System.out.println("update count: " + count);

			Assert.assertTrue(count > 0);

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
    }

	@Test
	public void test_sgw_getTrackers()
    {
		ExstreamSession exstreamSession = new ExstreamSession(otdsMtaUrl, otdsTenantUrl, sgwUrl, username, password);

		try {
			JsonNode otdsVersion = OtdsActions.otdsVersion(exstreamSession);
			System.out.println(JsonUtil.toPrettyString(otdsVersion));

			String otdsTicket = OtdsActions.otdsCredentials(exstreamSession);
			System.out.println("OTDS ticket: " + otdsTicket);

			String trackerId = "33AAAFF6-DA13-7B42-AAE4-048074BE67C5";

			SgwTracker tracker = SgwTrackersActions.getTrackers(exstreamSession, trackerId);

			Assert.assertEquals(tracker.getField("trackerid"), trackerId);
			//Assert.assertTrue(tracker != null);

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
    }

	@Test
	public void test_unknownTracker()
    {
		try {
			//ExstreamSession exstreamSession = new ExstreamSession(otdsMtaUrl, otdsTenantUrl, sgwUrl, username, password);
			RetryTracker retryTracker = new RetryTracker();
			retryTracker.initialize();
			//retryTracker.setSession(exstreamSession);
			
			
			List<String> retryTrackers = new ArrayList<String>();
			List<String> processedTrackers = new ArrayList<String>();

			retryTrackers.addAll(Arrays.asList("0000-empty","1111-processed","2222-inprogress","3333-error","4444-success","5555-error"));
			processedTrackers.addAll(Arrays.asList("1111-processed"));
			retryTracker.processTrackers(retryTrackers, processedTrackers);

			Assert.assertTrue(true);

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
    }

}

package com.opentext.exstream.proximus.jobtracking;

import java.util.ArrayList;
import java.util.List;

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
public class TestExstreamMockService extends TestExstreamDefaults
{	
	public TestExstreamMockService(){
		setupSSLProperties();
		//setupTLSSettings();
	}

	@Test
	public void test_otds()
    {
		ExstreamSession exstreamSession = new ExstreamSession(otdsMtaUrl, otdsTenantUrl, sgwUrl, username, password);

		try {
			JsonNode otdsVersion = OtdsActions.otdsVersion(exstreamSession);
			System.out.println(JsonUtil.toPrettyString(otdsVersion));

			String otdsTicket = OtdsActions.otdsCredentials(exstreamSession);
			System.out.println("OTDS ticket: " + otdsTicket);

			Assert.assertTrue( otdsTicket != null && !otdsTicket.equals(""));

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

}

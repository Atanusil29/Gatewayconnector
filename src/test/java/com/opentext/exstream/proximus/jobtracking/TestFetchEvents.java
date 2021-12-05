package com.opentext.exstream.proximus.jobtracking;


import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class TestFetchEvents extends TestExstreamDefaults
{	
	public TestFetchEvents(){
		setupSSLProperties();
		//setupTLSSettings();
	}

	@Test
	public void test_fetch_last()
    {
		String[] args = {"-last"};
		try {
			//ExstreamSession exstreamSession = new ExstreamSession(otdsMtaUrl, otdsTenantUrl, sgwUrl, username, password);
			FetchEvents.process(args);

			Assert.assertTrue(true);

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
    }

	@Test
	public void test_fetch_min()
    {
		String[] args = {"-minutes", "10"};
		try {
			//ExstreamSession exstreamSession = new ExstreamSession(otdsMtaUrl, otdsTenantUrl, sgwUrl, username, password);
			FetchEvents.process(args);

			Assert.assertTrue(true);

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
    }

}

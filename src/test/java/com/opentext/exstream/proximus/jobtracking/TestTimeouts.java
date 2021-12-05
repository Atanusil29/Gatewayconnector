package com.opentext.exstream.proximus.jobtracking;

import org.junit.Assert;
import org.junit.Test;


/**
 * Unit test for simple App.
 */
public class TestTimeouts extends TestExstreamDefaults
{	
	public TestTimeouts(){
		setupSSLProperties();
		//setupTLSSettings();
	}

	@Test
	public void test_timeouts()
    {
		String[] args = { "-minutes", "60" };
		try {
			Timeouts.process(args);
			Assert.assertTrue(true);

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
    }


}

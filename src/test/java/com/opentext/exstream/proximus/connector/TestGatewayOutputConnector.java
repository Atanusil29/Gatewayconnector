
package com.opentext.exstream.proximus.connector;

import org.junit.Assert;
import org.junit.Test;
import streamserve.connector.TestConfigVals;

/**
 * Modifies the PDF file and inserts crop, bleed, trim and art boxes
 */
public class TestGatewayOutputConnector
{
    
    @Test
    public final void testOutputConnector_SENT() throws Exception {
        try{
        	boolean status = testOutputConnector("False", "test_to_1@opentext.com;test_to_2@optentext.com");
	        Assert.assertTrue(status);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
    }
    
    @Test
    public final void testOutputConnector_UNSENT() throws Exception {
        try{
        	boolean status = testOutputConnector("True", "test_to_1@opentext.com;test_to_2@optentext.com");
	        Assert.assertFalse(status);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
    }

    @Test
    public final void testOutputConnector_UNSENT_v2() throws Exception {
        try{
        	boolean status = testOutputConnector("False", "invalid.address.com");
	        Assert.assertFalse(status);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
    }

    public final boolean testOutputConnector(String externalCompletion, String emailTo) throws Exception {
        TestConfigVals configVals = getDefaultConfig();

        configVals.setValue(GatewayOutputConnector.PROPERTY_EXTERNAL_JOB_COMPLETION, externalCompletion);
        configVals.setValue(GatewayOutputConnector.PROPERTY_TO_RECIPIENTS, emailTo);

        byte[] fileContent = "<html><body><p>Simple test email</p></body></html>".getBytes();

        boolean status = true;
        GatewayOutputConnector connector = new GatewayOutputConnector ();
        status &= connector.strsoStartJob(configVals);
        status &= connector.strsoOpen(configVals);
        status &= connector.strsoWrite(fileContent);
        status &= connector.strsoClose(configVals);
        status &= connector.strsoEndJob();
	    
        return status;
    }


    private TestConfigVals getDefaultConfig(){
        TestConfigVals configVals = new TestConfigVals();

        configVals.setValue(GatewayOutputConnector.PROPERTY_CLIENT_NAME, "CDCX");
        configVals.setValue(GatewayOutputConnector.PROPERTY_CONSUMER_NAME, "CVM");
        configVals.setValue(GatewayOutputConnector.PROPERTY_CHANNEL_NAME, "EMAIL");
        
        configVals.setValue(GatewayOutputConnector.PROPERTY_BODY_PATHS, "output.html");
        configVals.setValue(GatewayOutputConnector.PROPERTY_BODY_ENCODING, "UTF-8");
        configVals.setValue(GatewayOutputConnector.PROPERTY_BODY_LANGUAGE, "EN");

        configVals.setValue(GatewayOutputConnector.PROPERTY_FROM_SENDER, "test_from@opentext.com");
        configVals.setValue(GatewayOutputConnector.PROPERTY_REPLY_TO, "test_reply@opentext.com");
        //configVals.setValue(GatewayOutputConnector.PROPERTY_TO_RECIPIENTS, "test_to_1@opentext.com;test_to_2@optentext.com");
        configVals.setValue(GatewayOutputConnector.PROPERTY_CC_RECIPIENTS, "test_cc_1@opentext.com;test_cc_2@optentext.com");
        configVals.setValue(GatewayOutputConnector.PROPERTY_BCC_RECIPIENTS, "test_bcc_1@opentext.com;test_bcc_2@optentext.com");
        configVals.setValue(GatewayOutputConnector.PROPERTY_SUBJECT, "Test Subject - Unit Test");

        configVals.setValue(GatewayOutputConnector.PROPERTY_PRIORITY, "LOW");
        configVals.setValue(GatewayOutputConnector.PROPERTY_TIME_TO_LIVE, "5");
        configVals.setValue(GatewayOutputConnector.PROPERTY_ACCEPT_REPLY, "False");
        configVals.setValue(GatewayOutputConnector.PROPERTY_BLACKOUT_START, "11");
        configVals.setValue(GatewayOutputConnector.PROPERTY_BLACKOUT_END, "22");
        configVals.setValue(GatewayOutputConnector.PROPERTY_TRACKING, "True");
        //workflowId=$Workflow_ID;requestId=$Unique_ID_UUID;trackerId=$trackerID;sequenceNr=$sequence_number
        configVals.setValue(GatewayOutputConnector.PROPERTY_TAGS, "requestId=11111111;workflowId=2;sequenceNr=3;trackerId=4444");
        configVals.setValue(GatewayOutputConnector.PROPERTY_SHORTENING, "True");

        configVals.setValue(GatewayOutputConnector.PROPERTY_EXTERNAL_JOB_COMPLETION, "True");
        configVals.setValue(GatewayOutputConnector.PROPERTY_PARTID_TAG_NAME, "partId");

        return configVals;
        
    }
    
}


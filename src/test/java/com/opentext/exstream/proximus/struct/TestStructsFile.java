package com.opentext.exstream.proximus.struct;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opentext.exstream.proximus.struct.EventRequestType;
import com.opentext.exstream.proximus.struct.EventResponseType;
import com.opentext.exstream.proximus.struct.SendMessageType;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Assert;

/**
 * Unit test for event request and response structures.
 */
public class TestStructsFile 
{
	@Test
	public void test_sampleFetchEventsRequest()
    {
		String filename  = "src/test/resources/cdcgFetchEvents-request.json";
		ObjectMapper mapper = new ObjectMapper();
		EventRequestType request = null;

		//Object to JSON in file
		try {
			request = mapper.readValue(new File(filename), EventRequestType.class);
			String actual = request.toString();
			String expected = mapper.readTree(new File(filename)).toString();
	        Assert.assertEquals(expected,actual);

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
    }


	@Test
	public void test_sampleFetchEventsResponse()
    {
		String filename = "src/test/resources/cdcgFetchEvents-response.json";
		ObjectMapper mapper = new ObjectMapper();
		EventResponseType response = null;

		//Object to JSON in file
		try {
			response = mapper.readValue(new File(filename), EventResponseType.class);
			String actual = response.toString();
			String expected = mapper.readTree(new File(filename)).toString();
	        Assert.assertEquals(expected,actual);

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
    }

	@Test
	public void test_sampleSendMessageRequest()
    {
		String filename  = "src/test/resources/cdcgSendMessage-request.json";
		ObjectMapper mapper = new ObjectMapper();
		SendMessageType request = null;

		//Object to JSON in file
		try {
			request = mapper.readValue(new File(filename), SendMessageType.class);
			String actual = request.toString();
			String expected = mapper.readTree(new File(filename)).toString();
	        Assert.assertEquals(expected,actual);

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}

    }
	
	@Test
	public void test_sampleNotication()
    {
		File inputFile  = new File("src/test/resources/SampleNotification.xml");
		File outputFile  = new File("target/test-output/SampleNotification.xml");
		new File("target/test-output").mkdirs();

		outputFile.delete();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		String datetime = sdf.format(new Date());
		String workflowstep = "WORKFLOW_COMPLETED";
		String message = "success message";
		
		try {
			Notification notification = Notification.unmarshal(inputFile);
			notification.setEventTime(datetime);
			notification.setStepTime(datetime);
			notification.setStep(workflowstep);
			notification.setQualifier(message);

			notification.marshal(outputFile);
			
	        Assert.assertTrue(outputFile.exists());
	        Assert.assertTrue(outputFile.length() > 500);


		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}

    }

	@Test
	public void test_sampleNoticationFull()
    {
		File inputFile  = new File("src/test/resources/SampleNotificationFull.xml");
		File outputFile  = new File("target/test-output/SampleNotificationFull.xml");
		new File("target/test-output").mkdirs();

		outputFile.delete();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		String datetime = sdf.format(new Date());
		String workflowstep = "WORKFLOW_COMPLETED";
		String message = "success message";
		
		try {
			Notification notification = Notification.unmarshal(inputFile);
			notification.setEventTime(datetime);
			notification.setStepTime(datetime);
			notification.setStep(workflowstep);
			notification.setQualifier(message);
			notification.setWorkflowStarted("No");
			notification.setChannelSelected("No");

			notification.marshal(outputFile);
			
			//XXX Full Notification structure is not defined, so outputFile does not match.
	        Assert.assertTrue(outputFile.exists());
	        Assert.assertTrue(outputFile.length() > 500);


		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}

    }

	@Test
	public void test_sampleNoticationSent()
    {
		File inputFile  = new File("src/test/resources/SampleNotificationSent.xml");
		File outputFile  = new File("target/test-output/SampleNotificationSent.xml");
		new File("target/test-output").mkdirs();

		outputFile.delete();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		String datetime = sdf.format(new Date());
		String workflowstep = "WORKFLOW_COMPLETED";
		String message = "success message";
		
		try {
			Notification notification = Notification.unmarshal(inputFile);
			notification.setEventTime(datetime);
			notification.setStepTime(datetime);
			notification.setStep(workflowstep);
			notification.setQualifier(message);

			notification.marshal(outputFile);

	        Assert.assertTrue(outputFile.exists());
	        Assert.assertTrue(outputFile.length() > 500);


		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}

    }

}

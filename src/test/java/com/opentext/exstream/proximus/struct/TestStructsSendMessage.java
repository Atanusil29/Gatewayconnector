package com.opentext.exstream.proximus.struct;

import org.junit.Test;

import com.opentext.exstream.proximus.struct.BlackoutPeriodType;
import com.opentext.exstream.proximus.struct.MessageFileType;
import com.opentext.exstream.proximus.struct.MetaDataType;
import com.opentext.exstream.proximus.struct.RecipientType;
import com.opentext.exstream.proximus.struct.SendMessageType;
import com.opentext.exstream.proximus.struct.TagType;
import com.opentext.exstream.proximus.struct.ToRecipientType;

import org.junit.Assert;

/**
 * Unit test for simple App.
 */
public class TestStructsSendMessage 
{
	@Test
	public void test_request()
    {
		SendMessageType input = new SendMessageType();
		String json = input.toString();
        Assert.assertTrue("{\"clientName\":null,\"channelName\":null,\"messageFile\":null,\"metaData\":null}".equals(json));
    }

	@Test
	public void test_request0()
    {
		SendMessageType input = new SendMessageType();
		input.setClientName("test client");
		input.setChannelName("test channel");
		input.setMessageFile(new MessageFileType[0]);
		input.setMetaData(new MetaDataType());
		String json = input.toString();
        Assert.assertTrue("{\"clientName\":\"test client\",\"channelName\":\"test channel\",\"messageFile\":[],\"metaData\":{\"toRecipient\":null}}".equals(json));
    }

	@Test
	public void test_request1()
    {
		SendMessageType input = new SendMessageType();
		input.setClientName("test client");
		input.setChannelName("test channel");
		input.setMessageFile(new MessageFileType[]{new MessageFileType()});
		input.setMetaData(new MetaDataType());
		String json = input.toString();
        Assert.assertTrue("{\"clientName\":\"test client\",\"channelName\":\"test channel\",\"messageFile\":[{\"fileName\":null,\"fileCategory\":null,\"fileFormat\":null}],\"metaData\":{\"toRecipient\":null}}".equals(json));
    }

	@Test
	public void test_requestMetaData()
    {
		MetaDataType metaData = new MetaDataType();
		String json = metaData.toString();
        Assert.assertTrue("{\"toRecipient\":null}".equals(json));
    }

	@Test
	public void test_requestMetaDataFull()
    {
		MetaDataType metaData = new MetaDataType();
		metaData.setFromSender("from");
		metaData.setReplyTo("reply");
		metaData.setToRecipient(new ToRecipientType[]{new ToRecipientType("")});
		metaData.setCcRecipient(new RecipientType[]{new RecipientType("")});
		metaData.setBccRecipient(new RecipientType[]{new RecipientType("")});
		metaData.setSubject("subject");
		metaData.setPriority("HIGH");
		metaData.setTimeToLive(5);
		metaData.setAcceptReply(true);
		metaData.setBlackoutPeriod(new BlackoutPeriodType(6,10, null));
		metaData.setTrackingOn(true);
		metaData.setTag(new TagType[]{new TagType("tagname","tagvalue")});
		metaData.setShorteningOn(true);
		
		String json = metaData.toString();
        Assert.assertTrue("{\"fromSender\":\"from\",\"replyTo\":\"reply\",\"toRecipient\":[{\"contact\":\"\",\"isPrimary\":false}],\"ccRecipient\":[{\"contact\":\"\"}],\"bccRecipient\":[{\"contact\":\"\"}],\"subject\":\"subject\",\"priority\":\"HIGH\",\"timeToLive\":5,\"acceptReply\":true,\"blackoutPeriod\":{\"startHour\":6,\"endHour\":10},\"trackingOn\":true,\"tag\":[{\"name\":\"tagname\",\"value\":\"tagvalue\"}],\"shorteningOn\":true}".equals(json));
    }

	@Test
	public void test_requestMessageFile()
    {
		MessageFileType messageFile = new MessageFileType();
		String json = messageFile.toString();
        Assert.assertTrue("{\"fileName\":null,\"fileCategory\":null,\"fileFormat\":null}".equals(json));
    }

	@Test
	public void test_jsonPrettyPrint()
    {
		MessageFileType messageFile = new MessageFileType();
		String json = messageFile.toPrettyString();
		String expected = "{\r\n  \"fileName\" : null,\r\n  \"fileCategory\" : null,\r\n  \"fileFormat\" : null\r\n}";
        Assert.assertTrue(expected.equals(json));
    }

}

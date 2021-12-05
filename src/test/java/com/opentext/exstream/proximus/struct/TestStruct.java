package com.opentext.exstream.proximus.struct;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.opentext.exstream.proximus.struct.BlackoutPeriodType;
import com.opentext.exstream.proximus.struct.MessageFileType;
import com.opentext.exstream.proximus.struct.MetaDataType;
import com.opentext.exstream.proximus.struct.RecipientType;
import com.opentext.exstream.proximus.struct.SendMessageType;
import com.opentext.exstream.proximus.struct.TagType;
import com.opentext.exstream.proximus.struct.ToRecipientType;

public class TestStruct {

	private final byte[] content = "1234567890".getBytes();

	@Test
	public final void testStruct() throws Exception {
		SendMessageType sendMessage = new SendMessageType();
		sendMessage.setClientName("OpenText");
		sendMessage.setChannelName("EMAIL");

		MessageFileType body = new MessageFileType();
		body.setFileName("body.html");
		body.setFileCategory("BODY");
		body.setFileFormat("HTML");
		body.setFileEncoding("ISO-8859-1");
		body.setFileAlias("alias");
		body.setFileLanguage("EN");
		body.setContent(content);

		MessageFileType[] messages = new MessageFileType[4];
		messages[0] = body;

		for (int i = 0; i < 3; i++) {
			MessageFileType attachment = new MessageFileType();
			attachment.setFileName("attachment-" + (i + 1) + ".pdf");
			attachment.setFileCategory("ATTACHMENT");
			attachment.setFileFormat("PDF");
			attachment.setFileEncoding("ISO-8859-1");
			attachment.setFileAlias("alias");
			attachment.setFileLanguage("EN");
			attachment.setContent(content);
			messages[1 + i] = attachment;
		}

		sendMessage.setMessageFile(messages);

		MetaDataType metaData = new MetaDataType();
		metaData.setFromSender("from");
		metaData.setReplyTo("reply-to");

		ToRecipientType[] toRecipient = new ToRecipientType[4];
		toRecipient[0] = new ToRecipientType();
		toRecipient[0].setContact("to-1");
		toRecipient[0].setPrimary(true);
		toRecipient[0].setKind("GID-1");
		toRecipient[0].setReference("reference-1");
		toRecipient[1] = new ToRecipientType();
		toRecipient[1].setContact("to-2");
		toRecipient[2] = new ToRecipientType();
		toRecipient[2].setContact("to-3");
		toRecipient[2].setKind("MSISDN-3");
		toRecipient[3] = new ToRecipientType();
		toRecipient[3].setContact("to-4");
		toRecipient[3].setReference("reference-4");
		metaData.setToRecipient(toRecipient);

		RecipientType[] ccRecipient = new RecipientType[2];
		ccRecipient[0] = new RecipientType();
		ccRecipient[0].setContact("cc-1");
		ccRecipient[1] = new RecipientType();
		ccRecipient[1].setContact("cc-2");
		metaData.setCcRecipient(ccRecipient);

		RecipientType[] bccRecipient = new RecipientType[2];
		bccRecipient[0] = new RecipientType();
		bccRecipient[0].setContact("bcc-1");
		bccRecipient[1] = new RecipientType();
		bccRecipient[1].setContact("bcc-2");
		metaData.setCcRecipient(bccRecipient);

		metaData.setSubject("subject");
		metaData.setPriority("priority");
		metaData.setTimeToLive(42);
		metaData.setAcceptReply(true);

		BlackoutPeriodType blackoutPeriod = new BlackoutPeriodType();
		blackoutPeriod.setStartHour(3);
		blackoutPeriod.setEndHour(8);
		metaData.setBlackoutPeriod(blackoutPeriod);

		metaData.setTrackingOn(true);

		TagType[] tagArray = new TagType[2];
		tagArray[0] = new TagType();
		tagArray[0].setName("Name-1");
		tagArray[0].setValue("Value-1");
		tagArray[1] = new TagType();
		tagArray[1].setName("Name-2");
		tagArray[1].setValue("Value-2");
		metaData.setTag(tagArray);

		metaData.setShorteningOn(true);

		sendMessage.setMetaData(metaData);
		displayJson(sendMessage);
	}

	private void displayJson(Object obj) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		String json = mapper.writeValueAsString(obj);
		System.out.println(json);
	}

}

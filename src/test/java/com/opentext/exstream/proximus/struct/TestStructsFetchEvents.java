package com.opentext.exstream.proximus.struct;

import org.junit.Test;

import com.opentext.exstream.proximus.struct.EventListType;
import com.opentext.exstream.proximus.struct.EventRequestType;
import com.opentext.exstream.proximus.struct.EventResponseType;
import com.opentext.exstream.proximus.struct.EventType;
import com.opentext.exstream.proximus.struct.ResultType;
import com.opentext.exstream.proximus.struct.TagType;

import org.junit.Assert;

/**
 * Unit test for simple App.
 */
public class TestStructsFetchEvents 
{
	@Test
	public void test_request()
    {
		EventRequestType input = new EventRequestType();
		String json = input.toString();
        Assert.assertTrue("{\"clientName\":null,\"channelName\":null,\"eventPageSize\":256}".equals(json));
    }
	@Test
	public void test_response()
    {
		EventResponseType job = new EventResponseType();
		String json = job.toString();
        Assert.assertTrue("{\"result\":null}".equals(json));
    }
	@Test
	public void test_responseResult()
    {
		EventResponseType job = new EventResponseType();
		job.setResult(new ResultType());
		String json = job.toString();
        Assert.assertTrue("{\"result\":{\"eventList\":null}}".equals(json));
    }
	@Test
	public void test_responseSelectionReference()
    {
		EventResponseType job = new EventResponseType();
		ResultType result = new ResultType();
		result.setSelectionReference("");
		job.setResult(result);
		String json = job.toString();
        Assert.assertTrue("{\"result\":{\"selectionReference\":\"\",\"eventList\":null}}".equals(json));
    }
	@Test
	public void test_responseEventListEmpty()
    {
		EventResponseType job = new EventResponseType();
		EventListType[] eventList = new EventListType[0];
		ResultType result = new ResultType();
		result.setEventList(eventList);
		job.setResult(result);
		String json = job.toString();
        Assert.assertTrue("{\"result\":{\"eventList\":[]}}".equals(json));
    }
	@Test
	public void test_responseEventList1()
    {
		EventResponseType job = new EventResponseType();
		EventListType[] eventList = new EventListType[1];
		eventList[0] = new EventListType();
		ResultType result = new ResultType();
		result.setEventList(eventList);
		job.setResult(result);
		String json = job.toString();
        Assert.assertTrue("{\"result\":{\"eventList\":[{\"clientName\":null,\"event\":null,\"channelName\":null,\"tag\":null}]}}".equals(json));
    }

	@Test
	public void test_event()
    {
		EventType event = new EventType();
		String json = event.toString();
        Assert.assertTrue("{\"kind\":null,\"timestamp\":null}".equals(json));
    }

	@Test
	public void test_tag()
    {
		TagType tag = new TagType();
		String json = tag.toString();
        Assert.assertTrue("{\"name\":null,\"value\":null}".equals(json));
    }
}

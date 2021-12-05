package com.opentext.exstream.proximus.struct;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "notification")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder={"UUID", "eventTime", "WorkflowStarted", "ChannelSelected",
		"workflowID", "trackerid", "step", "stepTime", "qualifier", "channelName", "Consumer_Name", "event_Type", "squenceNumber" })
public class Notification {

	private String UUID = "";
	private String eventTime = "";
	private String WorkflowStarted;
	private String ChannelSelected;
	private String workflowID = "";
	private String trackerid = "";
	private String step = "";
	private String stepTime = "";
	private String qualifier = "";
	private String channelName;
	private String Consumer_Name;
	private String event_Type = "";
	private String squenceNumber;
	
	public String getUUID() {
		return UUID;
	}
	public void setUUID(String uUID) {
		UUID = uUID;
	}
	public String getEventTime() {
		return eventTime;
	}
	public void setEventTime(String eventTime) {
		this.eventTime = eventTime;
	}
	public String getWorkflowStarted() {
		return WorkflowStarted;
	}
	public void setWorkflowStarted(String workflowStarted) {
		WorkflowStarted = workflowStarted;
	}
	public String getChannelSelected() {
		return ChannelSelected;
	}
	public void setChannelSelected(String channelSelected) {
		ChannelSelected = channelSelected;
	}
	public String getWorkflowID() {
		return workflowID;
	}
	public void setWorkflowID(String workflowID) {
		this.workflowID = workflowID;
	}
	public String getTrackerid() {
		return trackerid;
	}
	public void setTrackerid(String trackerid) {
		this.trackerid = trackerid;
	}
	public String getStep() {
		return step;
	}
	public void setStep(String step) {
		this.step = step;
	}
	public String getStepTime() {
		return stepTime;
	}
	public void setStepTime(String stepTime) {
		this.stepTime = stepTime;
	}
	public String getQualifier() {
		return qualifier;
	}
	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}
	public String getChannelName() {
		return channelName;
	}
	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}
	public String getConsumer_Name() {
		return Consumer_Name;
	}
	public void setConsumer_Name(String consumer_Name) {
		Consumer_Name = consumer_Name;
	}
	public String getEvent_Type() {
		return event_Type;
	}
	public void setEvent_Type(String event_Type) {
		this.event_Type = event_Type;
	}
	
	public String getSquenceNumber() {
		return squenceNumber;
	}
	public void setSquenceNumber(String squenceNumber) {
		this.squenceNumber = squenceNumber;
	}
	public String marshal() throws JAXBException{
        JAXBContext jaxbContext = JAXBContext.newInstance(Notification.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        StringWriter sw = new StringWriter();
        jaxbMarshaller.marshal(this, sw);
        String xml = sw.toString();
        return xml;
	}
	public void marshal(File file) throws JAXBException{
        JAXBContext jaxbContext = JAXBContext.newInstance(Notification.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        jaxbMarshaller.marshal(this, file); 
	}
	public static Notification unmarshal(String xml) throws JAXBException{
		JAXBContext jaxbContext;
	    jaxbContext = JAXBContext.newInstance(Notification.class);              
	    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
	    Notification notification = (Notification) jaxbUnmarshaller.unmarshal(new StringReader(xml));
	    return notification;
	}
	public static Notification unmarshal(File file) throws JAXBException{
		JAXBContext jaxbContext;
	    jaxbContext = JAXBContext.newInstance(Notification.class);              
	    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
	    Notification notification = (Notification) jaxbUnmarshaller.unmarshal(file);
	    return notification;
	}
	
}

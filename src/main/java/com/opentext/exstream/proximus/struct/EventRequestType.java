package com.opentext.exstream.proximus.struct;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
public class EventRequestType extends Default {

	private String clientName;
	private String channelName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String selectionReference;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private SelectionPeriodType selectionPeriod;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TagType[] tag;
    private int eventPageSize=256;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int eventPageNr=0;
    
	public String getClientName() {
		return clientName;
	}
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
	public String getChannelName() {
		return channelName;
	}
	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}
	public String getSelectionReference() {
		return selectionReference;
	}
	public void setSelectionReference(String selectionReference) {
		this.selectionReference = selectionReference;
	}
	public SelectionPeriodType getSelectionPeriod() {
		return selectionPeriod;
	}
	public void setSelectionPeriod(SelectionPeriodType selectionPeriod) {
		this.selectionPeriod = selectionPeriod;
	}
	public TagType[] getTag() {
		return tag;
	}
	public void setTag(TagType[] tag) {
		this.tag = tag;
	}
	public int getEventPageSize() {
		return eventPageSize;
	}
	public void setEventPageSize(int eventPageSize) {
		this.eventPageSize = eventPageSize;
	}
	public int getEventPageNr() {
		return eventPageNr;
	}
	public void setEventPageNr(int eventPageNr) {
		this.eventPageNr = eventPageNr;
	}

}

package com.opentext.exstream.proximus.struct;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
public class ResultType extends Default {
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String selectionReference;
	private EventListType[] eventList;
	
	public String getSelectionReference() {
		return selectionReference;
	}
	public void setSelectionReference(String selectionReference) {
		this.selectionReference = selectionReference;
	}
	public EventListType[] getEventList() {
		return eventList;
	}
	public void setEventList(EventListType[] eventList) {
		this.eventList = eventList;
	}	
}

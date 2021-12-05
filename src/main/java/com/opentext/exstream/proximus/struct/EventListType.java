package com.opentext.exstream.proximus.struct;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
public class EventListType extends Default {
	private String clientName;
	private EventType event;
	private String channelName;
	private TagType[] tag;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private MessageType message;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String click;

	public String getClientName() {
		return clientName;
	}
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
	public EventType getEvent() {
		return event;
	}
	public void setEvent(EventType event) {
		this.event = event;
	}
	public String getChannelName() {
		return channelName;
	}
	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}
	public TagType[] getTag() {
		return tag;
	}
	public void setTag(TagType[] tag) {
		this.tag = tag;
	}
	public MessageType getMessage() {
		return message;
	}
	public void setMessage(MessageType message) {
		this.message = message;
	}
	public String getClick() {
		return click;
	}
	public void setClick(String click) {
		this.click = click;
	}
}

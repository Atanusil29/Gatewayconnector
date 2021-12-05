package com.opentext.exstream.proximus.struct;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
public class SendMessageType extends Default {

	private String clientName;
	private String channelName;
    private MessageFileType[] messageFile;
    private MetaDataType metaData;
    
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
	public MessageFileType[] getMessageFile() {
		return messageFile;
	}
	public void setMessageFile(MessageFileType[] messageFile) {
		this.messageFile = messageFile;
	}
	public MetaDataType getMetaData() {
		return metaData;
	}
	public void setMetaData(MetaDataType metaData) {
		this.metaData = metaData;
	}
	
}

package com.opentext.exstream.proximus.struct;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MetaDataType extends Default {
	private String fromSender;
	private String fromAlais;
	private String replyTo;
	@JsonInclude(JsonInclude.Include.ALWAYS)
	private ToRecipientType[] toRecipient;
	private RecipientType[] ccRecipient;
	private RecipientType[] bccRecipient;
	private String subject;
	private String priority;
	private Integer timeToLive;
	private Boolean acceptReply;
	private BlackoutPeriodType blackoutPeriod;
	private Boolean trackingOn;
	private Boolean monitoringOn;
	private TagType[] tag;
	private Boolean shorteningOn;
	
	public String getFromSender() {
		return fromSender;
	}
	public void setFromSender(String fromSender) {
		this.fromSender = fromSender;
	}
	public String getFromAlais() {
		return fromAlais;
	}
	public void setFromAlais(String fromAlais) {
		this.fromAlais = fromAlais;
	}
	public String getReplyTo() {
		return replyTo;
	}
	public void setReplyTo(String replyTo) {
		this.replyTo = replyTo;
	}
	public ToRecipientType[] getToRecipient() {
		return toRecipient;
	}
	public void setToRecipient(ToRecipientType[] toRecipient) {
		this.toRecipient = toRecipient;
	}
	public RecipientType[] getCcRecipient() {
		return ccRecipient;
	}
	public void setCcRecipient(RecipientType[] ccRecipient) {
		this.ccRecipient = ccRecipient;
	}
	public RecipientType[] getBccRecipient() {
		return bccRecipient;
	}
	public void setBccRecipient(RecipientType[] bccRecipient) {
		this.bccRecipient = bccRecipient;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getPriority() {
		return priority;
	}
	public void setPriority(String priority) {
		this.priority = priority;
	}
	@JsonIgnore
	public int getTimeToLive() {
		return (timeToLive!=null?timeToLive:0);
	}
	@JsonProperty("timeToLive")
	public Integer getTimeToLiveInteger() {
		return timeToLive;
	}
	@JsonProperty("timeToLive")
	public void setTimeToLive(int timeToLive) {
		this.timeToLive = Integer.valueOf(timeToLive);
	}
	@JsonIgnore
	public boolean isAcceptReply() {
		return (acceptReply!=null?acceptReply:false);
	}
	@JsonProperty("acceptReply")
	public Boolean getAcceptReply() {
		return acceptReply;
	}
	@JsonProperty("acceptReply")
	public void setAcceptReply(boolean acceptReply) {
		this.acceptReply = Boolean.valueOf(acceptReply);
	}
	@JsonProperty("blackoutPeriod")
	public BlackoutPeriodType getBlackoutPeriod() {
		return blackoutPeriod;
	}
	@JsonProperty("blackoutPeriod")
	public void setBlackoutPeriod(BlackoutPeriodType blackoutPeriod) {
		this.blackoutPeriod = blackoutPeriod;
	}
	@JsonIgnore
	public boolean isTrackingOn() {
		return (trackingOn!=null?trackingOn:false);
	}
	@JsonProperty("trackingOn")
	public Boolean getTrackingOn() {
		return trackingOn;
	}
	@JsonProperty("trackingOn")
	public void setTrackingOn(boolean trackingOn) {
		this.trackingOn = Boolean.valueOf(trackingOn);
	}
	@JsonIgnore
	public boolean isMonitoringOn() {
		return (monitoringOn!=null?monitoringOn:false);
	}
	@JsonProperty("monitoringOn")
	public Boolean getMonitoringOn() {
		return monitoringOn;
	}
	@JsonProperty("monitoringOn")
	public void setMonitoringOn(boolean monitoringOn) {
		this.monitoringOn = Boolean.valueOf(monitoringOn);
	}
	@JsonProperty("tag")
	public TagType[] getTag() {
		return tag;
	}
	@JsonProperty("tag")
	public void setTag(TagType[] tag) {
		this.tag = tag;
	}
	@JsonIgnore
	public boolean isShorteningOn() {
		return (shorteningOn!=null?shorteningOn:false);
	}
	@JsonProperty("shorteningOn")
	public Boolean getShorteningOn() {
		return shorteningOn;
	}
	@JsonProperty("shorteningOn")
	public void setShorteningOn(boolean shorteningOn) {
		this.shorteningOn = Boolean.valueOf(shorteningOn);
	}
}

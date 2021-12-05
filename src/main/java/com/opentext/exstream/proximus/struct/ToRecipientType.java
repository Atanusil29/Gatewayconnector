package com.opentext.exstream.proximus.struct;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
public class ToRecipientType extends RecipientType {
	private boolean isPrimary;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String kind;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String reference;

	public ToRecipientType(){}
	public ToRecipientType(String contact){
		super(contact);
	}
	public ToRecipientType(String contact, boolean isPrimary){
		super(contact);
		setPrimary(isPrimary);
	}

	@JsonProperty("isPrimary")
	public boolean isPrimary() {
		return isPrimary;
	}
	@JsonProperty("isPrimary")
	public void setPrimary(boolean isPrimary) {
		this.isPrimary = isPrimary;
	}

	@JsonProperty("kind")
	public String getKind() {
		return kind;
	}
	@JsonProperty("kind")
	public void setKind(String kind) {
		this.kind = kind;
	}

	@JsonProperty("reference")
	public String getReference() {
		return reference;
	}
	@JsonProperty("reference")
	public void setReference(String reference) {
		this.reference = reference;
	}
}

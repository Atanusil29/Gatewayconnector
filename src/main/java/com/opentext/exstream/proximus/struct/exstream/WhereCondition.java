package com.opentext.exstream.proximus.struct.exstream;

public class WhereCondition {

	private String operator;
	private String name;
	private String value;
	private String[] values;

	public WhereCondition(String operator, String name, String value) {
		this.operator = operator;
		this.name = name;
		this.value = value;
	}

	public WhereCondition(String operator, String name, String[] values) {
		this.operator = operator;
		this.name = name;
		this.values = values;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String[] getValues() {
		return values;
	}

	public void setValues(String[] values) {
		this.values = values;
	}

}

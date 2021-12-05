package com.opentext.exstream.proximus.struct;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class BlackoutPeriodType extends Default {
	private Integer startHour;
	private Integer endHour;
	private String calendarName;
	
	public BlackoutPeriodType(){}
	public BlackoutPeriodType(Integer startHour, Integer endHour, String calendarName){
		setStartHour(startHour);
		setEndHour(endHour);
		setCalendarName(calendarName);
	}
	
	public Integer getStartHour() {
		return startHour;
	}
	public void setStartHour(Integer startHour) {
		this.startHour = startHour;
	}
	public Integer getEndHour() {
		return endHour;
	}
	public void setEndHour(Integer endHour) {
		this.endHour = endHour;
	}
	public String getCalendarName() {
		return calendarName;
	}
	public void setCalendarName(String calendarName) {
		this.calendarName = calendarName;
	}
}

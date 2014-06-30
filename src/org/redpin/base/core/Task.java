package org.redpin.base.core;


/**
 * Task specific information at the time of history recorded
 * 
 * @author Waqas Hussain Siddiqui (waqas.siddiqi@hotmail.com)
 * @version 0.1
 */
public class Task {
	
	/*
	 * Job this task is about
	 */
	protected String jobStatus;
	
	/*
	 * Priority level of this task
	 */
	protected String priority;
	
	/*
	 * Type of transport
	 */
	protected String transportType;
	
	/*
	 * Additional comments related to this task
	 */
	protected String comment;
	
	/* **************** Constructors **************** */
	public Task() {
	}

	/* **************** Getter and Setter Methods **************** */
	public String getJobStatus() {
		return jobStatus;
	}

	public void setJobStatus(String jobStatus) {
		this.jobStatus = jobStatus;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getTransportType() {
		return transportType;
	}

	public void setTransportType(String transportType) {
		this.transportType = transportType;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}

package org.redpin.base.core;

import java.util.Date;

/**
 * Describes a history containing a time and location user was present at
 * 
 * @author Waqas Hussain Siddiqui (waqas.siddiqi@hotmail.com)
 * @version 0.1
 */
public class History {

	
	/*
	 * User to which this history belongs
	 */
	protected User user;
	
	/*
	 * Location when this history entry is recorded
	 */
	protected Location location;
	
	/*
	 * Date and time at which this history entry was recorded
	 */
	protected Date date;

	
	/*
	 * Task being performed when history was recorded
	 */
	protected Task task;
	
	/* **************** Constructors **************** */
	public History() {
		date = new Date();
	}
	
	public History(User user, Location location, Date date) {
		this.user = user;
		this.location = location;
		this.date = date;
	}
	
	/* **************** Getter and Setter Methods **************** */
	public User getUser() {
		return this.user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public Location getLocation() {
		return this.location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
	
	public Date getDate() {
		return this.date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}
}
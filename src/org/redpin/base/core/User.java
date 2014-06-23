package org.redpin.base.core;

/**
 * Describes a user 
 * 
 * @author Waqas Hussain Siddiqui (waqas.siddiqi@hotmail.com)
 * @version 0.1
 */
public class User {
	
	/*
	 * unique identifier, a username e.g. 'trex'
	 */
	protected String userName = "";
	
	/*
	 * Name of user e.g. 'T-Rex'
	 */
	protected String name = "";
	
	/* **************** Constructors **************** */
	
	public User() {
		userName = "";
		name = "";
	}
	
	public User(String userName, String name) {
		this.userName = userName;
		this.name = name;
	}
	
	/* **************** Getter and Setter Methods **************** */
	public String getUserName() {
		return this.userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
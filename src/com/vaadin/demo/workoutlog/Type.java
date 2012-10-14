package com.vaadin.demo.workoutlog;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * An entity type to demonstrate usage relations.
 */
@Entity
public class Type implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2857162632334721554L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	@SuppressWarnings("unused")
	private Date date = new Date();
	private String title = " -- new type -- ";
	@SuppressWarnings("unused")
	private float kilometers;

	public Type()
	{
	}

	public Long getId()
	{
		return id;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	@Override
	public String toString()
	{
		return "(" + id == null ? "-" : id.toString() + ") " + title;
	}

}

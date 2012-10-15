package com.vaadin.data.hbnutil.tests;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Type")
public class Type implements Serializable
{
	private static final long serialVersionUID = -1500836840486290399L;
	private Long id;
	private Date date = new Date();
	private String title = " -- new type -- ";
	private float kilometers;

	public Type()
	{
	}

	@Id
	@Column
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	@Column
	public Date getDate()
	{
		return date;
	}

	public void setDate(Date date)
	{
		this.date = date;
	}

	@Column
	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	@Column
	public float getKilometers()
	{
		return kilometers;
	}

	public void setKilometers(float kilometers)
	{
		this.kilometers = kilometers;
	}

	@Override
	public String toString()
	{
		return "(" + id == null ? "-" : id.toString() + ") " + title;
	}
}

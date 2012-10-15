package com.vaadin.data.hbnutil.tests;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "Workout")
public class Workout implements Serializable
{
	private static final long serialVersionUID = 5259314181953757833L;
	private Long id;
	private Date date = new Date();
	private String title = " -- new workout -- ";
	private float kilometers;
	private Type trainingType;
	private Set<Type> secondaryTypes;

	public Workout()
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

	@ManyToOne
	public Type getTrainingType()
	{
		return trainingType;
	}

	public void setTrainingType(Type trainingType)
	{
		this.trainingType = trainingType;
	}

	@ManyToMany
	public Set<Type> getSecondaryTypes()
	{
		return secondaryTypes;
	}

	public void setSecondaryTypes(Set<Type> secondaryTypes)
	{
		this.secondaryTypes = secondaryTypes;
	}
}

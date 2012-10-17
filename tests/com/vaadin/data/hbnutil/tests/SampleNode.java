package com.vaadin.data.hbnutil.tests;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "SampleNode")
public class SampleNode implements Serializable
{
	private static final long serialVersionUID = 5424273271266344474L;
	private Long id;
	private String title = "Untitled";
	private Date created = new Date();
	private SampleNode parent;

	public SampleNode()
	{
	}

	public SampleNode(String title, SampleNode parent)
	{
		this.title = title;
		this.parent = parent;
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
	public String getTitle()
	{
		return title;
	}
	
	public void setTitle(String title)
	{
		this.title = title;
	}
	
	@Column
	public Date getCreated()
	{
		return created;
	}
	
	public void setCreated(Date created)
	{
		this.created = created;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	public SampleNode getParent()
	{
		return parent;
	}
	
	public void setParent(SampleNode parent)
	{
		this.parent = parent;
	}
}

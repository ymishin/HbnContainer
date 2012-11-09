HbnContainer 1.x
================

[HbnContainer](https://vaadin.com/directory#addon/hbncontainer) is a data connector for [Vaadin](http://vaadin.com) based on the [Hibernate ORM](http://www.hibernate.org/). It is distributed as a Vaadin Add-on and it is used for binding entities with database records to feed components like  tables, forms and trees.

I've decided to continue support for the 1.x branch due to the state of Vaadin 7 at this point. From what I am reading and observing I suspect that it will be some time before it is stable enough for production. I think the best option is to maintain support for 1.x in parallel with 2.x until Vaadin 7 stabilizes.

HbnContainer originated with Matti Tahvonen at Vaadin who created it for an article explaining how to use Hibernate with Vaadin. I took over as maintainer in 2011 when Matti found that he didn't have time to maintain this project along with all the other work he was doing for Vaadin and for the open source community. Matti continues to advise and consult on this project occasionally, as needed.

**Download:**

[HbnContainer Page in the Vaadin Directory](https://vaadin.com/directory#addon/hbncontainer)

**Compatibility:**

* Vaadin 6 + Hibernate 3.x: HbnContainer 1.0
* Vaadin 6 + Hibernate 4.x: HbnContainer 1.x
* Vaadin 7 + Hibernate 4.x: HbnContainer 2.x

**License:**

* HbnContainer is licensed under the [Apache 2.0 License](https://github.com/gpiercey/HbnContainer/wiki/License)
* You are free to use HbnContainer in commercial applications

**Notes:**

* Please use the project page for reporting bugs
* Please use the project page for feature requests
* Please contribute any enhancements back to the community

**References:**

* [Vaadin](http://vaadin.com)
* [HbnContainer Addon](https://vaadin.com/directory#addon/hbncontainer)
* [HbnContainer Project](https://github.com/gpiercey/HbnContainer)

**Ivy Dependency Requirements:**

```
<dependencies>
	<!-- Vaadin -->
	<dependency org="com.vaadin" name="vaadin" rev="&vaadin.version;" />
	
	<!-- Google Guava Jars -->
	<dependency org="com.google.guava" name="guava" rev="&guava.version;"/>
	
	<!-- JBoss Hibernate -->
	<dependency org="org.hibernate" name="hibernate-core" rev="&hibernate.version;"/>
	<dependency org="org.hibernate" name="hibernate-c3p0" rev="&hibernate.version;"/>
	
	<!-- Apache Logging -->
	<dependency org="org.slf4j" name="slf4j-log4j12" rev="&slf4j.version;"/>
	<dependency org="log4j" name="log4j" rev="&log4j.version;"/>
	
	<!-- HyperSQL Database -->
	<dependency org="org.hsqldb" name="hsqldb" rev="&hsqldb.version;"/>
</dependencies>
```

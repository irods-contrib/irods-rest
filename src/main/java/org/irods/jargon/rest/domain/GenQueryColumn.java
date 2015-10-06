package org.irods.jargon.rest.domain;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * <pre>
 * Object to hold information about an individual return value from a GenQuery
 * request. It contains the column name and value.
 * 
 * The following is a sample XML representation:
 * 
 * {@code
 *   <column name="COLL_NAME">/tempZone/home/rods</column>
 * }
 * </pre>
 * 
 * @author jjames
 */
public class GenQueryColumn {

	/** The column name. */
	private String columnName = "";

	/** The column value. */
	private String columnValue = "";

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[column: name=" + columnName + " value=" + columnValue + "]";
	}

	/**
	 * Gets the column name.
	 *
	 * @return the column name
	 */
	@XmlAttribute(name = "name")
	public String getColumnName() {
		return columnName;
	}

	/**
	 * Sets the column name.
	 *
	 * @param columnName
	 *            the new column name
	 */
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	/**
	 * Gets the column value.
	 *
	 * @return the column value
	 */
	@XmlValue
	public String getColumnValue() {
		return columnValue;
	}

	/**
	 * Sets the column value.
	 *
	 * @param columnValue
	 *            the new column value
	 */
	public void setColumnValue(String columnValue) {
		this.columnValue = columnValue;
	}

}

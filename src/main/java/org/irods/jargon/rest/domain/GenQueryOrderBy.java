package org.irods.jargon.rest.domain;

import javax.xml.bind.annotation.XmlElement;

/**
 * <pre>
 * Object to hold GenQuery order by clause which consists of an order by column
 * and order by type (example: MAX).
 * 
 * The following is a sample XML representation of the order by clause:
 * 
 * {@code 
 * <order_by> 
 *   <column>DATA_NAME</column>
 *   <order_condition>ASC</order_condition> 
 * </order_by> 
 * }
 * </pre>
 * 
 * @author jjames
 */

public class GenQueryOrderBy {

	/** The column. */
	private String column = "";

	/** The order by type. */
	private String orderByType = "";

	/**
	 * Instantiates a new gen query order by.
	 */
	public GenQueryOrderBy() {
	}

	/**
	 * Instantiates a new gen query order by.
	 *
	 * @param column
	 *            the column
	 * @param orderByType
	 *            the order by type
	 */
	public GenQueryOrderBy(String column, String orderByType) {
		this.column = column;
		this.orderByType = orderByType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "OrderBy: column=" + column + " type=" + orderByType;
	}

	/**
	 * Gets the column.
	 *
	 * @return the column
	 */
	@XmlElement(name = "column")
	public String getColumn() {
		return column;
	}

	/**
	 * Sets the column.
	 *
	 * @param column
	 *            the new column
	 */
	public void setColumn(String column) {
		this.column = column;
	}

	/**
	 * Gets the order by type.
	 *
	 * @return the order by type
	 */
	@XmlElement(name = "order_condition")
	public String getOrderByType() {
		return orderByType;
	}

	/**
	 * Sets the order by type.
	 *
	 * @param orderByType
	 *            the new order by type
	 */
	public void setOrderByType(String orderByType) {
		this.orderByType = orderByType;
	}

}

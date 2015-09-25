package org.irods.jargon.rest.domain;

import javax.xml.bind.annotation.XmlElement;

/**
 * <pre>
 * Object to hold GenQuery request conditions. A condition contains a column
 * name, the condition's operator, and the value for the test.
 * 
 * The following is a sample XML representation of the condition:
 * 
 * {@code
 * <condition> 
 *   <column>META_DATA_ATTR_NAME</column> 
 *   <operator>LIKE</operator>
 *   <value>ABC</value> 
 * </condition>
 * }
 * </pre>
 * 
 * @author jjames
 */

public class GenQueryCondition {

	/** The column. */
	private String column = "";

	/** The operator. */
	private String operator = "";

	/** The value. */
	private String value = "";

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Condition: column=" + column + " operator=" + operator
				+ " value=" + value;
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
	 * Gets the value.
	 *
	 * @return the value
	 */
	@XmlElement(name = "value")
	public String getValue() {
		return value;
	}

	/**
	 * Sets the value.
	 *
	 * @param value
	 *            the new value
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Gets the operator.
	 *
	 * @return the operator
	 */
	@XmlElement(name = "operator")
	public String getOperator() {
		return operator;
	}

	/**
	 * Sets the operator.
	 *
	 * @param operator
	 *            the new operator
	 */
	public void setOperator(String operator) {
		this.operator = operator;
	}

}

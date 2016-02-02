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
	
	/** Single values */
	private String value = null;

	/** List of values for "IN" clause. */
	private GenQueryConditionValueList valueList = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[Condition: column=" + column + " operator=" + operator
				+ " value=" + valueList + "]";
	}

	/**
	 * Gets the column.
	 *
	 * @return the column
	 */
	@XmlElement(name = "column", required = true)
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
	@XmlElement(name = "value", required = false)
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

	@XmlElement(name = "value_list", required = false)
	public GenQueryConditionValueList getValueList() {
		return valueList;
	}

	public void setValueList(GenQueryConditionValueList v) {
		this.valueList = v;
	}

	/**
	 * Gets the operator.
	 *
	 * @return the operator
	 */
	@XmlElement(name = "operator", required = true)
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

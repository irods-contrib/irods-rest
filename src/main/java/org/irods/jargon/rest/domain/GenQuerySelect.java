package org.irods.jargon.rest.domain;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * <pre>
 * Object to hold GenQuery select field * 
 * The following is a sample XML representation of the order by clause:
 * 
 * {@code 
 * <select>COLL_NAME</select>
 * }
 * </pre>
 * 
 * @author jjames
 */
public class GenQuerySelect {
	
	/** The column. */
	private String column = "";
	
	/** The aggregate type. */
	private String aggregateType = "";
	
	/**
	 * Instantiates a new GenQuerySelect object with default column and aggregateType.
	 */
	public GenQuerySelect() {
		
	}
	
	/**
	 * Instantiates a new GenQuerySelect object with the given column and aggregateType.
	 *
	 * @param column the column
	 * @param aggregateType the aggregate type
	 */
	public GenQuerySelect(String column, String aggregateType) {
		this.column = column;
		this.aggregateType = aggregateType;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		return sb.toString();
	}
	
	/**
	 * Gets the column.
	 *
	 * @return the column
	 */
	@XmlValue 
	public String getColumn() {
		return column;
	}

	/**
	 * Sets the column.
	 *
	 * @param column the new column
	 */
	public void setColumn(String column) {
		this.column = column;
	}

	/**
	 * Gets the aggregate type.
	 *
	 * @return the aggregate type
	 */
	@XmlAttribute(name = "aggregate_type", required=false)
	public String getAggregateType() {
		return aggregateType;
	}

	/**
	 * Sets the aggregate type.
	 *
	 * @param aggregateType the new aggregate type
	 */
	public void setAggregateType(String aggregateType) {
		this.aggregateType = aggregateType;
	}



}

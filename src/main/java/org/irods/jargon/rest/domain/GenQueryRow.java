package org.irods.jargon.rest.domain;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;

/**
 * <pre>
 * Object to hold information about an individual return row from a GenQuery
 * request. It contains a list of GenQueryColumn values.
 * 
 * The following is a sample XML representation:
 * 
 * {@code
 *   <row>
 *     <column name="COLL_NAME">/tempZone/home/rods</column>
 *   </row>
 * }
 * </pre>
 * 
 * @author jjames
 */
public class GenQueryRow {
	
	/** The column list. */
	private ArrayList<GenQueryColumn> columnList = new ArrayList<GenQueryColumn>();
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Row: ");
		for (GenQueryColumn column : columnList) {
			sb.append(column);
			sb.append("\t");
		}
		sb.append(System.lineSeparator());
		return sb.toString();
	}

	/**
	 * Gets the column list.
	 *
	 * @return the column list
	 */
	@XmlElement(name = "column")
	public ArrayList<GenQueryColumn> getColumnList() {
		return columnList;
	}

	/**
	 * Sets the column list.
	 *
	 * @param columnList the new column list
	 */
	public void setColumnList(ArrayList<GenQueryColumn> columnList) {
		this.columnList = columnList;
	}
	


}

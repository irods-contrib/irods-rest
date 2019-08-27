/**
 * 
 */
package org.irods.jargon.rest.domain;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * Value object to hold return value from a GenQuery call.
 * 
 * @author jjames
 * 
 */
@XmlRootElement(name = "results")
public class GenQueryResponseData {

	/** The rows. */
	private ArrayList<GenQueryRow> rows = new ArrayList<GenQueryRow>();

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (GenQueryRow row : rows) {
			sb.append(row);
		}
		return sb.toString();
	}

	/**
	 * Gets the rows.
	 *
	 * @return the rows
	 */
	@XmlElement(name = "row")
	public ArrayList<GenQueryRow> getRows() {
		return rows;
	}
	
	/**
	 * Sets the rows.
	 *
	 * @param rows the new rows
	 */
	public void setRows(ArrayList<GenQueryRow> rows) {
		this.rows = rows;
	}
	
}

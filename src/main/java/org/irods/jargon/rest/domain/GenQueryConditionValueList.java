package org.irods.jargon.rest.domain;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;

/**
 * 
 * Object to hold a list of GenQuery condition values.
 * 
 * @author jjames
 */
public class GenQueryConditionValueList {


	/** The column name. */
	private ArrayList<String> values = new ArrayList<String>();


	/**
	 * Gets the search condition value..
	 *
	 * @return the search condition value 
	 */
	@XmlElement(name = "value", required=true)
	public ArrayList<String> getValues() {
		return values;
	}

	/**
	 * Sets the search condition value 
	 *
	 * @param value
	 *            the search condition value 
	 */
	public void setValues(ArrayList<String> values) {
		this.values = values;
	}

}

package org.irods.jargon.rest.domain;

import javax.xml.bind.annotation.XmlElement;

public class MetadataEntry {

	/**
	 * AVU attribute
	 */
	private String attribute = "";
	/**
	 * AVU value
	 */
	private String value = "";
	/**
	 * AVU unit
	 */
	private String unit = "";

	public MetadataEntry() {
		super();
	}

	/**
	 * @return the attribute
	 */
	@XmlElement
	public String getAttribute() {
		return attribute;
	}

	/**
	 * @return the value
	 */
	@XmlElement
	public String getValue() {
		return value;
	}

	/**
	 * @return the unit
	 */
	@XmlElement
	public String getUnit() {
		return unit;
	}

	/**
	 * @param attribute
	 *            the attribute to set
	 */
	public void setAttribute(final String attribute) {
		this.attribute = attribute;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(final String value) {
		this.value = value;
	}

	/**
	 * @param unit
	 *            the unit to set
	 */
	public void setUnit(final String unit) {
		this.unit = unit;
	}

}
/**
 * 
 */
package org.irods.jargon.rest.domain;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.irods.jargon.core.pub.BulkAVUOperationResponse.ResultStatus;

/**
 * Represents the result of a metadata operation for a particular AVU. This
 * could be an add, delete, or update. The result shows success or failure, and
 * any message.
 * <p/>
 * This is used to allow for bulk operations with AVUs, and reflects individual
 * success or failure, so that the caller may know if the operation is partially
 * successful, and the individual disposition of each AVU element in the
 * request.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@XmlRootElement(name = "metadataOperationResultEntry")
public class MetadataOperationResultEntry {

	private String attributeString = "";
	private String valueString = "";
	private String unit = "";
	private ResultStatus resultStatus;
	private String message;

	/**
	 * 
	 */
	public MetadataOperationResultEntry() {
	}

	/**
	 * @return the {@link ResultStatus} that reflects the success or failure of
	 *         an individual AVU opearation
	 */
	@XmlAttribute
	public ResultStatus getResultStatus() {
		return resultStatus;
	}

	/**
	 * @param resultStatus
	 *            the resultStatus to set
	 */
	public void setResultStatus(ResultStatus resultStatus) {
		this.resultStatus = resultStatus;
	}

	/**
	 * @return the message from the operation if an error occurred
	 */
	@XmlElement
	public String getMessage() {
		return message;
	}

	/**
	 * @param message
	 *            the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the avuAttribute
	 */
	@XmlElement
	public String getAttributeString() {
		return attributeString;
	}

	/**
	 * @param attributeString
	 *            the attributeString to set
	 */
	public void setAttributeString(String attributeString) {
		this.attributeString = attributeString;
	}

	/**
	 * @return the avu value
	 */
	@XmlElement
	public String getValueString() {
		return valueString;
	}

	/**
	 * @param valueString
	 *            the valueString to set
	 */
	public void setValueString(String valueString) {
		this.valueString = valueString;
	}

	/**
	 * @return the avu unit
	 */
	@XmlElement
	public String getUnit() {
		return unit;
	}

	/**
	 * @param unit
	 *            the unit to set
	 */
	public void setUnit(String unit) {
		this.unit = unit;
	}

}

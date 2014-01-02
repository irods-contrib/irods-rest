/**
 * 
 */
package org.irods.jargon.rest.domain;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents an operation on AVU metadata, such as a bulk add of AVU metadata
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@XmlRootElement(name = "metadataOperation")
public class MetadataOperation {

	private List<MetadataEntry> metadataEntries = new ArrayList<MetadataEntry>();

	public MetadataOperation() {
	}

	/**
	 * @return the metadataEntries
	 */
	public List<MetadataEntry> getMetadataEntries() {
		return metadataEntries;
	}

	/**
	 * @param metadataEntries
	 *            the metadataEntries to set
	 */
	public void setMetadataEntries(List<MetadataEntry> metadataEntries) {
		this.metadataEntries = metadataEntries;
	}

}

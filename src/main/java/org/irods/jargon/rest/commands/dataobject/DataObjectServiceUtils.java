/**
 * 
 */
package org.irods.jargon.rest.commands.dataobject;

import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.rest.domain.DataObjectData;

/**
 * Common utilities for data object services
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class DataObjectServiceUtils {

	/**
	 * Given an iRODS <code>DataObject</code> marshal into the equivalent data
	 * transport object to be serialized into JSON or XML
	 * 
	 * @param dataObject
	 * @return {@link DataObjectData}
	 */
	public static DataObjectData buildDataObjectValuesFromIrodsData(
			DataObject dataObject) {
		DataObjectData dataObjectData = new DataObjectData();

		dataObjectData.setChecksum(dataObject.getChecksum());
		dataObjectData.setCollectionId(dataObject.getCollectionId());
		dataObjectData.setCollectionName(dataObject.getCollectionName());
		dataObjectData.setComments(dataObject.getComments());
		dataObjectData.setCreatedAt(dataObject.getCreatedAt());
		dataObjectData.setDataMapId(dataObject.getDataMapId());
		dataObjectData.setDataName(dataObject.getDataName());
		dataObjectData.setDataOwnerName(dataObject.getDataOwnerName());
		dataObjectData.setDataOwnerZone(dataObject.getDataOwnerZone());
		dataObjectData.setDataPath(dataObject.getDataPath());
		dataObjectData.setDataReplicationNumber(dataObject
				.getDataReplicationNumber());
		dataObjectData.setDataSize(dataObject.getDataSize());
		dataObjectData.setDataStatus(dataObject.getDataStatus());
		dataObjectData.setDataTypeName(dataObject.getDataTypeName());
		dataObjectData.setDataVersion(dataObject.getDataVersion());
		dataObjectData.setExpiry(dataObject.getExpiry());
		dataObjectData.setId(dataObject.getId());
		dataObjectData.setObjectPath(dataObject.getObjectPath());
		dataObjectData.setReplicationStatus(dataObject.getReplicationStatus());
		dataObjectData.setResourceGroupName(dataObject.getResourceGroupName());
		dataObjectData.setResourceName(dataObject.getResourceName());
		dataObjectData.setSpecColType(dataObject.getSpecColType());
		dataObjectData.setUpdatedAt(dataObject.getUpdatedAt());
		return dataObjectData;
	}

	/**
	 * 
	 */
	private DataObjectServiceUtils() {
	}

}

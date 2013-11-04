/**
 * 
 */
package org.irods.jargon.rest.domain;

import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.irods.jargon.core.pub.domain.ObjStat.SpecColType;

/**
 * Represents an iRODS collection
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@XmlRootElement(name = "collection")
public class CollectionData {

	private int collectionId = 0;
	/**
	 * This will be the full absolute path of the collection, in the case of a
	 * mounted collection, such as a soft link, this may be the linked name,
	 * where the objectPath will contain the canonical path or actual physial
	 * location
	 */
	private String collectionName = "";
	/**
	 * The canonical absolute path for the object if this is a soft-linked
	 * collection. If this object is retrieved by the canonical path, or it is
	 * not a special collection, this will be blank
	 */
	private String objectPath = "";
	/**
	 * This will be the full absolute path of the parent of the given collection
	 */
	private String collectionParentName = "";
	private String collectionOwnerName = "";
	private String collectionOwnerZone = "";
	private String collectionMapId = "";
	private String collectionInheritance = "";
	private String comments = "";
	private String info1 = "";
	private String info2 = "";
	private Date createdAt = new Date();
	private Date modifiedAt = new Date();
	private SpecColType specColType = SpecColType.NORMAL;

	/**
	 * 
	 */
	public CollectionData() {
	}

	/**
	 * @return the collectionId
	 */
	@XmlAttribute
	public int getCollectionId() {
		return collectionId;
	}

	/**
	 * @param collectionId
	 *            the collectionId to set
	 */
	public void setCollectionId(final int collectionId) {
		this.collectionId = collectionId;
	}

	/**
	 * @return the collectionName
	 */
	@XmlAttribute
	public String getCollectionName() {
		return collectionName;
	}

	/**
	 * @param collectionName
	 *            the collectionName to set
	 */
	public void setCollectionName(final String collectionName) {
		this.collectionName = collectionName;
	}

	/**
	 * @return the objectPath
	 */

	public String getObjectPath() {
		return objectPath;
	}

	/**
	 * @param objectPath
	 *            the objectPath to set
	 */
	public void setObjectPath(final String objectPath) {
		this.objectPath = objectPath;
	}

	/**
	 * @return the collectionParentName
	 */
	@XmlAttribute
	public String getCollectionParentName() {
		return collectionParentName;
	}

	/**
	 * @param collectionParentName
	 *            the collectionParentName to set
	 */
	public void setCollectionParentName(final String collectionParentName) {
		this.collectionParentName = collectionParentName;
	}

	/**
	 * @return the collectionOwnerName
	 */
	@XmlAttribute
	public String getCollectionOwnerName() {
		return collectionOwnerName;
	}

	/**
	 * @param collectionOwnerName
	 *            the collectionOwnerName to set
	 */
	public void setCollectionOwnerName(final String collectionOwnerName) {
		this.collectionOwnerName = collectionOwnerName;
	}

	/**
	 * @return the collectionOwnerZone
	 */
	@XmlAttribute
	public String getCollectionOwnerZone() {
		return collectionOwnerZone;
	}

	/**
	 * @param collectionOwnerZone
	 *            the collectionOwnerZone to set
	 */
	public void setCollectionOwnerZone(final String collectionOwnerZone) {
		this.collectionOwnerZone = collectionOwnerZone;
	}

	/**
	 * @return the collectionMapId
	 */
	@XmlAttribute
	public String getCollectionMapId() {
		return collectionMapId;
	}

	/**
	 * @param collectionMapId
	 *            the collectionMapId to set
	 */
	public void setCollectionMapId(final String collectionMapId) {
		this.collectionMapId = collectionMapId;
	}

	/**
	 * @return the collectionInheritance
	 */
	@XmlAttribute
	public String getCollectionInheritance() {
		return collectionInheritance;
	}

	/**
	 * @param collectionInheritance
	 *            the collectionInheritance to set
	 */
	public void setCollectionInheritance(final String collectionInheritance) {
		this.collectionInheritance = collectionInheritance;
	}

	/**
	 * @return the comments
	 */
	@XmlAttribute
	public String getComments() {
		return comments;
	}

	/**
	 * @param comments
	 *            the comments to set
	 */
	public void setComments(final String comments) {
		this.comments = comments;
	}

	/**
	 * @return the info1
	 */
	@XmlAttribute
	public String getInfo1() {
		return info1;
	}

	/**
	 * @param info1
	 *            the info1 to set
	 */
	public void setInfo1(final String info1) {
		this.info1 = info1;
	}

	/**
	 * @return the info2
	 */
	@XmlAttribute
	public String getInfo2() {
		return info2;
	}

	/**
	 * @param info2
	 *            the info2 to set
	 */
	public void setInfo2(final String info2) {
		this.info2 = info2;
	}

	/**
	 * @return the createdAt
	 */
	@XmlAttribute
	public Date getCreatedAt() {
		return createdAt;
	}

	/**
	 * @param createdAt
	 *            the createdAt to set
	 */
	public void setCreatedAt(final Date createdAt) {
		this.createdAt = createdAt;
	}

	/**
	 * @return the modifiedAt
	 */
	@XmlAttribute
	public Date getModifiedAt() {
		return modifiedAt;
	}

	/**
	 * @param modifiedAt
	 *            the modifiedAt to set
	 */
	public void setModifiedAt(final Date modifiedAt) {
		this.modifiedAt = modifiedAt;
	}

	/**
	 * @return the specColType
	 */
	@XmlAttribute
	public SpecColType getSpecColType() {
		return specColType;
	}

	/**
	 * @param specColType
	 *            the specColType to set
	 */
	public void setSpecColType(final SpecColType specColType) {
		this.specColType = specColType;
	}

}

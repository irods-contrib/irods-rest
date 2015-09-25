/**
 * 
 */
package org.irods.jargon.rest.domain;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;



/**
 * Value object to hold return value from a GenQuery call.  The request
 * includes an optional count parameter, and optional zone hint, a list 
 * of select fields, a list of conditionals, and a list of order by clauses.
 * 
 * @author jjames
 * 
 */
@XmlRootElement(name = "query")
public class GenQueryRequestData {

	/** The count. */
	private int count = 100;
	
	/** The zone. */
	private String zone = "";
	
	/** The select field list. */
	private ArrayList<GenQuerySelect> selectFieldList = new ArrayList<GenQuerySelect>();
	
	/** The query condition list. */
	private ArrayList<GenQueryCondition> queryConditionList = new ArrayList<GenQueryCondition>();
	
	/** The order by list. */
	private ArrayList<GenQueryOrderBy> orderByList = new ArrayList<GenQueryOrderBy>();
	
	/**
	 * Gets the count.
	 *
	 * @return the count
	 */
	@XmlElement(required = false)
	public int getCount() {
		return count;
	}

	/**
	 * Sets the count.
	 *
	 * @param count the new count
	 */
	public void setCount(int count) {
		this.count = count;
	}

	/**
	 * Gets the zone.
	 *
	 * @return the zone
	 */
	@XmlElement(required = false)
	public String getZone() {
		return zone;
	}

	/**
	 * Sets the zone.
	 *
	 * @param zone the new zone
	 */
	public void setZone(String zone) {
		this.zone = zone;
	}

	/**
	 * Gets the select field list.
	 *
	 * @return the select field list
	 */
	@XmlElement(name = "select", required = true)
	public ArrayList<GenQuerySelect> getSelectFieldList() {
		return selectFieldList;
	}

	/**
	 * Sets the select field list.
	 *
	 * @param selectFieldList the new select field list
	 */
	public void setSelectFieldList(ArrayList<GenQuerySelect> selectFieldList) {
		this.selectFieldList = selectFieldList;
	}

	/**
	 * Gets the query condition list.
	 *
	 * @return the query condition list
	 */
	@XmlElement(name = "condition", required = false)
	public ArrayList<GenQueryCondition> getQueryConditionList() {
		return queryConditionList;
	}

	/**
	 * Sets the query condition list.
	 *
	 * @param queryConditionList the new query condition list
	 */
	public void setQueryConditionList(ArrayList<GenQueryCondition> queryConditionList) {
		this.queryConditionList = queryConditionList;
	}

	/**
	 * Gets the order by list.
	 *
	 * @return the order by list
	 */
	@XmlElement(name = "order_by", required = false)
	public ArrayList<GenQueryOrderBy> getOrderByList() {
		return orderByList;
	}

	/**
	 * Sets the order by list.
	 *
	 * @param orderByList the new order by list
	 */
	public void setOrderByList(ArrayList<GenQueryOrderBy> orderByList) {
		this.orderByList = orderByList;
	}

	
}

/**
 * 
 */
package org.irods.jargon.rest.commands.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.GenQueryField.SelectFieldTypes;
import org.irods.jargon.core.query.GenQueryOrderByField.OrderByType;
import org.irods.jargon.core.query.IRODSGenQueryBuilder;
import org.irods.jargon.core.query.IRODSGenQueryFromBuilder;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.QueryConditionOperators;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.rest.commands.AbstractIrodsService;
import org.irods.jargon.rest.domain.GenQueryColumn;
import org.irods.jargon.rest.domain.GenQueryCondition;
import org.irods.jargon.rest.domain.GenQueryOrderBy;
import org.irods.jargon.rest.domain.GenQueryRequestData;
import org.irods.jargon.rest.domain.GenQueryResponseData;
import org.irods.jargon.rest.domain.GenQueryRow;
import org.irods.jargon.rest.domain.GenQuerySelect;
import org.jboss.resteasy.annotations.providers.jaxb.json.Mapped;
import org.jboss.resteasy.annotations.providers.jaxb.json.XmlNsMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class GenQueryService.
 * 
 * @author jjames
 */
@Named
@Path("/genQuery")
public class GenQueryService extends AbstractIrodsService {

	/**
	 * A <code>HashMap</code> used to look up RodGenQueryEnum values from the string
	 * representation.
	 */
	// Reverse-lookup map for a RodsGenQueryEnum from a value
	private static final Map<String, RodsGenQueryEnum> genQueryFieldsLookup = new HashMap<String, RodsGenQueryEnum>();

	static {
		for (RodsGenQueryEnum e : RodsGenQueryEnum.values()) {
			genQueryFieldsLookup.put(e.getName(), e);
		}
	}

	/** The log. */
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * 
	 * Returns the GenQueryResponseData for the request provided in requestData. The
	 * request and response can be presented in either XML format (application/xml)
	 * or JSON format (application/json) depending on the Accept and Content-Type
	 * headers sent in the request.
	 * 
	 * The following is a sample request and response in XML format.
	 * 
	 * 
	 * Request:
	 * 
	 * <pre>
	 * {@code 
	 * <ns2:query xmlns:ns2="http://irods.org/irods-rest">
	 *   <select>RESC_NAME</select> 
	 *   <select>COLL_NAME</select>
	 *   <select>DATA_NAME</select> 
	 *   <condition> 
	 *     <column>COLL_NAME</column>
	 *     <operator>LIKE</operator> 
	 *     <value>/tempZone/home/%</value> 
	 *   </condition>
	 *   <condition> 
	 *     <column>META_DATA_ATTR_NAME</column>
	 *     <operator>LIKE</operator> 
	 *     <value>GUPI</value> 
	 *   </condition> <condition>
	 *   <order_by> 
	 *     <column>DATA_NAME</column>
	 *     <order_condition>ASC</order_condition> 
	 *   </order_by> 
	 * </ns2:query>
	 * }
	 * </pre>
	 * 
	 * Response:
	 * 
	 * <pre>
	 * {@code 
	 * <?xml version="1.0" encoding="UTF-8" standalone="yes"?> 
	 * <ns2:resultsxmlns:ns2="http://irods.org/irods-rest"> 
	 *   <row> 
	 *     <column name="RESC_NAME">demoResc</column> 
	 *     <column name="COLL_NAME">/tempZone/home/rods</column> 
	 *     <column name="DATA_NAME">CAMB001.hdf5</column> 
	 *   </row> 
	 *   <row> 
	 *     <column name="RESC_NAME">demoResc</column> 
	 *     <column name="COLL_NAME">/tempZone/home/rods</column> 
	 *     <column name="DATA_NAME">CAMB001.snapshot.hdf5</column> 
	 *   </row> 
	 * </ns2:results>
	 *}
	 * </pre>
	 *
	 * @param authorization
	 * @param requestData
	 * @return The response <code>GenQueryResponseData</code>
	 * @throws JargonException
	 * @throws GenQueryBuilderException
	 * @throws JargonQueryException
	 */
	@POST
	@Consumes({ "application/json" })
	@Produces({ "application/json" })
	@Mapped(namespaceMap = { @XmlNsMap(namespace = "http://irods.org/irods-rest", jsonName = "irods-rest") })
	public GenQueryResponseData getGenQueryData(@HeaderParam("Authorization") final String authorization,
			final GenQueryRequestData requestData)
			throws JargonException, GenQueryBuilderException, JargonQueryException {

		log.info("getGenQueryData()");
		if (authorization == null || authorization.isEmpty()) {
			throw new IllegalArgumentException("null or empty authorization");
		}

		try {
			IRODSAccount irodsAccount = retrieveIrodsAccountFromAuthentication(authorization);

			IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
			IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

			IRODSGenQueryExecutor irodsGenQueryExecutor = accessObjectFactory.getIRODSGenQueryExecutor(irodsAccount);

			IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);

			// Update the query builder for the select fields
			for (GenQuerySelect selectField : requestData.getSelectFieldList()) {

				// look up the Jargon enum for the select field
				RodsGenQueryEnum rodsGenQueryEnum = translateQueryColumnToEnum(selectField.getColumn());

				// if aggregateType attribute is missing or the empty string,
				// consider this as a non-aggregate field
				if (selectField.getAggregateType() == null || selectField.getAggregateType().equals("")) {
					builder.addSelectAsGenQueryValue(rodsGenQueryEnum);
				} else {
					builder.addSelectAsAgregateGenQueryValue(rodsGenQueryEnum,
							SelectFieldTypes.valueOf(selectField.getAggregateType().toUpperCase()));
				}
			}

			// Update the query builder for the condition fields.
			for (GenQueryCondition condition : requestData.getQueryConditionList()) {

				// look up the Jargon enum for the condition field
				RodsGenQueryEnum rodsGenQueryEnum = translateQueryColumnToEnum(condition.getColumn());

				// Must treat "in" clause Differently from others.
				if (condition.getOperator().equalsIgnoreCase("IN")) {

					if (condition.getValueList() == null || condition.getValueList().getValues() == null
							|| condition.getValueList().getValues().size() == 0) {
						throw new IllegalArgumentException("Condition (IN) must have a value_list.");
					} else if (condition.getValue() != null) {
						throw new IllegalArgumentException(
								"Condition (IN) should have a value_list and not a simple value.");
					}

					ArrayList<String> valueList = condition.getValueList().getValues();

					builder.addConditionAsMultiValueCondition(rodsGenQueryEnum,
							QueryConditionOperators.valueOf(condition.getOperator().toUpperCase()), valueList);
				} else {
					if (condition.getValue() == null) {
						throw new IllegalArgumentException(
								"Condition " + condition.getOperator().toUpperCase() + " must have a value");
					} else if (condition.getValueList() != null) {
						throw new IllegalArgumentException("Condition " + condition.getOperator().toUpperCase()
								+ " should not have a value_list.");
					}

					builder.addConditionAsGenQueryField(rodsGenQueryEnum,
							QueryConditionOperators.valueOf(condition.getOperator().toUpperCase()),
							condition.getValue());
				}
			}

			// Update the query builder for the order by fields.
			for (GenQueryOrderBy orderBy : requestData.getOrderByList()) {

				// look up the Jargon enum for the condition field
				RodsGenQueryEnum rodsGenQueryEnum = translateQueryColumnToEnum(orderBy.getColumn());

				builder.addOrderByGenQueryField(rodsGenQueryEnum, OrderByType.valueOf(orderBy.getOrderByType()));

			}

			IRODSGenQueryFromBuilder query = builder.exportIRODSQueryFromBuilder(requestData.getCount());

			// Execute the query. If the zone is not provided use the default.
			IRODSQueryResultSetInterface resultSet;
			if (requestData.getZone() == null || requestData.getZone() == "") {
				resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(query, 0);
			} else {
				resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResultInZone(query, 0,
						requestData.getZone());
			}

			// Build up the response
			GenQueryResponseData responseData = new GenQueryResponseData();

			for (IRODSQueryResultRow resultSetRow : resultSet.getResults()) {
				GenQueryRow row = new GenQueryRow();

				int i = 0;
				for (GenQuerySelect selectField : requestData.getSelectFieldList()) {
					String selectColumn = selectField.getColumn();
					if (selectField.getAggregateType() != null && selectField.getAggregateType() != "") {
						selectColumn = selectField.getAggregateType() + "(" + selectColumn + ")";
					}
					selectColumn = selectColumn.toUpperCase();

					String value = resultSetRow.getColumn(i);

					GenQueryColumn col = new GenQueryColumn();
					col.setColumnValue(value);
					col.setColumnName(selectColumn);
					row.getColumnList().add(col);
					++i;
				}
				responseData.getRows().add(row);

			}

			return responseData;
		} finally {
			// getIrodsAccessObjectFactory().closeSessionAndEatExceptions();
		}
	}

	/**
	 * Translates query column to the appropriate RodsGenQueryEnum enumeration.
	 *
	 * @param queryColumn
	 * @return The RodsGenQueryEnum enumeration
	 * @throws GenQueryBuilderException
	 *             if an enumeration does not exist for the query column.
	 */
	private static RodsGenQueryEnum translateQueryColumnToEnum(String queryColumn) throws GenQueryBuilderException {

		// look up the Jargon enum for the select field
		RodsGenQueryEnum rodsGenQueryEnum = genQueryFieldsLookup.get(queryColumn.toUpperCase());
		if (rodsGenQueryEnum == null) {
			throw new GenQueryBuilderException("Invalid query column " + queryColumn);
		}
		return rodsGenQueryEnum;
	}

}

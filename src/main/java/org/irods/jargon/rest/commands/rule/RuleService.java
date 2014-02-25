/**
 * 
 */
package org.irods.jargon.rest.commands.rule;

import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.RuleProcessingAO.RuleProcessingType;
import org.irods.jargon.rest.commands.AbstractIrodsService;
import org.irods.jargon.rest.domain.RuleExecResultWrapper;
import org.irods.jargon.rest.domain.RuleWrapper;
import org.jboss.resteasy.annotations.providers.jaxb.json.Mapped;
import org.jboss.resteasy.annotations.providers.jaxb.json.XmlNsMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST service for iRODS Rules.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@Named
@Path("/rule")
public class RuleService extends AbstractIrodsService {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * Do a bulk metadata add operation for the given data object. This takes a
	 * list of AVU entries in the PUT request body, and will attempt to add each
	 * AVU.
	 * <p/>
	 * A response body will log the disposition of each AVU add attempt, and any
	 * errors for an individual attempt are noted by the returned status and
	 * message for each entry. This allows partial success.
	 * 
	 * @param authorization
	 *            <code>String</code> with the basic auth header
	 * 
	 * @return response body derived from {@link RuleExecResultWrapper}
	 * @throws JargonException
	 */
	@POST
	@Consumes({ "application/xml", "application/json" })
	@Produces({ "application/xml", "application/json" })
	@Mapped(namespaceMap = { @XmlNsMap(namespace = "http://irods.org/irods-rest", jsonName = "irods-rest") })
	public RuleExecResultWrapper executeRule(
			@HeaderParam("Authorization") final String authorization,
			final RuleWrapper ruleWrapper) throws JargonException {

		log.info("addCollectionMetadata()");

		if (authorization == null || authorization.isEmpty()) {
			throw new IllegalArgumentException("null or empty authorization");
		}

		if (ruleWrapper == null) {
			throw new IllegalArgumentException("null ruleWrapper");
		}

		try {
			IRODSAccount irodsAccount = retrieveIrodsAccountFromAuthentication(authorization);
			RuleFunctions ruleFunctions = this.getServiceFunctionFactory()
					.instanceRuleFunctions(irodsAccount);
			log.info("executing...");
			return ruleFunctions.executeRule(
					ruleWrapper.getRuleAsOriginalText(),
					ruleWrapper.getIrodsRuleInputParameters(),
					RuleProcessingType.CLASSIC);

		} finally {
			getIrodsAccessObjectFactory().closeSessionAndEatExceptions();
		}
	}

}

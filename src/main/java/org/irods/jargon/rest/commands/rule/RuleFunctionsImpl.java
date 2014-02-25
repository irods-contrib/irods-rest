package org.irods.jargon.rest.commands.rule;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.RuleProcessingAO;
import org.irods.jargon.core.pub.RuleProcessingAO.RuleProcessingType;
import org.irods.jargon.core.rule.IRODSRuleExecResult;
import org.irods.jargon.core.rule.IRODSRuleExecResultOutputParameter;
import org.irods.jargon.core.rule.IRODSRuleParameter;
import org.irods.jargon.core.rule.JargonRuleException;
import org.irods.jargon.rest.commands.AbstractServiceFunction;
import org.irods.jargon.rest.configuration.RestConfiguration;
import org.irods.jargon.rest.domain.RuleExecResultOutputParameterWrapper;
import org.irods.jargon.rest.domain.RuleExecResultWrapper;
import org.irods.jargon.rest.domain.RuleParameterWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Business logic for interacting with iRODS rule engine, backing the REST
 * wrapper service
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class RuleFunctionsImpl extends AbstractServiceFunction implements
		RuleFunctions {

	private static final Logger log = LoggerFactory
			.getLogger(RuleFunctionsImpl.class);

	public RuleFunctionsImpl(RestConfiguration restConfiguration,
			IRODSAccount irodsAccount,
			IRODSAccessObjectFactory irodsAccessObjectFactory) {
		super(restConfiguration, irodsAccount, irodsAccessObjectFactory);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.rest.commands.rule.RuleFunctions#executeRule(java.lang
	 * .String, java.util.List,
	 * org.irods.jargon.core.pub.RuleProcessingAO.RuleProcessingType)
	 */
	@Override
	public RuleExecResultWrapper executeRule(final String ruleToExecute,
			final List<RuleParameterWrapper> inputParameterOverrides,
			final RuleProcessingType ruleProcessingType)
			throws JargonRuleException, JargonException {

		log.info("executeRule()");

		if (ruleToExecute == null || ruleToExecute.isEmpty()) {
			throw new IllegalArgumentException("null or empty ruleToExecute");
		}

		if (inputParameterOverrides == null) {
			throw new IllegalArgumentException("null inputParameterOverrides");
		}

		if (ruleProcessingType == null) {
			throw new IllegalArgumentException("null ruleProcessingType");
		}

		log.info("ruleToExecute:{}", ruleToExecute);
		log.info("inputParameterOverrides:{}", inputParameterOverrides);
		log.info("ruleProcessingType:{}", ruleProcessingType);

		RuleProcessingAO ruleProcessingAO = this.getIrodsAccessObjectFactory()
				.getRuleProcessingAO(getIrodsAccount());

		List<IRODSRuleParameter> parmsIrodsRuleParameters = new ArrayList<IRODSRuleParameter>();
		for (RuleParameterWrapper wrapper : inputParameterOverrides) {
			parmsIrodsRuleParameters.add(wrapper);
		}

		IRODSRuleExecResult execResult = ruleProcessingAO.executeRule(
				ruleToExecute, parmsIrodsRuleParameters, ruleProcessingType);

		log.info("got exec result:{}", execResult);
		RuleExecResultWrapper resultWrapper = new RuleExecResultWrapper();
		List<RuleExecResultOutputParameterWrapper> outputParameterWrappers = new ArrayList<RuleExecResultOutputParameterWrapper>();
		RuleExecResultOutputParameterWrapper wrappedParameter;

		for (IRODSRuleExecResultOutputParameter outParameter : execResult
				.getOutputParameterResults().values()) {

			wrappedParameter = new RuleExecResultOutputParameterWrapper(

			outParameter.getParameterName(), outParameter.getOutputParamType(),
					outParameter.getResultObject());

			outputParameterWrappers.add(wrappedParameter);
		}

		resultWrapper.setOutputParameterResults(outputParameterWrappers);

		return resultWrapper;

	}
}

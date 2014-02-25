package org.irods.jargon.rest.commands.rule;

import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.RuleProcessingAO.RuleProcessingType;
import org.irods.jargon.core.rule.JargonRuleException;
import org.irods.jargon.rest.domain.RuleExecResultWrapper;
import org.irods.jargon.rest.domain.RuleParameterWrapper;

public interface RuleFunctions {

	public abstract RuleExecResultWrapper executeRule(String ruleToExecute,
			List<RuleParameterWrapper> inputParameterOverrides,
			RuleProcessingType ruleProcessingType) throws JargonRuleException,
			JargonException;

}
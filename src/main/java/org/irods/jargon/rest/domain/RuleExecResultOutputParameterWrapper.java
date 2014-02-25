/**
 * 
 */
package org.irods.jargon.rest.domain;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.rule.IRODSRuleExecResultOutputParameter;

/**
 * Wrapper (with JAXB annotations) for the output parameter of a rule invocation
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@XmlRootElement(name = "ruleExecOutputParameter")
public class RuleExecResultOutputParameterWrapper extends
		IRODSRuleExecResultOutputParameter {

	public RuleExecResultOutputParameterWrapper() {
		super();
	}

	public RuleExecResultOutputParameterWrapper(String parameterName,
			OutputParamType outputParamType, Object resultObject)
			throws JargonException {
		super(parameterName, outputParamType, resultObject);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.rule.IRODSRuleExecResultOutputParameter#
	 * getParameterName()
	 */
	@Override
	@XmlElement
	public String getParameterName() {
		return super.getParameterName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.rule.IRODSRuleExecResultOutputParameter#
	 * getOutputParamType()
	 */
	@Override
	@XmlElement
	public OutputParamType getOutputParamType() {
		return super.getOutputParamType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.rule.IRODSRuleExecResultOutputParameter#getResultObject
	 * ()
	 */
	@Override
	@XmlElement
	public Object getResultObject() {
		return super.getResultObject();
	}

}

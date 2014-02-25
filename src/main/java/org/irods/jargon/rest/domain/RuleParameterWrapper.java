/**
 * 
 */
package org.irods.jargon.rest.domain;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.irods.jargon.core.rule.IRODSRuleParameter;

/**
 * Wrapper for <code>IRODSRuleParameter</code> with JAXB annotations
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@XmlRootElement(name = "ruleParameter")
public class RuleParameterWrapper extends IRODSRuleParameter {

	/**
	 * 
	 */
	public RuleParameterWrapper() {
	}

	/**
	 * @param name
	 * @param value
	 */
	public RuleParameterWrapper(String name, int value) {
		super(name, value);
	}

	/**
	 * @param value
	 */
	public RuleParameterWrapper(String value) {
		super(value);
	}

	/**
	 * @param value
	 */
	public RuleParameterWrapper(byte[] value) {
		super(value);
	}

	/**
	 * @param name
	 * @param value
	 */
	public RuleParameterWrapper(String name, String value) {
		super(name, value);
	}

	/**
	 * @param name
	 * @param value
	 * @param type
	 */
	public RuleParameterWrapper(String name, Object value, String type) {
		super(name, value, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.rule.IRODSRuleParameter#getType()
	 */
	@Override
	@XmlElement
	public String getType() {
		return super.getType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.rule.IRODSRuleParameter#getStringValue()
	 */
	@Override
	@XmlElement
	public String getStringValue() {
		return super.getStringValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.rule.IRODSRuleParameter#getUniqueName()
	 */
	@Override
	@XmlElement
	public String getUniqueName() {
		return super.getUniqueName();
	}

}

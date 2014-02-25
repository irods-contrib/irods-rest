/**
 * 
 */
package org.irods.jargon.rest.domain;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * JAXB annotated wrapper that reflects the result of executing an iRODS rule
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@XmlRootElement(name = "ruleExecResult")
public class RuleExecResultWrapper {

	private List<RuleExecResultOutputParameterWrapper> outputParameterResults = new ArrayList<RuleExecResultOutputParameterWrapper>();

	public RuleExecResultWrapper() {
	}

	/**
	 * @return the outputParameterResults
	 */
	public List<RuleExecResultOutputParameterWrapper> getOutputParameterResults() {
		return outputParameterResults;
	}

	/**
	 * @param outputParameterResults
	 *            the outputParameterResults to set
	 */
	public void setOutputParameterResults(
			List<RuleExecResultOutputParameterWrapper> outputParameterResults) {
		this.outputParameterResults = outputParameterResults;
	}

}

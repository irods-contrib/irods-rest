/**
 * 
 */
package org.irods.jargon.rest.domain;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.sun.xml.txw2.annotation.XmlElement;

/**
 * JAXB annotated wrapper for an iRODS rule
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@XmlRootElement(name = "rule")
public class RuleWrapper {

	private String ruleAsOriginalText;
	private List<RuleParameterWrapper> irodsRuleInputParameters = new ArrayList<RuleParameterWrapper>();

	/**
	 * 
	 */
	public RuleWrapper() {
	}

	/**
	 * @return the ruleAsOriginalText
	 */
	@XmlElement
	public String getRuleAsOriginalText() {
		return ruleAsOriginalText;
	}

	/**
	 * @param ruleAsOriginalText
	 *            the ruleAsOriginalText to set
	 */
	public void setRuleAsOriginalText(String ruleAsOriginalText) {
		this.ruleAsOriginalText = ruleAsOriginalText;
	}

	/**
	 * @return the irodsRuleInputParameters
	 */
	public List<RuleParameterWrapper> getIrodsRuleInputParameters() {
		return irodsRuleInputParameters;
	}

	/**
	 * @param irodsRuleInputParameters
	 *            the irodsRuleInputParameters to set
	 */
	public void setIrodsRuleInputParameters(
			List<RuleParameterWrapper> irodsRuleInputParameters) {
		this.irodsRuleInputParameters = irodsRuleInputParameters;
	}
}

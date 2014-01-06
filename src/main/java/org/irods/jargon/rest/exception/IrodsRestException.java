package org.irods.jargon.rest.exception;

import org.irods.jargon.core.exception.JargonException;

/**
 * General exception in rest processing
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IrodsRestException extends JargonException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9017853086935903803L;

	/**
	 * @param message
	 */
	public IrodsRestException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public IrodsRestException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public IrodsRestException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public IrodsRestException(final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public IrodsRestException(final String message,
			final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public IrodsRestException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

}

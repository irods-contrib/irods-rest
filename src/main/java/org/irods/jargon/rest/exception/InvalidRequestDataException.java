package org.irods.jargon.rest.exception;

/**
 * Data is missing from a request, or is invalid.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class InvalidRequestDataException extends IrodsRestException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1740038553681572966L;

	/**
	 * @param message
	 */
	public InvalidRequestDataException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public InvalidRequestDataException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public InvalidRequestDataException(final String message,
			final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public InvalidRequestDataException(final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public InvalidRequestDataException(final String message,
			final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public InvalidRequestDataException(final String message,
			final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

}

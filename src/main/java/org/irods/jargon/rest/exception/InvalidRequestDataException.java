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
	public InvalidRequestDataException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public InvalidRequestDataException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public InvalidRequestDataException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public InvalidRequestDataException(Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public InvalidRequestDataException(String message,
			int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public InvalidRequestDataException(String message, Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

}

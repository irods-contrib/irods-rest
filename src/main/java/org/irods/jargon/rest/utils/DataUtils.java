/**
 * 
 */
package org.irods.jargon.rest.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.irods.jargon.core.exception.JargonException;

/**
 * General utilities for manipulating parameters and paths coming in from
 * requests
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class DataUtils {

	/**
	 * 
	 */
	private DataUtils() {
	}

	/**
	 * Given path info from a URL, and a character encoding, return a decoded
	 * path.
	 * <p/>
	 * Paths in URLs must be encoded because of the wide range of characters
	 * that are used in iRODS absolute paths.
	 * 
	 * @param pathInfo
	 * @param encoding
	 * @return
	 * @throws JargonException
	 */
	public static String buildDecodedPathFromURLPathInfo(final String pathInfo,
			final String encoding) throws JargonException {

		if (pathInfo == null) {
			throw new IllegalArgumentException("null pathInfo");
		}

		if (encoding == null || encoding.isEmpty()) {
			throw new IllegalArgumentException("null or empty encoding");
		}

		/*
		 * If the url extra path info for the collection has been encoded
		 * (necessary if it contains any special character info), then it should
		 * decode with a leading / char.
		 * 
		 * If it's not been encoded, then it will lack a leading slash, as this
		 * is truncated during the URL mapping process in the framework.
		 */

		String decodedString;

		try {
			decodedString = URLDecoder.decode(pathInfo, encoding);
		} catch (UnsupportedEncodingException e1) {
			throw new JargonException("unsupported encoding", e1);
		}

		StringBuilder sb = new StringBuilder();

		if (decodedString.isEmpty()) {
			sb.append('/');

		} else if (decodedString.charAt(0) != '/') {

			sb.append('/');
		}
		sb.append(decodedString);
		return sb.toString();

	}

	/**
	 * Given an iRODS absolute path, and an encoding scheme, encode for use in a
	 * URL
	 * 
	 * @param path
	 * @param encoding
	 * @return
	 * @throws JargonException
	 */
	public static String encodeIrodsAbsolutePath(final String path,
			final String encoding) throws JargonException {

		if (path == null) {
			throw new IllegalArgumentException("null path");
		}

		if (encoding == null || encoding.isEmpty()) {
			throw new IllegalArgumentException("null or empty encoding");
		}

		try {
			return URLEncoder.encode(path, encoding);
		} catch (UnsupportedEncodingException e) {
			throw new JargonException("unsupported encoding", e);

		}
	}

}

package com.paras.framework.excel.exception;

/**
 * Exception to represent that an error has occurred while importing an excel document.
 * @author Paras
 *
 */
public class ImportException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -350377483583784451L;
	
	/**
	 * Create new ImportException 
	 */
	public ImportException() {}
	
	/**
	 * Create new ImportException with message.
	 */
	public ImportException( String message ) {
		super( message );
	}
}

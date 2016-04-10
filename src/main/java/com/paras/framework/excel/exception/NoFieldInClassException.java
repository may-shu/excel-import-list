package com.paras.framework.excel.exception;

/**
 * Exception class to represent that there exists a field in the mappings that doesn't exists in the class file.
 * @author Paras
 *
 */
public class NoFieldInClassException extends ImportException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6405835345245689645L;
	
	/**
	 * Create new NoFieldInClassException
	 */
	public NoFieldInClassException() {
		super();
	}
	
	/**
	 * Create new NoFieldInClassException with message.
	 */
	public NoFieldInClassException( String field ) {
		super( "There is no setter defined for " + field );
	}
}

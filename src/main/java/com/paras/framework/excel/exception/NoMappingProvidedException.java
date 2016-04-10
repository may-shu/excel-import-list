package com.paras.framework.excel.exception;
/**
 * Exception to represent a error condition where a user has forgot to provide a valid mapping for a column present in the passed excel.
 * @author Paras
 *
 */
public class NoMappingProvidedException extends ImportException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3414847353579906232L;

	public NoMappingProvidedException() {
		super();
	}
	
	public NoMappingProvidedException( String column ) {
		super( "No Mapping provided for column : " + column );
	}
}

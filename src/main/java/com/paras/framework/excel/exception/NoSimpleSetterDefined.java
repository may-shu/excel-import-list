package com.paras.framework.excel.exception;

public class NoSimpleSetterDefined extends ImportException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4513449915611823321L;
	
	/**
	 * Create new NoSimpleSetterDefined for field.
	 */
	private static final String PARTIAL = "No simple setter defined for field :";
	
	public NoSimpleSetterDefined( String field ) {
		super( PARTIAL + field );
	}

}

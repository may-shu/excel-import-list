package com.paras.framework.excel.exception;

/**
 * Exception to represent that upload excel contains an unsupported data content.
 * @author Paras
 *
 */
public class UnSupportedCellContentException extends ImportException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8862874400052809470L;

	public UnSupportedCellContentException( String cellType ) {
		super( "Excel contains unsupported cell type : " + cellType );
	}
	
	public UnSupportedCellContentException( int cellType ) {
		super( "Excel contains unsupported cell type : CellType :" + cellType );
	}
}

package com.paras.framework.excel.base;

/**
 * Excel column to class field mapping.
 * @author Paras
 *
 */
public class ColumnToFieldMapping {

	/**
	 * Name of column in excel.
	 */
	private String column;
	
	/**
	 * Name of field in the class file.
	 */
	private String field;
	
	/**
	 * Create a new ColumnToFieldMapping
	 */
	public ColumnToFieldMapping( String column, String field ) {
		this.column = column;
		this.field = field;
	}

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}
}

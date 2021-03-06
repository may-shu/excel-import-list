package com.paras.framework.excel;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;

import com.paras.framework.excel.base.ColumnToFieldMapping;
import com.paras.framework.excel.exception.ImportException;
import com.paras.framework.excel.exception.NoFieldInClassException;
import com.paras.framework.excel.exception.NoMappingProvidedException;
import com.paras.framework.excel.exception.NoSimpleSetterDefined;
import com.paras.framework.excel.exception.UnSupportedCellContentException;

/**
 * ExcelImporter class to read a excel and return list of objects.
 * 
 * Out of convention, the first row in the first sheet of excel would be considered as header row.
 * Content of this row must be present in the column to field mapping array.
 * 
 * @author Paras
 *
 * @param <T> Object that a row represents.
 */
public class ExcelImporter<T> {

	private static Logger LOGGER = Logger.getLogger( ExcelImporter.class );

	private static final int FIRST = 0;

	private Class<T> type;

	/**
	 * Column to class field mappings.
	 */
	private ColumnToFieldMapping[] mappings;

	/**
	 * Create a new ExcelImporter.
	 */
	public ExcelImporter( Class<T> type ){
		this.type = type;
	}



	/**
	 * Create a new ExcelImpoter with following mappings.
	 * @param mappings
	 */
	public ExcelImporter( Class<T> type, ColumnToFieldMapping... mappings ) {
		this.mappings = mappings;
		this.type = type;
	}

	/**
	 * Method to extract information from passed InputStream.
	 * @param stream InputStream for the file to read.
	 * @return
	 * @throws IOException
	 * @throws ImportException 
	 */
	@SuppressWarnings("rawtypes")
	public List<T> extract( InputStream stream ) throws IOException, ImportException {
		LOGGER.info( "In ExcelImporter | Starting Execution of extract" );

		List<T> list = new ArrayList<T>();

		Workbook workbook = null;
		Sheet first = null;
		Row firstRow = null;

		Method[] methods = null;

		try{

			workbook = WorkbookFactory.create( stream );

			first = workbook.getSheetAt( FIRST );

			firstRow = first.getRow( FIRST );			
			methods = new Method[ firstRow.getLastCellNum() ];

			/**
			 * Setting up methods array.
			 */
			Iterator<Cell> headerCells = firstRow.cellIterator();


			while( headerCells.hasNext() ) {
				Cell cell = headerCells.next();

				String columnName = cell.getStringCellValue();
				String field = getFieldMappingForColumn( columnName );

				if( field == null ) {
					throw new NoMappingProvidedException( columnName );
				}

				Method method = getSetter( field );

				if( method == null ) {
					throw new NoFieldInClassException( field );
				}

				final int index = cell.getColumnIndex();
				methods[ index ] = method;
			}

			/**
			 * Headers has been set.
			 * Now, we need to store data.
			 */
			Iterator<Row> rows = first.iterator();
			int rowIndex = -1;
			while( rows.hasNext() ) {

				Row row = rows.next();
				++rowIndex;

				T rowDataHolder = type.newInstance();

				if( rowIndex == FIRST ) {
					continue;
				} else {
					
					LOGGER.info( "In ExcelImporter | In Extract | Going for row index " + row.getRowNum());

					int columnIndex = 0;
					int mappingSize = mappings.length;
					
					while( columnIndex < mappingSize ) {					

						Method method = methods[ columnIndex ];
						LOGGER.info( "In ExcelImporter | In extract | Calling " + method.getName() + " with first parameter class " + method.getParameterTypes()[0].getName() );
						 
						Cell cell = row.getCell(columnIndex, Row.RETURN_BLANK_AS_NULL );

						if( cell != null ) {
							
							CellReference cellRef = new CellReference( row.getRowNum(), cell.getColumnIndex() );
							cellRef.formatAsString();
	
							switch( cell.getCellType()) {
								case Cell.CELL_TYPE_STRING : 
									
									String str = cell.getStringCellValue().trim();
									
									if( str.isEmpty() || StringUtils.isEmpty( str ) || StringUtils.isBlank( str ) || str.equals( " ")) {
										method.invoke( rowDataHolder );
									} else {
										
										if( Long.TYPE.equals( method.getParameterTypes()[0].getClass())) {
											method.invoke( rowDataHolder, Long.valueOf( str ));
										}
										method.invoke( rowDataHolder, str );
										
									}
		
									break;
		
								case Cell.CELL_TYPE_NUMERIC : 
									if( DateUtil.isCellDateFormatted( cell )) {
		
										Date date = cell.getDateCellValue();
										method.invoke( rowDataHolder, date );
		
									} else {
		
										Double num = cell.getNumericCellValue();
		
										Class setterClass = method.getParameterTypes()[0];
		
										if( Long.class.equals( setterClass )) {									
											method.invoke( rowDataHolder, Math.round( num ));
		
										} else if( Integer.class.equals( setterClass )) {
											method.invoke( rowDataHolder, new Integer( (int) Math.round( num )));
										}
		
									}
									break;
		
								case Cell.CELL_TYPE_BOOLEAN :
									
									Boolean flag = cell.getBooleanCellValue();
									
									if( Boolean.class.getName().equals( method.getParameterTypes()[0].getName())) {
									
										method.invoke( rowDataHolder, flag );
										
									}
									
									method.invoke( rowDataHolder, String.valueOf( flag ));
									break;
		
								default:
									throw new UnSupportedCellContentException( cell.getCellType() );
							}
						}
						
						columnIndex++;

					}

				}

				list.add( rowDataHolder );

			}

		} catch( EncryptedDocumentException ex ) {

			LOGGER.error( "In ExcelImporter | Caught EncryptedDocumentException " + ex.getMessage());
			throw new IOException( "Document is encrypted." );

		} catch( InvalidFormatException ex ) {

			LOGGER.error( "In ExcelImporter | Caught InvalidFormatException " + ex.getMessage() );
			throw new IOException( "Document is in invalid format." );

		} catch ( IllegalAccessException ex ) {

			LOGGER.error( "In ExcelImporter | Caught IllegalAccessException " + ex.getMessage() );
			ex.printStackTrace();

			throw new ImportException( "In ExcelImporter | Caught IllegalAccessException " + ex.getMessage());
		} catch ( InstantiationException ex ) {

			LOGGER.error( "In ExcelImporter | Caught InstantiationException " + ex.getMessage() );
			ex.printStackTrace();

			throw new ImportException( "In ExcelImporter | Caught InstantiationException " + ex.getMessage() );

		} catch ( InvocationTargetException ex ) {

			LOGGER.error( "In ExcelImporter | Caught InvokationTargetException " + ex.getMessage() );
			ex.printStackTrace();

			throw new ImportException( "In ExcelImporter | Caught InvokationTargetException " + ex.getMessage() );

		}
		LOGGER.info( "In ExcelImporter | Finished Execution of extract" );
		return list;
	}

	public ColumnToFieldMapping[] getMappings() {
		return mappings;
	}

	public void setMappings(ColumnToFieldMapping[] mappings) {
		this.mappings = mappings;
	}

	private String getFieldMappingForColumn( String columnName ) {
		for( ColumnToFieldMapping mapping : mappings ) {
			if( columnName.equals( mapping.getColumn() )) {
				return mapping.getField();
			}
		}

		return null;
	}

	private Method getSetter( String field ) throws NoSimpleSetterDefined {
		Method[] methods = type.getDeclaredMethods();
		String setterName = getSetterName( field );
		boolean methodFound = false;
		Method setter = null;
		
		for( Method method : methods ) {
			if( setterName.equals( method.getName() )) {
				
				methodFound = true;
				if( isSimpleSetterType( method )) {
					setter = method;
					break;
				}
			}
		}
		
		if( methodFound && setter != null ) {
			return setter;
		}
		
		if( methodFound && setter == null ) {
			throw new NoSimpleSetterDefined( field );
		}

		return null;
	}

	private String getSetterName( String field ) {
		return "set" + StringUtils.capitalize( field );
	}
	
	private boolean isSimpleSetterType( Method method ) {
		
		@SuppressWarnings("rawtypes")
		Class firstParam = method.getParameterTypes()[0];
		
		if( Integer.class.equals( firstParam )) {
			return true;
		} else if( Character.class.equals( firstParam )) {
			return true;
		} else if( Boolean.class.equals( firstParam )) {
			return true;
		} else if( Long.class.equals( firstParam )) {
			return true;
		} else if( String.class.equals( firstParam )) {
			return true;
		} else if( Double.class.equals( firstParam )) {
			return true;
		} else if( Float.class.equals( firstParam )) {
			return true;
		}
		
		return false;
	}
}

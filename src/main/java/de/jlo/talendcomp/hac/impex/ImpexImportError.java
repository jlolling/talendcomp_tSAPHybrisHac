package de.jlo.talendcomp.hac.impex;

import java.math.BigDecimal;
import java.util.Date;

import de.jlo.talendcomp.hac.TypeUtil;

public class ImpexImportError {
	
	private ImpexDataLine dataLine = null;
	private String errorMessage = null;
	private int dataLineNumber = 0;
	private int errorLineNumber = 0;
	private TypeUtil typeUtil = new TypeUtil();
	
	public ImpexDataLine getDataLine() {
		return dataLine;
	}
	
	public int getDataLineNumber() {
		if (dataLine != null) {
			return dataLine.getLineNumber();
		} else {
			return -1;
		}
	}
	
	public Object getCorrelationKey(String fieldName) {
		if (dataLine != null) {
			return dataLine.getCorrelationKey(fieldName);
		} else {
			return null;
		}
	}
	
	public void setDataLine(ImpexDataLine dataLine) {
		this.dataLine = dataLine;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public int getErrorLineNumber() {
		return errorLineNumber;
	}
	
	public void setErrorLineNumber(int errorLineNumber) {
		this.errorLineNumber = errorLineNumber;
	}
	
	@Override
	public String toString() {
		if (dataLine != null) {
			return "DATA: " + dataLine + " DATA-LINE-NR: " + dataLineNumber + "\nERROR: " + errorMessage;
		} else {
			return "DATA: (no matching data found) \nERROR: " + errorMessage;
		}
	}

	/**
	 * Returns a output value for the current result record
	 * @param outgoingSchemaColumn
	 * @return the value
	 */
	public Object getOutputValueAsObject(String outgoingSchemaColumn, boolean nullable) throws Exception {
		if (dataLine != null) {
			Object value = dataLine.getCorrelationKey(outgoingSchemaColumn);
			if (value == null && nullable == false) {
				throw new Exception("For column: " + outgoingSchemaColumn + " null value detected but column is configured as not nullable");
			}
			return value;
		} else {
			return null;
		}
	}
	
	public String getOutputValueAsString(String outgoingSchemaColumn, boolean nullable) throws Exception {
		Object value = getOutputValueAsObject(outgoingSchemaColumn, nullable);
		return typeUtil.convertToString(value, null);
	}
	
	public Integer getOutputValueAsInteger(String outgoingSchemaColumn, boolean nullable) throws Exception {
		Object value = getOutputValueAsObject(outgoingSchemaColumn, nullable);
		return typeUtil.convertToInteger(value, null);
	}
	
	public Long getOutputValueAsLong(String outgoingSchemaColumn, boolean nullable) throws Exception {
		Object value = getOutputValueAsObject(outgoingSchemaColumn, nullable);
		return typeUtil.convertToLong(value, null);
	}
	
	public Double getOutputValueAsDouble(String outgoingSchemaColumn, boolean nullable) throws Exception {
		Object value = getOutputValueAsObject(outgoingSchemaColumn, nullable);
		return typeUtil.convertToDouble(value, null);
	}
	
	public Float getOutputValueAsFloat(String outgoingSchemaColumn, boolean nullable) throws Exception {
		Object value = getOutputValueAsObject(outgoingSchemaColumn, nullable);
		return typeUtil.convertToFloat(value, null);
	}
	
	public BigDecimal getOutputValueAsBigDecimal(String outgoingSchemaColumn, boolean nullable) throws Exception {
		Object value = getOutputValueAsObject(outgoingSchemaColumn, nullable);
		return typeUtil.convertToBigDecimal(value, null);
	}
	
	public Boolean getOutputValueAsBoolean(String outgoingSchemaColumn, boolean nullable) throws Exception {
		Object value = getOutputValueAsObject(outgoingSchemaColumn, nullable);
		return typeUtil.convertToBoolean(value, null);
	}
	
	public Date getOutputValueAsDate(String outgoingSchemaColumn, boolean nullable, String pattern) throws Exception {
		Object value = getOutputValueAsObject(outgoingSchemaColumn, nullable);
		return typeUtil.convertToDate(value, pattern, null);
	}

	public Short getOutputValueAsShort(String outgoingSchemaColumn, boolean nullable) throws Exception {
		Object value = getOutputValueAsObject(outgoingSchemaColumn, nullable);
		return typeUtil.convertToShort(value, null);
	}
	
}

package de.jlo.talendcomp.hac.impex;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.jlo.talendcomp.hac.HybrisHac;


public class Schema2ImpexBuilder extends HybrisHac {
	
	private String impexMacros = null;
	private List<ColumnConfig> schemaColumnToImpexColumnMap = new ArrayList<>();
	private String action = null;
	private String table = null;
	private int maxRecords = 1000;
	public static String[] IMPEX_ACTIONS = {"INSERT","UPDATE","INSERT_UPDATE","REMOVE"};
	private List<Map<String, Object>> records = new ArrayList<>();
	private Map<String, Object> oneRecord = new HashMap<>();
	private String impexContent = null;
	private boolean quoteValues = false;
	private SimpleDateFormat defaultDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	public void addColumnHeader(String schemaColumn, String impexColumn, String datepattern, String numberPattern) {
		if (isEmpty(schemaColumn)) {
			throw new IllegalArgumentException("Schema column cannot be null or empty. impexColumn: " + impexColumn);
		}
		if (isEmpty(impexColumn) == false) {
			// only if the impex header is present, the column is relevant for the impex 
			ColumnConfig cc = new ColumnConfig();
			cc.schemaColumn = schemaColumn;
			cc.impexHeader = impexColumn.trim();
			if (isEmpty(datepattern) == false) {
				cc.dateFornat = new SimpleDateFormat(datepattern.trim());
			}
			if (isEmpty(numberPattern) == false) {
				cc.numberFormat = new DecimalFormat(numberPattern);
			}
			schemaColumnToImpexColumnMap.add(cc);
		}
	}
	
	public void setTable(String table) {
		if (isEmpty(table)) {
			throw new IllegalArgumentException("table cannot be null or empty");
		}
		this.table = table;
	}
	
	public void setAction(String action) {
		if (isEmpty(action)) {
			throw new IllegalArgumentException("action cannot be null or empty");
		}
		for (String a : IMPEX_ACTIONS) {
			if (a.equalsIgnoreCase(action.toString())) {
				this.action = a;
				break;
			}
		}
		if (this.action == null) {
			throw new IllegalArgumentException("Unknown table action set: " + action);
		}
	}
	
	public void setImpexMacros(String macros) {
		if (isEmpty(macros) == false) {
			this.impexMacros = macros.toString();
		}
	}
	
	private ColumnConfig getColumnConfig(String schemaColumn) {
		for (ColumnConfig cc : schemaColumnToImpexColumnMap) {
			if (cc.schemaColumn.equals(schemaColumn)) {
				return cc;
			}
		}
		return null;
	}
	
	private boolean isColumnRelevant(String schemaColumn) {
		for (ColumnConfig cc : schemaColumnToImpexColumnMap) {
			if (cc.schemaColumn.equals(schemaColumn)) {
				return true;
			}
		}
		return false;
	}

	public void setRowValue(String schemaColumn, Object value) {
		if (isColumnRelevant(schemaColumn)) {
			oneRecord.put(schemaColumn, value);
		}
	}
	
	public void addRow() {
		records.add(oneRecord);
		oneRecord = new HashMap<>();
	}

	public int getMaxRecords() {
		return maxRecords;
	}

	public void setMaxRecords(int maxRecords) {
		this.maxRecords = maxRecords;
	}

	public String getImpexContent() {
		return impexContent;
	}
	
	public String buildImpex() {
		StringBuilder content = new StringBuilder();
		if (impexMacros != null) {
			content.append(impexMacros);
			content.append("\n");
		}
		content.append(action);
		content.append(" ");
		content.append(table);
		for (ColumnConfig cc : schemaColumnToImpexColumnMap) {
			content.append(";");
			content.append(cc.impexHeader);
		}
		content.append("\n");
		for (Map<String, Object> oneRecord : records) {
			content.append(buildRecordRow(oneRecord));
			content.append("\n");
		}
		return content.toString();
	}
	
	public String buildRecordRow(Map<String, Object> oneRecord) {
		StringBuilder oneLine = new StringBuilder();
		for (Map.Entry<String, Object> oneColumn : oneRecord.entrySet()) {
			oneLine.append(";");
			oneLine.append(convertFieldToString(oneColumn.getKey(), oneColumn.getValue()));
		}
		return oneLine.toString();
	}
	
	public String convertFieldToString(String schemaColumn, Object value) {
		if (value == null) {
			return "";
		}
		ColumnConfig cc = getColumnConfig(schemaColumn);
		if (value instanceof String) {
			if (quoteValues) {
				return "\"" + ((String) value).trim() + "\"";
			} else {
				return ((String) value).trim();
			}
		} else if (value instanceof Number) {
			if (cc.numberFormat != null) {
				return cc.numberFormat.format(value);
			} else {
				return value.toString();
			}
		} else if (value instanceof Date) {
			if (cc.dateFornat != null) {
				return cc.dateFornat.format((Date) value);
			} else {
				return defaultDateFormat.format((Date) value);
			}
		} else if (value instanceof Boolean) {
			return ((Boolean) value).toString();
		} else {
			return value.toString();
		}
	}

	public void setQuoteValues(boolean quoteValues) {
		this.quoteValues = quoteValues;
	}

}
package de.jlo.talendcomp.hac.impex;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;

public class ColumnConfig {

	String schemaColumn = null;
	String impexHeader = null;
	SimpleDateFormat dateFornat = null;
	NumberFormat numberFormat = null;
	
	public int hashCode() {
		return schemaColumn.hashCode();
	}
	
	public boolean equals(Object o) {
		if (o instanceof ColumnConfig) {
			return ((ColumnConfig) o).schemaColumn.equals(schemaColumn);
		} else {
			return false;
		}
	}
}

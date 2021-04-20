/**
 * Copyright 2021 Jan Lolling jan.lolling@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 *  * 
 * This project is based of the jhac implementation of Klaus Hausschild
 * https://github.com/klaushauschild1984/jhac
 */
package de.jlo.talendcomp.hac.flexiblesearch;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.hybris.hac.flexiblesearch.FlexibleSearchQuery;
import com.sap.hybris.hac.flexiblesearch.QueryResult;

import de.jlo.talendcomp.hac.HybrisHac;
import de.jlo.talendcomp.hac.TypeUtil;

public class FlexibleSearchRunner extends HybrisHac {

	private int queryResultCount = 0;
	private int currentResultIndex = 0;
	private List<Map<String, String>> resultset = null;
	private Map<String, String> oneResult = null;
	private List<String> headers = null;
	private TypeUtil typeUtil = new TypeUtil();
	private List<String> listTalendOutgoingColumns = new ArrayList<>();
	private int maxResults = 1000;
	private Map<String, String> columnMapping = new HashMap<>();

	public void execute(String query) throws Exception {
		queryResultCount = 0;
		currentResultIndex = 0;
		oneResult = null;
		FlexibleSearchQuery fsquery = FlexibleSearchQuery
				.builder()
				.flexibleSearchQuery(query)
				.maxCount(maxResults)
				.build();
		QueryResult result = getConsole().flexibleSearch().query(fsquery);
		if (result.hasError()) {
			throw new Exception("Execute query:\n" + query + "\n failed: " + result.getException().getMessage(), result.getException());
		} else {
			queryResultCount = result.getResultCount();
			headers = result.getHeaders();
			if (headers.size() == 0) {
				throw new Exception("No headers received for query: " + query);
			}
			resultset = result.asMap();
		}
	}
	
	private String getQueryColumn(String schemaColumn) {
		String altName = columnMapping.get(schemaColumn);
		if (altName != null) {
			return altName;
		} else {
			return schemaColumn;
		}
	}
	
	public int getQueryResultCount() {
		return queryResultCount;
	}

	/**
	 * add a Talend schema output variable for the validation for later validation
	 * @param schemaOutputColumn
	 */
	public void addExpectedOutputVariable(String schemaOutputColumn) {
		if (isEmpty(schemaOutputColumn)) {
			throw new IllegalArgumentException("schemaOutputColumn cannot be null or empty");
		}
		listTalendOutgoingColumns.add(schemaOutputColumn);
	}

	public void validateOutputVariables() throws Exception {
		if (listTalendOutgoingColumns == null || listTalendOutgoingColumns.size() == 0) {
			throw new IllegalArgumentException("No outgoing schema columns given. At least one column is needed");
		}
		if (headers == null) {
			throw new IllegalStateException("Query not run before, no headers available");
		}
		StringBuilder missing = new StringBuilder();
		for (String col : listTalendOutgoingColumns) {
			if (headers.contains(col) == false) {
				// if a field has no alias the original name is always lowercase with p_ prefix
				String p_name = "p_" + col.toLowerCase();
				if (headers.contains(p_name)) {
					columnMapping.put(col, p_name);
				} else {
					if (missing.length() == 0) {
						missing.append("Outgoing schema column without matching query column (header) found: \n");
					} else {
						missing.append(",");
					}
					missing.append(col);
				}
			}
		}
		if (missing.length() > 1) {
			// add info about available headers
			missing.append("\nAvailable columns in query result: \n");
			boolean firstLoop = true;
			for (String header : headers) {
				if (firstLoop) {
					firstLoop = false;
				} else {
					missing.append(",");
				}
				missing.append(header);
			}
			throw new Exception("Check Talend column mapping to query result failed: " + missing.toString());
		}
	}
	
	public boolean next() {
		if (currentResultIndex < queryResultCount) {
			oneResult = resultset.get(currentResultIndex++);
			return true;
		} else {
			oneResult = null;
			return false;
		}
	}

	/**
	 * Returns a output value for the current result record
	 * @param outgoingSchemaColumn
	 * @return the value
	 */
	public Object getOutputValue(String outgoingSchemaColumn, boolean nullable) throws Exception {
		if (oneResult == null) {
			throw new IllegalStateException("We expect to have one result record but there is none.");
		} else {
			Object value = oneResult.get(getQueryColumn(outgoingSchemaColumn));
			if (value == null && nullable == false) {
				throw new Exception("For column: " + outgoingSchemaColumn + " null value detected but column is configured as not nullable");
			}
			return value;
		}
	}
	
	public String getOutputValueAsString(String outgoingSchemaColumn, boolean nullable) throws Exception {
		Object value = getOutputValue(outgoingSchemaColumn, nullable);
		return typeUtil.convertToString(value, null);
	}
	
	public Integer getOutputValueAsInteger(String outgoingSchemaColumn, boolean nullable) throws Exception {
		Object value = getOutputValue(outgoingSchemaColumn, nullable);
		return typeUtil.convertToInteger(value, null);
	}
	
	public Long getOutputValueAsLong(String outgoingSchemaColumn, boolean nullable) throws Exception {
		Object value = getOutputValue(outgoingSchemaColumn, nullable);
		return typeUtil.convertToLong(value, null);
	}
	
	public Double getOutputValueAsDouble(String outgoingSchemaColumn, boolean nullable) throws Exception {
		Object value = getOutputValue(outgoingSchemaColumn, nullable);
		return typeUtil.convertToDouble(value, null);
	}
	
	public Float getOutputValueAsFloat(String outgoingSchemaColumn, boolean nullable) throws Exception {
		Object value = getOutputValue(outgoingSchemaColumn, nullable);
		return typeUtil.convertToFloat(value, null);
	}
	
	public BigDecimal getOutputValueAsBigDecimal(String outgoingSchemaColumn, boolean nullable) throws Exception {
		Object value = getOutputValue(outgoingSchemaColumn, nullable);
		return typeUtil.convertToBigDecimal(value, null);
	}
	
	public Boolean getOutputValueAsBoolean(String outgoingSchemaColumn, boolean nullable) throws Exception {
		Object value = getOutputValue(outgoingSchemaColumn, nullable);
		return typeUtil.convertToBoolean(value, null);
	}
	
	public Date getOutputValueAsDate(String outgoingSchemaColumn, boolean nullable, String pattern) throws Exception {
		Object value = getOutputValue(outgoingSchemaColumn, nullable);
		return typeUtil.convertToDate(value, pattern, null);
	}

	public Short getOutputValueAsShort(String outgoingSchemaColumn, boolean nullable) throws Exception {
		Object value = getOutputValue(outgoingSchemaColumn, nullable);
		return typeUtil.convertToShort(value, null);
	}

	public int getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(Integer maxResults) {
		if (maxResults != null && maxResults > 0) {
			this.maxResults = maxResults;
		}
	}

}

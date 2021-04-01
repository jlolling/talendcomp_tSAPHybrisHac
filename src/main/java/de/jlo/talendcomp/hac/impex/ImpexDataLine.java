package de.jlo.talendcomp.hac.impex;

import java.util.HashMap;
import java.util.Map;

public class ImpexDataLine {
	
	private String content;
	private Map<String, Object> correlationKeys = new HashMap<>();
	private int lineNumber = 0;
	
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		if (content == null) {
			throw new IllegalArgumentException("line content cannot be null");
		}
		this.content = content;
	}
	
	public Object getCorrelationKey(String fieldName) {
		return correlationKeys.get(fieldName);
	}
	
	public void setCorrelationKey(String fieldName, Object correlationKey) {
		this.correlationKeys.put(fieldName, correlationKey);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof ImpexDataLine) {
			return ((ImpexDataLine) o).content.equals(content);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return content.hashCode();
	}
	
	@Override
	public String toString() {
		return content;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

}

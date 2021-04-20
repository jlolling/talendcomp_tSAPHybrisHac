package de.jlo.talendcomp.hac.impex;

import java.util.ArrayList;
import java.util.List;

import com.sap.hybris.hac.impex.Impex;
import com.sap.hybris.hac.impex.ImpexResult;

import de.jlo.talendcomp.hac.HybrisHac;
import de.jlo.talendcomp.hac.TypeUtil;

public class ImpexImportRunner extends HybrisHac {
	
	private List<ImpexDataLine> dataLines = new ArrayList<>();
	private String impexHeader = null;
	private List<String> impexErrorResponseLines = new ArrayList<>();
	private List<ImpexImportError> importErrors = new ArrayList<>();
	private String payloadStr = null;
	private boolean hasErrors = false;
	private int incomingLineNumber = 0;
	private int countImpexLines = 0;
	private int countRejectedLines = 0;
	private ImpexDataLine currentDataLine = null;
	
	public void addDataRow(String line) {
		if (line != null && line.trim().isEmpty() == false) {
			if (line.startsWith(";") == false) {
				line = ";" + line;
			}
			line = line.trim();
			incomingLineNumber++;
			currentDataLine = new ImpexDataLine();
			currentDataLine.setContent(line);
			currentDataLine.setLineNumber(incomingLineNumber);
			if (dataLines.contains(currentDataLine) == false) {
				dataLines.add(currentDataLine);
				countImpexLines++;
			} else {
				countRejectedLines++;
			}
		}
	}
	
	public void setCorrelationKey(String key, Object value) {
		if (key == null || key.trim().isEmpty()) {
			throw new IllegalArgumentException("key cannot be null or empty");
		}
		if (value != null) {
			currentDataLine.setCorrelationKey(key, value);
		}
	}
	
	public void execute() throws Exception {
		StringBuilder payload = new StringBuilder();
		payload.append(impexHeader);
		payload.append("\n");
		for (ImpexDataLine line : dataLines) {
			payload.append(line.getContent());
			payload.append("\n");
		}
		payloadStr = payload.toString();
		try {
			ImpexResult result = getConsole().impex()
			   .importData(
					Impex.builder()
		            	.scriptContent(payloadStr)
		            	.buildImport());
			impexErrorResponseLines = result.getErrors();
			hasErrors = result.hasError();
			// match input data lines against error response lines
		} catch (Throwable e) {
			String shortedPayload = payloadStr;
			if (payloadStr.length() > 2048) {
				shortedPayload = payloadStr.substring(0,2048) + "...cutted in exception message";
			}
			throw new Exception("Impex import failed: " + e.getMessage() + " payload (size " + payloadStr.length() + "):\n" + shortedPayload, e);
		}
	}
	
	public void matchImportErrors() {
		for (int e = 0, em = impexErrorResponseLines.size(); e < em; e++) {
			String errorLine = impexErrorResponseLines.get(e);
			if (TypeUtil.isEmpty(errorLine) == false) {
				String crunchedErrorLine = crunch(errorLine);
				boolean foundDataLine = false;
				for (int d = 0, dm = dataLines.size(); d < dm; d++) {
					ImpexDataLine dataLine = dataLines.get(d);
					String crunchedDataLine = crunch(dataLine.getContent());
					if (crunchedErrorLine.contains(crunchedDataLine)) {
						foundDataLine = true;
						ImpexImportError error = new ImpexImportError();
						error.setDataLine(dataLine);
						error.setErrorMessage(errorLine);
						error.setErrorLineNumber(e);
						importErrors.add(error);
						break;
					}
				}
				if (foundDataLine == false) {
					ImpexImportError error = new ImpexImportError();
					error.setDataLine(null);
					error.setErrorMessage(TypeUtil.unescapeHtmlEntities(errorLine));
					error.setErrorLineNumber(e);
					importErrors.add(error);
				}
			}
		}
	}
	
	public static String crunch(String line) {
		return TypeUtil.unescapeHtmlEntities(line.replace(" ", "").replace("\"", "").replace("\n", "").replace("<ignore>", "")).toLowerCase();
	}
	
	public List<ImpexImportError> getImportErrors() {
		return importErrors;
	}

	public String getImpexHeader() {
		return impexHeader;
	}

	public void setImpexHeader(String impexHeader) {
		if (impexHeader == null || impexHeader.trim().isEmpty()) {
			throw new IllegalArgumentException("Impex headercannot be null or empty");
		}
		this.impexHeader = impexHeader;
	}

	public String getImpexContent() {
		return payloadStr;
	}

	public boolean hasErrors() {
		return hasErrors;
	}

	public int getCountImpexLines() {
		return countImpexLines;
	}

	public int getCountRejectedLines() {
		return countRejectedLines;
	}

}

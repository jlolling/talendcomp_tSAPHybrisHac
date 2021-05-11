package de.jlo.talendcomp.hac.impex;

import com.sap.hybris.hac.scripting.Script;
import com.sap.hybris.hac.scripting.ScriptResult;

import de.jlo.talendcomp.hac.HybrisHac;
import de.jlo.talendcomp.hac.TypeUtil;

public class ScriptRunner extends HybrisHac {
	
	private String script = null;
	private String scriptType = null;
	private ScriptResult result = null;
	
	public void execute() throws Exception {
		result = getConsole()
			.scripting()
			.execute(Script.builder()
						.script(script)
						.build());
	}
	
	public boolean hasErrors() {
		if (result != null) {
			return result.hasError();
		} else {
			throw new IllegalStateException("No result available, did you call execute before?");
		}
	}
	
	public String getOutputText() {
		if (result != null) {
			return result.getOutputText();
		} else {
			throw new IllegalStateException("No result available, did you call execute before?");
		}
	}

	public String getExecutionResult() {
		if (result != null) {
			return result.getExecutionResult();
		} else {
			throw new IllegalStateException("No result available, did you call execute before?");
		}
	}
	
	public String getExceptionStackTrace() {
		if (result != null) {
			return result.getStacktraceText();
		} else {
			throw new IllegalStateException("No result available, did you call execute before?");
		}
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		if (TypeUtil.isEmpty(script)) {
			throw new IllegalArgumentException("Script cannot be null or empty");
		}
		this.script = script;
	}

	public String getScriptType() {
		return scriptType;
	}

	public void setScriptType(String scriptType) {
		if (TypeUtil.isEmpty(scriptType)) {
			throw new IllegalArgumentException("Script-Type cannot be null or empty");
		}
		this.scriptType = scriptType;
	}

}

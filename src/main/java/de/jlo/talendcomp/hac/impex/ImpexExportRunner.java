package de.jlo.talendcomp.hac.impex;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import com.sap.hybris.hac.impex.Impex;
import com.sap.hybris.hac.impex.ImpexResult;

import de.jlo.talendcomp.hac.HybrisHac;

public class ImpexExportRunner extends HybrisHac {

	private String impexHeaderWithFX = null;
	private ImpexResult impexResult = null;
	private String outputZipFile = null;
	
	public void execute() throws Exception {
		if (outputZipFile == null || outputZipFile.trim().isEmpty()) {
			throw new IllegalStateException("outputFile cannot be null or empty. method setOutputfile is not used before.");
		}
		File of = new File(outputZipFile);
		File od = of.getParentFile();
		od.mkdirs();
		try {
			impexResult = console
				    .impex()
				    .exportData(
				        Impex.builder()
				            .scriptContent(impexHeaderWithFX)
				            .buildExport());
		} catch (Exception e) {
			throw new Exception("Execute impex export failed: " + e.getMessage() + " impex-header: \n" + impexHeaderWithFX, e);
		}
		List<byte[]> result = impexResult.getExportResources();
		OutputStream fout = new FileOutputStream(of);
		try {
			for (byte[] b : result) {
				fout.write(b);
			}
		} catch (Exception e) {
			throw new Exception("Write result to output zip file: " + outputZipFile + " failed: " + e.getMessage(), e);
		} finally {
			fout.flush();
			fout.close();
		}	
	}

	public String getImpexHeaderWithFX() {
		return impexHeaderWithFX;
	}

	public void setImpexHeaderWithFX(String impexHeaderWithFX) {
		this.impexHeaderWithFX = impexHeaderWithFX;
	}

	public String getOutputZipFile() {
		return outputZipFile;
	}

	public void setOutputZipFile(String outputFile) {
		if (outputFile == null || outputFile.trim().isEmpty()) {
			throw new IllegalArgumentException("outputFile cannot be null or empty");
		}
		this.outputZipFile = outputFile;
	}
	
}

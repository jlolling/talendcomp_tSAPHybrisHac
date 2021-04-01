package de.jlo.talendcomp.hac;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import de.jlo.talendcomp.hac.impex.ImpexExportRunner;

public class TestImpexExportRunner {
	
	private Properties context = new Properties();
	
	@Before
	public void readProperties() throws Exception {
		File f = new File("/var/data/talend/resources/context/hac.properties");
		if (f.exists() == false) {
			throw new Exception("Context file does not exist");
		}
		InputStream fin = new FileInputStream(f);
		try {
			context.load(fin);
		} finally {
			fin.close();
		}
	}

	@Test
	public void testExportSimple() throws Exception {
		String endpoint = context.getProperty("hac_endpoint");
		String user = context.getProperty("hac_login");
		String pw = context.getProperty("hac_password");
		String htuser = context.getProperty("hac_htaccess_login");
		String htpw = context.getProperty("hac_htaccess_password");
		ImpexExportRunner r = new ImpexExportRunner();
		r.connect(endpoint, user, pw, htuser, htpw);
		String outputFile = "/tmp/test_impex_export.zip";
		r.setOutputZipFile(outputFile);
		String impexHeader = "INSERT_UPDATE VendorCustomer; customerCode; vendor(code)[unique = true]; unit(uid)[unique = true]; blocked[default = false]; visibleProducts(code)";
		r.setImpexHeaderWithFX(impexHeader);
		r.execute();
		assertTrue("No file was created", new File(outputFile).exists());
	}
	

}

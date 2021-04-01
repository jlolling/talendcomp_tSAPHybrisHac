package de.jlo.talendcomp.hac;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

public class TestImpexExport {
	
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
		
	}
	

}

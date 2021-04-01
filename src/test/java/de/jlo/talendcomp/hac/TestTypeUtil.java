package de.jlo.talendcomp.hac;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestTypeUtil {

	@Test
	public void testUnescapeHtml() {
		String in = "&#039;true&#039;";
		String out = TypeUtil.unescapeHtmlEntities(in);
		System.out.println(out);
		assertEquals("'true'", out);
	}
}

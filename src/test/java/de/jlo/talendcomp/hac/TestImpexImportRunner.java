package de.jlo.talendcomp.hac;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import de.jlo.talendcomp.hac.impex.ImpexImportError;
import de.jlo.talendcomp.hac.impex.ImpexImportRunner;
import de.jlo.talendcomp.hac.impex.Schema2ImpexBuilder;

public class TestImpexImportRunner {
	
	String impexHeader = "$catalogVersion=catalogversion(catalog(id[default='otkproductcatalog']),version[default='Online'])[unique=true]\n"
		    + "        $approved=approvalstatus(code)[default='approved']\n"
		    + "        $taxGroup=Europe1PriceFactory_PTG(code)[default=eu-vat-full]\n"
		    + "        $supercategories=supercategories(code,catalogversion(catalog(id[default='globalMarketplaceProductCatalog']),version[default='Online']))\n"
		    + "        $lang=en\n"
		    + "        $vendor=otk\n"
		    + "        $baseProduct=baseProduct(code, $catalogVersion)\n"
		    + "        $feature=system='MarketplaceClassification', version='1.0', translator=de.hybris.platform.catalog.jalo.classification.impex.ClassificationAttributeTranslator\n"
		    + "        $variantType=variantType(code)[unique = true,default = 'BatchVariantProduct']\n"
		    + "        INSERT_UPDATE Product;vendorSku;code[unique=true];name[lang=en];name[lang=de];displayName[lang=en];displayName[lang=de];description[lang=en];description[lang=de];summary[lang=en];summary[lang=de]; unit(code); alternateSellingUnits(code); priceUnit(code);$supercategories[vendor=$vendor][globalCatalogId='globalMarketplaceProductCatalog'][globalCatalogVersion='Online'][translator=de.hybris.platform.marketplaceservices.dataimport.batch.translator.MarketplaceCategoryTranslator];@thickness_otk[$feature];@width[$feature];@length[$feature];@lengthCoil[$feature];@weight_otk[$feature];@lengthCut_otk[$feature];@rangeOTK[$feature];@gradeEN[$feature];@finish[$feature];@gradeASTM[$feature];@standardsApproval[$feature];@surface[$feature];@otk_surfaceProtection[$feature];@interleavingPaper[$feature];@marking[$feature];@aproxnumberSheet[$feature];sequenceId[translator=de.hybris.platform.acceleratorservices.dataimport.batch.converter.SequenceIdTranslator];$catalogVersion;$approved;vendors(code)[default=$vendor];deliveryRegions(vendor(code),code)[default=$vendor:default];leadTime;$variantType;@otk_certificate_3_1[$feature, default=TRUE];\n";

	String impexData = ";;\"4307_Core_T3.0_W1000-L2000_2B_with_interleaving_paper\";\"otk_4307_Core_T3.0_W1000-L2000_2B_with_interleaving_paper\";;;;;;;;;\"PK\";;\"KGM\";\"otk_stockSheets\";\"3.0\";1000;2000;;1000;;\"rangeOTK_Core\";\"4307\";\"finish_2B\";\"gradeASTM_304,gradeASTM_304L\";\"standardsApproval_EN_10028_7,standardsApproval_EN_10088_2, standardsApproval_EN_10088_4\";;\"foiling_no\";\"interleavingPaper_yes\";\"marking_yes\";;;;\"approved\";;;2";

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
	public void testCreateImpex() throws Exception {
		Date testDate1 = new SimpleDateFormat("yyyy-MM-dd").parse("2021-03-10");
		Date testDate2 = new SimpleDateFormat("yyyy-MM-dd").parse("2021-01-01");
		String macro = "@cu = code[unique=tr]\n@name = name[lang=de]";
		Schema2ImpexBuilder h = new Schema2ImpexBuilder();
		h.setImpexMacros(macro);
		h.setAction("insert_update");
		h.setTable("Product");
		h.addColumnHeader("id", "@cu", null, "##########0");
		h.addColumnHeader("name", "@name", null, null);
		h.addColumnHeader("is_valid", "valid", null, null);
		h.addColumnHeader("unused", "", null, null);
		h.addColumnHeader("created_at", "creationDate", "dd.MM.yyyy", null);
		h.setRowValue("id", 12345678);
		h.setRowValue("name", "fancy-product1");
		h.setRowValue("isvalid", true);
		h.setRowValue("unused", new Date());
		h.setRowValue("created_at", testDate1);
		h.addRow();
		h.setRowValue("id", 98765432);
		h.setRowValue("name", "fancy-product2");
		h.setRowValue("isvalid", false);
		h.setRowValue("unused", new Date());
		h.setRowValue("created_at", testDate2);
		h.addRow();
		String actual = h.buildImpex();
		System.out.println(actual);
		String expected = "@cu = code[unique=tr]\n" +
				          "@name = name[lang=de]\n" +
						  "INSERT_UPDATE Product;@cu;@name;valid;creationDate\n" +
						  ";fancy-product1;10.03.2021;12345678\n" +
						  ";fancy-product2;01.01.2021;98765432";
		assertEquals("Impex wrong", expected.trim(), actual.trim());
	}
	
	@Test
	public void testConnectionFailedTest() throws Exception {
		String endpoint = context.getProperty("hac_endpoint");
		String user = context.getProperty("hac_login");
		String pw = context.getProperty("hac_password");
		String htuser = context.getProperty("hac_htaccess_login");
		String htpw = context.getProperty("hac_htaccess_password");
		ImpexImportRunner r = new ImpexImportRunner();
		try {
			r.connect(endpoint, user, pw, htuser, htpw);
			assertTrue(false);
		} catch (Exception e) {
			assertTrue(true);
		}
	}
	
	@Test
	public void testImpexImport() throws Exception {
		String endpoint = context.getProperty("hac_endpoint");
		String user = context.getProperty("hac_login");
		String pw = context.getProperty("hac_password");
		String htuser = context.getProperty("hac_htaccess_login");
		String htpw = context.getProperty("hac_htaccess_password");
		ImpexImportRunner r = new ImpexImportRunner();
		r.connect(endpoint, user, pw, htuser, htpw);
		r.setImpexHeader(impexHeader);
		r.addDataRow(impexData);
		r.setCorrelationKey("key", impexData.hashCode());
		r.execute();
		System.out.println(r.hasErrors());
		List<ImpexImportError> errors = r.getImportErrors();
		for (ImpexImportError e : errors) {
			System.out.println(e);
		}
	}
	
	@Test
	public void testMatch1() {
		String errorLine = ImpexImportRunner.crunch(",8804729421825,,,column 22: Classification attribute value 4571 not found| column 22: Classification attribute value 4571 not found ;<ignore>4571_Supra_T5.0_W1250-L2500_2B_with_interleaving_paper;<ignore>otk_4571_Supra_T5.0_W1250-L2500_2B_with_interleaving_paper;<ignore>Supra 316Ti/4571 - 2B sheet;<ignore>Supra 316Ti/4571 - 2B Blech;\"<ignore><p>Exact delivery cost will be shown in checkout-process and depends on order quantity</p><p><strong>Outokumpu Supra Range</strong></p> <ul> Stainless steels for highly corrosive environments (PRE 22 to 27) Austenitics are highly formable and weldable and used in claddings, storage tanks, and heat exchangers. Includes the well-known Outokumpu Supra 316L/4404 as well as several alternatives. Ferritic stainless steels are nickel free, have good formability, and can be used in applications with elevated temperatures <iframe width=\"\"640\"\" height=\"\"360\"\" src=\"\"https://www.youtube.com/embed/1Zt7e8vYEZc\"\" frameborder=\"\"0\"\" allow=\"\"autoplay; encrypted-media\"\" allowfullscreen></iframe>\"\"\";\"<ignore><span style=\"\"color: #306084\"\">Supra 316Ti/4571</span> <br/> <span style=\"\"color: #306084\"\"> </span>\";\"<ignore><p>Exact delivery cost will be shown in checkout-process and depends on order quantity</p><p><strong>Outokumpu Supra Range</strong></p> <ul> Stainless steels for highly corrosive environments (PRE 22 to 27) Austenitics are highly formable and weldable and used in claddings, storage tanks, and heat exchangers. Includes the well-known Outokumpu Supra 316L/4404 as well as several alternatives. Ferritic stainless steels are nickel free, have good formability, and can be used in applications with elevated temperatures <iframe width=\"\"640\"\" height=\"\"360\"\" src=\"\"https://www.youtube.com/embed/1Zt7e8vYEZc\"\" frameborder=\"\"0\"\" allow=\"\"autoplay; encrypted-media\"\" allowfullscreen></iframe>\"\"\";\"<ignore><p>Genauer Lieferpreis wird im Checkout-Prozess angezeigt und hängt von Bestellmenge ab</p> <p><strong>Outokumpu Supra Kategorie</strong></p> <ul> Für stark korrosive Umgebungen (PRE 22 bis 27). Enthält den bekannten Outokumpu Supra 316L/4404 sowie verschiedene Alternativen. Austenitische Güten sind ausgezeichnet form- und schweißbar und werden für Verkleidungen, Lagerbehälter und Wärmeaustauscher verwendet. Ferritischer Edelstahl ist nickelfrei, gut formbar und kann in Anwendungsbereichen mit höheren Temperaturen verwendet werden. <iframe width=\"\"640\"\" height=\"\"360\"\" src=\"\"https://www.youtube.com/embed/1Zt7e8vYEZc\"\" frameborder=\"\"0\"\" allow=\"\"autoplay; encrypted-media\"\" allowfullscreen></iframe>\"\"\";\"<ignore><p><strong>(!) Maximum order quantity: 1 package</strong><br><br></p><p>Toll costs (6.50 €/t) are included in product price</p>\";\"<ignore><p><strong>(!) Maximale Bestellmenge: 1 Paket</strong><br><br></p><p>Mautkosten (6,50 €/t) sind im Produktpreis enthalten</p>\";<ignore>PK;<ignore>;<ignore>KGM;<ignore>otk_stockSheets;<ignore>5.0;<ignore>1250;<ignore>2500;<ignore>;<ignore>1250;<ignore>;<ignore>rangeOTK_Supra;4571;<ignore>finish_2B;<ignore>gradeASTM_316Ti;<ignore>standardsApproval_EN_10028_7,standardsApproval_EN_10088_2, standardsApproval_EN_10088_4;<ignore>;<ignore>foiling_no;<ignore>interleavingPaper_yes;<ignore>marking_yes;<ignore>;<ignore>;<ignore>;<ignore>approved;<ignore>;<ignore>;<ignore>2;<ignore>");
		String dataLine = ImpexImportRunner.crunch(";\"4571_Supra_T5.0_W1250-L2500_2B_with_interleaving_paper\";\"otk_4571_Supra_T5.0_W1250-L2500_2B_with_interleaving_paper\";\"Supra 316Ti/4571 - 2B sheet\";\"Supra 316Ti/4571 - 2B Blech\";\"<p>Exact delivery cost will be shown in checkout-process and depends on order quantity</p><p><strong>Outokumpu Supra Range</strong></p> <ul> Stainless steels for highly corrosive environments (PRE 22 to 27) Austenitics are highly formable and weldable and used in claddings, storage tanks, and heat exchangers. Includes the well-known Outokumpu Supra 316L/4404 as well as several alternatives. Ferritic stainless steels are nickel free, have good formability, and can be used in applications with elevated temperatures <iframe width=\"\"640\"\" height=\"\"360\"\" src=\"\"https://www.youtube.com/embed/1Zt7e8vYEZc\"\" frameborder=\"\"0\"\" allow=\"\"autoplay; encrypted-media\"\" allowfullscreen></iframe>\"\"\";\"<span style=\"\"color: #306084\"\">Supra 316Ti/4571</span> <br/> <span style=\"\"color: #306084\"\"> </span>\";\"<p>Exact delivery cost will be shown in checkout-process and depends on order quantity</p><p><strong>Outokumpu Supra Range</strong></p> <ul> Stainless steels for highly corrosive environments (PRE 22 to 27) Austenitics are highly formable and weldable and used in claddings, storage tanks, and heat exchangers. Includes the well-known Outokumpu Supra 316L/4404 as well as several alternatives. Ferritic stainless steels are nickel free, have good formability, and can be used in applications with elevated temperatures <iframe width=\"\"640\"\" height=\"\"360\"\" src=\"\"https://www.youtube.com/embed/1Zt7e8vYEZc\"\" frameborder=\"\"0\"\" allow=\"\"autoplay; encrypted-media\"\" allowfullscreen></iframe>\"\"\";\"<p>Genauer Lieferpreis wird im Checkout-Prozess angezeigt und h&auml;ngt von Bestellmenge ab</p> <p><strong>Outokumpu Supra Kategorie</strong></p> <ul> F&uuml;r stark korrosive Umgebungen (PRE 22 bis 27). Enth&auml;lt den bekannten Outokumpu Supra 316L/4404 sowie verschiedene Alternativen. Austenitische G&uuml;ten sind ausgezeichnet form- und schwei&szlig;bar und werden f&uuml;r Verkleidungen, Lagerbeh&auml;lter und W&auml;rmeaustauscher verwendet. Ferritischer Edelstahl ist nickelfrei, gut formbar und kann in Anwendungsbereichen mit h&ouml;heren Temperaturen verwendet werden. <iframe width=\"\"640\"\" height=\"\"360\"\" src=\"\"https://www.youtube.com/embed/1Zt7e8vYEZc\"\" frameborder=\"\"0\"\" allow=\"\"autoplay; encrypted-media\"\" allowfullscreen></iframe>\"\"\";\"<p><strong>(!) Maximum order quantity: 1 package</strong><br><br></p><p>Toll costs (6.50 &euro;/t) are included in product price</p>\";\"<p><strong>(!) Maximale Bestellmenge: 1 Paket</strong><br><br></p><p>Mautkosten (6,50 &euro;/t) sind im Produktpreis enthalten</p>\";\"PK\";;\"KGM\";\"otk_stockSheets\";\"5.0\";1250;2500;;1250;;\"rangeOTK_Supra\";\"4571\";\"finish_2B\";\"gradeASTM_316Ti\";\"standardsApproval_EN_10028_7,standardsApproval_EN_10088_2, standardsApproval_EN_10088_4\";;\"foiling_no\";\"interleavingPaper_yes\";\"marking_yes\";;;;\"approved\";;;2");
		System.out.println(errorLine);
		System.out.println(dataLine);
		if (errorLine.contains(dataLine) == false) {
			assertTrue(false);
		} else {
			assertTrue(true);
		}
	}

	@Test
	public void testMatch2() {
		String errorLine = ImpexImportRunner.crunch(",8806844104705,,,column 3: cannot resolve value 'OTK-1D-scaled' for attribute 'galleryImages';<ignore>otk_4307_Core_T3.0_W1500_1D_295225_cuttable-l;<ignore>/ProductImageUploadFormat/OTK-1D-scaled.png;OTK-1D-scaled;<ignore>");
		String dataLine = ImpexImportRunner.crunch(";\"otk_4307_Core_T3.0_W1500_1D_295225_cuttable-l\";\"/ProductImageUploadFormat/OTK-1D-scaled.png\";\"OTK-1D-scaled\"");
		System.out.println(errorLine);
		System.out.println(dataLine);
		if (errorLine.contains(dataLine) == false) {
			assertTrue(false);
		} else {
			assertTrue(true);
		}
	}

	@Test
	public void testCheckDuplicates() {
		String dataLine1 = ";\"otk_4307_Core_T3.0_W1500_1D_295225_cuttable-l\";\"/ProductImageUploadFormat/OTK-1D-scaled.png\";\"OTK-1D-scaled\"";
		String dataLine2 = ";\"otk_4307_Core_T3.0_W1500_1D_295225_cuttable-l\";\"/ProductImageUploadFormat/OTK-1D-scaled.png\";\"OTK-1D-scaled\"";
		String dataLine3 = ";\"otk_4307_Core_T3.0_W1500_1D_295226_cuttable-l\";\"/ProductImageUploadFormat/OTK-1D-scaled.png\";\"OTK-1D-scaled\"";
		ImpexImportRunner r = new ImpexImportRunner();
		r.addDataRow(dataLine1);
		r.addDataRow(dataLine2);
		r.addDataRow(dataLine3);
		int expected = 2;
		int actual = r.getCountImpexLines();
		assertEquals(expected, actual);
	}
	
	String impexVariantProductHeader = "$catalogVersion=catalogversion(catalog(id[default='otkproductcatalog']),version[default='Online'])[unique=true]\n"
			+ "        $approved=approvalstatus(code)[default='approved']\n"
			+ "        $taxGroup=Europe1PriceFactory_PTG(code)[default=eu-vat-full]\n"
			+ "        $supercategories=supercategories(code,catalogversion(catalog(id[default='globalMarketplaceProductCatalog']),version[default='Online']))\n"
			+ "        $lang=en\n"
			+ "        $vendor=otk\n"
			+ "        $baseProduct=baseProduct(code, $catalogVersion)\n"
			+ "        $feature=system='MarketplaceClassification', version='1.0', translator=de.hybris.platform.catalog.jalo.classification.impex.ClassificationAttributeTranslator\n"
			+ "INSERT_UPDATE BatchVariantProduct;vendorSku;code[unique=true];name[lang=en];name[lang=de];displayName[lang=en];displayName[lang=de];description[lang=en];description[lang=de];summary[lang=en];summary[lang=de]; unit(code); alternateSellingUnits(code); priceUnit(code);$supercategories[vendor=$vendor][globalCatalogId='globalMarketplaceProductCatalog'][globalCatalogVersion='Online'][translator=de.hybris.platform.marketplaceservices.dataimport.batch.translator.MarketplaceCategoryTranslator];@thickness_otk[$feature];@width[$feature];@length[$feature];@lengthCoil[$feature];@weight_otk[$feature];@lengthCut_otk[$feature];@rangeOTK[$feature];@gradeEN[$feature];@finish[$feature];@gradeASTM[$feature];@standardsApproval[$feature];@surface[$feature];@otk_surfaceProtection[$feature];@interleavingPaper[$feature];@marking[$feature];@aproxnumberSheet[$feature];sequenceId[translator=de.hybris.platform.acceleratorservices.dataimport.batch.converter.SequenceIdTranslator];$catalogVersion;$approved;vendors(code)[default=$vendor];deliveryRegions(vendor(code),code)[default=$vendor:default];leadTime;baseProduct(code, $catalogVersion);";
	
	String impexVariantProductData = ";\"4404_Supra_T4.0_W1250-L2500_2B_321475\";\"otk_4404_Supra_T4.0_W1250-L2500_2B_321475\";\"Supra 316L/4404 - 2B sheet\";\"Supra 316L/4404 - 2B Blech\";\"<span style=\"\"color: #306084\"\">Supra 316L/4404</span> <br/> <span style=\"\"color: #306084\"\"> </span>\";\"<span style=\"\"color: #306084\"\">Supra 316L/4404</span> <br/> <span style=\"\"color: #306084\"\"> </span>\";\"<p>Exact delivery cost will be shown in checkout-process and depends on order quantity</p><p><strong>Outokumpu Supra Range</strong></p> <ul> Stainless steels for highly corrosive environments (PRE 22 to 27) Austenitics are highly formable and weldable and used in claddings, storage tanks, and heat exchangers. Includes the well-known Outokumpu Supra 316L/4404 as well as several alternatives. Ferritic stainless steels are nickel free, have good formability, and can be used in applications with elevated temperatures <iframe width=\"\"640\"\" height=\"\"360\"\" src=\"\"https://www.youtube.com/embed/1Zt7e8vYEZc\"\" frameborder=\"\"0\"\" allow=\"\"autoplay; encrypted-media\"\" allowfullscreen></iframe>\"\"\";\"<p>Genauer Lieferpreis wird im Checkout-Prozess angezeigt und h&auml;ngt von Bestellmenge ab</p> <p><strong>Outokumpu Supra Kategorie</strong></p> <ul> F&uuml;r stark korrosive Umgebungen (PRE 22 bis 27). Enth&auml;lt den bekannten Outokumpu Supra 316L/4404 sowie verschiedene Alternativen. Austenitische G&uuml;ten sind ausgezeichnet form- und schwei&szlig;bar und werden f&uuml;r Verkleidungen, Lagerbeh&auml;lter und W&auml;rmeaustauscher verwendet. Ferritischer Edelstahl ist nickelfrei, gut formbar und kann in Anwendungsbereichen mit h&ouml;heren Temperaturen verwendet werden. <iframe width=\"\"640\"\" height=\"\"360\"\" src=\"\"https://www.youtube.com/embed/1Zt7e8vYEZc\"\" frameborder=\"\"0\"\" allow=\"\"autoplay; encrypted-media\"\" allowfullscreen></iframe>\"\"\";\"<p><strong>(!) Maximum order quantity: 1 package</strong><br><br></p><p>Toll costs (6.50 &euro;/t) are included in product price</p>\";\"<p><strong>(!) Maximale Bestellmenge: 1 Paket</strong><br><br></p><p>Mautkosten (6,50 &euro;/t) sind im Produktpreis enthalten</p>\";\"PK\";;\"KGM\";\"otk_stockSheets\";\"4.0\";1250;2500;;1250;;\"rangeOTK_Supra\";\"gradeEN_1_4404_1_4401\";\"finish_2B\";\"gradeASTM_316,gradeASTM_316L\";\"standardsApproval_EN_10028_7,standardsApproval_EN_10088_2, standardsApproval_EN_10088_4\";;\"foiling_no\";\"interleavingPaper_yes\";\"marking_yes\";;;;\"approved\";;;2;\"otk_4404_Supra_T4.0_W1250-L2500_2B_with_interleaving_paper\"\n"
			+ ";\"4307_Core_T3.0_W1500-L3000_2B_321479\";\"otk_4307_Core_T3.0_W1500-L3000_2B_321479\";\"Core 304/4301 - 2B sheet\";\"Core 304/4301 - 2B Blech\";\"<span style=\"\"color: #009ee8\"\">Core 304/4307</span><br><span style=\"\"color: #009ee8\"\"> </span>\";\"<span style=\"\"color: #009ee8\"\">Core 304/4307</span><br><span style=\"\"color: #009ee8\"\"> </span>\";\"<p>Exact delivery cost will be shown in checkout-process and depends on order quantity</p><p><strong>Outokumpu Core range</strong></p>  <ul> <li>Stainless steels for corrosive environments (PRE 17 to 22)</li> <li>Austenitics are highly formable and weldable and are suitable for everything from cutlery to storage tanks</li> <li>Contains the all-purpose Core 304/4307 and Core 304L/4307, as well as several alternatives, including low-nickel and nickel-free products</li> <li>Ferritic stainless steels are nickel free, have good formability, and are used in architectural applications and appliances</li> </ul>  <iframe width=\"\"640\"\" height=\"\"360\"\" src=\"\"https://www.youtube.com/embed/X77-X6cYs1E\"\" frameborder=\"\"0\"\" allow=\"\"autoplay; encrypted-media\"\" allowfullscreen></iframe>\"\"\";\"<p>Genauer Lieferpreis wird im Checkout-Prozess angezeigt und h&auml;ngt von Bestellmenge ab</p> <p><strong>Outokumpu Core Kategorie</strong></p>  <ul> <li>F&uuml;r durchschnittlich korrosive Umgebungen (PRE 17 bis 22).</li> <li>Enth&auml;lt das renommierte Produkt Core 304L/4307 sowie Alternativen.</li> <li>Austenitische G&uuml;ten sind ausgezeichnet form- und schwei&szlig;bar und werden f&uuml;r Haushaltsger&auml;te, Rohre und Lagerbeh&auml;lter verwendet.</li> <li>Ferritischer Edelstahl ist nickelfrei, gut formbar und wird f&uuml;r Anwendungen in der Architektur sowie f&uuml;r Haushaltsger&auml;te verwendet. </li> </ul>  <iframe width=\"\"640\"\" height=\"\"360\"\" src=\"\"https://www.youtube.com/embed/X77-X6cYs1E\"\" frameborder=\"\"0\"\" allow=\"\"autoplay; encrypted-media\"\" allowfullscreen></iframe>\"\"\";\"<p><strong>(!) Maximum order quantity: 1 package</strong><br><br></p><p>Toll costs (6.50 &euro;/t) are included in product price</p>\";\"<p><strong>(!) Maximale Bestellmenge: 1 Paket</strong><br><br></p><p>Mautkosten (6,50 &euro;/t) sind im Produktpreis enthalten</p>\";\"PK\";;\"KGM\";\"otk_stockSheets\";\"3.0\";1500;3000;;1500;;\"rangeOTK_Core\";\"gradeEN_1_4301_1_4307\";\"finish_2B\";\"gradeASTM_304,gradeASTM_304L\";\"standardsApproval_EN_10028_7,standardsApproval_EN_10088_2, standardsApproval_EN_10088_4\";;\"foiling_no\";\"interleavingPaper_yes\";\"marking_yes\";;;;\"approved\";;;2;\"otk_4307_Core_T3.0_W1500-L3000_2B_with_interleaving_paper\"\n"
			+ ";\"4841_Therma_T4.0_W1000-L2000_1D_279557\";\"otk_4841_Therma_T4.0_W1000-L2000_1D_279557\";\"Therma 314/4841 - 1D sheet\";\"Therma 314/4841 - 1D Blech\";\"<span style=\"\"color: #e40134\"\"> Therma 314/4841 </span> <br/> <span style=\"\"color: #e40134\"\"> </span>\";\"<span style=\"\"color: #e40134\"\"> Therma 314/4841 </span> <br/> <span style=\"\"color: #e40134\"\"> </span>\";\"<p>Exact delivery cost will be shown in checkout-process and depends on order quantity</p><p><strong>Outokumpu Therma range</strong></p> <strong><span style=\"\"color: #e40134\"\"> Therma </span></strong> <br> An austenitic stainless steel with excellent oxidation and creep resistance in cyclic conditions that is best employed in temperatures up to 1150°C/2100°F. There is a slight susceptibility to embrittlement during continuous operation between 600–850°C/1110–1560°F.<br> <p><strong>Typical applications:</strong></p> <li>Furnace equipment (especially supporting parts)</li> <li>Annealing and hardening boxes</li> <li>Air heaters</li> <li>Exhaust systems</li> <li>Automotive components such as turbochargers</li> <li>Valves and flanges</li> </ul> <p><iframe allow=\"\"autoplay; encrypted-media\"\" allowfullscreen=\"\"\"\" frameborder=\"\"0\"\" height=\"\"360\"\" src=\"\"https://www.youtube.com/embed/_A8ui76XHjo\"\" width=\"\"640\"\"></iframe></p>\";\"<p>Genauer Lieferpreis wird im Checkout-Prozess angezeigt und h&auml;ngt von Bestellmenge ab</p> <p><strong>Outokumpu Therma Kategorie</strong></p> <strong><span style=\"\"color: #e40134\"\"> Therma </span></strong> <br> Austenitischer Edelstahl mit ausgezeichneter Oxidations- und Kriechbest&auml;ndigkeit unter zyklischen Bedingungen, der sich bestens f&uuml;r Temperaturen von bis zu 1150 °C eignet. Bei einem kontinuierlichen Einsatz zwischen 600 und 850 °C besteht eine leichte Anf&auml;lligkeit f&uuml;r Verspr&ouml;dung.<br> <p><strong>Typische Anwendungsbereiche:</strong></p> <li>Ofenanlagen (insbesondere tragende Teile)</li> <li>Gl&uuml;hk&auml;sten and H&auml;rtek&auml;sten</li> <li>Lufterhitzer</li> <li>Abgassysteme</li> <li>Automobil-Zubeh&ouml;rteile wie Turbolader</li> <li>Flansche und Ventile</li> </ul> <p><iframe allow=\"\"autoplay; encrypted-media\"\" allowfullscreen=\"\"\"\" frameborder=\"\"0\"\" height=\"\"360\"\" src=\"\"https://www.youtube.com/embed/_A8ui76XHjo\"\" width=\"\"640\"\"></iframe></p>\";\"<p><strong>(!) Maximum order quantity: 1 package</strong><br><br></p><p>Toll costs (6.50 &euro;/t) are included in product price</p>\";\"<p><strong>(!) Maximale Bestellmenge: 1 Paket</strong><br><br></p><p>Mautkosten (6,50 &euro;/t) sind im Produktpreis enthalten</p>\";\"PK\";;\"KGM\";\"otk_stockSheets\";\"4.0\";1000;2000;;1000;;\"rangeOTK_Therma\";\"gradeEN_1_4841\";\"finish_1D\";\"gradeASTM_314\";\"standardsApproval_EN_10095\";;\"foiling_no\";\"interleavingPaper_no\";\"marking_yes\";;;;\"unapproved\";;;2;\"otk_4841_Therma_T4.0_W1000-L2000_1D\"\n"
			+ ";\"4307_Core_T1.5_W1500-L3000_2B_321646\";\"otk_4307_Core_T1.5_W1500-L3000_2B_321646\";\"Core 304/4301 - 2B sheet\";\"Core 304/4301 - 2B Blech\";\"<span style=\"\"color: #009ee8\"\">Core 304/4307</span><br><span style=\"\"color: #009ee8\"\"> </span>\";\"<span style=\"\"color: #009ee8\"\">Core 304/4307</span><br><span style=\"\"color: #009ee8\"\"> </span>\";\"<p>Exact delivery cost will be shown in checkout-process and depends on order quantity</p><p><strong>Outokumpu Core range</strong></p>  <ul> <li>Stainless steels for corrosive environments (PRE 17 to 22)</li> <li>Austenitics are highly formable and weldable and are suitable for everything from cutlery to storage tanks</li> <li>Contains the all-purpose Core 304/4307 and Core 304L/4307, as well as several alternatives, including low-nickel and nickel-free products</li> <li>Ferritic stainless steels are nickel free, have good formability, and are used in architectural applications and appliances</li> </ul>  <iframe width=\"\"640\"\" height=\"\"360\"\" src=\"\"https://www.youtube.com/embed/X77-X6cYs1E\"\" frameborder=\"\"0\"\" allow=\"\"autoplay; encrypted-media\"\" allowfullscreen></iframe>\"\"\";\"<p>Genauer Lieferpreis wird im Checkout-Prozess angezeigt und h&auml;ngt von Bestellmenge ab</p> <p><strong>Outokumpu Core Kategorie</strong></p>  <ul> <li>F&uuml;r durchschnittlich korrosive Umgebungen (PRE 17 bis 22).</li> <li>Enth&auml;lt das renommierte Produkt Core 304L/4307 sowie Alternativen.</li> <li>Austenitische G&uuml;ten sind ausgezeichnet form- und schwei&szlig;bar und werden f&uuml;r Haushaltsger&auml;te, Rohre und Lagerbeh&auml;lter verwendet.</li> <li>Ferritischer Edelstahl ist nickelfrei, gut formbar und wird f&uuml;r Anwendungen in der Architektur sowie f&uuml;r Haushaltsger&auml;te verwendet. </li> </ul>  <iframe width=\"\"640\"\" height=\"\"360\"\" src=\"\"https://www.youtube.com/embed/X77-X6cYs1E\"\" frameborder=\"\"0\"\" allow=\"\"autoplay; encrypted-media\"\" allowfullscreen></iframe>\"\"\";\"<p><strong>(!) Maximum order quantity: 1 package</strong><br><br></p><p>Toll costs (6.50 &euro;/t) are included in product price</p>\";\"<p><strong>(!) Maximale Bestellmenge: 1 Paket</strong><br><br></p><p>Mautkosten (6,50 &euro;/t) sind im Produktpreis enthalten</p>\";\"PK\";;\"KGM\";\"otk_stockSheets\";\"1.5\";1500;3000;;1500;;\"rangeOTK_Core\";\"gradeEN_1_4301_1_4307\";\"finish_2B\";\"gradeASTM_304,gradeASTM_304L\";\"standardsApproval_EN_10028_7,standardsApproval_EN_10088_2, standardsApproval_EN_10088_4\";;\"foiling_no\";\"interleavingPaper_yes\";\"marking_yes\";;;;\"approved\";;;2;\"otk_4307_Core_T1.5_W1500-L3000_2B_with_interleaving_paper\"\n"
			+ ";\"4307_Core_T2.5_W2000-L4000_2B_319386\";\"otk_4307_Core_T2.5_W2000-L4000_2B_319386\";\"Core 304/4301 - 2B sheet\";\"Core 304/4301 - 2B Blech\";\"<span style=\"\"color: #009ee8\"\">Core 304/4307</span><br><span style=\"\"color: #009ee8\"\"> </span>\";\"<span style=\"\"color: #009ee8\"\">Core 304/4307</span><br><span style=\"\"color: #009ee8\"\"> </span>\";\"<p>Exact delivery cost will be shown in checkout-process and depends on order quantity</p><p><strong>Outokumpu Core range</strong></p>  <ul> <li>Stainless steels for corrosive environments (PRE 17 to 22)</li> <li>Austenitics are highly formable and weldable and are suitable for everything from cutlery to storage tanks</li> <li>Contains the all-purpose Core 304/4307 and Core 304L/4307, as well as several alternatives, including low-nickel and nickel-free products</li> <li>Ferritic stainless steels are nickel free, have good formability, and are used in architectural applications and appliances</li> </ul>  <iframe width=\"\"640\"\" height=\"\"360\"\" src=\"\"https://www.youtube.com/embed/X77-X6cYs1E\"\" frameborder=\"\"0\"\" allow=\"\"autoplay; encrypted-media\"\" allowfullscreen></iframe>\"\"\";\"<p>Genauer Lieferpreis wird im Checkout-Prozess angezeigt und h&auml;ngt von Bestellmenge ab</p> <p><strong>Outokumpu Core Kategorie</strong></p>  <ul> <li>F&uuml;r durchschnittlich korrosive Umgebungen (PRE 17 bis 22).</li> <li>Enth&auml;lt das renommierte Produkt Core 304L/4307 sowie Alternativen.</li> <li>Austenitische G&uuml;ten sind ausgezeichnet form- und schwei&szlig;bar und werden f&uuml;r Haushaltsger&auml;te, Rohre und Lagerbeh&auml;lter verwendet.</li> <li>Ferritischer Edelstahl ist nickelfrei, gut formbar und wird f&uuml;r Anwendungen in der Architektur sowie f&uuml;r Haushaltsger&auml;te verwendet. </li> </ul>  <iframe width=\"\"640\"\" height=\"\"360\"\" src=\"\"https://www.youtube.com/embed/X77-X6cYs1E\"\" frameborder=\"\"0\"\" allow=\"\"autoplay; encrypted-media\"\" allowfullscreen></iframe>\"\"\";\"<p><strong>(!) Maximum order quantity: 1 package</strong><br><br></p><p>Toll costs (6.50 &euro;/t) are included in product price</p>\";\"<p><strong>(!) Maximale Bestellmenge: 1 Paket</strong><br><br></p><p>Mautkosten (6,50 &euro;/t) sind im Produktpreis enthalten</p>\";\"PK\";;\"KGM\";\"otk_stockSheets\";\"2.5\";2000;4000;;2000;;\"rangeOTK_Core\";\"gradeEN_1_4301_1_4307\";\"finish_2B\";\"gradeASTM_304,gradeASTM_304L\";\"standardsApproval_EN_10028_7,standardsApproval_EN_10088_2, standardsApproval_EN_10088_4\";;\"foiling_no\";\"interleavingPaper_yes\";\"marking_yes\";;;;\"unapproved\";;;2;\"otk_4307_Core_T2.5_W2000-L4000_2B_with_interleaving_paper\"\n"
			+ ";\"4307_Core_T1.0_W1000-L2000_2B_321638\";\"otk_4307_Core_T1.0_W1000-L2000_2B_321638\";\"Core 304/4301 - 2B sheet\";\"Core 304/4301 - 2B Blech\";\"<span style=\"\"color: #009ee8\"\">Core 304/4307</span><br><span style=\"\"color: #009ee8\"\"> </span>\";\"<span style=\"\"color: #009ee8\"\">Core 304/4307</span><br><span style=\"\"color: #009ee8\"\"> </span>\";\"<p>Exact delivery cost will be shown in checkout-process and depends on order quantity</p><p><strong>Outokumpu Core range</strong></p>  <ul> <li>Stainless steels for corrosive environments (PRE 17 to 22)</li> <li>Austenitics are highly formable and weldable and are suitable for everything from cutlery to storage tanks</li> <li>Contains the all-purpose Core 304/4307 and Core 304L/4307, as well as several alternatives, including low-nickel and nickel-free products</li> <li>Ferritic stainless steels are nickel free, have good formability, and are used in architectural applications and appliances</li> </ul>  <iframe width=\"\"640\"\" height=\"\"360\"\" src=\"\"https://www.youtube.com/embed/X77-X6cYs1E\"\" frameborder=\"\"0\"\" allow=\"\"autoplay; encrypted-media\"\" allowfullscreen></iframe>\"\"\";\"<p>Genauer Lieferpreis wird im Checkout-Prozess angezeigt und h&auml;ngt von Bestellmenge ab</p> <p><strong>Outokumpu Core Kategorie</strong></p>  <ul> <li>F&uuml;r durchschnittlich korrosive Umgebungen (PRE 17 bis 22).</li> <li>Enth&auml;lt das renommierte Produkt Core 304L/4307 sowie Alternativen.</li> <li>Austenitische G&uuml;ten sind ausgezeichnet form- und schwei&szlig;bar und werden f&uuml;r Haushaltsger&auml;te, Rohre und Lagerbeh&auml;lter verwendet.</li> <li>Ferritischer Edelstahl ist nickelfrei, gut formbar und wird f&uuml;r Anwendungen in der Architektur sowie f&uuml;r Haushaltsger&auml;te verwendet. </li> </ul>  <iframe width=\"\"640\"\" height=\"\"360\"\" src=\"\"https://www.youtube.com/embed/X77-X6cYs1E\"\" frameborder=\"\"0\"\" allow=\"\"autoplay; encrypted-media\"\" allowfullscreen></iframe>\"\"\";\"<p><strong>(!) Maximum order quantity: 1 package</strong><br><br></p><p>Toll costs (6.50 &euro;/t) are included in product price</p>\";\"<p><strong>(!) Maximale Bestellmenge: 1 Paket</strong><br><br></p><p>Mautkosten (6,50 &euro;/t) sind im Produktpreis enthalten</p>\";\"PK\";;\"KGM\";\"otk_stockSheets\";\"1.0\";1000;2000;;1000;;\"rangeOTK_Core\";\"gradeEN_1_4301_1_4307\";\"finish_2B\";\"gradeASTM_304,gradeASTM_304L\";\"standardsApproval_EN_10028_7,standardsApproval_EN_10088_2, standardsApproval_EN_10088_4\";;\"foiling_no\";\"interleavingPaper_yes\";\"marking_yes\";;;;\"approved\";;;2;\"otk_4307_Core_T1.0_W1000-L2000_2B_with_interleaving_paper\"";
	
	@Test
	public void testImportProducts() throws Exception {
		String[] lines = impexVariantProductData.split("\n");
		System.out.println(lines.length + " records will be send");
		String endpoint = context.getProperty("hac_endpoint");
		String user = context.getProperty("hac_login");
		String pw = context.getProperty("hac_password");
		String htuser = context.getProperty("hac_htaccess_login");
		String htpw = context.getProperty("hac_htaccess_password");
		ImpexImportRunner r = new ImpexImportRunner();
		r.connect(endpoint, user, pw, htuser, htpw);
		r.setImpexHeader(impexVariantProductHeader);
		for (String row : lines) {
			r.addDataRow(row);
			r.setCorrelationKey("key", row.hashCode());
		}
		System.out.println("Send request...");
		r.execute();
		System.out.println("Has errors: " + r.hasErrors());
		r.matchImportErrors();
		List<ImpexImportError> errors = r.getImportErrors();
		for (ImpexImportError e : errors) {
			System.out.println(e);
		}
	}
	
}

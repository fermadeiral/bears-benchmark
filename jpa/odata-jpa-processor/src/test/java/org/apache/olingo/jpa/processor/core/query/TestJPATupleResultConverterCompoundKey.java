package org.apache.olingo.jpa.processor.core.query;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.processor.core.testmodel.AdministrativeDivisionDescriptionKey;
import org.apache.olingo.jpa.processor.core.util.ServiceMetadataDouble;
import org.apache.olingo.jpa.processor.core.util.TestBase;
import org.apache.olingo.jpa.processor.core.util.TestHelper;
import org.apache.olingo.jpa.processor.core.util.TupleDouble;
import org.apache.olingo.jpa.processor.core.util.UriHelperDouble;
import org.apache.olingo.server.api.ODataApplicationException;
import org.junit.Before;
import org.junit.Test;

public class TestJPATupleResultConverterCompoundKey extends TestBase {
	public static final int NO_POSTAL_ADDRESS_FIELDS = 8;
	public static final int NO_ADMIN_INFO_FIELDS = 2;
	private JPATupleResultConverter cut;
	private List<Tuple> jpaQueryResult;
	private UriHelperDouble uriHelper;
	private Map<String, String> keyPredicates;

	@Before
	public void setup() throws ODataException {
		helper = new TestHelper(persistenceAdapter.getMetamodel(), PUNIT_NAME);
		jpaQueryResult = new ArrayList<Tuple>();
		uriHelper = new UriHelperDouble();
		keyPredicates = new HashMap<String, String>();
	}

	@Test
	public void checkConvertsOneResultsTwoKeys() throws ODataApplicationException, ODataJPAModelException {
		// .../BusinessPartnerRoles(BusinessPartnerID='3',RoleCategory='C')

		final HashMap<String, List<Tuple>> resultContainer = new HashMap<String, List<Tuple>>(1);
		resultContainer.put("root", jpaQueryResult);

		cut = new JPATupleResultConverter(
				helper.sd,
				new JPAQueryResult(resultContainer, Long.parseLong("0"),
						helper.getJPAEntityType("BusinessPartnerRoles")),
				uriHelper,
				new ServiceMetadataDouble(nameBuilder, "BusinessPartnerRole"));

		HashMap<String, Object> result;

		result = new HashMap<String, Object>();
		result.put("BusinessPartnerID", new String("3"));
		result.put("RoleCategory", new String("C"));
		jpaQueryResult.add(new TupleDouble(result));

		uriHelper.setKeyPredicates(keyPredicates, "BusinessPartnerID");
		keyPredicates.put("3", "BusinessPartnerID='3',RoleCategory='C'");

		final EntityCollection act = cut.convertQueryResult();
		assertEquals(1, act.getEntities().size());
		assertEquals("3", act.getEntities().get(0).getProperty("BusinessPartnerID").getValue().toString());
		assertEquals("C", act.getEntities().get(0).getProperty("RoleCategory").getValue().toString());

		assertEquals("BusinessPartnerRoles(BusinessPartnerID='3',RoleCategory='C')",
				act.getEntities().get(0).getId().getPath());
	}

	@Test // EmbeddedIds are resolved to elementary key properties
	public void checkConvertsOneResultsEmbeddedKey() throws ODataApplicationException, ODataJPAModelException {
		// .../AdministrativeDivisionDescriptions(CodePublisher='ISO', CodeID='3166-1', DivisionCode='DEU',Language='en')

		final HashMap<String, List<Tuple>> resultContainer = new HashMap<String, List<Tuple>>(1);
		resultContainer.put("root", jpaQueryResult);

		cut = new JPATupleResultConverter(
				helper.sd,
				new JPAQueryResult(resultContainer, Long.parseLong("1"),
						helper.getJPAEntityType(
								"AdministrativeDivisionDescriptions")),
				uriHelper,
				new ServiceMetadataDouble());

		final AdministrativeDivisionDescriptionKey country = new AdministrativeDivisionDescriptionKey();
		country.setLanguage("en");

		HashMap<String, Object> result;

		result = new HashMap<String, Object>();
		result.put("CodePublisher", new String("ISO"));
		result.put("CodeID", new String("3166-1"));
		result.put("DivisionCode", new String("DEU"));
		result.put("Language", new String("en"));
		jpaQueryResult.add(new TupleDouble(result));
		uriHelper.setKeyPredicates(keyPredicates, "DivisionCode");
		keyPredicates.put("DEU", "CodePublisher='ISO',CodeID='3166-1',DivisionCode='DEU',Language='en'");

		final EntityCollection act = cut.convertQueryResult();
		assertEquals(1, act.getEntities().size());
		assertEquals("ISO", act.getEntities().get(0).getProperty("CodePublisher").getValue().toString());
		assertEquals("en", act.getEntities().get(0).getProperty("Language").getValue().toString());

		assertEquals(
				"AdministrativeDivisionDescriptions(CodePublisher='ISO',CodeID='3166-1',DivisionCode='DEU',Language='en')",
				act.getEntities().get(0).getId().getPath());
	}

}

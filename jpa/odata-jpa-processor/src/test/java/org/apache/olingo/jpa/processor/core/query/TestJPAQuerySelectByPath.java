package org.apache.olingo.jpa.processor.core.query;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.jpa.processor.core.testmodel.ImageLoader;
import org.apache.olingo.jpa.processor.core.util.IntegrationTestHelper;
import org.apache.olingo.jpa.processor.core.util.TestBase;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TestJPAQuerySelectByPath extends TestBase {

	@Test
	public void testNavigationToOwnPrimitiveProperty() throws IOException, ODataException {

		final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('3')/Name1");
		helper.assertStatus(200);

		final ObjectNode org = helper.getValue();
		assertEquals("Third Org.", org.get("value").asText());
	}

	@Ignore
	@Test
	public void testNavigationToOwnPrimitiveDescriptionProperty() throws IOException, ODataException {

		final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('3')/LocationName");
		helper.assertStatus(200);

		final ObjectNode org = helper.getValue();
		assertEquals("Vereinigte Staaten von Amerika", org.get("value").asText());
	}

	@Test
	public void testNavigationToComplexProperty() throws IOException, ODataException {

		final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('4')/Address");
		helper.assertStatus(200);

		final ObjectNode org = helper.getValue();
		assertEquals("USA", org.get("Country").asText());
	}

	@Test
	public void testNavigationToNestedComplexProperty() throws IOException, ODataException {

		final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
				"Organizations('4')/AdministrativeInformation/Created");
		helper.assertStatus(200);

		final ObjectNode org = helper.getValue();
		final JsonNode created = org.get("Created");
		assertEquals("98", created.get("By").asText());
	}

	@Ignore
	@Test
	public void testNavigationViaComplexAndNaviPropertyToPrimitive() throws IOException, ODataException {

		final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
				"Organizations('3')/AdministrativeInformation/Created/User/FirstName");
		helper.assertStatus(200);

		final ObjectNode org = helper.getValue();
		assertEquals("Max", org.get("value").asText());
	}

	@Test
	public void testNavigationToComplexPropertySelect() throws IOException, ODataException {

		final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
				"Organizations('4')/Address?$select=Country,Region");
		helper.assertStatus(200);

		final ObjectNode org = helper.getValue();
		assertEquals(3, org.size()); // Node "@odata.context" is also counted
		assertEquals("USA", org.get("Country").asText());
		assertEquals("US-UT", org.get("Region").asText());
	}

	@Test
	public void testNavigationToComplexPropertyExpand() throws IOException, ODataException {

		final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('4')/Address");
		helper.assertStatus(200);

		final ObjectNode org = helper.getValue();
		assertEquals("USA", org.get("Country").asText());
	}

	@Test
	public void testNavigationToComplexPrimitiveProperty() throws IOException, ODataException {

		final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations('4')/Address/Region");
		helper.assertStatus(200);

		final ObjectNode org = helper.getValue();
		assertEquals("US-UT", org.get("value").asText());
		assertEquals("$metadata#Organizations/Address/Region", org.get("@odata.context").asText());
	}

	@Ignore
	@Test
	public void testNavigationToStreamValue() throws IOException, ODataException {
		new ImageLoader().loadPerson(emf.createEntityManager(), "OlingoOrangeTM.png", "99");

		final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "PersonImages('99')/$value");
		helper.assertStatus(200);

		final byte[] act = helper.getBinaryResult();
		assertEquals(93316, act.length, 0);
	}

	@Ignore
	@Test
	public void testNavigationToStreamValueVia() throws IOException, ODataException {
		new ImageLoader().loadPerson(emf.createEntityManager(), "OlingoOrangeTM.png", "99");

		final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Persons('99')/Image/$value");
		helper.assertStatus(200);

		final byte[] act = helper.getBinaryResult();
		assertEquals(93316, act.length, 0);
	}

	@Ignore
	@Test
	public void testNavigationToComplexAttributeValue() throws IOException, ODataException {
		new ImageLoader().loadPerson(emf.createEntityManager(), "OlingoOrangeTM.png", "99");

		final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
				"Organizations('4')/AdministrativeInformation/Created/By/$value");
		helper.assertStatus(200);

		final String act = helper.getRawResult();
		assertEquals("98", act);
	}

	@Ignore
	@Test
	public void testNavigationToPrimitiveAttributeValue() throws IOException, ODataException {
		new ImageLoader().loadPerson(emf.createEntityManager(), "OlingoOrangeTM.png", "99");

		final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
				"Organizations('4')/ID/$value");
		helper.assertStatus(200);

		final String act = helper.getRawResult();
		assertEquals("4", act);
	}
}

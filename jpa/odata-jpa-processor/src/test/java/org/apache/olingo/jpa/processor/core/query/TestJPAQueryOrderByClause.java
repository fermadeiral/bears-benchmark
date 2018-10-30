package org.apache.olingo.jpa.processor.core.query;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.jpa.processor.core.util.IntegrationTestHelper;
import org.apache.olingo.jpa.processor.core.util.TestBase;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;

public class TestJPAQueryOrderByClause extends TestBase {

	@Test
	public void testOrderByOneProperty() throws IOException, ODataException {

		final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations?$orderby=Name1");
		helper.assertStatus(200);

		final ArrayNode orgs = helper.getValues();
		assertEquals("Eighth Org.", orgs.get(0).get("Name1").asText());
		assertEquals("Third Org.", orgs.get(9).get("Name1").asText());
	}

	@Test
	public void testOrderByOneComplexPropertyAsc() throws IOException, ODataException {

		final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations?$orderby=Address/Region");
		helper.assertStatus(200);

		final ArrayNode orgs = helper.getValues();
		assertEquals("US-CA", orgs.get(0).get("Address").get("Region").asText());
		assertEquals("US-UT", orgs.get(9).get("Address").get("Region").asText());
	}

	@Test
	public void testOrderByOneComplexPropertyDesc() throws IOException, ODataException {

		final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations?$orderby=Address/Region desc");
		if (helper.getStatus() != 200) {
			System.out.println(helper.getRawResult());
		}
		helper.assertStatus(200);

		final ArrayNode orgs = helper.getValues();
		assertEquals("US-UT", orgs.get(0).get("Address").get("Region").asText());
		assertEquals("US-CA", orgs.get(9).get("Address").get("Region").asText());
	}

	@Test
	public void testOrderByTwoPropertiesDescAsc() throws IOException, ODataException {

		final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
				"Organizations?$orderby=Address/Region desc,Name1 asc");
		helper.assertStatus(200);

		final ArrayNode orgs = helper.getValues();
		assertEquals("US-UT", orgs.get(0).get("Address").get("Region").asText());
		assertEquals("US-CA", orgs.get(9).get("Address").get("Region").asText());
		assertEquals("Third Org.", orgs.get(9).get("Name1").asText());
	}

	@Test
	public void testOrderByTwoPropertiesDescDesc() throws IOException, ODataException {

		final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
				"Organizations?$orderby=Address/Region desc,Name1 desc");
		helper.assertStatus(200);

		final ArrayNode orgs = helper.getValues();
		assertEquals("US-UT", orgs.get(0).get("Address").get("Region").asText());
		assertEquals("US-CA", orgs.get(9).get("Address").get("Region").asText());
		assertEquals("First Org.", orgs.get(9).get("Name1").asText());
	}

	@Ignore("TODO")
	@Test
	public void testOrderBy$CountDesc() throws IOException, ODataException {

		final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations?$orderby=Roles/$count desc");
		helper.assertStatus(200);

		final ArrayNode orgs = helper.getValues();
		assertEquals("3", orgs.get(0).get("ID").asText());
		assertEquals("2", orgs.get(1).get("ID").asText());
	}

	@Test
	public void testOrderBy$CountAndSelectAsc() throws IOException, ODataException {

		final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
				"Organizations?$select=ID,Name1,Name2,Address/Country&$orderby=Roles/$count asc");
		helper.assertStatus(200);

		final ArrayNode orgs = helper.getValues();
		assertEquals("3", orgs.get(9).get("ID").asText());
		assertEquals("2", orgs.get(8).get("ID").asText());
	}

	@Ignore("TODO")
	@Test
	public void testOrderBy$CountAsc() throws IOException, ODataException {

		final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
				"Organizations?$orderby=Roles/$count asc");
		helper.assertStatus(200);

		final ArrayNode orgs = helper.getValues();
		assertEquals("3", orgs.get(9).get("ID").asText());
		assertEquals("2", orgs.get(8).get("ID").asText());
	}

	@Ignore("TODO")
	@Test
	public void testOrderBy$CountDescComplexPropertyAcs() throws IOException, ODataException {

		final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
				"Organizations?$orderby=Roles/$count desc,Address/Region desc");
		helper.assertStatus(200);

		final ArrayNode orgs = helper.getValues();
		assertEquals("3", orgs.get(0).get("ID").asText());
		assertEquals("2", orgs.get(1).get("ID").asText());
		assertEquals("US-CA", orgs.get(9).get("Address").get("Region").asText());
		assertEquals("6", orgs.get(9).get("ID").asText());
	}

	@Test
	public void testOrderByAndFilter() throws IOException, ODataException {

		final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
				"AdministrativeDivisions?$filter=CodeID eq 'NUTS' or CodeID eq '3166-1'&$orderby=CountryCode desc");

		helper.assertStatus(200);

		final ArrayNode orgs = helper.getValues();
		assertEquals(4, orgs.size());
		assertEquals("USA", orgs.get(0).get("CountryCode").asText());
	}
}

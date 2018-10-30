package org.apache.olingo.jpa.processor.core.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.jpa.metadata.api.JPAEdmProvider;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.processor.core.api.JPAODataContextAccessDouble;
import org.apache.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import org.apache.olingo.jpa.processor.core.util.EdmEntityTypeDouble;
import org.apache.olingo.jpa.processor.core.util.ExpandItemDouble;
import org.apache.olingo.jpa.processor.core.util.TestBase;
import org.apache.olingo.jpa.processor.core.util.TestHelper;
import org.apache.olingo.jpa.processor.core.util.TupleDouble;
import org.apache.olingo.server.api.ODataApplicationException;
import org.junit.Before;
import org.junit.Test;

public class TestJPAExpandQueryCreateResult extends TestBase {
	private JPAExpandQuery cut;

	@Before
	public void setup() throws ODataException {
		helper = new TestHelper(persistenceAdapter.getMetamodel(), PUNIT_NAME);
		createHeaders();
		final EdmEntityType targetEntity = new EdmEntityTypeDouble(nameBuilder, "BusinessPartnerRole");
		final JPAODataSessionContextAccess context = new JPAODataContextAccessDouble(
				new JPAEdmProvider(PUNIT_NAME, persistenceAdapter.getMetamodel()), persistenceAdapter);
		cut = new JPAExpandQuery(
				null, context, persistenceAdapter.createEntityManager(),
				new ExpandItemDouble(targetEntity).getResourcePath(),
				helper.getJPAAssociationPath("Organizations", "Roles"), helper.sd.getEntityType(targetEntity),
				new HashMap<String, List<String>>());
		// new EdmEntitySetDouble(nameBuilder, "Organisations"), null, new HashMap<String, List<String>>());
	}

	@Test
	public void checkConvertOneResult() throws ODataJPAModelException, ODataApplicationException {
		final JPAAssociationPath exp = helper.getJPAAssociationPath("Organizations", "Roles");
		final List<Tuple> result = new ArrayList<Tuple>();
		final HashMap<String, Object> oneResult = new HashMap<String, Object>();
		oneResult.put("BusinessPartnerID", "1");
		oneResult.put("RoleCategory", "A");
		final Tuple t = new TupleDouble(oneResult);
		result.add(t);

		final Map<String, List<Tuple>> act = cut.convertResult(result, exp, 0, Long.MAX_VALUE);

		assertNotNull(act.get("1"));
		assertEquals(1, act.get("1").size());
		assertEquals("1", act.get("1").get(0).get("BusinessPartnerID"));
	}

	@Test
	public void checkConvertTwoResultOneParent() throws ODataJPAModelException, ODataApplicationException {
		final JPAAssociationPath exp = helper.getJPAAssociationPath("Organizations", "Roles");
		final List<Tuple> result = new ArrayList<Tuple>();
		HashMap<String, Object> oneResult;
		Tuple t;

		oneResult = new HashMap<String, Object>();
		oneResult.put("BusinessPartnerID", "2");
		oneResult.put("RoleCategory", "A");
		t = new TupleDouble(oneResult);
		result.add(t);
		oneResult = new HashMap<String, Object>();
		oneResult.put("BusinessPartnerID", "2");
		oneResult.put("RoleCategory", "C");
		t = new TupleDouble(oneResult);
		result.add(t);

		final Map<String, List<Tuple>> act = cut.convertResult(result, exp, 0, Long.MAX_VALUE);

		assertEquals(1, act.size());
		assertNotNull(act.get("2"));
		assertEquals(2, act.get("2").size());
		assertEquals("2", act.get("2").get(0).get("BusinessPartnerID"));
	}

	@Test
	public void checkConvertTwoResultOneParentTop1() throws ODataJPAModelException, ODataApplicationException {
		final JPAAssociationPath exp = helper.getJPAAssociationPath("Organizations", "Roles");
		final List<Tuple> result = new ArrayList<Tuple>();
		HashMap<String, Object> oneResult;
		Tuple t;

		oneResult = new HashMap<String, Object>();
		oneResult.put("BusinessPartnerID", "2");
		oneResult.put("RoleCategory", "A");
		t = new TupleDouble(oneResult);
		result.add(t);
		oneResult = new HashMap<String, Object>();
		oneResult.put("BusinessPartnerID", "2");
		oneResult.put("RoleCategory", "C");
		t = new TupleDouble(oneResult);
		result.add(t);

		final Map<String, List<Tuple>> act = cut.convertResult(result, exp, 0, 1);

		assertEquals(1, act.size());
		assertNotNull(act.get("2"));
		assertEquals(1, act.get("2").size());
		assertEquals("A", act.get("2").get(0).get("RoleCategory"));
	}

	@Test
	public void checkConvertTwoResultOneParentSkip1() throws ODataJPAModelException, ODataApplicationException {
		final JPAAssociationPath exp = helper.getJPAAssociationPath("Organizations", "Roles");
		final List<Tuple> result = new ArrayList<Tuple>();
		HashMap<String, Object> oneResult;
		Tuple t;

		oneResult = new HashMap<String, Object>();
		oneResult.put("BusinessPartnerID", "2");
		oneResult.put("RoleCategory", "A");
		t = new TupleDouble(oneResult);
		result.add(t);
		oneResult = new HashMap<String, Object>();
		oneResult.put("BusinessPartnerID", "2");
		oneResult.put("RoleCategory", "C");
		t = new TupleDouble(oneResult);
		result.add(t);

		final Map<String, List<Tuple>> act = cut.convertResult(result, exp, 1, 1000);

		assertEquals(1, act.size());
		assertNotNull(act.get("2"));
		assertEquals(1, act.get("2").size());
		assertEquals("C", act.get("2").get(0).get("RoleCategory"));
	}

	@Test
	public void checkConvertTwoResultTwoParent() throws ODataJPAModelException, ODataApplicationException {
		final JPAAssociationPath exp = helper.getJPAAssociationPath("Organizations", "Roles");
		final List<Tuple> result = new ArrayList<Tuple>();
		HashMap<String, Object> oneResult;
		Tuple t;

		oneResult = new HashMap<String, Object>();
		oneResult.put("BusinessPartnerID", "1");
		oneResult.put("RoleCategory", "A");
		t = new TupleDouble(oneResult);
		result.add(t);
		oneResult = new HashMap<String, Object>();
		oneResult.put("BusinessPartnerID", "2");
		oneResult.put("RoleCategory", "C");
		t = new TupleDouble(oneResult);
		result.add(t);

		final Map<String, List<Tuple>> act = cut.convertResult(result, exp, 0, Long.MAX_VALUE);

		assertEquals(2, act.size());
		assertNotNull(act.get("1"));
		assertNotNull(act.get("2"));
		assertEquals(1, act.get("2").size());
		assertEquals("C", act.get("2").get(0).get("RoleCategory"));
	}

	@Test
	public void checkConvertOneResultCompundKey() throws ODataJPAModelException, ODataApplicationException {
		final JPAAssociationPath exp = helper.getJPAAssociationPath("AdministrativeDivisions", "Parent");
		final List<Tuple> result = new ArrayList<Tuple>();
		final HashMap<String, Object> oneResult = new HashMap<String, Object>();
		oneResult.put("CodePublisher", "NUTS");
		oneResult.put("DivisionCode", "BE25");
		oneResult.put("CodeID", "2");
		oneResult.put("ParentCodeID", "1");
		oneResult.put("ParentDivisionCode", "BE2");
		final Tuple t = new TupleDouble(oneResult);
		result.add(t);

		final Map<String, List<Tuple>> act = cut.convertResult(result, exp, 0, Long.MAX_VALUE);

		assertNotNull(act.get("NUTS/2/BE25"));
		assertEquals(1, act.get("NUTS/2/BE25").size());
		assertEquals("BE2", act.get("NUTS/2/BE25").get(0).get("ParentDivisionCode"));
	}

	@Test
	public void checkConvertTwoResultsCompundKey() throws ODataJPAModelException, ODataApplicationException {
		final JPAAssociationPath exp = helper.getJPAAssociationPath("AdministrativeDivisions", "Parent");
		final List<Tuple> result = new ArrayList<Tuple>();
		HashMap<String, Object> oneResult;
		Tuple t;

		oneResult = new HashMap<String, Object>();
		oneResult.put("CodePublisher", "NUTS");
		oneResult.put("DivisionCode", "BE25");
		oneResult.put("CodeID", "2");
		oneResult.put("ParentCodeID", "1");
		oneResult.put("ParentDivisionCode", "BE2");
		t = new TupleDouble(oneResult);
		result.add(t);

		oneResult = new HashMap<String, Object>();
		oneResult.put("CodePublisher", "NUTS");
		oneResult.put("DivisionCode", "BE10");
		oneResult.put("CodeID", "2");
		oneResult.put("ParentCodeID", "1");
		oneResult.put("ParentDivisionCode", "BE1");
		t = new TupleDouble(oneResult);
		result.add(t);

		final Map<String, List<Tuple>> act = cut.convertResult(result, exp, 0, Long.MAX_VALUE);

		assertEquals(2, act.size());
		assertNotNull(act.get("NUTS/2/BE25"));
		assertEquals(1, act.get("NUTS/2/BE25").size());
		assertEquals("BE2", act.get("NUTS/2/BE25").get(0).get("ParentDivisionCode"));
		assertNotNull(act.get("NUTS/2/BE10"));
		assertEquals(1, act.get("NUTS/2/BE10").size());
		assertEquals("BE1", act.get("NUTS/2/BE10").get(0).get("ParentDivisionCode"));
	}

}

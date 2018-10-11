package io.beanmother.core;

import io.beanmother.testmodel.Author;
import org.junit.Test;

import java.net.URISyntaxException;

import static org.junit.Assert.assertNotNull;

/**
 * Test for {@link AbstractBeanMother}
 */
public class AbstractBeanMotherTest {

    static class TestObjectMother extends AbstractBeanMother {

        private final static TestObjectMother beanMother = new AbstractBeanMotherTest.TestObjectMother();

        public static TestObjectMother getInstance() {
            return beanMother;
        }

        private TestObjectMother() {
            super();
        }

        @Override
        public String[] defaultFixturePaths() {
            return new String[]{"testmodel_fixtures"};
        }
    }

    TestObjectMother beanMother = TestObjectMother.getInstance();

    @Test
    public void testSingleMapping() throws URISyntaxException {
        Author author = beanMother.bear("unknown_author", Author.class);
        assertNotNull(author);
        assertNotNull(author.getName());
    }

    @Test
    public void testMappingList() throws URISyntaxException {
//        List<Author> authors = beanMother.bear("unknown_author", Author.class, 5);
//        assertEquals(5, authors.size());
    }
}
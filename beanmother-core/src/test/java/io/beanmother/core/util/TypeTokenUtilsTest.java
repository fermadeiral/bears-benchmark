package io.beanmother.core.util;

import com.google.common.reflect.TypeToken;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link TypeTokenUtils}
 */
public class TypeTokenUtilsTest {

    @Test
    public void testGetGenericTypeTokens() {
        TypeToken source = new TypeToken<List<String>>() {};
        List<TypeToken<?>> typeTokens = TypeTokenUtils.extractGenericTypeTokens(source);

        assertEquals(typeTokens.size(), 1);
        assertEquals(typeTokens.get(0), TypeToken.of(String.class));
    }

    @Test
    public void testGetMultipleGenericTypeTokens() {
        TypeToken source = new TypeToken<Map<String, Integer>>() {};
        List<TypeToken<?>> typeTokens = TypeTokenUtils.extractGenericTypeTokens(source);

        assertEquals(typeTokens.size(), 2);
        assertEquals(typeTokens.get(0), TypeToken.of(String.class));
        assertEquals(typeTokens.get(1), TypeToken.of(Integer.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRaiseException_extractElementTypeToken() {
        TypeTokenUtils.extractElementTypeToken(TypeToken.of(Integer.class));
    }
}
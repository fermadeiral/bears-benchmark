/**
 * The MIT License
 *
 *   Copyright (c) 2017, Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 */
package io.github.benas.randombeans.randomizers.registry;

import io.github.benas.randombeans.FieldDefinition;
import io.github.benas.randombeans.annotation.Priority;
import io.github.benas.randombeans.api.EnhancedRandomParameters;
import io.github.benas.randombeans.api.Randomizer;
import io.github.benas.randombeans.api.RandomizerRegistry;
import io.github.benas.randombeans.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry of user defined randomizers.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
@Priority(-1)
public class CustomRandomizerRegistry extends AbstractRandomizerRegistry implements RandomizerRegistry {

    private final Map<FieldDefinition<?, ?>, Randomizer<?>> customFieldRandomizersRegistry = new HashMap<>();
    private final Map<Class<?>, Randomizer<?>> customTypeRandomizersRegistry = new HashMap<>();

    @Override
    public void init(EnhancedRandomParameters parameters) {
        // no op
    }

    @Override
    public Randomizer<?> getRandomizer(Field field) {
        for (FieldDefinition<?, ?> fieldDefinition : customFieldRandomizersRegistry.keySet()) {
            if (hasName(field, fieldDefinition.getName()) &&
                    isDeclaredInClass(field, fieldDefinition.getClazz()) &&
                    hasType(field, fieldDefinition.getType()) &&
                    isAnnotatedWithOneOf(field, fieldDefinition.getAnnotations()) &&
                    hasAllModifiers(field, fieldDefinition.getModifiers())) {
                return customFieldRandomizersRegistry.get(fieldDefinition);
            }
        }
        return getRandomizer(field.getType());
    }

    @Override
    public Randomizer<?> getRandomizer(Class<?> type) {
        // issue 241: primitive type were ignored: try to get randomizer by primtive type, if not, then try by wrapper type
        Randomizer<?> randomizer = customTypeRandomizersRegistry.get(type);
        if( randomizer == null) {
            Class<?> wrapperType = type.isPrimitive() ? ReflectionUtils.getWrapperType(type) : type;
            randomizer =  customTypeRandomizersRegistry.get(wrapperType);
        }
        return randomizer;
    }

    public <T, F, R> void registerRandomizer(final FieldDefinition<T,F> fieldDefinition, final Randomizer<R> randomizer) {
        customFieldRandomizersRegistry.put(fieldDefinition, randomizer);
    }

    public <T, R> void registerRandomizer(final Class<T> type, final Randomizer<R> randomizer) {
        customTypeRandomizersRegistry.put(type, randomizer);
    }

}

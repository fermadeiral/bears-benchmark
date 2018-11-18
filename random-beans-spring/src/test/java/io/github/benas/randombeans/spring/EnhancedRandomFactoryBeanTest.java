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
package io.github.benas.randombeans.spring;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import io.github.benas.randombeans.api.EnhancedRandom;

public class EnhancedRandomFactoryBeanTest {

    @Test
    public void testEnhancedRandomFactoryBeanWithDefaultRandomizers() {

        EnhancedRandom enhancedRandom = getEnhancedRandomFromSpringContext("/application-context.xml");

        // the enhancedRandom managed by spring should be correctly configured
        assertThat(enhancedRandom).isNotNull();

        // the enhancedRandom should generate valid instances
        Foo foo = enhancedRandom.nextObject(Foo.class);

        assertThat(foo).hasNoNullFieldsOrProperties();
    }

    @Test
    public void testEnhancedRandomFactoryBeanWithCustomRandomizers() {

        EnhancedRandom enhancedRandom = getEnhancedRandomFromSpringContext("/application-context-with-custom-randomizers.xml");

        // the enhancedRandom managed by spring should be correctly configured
        assertThat(enhancedRandom).isNotNull();

        // the enhancedRandom should generate valid instances
        Foo foo = enhancedRandom.nextObject(Foo.class);

        assertThat(foo).isNotNull();
        assertThat(foo.getName()).isIn(NameRandomizer.NAMES);
        assertThat(foo.getAge()).isEqualTo(10);
        assertThat(foo.getWeight()).isEqualTo(10);
        assertThat(foo.getBar()).isIn(NameRandomizer.NAMES);
        assertThat(foo.getNickName().length()).isLessThanOrEqualTo(3);
    }

    private EnhancedRandom getEnhancedRandomFromSpringContext(String contextFileName) {
        try (AbstractApplicationContext applicationContext = new ClassPathXmlApplicationContext(contextFileName)) {
            return applicationContext.getBean(EnhancedRandom.class);
        }
    }

}

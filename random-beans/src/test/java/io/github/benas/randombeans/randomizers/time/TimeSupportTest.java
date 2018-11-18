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
package io.github.benas.randombeans.randomizers.time;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandom;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.benas.randombeans.EnhancedRandomBuilder;
import io.github.benas.randombeans.FieldDefinitionBuilder;
import io.github.benas.randombeans.api.EnhancedRandom;
import io.github.benas.randombeans.beans.TimeBean;

public class TimeSupportTest {

    private EnhancedRandom enhancedRandom;

    @BeforeEach
    public void setUp() {
        enhancedRandom = aNewEnhancedRandom();
    }

    @Test
    public void threeTenTypesShouldBePopulated() {
        TimeBean timeBean = enhancedRandom.nextObject(TimeBean.class);

        assertThat(timeBean).hasNoNullFieldsOrProperties();
    }

    @Test
    // https://github.com/benas/random-beans/issues/135
    public void threeTenRandomizersCanBeOverriddenByCustomRandomizers() {
        EnhancedRandom customEnhancedRandom = EnhancedRandomBuilder.aNewEnhancedRandomBuilder()
                .exclude(FieldDefinitionBuilder.field().named("instant").ofType(Instant.class).inClass(TimeBean.class).get()).build();

        TimeBean timeBean = customEnhancedRandom.nextObject(TimeBean.class);

        assertThat(timeBean.getInstant()).isNull();
    }
}

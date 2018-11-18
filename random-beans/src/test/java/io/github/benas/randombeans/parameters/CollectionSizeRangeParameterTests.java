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
package io.github.benas.randombeans.parameters;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandomBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import io.github.benas.randombeans.api.EnhancedRandom;

public class CollectionSizeRangeParameterTests {

    @Test
    public void shouldNotAllowNegativeMinCollectionSize() {
        assertThatThrownBy(() -> aNewEnhancedRandomBuilder().collectionSizeRange(-1, 10).build()).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldNotAllowMinCollectionSizeGreaterThanMaxCollectionSize() {
        assertThatThrownBy(() -> aNewEnhancedRandomBuilder().collectionSizeRange(2, 1).build()).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void generatedCollectionSizeShouldBeInSpecifiedRange() {
        EnhancedRandom enhancedRandom = aNewEnhancedRandomBuilder().collectionSizeRange(0, 10).build();

        assertThat(enhancedRandom.nextObject(ArrayList.class).size()).isBetween(0, 10);
    }

    @Test // https://github.com/benas/random-beans/issues/191
    public void collectionSizeRangeShouldWorkForArrays() {
        EnhancedRandom enhancedRandom = aNewEnhancedRandomBuilder().collectionSizeRange(0, 10).build();

        String[] strArr = enhancedRandom.nextObject(String[].class);

        assertThat(strArr.length).isLessThanOrEqualTo(10);
    }

}

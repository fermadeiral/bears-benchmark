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
package io.github.benas.randombeans.randomizers.text;

import static io.github.benas.randombeans.randomizers.text.StringRandomizer.aNewStringRandomizer;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.benas.randombeans.randomizers.AbstractRandomizerTest;

public class StringRandomizerTest extends AbstractRandomizerTest<String> {

    @BeforeEach
    public void setUp() {
        randomizer = aNewStringRandomizer();
    }

    @Test
    public void generatedValueMustNotBeNull() {
        assertThat(randomizer.getRandomValue()).isNotNull();
    }

    @Test
    public void shouldGenerateTheSameValueForTheSameSeed() {
        // Given
        randomizer = aNewStringRandomizer(SEED);
        String expected = "eOMtThyhVNLWUZNRcBaQKxIy";

        // When
        String actual = randomizer.getRandomValue();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void theLengthOfTheGeneratedValueShouldBeLowerThanTheSpecifiedMaxLength() {
        // Given
        final int maxLength = 10;
        randomizer = aNewStringRandomizer(maxLength, SEED);
        String expectedValue = "eOM";

        // When
        String actual = randomizer.getRandomValue();

        // Then
        assertThat(actual).isEqualTo(expectedValue);
        assertThat(actual.length()).isLessThanOrEqualTo(maxLength);
    }

    @Test
    public void theLengthOfTheGeneratedValueShouldBeGreaterThanTheSpecifiedMinLength() {
        // Given
        final int minLength = 3;
        final int maxLength = 10;
        randomizer = aNewStringRandomizer(minLength, maxLength, SEED);
        String expectedValue = "eOMtThyh";

        // When
        String actual = randomizer.getRandomValue();

        // Then
        assertThat(actual).isEqualTo(expectedValue);
        assertThat(actual.length()).isBetween(minLength, maxLength);
    }

}

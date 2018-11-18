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
package io.github.benas.randombeans.randomizers.range;

import static io.github.benas.randombeans.randomizers.range.ZonedDateTimeRangeRandomizer.aNewZonedDateTimeRangeRandomizer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.ZonedDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.benas.randombeans.util.Constants;

public class ZonedDateTimeRangeRandomizerTest extends AbstractRangeRandomizerTest<ZonedDateTime> {

    private ZonedDateTime minZonedDateTime, maxZonedDateTime;

    @BeforeEach
    public void setUp() {
        minZonedDateTime = Constants.DEFAULT_DATES_RANGE.getMin().minusYears(50);
        maxZonedDateTime = Constants.DEFAULT_DATES_RANGE.getMax().plusYears(50);
        randomizer = aNewZonedDateTimeRangeRandomizer(minZonedDateTime, maxZonedDateTime);
    }

    @Test
    public void generatedZonedDateTimeShouldNotBeNull() {
        assertThat(randomizer.getRandomValue()).isNotNull();
    }

    @Test
    public void generatedZonedDateTimeShouldBeWithinSpecifiedRange() {
        assertThat(randomizer.getRandomValue()).isBetween(minZonedDateTime, maxZonedDateTime);
    }

    @Test
    public void generatedZonedDateTimeShouldBeAlwaysTheSameForTheSameSeed() {
        // Given
        randomizer = aNewZonedDateTimeRangeRandomizer(minZonedDateTime, maxZonedDateTime, SEED);
        ZonedDateTime expected = ZonedDateTime.parse("2046-10-12T17:24:27+01:00");

        // When
        ZonedDateTime randomValue = randomizer.getRandomValue();

        // Then
        assertThat(randomValue).isEqualTo(expected);
    }

    @Test
    public void whenSpecifiedMinZonedDateTimeIsAfterMaxZonedDateTime_thenShouldThrowIllegalArgumentException() {
        assertThatThrownBy(() -> aNewZonedDateTimeRangeRandomizer(maxZonedDateTime, minZonedDateTime)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void whenSpecifiedMinZonedDateTimeIsNull_thenShouldUseDefaultMinValue() {
        // Given
        randomizer = aNewZonedDateTimeRangeRandomizer(null, maxZonedDateTime);

        // When
        ZonedDateTime randomValue = randomizer.getRandomValue();

        // Then
        assertThat(randomValue).isBeforeOrEqualTo(maxZonedDateTime);
    }

    @Test
    public void whenSpecifiedMaxZonedDateTimeIsNull_thenShouldUseDefaultMaxValue() {
        // Given
        randomizer = aNewZonedDateTimeRangeRandomizer(minZonedDateTime, null);

        // when
        ZonedDateTime randomValue = randomizer.getRandomValue();

        // Then
        assertThat(randomValue).isAfterOrEqualTo(minZonedDateTime);
    }

}

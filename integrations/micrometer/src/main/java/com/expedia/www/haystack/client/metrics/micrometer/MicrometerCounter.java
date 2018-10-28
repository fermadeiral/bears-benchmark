/*
 * Copyright 2018 Expedia, Inc.
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 *
 */
package com.expedia.www.haystack.client.metrics.micrometer;

import com.expedia.www.haystack.client.metrics.Counter;

public class MicrometerCounter implements Counter {
    private final io.micrometer.core.instrument.Counter delegate;

    public MicrometerCounter(io.micrometer.core.instrument.Counter delegate) {
        this.delegate = delegate;
    }

    @Override
    public void increment(double amount) {
        delegate.increment(amount);
    }

    @Override
    public void decrement(double amount) {
        increment(-1 * amount);
    }

    @Override
    public double count() {
        return delegate.count();
    }
}

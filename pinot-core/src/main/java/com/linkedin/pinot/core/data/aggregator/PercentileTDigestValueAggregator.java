/**
 * Copyright (C) 2014-2018 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linkedin.pinot.core.data.aggregator;

import com.linkedin.pinot.common.data.FieldSpec.DataType;
import com.linkedin.pinot.core.common.ObjectSerDeUtils;
import com.linkedin.pinot.core.query.aggregation.function.AggregationFunctionType;
import com.linkedin.pinot.core.query.aggregation.function.PercentileTDigestAggregationFunction;
import com.tdunning.math.stats.TDigest;


public class PercentileTDigestValueAggregator implements ValueAggregator<Object, TDigest> {
  public static final DataType AGGREGATED_VALUE_TYPE = DataType.BYTES;

  private int _maxByteSize;

  @Override
  public AggregationFunctionType getAggregationType() {
    return AggregationFunctionType.PERCENTILETDIGEST;
  }

  @Override
  public DataType getAggregatedValueType() {
    return AGGREGATED_VALUE_TYPE;
  }

  @Override
  public TDigest getInitialAggregatedValue(Object rawValue) {
    TDigest initialValue;
    if (rawValue instanceof byte[]) {
      byte[] bytes = (byte[]) rawValue;
      initialValue = deserializeAggregatedValue(bytes);
      _maxByteSize = Math.max(_maxByteSize, bytes.length);
    } else {
      initialValue = TDigest.createMergingDigest(PercentileTDigestAggregationFunction.DEFAULT_TDIGEST_COMPRESSION);
      initialValue.add(((Number) rawValue).doubleValue());
      _maxByteSize = Math.max(_maxByteSize, initialValue.byteSize());
    }
    return initialValue;
  }

  @Override
  public TDigest applyRawValue(TDigest value, Object rawValue) {
    if (rawValue instanceof byte[]) {
      value.add(deserializeAggregatedValue((byte[]) rawValue));
    } else {
      value.add(((Number) rawValue).doubleValue());
    }
    _maxByteSize = Math.max(_maxByteSize, value.byteSize());
    return value;
  }

  @Override
  public TDigest applyAggregatedValue(TDigest value, TDigest aggregatedValue) {
    value.add(aggregatedValue);
    _maxByteSize = Math.max(_maxByteSize, value.byteSize());
    return value;
  }

  @Override
  public TDigest cloneAggregatedValue(TDigest value) {
    return deserializeAggregatedValue(serializeAggregatedValue(value));
  }

  @Override
  public int getMaxAggregatedValueByteSize() {
    return _maxByteSize;
  }

  @Override
  public byte[] serializeAggregatedValue(TDigest value) {
    return ObjectSerDeUtils.TDIGEST_SER_DE.serialize(value);
  }

  @Override
  public TDigest deserializeAggregatedValue(byte[] bytes) {
    return ObjectSerDeUtils.TDIGEST_SER_DE.deserialize(bytes);
  }
}

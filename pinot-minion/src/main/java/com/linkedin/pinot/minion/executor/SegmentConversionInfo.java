/**
 * Copyright (C) 2014-2016 LinkedIn Corp. (pinot-core@linkedin.com)
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
package com.linkedin.pinot.minion.executor;

import com.linkedin.pinot.core.minion.SegmentPurger;
import java.io.File;


public class SegmentConversionInfo {
  private File _file;
  private SegmentPurger _segmentPurger;
  private String _tableName;

  public SegmentConversionInfo(SegmentConversionInfoBuilder segmentConversionInfoBuilder) {
    _file = segmentConversionInfoBuilder._file;
    _segmentPurger = segmentConversionInfoBuilder._segmentPurger;
    _tableName = segmentConversionInfoBuilder._tableName;
  }

  public File getFile() {
    return _file;
  }

  public SegmentPurger getSegmentPurger() {
    return _segmentPurger;
  }

  public String getTableName() {
    return _tableName;
  }

  public SegmentConversionInfo build() {
    return this;
  }

  public static class SegmentConversionInfoBuilder {
    private File _file;
    private SegmentPurger _segmentPurger;
    private String _tableName;

    public SegmentConversionInfoBuilder() { }

    public SegmentConversionInfoBuilder setFile(File file) {
      _file = file;
      return this;
    }

    public SegmentConversionInfoBuilder setSegmentPurger(SegmentPurger segmentPurger) {
      _segmentPurger = segmentPurger;
      return this;
    }

    public SegmentConversionInfoBuilder setTableName(String tableName) {
      _tableName = tableName;
      return this;
    }

    public SegmentConversionInfo build() {
      return new SegmentConversionInfo(this);
    }
  }
}

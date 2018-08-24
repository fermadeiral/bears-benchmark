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
package com.linkedin.pinot.common.config;

import javax.annotation.Nullable;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Class representing configurations related to segment assignment strategy.
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReplicaGroupStrategyConfig {

  private String _partitionColumn;
  private int _numInstancesPerPartition;
  private boolean _mirrorAssignmentAcrossReplicaGroups;

  /**
   * Returns the number of instances that segments for a partition span.
   *
   * @return Number of instances used for a partition.
   */
  public int getNumInstancesPerPartition() {
    return _numInstancesPerPartition;
  }

  public void setNumInstancesPerPartition(int numInstancesPerPartition) {
    _numInstancesPerPartition = numInstancesPerPartition;
  }

  /**
   * Returns the configuration for mirror assignment across replica groups. If this is set to true, each server in
   * a replica group will be mirrored in other replica groups.
   *
   * @return Configuration for mirror assignment across replica groups.
   */
  public boolean getMirrorAssignmentAcrossReplicaGroups() {
    return _mirrorAssignmentAcrossReplicaGroups;
  }

  public void setMirrorAssignmentAcrossReplicaGroups(boolean mirrorAssignmentAcrossReplicaGroups) {
    _mirrorAssignmentAcrossReplicaGroups = mirrorAssignmentAcrossReplicaGroups;
  }

  /**
   * Returns the name of column used for partitioning. If this is set to null, we use the table level replica groups.
   * Otherwise, we use the partition level replica groups.
   *
   * @return Name of partitioning column.
   */
  @Nullable
  public String getPartitionColumn() {
    return _partitionColumn;
  }

  public void setPartitionColumn(String partitionColumn) {
    _partitionColumn = partitionColumn;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ReplicaGroupStrategyConfig that = (ReplicaGroupStrategyConfig) o;

    if (_numInstancesPerPartition != that._numInstancesPerPartition) {
      return false;
    }
    if (_mirrorAssignmentAcrossReplicaGroups != that._mirrorAssignmentAcrossReplicaGroups) {
      return false;
    }
    return _partitionColumn != null ? _partitionColumn.equals(that._partitionColumn) : that._partitionColumn == null;
  }

  @Override
  public int hashCode() {
    int result = _partitionColumn != null ? _partitionColumn.hashCode() : 0;
    result = 31 * result + _numInstancesPerPartition;
    result = 31 * result + (_mirrorAssignmentAcrossReplicaGroups ? 1 : 0);
    return result;
  }
}

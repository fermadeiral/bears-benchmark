/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.jackrabbit.oak.plugins.index.lucene.util;

import java.io.Reader;

import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.util.Version;

/**
 * A {@link CharTokenizer} dividing tokens at <code>\n</code>.
 * <p/>
 * This should be deprecated/removed and not used anymore in {@link org.apache.jackrabbit.oak.plugins.index.lucene.util.SuggestHelper}
 * (and related 'suggest fields merging code' removed in {@link org.apache.jackrabbit.oak.plugins.index.lucene.LuceneIndexEditor})
 * if / once LUCENE-5833 fix gets included in the Lucene version we ship.
 */
public class CRTokenizer extends CharTokenizer {
    public CRTokenizer(Version matchVersion, Reader input) {
        super(matchVersion, input);
    }

    @Override
    protected boolean isTokenChar(int c) {
        return c != '\n';
    }
}

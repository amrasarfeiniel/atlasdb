// Copyright 2015 Palantir Technologies
//
// Licensed under the BSD-3 License (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://opensource.org/licenses/BSD-3-Clause
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.palantir.atlasdb.table.api;

import java.util.concurrent.TimeUnit;

public interface AtlasDbDynamicMutableExpiringTable<ROW, COLUMN, COLUMN_VALUE, ROW_RESULT> extends
            AtlasDbDynamicMutableTable<ROW, COLUMN, COLUMN_VALUE, ROW_RESULT>,
            AtlasDbMutableExpiringTable<ROW, COLUMN_VALUE, ROW_RESULT> {
    public void put(ROW row, Iterable<COLUMN_VALUE> values, long duration, TimeUnit durationUnit);
    public void put(long duration, TimeUnit durationUnit, ROW row, COLUMN_VALUE... values);
}
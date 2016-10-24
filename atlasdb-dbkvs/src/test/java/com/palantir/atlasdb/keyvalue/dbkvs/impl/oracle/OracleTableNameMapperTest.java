/**
 * Copyright 2016 Palantir Technologies
 *
 * Licensed under the BSD-3 License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.palantir.atlasdb.keyvalue.dbkvs.impl.oracle;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

import org.junit.Assert;
import org.junit.Test;

import com.palantir.atlasdb.keyvalue.api.TableReference;
import com.palantir.atlasdb.keyvalue.dbkvs.impl.DbKvs;

public class OracleTableNameMapperTest {
    OracleTableNameMapper nameMapper = new OracleTableNameMapper();

    private static final int TABLE_NAME_LENGTH = 30;

    private static final String TABLE_PREFIX = "a_";
    private static final TableReference SHORT_TABLE_NAME = TableReference.createFromFullyQualifiedName("ns.test_table");
    private static final TableReference LONG_TABLE_NAME_WITHOUT_VOWELS = TableReference.createFromFullyQualifiedName("ns.Lng_tbl_nm_wtht_vwls_0123456789");

    private static final String ORACLE_TABLE_NAME_REGEX = "^(?!_)^[a-zA-Z0-9_]*$";

    @Test
    public void shouldNotModifyNameForShortTableNames() {
        String fullTableName = TABLE_PREFIX + DbKvs.internalTableName(SHORT_TABLE_NAME);
        Assert.assertThat(nameMapper.getShortPrefixedTableName(TABLE_PREFIX, SHORT_TABLE_NAME), is(fullTableName));
    }

    @Test
    public void shouldReturnValidAndLongestPossibleOracleTableNamesForLongTableNames() {
        Assert.assertThat(DbKvs.internalTableName(LONG_TABLE_NAME_WITHOUT_VOWELS).length(), greaterThan(TABLE_NAME_LENGTH));
        String shortTableName = nameMapper.getShortPrefixedTableName(TABLE_PREFIX, LONG_TABLE_NAME_WITHOUT_VOWELS);
        Assert.assertThat(shortTableName.length(), equalTo(TABLE_NAME_LENGTH));
        Assert.assertTrue(shortTableName.matches(ORACLE_TABLE_NAME_REGEX));
    }

    @Test
    public void shouldReturnDifferentValidOracleTableNamesForAlmostSimilarLongTableNames() {
        TableReference longTableName1 = TableReference.createFromFullyQualifiedName("ns." + LONG_TABLE_NAME_WITHOUT_VOWELS + "1");
        TableReference longTableName2 = TableReference.createFromFullyQualifiedName("ns." + LONG_TABLE_NAME_WITHOUT_VOWELS + "2");

        String shortTableName1 = nameMapper.getShortPrefixedTableName(TABLE_PREFIX, longTableName1);
        String shortTableName2= nameMapper.getShortPrefixedTableName(TABLE_PREFIX, longTableName2);

        Assert.assertNotEquals(shortTableName1, shortTableName2);
    }

    @Test
    public void shouldReturnMoreReadableTableNameForTableNamesWithVowels() {
        TableReference longTableNameWithManyVowels = TableReference.createFromFullyQualifiedName("ns.eeeeeaaaaabbbbbeeeeeaaaaabbbbbaaaaa");

        String shortTableName = nameMapper.getShortPrefixedTableName(TABLE_PREFIX, longTableNameWithManyVowels);
        String expectedName = TABLE_PREFIX + "ns__eeeeeaaaabbbbbbbbbb";

        Assert.assertThat(shortTableName.length(), is(TABLE_NAME_LENGTH));
        Assert.assertTrue(shortTableName.matches(ORACLE_TABLE_NAME_REGEX));
        Assert.assertThat(shortTableName, startsWith(expectedName));
    }

    @Test
    public void shouldReturnMoreReadableTableNameForTableNamesWithVowelsAndStillLong() {
        TableReference longTableNameWithLessVowels = TableReference.createFromFullyQualifiedName("ns.eeeeebbbbbbbbbbccccccccccddddddcccc");

        String shortTableName = nameMapper.getShortPrefixedTableName(TABLE_PREFIX, longTableNameWithLessVowels);
        String expectedName = TABLE_PREFIX + "ns__bbbbbbbbbbccccccccccddddddcccc".substring(0, 19);

        Assert.assertThat(shortTableName.length(), is(TABLE_NAME_LENGTH));
        Assert.assertTrue(shortTableName.matches(ORACLE_TABLE_NAME_REGEX));
        Assert.assertThat(shortTableName, startsWith(expectedName));
    }
}

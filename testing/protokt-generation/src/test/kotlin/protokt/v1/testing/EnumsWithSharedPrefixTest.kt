/*
 * Copyright (c) 2020 Toast, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package protokt.v1.testing

import org.junit.jupiter.api.Test

class EnumsWithSharedPrefixTest {
    @Test
    fun `reference each enum name`() {
        // prefix is stripped
        AnEnumType.UNSPECIFIED
        AnEnumType.FIRST
        AnEnumType.SECOND

        // not stripped, no common prefix
        AnEnumTypeNotPrefixed.AN_ENUM_TYPE_NOT_PREFIXED_UNSPECIFIED
        AnEnumTypeNotPrefixed.AN_ENUM_TYPE_NOT_PREFIXED_FIRST
        AnEnumTypeNotPrefixed.SECOND

        // not stripped, no underscore after type name
        AnEnumTypeNoTerminalUnderscore.AN_ENUM_TYPE_NO_TERMINAL_UNDERSCOREUNSPECIFIED
        AnEnumTypeNoTerminalUnderscore.AN_ENUM_TYPE_NO_TERMINAL_UNDERSCOREFIRST
        AnEnumTypeNoTerminalUnderscore.AN_ENUM_TYPE_NO_TERMINAL_UNDERSCORESECOND

        // not stripped, prefix is not upper case
        AnEnumTypeLowerCase.AN_ENUM_TYPE_LOWER_CASE_UNSPECIFIED
        AnEnumTypeLowerCase.AN_ENUM_TYPE_LOWER_CASE_FIRST
        AnEnumTypeLowerCase.AN_ENUM_TYPE_lower_CASE_SECOND
    }
}

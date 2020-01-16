/*
 * Copyright (c) 2019 Toast Inc.
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

package com.toasttab.protokt.shared

import java.util.jar.JarInputStream

const val DEFAULT_PROTOBUF_VERSION = "3.11.0"
const val MANIFEST_VERSION_PROPERTY = "Implementation-Version"

open class ProtoktExtension {
    var protocVersion = DEFAULT_PROTOBUF_VERSION
    var toolsVersion: String? = null
    var publishProto: Boolean = false

    val version by lazy {
        toolsVersion ?: ProtoktExtension::class.java.protectionDomain.codeSource.location.openStream().use {
            JarInputStream(it).manifest.mainAttributes.getValue(MANIFEST_VERSION_PROPERTY)
        }
    }
}

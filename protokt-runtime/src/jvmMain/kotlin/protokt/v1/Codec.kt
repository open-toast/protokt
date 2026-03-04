/*
 * Copyright (c) 2026 Toast, Inc.
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

@file:OptIn(OnlyForUseByGeneratedProtoCode::class)

package protokt.v1

internal actual val codec: Codec by lazy {
    val codecFqcn =
        System.getProperty("protokt.codec")
            ?: System.getenv("PROTOKT_CODEC")

    if (codecFqcn != null) {
        Class.forName(codecFqcn).getField("INSTANCE").get(null) as Codec
    } else {
        tryLoad("protokt.v1.OptimalJvmCodec") as? Codec
            ?: tryLoad("protokt.v1.OptimalKmpCodec") as? Codec
            ?: tryLoad("protokt.v1.ProtobufJavaCodec") as? Codec
            ?: tryLoad("protokt.v1.KotlinxIoCodec") as? Codec
            ?: ProtoktCodec
    }
}

private fun tryLoad(fqcn: String): Any? =
    try {
        Class.forName(fqcn).getField("INSTANCE").get(null)
    } catch (_: ClassNotFoundException) {
        null
    }

/*
 * Copyright (c) 2023 Toast, Inc.
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

spotless {
    kotlin {
        targetExclude(
            "protokt-jvm-extensions-lite/src/main/kotlin/com/toasttab/protokt/ext/inet_socket_address.kt",
            "protokt-extensions-lite/src/jvmMain/kotlin/com/toasttab/protokt/ext/protokt.kt",
            "protokt-extensions/src/jvmMain/kotlin/com/toasttab/protokt/ext/ProtoktProto.kt"
        )
    }
}

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

import com.toasttab.expediter.gradle.config.ExpediterExtension
import protokt.v1.gradle.protokt

plugins {
    id("protokt.jvm-conventions")
}

localProtokt()
pureKotlin()
enablePublishing()
compatibleWithAndroid()
trackKotlinApiCompatibility()

configure<ExpediterExtension> {
    ignore {
        // ClassValueCtorCache conditionally uses java.lang.ClassValue when not on Android
        callerStartsWith("kotlinx/coroutines/internal/ClassValueCtorCache")
    }
}

protokt {
    generate {
        types = false
        descriptors = false
        grpcKotlinStubs = true
    }
}

dependencies {
    protobufExcludingProtobufJava(libs.protoGoogleCommonProtos)

    api(project(":third-party:proto-google-common-protos-grpc"))
    api(libs.grpc.kotlin.stub)
}

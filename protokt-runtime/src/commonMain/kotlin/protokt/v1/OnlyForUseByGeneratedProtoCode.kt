/*
 * Copyright (c) 2025 Toast, Inc.
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

package protokt.v1

/**
 * Opt-in annotation to make it difficult to accidentally use APIs only intended for use by proto
 * generated code. See https://kotlinlang.org/docs/reference/opt-in-requirements.html for details on
 * how this API works.
 */
@RequiresOptIn(
    message =
    """
    This API is only intended for use by generated protobuf code, the code generator, and their own
    tests.  If this does not describe your code, you should not be using this API.
  """,
    level = RequiresOptIn.Level.ERROR,
)
@Retention(AnnotationRetention.BINARY)
annotation class OnlyForUseByGeneratedProtoCode

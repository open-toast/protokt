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

package protokt.v1.benchmarks

// PersistentCollectionFactory is internal to protokt-runtime-persistent-collections.
// On native, set PROTOKT_COLLECTION_FACTORY=protokt.v1.PersistentCollectionFactory
// before running benchmarks to test persistent collections.
actual fun applyBenchmarkConfig(collectionFactory: String) {}

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

package protokt.v1

import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Configures process-wide protobuf runtime implementations before their first use.
 */
object ProtoktRuntime {
    /**
     * Selects [codec] and retains the default collection implementation.
     */
    fun configure(codec: Codec) {
        RuntimeConfiguration.configure(codec, DefaultCollectionFactory)
    }

    /**
     * Selects [collectionFactory] and retains the default codec.
     */
    fun configure(collectionFactory: CollectionFactory) {
        RuntimeConfiguration.configure(ProtoktCodec, collectionFactory)
    }

    /**
     * Selects [codec] and [collectionFactory] together.
     */
    fun configure(
        codec: Codec,
        collectionFactory: CollectionFactory,
    ) {
        RuntimeConfiguration.configure(codec, collectionFactory)
    }
}

internal class ActiveRuntimeConfiguration(
    val codec: Codec,
    val collectionFactory: CollectionFactory,
)

@OptIn(ExperimentalAtomicApi::class)
internal object RuntimeConfiguration {
    private val state = AtomicReference<State>(State.Unconfigured)

    fun configure(
        codec: Codec,
        collectionFactory: CollectionFactory,
    ) {
        while (true) {
            when (val current = state.load()) {
                State.Unconfigured -> {
                    if (state.compareAndSet(current, State.Configured(ActiveRuntimeConfiguration(codec, collectionFactory)))) {
                        return
                    }
                }

                is State.Configured -> error("Protokt runtime is already configured")

                is State.Active -> error("Protokt runtime cannot be configured after first use")
            }
        }
    }

    fun active(): ActiveRuntimeConfiguration {
        while (true) {
            when (val current = state.load()) {
                State.Unconfigured -> {
                    val active = State.Active(ActiveRuntimeConfiguration(ProtoktCodec, DefaultCollectionFactory))
                    if (state.compareAndSet(current, active)) {
                        return active.configuration
                    }
                }

                is State.Configured -> {
                    val active = State.Active(current.configuration)
                    if (state.compareAndSet(current, active)) {
                        return active.configuration
                    }
                }

                is State.Active -> return current.configuration
            }
        }
    }

    internal fun resetForTest() {
        state.store(State.Unconfigured)
    }

    private sealed interface State {
        data object Unconfigured : State

        class Configured(
            val configuration: ActiveRuntimeConfiguration,
        ) : State

        class Active(
            val configuration: ActiveRuntimeConfiguration,
        ) : State
    }
}

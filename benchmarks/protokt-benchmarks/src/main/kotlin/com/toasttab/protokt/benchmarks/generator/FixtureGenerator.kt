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

package com.toasttab.protokt.benchmarks.generator

import com.toasttab.protokt.benchmarks.BenchmarkDataset
import com.toasttab.protokt.benchmarks.GenericMessage1
import com.toasttab.protokt.benchmarks.GenericMessage4
import com.toasttab.protokt.rt.Bytes
import com.toasttab.protokt.rt.KtMessage
import java.io.File
import java.lang.IllegalArgumentException
import java.security.SecureRandom
import kotlin.math.absoluteValue
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor

sealed class FieldType
class ScalarFieldType(val type: ScalarType) : FieldType()
class RepeatedFieldType(val type: ScalarType) : FieldType()
class MapFieldType(val keyType: ScalarType, val valueType: ScalarType) : FieldType()

sealed class ScalarType
class PrimitiveScalarType(val ktType: KClass<*>) : ScalarType()
class MessageScalarType(val ktType: KClass<out KtMessage>) : ScalarType()

internal object TypeInspector {
    fun type(prop: KType): FieldType {
        val propType = prop.classifier as KClass<*>
        return when (propType) {
            List::class -> RepeatedFieldType(scalarType(typeParam(prop, 0)))
            Map::class -> MapFieldType(scalarType(typeParam(prop, 0)), scalarType(typeParam(prop, 1)))
            else -> ScalarFieldType(scalarType(propType))
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun scalarType(cls: KClass<*>) = if (cls.isSubclassOf(KtMessage::class)) {
        MessageScalarType(cls as KClass<out KtMessage>)
    } else {
        PrimitiveScalarType(cls)
    }

    fun typeParam(prop: KType, idx: Int) = prop.arguments[idx].type!!.classifier as KClass<*>
}

/**
 * This was used to generate benchmark datasets.
 */
class FixtureGenerator(private val weight: Int) {
    private val random = SecureRandom()

    fun randomValue(fieldType: FieldType) = when (fieldType) {
        is ScalarFieldType -> randomValue(fieldType.type)
        is RepeatedFieldType -> (0..randomSize()).map { randomValue(fieldType.type) }
        is MapFieldType -> (0..randomSize()).associate { randomValue(fieldType.keyType) to randomValue(fieldType.valueType) }
    }

    fun randomValue(scalarType: ScalarType): Any? =
        when (scalarType) {
            is PrimitiveScalarType -> randomPrimitiveValue(scalarType.ktType)
            is MessageScalarType -> randomMessageValue(scalarType.ktType)
        }

    fun randomMessageValue(cls: KClass<out KtMessage>): KtMessage {
        val params = cls.primaryConstructor!!.parameters.associateWith { p ->
            if (p.name == "unknown") emptyMap<Any, Any>()
            else randomValue(TypeInspector.type(p.type))
        }

        return cls.primaryConstructor!!.callBy(params)
    }

    fun randomPrimitiveValue(cls: KClass<*>): Any? =
        when (cls) {
            Int::class -> random.nextInt()
            Long::class -> random.nextLong()
            Boolean::class -> random.nextInt() % 2 == 0
            Float::class -> random.nextFloat()
            Double::class -> random.nextDouble()
            Bytes::class -> Bytes(ByteArray(randomSize()).also { random.nextBytes(it) })
            String::class -> String(ByteArray(randomSize()).also { random.nextBytes(it) })
            else -> throw IllegalArgumentException("unknown $cls")
        }

    fun randomSize() = random.nextInt().absoluteValue % weight + 1

    fun generateDataset(name: String, msg: KClass<out KtMessage>, size: Int) =
        BenchmarkDataset {
            this.name = name
            messageName = msg.qualifiedName!!
            payload = (0..size).map {
                Bytes(randomMessageValue(msg).serialize())
            }
        }
}

fun KtMessage.writeToFile(file: String) {
    File(file).outputStream().buffered().use { serialize(it) }
}

fun main() {
    FixtureGenerator(100).generateDataset("large", GenericMessage1::class, 100).writeToFile("build/dataset-large")
    FixtureGenerator(5).generateDataset("large", GenericMessage1::class, 100).writeToFile("build/dataset-medium")
    FixtureGenerator(5).generateDataset("large", GenericMessage4::class, 100).writeToFile("build/dataset-small")
}

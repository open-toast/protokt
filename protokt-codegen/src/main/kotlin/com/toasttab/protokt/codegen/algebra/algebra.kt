/*
 * Copyright (c) 2019. Toast Inc.
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

package com.toasttab.protokt.codegen.algebra

import arrow.Kind
import arrow.core.Either
import arrow.core.FunctionK
import arrow.core.ListK
import arrow.core.Nel
import arrow.core.NonEmptyList
import arrow.core.Validated
import arrow.core.ValidatedNel
import arrow.core.extensions.nonemptylist.semigroup.semigroup
import arrow.core.extensions.validated.applicative.applicative
import arrow.core.fix
import arrow.core.identity
import arrow.core.k
import arrow.free.Free
import arrow.free.extensions.FreeMonad
import arrow.free.foldMap
import arrow.fx.IO
import arrow.fx.extensions.io.monadError.monadError
import arrow.fx.fix
import arrow.typeclasses.MonadError
import com.github.andrewoma.dexx.kollection.ImmutableList
import kotlinx.coroutines.runBlocking

// For an overview of Free, it is recommended that you read the following:
// https://typelevel.org/cats/datatypes/freemonad.html

typealias Accumulator<A> = (A) -> Unit

fun <A> Kind<ASTOps.F, A>.fix(): ASTOps<A> = this as ASTOps<A>

data class AST<A>(val data: A, val children: ImmutableList<AST<A>>)

typealias Annotator<A> = (A) -> A
typealias Effects<A, B> = (List<A>, acc: B) -> Unit

// Operations that can be run on an AST (Annotate, Side Effects)
sealed class ASTOps<A> : Kind<ASTOps.F, A> {
    class F private constructor()

    data class Annotate<B>(val type: B) : ASTOps<B>()
    data class Effect<B, C>(val types: List<B>, val acc: C) : ASTOps<Unit>()

    companion object : FreeMonad<F> {
        fun <A> annotate(type: A): Free<F, A> = Free.liftF(Annotate(type))
        fun <A, B> effect(types: List<A>, acc: B): Free<F, Unit> =
            Free.liftF(Effect(types, acc))
    }
}

// Runs our operations
object GeneratorT {
    @Suppress("UNCHECKED_CAST")
    operator fun <F, A, B> invoke(
        annotate: (A) -> A,
        effect: (List<A>, B) -> Unit,
        m: MonadError<F, Throwable>
    ): FunctionK<ASTOps.F, F> =
        object : FunctionK<ASTOps.F, F> {
            override fun <C> invoke(fa: Kind<ASTOps.F, C>): Kind<F, C> {
                return runBlocking {
                    when (val op = fa.fix()) {
                        is ASTOps.Annotate ->
                            m.run { effectCatch { annotate(op.type as A) } }
                        is ASTOps.Effect<*, *> ->
                            m.run {
                                effectCatch {
                                    effect(
                                        op.types.map { it as A },
                                        op.acc as B
                                    )
                                }
                            }
                    } as Kind<F, C>
                }
            }
        }
}

// this interpreter (invoke function) annotates and applies effects.
// We could have different implementations of invoke in separate namespaces.
// It may make sense to move the Interpreter into its own file.
// likewise, the "generate" function should probably take the interpreter
// function as an input parameter.
object Interpreter {
    operator fun <A, B, C> invoke(
        a: Annotator<A>,
        e: Effects<A, C>,
        input: List<B>,
        errors: (Throwable) -> Unit,
        transform: (B) -> A
    ): (C) -> Unit {
        return { out ->
            IO {
                when (val result = interpret(a, e, input, transform)) {
                    is Validated.Valid ->
                        ASTOps.effect(result.a, out).unsafeIO(a, e)
                    is Validated.Invalid ->
                        result.e.map(errors)
                }
            }
                .attempt()
                .unsafeRunSync()
                .fold(
                    { errors(it) },
                    ::identity
                )
        }
    }

    private fun <A, B, C> interpret(
        a: Annotator<A>,
        e: Effects<A, C>,
        input: List<B>,
        transform: (B) -> A
    ): Validated<NonEmptyList<Throwable>, ListK<A>> {
        val acc = ValidatedNel.applicative(Nel.semigroup<Throwable>())

        return input.map {
            Validated.fromEither(
                ASTOps.annotate(transform(it)).unsafeIO(a, e)
            ).toValidatedNel()
        }.k().traverse(acc, ::identity).fix()
    }

    // Free is a recursive structure that can be seen as
    // sequence of operations producing other operations.
    private fun <A, B, C> Free<ASTOps.F, A>.unsafeIO(
        a: Annotator<B>,
        e: Effects<B, C>
    ): Either<Throwable, A> {
        val m = IO.monadError()
        return foldMap(GeneratorT(a, e, m), m)
            .fix()
            .attempt()
            .unsafeRunSync()
    }
}

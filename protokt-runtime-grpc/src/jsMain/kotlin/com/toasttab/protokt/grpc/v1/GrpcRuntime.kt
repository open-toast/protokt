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

package com.toasttab.protokt.grpc.v1

import com.toasttab.protokt.v1.unmodifiableList
import kotlin.js.json

class StatusException(status: Status) : Exception()

class Status private constructor(
    val code: Code,
    val description: String? = null,
    val cause: Throwable? = null
) {
    /**
     * The set of canonical status codes. If new codes are added over time they must choose
     * a numerical value that does not collide with any previously used value.
     */
    enum class Code(
        val value: Int
    ) {
        /**
         * The operation completed successfully.
         */
        OK(0),

        /**
         * The operation was cancelled (typically by the caller).
         */
        CANCELLED(1),

        /**
         * Unknown error.  An example of where this error may be returned is
         * if a Status value received from another address space belongs to
         * an error-space that is not known in this address space.  Also
         * errors raised by APIs that do not return enough error information
         * may be converted to this error.
         */
        UNKNOWN(2),

        /**
         * Client specified an invalid argument.  Note that this differs
         * from FAILED_PRECONDITION.  INVALID_ARGUMENT indicates arguments
         * that are problematic regardless of the state of the system
         * (e.g., a malformed file name).
         */
        INVALID_ARGUMENT(3),

        /**
         * Deadline expired before operation could complete.  For operations
         * that change the state of the system, this error may be returned
         * even if the operation has completed successfully.  For example, a
         * successful response from a server could have been delayed long
         * enough for the deadline to expire.
         */
        DEADLINE_EXCEEDED(4),

        /**
         * Some requested entity (e.g., file or directory) was not found.
         */
        NOT_FOUND(5),

        /**
         * Some entity that we attempted to create (e.g., file or directory) already exists.
         */
        ALREADY_EXISTS(6),

        /**
         * The caller does not have permission to execute the specified
         * operation.  PERMISSION_DENIED must not be used for rejections
         * caused by exhausting some resource (use RESOURCE_EXHAUSTED
         * instead for those errors).  PERMISSION_DENIED must not be
         * used if the caller cannot be identified (use UNAUTHENTICATED
         * instead for those errors).
         */
        PERMISSION_DENIED(7),

        /**
         * Some resource has been exhausted, perhaps a per-user quota, or
         * perhaps the entire file system is out of space.
         */
        RESOURCE_EXHAUSTED(8),

        /**
         * Operation was rejected because the system is not in a state
         * required for the operation's execution.  For example, directory
         * to be deleted may be non-empty, an rmdir operation is applied to
         * a non-directory, etc.
         *
         * <p>A litmus test that may help a service implementor in deciding
         * between FAILED_PRECONDITION, ABORTED, and UNAVAILABLE:
         * (a) Use UNAVAILABLE if the client can retry just the failing call.
         * (b) Use ABORTED if the client should retry at a higher-level
         * (e.g., restarting a read-modify-write sequence).
         * (c) Use FAILED_PRECONDITION if the client should not retry until
         * the system state has been explicitly fixed.  E.g., if an "rmdir"
         * fails because the directory is non-empty, FAILED_PRECONDITION
         * should be returned since the client should not retry unless
         * they have first fixed up the directory by deleting files from it.
         */
        FAILED_PRECONDITION(9),

        /**
         * The operation was aborted, typically due to a concurrency issue
         * like sequencer check failures, transaction aborts, etc.
         *
         * <p>See litmus test above for deciding between FAILED_PRECONDITION,
         * ABORTED, and UNAVAILABLE.
         */
        ABORTED(10),

        /**
         * Operation was attempted past the valid range.  E.g., seeking or
         * reading past end of file.
         *
         * <p>Unlike INVALID_ARGUMENT, this error indicates a problem that may
         * be fixed if the system state changes. For example, a 32-bit file
         * system will generate INVALID_ARGUMENT if asked to read at an
         * offset that is not in the range [0,2^32-1], but it will generate
         * OUT_OF_RANGE if asked to read from an offset past the current
         * file size.
         *
         * <p>There is a fair bit of overlap between FAILED_PRECONDITION and OUT_OF_RANGE.
         * We recommend using OUT_OF_RANGE (the more specific error) when it applies
         * so that callers who are iterating through
         * a space can easily look for an OUT_OF_RANGE error to detect when they are done.
         */
        OUT_OF_RANGE(11),

        /**
         * Operation is not implemented or not supported/enabled in this service.
         */
        UNIMPLEMENTED(12),

        /**
         * Internal errors.  Means some invariants expected by underlying
         * system has been broken.  If you see one of these errors,
         * something is very broken.
         */
        INTERNAL(13),

        /**
         * The service is currently unavailable.  This is a most likely a
         * transient condition and may be corrected by retrying with
         * a backoff. Note that it is not always safe to retry
         * non-idempotent operations.
         *
         * <p>See litmus test above for deciding between FAILED_PRECONDITION,
         * ABORTED, and UNAVAILABLE.
         */
        UNAVAILABLE(14),

        /**
         * Unrecoverable data loss or corruption.
         */
        DATA_LOSS(15),

        /**
         * The request does not have valid authentication credentials for the
         * operation.
         */
        UNAUTHENTICATED(16);

        fun toStatus() =
            STATUS_LIST[value]

        private companion object {
            val STATUS_LIST = unmodifiableList(Code.values().map(::Status))
        }
    }

    fun withCause(cause: Throwable?) =
        Status(code, description, cause)

    fun withDescription(description: String?) =
        Status(code, description, cause)

    companion object {
        // A pseudo-enum of Status instances mapped 1:1 with values in Code. This simplifies construction
        // patterns for derived instances of Status.
        // A pseudo-enum of Status instances mapped 1:1 with values in Code. This simplifies construction
        // patterns for derived instances of Status.
        /** The operation completed successfully.  */
        val OK: Status = Status.Code.OK.toStatus()

        /** The operation was cancelled (typically by the caller).  */
        val CANCELLED: Status = Status.Code.CANCELLED.toStatus()

        /** Unknown error. See [Code.UNKNOWN].  */
        val UNKNOWN: Status = Status.Code.UNKNOWN.toStatus()

        /** Client specified an invalid argument. See [Code.INVALID_ARGUMENT].  */
        val INVALID_ARGUMENT: Status = Status.Code.INVALID_ARGUMENT.toStatus()

        /** Deadline expired before operation could complete. See [Code.DEADLINE_EXCEEDED].  */
        val DEADLINE_EXCEEDED: Status = Status.Code.DEADLINE_EXCEEDED.toStatus()

        /** Some requested entity (e.g., file or directory) was not found.  */
        val NOT_FOUND: Status = Status.Code.NOT_FOUND.toStatus()

        /** Some entity that we attempted to create (e.g., file or directory) already exists.  */
        val ALREADY_EXISTS: Status = Status.Code.ALREADY_EXISTS.toStatus()

        /**
         * The caller does not have permission to execute the specified operation. See [ ][Code.PERMISSION_DENIED].
         */
        val PERMISSION_DENIED: Status = Status.Code.PERMISSION_DENIED.toStatus()

        /** The request does not have valid authentication credentials for the operation.  */
        val UNAUTHENTICATED: Status = Status.Code.UNAUTHENTICATED.toStatus()

        /**
         * Some resource has been exhausted, perhaps a per-user quota, or perhaps the entire file system
         * is out of space.
         */
        val RESOURCE_EXHAUSTED: Status = Status.Code.RESOURCE_EXHAUSTED.toStatus()

        /**
         * Operation was rejected because the system is not in a state required for the operation's
         * execution. See [Code.FAILED_PRECONDITION].
         */
        val FAILED_PRECONDITION: Status = Status.Code.FAILED_PRECONDITION.toStatus()

        /**
         * The operation was aborted, typically due to a concurrency issue like sequencer check failures,
         * transaction aborts, etc. See [Code.ABORTED].
         */
        val ABORTED: Status = Status.Code.ABORTED.toStatus()

        /** Operation was attempted past the valid range. See [Code.OUT_OF_RANGE].  */
        val OUT_OF_RANGE: Status = Status.Code.OUT_OF_RANGE.toStatus()

        /** Operation is not implemented or not supported/enabled in this service.  */
        val UNIMPLEMENTED: Status = Status.Code.UNIMPLEMENTED.toStatus()

        /** Internal errors. See [Code.INTERNAL].  */
        val INTERNAL: Status = Status.Code.INTERNAL.toStatus()

        /** The service is currently unavailable. See [Code.UNAVAILABLE].  */
        val UNAVAILABLE: Status = Status.Code.UNAVAILABLE.toStatus()

        /** Unrecoverable data loss or corruption.  */
        val DATA_LOSS: Status = Status.Code.DATA_LOSS.toStatus()
    }
}

fun Server.addServiceTyped(
    service: ServiceDescriptor,
    implementation: dynamic
) =
    addService(
        json(
            *service.methods.map { method ->
                method.name.replaceFirstChar { it.lowercase() } to json(
                    "path" to "/${service.name}/${method.name}",
                    "requestStream" to !method.type.clientSendsOneMessage,
                    "responseStream" to !method.type.serverSendsOneMessage,
                    "requestSerialize" to { it: dynamic -> Buffer.from(method.requestMarshaller.serialize(it)) },
                    "requestDeserialize" to { it: ByteArray -> method.requestMarshaller.parse(it) },
                    "responseSerialize" to { it: dynamic -> Buffer.from(method.responseMarshaller.serialize(it)) },
                    "responseDeserialize" to { it: ByteArray -> method.responseMarshaller.parse(it) }
                )
            }.toTypedArray()
        ),
        implementation
    )

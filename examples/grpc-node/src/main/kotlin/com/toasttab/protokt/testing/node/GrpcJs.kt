@file:JsModule("@grpc/grpc-js")
@file:JsNonModule

package com.toasttab.protokt.testing.node

internal external class Server {
    fun addService(service: dynamic, implementation: dynamic)

    fun bindAsync(
        port: String,
        creds: ServerCredentials,
        callback: (error: Throwable?, port: Number) -> Unit
    )

    fun start()
}

internal external class ServiceDefinition {

}

internal external class UntypedServiceImplementation {

}

internal external class ServerCredentials {
    companion object {
        fun createInsecure(): ServerCredentials
    }
}

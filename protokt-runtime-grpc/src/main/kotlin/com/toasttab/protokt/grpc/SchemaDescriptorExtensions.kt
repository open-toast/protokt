package com.toasttab.protokt.grpc

import com.toasttab.protokt.FileDescriptor

@Suppress("DEPRECATION")
val SchemaDescriptor.fileDescriptor: FileDescriptor
    get() = fileDescriptorUntyped as FileDescriptor

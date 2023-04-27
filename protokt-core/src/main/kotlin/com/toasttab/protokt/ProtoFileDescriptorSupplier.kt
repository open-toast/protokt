package com.toasttab.protokt

interface ProtoFileDescriptorSupplier {
    /**
     * Returns file descriptor to the underlying proto file.
     */
    fun getFileDescriptor(): FileDescriptor
}

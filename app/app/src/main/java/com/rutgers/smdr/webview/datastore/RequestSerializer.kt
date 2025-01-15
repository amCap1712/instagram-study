package com.rutgers.smdr.webview.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object RequestSerializer : Serializer<com.rutgers.smdr.datastore.Request> {
    override val defaultValue: com.rutgers.smdr.datastore.Request = com.rutgers.smdr.datastore.Request.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): com.rutgers.smdr.datastore.Request {
        try {
            return com.rutgers.smdr.datastore.Request.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: com.rutgers.smdr.datastore.Request, output: OutputStream) = t.writeTo(output)
}

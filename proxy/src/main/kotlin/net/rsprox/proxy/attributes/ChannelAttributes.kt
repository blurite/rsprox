package net.rsprox.proxy.attributes

import io.netty.util.AttributeKey
import net.rsprot.crypto.cipher.StreamCipherPair
import net.rsprox.proxy.binary.BinaryBlob
import net.rsprox.proxy.binary.BinaryHeader
import net.rsprox.proxy.worlds.World

public val WORLD_ATTRIBUTE: AttributeKey<World> = AttributeKey.newInstance("proxy_world")
public val STREAM_CIPHER_PAIR: AttributeKey<StreamCipherPair> = AttributeKey.newInstance("stream_cipher_pair")
public val SESSION_ENCODE_SEED: AttributeKey<IntArray> = AttributeKey.newInstance("session_encode_seed")
public val BINARY_HEADER_BUILDER: AttributeKey<BinaryHeader.Builder> =
    AttributeKey.newInstance("binary_header_builder")
public val BINARY_BLOB: AttributeKey<BinaryBlob> = AttributeKey.newInstance("binary_blob")
public val INCOMING_BANK_PIN: AttributeKey<Boolean> = AttributeKey.newInstance("incoming_bank_pin")

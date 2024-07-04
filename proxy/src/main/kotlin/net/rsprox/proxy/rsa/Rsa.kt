@file:Suppress("MemberVisibilityCanBePrivate")

package net.rsprox.proxy.rsa

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import org.bouncycastle.asn1.DERNull
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.asn1.pkcs.RSAPrivateKey
import org.bouncycastle.asn1.pkcs.RSAPublicKey
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters
import org.bouncycastle.crypto.params.RSAKeyParameters
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters
import org.bouncycastle.crypto.util.PrivateKeyInfoFactory
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory
import org.bouncycastle.util.BigIntegers
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemReader
import org.bouncycastle.util.io.pem.PemWriter
import java.io.BufferedWriter
import java.io.IOException
import java.io.Reader
import java.math.BigInteger
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.FileAttribute
import java.security.SecureRandom
import java.security.spec.KeySpec
import java.security.spec.RSAPrivateCrtKeySpec

/**
 * RSA key generation utility functions. This entire file was copied from OpenRS2,
 * along with all the extra necessary functions.
 */
public val RSAPrivateCrtKeyParameters.publicKey: RSAKeyParameters
    get() = RSAKeyParameters(false, modulus, publicExponent)

private fun ByteBuf.toBigInteger(): BigInteger {
    val bytes = ByteBufUtil.getBytes(this, readerIndex(), readableBytes(), false)
    return BigInteger(bytes)
}

private fun BigInteger.toByteBuf(): ByteBuf {
    return Unpooled.wrappedBuffer(toByteArray())
}

public fun ByteBuf.rsa(key: RSAKeyParameters): ByteBuf {
    return Rsa.apply(toBigInteger(), key).toByteBuf()
}

public fun RSAPrivateCrtKeyParameters.toKeySpec(): KeySpec {
    return RSAPrivateCrtKeySpec(modulus, publicExponent, exponent, p, q, dp, dq, qInv)
}

public object Rsa {
    private const val PUBLIC_KEY = "PUBLIC KEY"
    private const val PRIVATE_KEY = "PRIVATE KEY"

    public val PUBLIC_EXPONENT: BigInteger = BigInteger("10001", 16)

    private val threadLocal = ThreadLocal.withInitial { SecureRandom() }

    public val secureRandom: SecureRandom
        get() = threadLocal.get()

    /*
     * The client writes the output of RSA to a 128 byte buffer. It prefixes
     * the output with a single length byte, leaving 127 bytes for the actual
     * RSA output.
     *
     * The maximum output length of RSA encryption is the key size plus one, so
     * the maximum key size supported by the client is 126 bytes - or 1008 bits.
     */
    public const val CLIENT_KEY_LENGTH: Int = 1008

    public const val JAR_KEY_LENGTH: Int = 2048

    // 1 in 2^80
    private const val CERTAINTY = 80

    /*
     * The magic number prepended as a byte to the plaintext before it is
     * encrypted by the server.
     */
    public const val MAGIC: Int = 10

    public fun generateKeyPair(length: Int): Pair<RSAKeyParameters, RSAPrivateCrtKeyParameters> {
        val generator = RSAKeyPairGenerator()
        generator.init(RSAKeyGenerationParameters(PUBLIC_EXPONENT, secureRandom, length, CERTAINTY))

        val keyPair = generator.generateKeyPair()
        return Pair(keyPair.public as RSAKeyParameters, keyPair.private as RSAPrivateCrtKeyParameters)
    }

    private fun generateBlindingFactor(m: BigInteger): Pair<BigInteger, BigInteger> {
        val max = m - BigInteger.ONE

        while (true) {
            val r = BigIntegers.createRandomInRange(BigInteger.ONE, max, secureRandom)
            val rInv =
                try {
                    r.modInverse(m)
                } catch (ex: ArithmeticException) {
                    continue
                }
            return Pair(r, rInv)
        }
    }

    public fun apply(
        ciphertext: BigInteger,
        key: RSAKeyParameters,
    ): BigInteger {
        if (key is RSAPrivateCrtKeyParameters) {
            // blind the input
            val e = key.publicExponent
            val m = key.modulus
            val (r, rInv) = generateBlindingFactor(m)

            val blindCiphertext = (r.modPow(e, m) * ciphertext).mod(m)

            // decrypt using the Chinese Remainder Theorem
            val p = key.p
            val q = key.q
            val dP = key.dp
            val dQ = key.dq
            val qInv = key.qInv

            val mP = (blindCiphertext.mod(p)).modPow(dP, p)
            val mQ = (blindCiphertext.mod(q)).modPow(dQ, q)

            val h = (qInv * (mP - mQ)).mod(p)

            val blindPlaintext = (h * q) + mQ

            // unblind output
            val plaintext = (blindPlaintext * rInv).mod(m)

            // defend against CRT faults (see https://people.redhat.com/~fweimer/rsa-crt-leaks.pdf)
            check(plaintext.modPow(e, m) == ciphertext)

            return plaintext
        } else {
            return ciphertext.modPow(key.exponent, key.modulus)
        }
    }

    public fun readPublicKey(path: Path): RSAKeyParameters {
        Files.newBufferedReader(path).use { reader ->
            return readPublicKey(reader)
        }
    }

    public fun readPublicKey(reader: Reader): RSAKeyParameters {
        val der = readSinglePemObject(reader, PUBLIC_KEY)

        val spki = SubjectPublicKeyInfo.getInstance(der)
        validateAlgorithm(spki.algorithm)

        val key = RSAPublicKey.getInstance(spki.parsePublicKey())
        return RSAKeyParameters(false, key.modulus, key.publicExponent)
    }

    public fun writePublicKey(
        path: Path,
        key: RSAKeyParameters,
    ) {
        val spki = SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(key)
        return writeSinglePemObject(path, PUBLIC_KEY, spki.encoded)
    }

    public fun readPrivateKey(path: Path): RSAPrivateCrtKeyParameters {
        Files.newBufferedReader(path).use { reader ->
            return readPrivateKey(reader)
        }
    }

    public fun readPrivateKey(reader: Reader): RSAPrivateCrtKeyParameters {
        val der = readSinglePemObject(reader, PRIVATE_KEY)

        val pki = PrivateKeyInfo.getInstance(der)
        validateAlgorithm(pki.privateKeyAlgorithm)

        val key = RSAPrivateKey.getInstance(pki.parsePrivateKey())
        return RSAPrivateCrtKeyParameters(
            key.modulus,
            key.publicExponent,
            key.privateExponent,
            key.prime1,
            key.prime2,
            key.exponent1,
            key.exponent2,
            key.coefficient,
        )
    }

    public fun writePrivateKey(
        path: Path,
        key: RSAKeyParameters,
    ) {
        val pki = PrivateKeyInfoFactory.createPrivateKeyInfo(key)
        return writeSinglePemObject(path, PRIVATE_KEY, pki.encoded)
    }

    private fun validateAlgorithm(id: AlgorithmIdentifier) {
        if (id.algorithm != PKCSObjectIdentifiers.rsaEncryption) {
            throw IOException("Invalid algorithm identifier, expecting rsaEncryption")
        }

        if (id.parameters != DERNull.INSTANCE) {
            throw IOException("Invalid algorithm parameters, expecting NULL")
        }
    }

    private fun readSinglePemObject(
        reader: Reader,
        type: String,
    ): ByteArray {
        val pemReader = PemReader(reader)

        val obj = pemReader.readPemObject()
        if (obj == null || obj.type != type || pemReader.readPemObject() != null) {
            throw IOException("Expecting single $type PEM object")
        }

        if (obj.headers.isNotEmpty()) {
            throw IOException("PEM headers unsupported")
        }

        return obj.content
    }

    private fun writeSinglePemObject(
        path: Path,
        type: String,
        content: ByteArray,
    ) {
        path.useAtomicBufferedWriter { writer ->
            PemWriter(writer).use {
                it.writeObject(PemObject(type, content))
            }
        }
    }

    public inline fun <T> Path.atomicWrite(
        sync: Boolean = true,
        f: (Path) -> T,
    ): T {
        parent.useTempFile(".$fileName", ".tmp") { tempFile ->
            val result = f(tempFile)
            if (sync) {
                tempFile.fsync()
            }

            Files.move(tempFile, this, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)

            if (sync) {
                try {
                    parent.fsync()
                } catch (ex: IOException) {
                    // can't fsync directories on (at least) Windows and jimfs
                }
            }

            return result
        }
    }

    public fun Path.fsync() {
        require(Files.isRegularFile(this) || Files.isDirectory(this))

        FileChannel.open(this, StandardOpenOption.READ).use { channel ->
            channel.force(true)
        }
    }

    public inline fun <T> Path.useTempFile(
        prefix: String? = null,
        suffix: String? = null,
        vararg attributes: FileAttribute<*>,
        f: (Path) -> T,
    ): T {
        val tempFile = Files.createTempFile(this, prefix, suffix, *attributes)
        try {
            return f(tempFile)
        } finally {
            Files.deleteIfExists(tempFile)
        }
    }

    public inline fun <T> Path.useAtomicBufferedWriter(
        vararg options: OpenOption,
        sync: Boolean = true,
        f: (BufferedWriter) -> T,
    ): T {
        return atomicWrite(sync) { path ->
            Files.newBufferedWriter(path, *options).use { writer ->
                f(writer)
            }
        }
    }
}

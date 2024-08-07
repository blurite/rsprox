package net.rsprox.patch.runelite

import com.github.michaelbull.logging.InlineLogger
import net.lingala.zip4j.ZipFile
import net.rsprox.patch.PatchResult
import net.rsprox.patch.Patcher
import java.io.File
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.copyTo
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.pathString
import kotlin.io.path.readBytes
import kotlin.io.path.readText
import kotlin.io.path.writeBytes
import kotlin.io.path.writeText

@Suppress("DuplicatedCode", "SameParameterValue")
public class RuneLitePatcher : Patcher<Unit> {
    public fun patch(
        path: Path,
        rsa: String,
        port: Int,
    ): PatchResult {
        return patch(
            path,
            rsa,
            "",
            "",
            port,
            Unit,
        )
    }

    @OptIn(ExperimentalPathApi::class)
    override fun patch(
        path: Path,
        rsa: String,
        javConfigUrl: String,
        worldListUrl: String,
        port: Int,
        metadata: Unit,
    ): PatchResult {
        if (!path.isRegularFile(LinkOption.NOFOLLOW_LINKS)) {
            throw IllegalArgumentException("Path $path does not point to a file.")
        }
        logger.debug { "Attempting to patch $path" }
        val time = System.currentTimeMillis()
        val outputFolder = path.parent.resolve("runelite-injected-client-$time")
        val oldModulus: String
        val patchedJar: Path
        try {
            logger.debug { "Attempting to patch a jar." }
            Files.createDirectories(outputFolder)
            ZipFile(path.toFile()).use { inputFile ->
                logger.debug { "Extracting existing classes from a zip file." }
                inputFile.extractAll(outputFolder.toFile().absolutePath)

                logger.debug { "Patching class files." }
                oldModulus = overwriteModulus(outputFolder, rsa)
                overwriteLocalHost(outputFolder)
                patchPort(outputFolder, port)
                patchedJar = path.parent.resolve(path.nameWithoutExtension + "-patched." + path.extension)
                ZipFile(patchedJar.toFile()).use { outputFile ->
                    val parentDir = outputFolder.toFile()
                    val files = parentDir.walkTopDown().maxDepth(1)
                    logger.debug { "Building a patched jar." }
                    for (file in files) {
                        if (file == parentDir) continue
                        if (file.isFile) {
                            outputFile.addFile(file)
                        } else {
                            outputFile.addFolder(file)
                        }
                    }
                    outputFile.charset = inputFile.charset
                }
            }
        } finally {
            logger.debug { "Deleting temporary extracted class files." }
            outputFolder.deleteRecursively()
        }
        logger.debug { "Jar patching complete." }
        return PatchResult.Success(
            oldModulus,
            patchedJar,
        )
    }

    @OptIn(ExperimentalStdlibApi::class)
    public fun sha256Hash(bytes: ByteArray): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        messageDigest.update(bytes)
        return messageDigest.digest().toHexString(HexFormat.UpperCase)
    }

    @OptIn(ExperimentalPathApi::class)
    public fun patchLocalHostSupport(path: Path): Path {
        val inputPath = path.parent.resolve(path.nameWithoutExtension + "-patched." + path.extension)
        val configurationPath = Path(System.getProperty("user.home"), ".rsprox")
        val runelitePath = configurationPath.resolve("runelite")
        val shaPath = runelitePath.resolve("latest-runelite.sha256")
        val existingClient = runelitePath.resolve("latest-runelite.jar")
        val currentSha256 = sha256Hash(path.readBytes())
        if (shaPath.exists(LinkOption.NOFOLLOW_LINKS) &&
            existingClient.exists(LinkOption.NOFOLLOW_LINKS)
        ) {
            val existingSha256 = shaPath.readText(Charsets.UTF_8)
            if (existingSha256 == currentSha256) {
                logger.debug { "Using cached runelite-client as sha-256 matches" }
                inputPath.writeBytes(existingClient.readBytes())
                return inputPath
            }
        }
        val copy = path.copyTo(inputPath)
        val time = System.currentTimeMillis()
        val outputFolder = path.parent.resolve("runelite-client-$time")
        try {
            ZipFile(copy.toFile()).use { inputFile ->
                inputFile.extractAll(outputFolder.toFile().absolutePath)
                val clientLoader =
                    outputFolder
                        .resolve("net")
                        .resolve("runelite")
                        .resolve("client")
                        .resolve("rs")
                        .resolve("ClientLoader.class")
                        .toFile()
                val bytes = clientLoader.readBytes()
                val replacement = replaceText(bytes, ".jagex.com", "")
                clientLoader.writeBytes(replacement)

                val patchedJar =
                    path.parent
                        .resolve(path.nameWithoutExtension + "-patched." + path.extension)
                ZipFile(patchedJar.toFile()).use { outputFile ->
                    val parentDir = outputFolder.toFile()
                    val metaInf = parentDir.resolve("META-INF")
                    metaInf.resolve("MANIFEST.MF").delete()
                    metaInf.resolve("RL.RSA").delete()
                    metaInf.resolve("RL.SF").delete()

                    replaceClass(
                        parentDir
                            .resolve("net")
                            .resolve("runelite")
                            .resolve("client")
                            .resolve("game")
                            .resolve("WorldClient.class"),
                        "Original WorldClient.class",
                        "WorldClient.class",
                    )

                    replaceClass(
                        parentDir
                            .resolve("net")
                            .resolve("runelite")
                            .resolve("client")
                            .resolve("RuneLite.class"),
                        "Original RuneLite.class",
                        "RuneLite.class",
                    )

                    val files = parentDir.walkTopDown().maxDepth(1)
                    logger.debug { "Building a patched jar." }
                    for (file in files) {
                        if (file == parentDir) continue
                        if (file.isFile) {
                            outputFile.addFile(file)
                        } else {
                            outputFile.addFolder(file)
                        }
                    }
                    outputFile.charset = inputFile.charset
                }
                val jarFile = patchedJar.toFile()
                sign(patchedJar)
                jarFile.copyTo(existingClient.toFile())
                shaPath.writeText(currentSha256, Charsets.UTF_8)
            }
        } finally {
            outputFolder.deleteRecursively()
        }
        return copy
    }

    @OptIn(ExperimentalPathApi::class)
    public fun patchRuneLiteApi(path: Path): Path {
        val inputPath = path.parent.resolve(path.nameWithoutExtension + "-patched." + path.extension)
        val copy = path.copyTo(inputPath)
        val time = System.currentTimeMillis()
        val outputFolder = path.parent.resolve("runelite-api-$time")
        try {
            ZipFile(copy.toFile()).use { inputFile ->
                inputFile.extractAll(outputFolder.toFile().absolutePath)
                val patchedJar =
                    path.parent
                        .resolve(path.nameWithoutExtension + "-patched." + path.extension)
                ZipFile(patchedJar.toFile()).use { outputFile ->
                    val parentDir = outputFolder.toFile()

                    writeFile("Varbits.class", parentDir)
                    writeFile("VarPlayer.class", parentDir)
                    writeFile("VarClientInt.class", parentDir)
                    writeFile("VarClientStr.class", parentDir)

                    val files = parentDir.walkTopDown().maxDepth(1)
                    logger.debug { "Building a patched API jar." }
                    for (file in files) {
                        if (file == parentDir) continue
                        if (file.isFile) {
                            outputFile.addFile(file)
                        } else {
                            outputFile.addFolder(file)
                        }
                    }
                    outputFile.charset = inputFile.charset
                }
            }
        } finally {
            outputFolder.deleteRecursively()
        }
        return copy
    }

    private fun sign(path: Path) {
        val signer =
            Path(System.getProperty("java.home"))
                .resolve("bin")
                .resolve("jarsigner")
        val fakeCertificate =
            Path(System.getProperty("user.home"))
                .resolve(".rsprox")
                .resolve("signkey")
                .resolve("fake-cert.jks")
        val processBuilder =
            ProcessBuilder()
                .command(
                    listOf(
                        signer.pathString,
                        "-keystore",
                        fakeCertificate.pathString,
                        "-storepass",
                        "123456",
                        path.pathString,
                        "test",
                    ),
                )
        processBuilder
            .inheritIO()
            .start()
            .waitFor()
    }

    private fun writeFile(
        name: String,
        folder: File,
    ) {
        // In order for developer mode to work, we must re-add the Var*.class that
        // RuneLite excludes from the API unless building from source
        val classByteArray =
            RuneLitePatcher::class.java
                .getResourceAsStream(name)
                ?.readAllBytes()
                ?: throw IllegalStateException("$name resource not available.")

        val apiDirectory =
            folder
                .toPath()
                .resolve("net")
                .resolve("runelite")
                .resolve("api")
        Files.createDirectories(apiDirectory)
        val classPath = apiDirectory.resolve(name)
        classPath.writeBytes(classByteArray)
    }

    private fun replaceClass(
        classFile: File,
        originalResource: String,
        replacementResource: String,
    ) {
        val replacementResourceFile =
            RuneLitePatcher::class.java
                .getResourceAsStream(replacementResource)
                ?.readAllBytes()
                ?: throw IllegalStateException("$replacementResource resource not available")

        val originalResourceFile =
            RuneLitePatcher::class.java
                .getResourceAsStream(originalResource)
                ?.readAllBytes()
                ?: throw IllegalStateException("$originalResource resource not available.")

        val originalBytes = classFile.readBytes()
        if (!originalBytes.contentEquals(originalResourceFile)) {
            throw IllegalStateException("Unable to patch RuneLite $replacementResource - out of date.")
        }

        // Overwrite the WorldClient.class file to read worlds from our proxied-list
        // This ensures that the world switcher still goes through the proxy tool,
        // instead of just connecting to RuneLite's own world list API.
        classFile.writeBytes(replacementResourceFile)
    }

    private fun patchPort(
        outputFolder: Path,
        port: Int,
    ) {
        val inputPort = toByteArray(listOf(3, 0, 0, 43594 ushr 8 and 0xFF, 43594 and 0xFF))
        val outputPort = toByteArray(listOf(3, 0, 0, port ushr 8 and 0xFF, port and 0xFF))
        for (file in outputFolder.toFile().walkTopDown()) {
            if (!file.isFile) continue
            val bytes = file.readBytes()
            val index = bytes.indexOf(inputPort)
            if (index == -1) {
                continue
            }
            logger.debug { "Patching port from 43594 to $port in ${file.name}" }
            bytes.replaceBytes(inputPort, outputPort)
            file.writeBytes(bytes)
        }
    }

    private fun toByteArray(list: List<Int>): ByteArray {
        return list.map(Int::toByte).toByteArray()
    }

    private fun ByteArray.replaceBytes(
        input: ByteArray,
        output: ByteArray,
    ) {
        val index = indexOf(input)
        check(index != -1) {
            "Unable to find byte sequence: ${input.contentToString()}"
        }
        overwrite(index, output)
    }

    private fun ByteArray.overwrite(
        index: Int,
        replacement: ByteArray,
    ) {
        for (i in replacement.indices) {
            this[i + index] = replacement[i]
        }
    }

    private fun overwriteModulus(
        outputFolder: Path,
        rsa: String,
    ): String {
        for (file in outputFolder.toFile().walkTopDown()) {
            if (!file.isFile) continue
            val bytes = file.readBytes()
            val index = bytes.indexOf("10001".toByteArray(Charsets.UTF_8))
            if (index == -1) {
                continue
            }
            logger.debug { "Attempting to patch modulus in class ${file.name}" }
            val (replacementBytes, oldModulus) =
                patchModulus(
                    bytes,
                    rsa,
                )
            file.writeBytes(replacementBytes)
            return oldModulus
        }
        throw IllegalStateException("Unable to find modulus.")
    }

    private fun overwriteLocalHost(outputFolder: Path) {
        for (file in outputFolder.toFile().walkTopDown()) {
            if (!file.isFile) continue
            val bytes = file.readBytes()
            val index = bytes.indexOf("127.0.0.1".toByteArray(Charsets.UTF_8))
            if (index == -1) continue
            logger.debug { "Patching localhost in file ${file.name}." }
            val new = patchLocalhost(bytes)
            file.writeBytes(new)
            return
        }
        throw IllegalStateException("Unable to find localhost.")
    }

    private fun patchModulus(
        bytes: ByteArray,
        replacement: String,
    ): Pair<ByteArray, String> {
        val sliceIndices =
            bytes.firstSliceIndices(0, 256) {
                isHex(it.toInt().toChar())
            }
        check(!isHex(bytes[sliceIndices.first - 1].toInt().toChar()))
        check(!isHex(bytes[sliceIndices.last + 1].toInt().toChar()))
        val slice = bytes.sliceArray(sliceIndices)
        val oldModulus = slice.toString(Charsets.UTF_8)
        val newModulus = replacement.toByteArray(Charsets.UTF_8)
        if (newModulus.size > slice.size) {
            throw IllegalStateException("New modulus cannot be larger than the old.")
        }
        val output = bytes.setString(sliceIndices.first, replacement)

        logger.debug { "Patched RSA modulus" }
        logger.debug { "Old modulus: $oldModulus" }
        logger.debug { "New modulus: $replacement" }
        return output to oldModulus
    }

    private fun patchLocalhost(bytes: ByteArray): ByteArray {
        // Rather than only accept the localhost below
        val searchInput = "127.0.0.1"
        // Due to the Java client using "endsWith" function, we can't set any string here
        val replacement = ""

        val newSet = replaceText(bytes, searchInput, replacement)
        logger.debug { "Replaced localhost from $searchInput to $replacement" }
        return newSet
    }

    private fun replaceText(
        bytes: ByteArray,
        input: String,
        replacement: String,
    ): ByteArray {
        require(replacement.length <= input.length) {
            "Replacement string cannot be longer than the input"
        }
        val searchBytes = input.toByteArray(Charsets.UTF_8)
        val index = bytes.indexOf(searchBytes)
        if (index == -1) {
            throw IllegalArgumentException("Unable to locate input $input")
        }
        return bytes.setString(index, replacement)
    }

    private fun ByteArray.setString(
        stringStartIndex: Int,
        replacementString: String,
    ): ByteArray {
        val oldLenByte1 = this[stringStartIndex - 2].toInt() and 0xFF
        val oldLenByte2 = this[stringStartIndex - 1].toInt() and 0xFF
        val oldLength = (oldLenByte1 shl 8) or oldLenByte2
        val lengthDelta = replacementString.length - oldLength
        val replacement = ByteArray(size + lengthDelta)

        // Fill in the bytes right up until the length of the string (unmodified)
        copyInto(replacement, 0, 0, stringStartIndex - 2)

        // Fill in the length of the replacement string
        check(replacementString.length in 0..<0xFFFF)
        val newSizeByte1 = replacementString.length ushr 8 and 0xFF
        val newSizeByte2 = replacementString.length and 0xFF
        replacement[stringStartIndex - 2] = newSizeByte1.toByte()
        replacement[stringStartIndex - 1] = newSizeByte2.toByte()

        // Fill in the actual replacement string itself
        val replacementBytes = replacementString.toByteArray(Charsets.UTF_8)
        for (i in replacementBytes.indices) {
            replacement[stringStartIndex + i] = replacementBytes[i]
        }

        // Fill in the trailing bytes that come after the string (unmodified)
        copyInto(
            replacement,
            stringStartIndex + replacementString.length,
            stringStartIndex + oldLength,
        )
        return replacement
    }

    private fun ByteArray.firstSliceIndices(
        startIndex: Int,
        length: Int = -1,
        condition: (Byte) -> Boolean,
    ): IntRange {
        var start = startIndex
        val size = this.size
        while (true) {
            // First locate the starting index where a byte is being accepted
            while (start < size) {
                val byte = this[start]
                if (condition(byte)) {
                    break
                }
                start++
            }
            var end = start + 1
            // Now find the end index where a byte is not being accepted
            while (end < size) {
                val byte = this[end]
                if (!condition(byte)) {
                    break
                }
                end++
            }
            if (length != -1 && end - start < length) {
                start = end
                continue
            }
            return start..<end
        }
    }

    private fun ByteArray.indexOf(
        search: ByteArray,
        startIndex: Int = 0,
    ): Int {
        require(search.isNotEmpty()) {
            "Bytes to search are empty"
        }
        require(startIndex >= 0) {
            "Start index is negative"
        }
        var matchOffset = 0
        var start = startIndex
        var offset = startIndex
        val size = size
        while (offset < size) {
            if (this[offset] == search[matchOffset]) {
                if (matchOffset++ == 0) {
                    start = offset
                }
                if (matchOffset == search.size) {
                    return start
                }
            } else {
                matchOffset = 0
            }
            offset++
        }
        return -1
    }

    private fun isHex(char: Char): Boolean {
        return char in lowercaseHexStringCharRange ||
            char in uppercaseHexStringCharRange ||
            char in hexDigitsCharRange
    }

    private companion object {
        private val lowercaseHexStringCharRange = 'a'..'f'
        private val uppercaseHexStringCharRange = 'A'..'F'
        private val hexDigitsCharRange = '0'..'9'
        private val logger = InlineLogger()
    }
}

package net.rsprox.proxy.preferences

import io.netty.buffer.Unpooled
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.Path
import kotlin.io.path.notExists

public data class ClientPreferences(
    public val removeRoof: Boolean = false,
    public val muteTitleScreen: Boolean = false,
    public val windowMode: Int = 1,
    public val totpPassCodes: Map<Int, Int> = emptyMap(),
    public val cachedUserName: String? = null,
    public val hideUserName: Boolean = false,
    public val brightness: Double = 0.8,
    public val midiVolume: Int = 127,
    public val waveVolume: Int = 127,
    public val ambientVolume: Int = 127,
    public val acceptedTOSVersion: Int = 0,
    public val displayFps: Boolean = false,
    public val fpsLimit: Int = 25,
    public val drawDistance: Int = 0,
    public val isSfx8Bit: Boolean = false,
) {
    public fun encode(): ByteArray {
        require(totpPassCodes.size <= UBYTE_MAX) {
            "TOTP pass code count cannot exceed $UBYTE_MAX: ${totpPassCodes.size}"
        }
        requireUByte("windowMode", windowMode)
        requireUByte("brightness", brightnessByte)
        requireUByte("midiVolume", midiVolume)
        requireUByte("waveVolume", waveVolume)
        requireUByte("ambientVolume", ambientVolume)
        requireUByte("acceptedTOSVersion", acceptedTOSVersion)
        requireUByte("drawDistance", drawDistance)

        val buffer = Unpooled.buffer(PREFERENCES_BUFFER_CAPACITY).toJagByteBuf()
        buffer.p1(VERSION)
        buffer.p1(if (removeRoof) 1 else 0)
        buffer.p1(if (muteTitleScreen) 1 else 0)
        buffer.p1(windowMode)
        buffer.p1(totpPassCodes.size)
        for ((key, value) in totpPassCodes) {
            buffer.p4(key)
            buffer.p4(value)
        }
        buffer.pjstr(cachedUserName ?: "")
        buffer.pboolean(hideUserName)
        buffer.p1(brightnessByte)
        buffer.p1(midiVolume)
        buffer.p1(waveVolume)
        buffer.p1(ambientVolume)
        buffer.p1(acceptedTOSVersion)
        buffer.p1(if (displayFps) 1 else 0)
        buffer.p4(fpsLimit)
        buffer.p1(drawDistance)
        buffer.p1(if (isSfx8Bit) 1 else 0)

        val bytes = ByteArray(buffer.writerIndex())
        buffer.buffer.getBytes(0, bytes)
        return bytes
    }

    public fun withCustomPreferences(): ClientPreferences {
        return copy(
            removeRoof = true,
            muteTitleScreen = true,
            windowMode = 2,
            acceptedTOSVersion = 1,
        )
    }

    private val brightnessByte: Int
        get() = (brightness * 100.0).toInt()

    public companion object {
        public const val VERSION: Int = 12
        private const val UBYTE_MAX: Int = 0xFF
        private const val PREFERENCES_BUFFER_CAPACITY: Int = 419

        public fun decode(bytes: ByteArray): ClientPreferences {
            if (bytes.isEmpty()) {
                return ClientPreferences()
            }
            return decode(Unpooled.wrappedBuffer(bytes).toJagByteBuf())
        }

        public fun decode(buffer: JagByteBuf): ClientPreferences {
            val version = buffer.g1()
            if (version !in 0..VERSION) {
                return ClientPreferences()
            }

            val removeRoof = buffer.g1() == 1
            val muteTitleScreen =
                if (version > 1) {
                    buffer.g1() == 1
                } else {
                    false
                }
            val windowMode =
                if (version > 3) {
                    buffer.g1()
                } else {
                    1
                }
            val totpPassCodes =
                if (version > 2) {
                    val count = buffer.g1()
                    buildMap(count) {
                        repeat(count) {
                            put(buffer.g4(), buffer.g4())
                        }
                    }
                } else {
                    emptyMap()
                }
            val cachedUserName =
                if (version > 4) {
                    buffer.gjstrnull()
                } else {
                    null
                }
            val hideUserName =
                if (version > 5) {
                    buffer.gboolean()
                } else {
                    false
                }
            val brightness: Double
            val midiVolume: Int
            val waveVolume: Int
            val ambientVolume: Int
            if (version > 6) {
                brightness = buffer.g1() / 100.0
                midiVolume = buffer.g1()
                waveVolume = buffer.g1()
                ambientVolume = buffer.g1()
            } else {
                brightness = 0.8
                midiVolume = 127
                waveVolume = 127
                ambientVolume = 127
            }
            val acceptedTOSVersion =
                if (version > 7) {
                    buffer.g1()
                } else {
                    0
                }
            val displayFps =
                if (version > 8) {
                    buffer.g1() == 1
                } else {
                    false
                }
            val fpsLimit =
                if (version > 9) {
                    buffer.g4()
                } else {
                    25
                }
            val drawDistance =
                if (version > 10) {
                    buffer.g1()
                } else {
                    0
                }
            val isSfx8Bit =
                if (version > 11) {
                    buffer.g1() == 1
                } else {
                    false
                }

            return ClientPreferences(
                removeRoof = removeRoof,
                muteTitleScreen = muteTitleScreen,
                windowMode = windowMode,
                totpPassCodes = totpPassCodes,
                cachedUserName = cachedUserName,
                hideUserName = hideUserName,
                brightness = brightness,
                midiVolume = midiVolume,
                waveVolume = waveVolume,
                ambientVolume = ambientVolume,
                acceptedTOSVersion = acceptedTOSVersion,
                displayFps = displayFps,
                fpsLimit = fpsLimit,
                drawDistance = drawDistance,
                isSfx8Bit = isSfx8Bit,
            )
        }

        private fun requireUByte(
            field: String,
            value: Int,
        ) {
            require(value in 0..UBYTE_MAX) {
                "$field must be between 0 and $UBYTE_MAX: $value"
            }
        }
    }
}

public data object ClientPreferencesFile {
    public val DEFAULT_PATH: Path =
        Path(
            System.getProperty("user.home"),
            ".rlcustom",
            "jagexcache",
            "oldschool",
            "LIVE",
            "preferences.dat",
        )

    public fun load(path: Path = DEFAULT_PATH): ClientPreferences? {
        if (path.notExists(LinkOption.NOFOLLOW_LINKS)) {
            return null
        }
        return ClientPreferences.decode(Files.readAllBytes(path))
    }

    public fun loadOrDefaultCustom(path: Path = DEFAULT_PATH): ClientPreferences {
        return load(path) ?: ClientPreferences().withCustomPreferences()
    }

    public fun write(
        preferences: ClientPreferences,
        path: Path = DEFAULT_PATH,
    ) {
        val parent = path.parent
        if (parent != null) {
            Files.createDirectories(parent)
        }
        Files.write(
            path,
            preferences.encode(),
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE,
            StandardOpenOption.SYNC,
        )
    }
}

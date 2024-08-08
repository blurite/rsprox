package net.rsprox.proxy.binary.credentials

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.proxy.config.BINARY_CREDENTIALS
import java.nio.file.Path

public class BinaryCredentialsStore(
    credentialsList: List<BinaryCredentials>,
) {
    public constructor() : this(emptyList())

    private val credentialsList: MutableList<BinaryCredentials> = credentialsList.toMutableList()

    public fun append(credentials: BinaryCredentials) {
        if (credentials in this.credentialsList) {
            return
        }
        logger.info {
            "Updating binary credentials for name '${credentials.displayName}'"
        }
        this.credentialsList += credentials
        write()
    }

    private fun write(path: Path = BINARY_CREDENTIALS) {
        val stream = BinaryCredentialsStream(path)
        stream.write(credentialsList)
    }

    public companion object {
        private val logger = InlineLogger()

        public fun read(path: Path = BINARY_CREDENTIALS): BinaryCredentialsStore {
            logger.debug {
                "Parsing binary credentials from $path"
            }
            val stream = BinaryCredentialsStream(path)
            val credentials = stream.parse()
            return BinaryCredentialsStore(credentials)
        }
    }
}

package net.rsprox.mcpbridge;

/** A failure reported to rsprox as {@code err}. Codes: bad_args, not_found, wrong_state, timeout, internal. */
final class BridgeException extends Exception {
    /** The code that tells rsprox what kind of failure this is. */
    final String code;

    /** Create a failure with the given code and message. */
    BridgeException(String code, String message) {
        super(message);
        this.code = code;
    }
}

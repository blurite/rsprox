package net.rsprox.mcpbridge;

/** A failure reported to rsprox as {@code err}. Codes: bad_args, not_found, wrong_state, timeout, internal. */
final class BridgeException extends Exception {
    final String code;

    BridgeException(String code, String message) {
        super(message);
        this.code = code;
    }
}

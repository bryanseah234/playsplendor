package com.splendor.exception;

/**
 * Exception indicating a failure during network operations in multiplayer mode.
 *
 * <p>Common causes:
 * <ul>
 *   <li>Socket connection failures (server unreachable, connection refused)</li>
 *   <li>Network communication errors (connection dropped, I/O failure)</li>
 *   <li>Protocol violations (malformed messages, unexpected message types)</li>
 *   <li>Timeout exceeded while waiting for client responses</li>
 * </ul>
 *
 * @see com.splendor.network.ServerSocketHandler
 * @see com.splendor.network.ClientHandler
 */
public class NetworkException extends SplendorException {

    /**
     * Constructs a new NetworkException with the specified detail message.
     *
     * @param message The detail message
     */
    public NetworkException(final String message) {
        super(message);
    }

    /**
     * Constructs a new NetworkException with the specified detail message and cause.
     *
     * @param message The detail message
     * @param cause The underlying cause
     */
    public NetworkException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

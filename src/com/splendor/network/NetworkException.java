/**
 * Exception thrown for network-related errors.
 * Used for connection failures, protocol violations, and network communication issues.
 */
package com.splendor.network;

import com.splendor.exception.SplendorException;

/**
 * Exception indicating a failure during network operations in multiplayer mode.
 * 
 * This exception is thrown by {@link ServerSocketHandler} and {@link ClientHandler}
 * when network communication fails. It extends {@link SplendorException} to integrate
 * with the application's unified exception handling strategy.
 * 
 * <p>Common causes:
 * <ul>
 *   <li>Socket connection failures (server unreachable, connection refused)</li>
 *   <li>Network communication errors (connection dropped, I/O failure)</li>
 *   <li>Protocol violations (malformed messages, unexpected message types)</li>
 *   <li>Timeout exceeded while waiting for client responses</li>
 * </ul>
 * 
 * <p>This exception is caught at the application boundary and converted to user-friendly
 * error messages displayed through the view layer.
 * 
 * @see ServerSocketHandler
 * @see ClientHandler
 * @see com.splendor.view.NetworkGameView
 */
public class NetworkException extends SplendorException {
    
    /**
     * Creates a new NetworkException with the specified message.
     * 
     * @param message Exception message
     */
    public NetworkException(final String message) {
        super(message);
    }
    
    /**
     * Creates a new NetworkException with the specified message and cause.
     * 
     * @param message Exception message
     * @param cause Underlying cause
     */
    public NetworkException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
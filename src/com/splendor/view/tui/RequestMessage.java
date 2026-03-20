package com.splendor.view.tui;

import com.williamcallahan.tui4j.compat.bubbletea.Message;

import java.util.concurrent.BlockingQueue;

/**
 * TUI4J Message wrapper for a TuiRequest.
 * Used to inject requests from TuiGameView into the TUI4J event loop.
 *
 * The static requestQueue reference is set once at startup by TuiGameView
 * before the Program is launched. This allows the Command lambda
 * (TuiAppModel::pollRequest) to access the queue without instance references.
 */
public record RequestMessage(TuiRequest request) implements Message {

    // Static queue reference — set once before Program.run()
    private static volatile BlockingQueue<TuiRequest> requestQueue;

    /**
     * Initializes the static queue reference. Must be called before
     * starting the TUI4J Program.
     */
    public static void setRequestQueue(final BlockingQueue<TuiRequest> queue) {
        requestQueue = queue;
    }

    /**
     * Blocking poll called from a Command worker thread.
     * Returns a RequestMessage wrapping the next TuiRequest.
     * Blocks until a request is available.
     */
    public static Message poll() {
        try {
            final TuiRequest req = requestQueue.take(); // blocking
            return new RequestMessage(req);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            return new RequestMessage(TuiRequest.close());
        }
    }
}

package com.splendor.network;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import com.splendor.exception.SplendorException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class NetworkIntegrationTest {

    private ServerSocketHandler server;
    private Thread serverThread;
    private final AtomicReference<Throwable> serverFailure = new AtomicReference<>();

    private final List<Socket> sockets = new ArrayList<>();
    private final List<BufferedReader> readers = new ArrayList<>();
    private final List<PrintWriter> writers = new ArrayList<>();

    @AfterEach
    void tearDown() {
        for (Socket socket : sockets) {
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        }

        for (PrintWriter writer : writers) {
            if (writer != null) {
                writer.close();
            }
        }

        for (BufferedReader reader : readers) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }

        if (server != null) {
            server.stopServer();
        }

        if (serverThread != null && serverThread.isAlive()) {
            try {
                serverThread.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Test
    @Timeout(30)
    void serverStartsAndAcceptsConnections() throws Exception {
        startServerInBackground();

        ClientConnection client = connectClient();

        assertEquals("Connected to Splendor server. Awaiting lobby instructions...", client.welcomeLine);
    }

    @Test
    @Timeout(30)
    void multipleClientsCanConnect() throws Exception {
        startServerInBackground();

        ClientConnection client1 = connectClient();
        ClientConnection client2 = connectClient();

        assertEquals("Connected to Splendor server. Awaiting lobby instructions...", client1.welcomeLine);
        assertEquals("Connected to Splendor server. Awaiting lobby instructions...", client2.welcomeLine);

        waitUntil(Duration.ofSeconds(5), () -> server.getConnectedClientCount() == 2,
                "Server did not report 2 connected clients");
        assertEquals(2, server.getConnectedClientCount());
    }

    @Test
    @Timeout(30)
    void serverBroadcastsToAllClients() throws Exception {
        startServerInBackground();

        ClientConnection client1 = connectClient();
        ClientConnection client2 = connectClient();

        waitUntil(Duration.ofSeconds(5), () -> server.getConnectedClientCount() == 2,
                "Expected 2 connected clients before broadcast");

        server.broadcastToAllClients("test message");

        assertEquals("test message", client1.reader.readLine());
        assertEquals("test message", client2.reader.readLine());
    }

    @Test
    @Timeout(30)
    void sendMessageToSpecificClient() throws Exception {
        startServerInBackground();

        ClientConnection client1 = connectClient();
        ClientConnection client2 = connectClient();

        waitUntil(Duration.ofSeconds(5), () -> server.getConnectedClientCount() == 2,
                "Expected 2 connected clients before targeted send");

        List<String> ids = server.getConnectedClientIds();
        assertEquals(2, ids.size());

        server.sendToClient(ids.get(0), "only-one-client");

        client1.socket.setSoTimeout(1500);
        client2.socket.setSoTimeout(1500);

        String client1Message = tryReadLine(client1);
        String client2Message = tryReadLine(client2);

        int deliveredCount = 0;
        if ("only-one-client".equals(client1Message)) {
            deliveredCount++;
        }
        if ("only-one-client".equals(client2Message)) {
            deliveredCount++;
        }

        assertEquals(1, deliveredCount, "Targeted delivery should reach exactly one client");
    }

    @Test
    @Timeout(30)
    void clientResponseHandling() throws Exception {
        startServerInBackground();

        ClientConnection client = connectClient();

        waitUntil(Duration.ofSeconds(5), () -> server.getConnectedClientCount() == 1,
                "Expected 1 connected client");

        String clientId = server.getConnectedClientIds().get(0);
        client.writer.println("simulated user input");

        String response = server.waitForClientResponse(clientId, 3000);
        assertEquals("simulated user input", response);
    }

    @Test
    @Timeout(30)
    void clientDisconnectHandling() throws Exception {
        startServerInBackground();

        ClientConnection client = connectClient();

        waitUntil(Duration.ofSeconds(5), () -> server.getConnectedClientCount() == 1,
                "Expected 1 connected client before disconnect");

        client.socket.close();

        waitUntil(Duration.ofSeconds(5), () -> server.getConnectedClientCount() == 0,
                "Server did not detect client disconnect");
        assertEquals(0, server.getConnectedClientCount());
    }

    @Test
    @Timeout(30)
    void waitForAtLeastClientsTimesOutWhenTargetNotReached() throws Exception {
        startServerInBackground();

        assertFalse(server.waitForAtLeastClients(1, 300));
    }

    private void startServerInBackground() throws InterruptedException {
        server = new ServerSocketHandler();
        serverThread = new Thread(() -> {
            try {
                server.startServer();
            } catch (SplendorException e) {
                serverFailure.set(e);
            } catch (Throwable t) {
                serverFailure.set(t);
            }
        }, "network-integration-test-server");

        serverThread.setDaemon(true);
        serverThread.start();

        Thread.sleep(500);

        waitUntil(Duration.ofSeconds(5), () -> server.getActualPort() > 0, "Server did not bind to a port");
        assertNull(serverFailure.get(), () -> "Server failed to start: " + serverFailure.get());
    }

    private ClientConnection connectClient() throws IOException {
        Socket socket = new Socket(InetAddress.getLoopbackAddress(), server.getActualPort());
        sockets.add(socket);

        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        readers.add(reader);

        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        writers.add(writer);

        String welcome = reader.readLine();

        return new ClientConnection(socket, reader, writer, welcome);
    }

    private String tryReadLine(ClientConnection client) throws IOException {
        try {
            return client.reader.readLine();
        } catch (SocketTimeoutException e) {
            return null;
        }
    }

    private void waitUntil(Duration timeout, Condition condition, String failureMessage)
            throws InterruptedException {
        long deadlineNanos = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadlineNanos) {
            if (condition.evaluate()) {
                return;
            }
            Thread.sleep(50);
        }
        fail(failureMessage);
    }

    @FunctionalInterface
    private interface Condition {
        boolean evaluate();
    }

    private static final class ClientConnection {
        private final Socket socket;
        private final BufferedReader reader;
        private final PrintWriter writer;
        private final String welcomeLine;

        private ClientConnection(Socket socket, BufferedReader reader, PrintWriter writer,
                                 String welcomeLine) {
            this.socket = socket;
            this.reader = reader;
            this.writer = writer;
            this.welcomeLine = welcomeLine;
        }
    }
}

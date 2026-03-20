package com.splendor.network;

import com.splendor.config.ConfigException;
import com.splendor.config.IConfigProvider;
import com.splendor.exception.SplendorException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

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

        assertEquals("Welcome to Splendor Network Game!", client.welcomeLine1);
        assertEquals("Commands: MOVE:action:params, QUERY:type, DISCONNECT", client.welcomeLine2);
    }

    @Test
    @Timeout(30)
    void multipleClientsCanConnect() throws Exception {
        startServerInBackground();

        ClientConnection client1 = connectClient();
        ClientConnection client2 = connectClient();

        assertEquals("Welcome to Splendor Network Game!", client1.welcomeLine1);
        assertEquals("Welcome to Splendor Network Game!", client2.welcomeLine1);

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

        String client1Id = server.getConnectedClients().stream()
                .filter(ch -> ch.getClientAddress().endsWith(":" + client1.socket.getLocalPort()))
                .findFirst()
                .map(ClientHandler::getClientId)
                .orElse(null);

        String client2Id = server.getConnectedClients().stream()
                .filter(ch -> ch.getClientAddress().endsWith(":" + client2.socket.getLocalPort()))
                .findFirst()
                .map(ClientHandler::getClientId)
                .orElse(null);

        assertNotNull(client1Id);
        assertNotNull(client2Id);
        assertTrue(ids.contains(client1Id));
        assertTrue(ids.contains(client2Id));

        server.sendToClient(client1Id, "only-client-1");

        client1.socket.setSoTimeout(1500);
        client2.socket.setSoTimeout(1500);

        String client1Message = client1.reader.readLine();
        assertEquals("only-client-1", client1Message);
        assertThrows(SocketTimeoutException.class, client2.reader::readLine);
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

        String response = server.pollClientResponse(clientId, 3000);
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
    void protocolMessageValidation() {
        assertTrue(NetworkProtocol.isValidMessage("MOVE:TAKE_3:R,G,B"));
        assertTrue(NetworkProtocol.isValidMessage("QUERY:STATE"));
        assertFalse(NetworkProtocol.isValidMessage(null));
        assertFalse(NetworkProtocol.isValidMessage(""));

        char[] tooLong = new char[NetworkProtocol.MAX_MESSAGE_LENGTH + 1];
        Arrays.fill(tooLong, 'A');
        assertFalse(NetworkProtocol.isValidMessage(new String(tooLong)));

        String created = NetworkProtocol.createMessage("MOVE", "BUY", "42");
        assertEquals("MOVE:BUY:42", created);

        String[] parsed = NetworkProtocol.parseMessage("QUERY:STATE");
        assertArrayEquals(new String[]{"QUERY", "STATE"}, parsed);
    }

    private void startServerInBackground() throws InterruptedException {
        server = new ServerSocketHandler(0, new TestConfigProvider());
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

        String welcome1 = reader.readLine();
        String welcome2 = reader.readLine();

        return new ClientConnection(socket, reader, writer, welcome1, welcome2);
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
        private final String welcomeLine1;
        private final String welcomeLine2;

        private ClientConnection(Socket socket, BufferedReader reader, PrintWriter writer,
                                 String welcomeLine1, String welcomeLine2) {
            this.socket = socket;
            this.reader = reader;
            this.writer = writer;
            this.welcomeLine1 = welcomeLine1;
            this.welcomeLine2 = welcomeLine2;
        }
    }

    private static final class TestConfigProvider implements IConfigProvider {
        @Override
        public void loadConfiguration() throws ConfigException {
        }

        @Override
        public String getStringProperty(String key, String defaultValue) {
            return defaultValue;
        }

        @Override
        public int getIntProperty(String key, int defaultValue) {
            return defaultValue;
        }

        @Override
        public boolean getBooleanProperty(String key, boolean defaultValue) {
            return defaultValue;
        }

        @Override
        public boolean hasProperty(String key) {
            return false;
        }
    }
}

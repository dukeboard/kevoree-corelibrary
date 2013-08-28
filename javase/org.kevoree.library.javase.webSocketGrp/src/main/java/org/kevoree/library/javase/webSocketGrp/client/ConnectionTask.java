package org.kevoree.library.javase.webSocketGrp.client;

import org.kevoree.log.Log;

import java.net.URI;
import java.nio.ByteBuffer;

/**
 * Thread that creates a WebSocket client to connect to the given URI.
 * When the client connection is initiated, this handler onConnectionSucceeded(WebSocketClient) method will be called.
 * Otherwise, if client's connection is closed, onConnectionClosed(WebSocketClient) method from handler will be called.
 * For each messages received by the WebSocket client, this handler onMessage(ByteBuffer) method will be called.
 * User: leiko
 * Date: 3/26/13
 * Time: 9:56 AM
 *
 */
public class ConnectionTask implements Runnable {

    private URI uri;
    private Handler handler;

    public ConnectionTask(URI uri, Handler handler) {
        this.uri = uri;
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            WebSocketClient client = new WebSocketClient(uri) {
                @Override
                public void onMessage(ByteBuffer bytes) {
                    handler.onMessage(bytes);
                }

                @Override
                public void onClose(int code, String reason, boolean flag) {
                    handler.onConnectionClosed(this);
                }
            };

            if (client.connectBlocking()) {
                handler.onConnectionSucceeded(client);
                return;
            } else {
                Log.debug("Unable to connect to {}", uri.toString());
            }
        } catch (InterruptedException e) {
            Log.debug("[STOP] ConnectionTask to {} = interrupted", uri.toString());
        }
    }

    // ===============
    //    HANDLER
    // ===============
    public interface Handler {
        void onMessage(ByteBuffer bytes);
        void onConnectionSucceeded(WebSocketClient client);
        void onConnectionClosed(WebSocketClient client);
    }
}

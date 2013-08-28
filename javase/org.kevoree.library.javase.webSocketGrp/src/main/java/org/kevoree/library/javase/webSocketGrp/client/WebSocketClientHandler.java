package org.kevoree.library.javase.webSocketGrp.client;

import org.kevoree.log.Log;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: leiko
 * Date: 3/26/13
 * Time: 9:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class WebSocketClientHandler {

    private static final long DEFAULT_LOOP_TIME = 5000;
    private long loopTime;
    private ConnectionTask.Handler handler;
    private List<ScheduledExecutorService> pool;
    private WebSocketClient connectedClient = null;

    public WebSocketClientHandler(long loopTime) {
        this.loopTime = loopTime;
        this.handler = defaultHandler;
        this.pool = Collections.synchronizedList(new ArrayList<ScheduledExecutorService>());
    }

    public WebSocketClientHandler(ConnectionTask.Handler handler) {
        this(DEFAULT_LOOP_TIME);
        this.handler = handler;
    }

    public WebSocketClientHandler() {
        this(DEFAULT_LOOP_TIME);
    }

    public void setHandler(ConnectionTask.Handler handler) {
        this.handler = handler;
    }

    public void startConnectionTasks(final List<URI> uris) {
        for (URI uri : uris) {
            final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            this.pool.add(executor);
            executor.scheduleWithFixedDelay(new ConnectionTask(uri, new ConnectionTask.Handler() {
                @Override
                public void onMessage(ByteBuffer bytes) {
                    handler.onMessage(bytes);
                }

                @Override
                public void onConnectionSucceeded(WebSocketClient client) {
                    connectedClient = client;
                    handler.onConnectionSucceeded(client);
                    stopAllTasks();
                }

                @Override
                public void onConnectionClosed(WebSocketClient client) {
                    handler.onConnectionClosed(client);
                    if (connectedClient.equals(client)) {
                        // restart connection tasks
                        startConnectionTasks(uris);
                    }
                }
            }), 0, loopTime, TimeUnit.MILLISECONDS);
        }
    }

    public void stopAllTasks() {
        for (ScheduledExecutorService executor : this.pool) {
            executor.shutdownNow();
            this.pool.remove(executor);
        }
    }

    private ConnectionTask.Handler defaultHandler = new ConnectionTask.Handler() {
        @Override
        public void onMessage(ByteBuffer bytes) {
            Log.debug("DefaultHandler: onMessage(ByteBuffer) called.");
        }

        @Override
        public void onConnectionSucceeded(WebSocketClient client) {
            Log.debug("DefaultHandler: onConnectionSucceed(WebSocketClient) called.");
        }

        @Override
        public void onConnectionClosed(WebSocketClient client) {
            Log.debug("DefaultHandler: onConnectionClosed(WebSocketClient) called.");
        }
    };
}

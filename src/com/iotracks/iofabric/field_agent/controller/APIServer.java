package com.iotracks.iofabric.field_agent.controller;

import com.iotracks.iofabric.utils.logging.LoggingService;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by josh on 6/4/15.
 */
public class APIServer {
    private HttpServer server;

    public void start() {
        int port = 12345;
        LoggingService.logInfo("CONTROLLER", "starting the http server on port " + port);
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            String base = "/api/v2";
            server.createContext(base + "/status", new RequestHandler("status"));
            server.createContext(base + "/instance/provision/key", new RequestHandler("provision"));
            server.createContext(base + "/instance/status", new RequestHandler("instance_status")); // ******************
            server.createContext(base + "/instance/config", new RequestHandler("instance_config"));
            server.createContext(base + "/instance/config/changes", new RequestHandler("send_config"));
            server.createContext(base + "/instance/changes", new RequestHandler("changes_list"));
            server.createContext(base + "/instance/containerlist", new RequestHandler("container_list"));
            server.createContext(base + "/instance/containerconfig", new RequestHandler("container_config"));
            server.createContext(base + "/instance/routing", new RequestHandler("routing"));
            server.createContext(base + "/instance/registries", new RequestHandler("registries"));
            server.setExecutor(null); // creates a default executor
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void shutdown() {
        try {
            server.stop(0);
        } catch (Exception ex) {}
    }
}

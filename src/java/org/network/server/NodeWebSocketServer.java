package org.network.server;

import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author Mateusz Kaproń
 */
@ApplicationScoped
@ServerEndpoint("/actions")
public class NodeWebSocketServer {

	private GraphBuilder graphBuilder = new GraphBuilder();

	@Inject
	private NodeSessionHandler sessionHandler;

	@OnOpen
	public void open(Session session) {
		System.out.println("Open Node session...");
		sessionHandler.addSession(session);
	}

	@OnClose
	public void close(Session session) {
		System.out.println("Close Node session...");
		sessionHandler.removeSession(session);
	}

	@OnError
	public void onError(Throwable error) {
		Logger.getLogger(NodeWebSocketServer.class.getName()).log(Level.SEVERE, null, error);
	}

	@OnMessage
	public void handleMessage(String message, Session session) throws InterruptedException {

		try (JsonReader reader = Json.createReader(new StringReader(message))) {
			JsonObject jsonMessage = reader.readObject();

			switch (jsonMessage.getString("action")) {
				case "start":
					if ("monkey".equals(jsonMessage.getString("name"))) {
						graphBuilder.buildGraph("monkey.txt", jsonMessage.getString("neurons"), sessionHandler, jsonMessage.getString("speed"));
					} else if ("test".equals(jsonMessage.getString("name"))) {
						graphBuilder.buildGraph("test.txt", "nullTest", sessionHandler, jsonMessage.getString("speed"));
					}
					break;
					
				case "startFromLog": 
					graphBuilder.buildGraph("buildGraphFromLog", jsonMessage.getString("objectParameters"), sessionHandler, jsonMessage.getString("speed"));
					break;
				case "update":
					graphBuilder.buildGraph("update", jsonMessage.getString("neurons"), sessionHandler, jsonMessage.getString("speedActive"));
					break;

				case "updateMode":
					graphBuilder.buildGraph("updateMode", jsonMessage.getString("mode"), sessionHandler, jsonMessage.getString("speed"));
					break;
				case "remove":
					break;
				case "resetLines":
					graphBuilder.buildGraph("resetGraph", "null", sessionHandler, "null");
					break;
				case "submitData":
					graphBuilder.buildGraph("submitData", jsonMessage.getString("objectParameters"), sessionHandler, jsonMessage.getString("speed"));
					break;
			}
		}
	}
}

package org.network.server;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.json.JsonObject;
import javax.json.spi.JsonProvider;
import javax.websocket.Session;
import org.network.model.Node;

/**
 *
 * @author Mateusz Kaproń
 */
@ApplicationScoped
public class NodeSessionHandler {
	
	private int nodeId = 1;
	private int sentenceId = 1000;
	private final Set sessions = new HashSet<>();
	private final Set nodes = new LinkedHashSet<>();
	
	public void addSession(Session session) {
		sessions.add(session);
		//Anty-remove refresh
//		for (Object node : nodes) {
//			JsonObject addMessage = createNodeJson((Node)node);
//			sendToSession(session, addMessage);
//		}
	}
	
	public void removeSession(Session session) {
		nodeId = 1;
		sentenceId = 1000;
		sessions.remove(session);
	}
	
	public List getNodes() {
		return new ArrayList(nodes);
	}
	
	public void addNode(Node node) {
		node.setId(nodeId);
		nodes.add(node);
		nodeId++;
		JsonObject addMessage = createNodeJson(node);
		sendToAllConnectedSessions(addMessage);
	}
	
	public void addLines(Node node, String coeffString) {
		addLinesJson(node, coeffString);
	}
	
	public void updateLines(Node node, String color) {
		JsonProvider provider = JsonProvider.provider();
		JsonObject updateNode = provider.createObjectBuilder()
				.add("action", "updateLines")
				.add("id", node.getId())
				.add("level", node.getLevel())
				.add("attribute", node.getAttribute())
				.add("name", node.getName())
				.add("color", color)
				.add("neighbours", node.getAnotherNeighboursAsString())
				.build();
		sendToAllConnectedSessions(updateNode);
	}
	
	public void updateLines(Node node, String nameOfNode, String color) {
		JsonProvider provider = JsonProvider.provider();
		JsonObject updateNode = provider.createObjectBuilder()
				.add("action", "updateLines")
				.add("id", node.getId())
				.add("level", node.getLevel())
				.add("attribute", node.getAttribute())
				.add("name", node.getName())
				.add("color", color)
				.add("neighbours", nameOfNode)
				.build();
		sendToAllConnectedSessions(updateNode);
	}
	
	public void resetLines() {
		JsonProvider provider = JsonProvider.provider();
		JsonObject updateNode = provider.createObjectBuilder()
				.add("action", "resetLines")
				.build();
		sendToAllConnectedSessions(updateNode);
	}
	
	public void resetNodes(List<Node> allNodes) {
		for (Node node: allNodes) {
			node.setActive(false);
			JsonProvider provider = JsonProvider.provider();
			JsonObject resetNode = provider.createObjectBuilder()
				.add("action", "resetNodes")
				.add("name", node.getName())
				.add("attribute", node.getAttribute())
				.build();
			sendToAllConnectedSessions(resetNode);
		}
	}
	
	public void updateBestLine(Node node) {
		JsonProvider provider = JsonProvider.provider();
		JsonObject updateNode = provider.createObjectBuilder()
				.add("action", "updateBestLines")
				.add("id", node.getId())
				.add("level", node.getLevel())
				.add("attribute", node.getAttribute())
				.add("name", node.getName())
				.add("neighbours", node.getStringBestNeighbour())
				.build();
		sendToAllConnectedSessions(updateNode);
	}
	
	public void activeNeuron(Node node) {
		JsonProvider provider = JsonProvider.provider();
		JsonObject sentenceAddMsg = provider.createObjectBuilder()
				.add("action", "activeNeuron")
				.add("id", node.getId())
				.add("level", node.getLevel())
				.add("attribute", node.getAttribute())
				.add("name", node.getName())
				.build();
		sendToAllConnectedSessions(sentenceAddMsg);
	}
	
	public void updateNode(Node node) {
		updateNodeJson(node);
	}
	
	public void updateSentence(String word, String color) {
		JsonProvider provider = JsonProvider.provider();
		JsonObject sentenceAddMsg = provider.createObjectBuilder()
				.add("action", "updateSentence")
				.add("id", sentenceId)
				.add("name", word)
				.add("color", color)
				.build();
		sendToAllConnectedSessions(sentenceAddMsg);
	}
	
	public void removeSentence() {
		JsonProvider provider = JsonProvider.provider();
		JsonObject sentenceAddMsg = provider.createObjectBuilder()
				.add("action", "removeSentence")
				.add("id", sentenceId)
				.build();
		sendToAllConnectedSessions(sentenceAddMsg);
	}
	
	public void addSentence(String sentence) {
		JsonProvider provider = JsonProvider.provider();
		sentenceId++;
		JsonObject sentenceAddMsg = provider.createObjectBuilder()
				.add("action", "addSentence")
				.add("id", sentenceId)
				.add("name", sentence)
				.build();
		sendToAllConnectedSessions(sentenceAddMsg);
	}
	
	private Node getNodeByName(String name) {
		for (Object node : nodes) {
			if ((((Node)node).getName()).equals(name)) {
				return (Node)node;
			}
		}
		return null;
	}
	
	private JsonObject createNodeJson(Node node) {
		JsonProvider provider = JsonProvider.provider();
		JsonObject addNode = provider.createObjectBuilder()
				.add("action", "add")
				.add("id", node.getId())
				.add("level", node.getLevel())
				.add("attribute", node.getAttribute())
				.add("name", node.getName())
				.add("neighbours", node.getNeighboursAsString())
				.build();
		return addNode;
	}
	
	private void addLinesJson(Node node, String coeffString) {
		JsonProvider provider = JsonProvider.provider();
		JsonObject updateNode = provider.createObjectBuilder()
				.add("action", "addLines")
				.add("id", node.getId())
				.add("level", node.getLevel())
				.add("attribute", node.getAttribute())
				.add("name", node.getName())
				.add("neighbours", node.getNeighboursAsString())
				.add("coeff", node.getCoeffAsString())
//				.add("coeff", "")
				.build();
		System.out.println(node.getNeighboursAsString());
		System.out.println(node.getCoeffAsString());
		sendToAllConnectedSessions(updateNode);
	}
	
	private void updateNodeJson(Node node) {
		JsonProvider provider = JsonProvider.provider();
		JsonObject updateMessage = provider.createObjectBuilder()
				.add("action", "update")
				.add("id", node.getId())
				.add("level", node.getLevel())
				.add("attribute", node.getAttribute())
				.add("name", node.getName())
				.build();
		sendToAllConnectedSessions(updateMessage);
	}
	
	private void sendToAllConnectedSessions(JsonObject message) {
		for (Object session : sessions) {
			sendToSession((Session)session, message);
		}
    }
	
	private void sendToSession(Session session, JsonObject message) {
		try {
			session.getBasicRemote().sendText(message.toString());
		} catch (IOException ex) {
			sessions.remove(session);
			Logger.getLogger(NodeSessionHandler.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}

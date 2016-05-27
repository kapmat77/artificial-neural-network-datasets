package org.network.server;

/**
 *
 * @author Mateusz Kaproń
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.network.model.Coefficients;
import org.network.model.Node;
import org.network.model.Sentence;

public class GraphBuilder {

	private final static String PATH = "C:\\Users\\Kapmat\\Desktop\\artificial-neural-network-datasets\\src\\java\\resources\\test.txt";
	
	private List<Node> neuralList = new ArrayList<>();
	List<String> attributeList = new ArrayList<>();
	Map<String, List<Node>> valueMap = new HashMap<>();
	Map<String, Double> minMap = new HashMap<>();
	Map<String, Double> maxMap = new HashMap<>();
	private String coeffString = ""; 
	private int DELAY = 100;
	private int numberOfObjects = 0;
	private List<Node> inputNodes = new ArrayList<>();
	List<Map<Node, Double>> sortedValueList = new ArrayList<>();
	Map<Node, Double> bigMap = new LinkedHashMap<>();
	Map<Node, Double> sortedBigMap = new LinkedHashMap<>();
	private List<Node> mainNodeList = new ArrayList<>();
	private List<String> inputValues = new ArrayList<>();
	private int findNode = 0;
	private boolean stopSearching = false;
	
	private static List<Node> allNodes = new ArrayList<>();
	
	private static int neuronsToActive = 0;

	
	public void buildGraph(String data, String activeWord, NodeSessionHandler sessionHandler,
			String speed) throws InterruptedException {
		
		setDelay(speed);
		numberOfObjects = 0;
		findNode = 0;
		if (data.equals("resetGraph")) {
			System.out.println("Allnode size:" + allNodes.size());
			sessionHandler.resetLines();
			sessionHandler.resetNodes(allNodes);
			
		} else if (data.equals("submitData")) {
			inputValues.clear();
			String[] inputList = activeWord.split("\\s+");
			for (int i = 0; i<inputList.length; i++) {
				inputList[i] = inputList[i].replace(",", ".");
				inputValues.add(inputList[i]);
			}	
		} else if (data.equals("update")) {
			neuronsToActive = Integer.valueOf(activeWord);
			setCoeff(sessionHandler);
			Thread.sleep(500);
			sendAddSentenceJson(sessionHandler, "");
			sessionHandler.resetLines();
			Thread.sleep(100);
			findAndActiveNeuron(sessionHandler, Integer.valueOf(speed));
		} else if (data.equals("buildGraphFromLog")) {
			System.out.println("BGFL is here");
			inputValues.clear();
			String[] inputList = activeWord.replace("BGFL:","").replace("|","").replace("-","").split("\\s+");
			for (int i = 0; i<inputList.length; i++) {
				inputList[i] = inputList[i];
				inputValues.add(inputList[i]);
				if (!inputList[i].contains(".o")) {
					i++;
				}
			}
			
			for (String inputNode: inputValues) {
				if (inputNode.contains(".o")) {
					String newString;
					int firstIndex = inputNode.indexOf("o");
					int secondIndex = firstIndex+1;
					int thirdIndex = secondIndex+1;
					int fourthIndex = thirdIndex+1;
					if (inputNode.charAt(thirdIndex)==':') {
						newString = String.valueOf(inputNode.charAt(firstIndex));
						newString = newString + String.valueOf(inputNode.charAt(secondIndex));
					} else if (inputNode.charAt(fourthIndex)==':') {
						newString = String.valueOf(inputNode.charAt(firstIndex));
						newString = newString + String.valueOf(inputNode.charAt(secondIndex));
						newString = newString + String.valueOf(inputNode.charAt(thirdIndex));
					} else {
						newString = String.valueOf(inputNode.charAt(firstIndex));
						newString = newString + String.valueOf(inputNode.charAt(secondIndex));
						newString = newString + String.valueOf(inputNode.charAt(thirdIndex));
						newString = newString + String.valueOf(inputNode.charAt(fourthIndex));
					}
					inputNode = newString;
					Node mainNode = findNodeByNameAll(inputNode);
					Node classNode = new Node();
					for (Node neighNode: mainNode.getNeighbours()) {
						if (neighNode.getAttribute().equalsIgnoreCase("CLASS")) {
							classNode = neighNode;
							break;
						}
					}
					Thread.sleep(100);
					sessionHandler.activeNeuron(classNode);
					Thread.sleep(100);
				}
				sessionHandler.activeNeuron(findNodeByNameAll(inputNode));
			}
			
		} else if(!data.equals("submitData")) {
			
			FileOperations file = new FileOperations();
			List<Sentence> inputSentences = file.readDataFromFile(PATH);
			attributeList = file.getAttributeList();
			createGraph(inputSentences, sessionHandler);
			
			allNodes.clear();
			allNodes.addAll(mainNodeList);
			allNodes.addAll(neuralList);

		} 

	}
	
	private void findAndActiveNeuron(NodeSessionHandler sessionHandler, int speed) throws InterruptedException {
		int multiplier = 10000;
		switch(speed) {
			case 10:
				multiplier = 1;
				break;
			case 9:
				multiplier = 1000;
				break;
			case 8:
				multiplier = 3000;
				break;
			case 7:
				multiplier = 5000;
				break;
			case 6:
				multiplier = 10000;
				break;
			case 5:
				multiplier = 20000;
				break;
			case 4:
				multiplier = 40000;
				break;	
			case 3:
				multiplier = 60000;
				break;
			case 2:
				multiplier = 80000;
				break;
			case 1:
				multiplier = 100000;
				break;		
		}

		double sleepTime = 0;
		double maxSleep = 0;
		boolean first = true;
		double prevValue = 0;
		int activeNeurons = 0;
		for (Map.Entry<Node, Double> entry: sortedBigMap.entrySet()) {
			if (first) {
				maxSleep = multiplier;
				sleepTime = 0;
				first = false;
			} else {
				sleepTime = maxSleep - multiplier*entry.getValue();
				maxSleep = maxSleep - sleepTime;
			}


			if (prevValue != entry.getValue()) {
				Thread.sleep((long) (sleepTime));
			}
			prevValue = entry.getValue();
			//JSON - active neuron
			sendActiveNeuronJson(sessionHandler, entry.getKey());
			
			//JSON - update sentence
			sendUpdateSentenceJson(sessionHandler, String.valueOf(entry.getKey().getName() + " - " + randDouble(entry.getValue())), "yellow");
			
			//JSON - update line between attibute and mainNode
			sendUpdateLinesJson(sessionHandler, entry.getKey(), "white");
			
			int activeNeighbours;
			for (Node node: mainNodeList) {
				activeNeighbours = 0;
				for (Node neighbour: node.getNeighbours()) {
					if (neighbour.isActive()) {
						activeNeighbours++;
					}
				}
				
				
				Node classNode = new Node();
				for (Node attrNode: node.getNeighbours()) {
					if (attrNode.getAttribute().equalsIgnoreCase("class")) {
						classNode = attrNode;
					}
				}
				
				if ((activeNeighbours==(node.getNeighbours().size()-1) && !node.isActive()
						&& !classNode.isActive()) || (activeNeighbours==(node.getNeighbours().size()) && !node.isActive())) {
					
					activeNeurons++;
					findNode++;
					//JSON - active mainNode
					sendActiveNeuronJson(sessionHandler, node);
					
					//JSON - update sentence (show active mainNode)
					sendUpdateSentenceJson(sessionHandler, " " + findNode +"."+node.getName()+":" + classNode.getName().replaceAll("CLASS", "") +" ", "red");
					
					if (speed!=10) {
						Thread.sleep(100);
					}
					
					
					//JSON - draw red line from object to classNode
					sendUpdateLinesOneNeighbourJson(sessionHandler, classNode, " " +node.getName(), "red");
					
					if (speed!=10) {
						Thread.sleep(100);
					}
					
					//JSON - active classNode
					sendActiveNeuronJson(sessionHandler, classNode);
					
					while(stopSearching) {
						Thread.sleep(100);
					}
				}
			}
			if (activeNeurons >= neuronsToActive) {
				break;
			}
		}	
	}
		
	public double randDouble(double value) {
		int valueInt = (int)(value*100);
		double valueDouble = (double)valueInt/100;
		return valueDouble;
	}

	private void createGraph(List<Sentence> inputSentences, NodeSessionHandler sessionHandler) throws InterruptedException {
		
		for (int k=0; k<inputSentences.size(); k++) {
			List<String> neighbours = new ArrayList<>();
			for (String word: inputSentences.get(k).getWords()) {
				neighbours.add(word);
			}
			
			//Json add new sentence
			sendAddSentenceJson(sessionHandler, inputSentences.get(k).toString());
			
			Node mainNode = new Node();
			mainNode.setName("o" + String.valueOf(k+1));
			numberOfObjects++;
			
			mainNodeList.add(mainNode);
			
			//Json create mainNode
			sendAddNodeJson(sessionHandler, mainNode);
			
			int index = 0;
			
			List<String> listOfWords = inputSentences.get(k).getWords();
			for (int i=0; i<(listOfWords.size()); i++) {
				Node node = findNodeByNameAndAttribute(listOfWords.get(i)+attributeList.get(i));
				index++;
				
				System.out.println("KEY: " + valueMap.containsKey(attributeList.get(i)));
				
				if (!valueMap.containsKey(attributeList.get(i))) {
					List<Node> valueList = new ArrayList<>();
					valueList.add(node);
					valueMap.put(attributeList.get(i),valueList);
				} else {
					valueMap.get(attributeList.get(i)).add(node);
				}
				
				//Json update single sentence
				if (node.getName().equals("null")) {
					node.setName(listOfWords.get(i)+attributeList.get(i));
					if (!attributeList.get(i).equals("CLASS")) {
						node.setValue(Double.valueOf((listOfWords.get(i)).replaceAll(",", ".")));
					}					
					node.setLevel(1);
					node.setAttribute(attributeList.get(i));
					neuralList.add(node);
					setConnections(mainNode, node, index, neighbours);
					//Json add new node
					sendAddNodeJson(sessionHandler, node);
				} else {
					node.increaseLevel();
					setConnections(mainNode, node, index, neighbours);	
				}
				
			}
			sendAddLinesJson(sessionHandler, mainNode);
		}
		
		sendAddSentenceJson(sessionHandler, "Etap uczenia sieci neuronowej zakończony powodzeniem - ilość węzłów: " 
				+ (neuralList.size()+numberOfObjects) + " | ilość obiektów: " + numberOfObjects);
		
		//find min and max values
		findMinAndMaxValues();
		
		
		
	}
	
	private void setCoeff(NodeSessionHandler sessionHandler) throws InterruptedException {
				for (int i=0; i<attributeList.size()-1; i++) {
			inputNodes.add(new Node(inputValues.get(i)+attributeList.get(i)));
			if (inputValues.get(i).equals("-")) {
//				inputNodes.get(i).setValue(null);
			} else {
				inputNodes.get(i).setValue(Double.valueOf(inputValues.get(i)));
			}
		}
		
		//Connect attributes
		for (int i=0; i<attributeList.size()-1; i++) {
			List<Node> valueList = valueMap.get(attributeList.get(i));
			for (Node node : valueList) {
				inputNodes.get(i).addNeighbour(node, false);
				
				double coefficient;
				if (inputValues.get(i).equals("-")) {
					coefficient = 1;
					sendActiveNeuronJson(sessionHandler, node);
				} else {
					coefficient = countCoefficient(inputNodes.get(i), node, attributeList.get(i));
				}
				 
				inputNodes.get(i).addNeighCoefficient(node,coefficient);
			}
			bigMap.putAll(inputNodes.get(i).getNeighCoefficient());
		}
		
		sortedBigMap = sortByValue(bigMap);
	}
	
	private void setConnections(Node mainNode, Node node, int firstIndex, List<String> neighbours) {
		mainNode.addNeighbour((Node)node, false);
		node.addNeighbour(mainNode, false);
	}

	private double countCoefficient(Node firstNode, Node secondNode, String attribute) {
		
		double R = getMax(maxMap.get(attribute), firstNode.getValue()) - getMin(minMap.get(attribute), firstNode.getValue()); 
		
		double diff = Math.abs(firstNode.getValue() - secondNode.getValue());
		double coeff ;
		if (R==0) {
			coeff = 1;
		} else {
			coeff = 1-(diff/R);
			if (coeff==0) {
				coeff = 0.01;
			}
		}
	
		return coeff;
	}
	
	private double getMax(Double first, Double second) {
		if (first > second) {
			return first;
		} else {
			return second;
		}
	}
	
	private double getMin(Double first, Double second) {
		if (first > second) {
			return second;
		} else {
			return first;
		}
	}
	
	private Coefficients countCoefficients(Node firstNode, int firstIndex, Node secondNode, int secondIndex) {

		Double tau = Double.valueOf(firstIndex - secondIndex);
		//Synaptic effectiveness
		Double sE = secondNode.getNeighCoefficient().get(firstNode) + 1/tau;
		//Synaptic weight
		Double sW = (2*sE)/(secondNode.getLevel()+sE);

		return new Coefficients(sW,sE);
	}

	private Node findNodeByName(String word) {
		for (Node singleNode: neuralList) {
			if (singleNode.getName().equals(word)) {
				return singleNode;
			}
		}
		return new Node();
	}
	
	private Node findNodeByNameAll(String word) {
		for (Node singleNode: allNodes) {
			if (singleNode.getName().equals(word)) {
				return singleNode;
			}
		}
		return new Node();
	}
	
	private Node findNodeByNameAndAttribute(String word) {
		for (Node singleNode: neuralList) {
			if (singleNode.getName().equals(word)) {
				return singleNode;
			}
		}
		return new Node();
	}

	private void sendAddNodeJson(NodeSessionHandler sessionHandler, Node node) throws InterruptedException {
		sessionHandler.addNode(node);
		Thread.sleep(DELAY);
	}

	private void sendUpdateNodeJson(NodeSessionHandler sessionHandler, Node node) throws InterruptedException {
		sessionHandler.updateNode(node);
		Thread.sleep(DELAY);
	}

	private void sendAddSentenceJson(NodeSessionHandler sessionHandler, String sentence) throws InterruptedException {
		sessionHandler.addSentence(sentence);
		Thread.sleep(DELAY);
	}

	private void sendUpdateSentenceJson(NodeSessionHandler sessionHandler, String word, String color) throws InterruptedException {
		sessionHandler.updateSentence(word,color);
	}
	
	private void sendRemoveSentenceJson(NodeSessionHandler sessionHandler) throws InterruptedException {
		sessionHandler.removeSentence();
	}

	private void sendAddLinesJson(NodeSessionHandler sessionHandler, Node node) throws InterruptedException {
		sessionHandler.addLines(node,coeffString);
		Thread.sleep(DELAY);
	}
	
	private void sendUpdateLinesJson(NodeSessionHandler sessionHandler, Node node, String color) throws InterruptedException {
		sessionHandler.updateLines(node, color);
//		Thread.sleep(DELAY);
	}

	private void sendActiveNeuronJson(NodeSessionHandler sessionHandler, Node node) throws InterruptedException {
		node.setActive(true);
		sessionHandler.activeNeuron(node);
	}
	
	private void sendUpdateBestLineJson(NodeSessionHandler sessionHandler, Node node) throws InterruptedException {
		sessionHandler.updateBestLine(node);
		Thread.sleep(DELAY);
	}
	
	private void setDelay(String speed) {
		switch(speed) {
			case "1":
				DELAY = 3000;
				break;
			case "2":
				DELAY = 2000;
				break;
			case "3":
				DELAY = 1000;
				break;
			case "4":
				DELAY = 500;
				break;
			case "5":
				DELAY = 300;
				break;
			case "6":
				DELAY = 150;
				break;
			case "7":
				DELAY = 100;
				break;
			case "8":
				DELAY = 50;
				break;
			case "9":
				DELAY = 10;
				break;
			case "10":
				DELAY = 0;
				break;	
		}
	}

	private void findMinAndMaxValues() {
		double maxValue;
		double minValue;
		double currentValue;
		boolean first;
		//Find max and min values
		for (String attrName: attributeList) {
			List<Node> valueList = valueMap.get(attrName);
			maxValue = 0;
			minValue = 0;
			currentValue = 0;
			first = true;
			for (Node node : valueList) {
				try {
					currentValue = Double.valueOf(node.getName().replaceAll(attrName, "").replaceAll(",", "."));
					if (first) {
						maxValue = currentValue;
						minValue = currentValue;
						first = false;
					} else {
						if (currentValue>maxValue) {
							maxValue = currentValue;
						} else if (currentValue<minValue) {
							minValue = currentValue;
						}
					}
				} catch (Exception e) {
					//Nic nie rób
				}	
			}
			minMap.put(attrName, minValue);
			maxMap.put(attrName, maxValue);		
		}
	}
	
	public <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map )
	{
		Map<K, V> result = new LinkedHashMap<>();
		Stream<Map.Entry<K, V>> st = map.entrySet().stream();

		st.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
		   .forEachOrdered(e -> result.put(e.getKey(), e.getValue()));
		
		return result;
	}

	private void sendUpdateLinesOneNeighbourJson(NodeSessionHandler sessionHandler, Node classNode, String nameOfNode,
			String color) throws InterruptedException {
		sessionHandler.updateLines(classNode, nameOfNode, color);
	}
}


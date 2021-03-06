package org.network.model;

/**
 *
 * @author Mateusz Kaproń
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Node {

	private int id = 0;
	private double value;

	private String name = "";
	private String attribute = "";
	private double maxValue = 0.0;
	private double minValue = 0.0;
	private double coeff = 0.0;

	private boolean active = false;

	private int level = 0;
	private List<Node> neighbours = new ArrayList<>();
	private List<Node> someOfNeighbours = new ArrayList<>();
	private List<Node> bestNeighbours = new ArrayList<>();
	private Map<Node, Double> neighCoefficient = new HashMap();
	private Map<Node, Coefficients> neighActive = new HashMap();

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public double getCoeff() {
		return coeff;
	}

	public void setCoeff(double coeff) {
		this.coeff = coeff;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

	public double getMinValue() {
		return minValue;
	}

	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}

	public Map<Node, Coefficients> getNeighActive() {
		return neighActive;
	}

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public void setNeighActive(Map<Node, Coefficients> neighActive) {
		this.neighActive = neighActive;
	}

	public void addNeighActive(Node neighbour, Coefficients coefficient) {
		this.neighActive.put(neighbour, coefficient);
	}

	public Node() {
		this.name = "null";
	}

	public Node(String newName) {
		this.name = newName;
		level = 1;
	}

	public int getId() {
		return id;
	}

	public void setId(int newId) {
		this.id = newId;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public void increaseLevel() {
		this.level = this.level + 1;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Node> getNeighbours() {
		return neighbours;
	}

	public String getCoeffAsString() {
		String str = "";

		for (Node node : neighbours) {
			if (node.getNeighbours().size() > 0 && node.getNeighCoefficient().get(this) != null) {
				str = str + " " + (double) (node.getNeighCoefficient().get(this));
			}
		}
		return str;
	}

	public String getNeighboursAsString() {
		String str = "";

		for (Node node : neighbours) {
			str = str + " " + node.name;
		}

		return str;
	}

	public String getAnotherNeighboursAsString() {
		String str1[] = new String[someOfNeighbours.size() + 1];
		String str2[] = new String[neighbours.size() + 1];
		String str3 = "";

		List<String> firstList = new ArrayList<>();
		List<String> secondList = new ArrayList<>();

		for (Node node : someOfNeighbours) {
			firstList.add(node.name);
		}

		for (Node node : neighbours) {
			secondList.add(node.name);
		}

		secondList.removeAll(firstList);

		for (int j = 0; j < secondList.size(); j++) {
			str3 = str3 + " " + secondList.get(j);
		}

		return str3;
	}

	public void setNeighbours(List<Node> neighbours) {
		this.neighbours = neighbours;
	}

	public String getStringBestNeighbour() {
		String str = "";
		for (Node node : bestNeighbours) {
			str = str + " " + node.name;
		}
		return str;
	}

	public void setBestNeighboyr(List<Node> best) {
		bestNeighbours.addAll(best);
	}

	public void addNeighbour(Node neighbour, boolean nodePath) {
		boolean exist = false;

		for (Node node : neighbours) {
			if (node.getName().equals(neighbour.getName())) {
				exist = true;
				break;
			}
		}

		if (!exist) {
			this.neighbours.add(neighbour);
		}
		if (nodePath) {
			for (Node node : someOfNeighbours) {
				if (node.getName().equals(neighbour.getName())) {
					exist = true;
					break;
				}
			}

			if (!exist) {
				this.someOfNeighbours.add(neighbour);
			}
		}
	}

	private Coefficients getCoefficient() {
		return new Coefficients(0.0, 0.0);
	}

	public Map<Node, Double> getNeighCoefficient() {
		return neighCoefficient;
	}

	public void setNeighCoefficient(Map<Node, Double> neighCoefficient) {
		this.neighCoefficient = neighCoefficient;
	}

	public void addNeighCoefficient(Node neighbour, Double coefficient) {
		this.neighCoefficient.put(neighbour, coefficient);
	}

}

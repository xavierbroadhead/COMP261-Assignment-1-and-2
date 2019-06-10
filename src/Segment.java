import java.util.ArrayList;
import java.util.List;

public class Segment {

	private Node start;
	private Node end;
	private double weight;
	private Road road;
	private List<Location> coords = new ArrayList<>();
	
	public Segment(Node start, Node end, double weight, Road road, List<Location> coords) {
		this.start = start;
		this.end = end;
		this.weight = weight;
		this.setRoad(road);
		this.setCoords(coords);
	}
	
	public boolean containsNode(Node n) {
		assert n != null;
		return n.equals(this.start) || n.equals(this.end);
	}
	public Node requestOppositeNode(Node n) {
		assert this.containsNode(n);
		return n.equals(this.start) ? this.end : this.start;
		
	}
	public Node getStartNode() {
		return this.start;
	}
	
	public Node getEndNode() {
		return this.end;
	}
	
	public double getWeight() {
		return this.weight;
	}

	public List<Location> getCoords() {
		return this.coords;
	}

	public void setCoords(List<Location> coords) {
		this.coords = coords;
	}
	public List<Node> getNodes(){
		List<Node> buffer = new ArrayList<>();
		buffer.add(this.start);
		buffer.add(this.end);
		return buffer;
	}
	public Road getRoad() {
		return this.road;
	}

	public void setRoad(Road road) {
		this.road = road;
	}
	
}

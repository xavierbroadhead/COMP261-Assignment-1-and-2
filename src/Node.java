import java.util.ArrayList;
import java.util.List;

public class Node {

	private Location location;
	private List<Segment> segments = new ArrayList<>();
	private final int ID;
	private int depth;
	private Node previousNode;
	
	public Node(int ID, Location location) {
		this.ID = ID;
		this.location = location;
	}
	
	public boolean addPreviousNode(Node n) {
		return (this.previousNode = n) != null;
	}
	public boolean hasPreviousNode() {
		return this.previousNode != null;
	}
	public Node getPreviousNode() {
		return this.previousNode;
	}
	
	public List<Node> getNeighbours(){
		List<Node> buffer = new ArrayList<>();
		for(Segment s : this.segments) {
			buffer.add(s.requestOppositeNode(this));
		}
		return buffer;
	}
	
	public void maxDepth() {
		this.depth = Integer.MAX_VALUE;
	}
	public int getDepth() {
		return this.depth;
	}
	
	public Location getLocation() {
		return this.location;
	}
	
	public List<Segment> getSegments(){
		return this.segments;
	}
	
	public boolean addSegment(Segment s) {
		return this.segments.add(s);
	}
	
	public int getID() {
		return this.ID;
	}

	public void setDepth(int depth) {
		this.depth = depth;
		
	}
}

import java.util.ArrayDeque;

public class ArticulationPointNode {

	private Node node;
	private int reach;
	private ArticulationPointNode parent;
	private int depth;
	private ArrayDeque<Node> children;
	
	public ArticulationPointNode(Node node, int reach, ArticulationPointNode parent) {
		this.node = node;
		this.reach = reach;
		this.parent = parent;
	}
	
	public int getDepth() {
		return depth;
	}
	
	public void setDepth(int depth) {
		this.depth = depth;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public int getReach() {
		return reach;
	}

	public void setReach(int reach) {
		this.reach = reach;
	}

	public ArticulationPointNode getParent() {
		return parent;
	}

	public void setParent(ArticulationPointNode parent) {
		this.parent = parent;
	}

	public ArrayDeque<Node> getChildren() {
		return children;
	}

	public void setChildren(ArrayDeque<Node> children) {
		this.children = children;
	}
}

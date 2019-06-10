import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

public class AucklandRoadMap extends GUI {
	
	//Integer refers to the roadID or nodeID respectively
	private Map<Integer, Road> roads = new HashMap<>();
	private Map<Integer, Node> nodes = new HashMap<>();
	
	private double scale;
	private int test;
	private Location origin;
	private Trie trie;
	private static Node firstClick;
	private static Node secondClick;
	
	private List<Segment> segments = new ArrayList<>();
	private List<Node> unvisitedNodes = new ArrayList<>();
	
	private Set<Node> articulationPoints = new HashSet<>();
	private Set<Road> highlightedRoads = new HashSet<>();
	private Set<Node> highlightedNodes = new HashSet<>();
	private Set<Segment> highlightedSegments = new HashSet<>();
	
	private Stack<ArticulationPointNode> articulationStack = new Stack<>();
	
	private static HashSet<Node> closedSet = new HashSet<>();
	private static Queue<Node> openSet;
	
	public AucklandRoadMap() {
		this.test = 0;
		this.scale = 2;
		redraw();
		this.trie = new Trie();
	}
	
	/** Initialise origin so that it fits the entire data set within the windows bounds,
	 *  called after data set has been loaded.
	 */
	
	public void initialiseMaxValues() {
		double maxPositiveY = Double.MIN_VALUE;
		double maxPositiveX = Double.MIN_VALUE;
		double maxNegativeY = Double.MAX_VALUE;
		double maxNegativeX = Double.MAX_VALUE;
		
		for (Node n : this.nodes.values()) {
			if (n.getLocation().x > maxPositiveX) {
				maxPositiveX = n.getLocation().x;
			}
			if (n.getLocation().y > maxPositiveY) {
				maxPositiveY = n.getLocation().y;
			}
			if (n.getLocation().x < maxNegativeX) {
				maxNegativeX = n.getLocation().x;
			}
			if (n.getLocation().y < maxNegativeY) {
				maxNegativeY = n.getLocation().y;
			}
		}
		this.origin = new Location (maxNegativeX, maxPositiveY);
	}
	
	/** Redraws window based on updated position and scale. Called every update in state
	 * 
	 * 
	 */
	@Override
	protected void redraw(Graphics g) {
		if (!this.nodes.isEmpty()) {
			g.setColor(Color.BLACK);
			for (Node n : this.nodes.values()) {
				drawNode(g, n);
			}
		}
		if (!this.roads.isEmpty()) {
			g.setColor(Color.BLUE);
			for (Road r : this.roads.values()) {
				drawRoad(g, r);
			}
		}
		if (!this.highlightedRoads.isEmpty()) {
			g.setColor(Color.RED);
			for (Road r : this.highlightedRoads) {
				drawRoad(g, r);
			}
		}
		if (!this.highlightedSegments.isEmpty()) {
			g.setColor(Color.RED);
			for (Segment s : this.highlightedSegments) {
				drawSegment(g, s);
			}
		}
		if (!this.highlightedNodes.isEmpty()) {
			g.setColor(Color.GREEN);
			for (Node n : this.highlightedNodes) {
				drawNode(g, n);
			}
		}
		if (!this.articulationPoints.isEmpty()) {
			g.setColor(Color.orange);
			for (Node n : this.articulationPoints) {
				drawNode(g, n);
			}
		}
	}
	
	/** Find node closest to clicked location
	 * 
	 * 
	 */
	
	@Override
	protected void onClick(MouseEvent e) {
		getTextOutputArea().setText(null); //clear output area
		Location click = Location.newFromPoint(e.getPoint(), this.origin, this.scale); //obtain exact location of click
		double distance = Double.MAX_VALUE; //start with max value so we can find smallest distance
		Node bufferNode = null;
		
		for (Node n : this.nodes.values()) {
			double comparable = click.distance(n.getLocation()); //check each node's distance to click location, assign node to buffer node if closer than current distance
			if (comparable < distance) {
				distance = comparable;
				bufferNode = n;
			}
		}
		//check if it is the first input click; if so, store the node associated with the click in the static first click node
		if (firstClick == null) {
			firstClick = bufferNode;
		}
		//otherwise, it must be the second click, now we have two nodes which we can work with
		else if (secondClick == null) {
			secondClick = bufferNode;
		}
		getTextOutputArea().append("Identified node: " + Integer.toString(firstClick.getID()) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		
		//Set to avoid duplicates, add road names to buffer after we obtain them from segments the found node contains
		Set<String> bufferRoads = new HashSet<>();
		
		//only iterate for the first click as this is the only relevant intersection for this operation
		for (Segment s : firstClick.getSegments()) {
			bufferRoads.add(s.getRoad().getLabel());
		}
		
		getTextOutputArea().append("Roads at this intersection: \n"); //$NON-NLS-1$
		
		//now, add these road names to our text output area from our set
		for (String s : bufferRoads) {
			getTextOutputArea().append(s + "\n"); //$NON-NLS-1$
		}
		//ensure we don't receive a null pointer
		if (firstClick != null) {
			System.out.println("First node: " + firstClick.getID() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (secondClick != null) {
			System.out.println("Second node: " + secondClick.getID() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (firstClick != null && secondClick != null) {
			this.highlightedNodes.clear();
			this.highlightedSegments.clear();
			for (Segment s : aStarSearch(firstClick, secondClick)) {
				this.highlightedSegments.add(s);
			}
			getTextOutputArea().setText(null);
			getTextOutputArea().append("Your route is as follows: \n");
			double totalLength = 0.0;
			for (Road r : this.highlightedRoads) {
				getTextOutputArea().append(r.getLabel() + "\n");
				for (Segment s : r.getSegments()) {
					totalLength += s.getWeight();
				}
			}
			getTextOutputArea().append("Total distance: " + totalLength + "km.");
		}
		//if two clicks are stored in memory, after we have iterated using their values, we want to clear
		//memory in order to be able to perform a new search
		if (firstClick != null && secondClick != null) {
			firstClick = null;
			secondClick = null;
		}
	}
	
	/** Iterative search which searches based on prefix.
	 *
	 */
	
	protected void iterativeSearch() {
		getTextOutputArea().setText(null);
		StringBuffer query = new StringBuffer(getSearchBox().getText());
		Set<String> buffer = new HashSet<>();
		
		for (Road r : this.roads.values()) {
			if (r.getLabel().contains(query)) {
				buffer.add(r.getLabel());
			}
		}
		
		getTextOutputArea().append("Did you mean: \n"); //$NON-NLS-1$
		
		for (String s : buffer) {
			getTextOutputArea().append(s + " \n"); //$NON-NLS-1$
		}
	}
	
	/** Trie search implementation
	 * 
	 */
	
	@Override
	protected void onSearch() {
		getTextOutputArea().setText(null);
		this.highlightedRoads.clear();
		String query = getSearchBox().getText();
		this.highlightedRoads.addAll(this.trie.getAll(query));
		
		Set<String> bufferSet = new HashSet<>();
		for (Road r : this.highlightedRoads) {
			bufferSet.add(r.getLabel());
		}
		
		getTextOutputArea().append("Did you mean: \n"); //$NON-NLS-1$
		for (String s : bufferSet) {
			getTextOutputArea().append(s + " \n"); //$NON-NLS-1$
		}
	}
	
	/** 
	 * ==========================================================
	 * 		ROUTING ALGORITHMS AND RELEVANT HELPER METHODS
	 * ==========================================================
	 */
	
	public static HashSet<Node> reconstructPathNodes(Node current) {
		Node buffer = current;
		HashSet<Node> path = new HashSet<>();
		path.add(buffer);
		//while the currently iterated node has a previous node stored in its field;
		//i.e. while there is still a path to reconstruct, as this will be null in the start node,
		//hasPreviousNode() returns false if there is a null value stored in the field
		while (buffer.hasPreviousNode()) {
			path.add(buffer.getPreviousNode());
			buffer = buffer.getPreviousNode();
		}
		System.out.println("Reconstructing nodes: " + path.size()); //$NON-NLS-1$
		return path;
	}
	
	/** Returns the path to the passed node from the start node
	 * 
	 * 
	 * @param current - the node which we want to construct a path to
	 * @return
	 */
	public HashSet<Segment> reconstructPathSegments(Node current){
		//store passed node in local memory
		Node buffer = current;
		this.highlightedNodes.add(buffer);
		HashSet<Segment> path = new HashSet<>();
		while (buffer.hasPreviousNode()) {
			for (Segment s : buffer.getSegments()) {
				if (s.requestOppositeNode(buffer).equals(buffer.getPreviousNode())) {
					path.add(s);
					break;
				}
			}
			buffer = buffer.getPreviousNode();
			this.highlightedNodes.add(buffer);
		}
		System.out.println("Reconstructing segments: " + path.size()); //$NON-NLS-1$
		return path;
	}
	
	/** Returns the h value between two nodes
	 * 
	 * @param a - Start node
	 * @param b - Goal node
	 * @return - double representing the estimated h cost between two nodes
	 */
	public static double requestHValue(Node start, Node goal) {
		return start.getLocation().distance(goal.getLocation());
	}
	
	/** Returns G value from any given node, provided a proper A* foundation has been established
	 * 
	 * @param n - node whose g value we want to calculate
	 * @return - cost from n to start node
	 */
	public static double requestGValue(Node n) {
		double cost = 0.0;
		Node buffer = n;
		while (buffer.hasPreviousNode()) {
			for (Segment s : buffer.getSegments()) {
				if (s.containsNode(buffer.getPreviousNode())) {
					cost += s.getWeight();
					break;
				}
			}
			buffer = buffer.getPreviousNode();
		}
		return cost;
	}
	
	/** Returns F value between two nodes
	 * 
	 * @param start - node whose f value we want to calculate
	 * @param goal - end point node 
	 * @return
	 */
	public static double requestFValue(Node start, Node goal) {
		return (requestHValue(start, goal) + requestGValue(start));
	}
	
	/** Populate the set with the passed node's neighbours
	 * 
	 * 
	 * @param n - node whose neighbours we want to add to the set
	 */
	public void addNeighbourNodes(Node n) {
		Node bufferNode;
		System.out.println("neighbour nodes " + this.test++); //$NON-NLS-1$
		for (Segment s : n.getSegments()) {
			assert s.containsNode(n);
			if (s.requestOppositeNode(n) != null) bufferNode = s.requestOppositeNode(n);
			else continue;
			if (!openSet.contains(bufferNode))
				openSet.add(s.requestOppositeNode(n));
		}
	}

	/** Returns a Set representing the most efficient path from the first passed node 'start'
	 *  to the second passed node 'goal'
	 * 
	 * @param start - the node which we want to begin searching from
	 * @param goal - the destination node
	 * @return
	 */
	public HashSet<Segment> aStarSearch(Node start, Node goal){
		long startTime = System.nanoTime();
		Node currentNode = null;
		//discovered, unevaluated nodes
		openSet = new PriorityQueue<>(new Comparator<Node>() {
			@Override
			public int compare(Node a, Node b) {
				return Double.compare(requestFValue(a, goal), requestFValue(b, goal));
			}
		});
		//add start node to open set to begin evaluation from that node
		openSet.add(start);
		int i = 0;
		
		while (!openSet.isEmpty()) {
			System.out.println("search function" + i++); //$NON-NLS-1$
			Node bufferNode = openSet.poll();
			closedSet.add(bufferNode);
			bufferNode.addPreviousNode(currentNode);
			currentNode = bufferNode;
			if (currentNode.equals(goal)) {
				return reconstructPathSegments(currentNode);
			}
			addNeighbourNodes(currentNode);
		}
		
		long endTime = System.nanoTime();
		System.out.print("A* Search took: " + ((startTime - endTime) / 1000000)); //$NON-NLS-1$
		return null;
	}
	
	/** Initialise and monitor the articulation points finding algorithm as it runs
	 * 
	 */
	@SuppressWarnings("null")
	public void findArtPoints() {
		long startTime = System.nanoTime();
		//populate the unvisited list
		for (Node n : this.nodes.values()) {
			this.unvisitedNodes.add(n);
		}
		
		//while there are still nodes to check
		while (this.unvisitedNodes.size() > 0) {
			int numSubTree = 0;
			Node startNode = null;
			for (int i = 0; i < this.unvisitedNodes.size(); i++) {
				this.unvisitedNodes.get(i).maxDepth();
				if (i == 0) {
					startNode = this.unvisitedNodes.get(i);
				}
			}
			for (Node n : startNode.getNeighbours()) {
				this.unvisitedNodes.remove(startNode);
				if (n.getDepth() == Integer.MAX_VALUE) {
					getArtPoints(n, startNode);
					numSubTree++;
				}
			}
			if (numSubTree > 1) this.articulationPoints.add(startNode);
		}
		long endTime = System.nanoTime();
		System.out.println("Found " + this.articulationPoints.size() + " articulation points."); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println("Articulation point finding took: " + ((startTime - endTime) / 1000000)); //$NON-NLS-1$
		
	}
	
	/** Populates the articulationPoints Set with found articulation points
	 * 
	 * 
	 * @param startNode - Node we want to start searching from
	 * @param root - Root node of the data structure
	 */
	public void getArtPoints(Node startNode, Node root) {
		
		this.articulationStack.push(new ArticulationPointNode(startNode, 1, new ArticulationPointNode(root, 0, null)));
		try {
			while (!this.articulationStack.isEmpty()) {
				ArticulationPointNode artNode = this.articulationStack.peek();
				Node node = artNode.getNode();
			
				if (artNode.getChildren() == null) {
					node.setDepth(artNode.getDepth());
					artNode.setReach(artNode.getDepth());
					ArrayDeque<Node> buffer = new ArrayDeque<>();
					artNode.setChildren(buffer);
				
					for (Node neighbour : node.getNeighbours()) {
						if (neighbour != artNode.getParent().getNode()) {
							buffer.offer(neighbour);
						}
					}
				}
				else if (!artNode.getChildren().isEmpty()) {
					Node child = artNode.getChildren().poll();
					if (child.getDepth() < Integer.MAX_VALUE) {
						artNode.setReach(Math.min(artNode.getReach(), child.getDepth()));
					}
					else this.articulationStack.push(new ArticulationPointNode(child, node.getDepth() + 1, artNode));
				}
				else {
					if (node != startNode) {
						if (artNode.getReach() >= artNode.getParent().getDepth()) {
							this.articulationPoints.add(artNode.getParent().getNode());
						}
						artNode.getParent().setReach(Math.min(artNode.getParent().getReach(), artNode.getReach()));
					}
					this.unvisitedNodes.remove(this.articulationStack.pop().getNode());
				}
			}
		}
		catch(NullPointerException e) {
			System.out.println("Caught exception: " + e); //$NON-NLS-1$
		}
	}
	
	/** Iterate over roads HashMap's values to populate tree with relevant nodes
	 * 
	 */
	protected void loadTries(){
		if (this.roads.values() == null) {
			return;
		}
		for (Road r : this.roads.values()) {
			this.trie.addRoad(r);
		}
	}
	
	/** Updates screen based on action performed by the user
	 * @param Move m - enum denoting which action was performed
	 */
	
	@Override
	protected void onMove(Move m) {
		if (m == null) {
			return;
		}
		if (m.equals(Move.NORTH)) {
			this.origin = new Location(this.origin.x, this.origin.y + 3);
		}
		else if (m.equals(Move.EAST)) {
			this.origin = new Location(this.origin.x + 3, this.origin.y);
		}
		else if(m.equals(Move.SOUTH)) {
			this.origin = new Location(this.origin.x, this.origin.y - 3);
		}
		else if(m.equals(Move.WEST)) {
			this.origin = new Location(this.origin.x - 3, this.origin.y);
		}
		else if (m.equals(Move.ZOOM_IN)) {
			this.scale += (this.scale*0.1);
		}
		else {
			this.scale -= (this.scale*0.1);
		}
		redraw();
	}

	@SuppressWarnings("hiding")
	@Override
	protected void onLoad(File nodes, File roads, File segments, File polygons) {
		//Break each load function up into its own method, easier to isolate each problem and tackle them one by one
		parseNodesFile(nodes);
		parseRoadsFile(roads);
		parseSegmentsFile(segments);
		loadTries();
		findArtPoints();
		initialiseMaxValues();
	}
	
	/* All parsing methods are a modified version of 
	 * https://stackoverflow.com/questions/19575308/read-a-file-separated-by-tab-and-put-the-words-in-an-arraylist
	 * 
	 */
	
	@SuppressWarnings({ "boxing", "hiding", "resource" })
	private void parseNodesFile(File nodes) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(nodes));
			in.readLine(); //skips first line
			for(String line = in.readLine(); line != null; line = in.readLine()) {
				String [] buffer = line.split("\t"); //$NON-NLS-1$
				int nodeID = Integer.parseInt(buffer[0]);
				double nodeLat = Double.parseDouble(buffer[1]);
				double nodeLon = Double.parseDouble(buffer[2]);
				
				this.nodes.put(nodeID, new Node(nodeID, Location.newFromLatLon(nodeLat, nodeLon)));
			}
			in.close();
		}
		catch(FileNotFoundException e) {
			System.out.println(e);
			return;
		}
		catch(IOException e) {
			System.out.println(e);
			return;
		}
	}
	
	@SuppressWarnings({ "hiding", "boxing", "resource" })
	private void parseRoadsFile(File roads) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(roads));
			in.readLine(); //skips first line
			for (String line = in.readLine(); line != null; line = in.readLine()) {
				String[] buffer = line.split("\t"); //$NON-NLS-1$
				int roadID = Integer.parseInt(buffer[0]);
				int type = Integer.parseInt(buffer[1]);
				String label = buffer[2];
				String city = buffer[3];
				boolean oneWay = buffer[4].equals("1"); //$NON-NLS-1$
				int speed = Integer.parseInt(buffer[5]);
				int roadClass = Integer.parseInt(buffer[6]);
				boolean notForCar = buffer[7].equals("1"); //$NON-NLS-1$
				boolean notForPede = buffer[8].equals("1");	 //$NON-NLS-1$
				boolean notForBike = buffer[9].equals("1"); //$NON-NLS-1$
				
				this.roads.put(roadID, new Road(roadID, type, label, city, oneWay,
						  speed, roadClass, notForCar, notForPede,
						  notForBike));
			}
			in.close();
		}
		catch(FileNotFoundException e){
			System.out.println(e);
			return;
		}
		catch(IOException e) {
			System.out.println(e);
			return;
			
		}
	}
	
	@SuppressWarnings({ "hiding", "boxing", "resource" })
	private void parseSegmentsFile(File segments) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(segments));
			in.readLine(); // skip first line
			for(String line = in.readLine(); line != null; line = in.readLine()) {
				String [] buffer = line.split("\t", 5); ///First 5 tabs, don't include co-ords //$NON-NLS-1$
				Road road = this.roads.get((Integer.parseInt(buffer[0])));
				double roadLength = Double.parseDouble(buffer[1]);
				Node bufferNode1 = this.nodes.get(Integer.parseInt(buffer[2]));
				Node bufferNode2 = this.nodes.get(Integer.parseInt(buffer[3]));
				
				String[] coords = buffer[4].split("\t"); //Create new array after first 4 elements to have a separate coords array //$NON-NLS-1$
				
				List<Location> coordsLocations = new ArrayList<>();
				
				for (int i = 0; i < coords.length; i+= 2) {
					coordsLocations.add(Location.newFromLatLon(
							Double.parseDouble(coords[i]), Double.parseDouble(coords[i+1])));
				}
				Segment bufferSegment = new Segment(bufferNode1, bufferNode2, roadLength, road, coordsLocations);
				try {
					bufferNode1.addSegment(bufferSegment);
					bufferNode2.addSegment(bufferSegment);
				}
				catch(NullPointerException e) {
					System.out.println("Caught exception: " + e); //$NON-NLS-1$
				}
				this.segments.add(bufferSegment);
				road.addSegment(bufferSegment);
			}
			in.close();
		}
		catch(FileNotFoundException e) {
			System.out.println(e);
			return;
		}
		catch(IOException e) {
			System.out.println(e);
			return;
		}
	}
	
	private void drawSegment(Graphics g, Segment s) {
		List<Location> buffer = s.getCoords();
		for (int i = 0; i < s.getCoords().size() - 1; i++) {
			g.drawLine(buffer.get(i).asPoint(this.origin, this.scale).x, 
					   buffer.get(i).asPoint(this.origin, this.scale).y, 
					   buffer.get(i+1).asPoint(this.origin, this.scale).x, 
					   buffer.get(i+1).asPoint(this.origin, this.scale).y);
		}
	}
	private void drawRoad(Graphics g, Road r) {
		if (r == null) return;
		for (Segment s : r.getSegments()) {
			if (s.getRoad() == r) {
				List<Location> buffer = s.getCoords();
				//iterate through each segment, draw from its first node to its next
				for (int i = 0; i < s.getCoords().size() - 1; i++) {
					g.drawLine(buffer.get(i).asPoint(this.origin, this.scale).x, 
							   buffer.get(i).asPoint(this.origin, this.scale).y, 
							   buffer.get(i+1).asPoint(this.origin, this.scale).x, 
							   buffer.get(i+1).asPoint(this.origin, this.scale).y);
				}
			}
		}
	}
	
	private void drawNode(Graphics g, Node n) {
		Point p = n.getLocation().asPoint(this.origin, this.scale);
		g.fillOval(p.x, p.y, 4, 4);
	}
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		new AucklandRoadMap();
	}
}

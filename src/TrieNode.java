import java.util.ArrayList;
import java.util.HashMap;

/** Basic Trienode data structure
 *
 */
public class TrieNode {
	
	private ArrayList<Road> roads;
	private HashMap<Character, TrieNode> children;
	
	public TrieNode() {
		children = new HashMap<Character, TrieNode>();
		roads = new ArrayList<Road>();
	}
	
	public TrieNode getChild(Character c) {
		return children.get(c);
	}
	
	public TrieNode addChild(Character c, TrieNode node) {
		return children.put(c, node);
	}
	
	public boolean hasChild(Character ch) {
		return this.children.containsKey(ch);
	}
	
	public ArrayList<Road> getRoads(){
		return roads;
	}
	
	public boolean addRoad(Road r) {
		return roads.add(r);
	}
	
	public boolean hasChildren() {
		return !children.isEmpty();
	}
	
	public HashMap<Character, TrieNode> getChildren(){
		return children;
	}
	
}

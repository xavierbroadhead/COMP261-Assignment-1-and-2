import java.util.*;

public class Trie {
	
	private TrieNode root;
	
	public Trie() {
		root = new TrieNode();
	}
	
	/** Adds a road to the correct node on the trie
	 * 
	 * @param r - Road we want to add to the trie
	 * @return true if adding was successful
	 */
	
	public boolean addRoad(Road r) {
		TrieNode node = root;
		for (char c : r.getLabel().toCharArray()) {
			if (!node.hasChild(c)) {
				TrieNode bufferNode = new TrieNode();
				node.addChild(c, bufferNode);
				node = bufferNode;
			}
			else node = node.getChild(c);
		}
		return node.addRoad(r);
	}
	
	/** Get a List of roads that exactly match the passed string.
	 * 
	 * 
	 * @param s - string we are searching for
	 * @return List of all road objects whose label exactly matches the passed string
	 */
	
	public List<Road> getRoads(String s){
		TrieNode node = root;
		for (char c : s.toCharArray()) {
			if (!node.hasChild(c)) {
				return null;
			}
			else node = node.getChild(c);
		}
		return node.getRoads();
	}
	
	/** Get a list of all road objects whose label prefix matches the passed prefix
	 * 
	 * 
	 * @param prefix - search query, prefix we are searching
	 * @return List of all roads whose label prefix matches the passed prefix
	 */
	
	public List<Road> getAll(String prefix){
		List<Road> results = new ArrayList<Road>();
		TrieNode node = root;
		for (char c : prefix.toCharArray()) {
			if (node.hasChild(c)) {
				node = node.getChild(c);
			}
			else return results;
		}
		getAllFrom(node, results);
		return results;
	}
	
	/** Returns all road objects from a pre-determined node in the trie.
	 * 
	 * 
	 * @param node - the node we want to start searching from
	 * @param results - the list we want to store the road objects we find in
	 */
	
	public void getAllFrom(TrieNode node, List<Road> results){
		for (Road r : node.getRoads()) {
			results.add(r);
		}
		for (TrieNode n : node.getChildren().values()) {
			getAllFrom(n, results);
		}
	}
}
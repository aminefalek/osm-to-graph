import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class RoadNetwork extends DefaultHandler {
	
	// Name of the region (used when to save and load the graph)
	String region;
	
	// Number of Nodes and Edges in the Graph
	public int numNodes;
	public int numEdges;
	
	//Graph Adjacency lists for outgoing and incoming arcs
	public ArrayList<ArrayList<Edge>> outgoingEdges;
	public ArrayList<ArrayList<Edge>> incomingEdges;
	
	// List of all the nodes in the Graph
	public ArrayList<Node> nodes;
	
	// Maps osmId of a node to its index in the nodes list
	public Map<Long, Integer> osmIdToNodeIndex;
	
	// List of all road types in the osm file 
	public ArrayList<String> roadTypes;
	
	// Used to reference all the nodes that make up that particular "way" in the .osm file
	public ArrayList<Long> wayNodes;
	
	// Speed values based on road type, should be set according to the region
	public Map<String, Integer> speeds;	
	
	private boolean inWay = false;
	private boolean isHighway = false;
	private String key, valHighway, valOneway;
	
	// Constructor  
	public RoadNetwork(String region) throws NumberFormatException, IOException {
		this.region = region;
				
		numNodes = 0;
		numEdges = 0;
		
		outgoingEdges = new ArrayList<>();
		incomingEdges = new ArrayList<>();
		nodes = new ArrayList<>();
		
		speeds = new HashMap<>();
		speeds.put("motorway", 110);
		speeds.put("trunk", 110);
		speeds.put("primary", 70);
		speeds.put("secondary", 60);
		speeds.put("tertiary", 50);
		speeds.put("motorway_link", 50);
		speeds.put("trunk_link", 50);
		speeds.put("primary_link", 50);
		speeds.put("secondary_link", 50);
		speeds.put("road", 40);
		speeds.put("unclassified", 40);
		speeds.put("residential", 30);
		speeds.put("unsurfaced", 30);
		speeds.put("living_street", 10);
		speeds.put("service", 5);
	}
	
	private void addNode(long osmId, double latitude, double longitude) {
		Node node = new Node(osmId, latitude, longitude);
		
		outgoingEdges.add(new ArrayList<Edge>());
		incomingEdges.add(new ArrayList<Edge>());
		nodes.add(node);
		osmIdToNodeIndex.put(osmId, numNodes);
		numNodes += 1;
	}
	
	private void addEdge(int baseNode, int headNode, double length, double travelTime) {
		Edge outgoingEdge = new Edge(headNode, length, travelTime);
		Edge incomingEdge = new Edge(baseNode, length, travelTime);
		
		outgoingEdges.get(baseNode).add(outgoingEdge);
		incomingEdges.get(headNode).add(incomingEdge);
		numEdges += 1;
	}
	
	public void parseOsmFile(String osmFilepath) {
		osmIdToNodeIndex = new HashMap<>();
		wayNodes = new ArrayList<>();
		roadTypes = new ArrayList<>();
		valOneway = "no";
		
		File xmlDoc = new File(osmFilepath);
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();			
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(xmlDoc, this);		  
		}
		catch(Exception e) {
			System.out.println("Problem, " + e.toString());
		} 
	}
	
	// At beginning of tag
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equalsIgnoreCase("node")) {
			long osmId = Long.parseLong(attributes.getValue("id"));
			float lat = Float.parseFloat(attributes.getValue("lat"));
			float lon = Float.parseFloat(attributes.getValue("lon"));			
			addNode(osmId, lat, lon);
		}
		else if (qName.equalsIgnoreCase("way")) {
			inWay = true;
		}
		else if (qName.equalsIgnoreCase("nd") && inWay) {
			wayNodes.add(Long.parseLong(attributes.getValue("ref")));
		}
		else if (qName.equalsIgnoreCase("tag") && inWay) {
			key = attributes.getValue("k");
			if (key.equals("highway")) {
				isHighway = true;
				valHighway = attributes.getValue("v");
				if (!roadTypes.contains(valHighway)) {
					roadTypes.add(valHighway);
				}
			}			
			if (key.equals("oneway")) {
				valOneway = attributes.getValue("v");
			}
		}
	}
  
	// At end of tag
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equalsIgnoreCase("way") && isHighway && speeds.keySet().contains(valHighway)) {
			int speed = speeds.get(valHighway);
			
			// Insert edges for each two consecutive nodes in the way
			long baseNodeOsmId = wayNodes.get(0);
			int baseNode = osmIdToNodeIndex.get(baseNodeOsmId);
			
			for(int i=1; i<wayNodes.size(); i++) {
				long headNodeOsmId = wayNodes.get(i);
				int headNode = osmIdToNodeIndex.get(headNodeOsmId);
				
				double length = HaversineDistance.distance(nodes.get(baseNode).latitude, nodes.get(baseNode).longitude, nodes.get(headNode).latitude, nodes.get(headNode).longitude);
				double travelTime = length/speed;
				
				if (valOneway.equals("yes") || valOneway.equals("1")) {
					addEdge(baseNode, headNode, length, travelTime);
				}
				
				else if (valOneway.equals("-1")) {
					addEdge(headNode, baseNode, length, travelTime);						
				}
				
				else {
					addEdge(baseNode, headNode, length, travelTime);	
					addEdge(headNode, baseNode, length, travelTime);	
				}				
				
				baseNodeOsmId = headNodeOsmId;
				baseNode = headNode;
			}
			wayNodes.clear();
			valOneway = "no";
			inWay = false;
		}
	}

}

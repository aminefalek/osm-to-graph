import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class Main {
	
	public static void main(String[] args) throws NumberFormatException, IOException, ParserConfigurationException, SAXException{
		String osmFilepath = "strasbourg.osm";
		String region = "strasbourg";
		
		RoadNetwork graph = new RoadNetwork(region);
		graph.parseOsmFile(osmFilepath);
		
		System.out.println("Total number of nodes and edges:");
		System.out.println("nodes: " + graph.numNodes);
		System.out.println("edges: " + graph.numEdges);
		System.out.println();
		
		graph.reduceToLargestConnectedComponent();
		
		System.out.println("Largest component number of nodes and edges:");
		System.out.println("nodes: " + graph.numNodes);
		System.out.println("edges: " + graph.numEdges);
	}

}

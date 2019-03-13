import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class Main {
	
	public static void main(String[] args) throws NumberFormatException, IOException, ParserConfigurationException, SAXException{
		String osmFilepath = "strasbourg.osm";
		String region = "strasbourg";
		
		RoadNetwork graph = new RoadNetwork(region);
		graph.parseOsmFile(osmFilepath);
		
		System.out.println(graph.numNodes);
		System.out.println(graph.numEdges);
	}

}

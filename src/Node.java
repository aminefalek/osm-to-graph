
public class Node {
	// The OSM id of the node.
	public long osmId;

	public double latitude;
	public double longitude;
	
	public Node(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	@Override
	public String toString() {
		return (String.valueOf(latitude) + "," + String.valueOf(longitude));		
	}
}

package eu.scasefp7.eclipse.servicecomposition.views;
/**
 * 
 * @author mkoutli
 *Represents the edge of the workflow (zest graph)
 */
public class MyConnection {
	//edge attributes
	  final String id; 
	  final String label; 
	  final MyNode source;
	  final MyNode destination;
	  
	  public MyConnection(String id, String label, MyNode source, MyNode destination) {
	    this.id = id;
	    this.label = label;
	    this.source = source;
	    this.destination = destination;
	  }

	  public String getLabel() {
	    return label;
	  }
	  
	  public MyNode getSource() {
	    return source;
	  }
	  public MyNode getDestination() {
	    return destination;
	  }
	  
	} 

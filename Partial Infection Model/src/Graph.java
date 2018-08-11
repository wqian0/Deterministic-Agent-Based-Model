import java.util.ArrayList;
import java.util.HashMap;
public class Graph {
	private ArrayList<Vertice> vertices;
	private ArrayList<Edge> edgeList;
	private int numVertices;
	private int numEdges;
	private int dayID;
	public Graph(ArrayList<Vertice> vertices, ArrayList<Edge> edgeList, int dayID)
	{
		this.dayID =dayID;
		this.edgeList=edgeList;
		this.vertices=vertices;
		numVertices = vertices.size();
		numEdges = edgeList.size();
	}
	public int getNumVertices()
	{
		return numVertices;
	}
	public int getDayID()
	{
		return dayID;
	}
	public double getAvgDegree()
	{
		double total=0;
		for(Vertice v: vertices)
			total+=v.getEdges(dayID).size();
		total/=numVertices;
		return total;
	}
	public Vertice getVertex(String ID)
	{
		for(Vertice v: vertices)
			if(v.getID().equals(ID))
				return v;
		System.out.println(ID);
		return null;
	}
	public int getNumEdges()
	{
		return numEdges;
	}
	public ArrayList<Vertice> getVertices()
	{
		return vertices;
	}
	public ArrayList<Edge> getEdges()
	{
		return edgeList;
	}

	public void showEdges()
	{
		for(Edge e: edgeList)
		{
			System.out.println(e);
		}
	}
}

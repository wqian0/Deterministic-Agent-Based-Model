import java.util.ArrayList;
import java.util.HashMap;
public class Graph {
	private ArrayList<Vertice> vertices;
	private HashMap<String, Vertice> map;
	private ArrayList<Edge> edgeList;
	private int numVertices;
	private int numEdges;
	private int dayID;
	public Graph(ArrayList<Vertice> vertices, ArrayList<Edge> edgeList, int dayID)
	{
		this.dayID =dayID;
		this.edgeList=edgeList;
		this.vertices=vertices;
		map = new HashMap<>();
		for(Vertice v: vertices)
			map.put(v.getID(), v);
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
	public int getNumEdges()
	{
		return numEdges;
	}
	public ArrayList<Vertice> getVertices()
	{
		return vertices;
	}
	public Vertice getVertice(String ID)
	{
		return map.get(ID);
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

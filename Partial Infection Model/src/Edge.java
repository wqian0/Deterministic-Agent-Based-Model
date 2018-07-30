
public class Edge{
	private Vertice v1;
	private Vertice v2;
	private double weight;
	public Edge(Vertice v1, Vertice v2, double weight)
	{
		this.v1 = v1;
		this.v2= v2;
		this.weight=weight;
	}
	public double getWeight()
	{
		return weight;
	}
	public void setWeight(double input)
	{
		weight=input;
	}
	public Vertice getSource()
	{
		return v1;
	}
	public Vertice getTarget()
	{
		return v2;
	}
	public Vertice getOther(Vertice v)
	{
		if(v==v1)
			return v2;
		else if(v==v2)
			return v1;
		return null;
			
	}
	public String toString()
	{
		return v1.getID() + "\t" + v2.getID() +"\t" + weight;
	}
}

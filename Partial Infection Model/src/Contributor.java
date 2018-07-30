
public class Contributor {
	public Vertice v;
	public double PNI; //probability not infected as a result of v interacting with source node
	public Contributor(Vertice v, double PNI)
	{
		this.v=v;
		this.PNI=PNI;
	}
}

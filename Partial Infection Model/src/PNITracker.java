import java.util.ArrayList;

//Holds values for a single day. PNI = probability not infected
public class PNITracker {
	public double PNI;
	public ArrayList<Contributor> cL;
	public PNITracker()
	{
		PNI=1;
		cL=new ArrayList<>();
	}
	public PNITracker(double PNI, ArrayList<Contributor> cL)
	{
		this.PNI=PNI;
		this.cL=cL;
	}
	public double getPNI(Vertice v)
	{
		for(Contributor c: cL)
		{
			if(c.v==v)
				return c.PNI;
		}
		return 1.0;
	}
	public void setValues(PNITracker other)
	{
		PNI=other.PNI;
		cL=other.cL;
	}
	public void reset()
	{
		cL.clear();
		PNI=1;
	}
}

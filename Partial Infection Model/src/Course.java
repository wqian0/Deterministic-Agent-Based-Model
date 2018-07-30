import java.util.ArrayList;
public class Course implements Comparable<Course> {
	private ArrayList<Vertice> students;
	private Time time;
	private int ID;
	//private double inherentWeight;
	

	public Course(int ID, Time time)
	{
		//inherentWeight=0;
		this.ID = ID;
		this.time=time;
		students = new ArrayList<>();
	}
	public int getID()
	{ 
		return ID;
	}
	public ArrayList<Vertice> getStudents()
	{
		return students;
	}
	public int getNumStudents()
	{
		return students.size();
	}
	public ArrayList<Integer> getRandContactsList()
	{
		ArrayList<Integer> returnList = new ArrayList<>();
		for(int i=0; i<students.size(); i++)
		{
			returnList.add((int)(4*Math.sqrt(time.getDuration())));
		}
		return returnList;
	}
	public void show()
	{
		System.out.println("ID:" + ID + " Duration:"+time.getDuration()+" size:"+students.size());
	}
	public void addStudent(Vertice v)
	{
		students.add(v);
	}
	public Time getTime()
	{
		return time;
	}
	@Override 
	public int compareTo(Course c)
	{
		if(time.getStart()<c.getTime().getStart())
			return -1;
		else if(time.getStart()>c.getTime().getStart())
			return 1;
		return 0;
	}
}

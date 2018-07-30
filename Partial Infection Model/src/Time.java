import java.util.ArrayList;
public class Time{
	private ArrayList<Integer> days; //mon-friday = 0-4
	private double start; //14.5 would denote 2:30 pm
	private double end;
	public Time(ArrayList<Integer> days, double start, double end)
	{
		this.days = days;
		this.start = start;
		this.end = end;
	}
	public ArrayList<Integer> getDays()
	{
		return days;
	}
	public double getStart()
	{
		return start;
	}
	public double getEnd()
	{
		return end;
	}
	public double getDuration()
	{
		return end-start;
	}
}

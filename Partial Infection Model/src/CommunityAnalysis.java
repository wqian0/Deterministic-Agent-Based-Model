import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

public class CommunityAnalysis {
	
	static final String inputDirectory = "C:\\Simulation Input\\";
	
	public static HashMap<Integer, ArrayList<String>> getCommunities(Scanner sc)
	{
		HashMap<Integer, ArrayList<String>> communities = new HashMap<>(); 
		ArrayList<String> temp = new ArrayList<>();
		sc.nextLine();
		int current=0;
		
		while(sc.hasNextLine())
		{
			current=sc.nextInt();
			if(communities.containsKey(current))
				communities.get(current).add(sc.next());
			else
			{
				temp.add(sc.next());
				communities.put(current, new ArrayList<String>(temp));
				temp.clear();
			}
		}
		return communities;
	}
	public static ArrayList<Community> convertFormat(HashMap<Integer, ArrayList<String>> input)
	{
		Iterator<Integer> it = input.keySet().iterator();
		ArrayList<Community> result = new ArrayList<>();
		Community temp;
		Integer x;
		while(it.hasNext())
		{
			x= it.next();
			temp = new Community();
			temp.list=input.get(x);
			temp.communityID=x;
			result.add(temp);
		}
		return result;
	}
	public static ArrayList<ArrayList<String>> convertToList(HashMap<Integer, ArrayList<String>> input)
	{
		ArrayList<ArrayList<String>> result= new ArrayList<>();
		Iterator<Integer> it = input.keySet().iterator();
		while(it.hasNext())
		{
			result.add(input.get(it.next()));
		}
		return result;
	}
	public static int countUnion(ArrayList<ArrayList<String>> c1, ArrayList<ArrayList<String>> c2)
	{
		int intersection=0;
		int total=0;
		for(ArrayList<String> list1: c1)	
		{
			for(String s1: list1)
			{
				for(ArrayList<String> list2: c2)
				{
					for(String s2: list2)
					{
						if(s1.equals(s2))
							intersection++;
					}
				}
			}
		}
		for(ArrayList<String> list1: c1)	
		{
			total+=list1.size();
		}
		for(ArrayList<String> list2: c2)	
		{
			total+=list2.size();
		}
		return total-intersection;
	}
	public static double jaccardIndex(Community c1, Community c2)
	{
		double intersection=0;
		double total=0;
		for(String s1: c1.list)
		{
			for(String s2: c2.list)
			{
				if(s1.equals(s2))
					intersection++;
			}
		}
		total+=c1.list.size();
		total+=c2.list.size();
		return intersection/(total-intersection);
	}
	public static double calculateNMI(ArrayList<ArrayList<String>> c1, ArrayList<ArrayList<String>> c2, double totalSize)
	{
		double numerator=0;
		double denominator=0;
		double tempP=0;
		for(int i=0; i<c1.size(); i++)
		{
			for(int j=0; j<c2.size(); j++)
			{
				tempP=P(c1.get(i),c2.get(j),totalSize);
				if(tempP!=0)
					numerator+=tempP*Math.log10(tempP/(Pi_(c1.get(i),c2,totalSize)*P_j(c1,c2.get(j),totalSize)));
			}
		}
		numerator*=-2;
		for(int i=0; i<c1.size();i++)
		{
			tempP=Pi_(c1.get(i),c2,totalSize);
			if(tempP!=0)
				denominator+=tempP*Math.log10(tempP);
		}
		for(int j=0; j<c2.size();j++)
		{
			tempP=P_j(c1,c2.get(j),totalSize);
			if(tempP!=0)
				denominator+=tempP*Math.log10(tempP);
		}
		return numerator/denominator;
	}
	public static double P(ArrayList<String> L1, ArrayList<String> L2, double totalSize)
	{
		double count=0;
		for(String s: L1)
			for(String t: L2)
			{
				if(s.equals(t))
				{
					count++;
				}
			}
		return count/totalSize;
	}
	public static double Pi_(ArrayList<String> i, ArrayList<ArrayList<String>> j, double totalSize)
	{
		double total=0;
		for(int c=0; c<j.size(); c++)
			total+=P(i,j.get(c),totalSize);
		return total;
	}
	public static double P_j(ArrayList<ArrayList<String>> i, ArrayList<String> j, double totalSize)
	{
		double total=0;
		for(int c=0; c<i.size(); c++)
			total+=P(i.get(c),j,totalSize);
		return total;
	}
	public static double[][] getTable(ArrayList<Scanner> input)
	{
		double[][] results = new double[input.size()][input.size()];
		ArrayList<ArrayList<ArrayList<String>>> list = new ArrayList<>();
		for(Scanner s: input)
		{
			list.add(convertToList(getCommunities(s)));
			s.close();
		}
		int tempTotal;
		for(int i=0; i<list.size(); i++)
		{
			for(int j=0; j<list.size(); j++)
			{
				tempTotal = countUnion(list.get(i), list.get(j));
				results[i][j] = calculateNMI(list.get(i),list.get(j), tempTotal);
				
			}
		}
		return results;
	}
	
	public static void main(String[] args) throws IOException
	{
		Scanner Monday = new Scanner(new File(inputDirectory+"Monday\\Monday Duration Communities.txt"));
		Scanner Tuesday = new Scanner(new File(inputDirectory+"Tuesday\\Tuesday Duration Communities.txt"));
		Scanner Wednesday = new Scanner(new File(inputDirectory+"Wednesday\\Wednesday Duration Communities.txt"));
		Scanner Thursday = new Scanner(new File(inputDirectory+"Thursday\\Thursday Duration Communities.txt"));
		Scanner Friday = new Scanner(new File(inputDirectory+"Friday\\Friday Duration Communities.txt"));
		ArrayList<Scanner> input = new ArrayList<>();
		input.add(Monday); input.add(Tuesday); input.add(Wednesday); input.add(Thursday); input.add(Friday);
		double[][] result = getTable(input);
		
		for(int i=0; i<result.length; i++)
		{
			for(int j=0; j<result.length; j++)
			{
				System.out.printf("%.3f \t", result[i][j]);
			}
			System.out.println();
		}
	}
}

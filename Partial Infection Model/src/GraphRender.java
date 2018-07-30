
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
//import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GraphRender extends Application{
	DynamicSimulation DS;
	StaticSimulation SS;
	int weekday=0;
	static final int canvasSize = 1500;
	static final double radius=3; 
	Group root;
	HashMap<String, Vertice> IDmap;
	HashMap<Vertice, Location> map;
	ArrayList<Vertice> vertices;
	ArrayList<Edge> edges;

	// Windows-based directory. Use forward slash for Linux. 
	static final String inputDirectory = "C:\\Simulation Input\\";
	static final boolean fullGraphMode=true;
	static final int numDayGraphs=5;

	//Values for T, alpha, gamma, and contacts per hour. Alpha must be >=1.
	static final double transmissionProbability=0.3;
	static final int latentPeriod=30;
	static final int infectiousPeriod=15;
	static final int contactsPerHour=3;
	static final int numCentralities=4;

	public static void main(String[] args) {
		launch(args);
	}
	public void setLocationMap() throws FileNotFoundException
	{
		Scanner coordinates = new Scanner(new File(inputDirectory+"Coordinates.txt"));
		map=Main.getLocations(coordinates, IDmap);
		for(Vertice v: vertices)
			map.put(v, new Location(map.get(v).x/8.0+500,map.get(v).y/8.0+100));
	}
	public void setSim() throws FileNotFoundException
	{
		Scanner IDs = new Scanner(new File("C:\\Users\\Billy\\Documents\\Simple Weighted Communities\\IDList.txt"));

		// maps ID to vertices
		IDmap= new HashMap<>();
		String temp;
		while(IDs.hasNextLine())
		{
			temp =IDs.next();
			IDmap.put(temp, new Vertice(temp,5));
		}
		IDs.close();
		vertices = new ArrayList<>(IDmap.values());
		Scanner c_FullD = new Scanner(new File(inputDirectory+"Full Graph Duration Communities.txt"));
		HashMap<Integer, ArrayList<String>> commMap = CommunityAnalysis.getCommunities(c_FullD);
		c_FullD.close();

		Scanner nodeProperties = new Scanner(new File(inputDirectory+"Node Properties.txt"));

		Scanner v_Meta = new Scanner(new File(inputDirectory+"Meta ID List.txt"));
		PrintWriter pw = new PrintWriter("outfile.txt");
		HashMap<String, Vertice> metaMap = new HashMap<String, Vertice>();

		String metaTemp;
		while(v_Meta.hasNextLine())
		{
			metaTemp =v_Meta.nextLine();
			metaMap.put(metaTemp, new Vertice((String)metaTemp, 1));
		}
		v_Meta.close();
		v_Meta = new Scanner(new File(inputDirectory+"Meta ID List.txt"));
		Scanner e_Meta = new Scanner(new File(inputDirectory+"Meta Edge List.txt"));

		Graph Meta = Main.getGraph(v_Meta, e_Meta, metaMap, 0);
		v_Meta.close();
		double[][] dist = Main.allPairsSP(Meta, pw);

		Scanner courseList = new Scanner(new File(inputDirectory+"Courses.txt"));
		Scanner courseTimes = new Scanner(new File(inputDirectory+"CourseTimes.txt"));
		ArrayList<Course> courses = Main.initCourses(IDmap,courseTimes,courseList);
		ArrayList<ArrayList<Course>> coursesPerDay = new ArrayList<>();

		for(int i=0; i<numDayGraphs; i++)
		{
			coursesPerDay.add(new ArrayList<Course>());
		}
		for(Course c: courses)
			for(Integer day:c.getTime().getDays())
				coursesPerDay.get((int)day).add(c);

		Main.setContactsByDuration(vertices,coursesPerDay,contactsPerHour, fullGraphMode);
		Main.addCentralities(nodeProperties,IDmap,numCentralities);

		if(fullGraphMode) 
		{
			Scanner e_Full = new Scanner(new File("C:\\Users\\Billy\\Documents\\Simple Weighted Communities\\out_Alld.txt"));
			edges = Main.getEdges(e_Full, IDmap, 0);
			Graph Full = new Graph(vertices,edges,0);
			e_Full.close();
			SS = new StaticSimulation(Full,transmissionProbability,latentPeriod,infectiousPeriod);
		}
		else
		{
			Graph mondayGraph = Main.processDay("Monday",0, IDmap);
			Graph tuesdayGraph = Main.processDay("Tuesday",1, IDmap);
			Graph wednesdayGraph = Main.processDay("Wednesday",2, IDmap);
			Graph thursdayGraph = Main.processDay("Thursday",3, IDmap);
			Graph fridayGraph = Main.processDay("Friday",4, IDmap);

			Graph[] graphList = {mondayGraph,tuesdayGraph,wednesdayGraph,thursdayGraph,fridayGraph};

			DS = new DynamicSimulation(graphList,vertices,transmissionProbability,latentPeriod,infectiousPeriod);
		}

		Main.runRingVacc(Meta, commMap,IDmap, dist, 0, 0, 0, true);
	}


	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		primaryStage.setTitle("Agent-Based Disease Model");
		root = new Group();
		Canvas canvas = new Canvas(canvasSize, canvasSize);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		setSim();
		setLocationMap();
		gc.clearRect(0, 0, canvasSize, canvasSize);
		drawLines(gc);
		root.getChildren().add(canvas);
		if(fullGraphMode)
		{
			SS.setTrickler(vertices.get(0));
			drawShapesSS(gc);
			canvas.addEventHandler(MouseEvent.MOUSE_CLICKED,
					new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent t) {
					SS.runTrickleDay();
					gc.clearRect(0, 0, canvasSize, canvasSize);
					drawLines(gc);
					drawShapesSS(gc);	
					primaryStage.setTitle("Agent-Based Disease Model "+"Day "+SS.getDay());
				}
			});
		}
		else
		{
			DS.setTrickler(vertices.get(0));
			DS.setStartDay(vertices.get(0).getStartingPoint());
			drawShapesDS(gc);
			canvas.addEventHandler(MouseEvent.MOUSE_CLICKED,
					new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent t) {
					weekday=DS.getWeekday();
					DS.runTrickleDay();
					gc.clearRect(0, 0, canvasSize, canvasSize);
					drawLines(gc);
					drawShapesDS(gc);	
					primaryStage.setTitle("Agent-Based Disease Model "+"Day "+DS.getDay());
				}
			});
		}
	
		primaryStage.setScene(new Scene(root));
		primaryStage.show();
	}
	private void resetGc(GraphicsContext gc) {
		gc.setStroke(Color.GREY);
		gc.setLineWidth(.3);
	}
	private void drawLines(GraphicsContext gc) {
		resetGc(gc);
		if(fullGraphMode)
		{
			for(Edge e: edges)
			{
				gc.strokeLine(map.get(e.getSource()).x, map.get(e.getSource()).y, map.get(e.getTarget()).x, map.get(e.getTarget()).y);
			}
		}
		else
		{
			for(Edge e: DS.getGraph(weekday).getEdges())
			{
				gc.strokeLine(map.get(e.getSource()).x, map.get(e.getSource()).y, map.get(e.getTarget()).x, map.get(e.getTarget()).y);
			}
		}
	}

	private void drawShapesSS(GraphicsContext gc)
	{
		gc.setStroke(Color.BLACK);
		gc.setLineWidth(1);
		int colorValue=255;
		Color color;
		for(Vertice v: vertices)
		{
			if(v.getRecoveryState())
			{
				if(v.getState()!=Vertice.HealthState.vaccinated&&!v.getVaccinationState())
				{
					color=Color.LIGHTGREEN;
				}
				else
					color=Color.BLUE;
			}
			else
				color=Color.rgb(255, (int)(255-255*v.getCumulation()), 0);
			gc.setFill(color);
			gc.fillOval(map.get(v).x-radius, map.get(v).y-radius, 2*radius, 2*radius);
			gc.strokeOval(map.get(v).x-radius, map.get(v).y-radius, 2*radius, 2*radius);
		}
	}
	private void drawShapesDS(GraphicsContext gc)
	{
		gc.setStroke(Color.BLACK);
		gc.setLineWidth(1);
		int colorValue=255;
		Color color;
		for(Vertice v: vertices)
		{
			if(v.getRecoveryState())
			{
				if(v.getState()!=Vertice.HealthState.vaccinated&&!v.getVaccinationState())
				{
					color=Color.LIGHTGREEN;
				}
				else
					color=Color.BLUE;
			}
			else
				color=Color.rgb(255, (int)(255-255*v.getCumulation()), 0);
			gc.setFill(color);
			if(v.getEdges(weekday).size()!=0)
			{
				gc.fillOval(map.get(v).x-radius, map.get(v).y-radius, 2*radius, 2*radius);
				gc.strokeOval(map.get(v).x-radius, map.get(v).y-radius, 2*radius, 2*radius);
			}
		}
	}


}



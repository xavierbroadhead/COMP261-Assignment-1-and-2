import java.util.ArrayList;
import java.util.List;

public class Road {
	
	private int ID;
	private int type;
	private String label;
	private String city;
	private boolean oneWay;
	private int speed;
	private int roadClass;
	private boolean notForCar;
	private boolean notForPed;
	private boolean notForBike;
	private List<Segment> segments = new ArrayList<Segment>();
	
	public Road(int ID, int type, String label, String city, boolean oneWay, int speed, int roadClass, boolean notForCar, boolean notForPed, boolean notForBike) {
		this.ID = ID;
		this.type = type;
		this.label = label;
		this.city = city;
		this.oneWay = oneWay;
		this.speed = speed;
		this.roadClass = roadClass;
		this.notForCar = notForCar;
		this.notForPed = notForPed;
		this.notForBike = notForBike;
	}

	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public boolean isOneWay() {
		return oneWay;
	}

	public void setOneWay(boolean oneWay) {
		this.oneWay = oneWay;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public int getRoadClass() {
		return roadClass;
	}

	public void setRoadClass(int roadClass) {
		this.roadClass = roadClass;
	}

	public boolean isNotForCar() {
		return notForCar;
	}

	public void setNotForCar(boolean notForCar) {
		this.notForCar = notForCar;
	}

	public boolean isNotForPed() {
		return notForPed;
	}

	public void setNotForPed(boolean notForPed) {
		this.notForPed = notForPed;
	}

	public boolean isNotForBike() {
		return notForBike;
	}
	
	public boolean addSegment(Segment s) {
		return segments.add(s);
	}
	
	public List<Segment> getSegments(){
		return segments;
	}

	public void setNotForBike(boolean notForBike) {
		this.notForBike = notForBike;
	}
}

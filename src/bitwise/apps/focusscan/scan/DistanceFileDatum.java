package bitwise.apps.focusscan.scan;

import java.util.ArrayList;

public class DistanceFileDatum {
	
	private int imageNumber = 0;
	private ArrayList<Float> distanceInMeters = new ArrayList<Float>();
	private String path = "";
	
	public DistanceFileDatum() {
		
	}
	
	public int getImageNumber() {
		return imageNumber;
	}
	
	public void setImageNumber(int in) {
		imageNumber = in;
	}
	
	public ArrayList<Float> getDistanceInMeters() {
		return distanceInMeters;
	}
	
	public void setDistanceInMeters(ArrayList<Float> in) {
		distanceInMeters = in;
	}
	
	public void setPath(String in) {
		path = in;
	}
	
	public String getPath() {
		return path;
	}
}

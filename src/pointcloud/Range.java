package pointcloud;

public class Range {
	public final int start;
	public final int end;
	public final double intensity;

	public Range(int s, int e, double in_intensity) {
		start = s;
		end = e;
		intensity = in_intensity;
	}
	
	public Range(int s, int e) {
		start = s;
		end = e;
		intensity = 128;
	}
}

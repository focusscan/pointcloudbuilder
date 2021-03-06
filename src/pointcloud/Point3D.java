package pointcloud;

public class Point3D {
	private float x = 0;
	private float y = 0;
	private float z = 0;
	private float color = 0;
	
	public Point3D() {
	}
	
	public Point3D(float xx, float yy, float zz, float in_color) {
		x = xx;
		y = yy;
		z = zz;
		color = in_color;
	}
	
	public float getX() { return x; }
	public float getY() { return y; }
	public float getZ() { return z; }
	
	public String toPCDString() {
		return x + " " + y + " " + z + " " + color;
	}

}

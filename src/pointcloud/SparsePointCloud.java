package pointcloud;

import java.util.ArrayList;

public class SparsePointCloud {
	private int width;
	private int height;
	private ArrayList<ArrayList<ArrayList<Integer>>> pointCloud;
	
	public SparsePointCloud(int w, int h) {
		width = w;
		height = h;
		pointCloud = new ArrayList<ArrayList<ArrayList<Integer>>>(height);

		for (int i = 0; i < height; i++) {
			pointCloud.add(new ArrayList<ArrayList<Integer>>(width));
			for (int j = 0; j < width; j++) {
				pointCloud.get(i).add(new ArrayList<Integer>());
			}
		}
	}
	
	public void addPoint(int x, int y, int z) {
		pointCloud.get(y).get(x).add(z);
	}
	
	public ArrayList<Range> getSlugs(int x, int y, int skip) {
		ArrayList<Range> slugs = new ArrayList<Range>();
		ArrayList<Integer> xy = pointCloud.get(y).get(x);
		xy.sort(null);
		
		int start = -1;
		int end = -1;
		
		for (Integer zind : xy) {
			if (skip == -1) {
				slugs.add(new Range(zind, zind));
			} else if (start == -1) {
				start = zind;
				end = zind;
				if (zind == xy.get(xy.size() - 1)) {
					slugs.add(new Range(zind, zind));
				}
			} else {
				if (zind - end < skip) {
					end = zind;
				} 
				
				if (zind - end >= skip || zind == xy.get(xy.size() - 1)) {
					slugs.add(new Range(start, end));
					start = -1;
					end = -1;
				}
			}
		}
		return slugs;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
}

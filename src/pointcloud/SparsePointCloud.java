package pointcloud;

import java.util.ArrayList;
import java.util.Comparator;

public class SparsePointCloud {
	public static class Datum {
		public final int z;
		public final double intensity;
		
		public Datum(int in_z, double in_intensity) {
			z = in_z;
			intensity = in_intensity;
		}
	}
	
	private int width;
	private int height;
	private ArrayList<ArrayList<ArrayList<Datum>>> pointCloud;
	
	public SparsePointCloud(int w, int h) {
		width = w;
		height = h;
		pointCloud = new ArrayList<ArrayList<ArrayList<Datum>>>(height);

		for (int i = 0; i < height; i++) {
			pointCloud.add(new ArrayList<ArrayList<Datum>>(width));
			for (int j = 0; j < width; j++) {
				pointCloud.get(i).add(new ArrayList<Datum>());
			}
		}
	}
	
	public void addPoint(int x, int y, int z, double intensity) {
		pointCloud.get(y).get(x).add(new Datum(z, intensity));
	}
	
	public ArrayList<Range> getSlugs(int x, int y) {
		ArrayList<Datum> xy = pointCloud.get(y).get(x);
		if (null == xy || xy.size() <= 1)
			return null;
		
		xy.sort(new Comparator<Datum>() {
			@Override
			public int compare(Datum a, Datum b) {
				return Double.compare(b.intensity, a.intensity);
			}
		});
		
		double max_intensity = xy.get(0).intensity;
		double sum_intensity = 0;
		int i = 0;
		Datum a = null;
		Datum b = null;
		
		ArrayList<Range> rs = new ArrayList<>();
		for (double w = 0.9; w > 0.04; w -= 0.1) {
			for (Datum d : xy) {
				i++;
				sum_intensity += d.intensity;
				
				if (null == a) {
					a = d;
					b = d;
				}
				else {
					if (d.intensity / max_intensity < w) {
						if (null != a && null != b)
							rs.add(new Range(a.z, b.z, sum_intensity / i));
					}
					if (d.z < a.z)
						a = d;
					if (b.z < d.z)
						b = d;
				}
			}
		}
		
		return rs;
	}
	
	public Range getSlug0(int x, int y) {
		ArrayList<Datum> xy = pointCloud.get(y).get(x);
		if (null == xy || xy.size() <= 1)
			return null;
		
		double mean = 0;
		for (Datum d : xy) {
			mean += d.intensity;
		}
		mean = mean / xy.size();
		
		// Sample standard deviation, using Bessel's correction
		double stddev = 0;
		for (Datum d : xy) {
			double diff = d.intensity - mean;
			stddev += diff * diff;
		}
		stddev = Math.sqrt(stddev / (xy.size() - 1));
		
		Datum a = null;
		Datum b = null;
		for (Datum d : xy) {
			if (d.intensity > mean + 1 * stddev) {
				if (null == a)
					a = d;
				else
					b = d;
			}
		}
		
		if (null == a || null == b)
			return null;
		
		return new Range(a.z, b.z);
	}
	
	public ArrayList<Range> getSlugs0(int x, int y, int skip) {
		ArrayList<Range> slugs = new ArrayList<Range>();
		ArrayList<Datum> xy = pointCloud.get(y).get(x);
		xy.sort(new Comparator<Datum>() {
			@Override
			public int compare(Datum a, Datum b) {
				return Integer.compare(a.z, b.z);
			}
		});
		
		int start = -1;
		int end = -1;
		
		for (Datum z : xy) {
			final int zind = z.z;
			if (skip == -1) {
				slugs.add(new Range(zind, zind));
			} else if (start == -1) {
				start = zind;
				end = zind;
				if (zind == xy.get(xy.size() - 1).z) {
					slugs.add(new Range(zind, zind));
				}
			} else {
				if (zind - end < skip) {
					end = zind;
				} 
				
				if (zind - end >= skip || zind == xy.get(xy.size() - 1).z) {
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

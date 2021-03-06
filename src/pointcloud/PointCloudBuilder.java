package pointcloud;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import bitwise.apps.focusscan.scan.DistanceFile;
import bitwise.apps.focusscan.scan.EdgeFile;
import bitwise.apps.focusscan.scan.EdgeFileDatum;
import config.Config;
import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;

public class PointCloudBuilder {

	public static void main(String[] args) {
		
		if (args.length < 1) {
			System.out.println("Usage: PointCloud config-file");
			return;
		}
		
		try {
			Config config = new Config(Paths.get(args[0]));
			Path configDir = Paths.get(args[0]).getParent();
			EdgeFile edgeManifest = new EdgeFile(configDir.resolve(config.getEdgeManifestPath()));
			DistanceFile distManifest = new DistanceFile(configDir.resolve(config.getDistManifestPath()));
			Path scanDir = configDir.resolve(config.getEdgeManifestPath()).getParent();
			
			ArrayList<Point3D> output = new ArrayList<Point3D>();
			
			float[] avgPixelValues = null;
			ArrayList<float[]> oldPixelData = new ArrayList<float[]>();

			long[] dims = {-1, -1};
			long[] maxdims = {-1, -1};

			int numPoints = 0;
			
			SparsePointCloud pc = null;
			DistanceFunction distances = new DistanceFunction(distManifest, config);
			for (EdgeFileDatum e : edgeManifest.getData()) {
				int imageNum = e.getImageNumber();
				
				int file_id = H5.H5Fopen(scanDir.resolve(e.getPath()).toString(), HDF5Constants.H5F_ACC_RDONLY, HDF5Constants.H5P_DEFAULT);
				int dataset_id = H5.H5Dopen(file_id, "/img_data", HDF5Constants.H5P_DEFAULT);
				if (dims[0] == -1) {
					H5.H5Sget_simple_extent_dims(H5.H5Dget_space(dataset_id), dims, maxdims);
				}
				float[] pixelData = new float[(int) (dims[0] * dims[1])];			
				
				H5.H5Dread(dataset_id, HDF5Constants.H5T_NATIVE_FLOAT, HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, pixelData);
				H5.H5Dclose(dataset_id);
				H5.H5Fclose(file_id);
				
				if (avgPixelValues == null) {
					avgPixelValues = new float[(int)(dims[0] * dims[1])];
					pc = new SparsePointCloud((int)dims[0], (int)dims[1]);
				}
				
				for (int i = 0; i < pixelData.length; i++) {
					avgPixelValues[i] += Math.abs(pixelData[i]);
					if (oldPixelData.size() >= config.getMovingAverageWindow()) {
						avgPixelValues[i] -= Math.abs(oldPixelData.get(0)[i]);
					}
				}
				
				if (oldPixelData.size() >= config.getMovingAverageWindow()) {
					oldPixelData.remove(0);
				}
				oldPixelData.add(pixelData);
				
				System.out.print("*");
				
				// Crop in by 5 pixels to remove edge noise
				for (int i = 5; i < dims[0]-5; i++) {
					for (int j = 5; j < dims[1]-5; j++) {						
						double intensity = avgPixelValues[(int)(j * dims[0] + i)] / config.getMovingAverageWindow();
						if (intensity > config.getNoiseThreshold()) {
							++numPoints;
							pc.addPoint(i, j, imageNum - 1, intensity);
						}
					}
				}
			}

			for (int y = 0; y < pc.getHeight(); y++) {
				for (int x = 0; x < pc.getWidth(); x++) {
					ArrayList<Range> rs = pc.getSlugs(x, y);
					if (null == rs || 0 == rs.size())
						continue;
					
					float distance = 0;
					double intensity = 0;
					for (Range r : rs) {
						double d1 = distances.getDistance(r.start);
						double d2 = distances.getDistance(r.end);
						
						distance += (float) (2 * d1 * d2 / (d1 + d2));
						if (r.intensity > intensity)
							intensity = r.intensity;
					}
					distance = distance / rs.size();
					intensity = Math.min(255, intensity);
					
					// Compute the width/height (in meters) of the x and y axes for the current plane
					float width = (float) (36.0 / distManifest.get35MMFocalLength() * distance);
					float height = (float) (24.0 / distManifest.get35MMFocalLength() * distance);
					
					int color = (int) (0x00ff0000 * (intensity / 255) + 0x000000ff * (1 - intensity / 255));

					output.add(new Point3D(
								width / dims[0] * x - width,
								height - height / dims[1] * y,
								(float)(Math.tan(config.getZAxisShear()) * (height - height / dims[1] * y) - distance * config.getDistanceScalar()),
								color));
					
					/*
					// All the points are stored in "slugs" in the z-axis.  Each slug gets compressed to a single point at its center of mass
					ArrayList<Range> xySlugs = pc.getSlugs(x, y, config.getZAxisSegmentSkip());
					for (Range r : xySlugs) {
						float distance = distances.getDistance((r.start + r.end) / 2);
						if ((r.start + r.end) % 2 != 0) {
							distance += distances.getDistance((r.start + r.end) / 2 + 1);
							distance /= 2.0f;
						}
						
						// Compute the width/height (in meters) of the x and y axes for the current plane
						float width = (float) (36.0 / distManifest.get35MMFocalLength()) * distance;
						float height = (float) (24.0 / distManifest.get35MMFocalLength()) * distance;

						output.add(new Point3D(
									width / dims[0] * x - width,
									height - height / dims[1] * y,
									(float)(Math.tan(config.getZAxisShear()) * (height - height / dims[1] * y) - distance * config.getDistanceScalar())));
					}
					*/
				}
			}
			
			System.out.println(numPoints);
			PCDWriter.writePointCloud(configDir.resolve(config.getOutputFile().toString()), output);
		} catch (Exception e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

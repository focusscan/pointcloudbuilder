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
		}
		
		try {
			Config config = new Config(Paths.get(args[0]));
			EdgeFile edgeManifest = new EdgeFile(config.getEdgeManifestPath());
			DistanceFile distManifest = new DistanceFile(config.getDistManifestPath());
			Path scanDir = config.getEdgeManifestPath().getParent();
			
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
						
						// I don't totally understand why this needs to be reversed here, but it produces bad output if you 
						// use the (more natural) i * dims[1] + j for the index.  It must have something to do with how the
						// data is stored in the HDF array, but it seems odd that the heatmap application didn't have the same
						// problem...
						if (avgPixelValues[(int)(j * dims[0] + i)] / config.getMovingAverageWindow() > config.getNoiseThreshold()) {	// TODO not quite correct at the ends
							++numPoints;
							pc.addPoint(i, j, imageNum - 1);
						}
					}
				}
			}

			for (int y = 0; y < pc.getHeight(); y++) {
				for (int x = 0; x < pc.getWidth(); x++) {
					
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
				}
			}
			
			System.out.println(numPoints);
			PCDWriter.writePointCloud(config.getOutputFile().toString(), output);
		} catch (Exception e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

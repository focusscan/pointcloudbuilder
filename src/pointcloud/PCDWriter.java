package pointcloud;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class PCDWriter {
	
	public static void writePointCloud(String filename, ArrayList<Point3D> points) throws IOException {
		Path output = Paths.get(filename);
		ArrayList<String> lines = new ArrayList<String>(Arrays.asList(
				"# .PCD v.7 - Point Cloud Data file format",
				"VERSION .7",
				"FIELDS x y z",
				"SIZE 4 4 4",
				"TYPE F F F",
				"WIDTH " + points.size(),
				"HEIGHT 1",
				"VIEWPOINT 0 0 0 1 0 0 0",
				"POINTS " + points.size(),
				"DATA ascii"
				));
		
		for (Point3D p : points) {
			if (Math.abs(p.getX() - -0.056585252) < 0.0001 && 
				Math.abs(p.getY() - -2.2725E-4) < 0.0001 &&
				Math.abs(p.getZ() - 0.89) < 0.0001)
				System.out.println("ding");
			lines.add(p.toPCDString());
		}
		
		Files.write(output, lines);	
	}
}

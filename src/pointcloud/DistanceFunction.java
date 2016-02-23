package pointcloud;

import java.util.ArrayList;
import java.util.Collections;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math3.stat.regression.RegressionResults;

import bitwise.apps.focusscan.scan.DistanceFile;
import bitwise.apps.focusscan.scan.DistanceFileDatum;
import config.Config;

public class DistanceFunction {
	ArrayList<Double> points;
	
	public DistanceFunction(DistanceFile distManifest, Config config) {
		ArrayList<Double> inputPoints = new ArrayList<Double>(distManifest.getData().size());
		for (DistanceFileDatum d : distManifest.getData()) {
			if (config.getRangeValue() == Config.RangeValueOption.High) {
				inputPoints.add((double)Collections.max(d.getDistanceInMeters()));
			} else if (config.getRangeValue() == Config.RangeValueOption.Mean) {
				double avg = 0.0;
				for (Float dist : d.getDistanceInMeters()) {
					avg += dist / d.getDistanceInMeters().size();
				}
				inputPoints.add(avg);
			} else if (config.getRangeValue() == Config.RangeValueOption.Low) {
				inputPoints.add((double)Collections.min(d.getDistanceInMeters()));
			}
		}
		
		if (config.getInterpolationMethod() == Config.InterpolationMethod.None) {
			points = inputPoints;
		} else if (config.getInterpolationMethod() == Config.InterpolationMethod.Linear) {
			points = linearRegression(inputPoints);
		} else if (config.getInterpolationMethod() == Config.InterpolationMethod.Quadratic) {
			points = quadraticLeastSquares(inputPoints);
		}
	}
	
	public float getDistance(int i) {
		return (float)points.get(i).doubleValue();
	}
	
	public float getAverageDistance() {
		float sum = 0.0f;
		for (Double point : points) {
			sum += point;
		}
		return sum / points.size();
	}
	
	private ArrayList<Double> linearRegression(ArrayList<Double> inputPoints) {
		SimpleRegression r = new SimpleRegression(true);
		for (int i = 0; i < inputPoints.size(); i++) {
			r.addData(i, inputPoints.get(i));
		}
		RegressionResults results = r.regress();
		ArrayList<Double> outputPoints = new ArrayList<Double>(inputPoints.size());
		for (int i = 0; i < inputPoints.size(); i++) {
			outputPoints.add(results.getParameterEstimate(0) + results.getParameterEstimate(1) * i);
		}
		return outputPoints;
	}

	private ArrayList<Double> quadraticLeastSquares(ArrayList<Double> inputPoints) {
		OLSMultipleLinearRegression r = new OLSMultipleLinearRegression();
		double[][] data = new double[inputPoints.size()][2];
		for (int i = 0; i < inputPoints.size(); i++) {
			data[i][0] = i;
			data[i][1] = i * i;
		}
		double[] y = new double[inputPoints.size()];
		for (int i = 0; i < inputPoints.size(); i++) {
			y[i] = inputPoints.get(i).doubleValue();
		}
		r.newSampleData(y, data);
		double[] b = r.estimateRegressionParameters();
		
		ArrayList<Double> outputPoints = new ArrayList<Double>(inputPoints.size());
		for (int i = 0; i < inputPoints.size(); i++) {
			outputPoints.add(b[0] + i * b[1] + i * i * b[2]);
		}
		return outputPoints;
	}

}

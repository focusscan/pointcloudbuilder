package config;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class Config {
	public enum PointCloudFormat { Binary, ASCII };
	public enum InterpolationMethod { None, Linear, Quadratic };
	public enum RangeValueOption { High, Mean, Low };
	
	private Path edgeManifestPath;
	private Path distManifestPath;

	private Path outputFile;

	private PointCloudFormat outputFormat;
	private float movingAverageWindow = 1;
	private float noiseThreshold = 0;
	private int zAxisSegmentSkip = -1;
	private float zAxisShearAngle = 0;

	private float distanceScalar = 1;
	private InterpolationMethod interpolationMethod = InterpolationMethod.None;
	private RangeValueOption rangeValue = RangeValueOption.Low;
	
	public Config(Path in) throws ParserConfigurationException, SAXException, IOException, ConfigException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		DocumentBuilder db = dbf.newDocumentBuilder(); 
		Document doc = db.parse(in.toFile());
		
		Node makePointCloud = doc.getFirstChild();
		while (null != makePointCloud && (Node.ELEMENT_NODE != makePointCloud.getNodeType() || !makePointCloud.getNodeName().equalsIgnoreCase("makePointCloud"))) {
			makePointCloud = makePointCloud.getNextSibling();
		}
		
		
		for (Node output = makePointCloud.getFirstChild(); null != output; output = output.getNextSibling()) {

			if (Node.ELEMENT_NODE == output.getNodeType() && output.getNodeName().equalsIgnoreCase("pcInput")) {
				NamedNodeMap atts = output.getAttributes();
				Node edge = atts.getNamedItem("edgeManifest");
				if (null == edge)
					throw new NoEdgePathDefinedException();
				edgeManifestPath = Paths.get(edge.getNodeValue());
				
				Node dist = atts.getNamedItem("distManifest");
				if (null == dist)
					throw new NoDistPathDefinedException();
				distManifestPath = Paths.get(dist.getNodeValue());
			}

			if (Node.ELEMENT_NODE == output.getNodeType() && output.getNodeName().equalsIgnoreCase("pcOutput")) {
				NamedNodeMap atts = output.getAttributes();
				Node name = atts.getNamedItem("name");
				if (null == name)
					throw new NoNameSpecifiedException();
				outputFile = Paths.get(name.getNodeValue());
				
				Node format = atts.getNamedItem("format");
				if (null == format)
					throw new NoFormatSpecifiedException();
				
				if (format.getNodeValue().equalsIgnoreCase("binary")) {
					// TODO
					System.err.println("Binary PCD format currently unsupported; using ASCII");
					outputFormat = PointCloudFormat.ASCII;
				} else if (format.getNodeValue().equalsIgnoreCase("ascii")) {
					outputFormat = PointCloudFormat.ASCII;
				} else {
					throw new InvalidFormatException(format.getNodeValue());
				}
			}
			
			else if (Node.ELEMENT_NODE == output.getNodeType() && output.getNodeName().equalsIgnoreCase("pcFilter")) {
				NamedNodeMap atts = output.getAttributes();
				Node attMovingAverageWindow = atts.getNamedItem("movingAverageWindow");
				Node attNoiseThreshold = atts.getNamedItem("noiseThreshold");
				Node attZAxisSegmentSkip = atts.getNamedItem("zAxisSegmentSkip");
				Node attZAxisShearAngle = atts.getNamedItem("zAxisShearAngle");
				
				if (null != attMovingAverageWindow) {
					movingAverageWindow = Float.parseFloat(attMovingAverageWindow.getNodeValue());
				}
				if (null != attNoiseThreshold) {
					noiseThreshold = Float.parseFloat(attNoiseThreshold.getNodeValue());
				}
				if (null != attZAxisSegmentSkip) {
					zAxisSegmentSkip = Integer.parseInt(attZAxisSegmentSkip.getNodeValue());
				}
				if (null != attZAxisShearAngle) {
					zAxisShearAngle = Float.parseFloat(attZAxisShearAngle.getNodeValue());
				}
			}

			else if (Node.ELEMENT_NODE == output.getNodeType() && output.getNodeName().equalsIgnoreCase("pcDistance")) {
				NamedNodeMap atts = output.getAttributes();
				Node attScaleBy = atts.getNamedItem("scaleBy");
				Node attInterpolationMethod = atts.getNamedItem("interpolationMethod");
				Node attRangeValue = atts.getNamedItem("rangeValue");
				
				if (null != attScaleBy) {
					distanceScalar = Float.parseFloat(attScaleBy.getNodeValue());
				}
				if (null != attInterpolationMethod) {
					if (attInterpolationMethod.getNodeValue().equalsIgnoreCase("none")) {
						interpolationMethod = InterpolationMethod.None;
					} else if (attInterpolationMethod.getNodeValue().equalsIgnoreCase("linear")) {
						interpolationMethod = InterpolationMethod.Linear;
					} else if (attInterpolationMethod.getNodeValue().equalsIgnoreCase("quadratic")) {
						interpolationMethod = InterpolationMethod.Quadratic;
					}
				}
				if (null != attRangeValue) {
					if (attRangeValue.getNodeValue().equalsIgnoreCase("high")) {
						rangeValue = RangeValueOption.High;
					} else if (attRangeValue.getNodeValue().equalsIgnoreCase("mean")) {
						rangeValue = RangeValueOption.Mean;
					} else if (attRangeValue.getNodeValue().equalsIgnoreCase("low")) {
						rangeValue = RangeValueOption.Low;
					}
				}
			}
		}
	}
	
	public Path getEdgeManifestPath() {
		return edgeManifestPath;
	}
	
	public Path getDistManifestPath() {
		return distManifestPath;
	}
	
	public Path getOutputFile() {
		return outputFile;
	}
	
	public PointCloudFormat getOutputFormat() {
		return outputFormat;
	}
	
	public float getMovingAverageWindow() {
		return movingAverageWindow;
	}

	public float getNoiseThreshold() {
		return noiseThreshold;
	}

	public int getZAxisSegmentSkip() {
		return zAxisSegmentSkip;
	}

	public float getZAxisShear() {
		return zAxisShearAngle;
	}

	public float getDistanceScalar() {
		return distanceScalar;
	}

	public InterpolationMethod getInterpolationMethod() {
		return interpolationMethod;
	}

	public RangeValueOption getRangeValue() {
		return rangeValue;
	}

}

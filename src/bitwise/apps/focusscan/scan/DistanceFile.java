package bitwise.apps.focusscan.scan;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class DistanceFile {
	private static final String SCAN_DISTANCE = "scanDistance";
	private static final String FOCAL_LENGTH = "focalLength";
	private static final String PROPERTIES = "properties";
	private static final String SCAN_PATH = "scanPath";
	private static final String DATA = "data";
	private static final String PATH = "path";
	private static final String IMAGE = "image";
	private static final String DISTANCE_ARRAY = "distanceArray";
	private static final String IMAGE_NUMBER = "imageNumber";
	private static final String DISTANCE_METERS = "distanceInMeters";
	
	private String scanPath = "";
	private float focalLength = 0.0f;
	
	private List<DistanceFileDatum> data = new ArrayList<>();
	
	public DistanceFile() {
		
	}
	
	public DistanceFile(Path in) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		DocumentBuilder db = dbf.newDocumentBuilder(); 
		Document doc = db.parse(in.toFile());
		
		Node scanDist = doc.getFirstChild();
		while (null != scanDist && (Node.ELEMENT_NODE != scanDist.getNodeType() || !scanDist.getNodeName().equalsIgnoreCase(SCAN_DISTANCE))) {
			scanDist = scanDist.getNextSibling();
		}
		assert (null != scanDist);
		
		// Scan properties
		{
			Node properties = scanDist.getFirstChild();
			while (null != properties && (Node.ELEMENT_NODE != properties.getNodeType() || !properties.getNodeName().equalsIgnoreCase(PROPERTIES))) {
				properties = properties.getNextSibling();
			}
			assert (null != properties);
			
			for (Node prop = properties.getFirstChild(); prop != null; prop = prop.getNextSibling()) {
				if (Node.ELEMENT_NODE == prop.getNodeType()) {
					if (prop.getNodeName().equalsIgnoreCase(SCAN_PATH)) {
						scanPath = prop.getTextContent();
					}
					
					if (prop.getNodeName().equalsIgnoreCase(FOCAL_LENGTH)) {
						focalLength = Float.parseFloat(prop.getTextContent());
					}
				}
			}
		}
		// End scan properties
		
		Node dataNode = scanDist.getFirstChild();
		while (null != dataNode && (Node.ELEMENT_NODE != dataNode.getNodeType() || !dataNode.getNodeName().equalsIgnoreCase(DATA))) {
			dataNode = dataNode.getNextSibling();
		}
		assert (null != dataNode);
		for (Node capture = dataNode.getFirstChild(); capture != null; capture = capture.getNextSibling()) {
			if (Node.ELEMENT_NODE == capture.getNodeType() && capture.getNodeName().equalsIgnoreCase(IMAGE)) {
				int imageNumber = -1;
				ArrayList<Float> distance = new ArrayList<Float>();
				String path = null;
				
				imageNumber = Integer.parseInt(capture.getAttributes().getNamedItem(IMAGE_NUMBER).getNodeValue());
				assert (imageNumber >= 0);
				
				for (Node prop = capture.getFirstChild(); prop != null; prop = prop.getNextSibling()) {
					if (Node.ELEMENT_NODE == prop.getNodeType()) {
						if (prop.getNodeName().equalsIgnoreCase(DISTANCE_ARRAY)) {
							for (Node dist = prop.getFirstChild(); dist != null; dist = dist.getNextSibling()) {
								distance.add(Float.parseFloat(dist.getTextContent()));
							}
						}
						else if (prop.getNodeName().equalsIgnoreCase(PATH)) {
							path = prop.getTextContent();
						}
					}
				}
				
				DistanceFileDatum datum = new DistanceFileDatum();
				datum.setImageNumber(imageNumber);
				datum.setDistanceInMeters(distance);
				datum.setPath(path);
				data.add(datum);
			}
		}
	}
	
	public boolean saveToFile(Path out) {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		try {
			Transformer transformer = transformerFactory.newTransformer();
			transformer.transform(getXml(), new StreamResult(out.toFile()));
		} catch (TransformerException e) {
			return false;
		}
		
		return true;
	}
	
	public DOMSource getXml() {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		Document doc = docBuilder.newDocument();
		
		Element scanDistE = doc.createElement(SCAN_DISTANCE);
		doc.appendChild(scanDistE);
		
		{
			Element edgeScanPropertiesE = doc.createElement(PROPERTIES);
			scanDistE.appendChild(edgeScanPropertiesE);
			
			{
				Element scanPathE = doc.createElement(SCAN_PATH);
				scanPathE.appendChild(doc.createTextNode(scanPath));
				edgeScanPropertiesE.appendChild(scanPathE);
			}
			
			{
				Element focalLengthE = doc.createElement(FOCAL_LENGTH);
				focalLengthE.appendChild(doc.createTextNode(Float.toString(focalLength)));
				edgeScanPropertiesE.appendChild(focalLengthE);
			}
		}
		
		{
			Element dataE = doc.createElement(DATA);
			scanDistE.appendChild(dataE);
			
			for (DistanceFileDatum datum : data) {
				Element imageE = doc.createElement(IMAGE);
				imageE.setAttribute(IMAGE_NUMBER, String.format("%d", datum.getImageNumber()));
				Element distArrayE = doc.createElement(DISTANCE_ARRAY);
				imageE.appendChild(distArrayE);
				for (Float dist : datum.getDistanceInMeters()) {
					Element distNode = doc.createElement(DISTANCE_METERS);
					distNode.setTextContent(Float.toString(dist));
					distArrayE.appendChild(distNode);
				}
				Element pathNode = doc.createElement(PATH);
				pathNode.setTextContent(datum.getPath());
				imageE.appendChild(pathNode);
				dataE.appendChild(imageE);
			}
		}
		
		return new DOMSource(doc);
	}
	
	public String getScanPath() {
		return scanPath;
	}
	
	public void setScanPath(String in) {
		scanPath = in;
	}
	
	public float get35MMFocalLength() {
		return focalLength;
	}
	
	public void set35MMFocalLength(Float in) {
		focalLength = in;
	}
	
	public List<DistanceFileDatum> getData() {
		return data;
	}
	
	public DistanceFileDatum getDataByImageNumber(int imageNumber) {
		for (DistanceFileDatum datum : data) {
			if (datum.getImageNumber() == imageNumber)
				return datum;
		}
		return null;
	}
}

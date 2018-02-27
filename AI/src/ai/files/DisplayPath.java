package ai.files;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.providers.Microsoft;
import de.fhpotsdam.unfolding.utils.MapUtils;
import processing.core.PApplet;

@SuppressWarnings("serial")
public class DisplayPath extends PApplet {
	UnfoldingMap map;
	static List<SimplePointMarker> houses = new ArrayList<SimplePointMarker>();
	static List<SimpleLinesMarker> lines = new ArrayList<SimpleLinesMarker>();
	
	public static void readHouses() throws IOException {
		File fileName = new File("/Users/Justin/AQP-AI/AI/precomputeNeighbors/", "precomputeNeighbors.txt");

		//System.out.println(new File(".").getAbsoluteFile());
		// first read in all houses
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String currentLine = br.readLine();
		while (!currentLine.equals("..........")) {
			String lat = br.readLine();
			String lon = br.readLine();
			String block = br.readLine();
			String category = br.readLine();
			if (block.equals("NA")) {
				// do nothing
			} else {
				SimplePointMarker toAddMarker = new SimplePointMarker(new Location(Float.parseFloat(lat), Float.parseFloat(lon)));
				if (Integer.parseInt(category) == 0) {
					Color c = new Color(255, 255, 178);
					toAddMarker.setColor(c.getRGB());
				} else if (Integer.parseInt(category) == 1) {
					Color c = new Color(254, 204, 92);
					toAddMarker.setColor(c.getRGB());
				} else if (Integer.parseInt(category) == 2) {
					Color c = new Color(253, 141, 60);
					toAddMarker.setColor(c.getRGB());
				} else if (Integer.parseInt(category) == 3) {
					Color c = new Color(240, 59, 32);
					toAddMarker.setColor(c.getRGB());
				} else if (Integer.parseInt(category) == 4) {
					Color c = new Color(189, 0, 38);
					toAddMarker.setColor(c.getRGB());
				}
				houses.add(toAddMarker);
			}
			currentLine = br.readLine();
		}
		br.close();
	}
	
	public static void readLines() throws IOException {
		File fileName = new File("/Users/Justin/AQP-AI/AI/display/", "housesTueFeb2712_18_29PET2018.txt");

		// first read in all houses
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		float lat1 = Float.parseFloat(br.readLine());
		float lon1 = Float.parseFloat(br.readLine());
		for (int i=0; i < 5;i++) {
			float lat2 = Float.parseFloat(br.readLine());
			float lon2 = Float.parseFloat(br.readLine());
			SimpleLinesMarker line = new SimpleLinesMarker(new Location(lat1, lon1), new Location(lat2, lon2));
			line.setColor(0);
			line.setStrokeWeight(3);
			
			lines.add(line);
			
			lat1 = lat2;
			lon1 = lon2;
		}
		br.close();
	}
	
	public void setup() {
	map = new UnfoldingMap(this, new Microsoft.AerialProvider());


	try {
		readHouses();
	} catch (IOException e) {
		e.printStackTrace();
	}
	try {
		readLines();
	} catch (IOException e) {
		e.printStackTrace();
	}
	
	for (SimplePointMarker spm : houses) {
		map.addMarker(spm);
	}
	for (SimpleLinesMarker slm : lines) {
		map.addMarker(slm);
	}

	size(1200, 750, P2D);

	map.zoomAndPanTo(17, houses.get(0).getLocation());
	MapUtils.createDefaultEventDispatcher(this, map);
	}
	
	public void draw() {
		map.draw();
	}
	
	public static void main(String[] args) {
		
		
		PApplet.main(new String[] { DisplayPath.class.getName() });
	}
}

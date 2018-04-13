package ai.files;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import ai.files.Triangulation.InvalidVertexException;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.providers.Microsoft;
import de.fhpotsdam.unfolding.utils.MapUtils;
import processing.core.PApplet;

@SuppressWarnings("serial")
public class DisplayPath extends PApplet {
	static UnfoldingMap map;
	static List<SimplePointMarker> houses = new ArrayList<SimplePointMarker>();
	static List<SimpleLinesMarker> lines = new ArrayList<SimpleLinesMarker>();

	public static void readHouses() throws IOException {
		File fileName = new File("/Users/Justin/AQP-AI/AI/precomputeNeighbors/", "precomputeNeighbors.txt");

		// System.out.println(new File(".").getAbsoluteFile());
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
				SimplePointMarker toAddMarker = new SimplePointMarker(
						new Location(Float.parseFloat(lat), Float.parseFloat(lon)));
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

	public static void readTriangulation() {
		// prepare triPointList
		List<Vertex> triPointList = new ArrayList<Vertex>();

		for (SimpleLinesMarker line : lines) {
			Location l1 = line.getLocation(0);
			triPointList.add(new Vertex(l1.getLon(), l1.getLat()));
			Location l2 = line.getLocation(1);
			triPointList.add(new Vertex(l2.getLon(), l2.getLat()));
		}

		// get max lat and long for Delaunay triangulation
		Iterator<SimplePointMarker> iter = houses.iterator();
		SimplePointMarker firstp = iter.next();
		Float maxLat = firstp.getLocation().getLat();
		Float minLat = firstp.getLocation().getLat();
		Float maxLon = firstp.getLocation().getLon();
		Float minLon = firstp.getLocation().getLon();
		while (iter.hasNext()) {
			SimplePointMarker toCompare = iter.next();
			Float toCompareLat = toCompare.getLocation().getLat();
			if (toCompareLat < minLat) {
				minLat = toCompareLat;
			} else if (toCompareLat > maxLat) {
				maxLat = toCompareLat;
			}
			Float toCompareLon = toCompare.getLocation().getLon();
			if (toCompareLon < minLon) {
				minLon = toCompareLon;
			} else if (toCompareLon > maxLon) {
				maxLon = toCompareLon;
			}
		}
		// left bottom corner
		triPointList.add(new Vertex(minLon.doubleValue() - 0.0001, minLat.doubleValue() - 0.0001));

		// left top corner
		triPointList.add(new Vertex(minLon.doubleValue() - 0.0001, maxLat.doubleValue() + 0.0001));

		// right bottom corner
		triPointList.add(new Vertex(maxLon.doubleValue() + 0.0001, minLat.doubleValue() - 0.0001));

		// right top corner
		triPointList.add(new Vertex(maxLon.doubleValue() + 0.0001, maxLat.doubleValue() + 0.0001));

		// display triangulation
		Triangulation delaunayMesh = new Triangulation();
		delaunayMesh.addAllVertices(triPointList);
		try {
			delaunayMesh.triangulate();
		} catch (InvalidVertexException e) {
			e.printStackTrace();
		}

		LinkedHashSet<Triangle> triList = delaunayMesh.getTriangles();

		// for display of triangulation at the end of the simulation
		for (Triangle triToDisplay : triList) {
			Location first = new Location(triToDisplay.a.y, triToDisplay.a.x);
			Location second = new Location(triToDisplay.b.y, triToDisplay.b.x);
			Location third = new Location(triToDisplay.c.y, triToDisplay.c.x);

			SimpleLinesMarker edgeOne = new SimpleLinesMarker(first, second);
			SimpleLinesMarker edgeTwo = new SimpleLinesMarker(second, third);
			SimpleLinesMarker edgeThree = new SimpleLinesMarker(third, first);

			edgeOne.setColor(-16776961);
			edgeOne.setStrokeWeight(2);
			edgeTwo.setColor(-16776961);
			edgeTwo.setStrokeWeight(2);
			edgeThree.setColor(-16776961);
			edgeThree.setStrokeWeight(2);

			map.addMarker(edgeOne);
			map.addMarker(edgeTwo);
			map.addMarker(edgeThree);
		}

		// get triangle information
		int[] cntHouses = new int[triList.size()];

		int cnt = 0;
		int total = 0;
		for (Triangle t : triList) {
			for (Marker cntHouse : houses) {
				// make boundaryTriangle
				Point[] vertices = new Point[3];

				vertices[0] = new Point(t.a.x, t.a.y);
				vertices[1] = new Point(t.b.x, t.b.y);
				vertices[2] = new Point(t.c.x, t.c.y);
				BoundaryTriangle boundTri = new BoundaryTriangle(vertices);

				// check if point is within boundaryTriangle
				if (boundTri.contains(new Point((double) cntHouse.getLocation().getLon(),
						(double) cntHouse.getLocation().getLat()))) {
					cntHouses[cnt] = cntHouses[cnt] + 1;
					total++;
				}
			}
			cnt++;
		}
		Arrays.sort(cntHouses);
		List<Integer> listCntHouses = new ArrayList<>();
		for (int a : cntHouses) {
			listCntHouses.add(0, a);
		}
		@SuppressWarnings("unused")
		int maxValue = listCntHouses.get(0);
		@SuppressWarnings("unused")
		Double averageTri = (double) total / (double) cntHouses.length;
	}

	public static void readLines() throws IOException {
		File fileName = new File("/Users/Justin/AQP-AI/AI/display/", "housesFriApr1316_32_02PET2018.txt");

		// first read in all houses
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		float lat1 = Float.parseFloat(br.readLine());
		float lon1 = Float.parseFloat(br.readLine());
		boolean isEnd = false;
		while (!isEnd) {
			String test = br.readLine();
			if (test == null) {
				isEnd = true;
			} else {
				float lat2 = Float.parseFloat(test);
				float lon2 = Float.parseFloat(br.readLine());
				SimpleLinesMarker line = new SimpleLinesMarker(new Location(lat1, lon1), new Location(lat2, lon2));
				line.setColor(0);
				line.setStrokeWeight(3);

				lines.add(line);

				lat1 = lat2;
				lon1 = lon2;
			}
			
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
		readTriangulation();

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

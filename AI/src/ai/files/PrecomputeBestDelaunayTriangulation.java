package ai.files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ai.files.Triangulation.InvalidVertexException;

public class PrecomputeBestDelaunayTriangulation {

	List<House> houses = new ArrayList<House>();
	Set<Vertex> bestDelTriVerts = new HashSet<Vertex>();
	Set<House> bestDelTriHouses = new HashSet<House>();

	PrecomputeBestDelaunayTriangulation(String fileToCompute, int nHousesTri) throws IOException {
		// I: Load all houses
		File fileName = new File(fileToCompute);

		BufferedReader br = new BufferedReader(new FileReader(fileName));

		String currentLine = br.readLine();
		currentLine = br.readLine();
		while (currentLine != null) {
			int uniEnd = currentLine.indexOf(" ");
			int latEnd = currentLine.indexOf(" ", uniEnd + 1);
			int lonEnd = currentLine.indexOf(" ", latEnd + 1);
			int quantEnd = currentLine.indexOf(" ", lonEnd + 1);
			int blockEnd = currentLine.length();

			String uni = currentLine.substring(0, uniEnd);
			String lat = currentLine.substring(uniEnd + 1, latEnd);
			String lon = currentLine.substring(latEnd + 1, lonEnd);
			String quant = currentLine.substring(lonEnd + 1, quantEnd);
			String block = currentLine.substring(quantEnd + 1, blockEnd);

			uni = uni.trim();
			uni = uni.replaceAll("\"", "");
			lat = lat.trim();
			lat = lat.replaceAll("\"", "");
			lon = lon.trim();
			lon = lon.replaceAll("\"", "");
			quant = quant.trim();
			quant = quant.replaceAll("\"", "");
			block = block.trim();
			block = block.replaceAll("\"", "");

			if (block.equals("NA")) {
				// do nothing
			} else {
				House houseToAdd = new House(uni, Integer.parseInt(block), Float.parseFloat(lat), Float.parseFloat(lon),
						Integer.parseInt(quant));

				// add to list of houses
				houses.add(houseToAdd);
			}
			currentLine = br.readLine();
		}
		br.close();

		// II: run triangulation loop

		// get max lat and long for Delaunay triangulation
		Iterator<House> iter = houses.iterator();
		House first = iter.next();
		Float maxLat = first.getLatitude();
		Float minLat = first.getLatitude();
		Float maxLon = first.getLongitude();
		Float minLon = first.getLongitude();
		while (iter.hasNext()) {
			House toCompare = iter.next();
			Float toCompareLat = toCompare.getLatitude();
			if (toCompareLat < minLat) {
				minLat = toCompareLat;
			} else if (toCompareLat > maxLat) {
				maxLat = toCompareLat;
			}
			Float toCompareLon = toCompare.getLongitude();
			if (toCompareLon < minLon) {
				minLon = toCompareLon;
			} else if (toCompareLon > maxLon) {
				maxLon = toCompareLon;
			}
		}
		// left bottom corner
		bestDelTriVerts.add(new Vertex(minLon.doubleValue() - 0.0001, minLat.doubleValue() - 0.0001));
		// left top corner
		bestDelTriVerts.add(new Vertex(minLon.doubleValue() - 0.0001, maxLat.doubleValue() + 0.0001));
		// right bottom corner
		bestDelTriVerts.add(new Vertex(maxLon.doubleValue() + 0.0001, minLat.doubleValue() - 0.0001));
		// right top corner
		bestDelTriVerts.add(new Vertex(maxLon.doubleValue() + 0.0001, maxLat.doubleValue() + 0.0001));

		// get houses of best triangle, triangulate for each one to get best house to go to
		for (int i = 0; i < nHousesTri; i++) {
			List<House> housesBiggestTriangle = getHousesBiggestTriangle();
			House bestHouse = null;
			int lowestNHouses = houses.size();
			int cnt2 = 0;
			for (House h : housesBiggestTriangle) {
				cnt2++;
				System.out.println(cnt2);
				System.out.println(i);
				Triangulation delaunayMesh = new Triangulation();
				delaunayMesh.addAllVertices(bestDelTriVerts);
				delaunayMesh.addVertex(new Vertex(h.getLongitude(), h.getLatitude()));
				try {
					delaunayMesh.triangulate();
				} catch (InvalidVertexException e) {
					e.printStackTrace();
				}

				// get max value of this triangulation
				LinkedHashSet<Triangle> triList = delaunayMesh.getTriangles();
				// get triangle information
				int[] cntHouses = new int[triList.size()];
				int cnt = 0;
				for (Triangle t : triList) {
					for (House cntHouse : houses) {
						// make boundaryTriangle
						Point[] vertices = new Point[3];

						vertices[0] = new Point(t.a.x, t.a.y);
						vertices[1] = new Point(t.b.x, t.b.y);
						vertices[2] = new Point(t.c.x, t.c.y);
						BoundaryTriangle boundTri = new BoundaryTriangle(vertices);

						// check if point is within boundaryTriangle
						if (boundTri.contains(
								new Point((double) cntHouse.getLongitude(), (double) cntHouse.getLatitude()))) {
							cntHouses[cnt] = cntHouses[cnt] + 1;
						}
					}
					cnt++;
				}
				Arrays.sort(cntHouses);
				List<Integer> listCntHouses = new ArrayList<>();
				for (int a : cntHouses) {
					listCntHouses.add(0, a);
				}
				int maxValue = listCntHouses.get(0);

				// compare the value
				if (maxValue < lowestNHouses) {
					bestHouse = h;
					lowestNHouses = maxValue;
				}
			}

			// now, we have best house to go to, so we add to bestDelTriVerts and bestDelTriHouses
			bestDelTriVerts.add(new Vertex(bestHouse.getLongitude(), bestHouse.getLatitude()));
			bestDelTriHouses.add(bestHouse);
		}
	}
	
	public List<String> getBestHouses() {
		List<String> toReturn = new ArrayList<String>();
		for (House bestHouseEntry : bestDelTriHouses) {
			toReturn.add(bestHouseEntry.getUnicode());
		}
		
		return toReturn;
	}

	public List<House> getHousesBiggestTriangle() {
		List<House> toReturn = new ArrayList<House>();

		// get triangulation
		Triangulation delaunayMesh = new Triangulation();
		delaunayMesh.addAllVertices(bestDelTriVerts);
		try {
			delaunayMesh.triangulate();
		} catch (InvalidVertexException e) {
			e.printStackTrace();
		}

		// make new map of triangle to the list of houses in this triangle
		LinkedHashSet<Triangle> triList = delaunayMesh.getTriangles();
		Map<Triangle, ArrayList<House>> triMap = new HashMap<Triangle, ArrayList<House>>();
		for (Triangle triListEntry : triList) {
			triMap.put(triListEntry, new ArrayList<House>());
		}

		// get the houses for each triangle
		for (Triangle t : triMap.keySet()) {
			for (House cntHouse : houses) {
				// make boundaryTriangle
				Point[] vertices = new Point[3];

				vertices[0] = new Point(t.a.x, t.a.y);
				vertices[1] = new Point(t.b.x, t.b.y);
				vertices[2] = new Point(t.c.x, t.c.y);
				BoundaryTriangle boundTri = new BoundaryTriangle(vertices);

				// check if point is within boundaryTriangle
				if (boundTri.contains(new Point((double) cntHouse.getLongitude(), (double) cntHouse.getLatitude()))) {
					triMap.get(t).add(cntHouse);
				}
			}
		}

		// return the triangle with the most houses
		for (Triangle completeTriMapEntry : triMap.keySet()) {
			if (toReturn.isEmpty()) {
				toReturn = triMap.get(completeTriMapEntry);
			} else {
				if (triMap.get(completeTriMapEntry).size() > toReturn.size()) {
					toReturn = triMap.get(completeTriMapEntry);
				}
			}
		}

		return toReturn;
	}
	
	public static void main(String args[]) {
		PrecomputeBestDelaunayTriangulation delTri = null;
		try {
			delTri = new PrecomputeBestDelaunayTriangulation("forSimulator.csv", 5);
		} catch (IOException e) {
			e.printStackTrace();
		}
		List<String> bestUnis = delTri.getBestHouses();
		for (String s : bestUnis) {
			System.out.println(s);
		}
		
	}
}

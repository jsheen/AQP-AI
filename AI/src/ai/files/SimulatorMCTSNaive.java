package ai.files;

import processing.core.PApplet;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import ai.files.MCTSTree.Node;
import ai.files.Triangulation.InvalidVertexException;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.providers.Microsoft;

/**
 * Chiri Run AI Simulator February 2018
 * 
 * Simulator to test AI methods
 * 
 */

@SuppressWarnings("serial")
public class SimulatorMCTSNaive extends PApplet {

	static boolean displayGUI = true; // whether or not the GUI should be
										// displayed

	static int pause = 0; // how long algorithm should pause between decisions

	static int nSims = 100;
	static double[][] sims = new double[100][14]; // int array to store
													// simulation results

	static UnfoldingMap map;
	static Location arequipaLocation = null; // central location so that the map
												// could reorganize itself
	static List<SimplePointMarker> houseMarkers = new ArrayList<SimplePointMarker>(); // list
																						// of
																						// all
																						// houses
																						// in
																						// the
																						// search
																						// zone
	static List<SimpleLinesMarker> slm = new ArrayList<SimpleLinesMarker>(); // new
																				// marker
																				// from
																				// the
																				// UnfoldingMap
																				// API
																				// to
																				// mark
																				// line
	static double distanceLeftToTravelSave = 4;
	static double distanceLeftToTravel = 4; // finite time horizon (distance
											// allowed to travel) (kilometers)
	static double totalDistance = 0; // total distance traveled for end game
										// output
	static LabeledMarker prevMark = null; // previous marker that was selected
	static List<Vertex> triPointList = new ArrayList<Vertex>();
	static String outputFileName = "error";

	// MCTS global variables
	static MCTSTree tree = null;
	static int Cp = 1;
	static int nNeighExpand = 1;

	// creates house markers
	public static void readHouseGPS() throws IOException {
		File fileName = new File("forSimulator.csv");
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
				Location houseLocation = new Location(Float.parseFloat(lat), Float.parseFloat(lon));
				LabeledMarker marker = new LabeledMarker(houseLocation, uni, Integer.parseInt(block));

				// add the category (quantile)
				marker.category = Integer.parseInt(quant);

				// add infestation chance of the marker
				Random random = new Random();
				random.setSeed(42);
				if (marker.category == 4) {
					int ans = random.nextInt(100);
					if (ans == 0) {
						marker.infested = true;
					}
				} else if (marker.category == 3) {
					int ans = random.nextInt(125);
					if (ans == 0) {
						marker.infested = true;
					}
				} else if (marker.category == 2) {
					int ans = random.nextInt(150);
					if (ans == 0) {
						marker.infested = true;
					}
				} else if (marker.category == 1) {
					int ans = random.nextInt(175);
					if (ans == 0) {
						marker.infested = true;
					}
				} else if (marker.category == 0) {
					int ans = random.nextInt(200);
					if (ans == 0) {
						marker.infested = true;
					}
				}

				if (displayGUI) {
					// setColor, add to map
					Marker toAddMarker = marker;
					toAddMarker.setColor(211);
					map.addMarker(toAddMarker);
				}

				// add to list of houses
				houseMarkers.add(marker);
				if (arequipaLocation == null) {
					arequipaLocation = new Location(Float.parseFloat(lat), Float.parseFloat(lon));
				}
			}

			currentLine = br.readLine();
		}

		// get max lat and long for Delaunay triangulation
		Iterator<SimplePointMarker> iter = houseMarkers.iterator();
		SimplePointMarker first = iter.next();
		Float maxLat = first.getLocation().getLat();
		Float minLat = first.getLocation().getLat();
		Float maxLon = first.getLocation().getLon();
		Float minLon = first.getLocation().getLon();
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

		br.close();
	}

	// get closest neighbors helper function
	public static ArrayList<LabeledMarker> getNeighbors(Node n, int nNeigh) {
		// find the 'nNeigh' closest markers for each marker
		HashSet<LabeledMarker> s = new HashSet<LabeledMarker>();
		for (SimplePointMarker neighbor : houseMarkers) {
			if (n.getHouse() == neighbor || n.isAncestorHouse((LabeledMarker) neighbor)) {
				// do nothing, it is the same marker
				// OR it may have already been searched so do not add
			} else {
				if (s.size() < nNeigh) {
					s.add((LabeledMarker) neighbor);
				} else {
					// 1. get max from the set
					Iterator<LabeledMarker> iter = s.iterator();
					Marker max = iter.next();
					while (iter.hasNext()) {
						Marker compareMax = iter.next();
						double distanceOld = n.getHouse().getDistanceTo(max.getLocation());
						double distanceNew = n.getHouse().getDistanceTo(compareMax.getLocation());

						// compare, replace if necessary
						if (distanceOld > distanceNew) {
							max = compareMax;
						}
					}

					// 2. replace if necessary
					double distanceMax = n.getHouse().getDistanceTo(max.getLocation());
					double distanceNewMax = n.getHouse().getDistanceTo(neighbor.getLocation());

					if (distanceMax > distanceNewMax) {
						s.remove(max);
						s.add((LabeledMarker) neighbor);
					}
				}
			}
		}

		// another sort for these 'nNeigh' neighbors again
		Iterator<LabeledMarker> iter = s.iterator();
		ArrayList<LabeledMarker> srtdN = new ArrayList<LabeledMarker>();

		while (iter.hasNext()) {
			LabeledMarker toAdd = iter.next();
			for (int j = 0; j < srtdN.size(); j++) {
				if (toAdd.compareDist(n.getHouse(), srtdN.get(j)) == -1) {
					srtdN.add(j, toAdd);
					break;
				}
			}
			// if needed to add at the end
			if (!srtdN.contains(toAdd)) {
				srtdN.add(toAdd);
			}
		}

		return srtdN;
	}

	// helper function to check if it has any nulls
	public boolean hasNull(Iterable<?> l) {
		for (Object element : l)
			if (element == null)
				return true;
		return false;
	}

	// helper function to check how many nulls
	public int nNulls(Iterable<?> l) {
		int cnt = 0;
		for (Object element : l)
			if (element == null)
				cnt++;
		return cnt;
	}

	public void setup() {
		map = new UnfoldingMap(this, new Microsoft.AerialProvider());
		if (displayGUI) {
			size(1200, 750, P2D);
			// load all houses
			try {
				readHouseGPS();
			} catch (IOException e) {
				e.printStackTrace();
			}
			map.zoomAndPanTo(17, arequipaLocation);
			MapUtils.createDefaultEventDispatcher(this, map);
		} else {
			// load all houses
			try {
				readHouseGPS();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void draw() {
		if (displayGUI) {
			// map has a function to draw itself on the papplet
			for (SimpleLinesMarker mm : slm) {
				map.addMarker(mm);
			}
			map.draw();

			// draw information box
			for (Marker marker : map.getMarkers()) {
				if (marker.isSelected()) {
					fill(255, 255, 255);
					rect(mouseX - 47, mouseY - 100, 95, 85);
					fill(0, 0, 0);
					text("Searched: " + ((LabeledMarker) marker).searched, mouseX - 43, mouseY - 80);
					text(((LabeledMarker) marker).unicode, mouseX - 43, mouseY - 60);
					text("Risk Level: " + (((LabeledMarker) marker).category + 1), mouseX - 43, mouseY - 40);
				}
			}

			// display time
			fill(255, 255, 255);
			rect(5, 5, 320, 20);
			fill(0, 0, 0);
			text("Distance left to travel (km): " + distanceLeftToTravel, 10, 20);

			// display prevMark information
			fill(255, 255, 255);
			rect(5, 30, 153, 20);
			if (prevMark != null) {
				if (prevMark.infested) {
					fill(255, 0, 0);
					text("FOUND INFESTED HOUSE!", 10, 45);
				} else {
					fill(0, 0, 0);
					text("NOT INFESTED...", 10, 45);
				}
			}
		}

		// end game
		if (distanceLeftToTravel < 0) {
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
			if (displayGUI) {
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
			}

			// get triangle information
			int[] cntHouses = new int[triList.size()];

			int cnt = 0;
			int total = 0;
			for (Triangle t : triList) {
				for (Marker cntHouse : houseMarkers) {
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
			int maxValue = listCntHouses.get(0);

			Double averageTri = (double) total / (double) cntHouses.length;

			// tally up score
			int cntInfestSearch = 0;
			int cntSearch = 0;
			int cntMostHighRiskNotSearched = 0;
			int cntMostHighRiskSearched = 0;
			int cntHighRiskSearched = 0;
			int cntMedRiskSearched = 0;
			int cntLowRiskSearched = 0;
			int cntMostLowRiskSearched = 0;

			for (Marker m : houseMarkers) {
				if (((LabeledMarker) m).searched && ((LabeledMarker) m).infested) {
					cntInfestSearch++;
				}
				if (((LabeledMarker) m).searched) {
					cntSearch++;
				}
				if (((LabeledMarker) m).category == 4 && !((LabeledMarker) m).searched) {
					cntMostHighRiskNotSearched++;
				}
				if (((LabeledMarker) m).category == 4 && ((LabeledMarker) m).searched) {
					cntMostHighRiskSearched++;
				}
				if (((LabeledMarker) m).category == 3 && ((LabeledMarker) m).searched) {
					cntHighRiskSearched++;
				}
				if (((LabeledMarker) m).category == 2 && ((LabeledMarker) m).searched) {
					cntMedRiskSearched++;
				}
				if (((LabeledMarker) m).category == 1 && ((LabeledMarker) m).searched) {
					cntLowRiskSearched++;
				}
				if (((LabeledMarker) m).category == 0 && ((LabeledMarker) m).searched) {
					cntMostLowRiskSearched++;
				}
			}

			if (displayGUI) {
				DecimalFormat dfUse = new DecimalFormat("#.##");
				Object[] options = { "OK" };
				JOptionPane.showOptionDialog(null,
						"Number of houses: " + houseMarkers.size() + "\n" + "Number of houses visited: " + cntSearch
								+ "\n\n" + "Number of INFESTED houses searched: " + cntInfestSearch + "\n"
								+ "Number of most high risk houses not searched: " + cntMostHighRiskNotSearched + "\n\n"
								+ "Number of most high risk houses searched: " + cntMostHighRiskSearched + "\n"
								+ "Number of high risk houses searched: " + cntHighRiskSearched + "\n"
								+ "Number of medium risk houses searched: " + cntMedRiskSearched + "\n"
								+ "Number of low risk houses searched: " + cntLowRiskSearched + "\n"
								+ "Number of most low risk houses searched: " + cntMostLowRiskSearched + "\n\n"
								+ "Total distance traveled (kilometers): " + dfUse.format(totalDistance) + "\n\n"
								+ "Max number of houses in a triangle: " + maxValue + "\n\n"
								+ "Average number of houses per triangle: " + averageTri,
						"END OF SIMULATION", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
						options[0]);
			}

			if (nSims > 0) {
				// save results in table
				sims[nSims - 1][0] = houseMarkers.size();
				sims[nSims - 1][1] = cntSearch;
				sims[nSims - 1][2] = cntInfestSearch;
				sims[nSims - 1][3] = cntMostHighRiskNotSearched;
				sims[nSims - 1][4] = cntMostHighRiskSearched;
				sims[nSims - 1][5] = cntHighRiskSearched;
				sims[nSims - 1][6] = cntMedRiskSearched;
				sims[nSims - 1][7] = cntLowRiskSearched;
				sims[nSims - 1][8] = cntMostLowRiskSearched;
				sims[nSims - 1][9] = totalDistance;
				sims[nSims - 1][10] = maxValue;
				sims[nSims - 1][11] = averageTri;
				sims[nSims - 1][12] = distanceLeftToTravelSave;
				sims[nSims - 1][13] = sims.length;

				// set number of simulations subtracting one
				nSims = nSims - 1;

				// reset everything for next simulation
				if (nSims > 0) {
					houseMarkers = new ArrayList<SimplePointMarker>();
					arequipaLocation = null;
					setup();
					slm = new ArrayList<SimpleLinesMarker>();
					distanceLeftToTravel = distanceLeftToTravelSave;
					totalDistance = 0;
					prevMark = null;
					triPointList = new ArrayList<Vertex>();

					// invoke next simulation
					makeMCTS();
					traverseTree();
				} else {
					// write csv
					writeTextFile(outputFileName);

					distanceLeftToTravel = 0;
				}
			}
		}
	}

	public static void writeTextFile(String outputFileNameToAdd) {

		// get average
		double toAdd0 = 0;
		double toAdd1 = 0;
		double toAdd2 = 0;
		double toAdd3 = 0;
		double toAdd4 = 0;
		double toAdd5 = 0;
		double toAdd6 = 0;
		double toAdd7 = 0;
		double toAdd8 = 0;
		double toAdd9 = 0;
		double toAdd10 = 0;
		double toAdd11 = 0;
		double toAdd12 = 0;
		double toAdd13 = 0;
		for (int i = 0; i < sims.length; i++) {
			toAdd0 = toAdd0 + sims[i][0];
			toAdd1 = toAdd1 + sims[i][1];
			toAdd2 = toAdd2 + sims[i][2];
			toAdd3 = toAdd3 + sims[i][3];
			toAdd4 = toAdd4 + sims[i][4];
			toAdd5 = toAdd5 + sims[i][5];
			toAdd6 = toAdd6 + sims[i][6];
			toAdd7 = toAdd7 + sims[i][7];
			toAdd8 = toAdd8 + sims[i][8];
			toAdd9 = toAdd9 + sims[i][9];
			toAdd10 = toAdd10 + sims[i][10];
			toAdd11 = toAdd11 + sims[i][11];
			toAdd12 = toAdd12 + sims[i][12];
			toAdd13 = toAdd13 + sims[i][13];
		}
		Double average0 = toAdd0 / sims.length;
		Double average1 = toAdd1 / sims.length;
		Double average2 = toAdd2 / sims.length;
		Double average3 = toAdd3 / sims.length;
		Double average4 = toAdd4 / sims.length;
		Double average5 = toAdd5 / sims.length;
		Double average6 = toAdd6 / sims.length;
		Double average7 = toAdd7 / sims.length;
		Double average8 = toAdd8 / sims.length;
		Double average9 = toAdd9 / sims.length;
		Double average10 = toAdd10 / sims.length;
		Double average11 = toAdd11 / sims.length;
		Double average12 = toAdd12 / sims.length;
		Double average13 = toAdd13 / sims.length;

		String fileName = new String("MCTSNaive" + new Date());
		fileName = fileName.replaceAll("\\s+", "");
		fileName = fileName.replaceAll(":", "_");
		System.out.println(fileName);
		File fileToWrite = new File("output/", fileName + ".txt");

		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(fileToWrite));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {

			// write all results
			writer.write("Method Used: MCTSNaive");
			writer.write("\n");
			writer.write("Number of houses: " + String.valueOf(average0));
			writer.write("\n");
			writer.write("Number of inspected houses: " + String.valueOf(average1));
			writer.write("\n");
			writer.write("Number of inspected houses that were infested: " + String.valueOf(average2));
			writer.write("\n");
			writer.write("Number of most high risk houses NOT inspected: " + String.valueOf(average3));
			writer.write("\n");
			writer.write("Number of most high risk houses inspected: " + String.valueOf(average4));
			writer.write("\n");
			writer.write("Number of high risk houses inspected: " + String.valueOf(average5));
			writer.write("\n");
			writer.write("Number of medium risk houses inspected: " + String.valueOf(average6));
			writer.write("\n");
			writer.write("Number of low risk houses inspected: " + String.valueOf(average7));
			writer.write("\n");
			writer.write("Number of most low risk houses inspected: " + String.valueOf(average8));
			writer.write("\n");
			writer.write("Total distance traveled:  " + String.valueOf(average9));
			writer.write("\n");
			writer.write("Max number of houses in a triangle: " + String.valueOf(average10));
			writer.write("\n");
			writer.write("Average number of houses in a triangle: " + String.valueOf(average11));
			writer.write("\n");
			writer.write("Distance given to travel: " + String.valueOf(average12));
			writer.write("\n");
			writer.write("Number of simulations: " + String.valueOf(average13));

			writer.flush();
			writer.close();
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void writeCsvFile(String fileName) {
		// Delimiter used in CSV file
		final String NEW_LINE_SEPARATOR = ";";

		try {
			FileWriter fileWriter = new FileWriter(fileName, true);

			// get average
			double toAdd0 = 0;
			double toAdd1 = 0;
			double toAdd2 = 0;
			double toAdd3 = 0;
			double toAdd4 = 0;
			double toAdd5 = 0;
			double toAdd6 = 0;
			double toAdd7 = 0;
			double toAdd8 = 0;
			double toAdd9 = 0;
			double toAdd10 = 0;
			double toAdd11 = 0;
			double toAdd12 = 0;
			double toAdd13 = 0;
			for (int i = 0; i < sims.length; i++) {
				toAdd0 = toAdd0 + sims[i][0];
				toAdd1 = toAdd1 + sims[i][1];
				toAdd2 = toAdd2 + sims[i][2];
				toAdd3 = toAdd3 + sims[i][3];
				toAdd4 = toAdd4 + sims[i][4];
				toAdd5 = toAdd5 + sims[i][5];
				toAdd6 = toAdd6 + sims[i][6];
				toAdd7 = toAdd7 + sims[i][7];
				toAdd8 = toAdd8 + sims[i][8];
				toAdd9 = toAdd9 + sims[i][9];
				toAdd10 = toAdd10 + sims[i][10];
				toAdd11 = toAdd11 + sims[i][11];
				toAdd12 = toAdd12 + sims[i][12];
				toAdd13 = toAdd13 + sims[i][13];
			}
			Double average0 = toAdd0 / sims.length;
			Double average1 = toAdd1 / sims.length;
			Double average2 = toAdd2 / sims.length;
			Double average3 = toAdd3 / sims.length;
			Double average4 = toAdd4 / sims.length;
			Double average5 = toAdd5 / sims.length;
			Double average6 = toAdd6 / sims.length;
			Double average7 = toAdd7 / sims.length;
			Double average8 = toAdd8 / sims.length;
			Double average9 = toAdd9 / sims.length;
			Double average10 = toAdd10 / sims.length;
			Double average11 = toAdd11 / sims.length;
			Double average12 = toAdd12 / sims.length;
			Double average13 = toAdd13 / sims.length;

			// get standard deviations
			double[] simsSD = new double[sims.length];
			for (int i = 0; i < sims.length; i++) {
				simsSD[i] = sims[i][0] - toAdd0;
			}
			double SD = 0;
			for (int j = 0; j < sims.length; j++) {
				SD = SD + simsSD[j];
			}

			// write all results
			fileWriter.append("\n");
			fileWriter.append("randomMethod");
			fileWriter.append(NEW_LINE_SEPARATOR);
			fileWriter.append(String.valueOf(average0));
			fileWriter.append(NEW_LINE_SEPARATOR);
			fileWriter.append(String.valueOf(average1));
			fileWriter.append(NEW_LINE_SEPARATOR);
			fileWriter.append(String.valueOf(average2));
			fileWriter.append(NEW_LINE_SEPARATOR);
			fileWriter.append(String.valueOf(average3));
			fileWriter.append(NEW_LINE_SEPARATOR);
			fileWriter.append(String.valueOf(average4));
			fileWriter.append(NEW_LINE_SEPARATOR);
			fileWriter.append(String.valueOf(average5));
			fileWriter.append(NEW_LINE_SEPARATOR);
			fileWriter.append(String.valueOf(average6));
			fileWriter.append(NEW_LINE_SEPARATOR);
			fileWriter.append(String.valueOf(average7));
			fileWriter.append(NEW_LINE_SEPARATOR);
			fileWriter.append(String.valueOf(average8));
			fileWriter.append(NEW_LINE_SEPARATOR);
			fileWriter.append(String.valueOf(average9));
			fileWriter.append(NEW_LINE_SEPARATOR);
			fileWriter.append(String.valueOf(average10));
			fileWriter.append(NEW_LINE_SEPARATOR);
			fileWriter.append(String.valueOf(average11));
			fileWriter.append(NEW_LINE_SEPARATOR);
			fileWriter.append(String.valueOf(average12));
			fileWriter.append(NEW_LINE_SEPARATOR);
			fileWriter.append(String.valueOf(average13));

			fileWriter.flush();
			fileWriter.close();
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static Node getBestChild(Node parent) {
		List<Node> children = parent.getChildren();
		int maxChildDex = 0;
		float maxChildVal = 0;
		for (int i = 0; i < children.size(); i++) {
			// get child value
			Node child = children.get(i);
			float val = 0;
			if (child.getCount() == 0) {
				val = Float.POSITIVE_INFINITY;
			} else {
				val = (float) (child.getQVal() + 2 * Cp * Math.sqrt((2 * Math.log(tree.curr.getCount())) / child.getCount()));
			}
			// flip coin if child with equal value, replace if there is a child
			// with a greater value
			if (val == maxChildVal) {
				Random coinFlip = new Random();
				coinFlip.setSeed(42);
				int ans = coinFlip.nextInt(2);
				if (ans == 0) {
					maxChildDex = i;
					maxChildVal = val;
				}
			} else if (val > maxChildVal) {
				maxChildDex = i;
				maxChildVal = val;
			}
		}
		Node toReturnNode = children.get(maxChildDex);
		return toReturnNode;
	}

	private static boolean isTerminalState(Node toCheck, int nNeighbors) {
		boolean ans = true;
		ArrayList<LabeledMarker> neighbors = getNeighbors(toCheck, nNeighbors);
		Iterator<LabeledMarker> neighborIter = neighbors.iterator();
		// check if it is possible to have any children
		while (neighborIter.hasNext()) {
			LabeledMarker labeledMarkerToAdd = neighborIter.next();
			float distToNeighbor = (float) tree.curr.getHouse().getDistanceTo(labeledMarkerToAdd.getLocation());
			float distLeft = tree.curr.getDist() - distToNeighbor;
			if (distLeft > 0) { // only change ans if there is a child that is
								// able to be added
				ans = false;
			}
		}
		return ans;
	}

	private static void expandNode(Node toExpand) {
		if (!isTerminalState(toExpand, nNeighExpand)) {
			// starts as leaf, some number of actions are added
			ArrayList<LabeledMarker> neighbors = getNeighbors(toExpand, nNeighExpand);
			Iterator<LabeledMarker> neighborIter = neighbors.iterator();
			// add all children
			while (neighborIter.hasNext()) {
				LabeledMarker labeledMarkerToAdd = neighborIter.next();
				float distToNeighbor = (float) toExpand.getHouse().getDistanceTo(labeledMarkerToAdd.getLocation());
				float distLeft = toExpand.getDist() - distToNeighbor;
				if (distLeft > 0) { // only add the child if it does not go over
									// the rest of the distance
					toExpand.addChild(new Node(toExpand, labeledMarkerToAdd, distLeft));
				}
			}
		}
	}

	public static void makeMCTS() {
		int numIterations = 500;
		while (numIterations > 0) {
			// Part I: Selection
			// find the child that maximizes the algorithm, and eventually the leaf
			while (!tree.curr.isLeaf()) {
				Node bestChild = getBestChild(tree.curr);
				tree.curr = bestChild;
			}

			// Part II: Expansion
			if (tree.curr.getCount() != 0) {
				// add children to the current node
				expandNode(tree.curr);
				// find most promising child as well
				tree.curr = getBestChild(tree.curr);
			}

			// Part III: Rollout (do this from whichever node is set as curr in the tree)
			double ROVal = 0;
			if (!isTerminalState(tree.curr, nNeighExpand)) {
				MCTSTree toSimulate = new MCTSTree(
						new Node(tree.curr.getParent(), tree.curr.getHouse(), tree.curr.getDist()));
				int simIter = 50;
				double sumSimVals = 0;
				while (simIter > 0) {
					// get simulation value
					double simVal = getSimulationValue(toSimulate.curr);
					sumSimVals = sumSimVals + simVal;
					// update simulation iteration number
					simIter = simIter - 1;
				}
				ROVal = sumSimVals / simIter;
			}

			// Part IV: Update
			if (!isTerminalState(tree.curr, nNeighExpand)) {
				while (tree.curr.getParent() != null) {
					tree.curr.addCount();
					tree.curr.setSumVals((float) (tree.curr.getSumVals() + ROVal));
					
					float qValToAdd = (1 / tree.curr.getCount()) * tree.curr.getSumVals();
					tree.curr.setQVal(qValToAdd);
				}
			}

			// reset the curr
			tree.curr = tree.root;

			// update iterations
			numIterations = numIterations - 1;
		}
	}

	public static double getSimulationValue(Node toSimulate) {
		Node curr = new Node(toSimulate.getParent(), toSimulate.getHouse(), toSimulate.getDist());
		double sum = 0;
		int cnt = 1;
		while (!isTerminalState(curr, nNeighExpand)) {
			// update sum value
			if (curr.getHouse().category == 1) {
				sum = sum + 0.2;
			} else if (curr.getHouse().category == 2) {
				sum = sum + 0.4;
			} else if (curr.getHouse().category == 3) {
				sum = sum + 0.6;
			} else if (curr.getHouse().category == 4) {
				sum = sum + 0.8;
			}
			// expand this current node
			expandNode(curr);
			// go to random child
			Random random = new Random();
			random.setSeed(42);
			curr = curr.getChildren().get(random.nextInt(curr.getChildren().size()));
			// keep track of how deep the branch is
			cnt++;
		}
		// add last leaf node to count
		cnt++;

		return sum / cnt;
	}

	public static void traverseTree() {
		while (distanceLeftToTravel > 0) {
			// pause between each decision made
			try {
				Thread.sleep(pause);
			} catch (InterruptedException e) {
			}

			if (prevMark == null) {
				SimplePointMarker m = houseMarkers.get(2);
				prevMark = (LabeledMarker) m;
			} else {
				// the previous marker has now been 'clicked'
				prevMark.searched = true;
				prevMark.isPrevMark = true;

				// add point for triangulation, draw function will get the triangulation and draw it
				triPointList.add(
						new Vertex((double) prevMark.getLocation().getLon(), (double) prevMark.getLocation().getLat()));

				// next marker to search (MCTS algorithm)
				LabeledMarker nextU = null;
				Iterator<Node> childIter = tree.curr.getChildren().iterator();
				Node maxChild = childIter.next();
				while (childIter.hasNext()) {
					Node toCheck = childIter.next();
					if (toCheck.getQVal() > maxChild.getQVal()) {
						maxChild = toCheck;
					}
				}
				nextU = maxChild.getHouse();
				
				// update the curr of the tree
				tree.curr = maxChild;
				
				// update the distance left to travel
				distanceLeftToTravel = distanceLeftToTravel - prevMark.getDistanceTo(nextU.getLocation());

				if (distanceLeftToTravel > 0) {

					// update the total distance
					totalDistance = totalDistance + prevMark.getDistanceTo(nextU.getLocation());

					// add line to show path
					SimpleLinesMarker toAddLine = new SimpleLinesMarker(prevMark.getLocation(), nextU.getLocation());
					toAddLine.setColor(0);
					toAddLine.setStrokeWeight(3);
					slm.add(toAddLine);

					// replace for next loop iteration
					prevMark.isPrevMark = false;
					prevMark = nextU;
				}

			}
		}
	}

	// From the given labeled marker, get the closest marker that is the color given
	public static LabeledMarker getClosestColor(LabeledMarker lm, int color) {
		LabeledMarker toReturn = null;
		for (SimplePointMarker hm : houseMarkers) {
			LabeledMarker compare = (LabeledMarker) hm;
			if (toReturn == null) {
				toReturn = compare;
			} else {
				if (!compare.searched && compare.category == color && !(compare == lm)
						&& lm.getDistanceTo(compare.getLocation()) < lm.getDistanceTo(toReturn.getLocation())) {
					toReturn = compare;
				}
			}
		}
		return toReturn;
	}

	public static void main(String args[]) {
		File fileName = new File(args[0]);
		outputFileName = args[0];
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		String guiToParse = null;
		String pauseToParse = null;
		String simsToParse = null;
		String distToParse = null;
		String cpToParse = null;
		String nNeighExpandToParse = null;
		try {
			guiToParse = br.readLine();
			guiToParse = guiToParse.trim();
			guiToParse = guiToParse.replaceAll("DISPLAY_GUI = ", "");

			pauseToParse = br.readLine();
			pauseToParse = pauseToParse.trim();
			pauseToParse = pauseToParse.replaceAll("PAUSE_MILLISECONDS = ", "");

			simsToParse = br.readLine();
			simsToParse = simsToParse.trim();
			simsToParse = simsToParse.replaceAll("NUM_SIMULATIONS = ", "");

			distToParse = br.readLine();
			distToParse = distToParse.trim();
			distToParse = distToParse.replaceAll("DISTANCE_FOR_SIM_KM = ", "");

			cpToParse = br.readLine();
			cpToParse = cpToParse.trim();
			cpToParse = cpToParse.replaceAll("CP = ", "");

			nNeighExpandToParse = br.readLine();
			nNeighExpandToParse = nNeighExpandToParse.trim();
			nNeighExpandToParse = nNeighExpandToParse.replaceAll("NUM_NEIGHBORS_EXPANSION = ", "");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		displayGUI = Boolean.parseBoolean(guiToParse);
		pause = Integer.parseInt(pauseToParse);
		nSims = Integer.parseInt(simsToParse);
		sims = new double[nSims][14];
		distanceLeftToTravelSave = Integer.parseInt(distToParse);
		distanceLeftToTravel = Integer.parseInt(distToParse);
		Cp = Integer.parseInt(cpToParse);
		nNeighExpand = Integer.parseInt(nNeighExpandToParse);

		// main window (also invokes set-up)
		PApplet.main(new String[] { SimulatorMCTSNaive.class.getName() });

		// title
		if (displayGUI == true) {
			JFrame f = new JFrame();
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			f.setUndecorated(true);
			ImageIcon title = new ImageIcon("title.png", "title");
			JLabel lbl = new JLabel(title);
			f.getContentPane().add(lbl);
			f.setSize(title.getIconWidth(), title.getIconHeight());
			int x = (screenSize.width - f.getSize().width) / 2;
			int y = (screenSize.height - f.getSize().height) / 2;
			f.setLocation(x, y);
			f.setVisible(true);
			// pause for system to draw all houses before running update
			// function
			try {
				Thread.sleep(13000);
			} catch (InterruptedException e) {
			}
			f.dispose();
		}

		// get MCTS
		tree = new MCTSTree(new Node(null, (LabeledMarker) houseMarkers.get(2), (float) distanceLeftToTravelSave));
		makeMCTS();
		traverseTree();
	}
}
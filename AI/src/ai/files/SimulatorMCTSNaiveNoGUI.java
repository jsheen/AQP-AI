package ai.files;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

import ai.files.MCTSTree.Node;
import ai.files.Triangulation.InvalidVertexException;

/**
 * Chiri Run AI Simulator February 2018
 * 
 * Simulator to test AI methods
 * 
 */

public class SimulatorMCTSNaiveNoGUI {

	static int pause = 0; // how long algorithm should pause between decisions

	static int nSims = 100;
	static double[][] sims = new double[100][14]; // int array to store
													// simulation results

	static double distanceLeftToTravelSave = 4;
	static double distanceLeftToTravel = 4; // finite time horizon (distance
											// allowed to travel) (kilometers)
	static double totalDistance = 0; // total distance traveled for end game
										// output
	static List<Vertex> triPointList = new ArrayList<Vertex>();
	static String outputFileName = "error";
	static List<House> houseList = new ArrayList<House>();
	static List<HouseLine> lineList = new ArrayList<HouseLine>();
	
	// MCTS global variables
	static MCTSTree tree = null;
	static int Cp = 1;
	static int nNeighExpand = 1;
	static int numIterBuildTree = 50;
	static int numIterBuildTreeSave = 50;

	// creates houses
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
				House houseToAdd = new House(uni, Integer.parseInt(block), Float.parseFloat(lat),
						Float.parseFloat(lon), Integer.parseInt(quant));

				// add infestation chance of the house
				Random random = new Random();
				random.setSeed(42);
				if (houseToAdd.getCategory() == 4) {
					int ans = random.nextInt(100);
					if (ans == 0) {
						houseToAdd.setInfested(true);
					}
				} else if (houseToAdd.getCategory() == 3) {
					int ans = random.nextInt(125);
					if (ans == 0) {
						houseToAdd.setInfested(true);
					}
				} else if (houseToAdd.getCategory() == 2) {
					int ans = random.nextInt(150);
					if (ans == 0) {
						houseToAdd.setInfested(true);
					}
				} else if (houseToAdd.getCategory() == 1) {
					int ans = random.nextInt(175);
					if (ans == 0) {
						houseToAdd.setInfested(true);
					}
				} else if (houseToAdd.getCategory() == 0) {
					int ans = random.nextInt(200);
					if (ans == 0) {
						houseToAdd.setInfested(true);
					}
				}

				// add to list of houses
				houseList.add(houseToAdd);
			}

			currentLine = br.readLine();
		}

		// get max lat and long for Delaunay triangulation
		Iterator<House> iter = houseList.iterator();
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
	public static ArrayList<House> getNeighbors(Node n, int nNeigh) {
		// find the 'nNeigh' closest neighbors for each house
		HashSet<House> s = new HashSet<House>();
		for (House neighbor : houseList) {
			if (n.getHouse().equals(neighbor) || n.isAncestorHouse(neighbor)) {
				// do nothing, it is the same house
				// OR it may have already been searched so do not add
			} else {
				if (s.size() < nNeigh) {
					s.add(neighbor);
				} else {
					// 1. get max from the set
					Iterator<House> iter = s.iterator();
					House max = iter.next();
					while (iter.hasNext()) {
						House compareMax = iter.next();
						float distanceOld = (float) n.getHouse().getDistanceTo(max);
						float distanceNew = (float) n.getHouse().getDistanceTo(compareMax);

						// compare, replace if necessary
						if (distanceOld > distanceNew) {
							max = compareMax;
						}
					}

					// 2. replace if necessary
					float distanceMax = (float) n.getHouse().getDistanceTo(max);
					float distanceNewMax = (float) n.getHouse().getDistanceTo(neighbor);

					if (distanceMax > distanceNewMax) {
						s.remove(max);
						s.add(neighbor);
					}
				}
			}
		}

		// another sort for these 'nNeigh' neighbors again
		Iterator<House> iter = s.iterator();
		ArrayList<House> srtdN = new ArrayList<House>();

		while (iter.hasNext()) {
			House toAdd = iter.next();
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
		if (parent.getChildren().isEmpty()) {
			throw new java.lang.Error("parent has no children");
		}
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
				val = (float) (child.getQVal()
						+ 2 * Cp * Math.sqrt((2 * Math.log(tree.curr.getCount())) / child.getCount()));
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
		ArrayList<House> neighbors = getNeighbors(toCheck, nNeighbors);
		Iterator<House> neighborIter = neighbors.iterator();
		// check if it is has any children
		while (neighborIter.hasNext()) {
			House houseToAdd = neighborIter.next();
			float distToNeighbor = (float) toCheck.getHouse().getDistanceTo(houseToAdd);
			float distLeft = toCheck.getDist() - distToNeighbor;
			if (distLeft > 0) { // only change ans if there is a child that is
								// able to be added
				ans = false;
			}
		}
		return ans;
	}

	private static void expandNode(Node toExpand) {
		// will expand the node as long as the node is not a terminal state and the distance is not overcome
		if (!isTerminalState(toExpand, nNeighExpand)) {
			// starts as leaf, some number of actions are added
			ArrayList<House> neighbors = getNeighbors(toExpand, nNeighExpand);
		
			Iterator<House> neighborIter = neighbors.iterator();
			// add all children
			while (neighborIter.hasNext()) {
				House houseToAdd = neighborIter.next();
				float distToNeighbor = (float) toExpand.getHouse().getDistanceTo(houseToAdd);
				float distLeft = toExpand.getDist() - distToNeighbor;
				if (distLeft > 0) { // only add the child if it does not go over
									// the rest of the distance
					toExpand.addChild(new Node(toExpand, houseToAdd, distLeft));
				}
			}
		}
	}

	public static void makeMCTS() {
		while (numIterBuildTree > 0) {
			System.out.println(numIterBuildTree);
			// Part I: Selection
			// find the child that maximizes the algorithm, and eventually the leaf
			while (!tree.curr.isLeaf()) {
				Node bestChild = getBestChild(tree.curr);
				tree.curr = bestChild;
			}

			// Part II: Expansion
			if (tree.curr.getCount() != 0) {
				// add children to the current node
				if (!isTerminalState(tree.curr, nNeighExpand)) {
					expandNode(tree.curr);
				}
				
				// find most promising child as well
				tree.curr = getBestChild(tree.curr);
			}

			// Part III: Rollout (do this from whichever node is set as curr in the tree)
			double ROVal = 0;
			if (!isTerminalState(tree.curr, nNeighExpand)) {
				double simIter = 15;
				double sumSimVals = 0;
				while (simIter > 0) {
					// get simulation value
					double simVal = getSimulationValue(tree.curr);
					sumSimVals = sumSimVals + simVal;
					// update simulation iteration number
					simIter = simIter - 1;
				}
				ROVal = sumSimVals / 15;
			}

			// Part IV: Update
			while (tree.curr.getParent() != null) {
				tree.curr.addCount();
				tree.curr.setSumVals((float) (tree.curr.getSumVals() + ROVal));

				float qValToAdd = (1 / tree.curr.getCount()) * tree.curr.getSumVals();
				tree.curr.setQVal(qValToAdd);
				tree.curr = tree.curr.getParent();
			}
			// need to add to final count for the ROOT NODE, otherwise will not update
			tree.curr.addCount();
			tree.curr.setSumVals((float) (tree.curr.getSumVals() + ROVal));
			float qValToAdd = (1 / tree.curr.getCount()) * tree.curr.getSumVals();
			tree.curr.setQVal(qValToAdd);
		
			// reset the curr
			tree.curr = tree.root;
			
			// update iterations
			numIterBuildTree = numIterBuildTree - 1;
		}
	}

	public static double getSimulationValue(Node toSimulate) {
		Node curr = new Node(toSimulate.getParent(), toSimulate.getHouse(), toSimulate.getDist());
		double sum = 0;
		int cnt = 1;
		while (!isTerminalState(curr, nNeighExpand)) {
			// update sum value
			if (curr.getHouse().getCategory() == 1) {
				sum = sum + 0.2;
			} else if (curr.getHouse().getCategory() == 2) {
				sum = sum + 0.4;
			} else if (curr.getHouse().getCategory() == 3) {
				sum = sum + 0.6;
			} else if (curr.getHouse().getCategory() == 4) {
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
		House prevMark = null;
		while (!tree.curr.isLeaf()) {
			// pause between each decision made
			try {
				Thread.sleep(pause);
			} catch (InterruptedException e) {
			}

			prevMark = tree.curr.getHouse();

			// the previous house has now been 'clicked'
			prevMark.setSearched(true);

			// add point for triangulation, draw function will get the
			// triangulation and draw it
			triPointList.add(
					new Vertex((double) prevMark.getLongitude(), (double) prevMark.getLatitude()));

			// next house to search (MCTS algorithm)
			House nextU = null;
			Iterator<Node> childIter = tree.curr.getChildren().iterator();
			Node maxChild = childIter.next();
			while (childIter.hasNext()) {
				Node toCheck = childIter.next();
				if (toCheck.getQVal() > maxChild.getQVal()) {
					maxChild = toCheck;
				}
			}
			nextU = maxChild.getHouse();

			// update the distance left to travel
			distanceLeftToTravel = distanceLeftToTravel - prevMark.getDistanceTo(nextU);

			// update the total distance
			totalDistance = totalDistance + prevMark.getDistanceTo(nextU);

			// add line to show path
			HouseLine toAddLine = new HouseLine(prevMark, nextU);

			lineList.add(toAddLine);

			// update the curr of the tree
			tree.curr = maxChild;
		}
	}

	// From the given house, get the closest house that is the color given
	public static House getClosestColor(House lm, int color) {
		House toReturn = null;
		for (House hm : houseList) {
			House compare = hm;
			if (toReturn == null) {
				toReturn = compare;
			} else {
				if (!compare.getSearched() && compare.getCategory() == color && !(compare.equals(lm))
						&& lm.getDistanceTo(compare) < lm.getDistanceTo(toReturn)) {
					toReturn = compare;
				}
			}
		}
		return toReturn;
	}

	public static void main(String args[]) {
		// read all houses into the simulation
		try {
			readHouseGPS();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		File fileName = new File(args[0]);
		outputFileName = args[0];
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		String pauseToParse = null;
		String simsToParse = null;
		String distToParse = null;
		String cpToParse = null;
		String nNeighExpandToParse = null;
		String numIterBuildTreeToParse = null;
		try {
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
			
			numIterBuildTreeToParse = br.readLine();
			numIterBuildTreeToParse = numIterBuildTreeToParse.trim();
			numIterBuildTreeToParse = numIterBuildTreeToParse.replaceAll("NUM_ITER_BUILD_TREE = ", "");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		pause = Integer.parseInt(pauseToParse);
		nSims = Integer.parseInt(simsToParse);
		sims = new double[nSims][14];
		distanceLeftToTravelSave = Double.parseDouble(distToParse);
		distanceLeftToTravel = Double.parseDouble(distToParse);
		Cp = Integer.parseInt(cpToParse);
		nNeighExpand = Integer.parseInt(nNeighExpandToParse);
		numIterBuildTree = Integer.parseInt(numIterBuildTreeToParse);
		numIterBuildTreeSave = Integer.parseInt(numIterBuildTreeToParse);

		// invoke MCTS function
		tree = new MCTSTree(new Node(null, houseList.get(2), (float) distanceLeftToTravelSave));
		makeMCTS();
		traverseTree();
		
		// write all results
		// display triangulation
		Triangulation delaunayMesh = new Triangulation();
		delaunayMesh.addAllVertices(triPointList);
		try {
			delaunayMesh.triangulate();
		} catch (InvalidVertexException e) {
			e.printStackTrace();
		}

		LinkedHashSet<Triangle> triList = delaunayMesh.getTriangles();

		// get triangle information
		int[] cntHouses = new int[triList.size()];

		int cnt = 0;
		int total = 0;
		for (Triangle t : triList) {
			for (House cntHouse : houseList) {
				// make boundaryTriangle
				Point[] vertices = new Point[3];

				vertices[0] = new Point(t.a.x, t.a.y);
				vertices[1] = new Point(t.b.x, t.b.y);
				vertices[2] = new Point(t.c.x, t.c.y);
				BoundaryTriangle boundTri = new BoundaryTriangle(vertices);

				// check if point is within boundaryTriangle
				if (boundTri.contains(new Point((double) cntHouse.getLongitude(),
						(double) cntHouse.getLatitude()))) {
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

		for (House m : houseList) {
			if (m.getSearched() && m.getInfested()) {
				cntInfestSearch++;
			}
			if (m.getSearched()) {
				cntSearch++;
			}
			if (m.getCategory() == 4 && !m.getSearched()) {
				cntMostHighRiskNotSearched++;
			}
			if (m.getCategory() == 4 && m.getSearched()) {
				cntMostHighRiskSearched++;
			}
			if (m.getCategory() == 3 && m.getSearched()) {
				cntHighRiskSearched++;
			}
			if (m.getCategory() == 2 && m.getSearched()) {
				cntMedRiskSearched++;
			}
			if (m.getCategory() == 1 && m.getSearched()) {
				cntLowRiskSearched++;
			}
			if (m.getCategory() == 0 && m.getSearched()) {
				cntMostLowRiskSearched++;
			}
		}

		if (nSims > 0) {
			// save results in table
			sims[nSims - 1][0] = houseList.size();
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
				houseList = new ArrayList<House>();
				lineList = new ArrayList<HouseLine>();
				distanceLeftToTravel = distanceLeftToTravelSave;
				totalDistance = 0;
				triPointList = new ArrayList<Vertex>();
				numIterBuildTree = numIterBuildTreeSave;

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
package ai.files;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class PrecomputeNeighbors {

	ArrayList<House> houses = new ArrayList<House>();
	ArrayList<ArrayList<House>> houseMat = new ArrayList<ArrayList<House>>();

	public PrecomputeNeighbors(String fileToCompute) throws IOException {
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
		
		// get closest neighbors
		for (House h : houses) {
			ArrayList<House> neighbors = getNeighbors(h, houses.size() - 1);
			
			// add the house to the first index
			neighbors.add(0, h);
			
			// add to the matrix
			houseMat.add(neighbors);
		}
		
		// write csv
		writeTextFile();
	}
	
	// write csv file in certain format for loading
	public void writeTextFile() {
		String fileName = new String("precomputeNeighbors");
		File fileToWrite = new File("precomputeNeighbors/", fileName + ".txt");
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileToWrite));

			// add all houses first
			for (House hou : houses) {
				StringBuilder sb = new StringBuilder();
				
				sb.append(hou.getUnicode()).append("\n");
				sb.append(hou.getLatitude()).append("\n");
				sb.append(hou.getLongitude()).append("\n");
				sb.append(hou.getBlock()).append("\n");
				sb.append(hou.getCategory()).append("\n");
				
				String toAdd = sb.toString();
				
				writer.append(toAdd);
			}
			writer.append("..........");
			
			
			// put everything into one string, then append
			for (ArrayList<House> houseList : houseMat) {
				for (House h : houseList) {
					writer.append("\n");
					
					String toAdd = h.getUnicode();
					
					writer.append(toAdd);
				}
				writer.append("\n");
				writer.append(".....");
			}
			writer.flush();
			writer.close();
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	// get closest neighbors helper function
	public ArrayList<House> getNeighbors(House h, int nNeigh) {
		// find the 'nNeigh' closest neighbors for each house
		HashSet<House> s = new HashSet<House>();
		for (House neighbor : houses) {
			if (h.equals(neighbor)) {
				// do nothing, it is the same house
			} else {
				if (s.size() < nNeigh) {
					s.add(neighbor);
				} else {
					// 1. get max from the set
					Iterator<House> iter = s.iterator();
					House max = iter.next();
					while (iter.hasNext()) {
						House compareMax = iter.next();
						float distanceOld = (float) h.getDistanceTo(max);
						float distanceNew = (float) h.getDistanceTo(compareMax);

						// compare, replace if necessary
						if (distanceOld > distanceNew) {
							max = compareMax;
						}
					}

					// 2. replace if necessary
					float distanceMax = (float) h.getDistanceTo(max);
					float distanceNewMax = (float) h.getDistanceTo(neighbor);

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
				if (toAdd.compareDist(h, srtdN.get(j)) == -1) {
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
	
	public static void main(String args[]) {
		try {
			@SuppressWarnings("unused")
			PrecomputeNeighbors pc = new PrecomputeNeighbors("forSimulator.csv");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

package ai.files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class PrecomputeBlockMatrix {

	HashMap<Block, ArrayList<Block>> blockMat = new HashMap<Block, ArrayList<Block>>();
	
	public PrecomputeBlockMatrix() throws IOException {
		// this map is used in order to prepare the houses before creating the block objects
		HashMap<Integer, ArrayList<House>> blockMap = new HashMap<Integer, ArrayList<House>>();
		
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
				House houseToAdd = new House(uni, Integer.parseInt(block), Float.parseFloat(lat), Float.parseFloat(lon),
						Integer.parseInt(quant));
				
				if (!blockMap.keySet().contains(houseToAdd.getBlock())) {
					ArrayList<House> housesInBlock = new ArrayList<House>();
					housesInBlock.add(houseToAdd);
					blockMap.put(houseToAdd.getBlock(), housesInBlock);
				} else {
					// update house list
					blockMap.get(houseToAdd.getBlock()).add(houseToAdd);
				}
			}
			currentLine = br.readLine();
		}
		br.close();

		// convert the block map to a block list
		ArrayList<Block> blockList = new ArrayList<Block>();
		for (Integer blockNum : blockMap.keySet()) {
			blockList.add(new Block(blockNum, blockMap.get(blockNum)));
		}

		// now that we have the blocks, for each block we need to get the closest
		// blocks from closest to farthest away
		for (Block block : blockList) {
			ArrayList<Double> neighBlocksDist = new ArrayList<Double>();
			for (int i=0; i < blockList.size(); i++) {
				if (block == blockList.get(i)) {
					neighBlocksDist.add(Double.POSITIVE_INFINITY);
				} else {
					neighBlocksDist.add(block.getDistanceTo(blockList.get(i)));
				}
			}
			
			ArrayList<Block> sortedNeighBlocks = new ArrayList<Block>();
			boolean stillMore = true;
			while(stillMore) {
				Block blockNeigh = blockList.get(neighBlocksDist.indexOf(Collections.min(neighBlocksDist)));
				sortedNeighBlocks.add(blockNeigh);
				
				// set this to positive infinity
				neighBlocksDist.set(neighBlocksDist.indexOf(Collections.min(neighBlocksDist)), Double.POSITIVE_INFINITY);
				
				// check whether or not the entire array is made up of positive infinities
				boolean ans = false;
				for (Double dist : neighBlocksDist) {
					if (dist != Double.POSITIVE_INFINITY) {
						ans = true;
					}
				}
				stillMore = ans;
			}
			
			blockMat.put(block, sortedNeighBlocks);
		}
	}

	// get the matrix
	public HashMap<Block, ArrayList<Block>> getBlockMatrix() {
		return blockMat;
	}
}

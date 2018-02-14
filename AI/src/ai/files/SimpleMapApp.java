package ai.files;

import processing.core.PApplet;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.providers.Microsoft;

/**
 * Chiri Run AI Justin Sheen August/September 2017
 * 
 * AI application using MDPs and Q-learning to optimize the actions of
 * inspectors to find more infested houses using learning algorithms.
 * 
 */

@SuppressWarnings("serial")
public class SimpleMapApp extends PApplet {

	UnfoldingMap map;
	Location arequipaLocation = null;
	static List<SimplePointMarker> houseMarkers = new ArrayList<SimplePointMarker>();
	static QLearningTable q = new QLearningTable();
	
	static SimpleLinesMarker slm = new SimpleLinesMarker();

	static double time = 500; // finite time horizon (number of houses visited)
	static double inspecTime = 20;// 20 minutes per inspection
	static double walkTime = 15; // 15 minutes per kilometer

	static double saveTime = 300;
	static double saveInspecTime = 20;
	static double saveWalkTime = 15;
	
	static int fifteenReward = 0;
	static int fifteenRVal = 0;

	double totalDistance = 0; // total distance traveled for end game output

	static LabeledMarker prevMark = null;
    
	static int countSearch = 0;
	static ArrayList<Integer> searchRecord = new ArrayList<Integer>();
	
	
	// creates house markers
	public void readHouseGPS() throws IOException {
		File fileName = new File(System.getProperty("user.home")
				+ "/PETM-shiny/Static_Data_formodel/MERGES_BLOCKS_GPS_ROCIADO/MM_gps_rociado.csv");
		BufferedReader br = new BufferedReader(new FileReader(fileName));

		String currentLine = br.readLine();
		currentLine = br.readLine();
		while (currentLine != null) {
			int uniEnd = currentLine.indexOf(",");
			int pEnd = currentLine.indexOf(",", uniEnd + 1);
			int dEnd = currentLine.indexOf(",", pEnd + 1);
			int lEnd = currentLine.indexOf(",", dEnd + 1);
			int vEnd = currentLine.indexOf(",", lEnd + 1);
			int latEnd = currentLine.indexOf(",", vEnd + 1);
			int lonEnd = currentLine.indexOf(",", latEnd + 1);
			int blockEnd = currentLine.indexOf(",", lonEnd + 1);

			String uni = currentLine.substring(0, uniEnd);
			String p = currentLine.substring(uniEnd + 1, pEnd);
			String d = currentLine.substring(pEnd + 1, dEnd);
			String l = currentLine.substring(dEnd + 1, lEnd);
			String v = currentLine.substring(lEnd + 1, vEnd);
			String lat = currentLine.substring(vEnd + 1, latEnd);
			String lon = currentLine.substring(latEnd + 1, lonEnd);
			String block = currentLine.substring(lonEnd + 1, blockEnd);

			uni = uni.trim(); uni = uni.replaceAll("\"", "");
			p = p.trim(); p = p.replaceAll("\"", "");
			d = d.trim(); d = d.replaceAll("\"", "");
			l = l.trim(); l = l.replaceAll("\"", "");
			v = v.trim(); v = v.replaceAll("\"", "");
			lat = lat.trim(); lat = lat.replaceAll("\"", "");
			lon = lon.trim(); lon = lon.replaceAll("\"", "");
			block = block.trim(); block = block.replaceAll("\"", "");

			if (p.equals("1") && d.equals("10") && l.equals("38")) {

				if (block.equals("NA")) {
					// do nothing
				} else {
					Location houseLocation = new Location(Float.parseFloat(lat), Float.parseFloat(lon));
					LabeledMarker marker = new LabeledMarker(houseLocation, uni, Integer.parseInt(block));
					houseMarkers.add(marker);
					if (arequipaLocation == null) {
						arequipaLocation = new Location(Float.parseFloat(lat), Float.parseFloat(lon));
						System.out.println(arequipaLocation);
					}
				}
			}
			currentLine = br.readLine();
		}

		for (SimplePointMarker marker : houseMarkers) {
			Marker toAddMarker = marker;
			toAddMarker.setColor(211);

			Random random = new Random();

			int ans = random.nextInt(35);
			if (ans == 0) {
				((LabeledMarker) marker).infested = true;
				((LabeledMarker) marker).category = randomRisk(true);
			} else {
				((LabeledMarker) marker).infested = false;
				((LabeledMarker) marker).category = randomRisk(false);
			}

			map.addMarker(toAddMarker);
		}

		// Add all states to the Q-table (27 in total)
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				for (int k = 0; k < 4; k++) {
					Integer[] toAdd = new Integer[3];
					toAdd[0] = i;
					toAdd[1] = j;
					toAdd[2] = k;
					State toAddS = new State(toAdd);
					q.checkStateExist(toAddS);
				}
			}
		}
		
		br.close();
	}

	// get closest neighbors helper function
	public static ArrayList<LabeledMarker> getNeighbors(LabeledMarker m, int nNeigh) {
		// find the 10 closest markers for each marker
		HashSet<LabeledMarker> s = new HashSet<LabeledMarker>();
		for (SimplePointMarker neighbor : houseMarkers) {
			if (m == neighbor || ((LabeledMarker) neighbor).searched == true) {
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
						double distanceOld = m.getDistanceTo(max.getLocation());
						double distanceNew = m.getDistanceTo(compareMax.getLocation());

						// compare, replace if necessary
						if (distanceOld > distanceNew) {
							max = compareMax;
						}
					}

					// 2. replace if necessary
					double distanceMax = m.getDistanceTo(max.getLocation());
					double distanceNewMax = m.getDistanceTo(neighbor.getLocation());

					if (distanceMax > distanceNewMax) {
						s.remove(max);
						s.add((LabeledMarker) neighbor);
					}
				}
			}
		}
       
		// another way for these 10 neighbors again
		Iterator<LabeledMarker> iter = s.iterator();
		ArrayList<LabeledMarker> srtdN = new ArrayList<LabeledMarker>();
		
		while (iter.hasNext()) {
			LabeledMarker toAdd = iter.next();
			for (int j = 0; j < srtdN.size(); j++) {
				if (toAdd.compareDist(m, srtdN.get(j)) == -1) {
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

	// set the pattern of infestation
	public int randomRisk(boolean infested) {
		Random randomRisk = new Random();
		int ans = randomRisk.nextInt(100);
		if (infested) {
			if (ans >= 0 && ans <= 80) {
				return 0;
			} else if (ans > 80 && ans <= 95) {
				return 2;
			} else {
				return 1;
			}
		} else {
			if (ans >= 0 && ans <= 80) {
				return 1;
			} else if (ans > 80 && ans <= 95) {
				return 2;
			} else {
				return 0;
			}
		}
	}

	public void setup() {
		size(1200, 750, P2D);
		map = new UnfoldingMap(this, new Microsoft.AerialProvider());

		try {
			readHouseGPS();
		} catch (IOException e) {
			e.printStackTrace();
		}

		map.zoomAndPanTo(17, arequipaLocation);
		MapUtils.createDefaultEventDispatcher(this, map);
	}

	public void draw() {
		// map has a function to draw itself on the papplet
		map.addMarker(slm);
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
		rect(5, 5, 175, 20);
		fill(0, 0, 0);
		DecimalFormat df = new DecimalFormat("#.##");
		text("Houses left to check: " + time, 10, 20);
        
		// display prevMark
		fill(255, 255, 255);
		rect(5, 30, 153, 20);
		if (prevMark != null) {
			if (prevMark.infested) {
				fill(255, 0, 0);
				text ("FOUND INFESTED HOUSE!", 10, 45);
			} else {
				fill(0, 0, 0);
				text ("NOT INFESTED...", 10, 45);
			}
		}
		
		// end game
		if (time <= 0) {
			// write csv file
			writeCsvFile("results.csv");
			
			int cntSearch = 0;
			int cntSOne = 0;
			int cntSTwo = 0;
			int cntSThree = 0;
			int cntInfest = 0;
			int cntIOne = 0;
			int cntITwo = 0;
			int cntIThree = 0;
			int cntFalse = 0;

			for (Marker m : houseMarkers) {
				if (((LabeledMarker) m).searched && ((LabeledMarker) m).infested) {
					cntSearch++;
					if (((LabeledMarker) m).category == 0) {
						cntSOne++;
					} else if (((LabeledMarker) m).category == 1) {
						cntSTwo++;
					} else if (((LabeledMarker) m).category == 2) {
						cntSThree++;
					}
				} else if (!((LabeledMarker) m).searched && ((LabeledMarker) m).infested) {
					cntInfest++;
					if (((LabeledMarker) m).category == 0) {
						cntIOne++;
					} else if (((LabeledMarker) m).category == 1) {
						cntITwo++;
					} else if (((LabeledMarker) m).category == 2) {
						cntIThree++;
					}
				} else if (((LabeledMarker) m).searched && !((LabeledMarker) m).infested) {
					cntFalse++;
				}
			}

			Object[] options = { "OK" };
			JOptionPane.showOptionDialog(null,
					"Number of houses: " + houseMarkers.size() + "\n\n" + 
					"Number of infested houses searched: " + cntSearch + "\n" + "Previously infested: " + cntSOne + "\n"
							+ "Previously uninfested: " + cntSTwo + "\n" + "Previously uninspected: " + cntSThree  
							+ "\n\n" + "Number of infested houses not searched: " + cntInfest + "\n" 
							+ "Previously infested: " + cntIOne + "\n" + "Previously uninfested: " + cntITwo + "\n" 
							+ "Previously uninspected: " + cntIThree + "\n\n" 
							+ "Number of searched houses that were not infested: " + cntFalse + "\n\n" 
							+ "Total distance traveled (kilometers): " + df.format(totalDistance),
					"END OF SIMULATION", 
					JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			time = 0;
		}
	}
   
	public static State getState(ArrayList<LabeledMarker> list) {
		Integer[] arrNeigh = new Integer[10];
		arrNeigh[0] = list.get(0).category; arrNeigh[1] = list.get(1).category; 
		arrNeigh[2] = list.get(2).category; arrNeigh[3] = list.get(3).category; 
		arrNeigh[4] = list.get(4).category; arrNeigh[5] = list.get(5).category;
		arrNeigh[6] = list.get(6).category; arrNeigh[7] = list.get(7).category; 
		arrNeigh[8] = list.get(8).category; arrNeigh[9] = list.get(9).category; 
		
		// get stateNeigh from previous array of 10 neighbors
		Integer[] stateNeigh = new Integer[3];
		stateNeigh[0] = arrNeigh[0]; // closest
		stateNeigh[1] = arrNeigh[1]; // second closest
		int nZero = 0; // third 'cell'
		int nOne = 0;
		int nTwo = 0;
		for (int i = 2; i < 10; i++) {
			if (arrNeigh[i] == 0) { nZero++; }
			else if (arrNeigh[i] == 1) { nOne++; }
			else { nTwo++; }
		}
		int colorAns = max(nZero, nOne, nTwo); // determine max
		if (colorAns == nZero) { stateNeigh[2] = 0; }
		else if (colorAns == nOne) { stateNeigh[2] = 1; }
		else { stateNeigh[2] = 2; }
		
		State toReturn = new State(stateNeigh);
		return toReturn;
	}
	
	public static void writeCsvFile(String fileName) {
		//Delimiter used in CSV file
		final String NEW_LINE_SEPARATOR = "\n";

		try {
			FileWriter fileWriter = new FileWriter(fileName);
			while (!searchRecord.isEmpty()) {
				Integer toAdd = searchRecord.remove(0);
				fileWriter.append(String.valueOf(toAdd));
				fileWriter.append(NEW_LINE_SEPARATOR);
			}
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void updateFunction() {
		while (time > 0) {
			// pause between each decision made
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}

			double reward = -1;
			if (prevMark == null) {
				SimplePointMarker m = houseMarkers.get(2); // get one of the closest points
				prevMark = (LabeledMarker) m;
			} else {
				// the previous marker has now been 'clicked'
				prevMark.searched = true;
				prevMark.isPrevMark = true;
				
				// next marker to search
				LabeledMarker nextU = null;
				
				if (fifteenReward == 14) {
					if (fifteenRVal < 6) {
						// Here, we must go to a new site (that has not been searched)
						ArrayList<LabeledMarker> randNotSearch = new ArrayList<LabeledMarker>();
						for (SimplePointMarker j : houseMarkers) {
							if (!((LabeledMarker) j).searched) {
								randNotSearch.add((LabeledMarker) j);
							}
						}
						Random levyRandom = new Random();
						int ans = levyRandom.nextInt(randNotSearch.size() - 1);
						prevMark.isPrevMark = false; // old prevMark
						
						prevMark = (LabeledMarker) randNotSearch.get(ans);
						prevMark.isPrevMark = true;
						prevMark.searched = true;
					}
					if (prevMark.infested) { // reset
						fifteenReward = 1;
						fifteenRVal = 1;
					} else {
						fifteenReward = 1;	
					}
				} else {

				// closest neighbor array of the previous marker
				ArrayList<LabeledMarker> prevMarkN = getNeighbors(prevMark, 10);
					
				// go to next state and observe 
				State s = getState(prevMarkN);
				int action = q.selectAction(s);
				
				// (cont'd) get the next labeledMarker
				if (action == 0) {
					// closest
					nextU = prevMarkN.get(0);
				} else if (action == 1) {
					// second closest
					nextU = prevMarkN.get(1);
				} else if (action == 2) {
					// closest black
				    nextU = getClosestColor(prevMark, 0);
				} else if (action == 3) {
					// closest white
					nextU = getClosestColor(prevMark, 1);
				} else if (action == 4) {
					// closest grey
					nextU = getClosestColor(prevMark, 2);
				}
                
				if (nextU.infested) {
					reward = 1; // distance does not yet factor into reward, just time.
					fifteenReward++;
					fifteenRVal++;
					countSearch++;
					searchRecord.add(countSearch);
				} else {
					reward = 0;
					fifteenReward++;
					searchRecord.add(countSearch);
				}
				
				// get 'next' labeled marker's state
                ArrayList<LabeledMarker> nextMarkN = getNeighbors(nextU, 10);
                State s_ = getState(nextMarkN);
				// learn from the observation
				q.learn(s, action, s_, reward);

				// replace for next loop iteration
				prevMark.isPrevMark = false; // set to prevMark == false
				prevMark = nextU;

				} // end of else statement

				time = time - 1;
				
			}
		}
	}
    
	public static LabeledMarker getClosestColor(LabeledMarker lm, int color) {
		LabeledMarker toReturn = null;
		for (SimplePointMarker hm : houseMarkers) {
			LabeledMarker compare = (LabeledMarker) hm;
			if (toReturn == null) {
				toReturn = compare;
			} else {
				if (!compare.searched && compare.category == color && !(compare == lm) && 
						lm.getDistanceTo(compare.getLocation()) < lm.getDistanceTo(toReturn.getLocation())) {
					toReturn = compare;
				}
			}
		}
		return toReturn;
	}
	
	public static void main(String args[]) {
		// main window
		PApplet.main(new String[] { SimpleMapApp.class.getName() });

		// title
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
		try {
			Thread.sleep(13000);
		} catch (InterruptedException e) {
		}
		f.dispose();
        
		// update function
		updateFunction();
	}

}
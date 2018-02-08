package de.fhpotsdam.unfolding.examples;

public class Scratch {
//	// create block lines
//		public void readBlockGPS() throws IOException {
//			File fileName = new File(System.getProperty("user.home") + 
//					"/PETM-shiny/unicode_numberMzn/Manzanas _Arequipa/"
//					+ "Mariano Melgar/MARIANO MELGAR.csv");
//			BufferedReader br = new BufferedReader(new FileReader(fileName));
//			
//			String currentLine = br.readLine();
//			currentLine = br.readLine();
//			
//			ArrayList<SimplePointMarker> l = new ArrayList<SimplePointMarker>();
//			boolean startNew = false;
//			
//			while (currentLine != null) {
//				int trackEnd = currentLine.indexOf(",");
//				int identEnd = currentLine.indexOf(",", trackEnd + 1);
//				int latEnd = currentLine.indexOf(",", identEnd + 1);
//				int lonEnd = currentLine.indexOf(",", latEnd + 1);
//				
//				String track = currentLine.substring(0, trackEnd);
//				String ident = currentLine.substring(trackEnd + 1, identEnd);
//				String lat = currentLine.substring(identEnd + 1, latEnd);
//				String lon = currentLine.substring(latEnd + 1, lonEnd);
//				
//				track = track.trim();
//				ident = ident.trim();
//				lat = lat.trim();
//				lon = lon.trim();
//				
//				if (lat.equals("") && lon.equals("")) {
//					startNew = true;
//					connectionMarkers.add(l); // add the previous one
//				} else {
//					if (startNew) {
//						l = new ArrayList<SimplePointMarker>();
//						l.add(new SimplePointMarker(new Location(Float.parseFloat(lat), Float.parseFloat(lon))));
//						startNew = false;
//					} else {
//						l.add(new SimplePointMarker(new Location(Float.parseFloat(lat), Float.parseFloat(lon))));
//					}
//				}
//				currentLine = br.readLine();
//			}
//			connectionMarkers.add(l); // add last manzana
//			
//			connectionMarkers.remove(0); // remove the first one (empty)
//			Iterator<ArrayList<SimplePointMarker>> iter = connectionMarkers.iterator();
//			
//			while (iter.hasNext()) {
//				ArrayList<SimplePointMarker> currL = iter.next();
//				Iterator<SimplePointMarker> miter = currL.iterator();
//				SimplePointMarker first = miter.next();
//				SimplePointMarker prev = first;
//				while (miter.hasNext()) {
//					SimplePointMarker curr = miter.next();
//					ConnectionMarker cm = new ConnectionMarker(prev, curr);
//					cMarkers.add(cm);
//					prev = curr;
//				}
//				ConnectionMarker last = new ConnectionMarker(first, prev);
//				cMarkers.add(last);
//			}
//			System.out.println(cMarkers.size());
//			br.close();
//		}
	
	
//	Iterator<ConnectionMarker> citer = cMarkers.iterator();
//	
//	while (citer.hasNext()) {
//		List<Location> list = citer.next().getLocations();
//		
//		Location first = list.get(0);
//		Location second = list.get(1);
//		
//		line(first.x, first.y, second.x, second.y);
//	}
	
	

//	for (SimplePointMarker marker : houseMarkers) {
//		State mState = ((LabeledMarker) marker).getState();
//		q.checkStateExist(mState);
//		System.out.println(q.table.keySet().size());
//	}
}


//// create a pop up that tells if infested or not
//if (searched == true) {
//	Object[] options = { "OK" };
//    JOptionPane.showOptionDialog(null, 
//    		     "This house has already been searched!",
//    "Already Searched!", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
//    null, options, options[0]);
//} else if (infested == true) {
//	Object[] options = { "OK" };
//    JOptionPane.showOptionDialog(null, 
//    		     "Category: " + (category+1)
//               + "\n\n"
//               + "You have found an infested house!",
//    "Infested house!", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
//    null, options, options[0]);
//    
//    searched = true;
//} else if (infested == false) {
//	Object[] options = { "OK" };
//    JOptionPane.showOptionDialog(null, 
//    		     "Category: " + (category+1)
//               + "\n\n"
//               + "This house is not infested."
//               + "\n\n"
//               + "Point Value: 0",
//    "Not infested", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
//    null, options, options[0]);
//    
//    searched = true;
//}


// create new line
//SimpleLinesMarker sl = new SimpleLinesMarker(prevMark.getLocation(), nextU.getLocation());
//sl.setColor(0);
//sl.setStrokeWeight(3);
//slm = sl;






//// order these 10 neighbors from closest to farthest
//Iterator<LabeledMarker> iter = s.iterator();
//LabeledMarker one = iter.next();
//Double oneD = m.getDistanceTo(one.getLocation());
//LabeledMarker two = iter.next();
//Double twoD = m.getDistanceTo(two.getLocation());
//LabeledMarker three = iter.next();
//Double threeD = m.getDistanceTo(three.getLocation());
//LabeledMarker four = iter.next();
//Double fourD = m.getDistanceTo(four.getLocation());
//LabeledMarker five = iter.next();
//Double fiveD = m.getDistanceTo(five.getLocation());
//LabeledMarker six = iter.next();
//Double sixD = m.getDistanceTo(six.getLocation());
//LabeledMarker seven = iter.next();
//Double sevenD = m.getDistanceTo(seven.getLocation());
//LabeledMarker eight = iter.next();
//Double eightD = m.getDistanceTo(eight.getLocation());
//LabeledMarker nine = iter.next();
//Double nineD = m.getDistanceTo(nine.getLocation());
//LabeledMarker ten = iter.next();
//Double tenD = m.getDistanceTo(ten.getLocation());
//
//ArrayList<Double> l = new ArrayList<>();
//l.add(oneD);
//l.add(twoD);
//l.add(threeD);
//l.add(fourD);
//l.add(fiveD);
//l.add(sixD);
//l.add(sevenD);
//l.add(eightD);
//l.add(nineD);
//l.add(tenD);
//
//Collections.sort(l); // closest to farthest neighbors
//
//int oneI = l.indexOf(oneD);
//int twoI = l.indexOf(twoD);
//int threeI = l.indexOf(threeD);
//int fourI = l.indexOf(fourD);
//int fiveI = l.indexOf(fiveD);
//int sixI = l.indexOf(sixD);
//int sevenI = l.indexOf(sevenD);
//int eightI = l.indexOf(eightD);
//int nineI = l.indexOf(nineD);
//int tenI = l.indexOf(tenD);
//
//ArrayList<LabeledMarker> srtdN = new ArrayList<LabeledMarker>();
//for (int i = 0; i < l.size(); i++) {
//	srtdN.add(null);
//}
//srtdN.set(oneI, one);
//srtdN.set(twoI, two);
//srtdN.set(threeI, three);
//srtdN.set(fourI, four);
//srtdN.set(fiveI, five);
//srtdN.set(sixI, six);
//srtdN.set(sevenI, seven);
//srtdN.set(eightI, eight);
//srtdN.set(nineI, nine);
//srtdN.set(tenI, ten);

////medium length action / random
//// 1. make the array to randomly choose from
//ArrayList<LabeledMarker> arrChoose = new ArrayList<LabeledMarker>();
//Iterator<LabeledMarker> iter = prevMarkN.iterator();
//iter.next(); iter.next();
//while (iter.hasNext()) {
//	LabeledMarker toAdd = iter.next();
//	if (toAdd.category == s.colors[2]) {
//		arrChoose.add(toAdd);
//	}
//}
//// 2. randomly choose from the array
//Random randomNeigh = new Random();
//int ans = randomNeigh.nextInt(arrChoose.size() - 1);
//nextU = arrChoose.get(ans);

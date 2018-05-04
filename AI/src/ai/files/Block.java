package ai.files;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class Block {
	
	int num = 0;
	float originLat = 0;
	float originLon = 0;
	ArrayList<House> housesInBlock = new ArrayList<House>();
	
	
	public Block(int toAddNum, ArrayList<House> toAddHousesInBlock) {
		num = toAddNum;
		housesInBlock = toAddHousesInBlock;
		
		originLat = getOriginLat(toAddHousesInBlock);
		originLon = getOriginLon(toAddHousesInBlock);
	}
	
	Float getOriginLat(ArrayList<House> houseList) {
		ArrayList<Float> lats = new ArrayList<Float>();
		
		Iterator<House> iter = houseList.iterator();
		while (iter.hasNext()) {
			lats.add(iter.next().getLatitude());
		}
		
		Collections.sort(lats);
		
		int numElems = lats.size();
		Float median = (float) 0;
		if (numElems % 2 == 0) { // even
			median = (lats.get((numElems - 1) / 2) + lats.get((numElems - 2) / 2) / 2);
		} else { // odd
			median = lats.get(numElems - 1 / 2);
		}
		
		return median;
	}
	
    Float getOriginLon(ArrayList<House> houseList) {
        ArrayList<Float> lons = new ArrayList<Float>();
		
		Iterator<House> iter = houseList.iterator();
		while (iter.hasNext()) {
			lons.add(iter.next().getLongitude());
		}
		
		Collections.sort(lons);
		
		int numElems = lons.size();
		Float median = (float) 0;
		if (numElems % 2 == 0) { // even
			median = (lons.get((numElems - 1) / 2) + lons.get((numElems - 2) / 2) / 2);
		} else { // odd
			median = lons.get(numElems - 1 / 2);
		}
		
		return median;
	}
	
	public void add(House toAddHouse) {
		if (toAddHouse.getBlock() != num) {
			throw new IllegalArgumentException("House is not within this block");
		}
		
		housesInBlock.add(toAddHouse);
		
		originLat = getOriginLat(housesInBlock);
		originLon = getOriginLon(housesInBlock);
	}
	
	public double getDistanceTo(Block compareBlock) {
		float lon1 = this.originLon;
		float lat1 = this.originLat;
		float lon2 = compareBlock.originLon;
		float lat2 = compareBlock.originLat;
		
	    final int R = 6371; // Radius of the earth

	    double latDistance = Math.toRadians(lat2 - lat1);
	    double lonDistance = Math.toRadians(lon2 - lon1);
	    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
	            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
	            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	    double distance = R * c * 1000; // convert to meters

	    // correct for meters to kilometers
	    distance = distance / 1000;
	    
	    return distance;
	}
	
}

package ai.files;

public class House {
	private String unicode = "";
	private float latitude = 0;
	private float longitude = 0;
	private boolean infested = false;
	private boolean searched = false;
	private int block = -1;
	private int category = -1;
	
	public House(String toAddUnicode, int toAddBlock, float toAddLat, float toAddLon, int toAddCategory) {
		unicode = toAddUnicode;
		block = toAddBlock;
		latitude = toAddLat;
		longitude = toAddLon;
		category = toAddCategory;
	}
	
	public String getUnicode() {
		return unicode;
	}
	
	public float getLatitude() {
		return latitude;
	}
	
	public float getLongitude() {
		return longitude;
	}
	
	public void setInfested(boolean toAddInfested) {
		infested = toAddInfested;
	}
	public boolean getInfested() {
		return infested;
	}
	
	public void setSearched(boolean toAddSearched) {
		searched = toAddSearched;
	}
	public boolean getSearched() {
		return searched;
	}
	
	public int getBlock() {
		return block;
	}
	
	public int getCategory() {
		return category;
	}
	
	@Override
	public boolean equals(Object toCompare) {
		if (toCompare == null) {
	        return false;
	    }
	    if (!House.class.isAssignableFrom(toCompare.getClass())) {
	        return false;
	    }
	    final House other = (House) toCompare;
	    if (this.unicode.equals(other.getUnicode())) {
	    	return true;
	    } else {
	    	return false;
	    }
	}
	
	public double getDistanceTo(House houseToGetDistanceTo) {
		float lon1 = this.longitude;
		float lat1 = this.latitude;
		float lon2 = houseToGetDistanceTo.getLongitude();
		float lat2 = houseToGetDistanceTo.getLatitude();
		
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
	
	public int compareDist(House o, House toCompare) {
		double thisD = o.getDistanceTo(this);
		double compareD = o.getDistanceTo(toCompare);

		if (thisD > compareD) {
			return 1;
		} else {
			return -1;
		}
	}

}

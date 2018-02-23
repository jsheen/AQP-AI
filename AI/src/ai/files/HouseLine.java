package ai.files;

public class HouseLine {
	private House h1 = null;
	private House h2 = null;
	
	public HouseLine(House toAddh1, House toAddh2) {
		h1 = toAddh1;
		h2 = toAddh2;
	}
	
	public House getHouseOne() {
		return h1;
	}
	
	public House getHouseTwo() {
		return h2;
	}
}

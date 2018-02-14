package ai.files;

import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import processing.core.PGraphics;

public class LabeledMarker extends SimplePointMarker {
	
	String unicode = "";
	boolean infested = false;
	boolean searched = false;
	boolean isPrevMark = false;
	Integer block = -1;

	int category = 0; // 0 == infested(black) ; 1 == uninfested(white) ; 2 == uninspected(grey)
	
	public LabeledMarker(Location location, String toAddUnicode, Integer toAddBlock) {
		super(location);
		unicode = toAddUnicode;
		block = toAddBlock;
	}
	
	public void draw(PGraphics pg, float x, float y) {
		// ignore if it is selected
		
		// color according to whether or not it is searched
		if (searched) {
			pg.fill(0, 0, 0);
		    pg.ellipse(x, y, 10, 10);
		} else if (category == 0) {
			pg.fill(255, 255, 178);
		    pg.ellipse(x, y, 10, 10);
		} else if (category == 1) {
			pg.fill(254, 204, 92);
		    pg.ellipse(x, y, 10, 10);
		} else if (category == 2){
			pg.fill(253, 141, 60);
		    pg.ellipse(x, y, 10, 10);
		} else if (category == 3){
			pg.fill(240, 59, 32);
		    pg.ellipse(x, y, 10, 10);
		} else if (category == 4){
			pg.fill(189, 0, 38);
		    pg.ellipse(x, y, 10, 10);
		}
	}
	
	public int compareDist(LabeledMarker o, LabeledMarker toCompare) {
		double thisD = o.getDistanceTo(this.getLocation());
		double compareD = o.getDistanceTo(toCompare.getLocation());
		
		if (thisD > compareD) {
			return 1;
		} else {
			return -1;
		}
	}
}

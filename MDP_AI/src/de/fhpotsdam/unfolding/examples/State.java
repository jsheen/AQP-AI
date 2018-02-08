package de.fhpotsdam.unfolding.examples;

public class State {
	
	Integer[] colors;
	
	public State (Integer[] addColors) {
		colors = addColors;
	}
	
	@Override
	public boolean equals(Object o) {
		// check trivial cases
		if (o == this) {
			return true;
		} else if (!(o instanceof State)) {
            return false;
        }
		// typecast
		State toCompare = (State) o;
		boolean ans = true;
		for (int i = 0; i < colors.length; i++) {
			if (this.colors[i] != toCompare.colors[i]) {
				ans = false;
			}
		}
		return ans;
	}
	
	@Override
	public int hashCode() {
		int ans = 17;
		ans = 31 * colors[0];
		return ans;
	}
}

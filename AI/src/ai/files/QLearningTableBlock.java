package ai.files;

import java.util.HashMap;
import java.util.Random;
import java.util.Set;

public class QLearningTableBlock {
	
	HashMap<State, double[]> table;
	double lR = 0.7; // learning rate
	double gamma = 0.6; // future rewards
	double epsilon = 0.9; // percentage of time it will choose best action
	
	public QLearningTableBlock () {
		table = new HashMap<State, double[]>();
	}
	
	public int selectAction(State s) {
		if (Math.random() < epsilon) {
			// best action
			double[] actTble = table.get(s);
			int maxActIX = 0; // initialize
			int i = 1;
			while (i < actTble.length) {
				if (actTble[maxActIX] < actTble[i]) {
					maxActIX = i;
				}
				i++;
			}
			return maxActIX;
		} else {
			// random action
			Random random = new Random();
			int result = random.nextInt(5);
			return result;	
		}
	}
	
	// we want to learn multiple things, not just for one state, 
	// but how to act in other future states as well
	public void learn(State s, Integer a, State s_, Double r) {
		// Update I: The state itself
		// old value
		double q_old = table.get(s)[a.intValue()];
		
		// find max reward action next
		double[] nxtActTble = table.get(s_);
		int maxActIX = 0; // initialize
		int i = 1;
		while (i < nxtActTble.length) {
			if (nxtActTble[maxActIX] < nxtActTble[i]) {
				maxActIX = i;
			}
			i++;
		}
		double q_opt = table.get(s_)[maxActIX];
		
		// update the state
		table.get(s)[a.intValue()] += lR * (r + gamma * (q_opt) - q_old);
		
		// Update II: States that have exact same colors
		// Update III: Learning distance (though this may be implicit in the above two)
	}
	
	// add state if it does not exist
	public void checkStateExist(State toCheck) {
		Set<State> states = table.keySet();
		if (states.contains(toCheck)) {
			// do nothing
		} else {
			table.put(toCheck, new double[5]);
		}
	}

}

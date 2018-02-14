package ai.files;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class Testing {
	
	@Test
	public void simpleStateTest() {
		Integer[] iOne = new Integer[3];
		Integer[] iTwo = new Integer[3];
		iOne[0] = 1; iOne[1] = 2; iOne[2] = 3;
		iTwo[0] = 1; iTwo[1] = 2; iTwo[2] = 3;
		
		State sOne = new State(iOne);
		State sTwo = new State(iTwo);
		
		assertEquals("These two states are the same", sOne, sTwo);
	}
	
	@Test
	public void simpleQTest() {
		Integer[] iOne = new Integer[3];
		Integer[] iTwo = new Integer[3];
		iOne[0] = 1; iOne[1] = 2; iOne[2] = 3;
		iTwo[0] = 1; iTwo[1] = 2; iTwo[2] = 3;
		
		State sOne = new State(iOne);
		State sTwo = new State(iTwo);
		
		double[] act = new double[3];
		QLearningTable q = new QLearningTable();
		q.checkStateExist(sOne);
		assertEquals("Size makes sense", q.table.size(), 1);
		assertTrue("Returns correct value", Arrays.equals(q.table.get(sOne), act));
		assertTrue("Returns correct value", Arrays.equals(q.table.get(sTwo), act));
	}
	
	@Test
	public void doublesEqualTest() {
		Double a = new Double(0.0);
		Double b = new Double(0.0);
		
		List<Double> l = new ArrayList<Double>();
		l.add(a);
		l.add(b);
		int ai = l.indexOf(a);
		int bi = l.indexOf(b);
		
		assertEquals("These two doubles are equal", a, b);
		assertEquals("These two indices are the same", ai, bi);
	}
}

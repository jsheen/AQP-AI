package ai.files;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ai.files.MCTSTree.Node;
import ai.files.Triangulation.InvalidVertexException;

public class Testing {
	
	@Test
	public void simpleAncestorTest() {
		MCTSTree t = new MCTSTree(1);
		t.curr.addChild(new Node(t.curr, new House("1.13.100.528", 19, (float) -16.40531, (float) -71.49131,  4), 1));
		t.curr = t.curr.getChildren().get(0);
		t.curr.addChild(new Node(t.curr, new House("1.13.100.516", 19, (float) -16.40231, (float) -71.49333,  4), 1));
		t.curr = t.curr.getChildren().get(0);
		assertTrue("One state is the ancestor of the other", t.curr.isAncestorHouse(t.curr.getParent().getHouse()));
	}
	
	@Test
	public void simpleTriangulationTest() {
		
		Vertex v1 = new Vertex(-16.40585, -71.49106);
		Vertex v2 = new Vertex(-16.40586, -71.49113);
		Vertex v3 = new Vertex(-16.405825, -71.4911);
		Vertex v4 = new Vertex(-16.405602, -71.491135);
		Vertex v5 = new Vertex(-16.405617, -71.49121);
		Vertex v6 = new Vertex(-16.405472, -71.49128);
		
		List<Vertex> vl = new ArrayList<Vertex>();
		vl.add(v1); vl.add(v2); vl.add(v3); vl.add(v4); vl.add(v5); vl.add(v6);
		
		Triangulation delaunayMesh = new Triangulation();
		delaunayMesh.addAllVertices(vl);
		
		try {
			delaunayMesh.triangulate();
		} catch (InvalidVertexException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void anotherSimpleTriangulationTest() {
		
		Vertex v1 = new Vertex(-71.49115753173828, -16.406145095825195);
		Vertex v2 = new Vertex(-71.49131774902344, -16.406198501586914);
		Vertex v3 = new Vertex(-71.49127197265625, -16.406190872192383);
		
		List<Vertex> vl = new ArrayList<Vertex>();
		vl.add(v1); vl.add(v2); vl.add(v3);
		
		Triangulation delaunayMesh = new Triangulation();
		delaunayMesh.addAllVertices(vl);
		
		try {
			delaunayMesh.triangulate();
		} catch (InvalidVertexException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void fourCornersTriangulationTest() {

		Vertex v1 = new Vertex(-71.49650655517578, -16.40518106689453);
		Vertex v2 = new Vertex(-71.49650655517578, -16.40978132019043);
		Vertex v3 = new Vertex(-71.49046243896484, -16.40518106689453);
		Vertex v4 = new Vertex(-71.49046243896484, -16.40978132019043);
		
		List<Vertex> vl = new ArrayList<Vertex>();
		vl.add(v1); vl.add(v2); vl.add(v3); vl.add(v4);
		
		Triangulation delaunayMesh = new Triangulation();
		delaunayMesh.addAllVertices(vl);
		
		Throwable caught = null;
		try {
			delaunayMesh.triangulate();
		} catch (InvalidVertexException e) {
			caught = e;
			e.printStackTrace();
		}
		assertNull(caught);
	}
	
	@Test
	public void anotherSimpleTriangulationTestWithCorners() {
		Vertex v1 = new Vertex(-71.49116, -16.406145);
		Vertex v2 = new Vertex(-71.49132, -16.406199);
		Vertex v3 = new Vertex(-71.49127, -16.40619);
		Vertex v4 = new Vertex(-71.49115753173828, -16.406145095825195);
		Vertex v5 = new Vertex(-71.49131774902344, -16.406198501586914);
		Vertex v6 = new Vertex(-71.49127197265625, -16.406190872192383);
		Vertex v7 = new Vertex(-71.49650655517578, -16.40518106689453);
		Vertex v8 = new Vertex(-71.49650655517578, -16.40978132019043);
		Vertex v9 = new Vertex(-71.49046243896484, -16.40518106689453);
		Vertex v10 = new Vertex(-71.49046243896484, -16.40978132019043);
		
		List<Vertex> vl = new ArrayList<Vertex>();
		vl.add(v1); vl.add(v2); vl.add(v3); vl.add(v4); vl.add(v5);
		vl.add(v6); vl.add(v7); vl.add(v8); vl.add(v9); vl.add(v10);
		
		Triangulation delaunayMesh = new Triangulation();
		delaunayMesh.addAllVertices(vl);
		
		try {
			delaunayMesh.triangulate();
		} catch (InvalidVertexException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void shouldFail() {
		
		Vertex v1 = new Vertex(-71.49046243896484, -16.40518106689453);
		Vertex v2 = new Vertex(-71.49046243896484, -16.40978132019043);
		Vertex v3 = new Vertex(-71.49650655517578, -16.40978132019043);
		Vertex v4 = new Vertex(-71.49650655517578, -16.40518106689453);
		Vertex v5 = new Vertex(-71.49046243896484, -16.40518106689453);
		Vertex v6 = new Vertex(-71.49046243896484, -16.40978132019043);
		Vertex v7 = new Vertex(-71.49650655517578, -16.40978132019043);
		Vertex v8 = new Vertex(-71.49650655517578, -16.40518106689453);
		
		List<Vertex> vl = new ArrayList<Vertex>();
		vl.add(v1); vl.add(v2); vl.add(v3); vl.add(v4); vl.add(v5);
		vl.add(v6); vl.add(v7); vl.add(v8);
		
		Triangulation delaunayMesh = new Triangulation();
		delaunayMesh.addAllVertices(vl);
		
		try {
			delaunayMesh.triangulate();
		} catch (InvalidVertexException e) {
			e.printStackTrace();
		}
	}
	
	
}

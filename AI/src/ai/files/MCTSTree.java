package ai.files;

import java.util.ArrayList;
import java.util.List;

public class MCTSTree {
	
	Node root = null; // true root node
	Node curr = null;
	
	public MCTSTree(float toAddDistance) {
		root = new Node(null, null, toAddDistance);
		curr = root;
	}
	
	public static class Node
	{
	   private Node parent = null;
	   private House house = null;
	   private int count = 0;
	   private float sumVals = 0;
	   private float qVal = 0;
	   private List<Node> children = new ArrayList<Node>();
	   private float dist = 0;

	   public Node(Node toAddParent, House toAddHouse, float toAddDist)
	   {
		  parent = toAddParent;
	      house = toAddHouse;
	      dist = toAddDist;
	   }
	   
	   public boolean isRoot() {
		   if (parent == null & house == null) {
			   return true;
		   } else {
			   return false;
		   }
	   }
	   
	   public void addChild(Node toAdd) {
		   this.children.add(toAdd);
	   }
	   
	   public boolean isLeaf() {
		   return children.isEmpty();
	   }
	   
	   public int getCount() {
		   return count;
	   }
	   
	   public float getSumVals() {
		   return sumVals;
	   }
	   
	   public float getQVal() {
		   return qVal;
	   }
	   
	   public void addCount() {
		   count = count + 1;
	   }

	   public void setSumVals(float toSetSumVal) {
		   sumVals = toSetSumVal;
	   }
	   
	   public void setQVal(float toSetQVal) {
		   qVal = toSetQVal;
	   }
	   
	   public House getHouse() {
		   return house;
	   }
	   
	   public int getNChild(){
		   return children.size();
	   }
	   
	   public List<Node> getChildren(){
		   return children;
	   }
	   
	   public void setDist(float toAddDist) {
		   dist = toAddDist;
	   }
	   
	   public float getDist() {
		   return dist;
	   }
	   
	   public Node getParent() {
		   return parent;
	   }
	   
	   public boolean isAncestorHouse(House toCheck) {
		   if (this.isRoot()) {
			   return false;
		   } else if (this.house.equals(toCheck)) {
			   return true;
		   } else {
			   return this.parent.isAncestorHouse(toCheck);
		   }
	   }
	   
	   public int getLengthBranch() {
		   // get number of edges
		   if (this.isRoot()) {
			   return 1;
		   } else {
			   return this.parent.getLengthBranch() + 1;
		   }
	   }
	   
	   public int howLongIsBranch() {
		   if (this.isRoot()) {
			   return 0;
		   } else {
			   return 1 + this.parent.howLongIsBranch();
		   }
	   }
	   
	   public void printQvalTree(int level) {
		   for (int i = 1; i < level; i++) {
		        System.out.print("\t");
		    }
		    System.out.println(this.qVal);
		    for (Node child : children) {
		        child.printQvalTree(level + 1);
		    }
	   }
	   
	   public void printCountTree(int level) {
		   for (int i = 1; i < level; i++) {
		        System.out.print("\t");
		    }
		    System.out.println(this.count);
		    for (Node child : children) {
		        child.printCountTree(level + 1);
		    }
	   }
	}
}

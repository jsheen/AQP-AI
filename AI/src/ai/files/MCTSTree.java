package ai.files;

import java.util.LinkedList;
import java.util.List;

public class MCTSTree {
	
	Node root = null;
	Node curr = null;
	
	public MCTSTree() {
	}
	
	public static class Node
	{
	   private LabeledMarker house = null;
	   private int count = 0;
	   private float reward = 0;
	   private List<Node> children;

	   public Node(LabeledMarker toAddHouse)
	   {
	      house = toAddHouse;
	   }
	   
	   public void addChild(Node toAdd) {
		   if (children.isEmpty()) {
			   children = new LinkedList<Node>();
			   children.add(toAdd);
		   } else {
			   this.children.add(toAdd);
		   }
	   }
	   
	   public boolean isLeaf() {
		   return children.isEmpty();
	   }
	   
	   public int getCount() {
		   return count;
	   }
	   
	   public float getReward() {
		   return reward;
	   }
	   
	   public void addCount() {
		   count = count + 1;
	   }
	   
	   public void setCount(int toSetCount) {
		   count = toSetCount;
	   }
	   
	   public void setReward(float toSetReward) {
		   reward = toSetReward;
	   }
	   
	   public LabeledMarker getHouse() {
		   return house;
	   }
	   
	   public int getNChild(){
		   return children.size();
	   }
	   
	   public List<Node> getChildren(){
		   return children;
	   }
	}
	
	public void addNode(Node toAdd) {
		// will add the node to the current node
		if (root == null) {
			root = toAdd;
			curr = toAdd;
		} else {
			curr.addChild(toAdd);
		}
	}
	
	public int getNChildCurr() {
		// get the number of children of the current node
		return curr.getNChild();
	}
	
	public List<Node> getChildrenCurr(){
		return curr.getChildren();
	}
}

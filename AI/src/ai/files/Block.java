package ai.files;

public class Block {
	
	int nInfest = 0;
	int nUnInfest = 0;
	int nUnInspect = 0;
	
	public Block(int toAddnInfest, int toAddnUnInfest, int toAddnUnInspect) {
		nInfest = toAddnInfest;
		nUnInfest = toAddnUnInfest;
		nUnInspect = toAddnUnInspect;
	}
}

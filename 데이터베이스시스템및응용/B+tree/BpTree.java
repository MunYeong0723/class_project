import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;

//implements Externalizable
public class BpTree{
	private String datName; //index.dat
	private int degree;
	private IndexNode root;
	private DataNode leftMost;
	
	public BpTree() {}
	public BpTree(String datName, int degree) {
		this.datName = datName;
		this.degree = degree;
		this.root = null;
		this.leftMost = null;
	}
	
	public IndexNode getRoot() { return root; }
	public DataNode getLeftMost() { return leftMost; }
	public void setRoot(IndexNode parent) { root = parent; }
	public void setLeftMost(DataNode child) { leftMost = child; }
	
	public void create() {
		leftMost = new DataNode(degree);
	}
	
	public void saveData(BufferedWriter writeKey) {
		try {
			/*Node n = null;
			while(true) {
				if(n == null) {
					System.out.println("there is no IndexNode root yet");
					for(int i = 0; i<leftMost.numberOfKeys; i++) {
						writeKey.write(leftMost.array.get(i).getFirst() + ", ");
					}
					System.out.println();
					System.out.println();
					
					break;
				}
			}*/
			
			String inputDegree = degree + "\n";
			writeKey.write(inputDegree);
			
			DataNode haveToFind = leftMost;
			while(haveToFind != null) {
				int i = 0;
				for(; i < haveToFind.numberOfKeys; i++) {
					String input = haveToFind.array.get(i).getFirst() + "," + haveToFind.array.get(i).getSecond() + "\n";
					writeKey.write(input);
				}
				if(i == haveToFind.numberOfKeys) haveToFind = (DataNode)haveToFind.pointer;
			}
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*@Override
	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
		this.root = (IndexNode)input.readObject();
		this.leftMost = (DataNode)input.readObject();
		
	}
	@Override
	public void writeExternal(ObjectOutput output) throws IOException {
		output.writeObject(this.root);
		output.writeObject(this.leftMost);
	}*/
	
/*	//note
	public void printTree(Node n) {
		if(n == null) {
			System.out.println("there is no IndexNode root yet");
			for(int i = 0; i<leftMost.numberOfKeys; i++) {
				System.out.print(leftMost.array.get(i).getFirst() + ", ");
			}
			System.out.println();
			System.out.println();
		}
		else {
			if(n.getClass() == leftMost.getClass()) {
				DataNode print = (DataNode)n;
				
				//note
				System.out.println("myParent key in printTree : " + n.myParent.array.get(0).getFirst());
				System.out.println();
				
				System.out.print("leafNode : ");
				for(int i = 0; i < print.numberOfKeys; i++) {
					System.out.print(print.array.get(i).getFirst() + ", ");
				}
				System.out.println();
				
				if(print.leftNode != null) {
					System.out.print("sibling : ");
					for(int i = 0; i < print.leftNode.numberOfKeys; i++) {
						System.out.print(print.leftNode.array.get(i).getFirst() + ", ");
					}
					System.out.println();
				}
				if(print.pointer != null) {
					System.out.print("right sibling : ");
					DataNode p = (DataNode)print.pointer;
					for(int i = 0; i < p.numberOfKeys; i++) {
						System.out.print(p.array.get(i).getFirst() + ", ");
					}
					System.out.println();
				}
				
			}else {
				IndexNode print = (IndexNode)n;
				
				//note
				if(n.myParent != null) {
					System.out.println("myParent.myParent key in printTree : " + n.myParent.array.get(0).getFirst());
					System.out.println();
				}
				
				for(int i = 0; i < print.numberOfKeys; i++) {
					printTree(print.array.get(i).getSecond());
					System.out.println("parent(" + i + ") key : " + print.array.get(i).getFirst());
					System.out.println();
				}
				
				System.out.println("pointer : ");
				printTree(print.pointer);
				System.out.println();
			}
		}
	}*/
	
	//insert/delete하고자 하는 key가 있는지 없는지 체크해줌. DataNode에서 찾음.
	public boolean search(int find, Node n) {
		if(n.getClass() == leftMost.getClass()) {
			DataNode child = (DataNode)n;
			for(int i = 0; i < child.numberOfKeys; i++) {
				if(find == child.array.get(i).getFirst()) {
					return true;
				}
			}
			return false;
		}
		else {
			boolean what = false;
			IndexNode parent = (IndexNode)n;
			int i = 0;
			for(; i < n.numberOfKeys; i++) {
				if(find == parent.array.get(i).getFirst()) { return true; }
				else if(find < parent.array.get(i).getFirst()) {
					what = search(find, parent.array.get(i).getSecond());
					break;
				}
			}
			if(i == n.numberOfKeys) what = search(find, parent.pointer);
			
			return what;	
		}
	}
		
	//insert/delete하고자 하는 key가 들어가야 할/있는 dataNode return하는 함수.
	public DataNode findNode(Node n, int add) {
		if(n.getClass() == leftMost.getClass()) {
			return (DataNode)n;
		}
		else {
			DataNode found = null;
			IndexNode parent = (IndexNode)n;
			
			int i = 0;
			for(; i < parent.numberOfKeys; i++) {
				if(add < parent.array.get(i).getFirst()) {
					found = findNode(parent.array.get(i).getSecond(), add);
					break;
				}
			}
			if(i == parent.numberOfKeys) found = findNode(parent.pointer, add);
			return found;
		}
	}
	
	public void insert(int key, int value) {
		if(leftMost == null) {
			System.out.println("tree error");
			return;
		}
		
		//중복인지 아닌지 체크해주기
		if(root == null) {
			if(search(key, leftMost)) {
				System.out.println(key + " key already exists");
				return;
			}
		}else {
			if(search(key, root)) {
				System.out.println(key + " key already exists");
				return;
			}
		}
		
		//search 해서 insert할 node 찾아주기 by findNode()
		if(root == null) {
			leftMost.insert(this, key, value);		
		}else {
			DataNode haveToInsert = findNode(root, key);
			haveToInsert.insert(this, key, value);
		}
		
	//	System.out.println(key + " insert complete");
	}
	
	public void delete(int key) {
		if(leftMost == null) {
			System.out.println("tree error");
			return;
		}
		
		//delete할 key가 있는 DataNode 찾기
		if(root == null) {
			//지우고자 하는 key가 있는지 체크
			if(search(key, leftMost)) leftMost.delete(this, key);
			else {
				System.out.println("There are no key to delete in tree");
				return;
			}
		}
		else {
			//지우고자 하는 key가 있는지 체크
			if(search(key, root)) {
				DataNode haveToDelete = findNode(root, key);
				haveToDelete.delete(this, key);
			}
			else {
				System.out.println("There are no key to delete in tree");
				return;
			}
						
		}		
		System.out.println(key + " delete complete");
	}
	
	
	public void singleSearch(int find) {
		if(leftMost == null) {
			System.out.println("tree error");
			return;
		}
		
		if(root == null) { //root가 dataNode일 때
			int i = 0;
			for(; i<leftMost.numberOfKeys; i++) {
				if(find == leftMost.array.get(i).getFirst()) {
					System.out.println(leftMost.array.get(i).getSecond());
					break;
				}
			}
			if(i == leftMost.numberOfKeys) System.out.println("NOT FOUND");
		}else {
			Node travel = root;
			boolean debug = false;
			while(travel.getClass() != leftMost.getClass()) {
				debug = false;
				//node 안에 있는 key 모두 출력하기
				IndexNode newTravel = (IndexNode)travel;
				
				for(int i = 0; i<newTravel.numberOfKeys; i++) {
					System.out.print(newTravel.array.get(i).getFirst());
					if(i+1 != newTravel.numberOfKeys) System.out.print(",");
					
					if(find < newTravel.array.get(i).getFirst() && !debug) {
						travel = newTravel.array.get(i).getSecond();
						debug = true;
					}
				}
				System.out.println();
				
				if(!debug) travel = newTravel.pointer;
			}
			//leafNode까지 찾아왔을 때	
			DataNode leaf = (DataNode)travel;
			int j = 0;
			for(; j<leaf.numberOfKeys; j++) {
				if(find == leaf.array.get(j).getFirst()) {
					System.out.println(leaf.array.get(j).getSecond());
					break;
				}
			}
			if(j == leaf.numberOfKeys) System.out.println("NOT FOUND");
		}
	}
	
	public void rangeSearch(int start, int end) {
		if(leftMost == null) {
			System.out.println("tree error");
			return;
		}
		if(start > end) {
			System.out.println("rangeSearch() input error");
			return;
		}
		
		DataNode haveToFind = leftMost;
		while(haveToFind != null) {
			boolean debug = false;
			int i = 0;
			for(; i < haveToFind.numberOfKeys; i++) {
				if(start <= haveToFind.array.get(i).getFirst()) debug = true;
				if(end < haveToFind.array.get(i).getFirst()) return;
				if(debug) {
					System.out.println(haveToFind.array.get(i).getFirst() + "," + haveToFind.array.get(i).getSecond());
				}
			}
			if(i == haveToFind.numberOfKeys) haveToFind = (DataNode)haveToFind.pointer;
		}
	}
	
}

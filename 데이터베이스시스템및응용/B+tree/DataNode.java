import java.util.ArrayList;
//import java.util.Collections;

//implements Externalizable
public class DataNode extends Node{
	ArrayList<TwoTypePair<Integer, Integer>> array;
	
	public DataNode(int degree) {
		super(degree);
		array = new ArrayList<TwoTypePair<Integer, Integer>>();
	}
	

	/*@Override
	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
		this.array = (ArrayList<TwoTypePair<Integer, Integer>>)input.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput output) throws IOException {
		output.writeObject(array);
	}*/


	public void insert(BpTree bp, int key, int value) {
		//key�� �� index ã���ֱ�
		int j = 0;
		for(; j<numberOfKeys; j++) {
			if(key < array.get(j).getFirst()) {
				break;
			}
		}
		TwoTypePair<Integer, Integer> newOne = new TwoTypePair<Integer, Integer>(key, value);
		array.add(j, newOne);
		numberOfKeys++;
		
		//redistribute
		if(isOverhead()) {
			DataNode d = new DataNode(degree);
			
			//���� ������ ���ο� dataNode�� �ű�
			for(int i = degree/2; i < degree; i++) {
				TwoTypePair<Integer, Integer> p = array.get(i);
				d.array.add(p);
				d.numberOfKeys++;
			}
			int fix = degree/2;
			for(int i = degree/2; i < degree; i++) {
				array.remove(fix);
				numberOfKeys--;
			}
			if(pointer != null) { 
				d.pointer = pointer;
				if(pointer.leftNode != null) {
					pointer.leftNode = d;
				}
			}
			pointer = d; //next DataNode pointer
			d.leftNode = this;
		//	if(leftNode == null) bp.setLeftMost(this);
			
			
			if(bp.getRoot() == null) {
				//parent���� ���ο� dataNode�� �ּ� Ű�� ����
				IndexNode parent = new IndexNode(degree);
				int parentKey = d.array.get(0).getFirst();
				
				TwoTypePair<Integer, Node> addPair = new TwoTypePair<Integer, Node>(parentKey, this);
				parent.array.add(addPair);
				parent.numberOfKeys++;
				parent.pointer = d; //d : rightNode
				
				this.myParent = parent;
				d.myParent = parent;
				
				bp.setRoot(parent);
			}
			else {
				int parentKey = d.array.get(0).getFirst();
				
				//parent���� parentKey�� �� ��� ã��(sort ���ֱ� ����)
				int i = 0;
				for(; i<myParent.numberOfKeys; i++) {
					if(parentKey < myParent.array.get(i).getFirst()) {
						break;
					}
				}
				TwoTypePair<Integer, Node> addPair = new TwoTypePair<Integer, Node>(parentKey, this);
				myParent.array.add(i,addPair);
				myParent.numberOfKeys++;
				d.myParent = myParent;
				
				//pointer �缳��
				DataNode repointing = (DataNode)myParent.array.get(i).getSecond().pointer;
				//parent���� ���� ���� pair�� �� ������ ���� ��
				if(i+1 == myParent.numberOfKeys) {
					myParent.pointer = d;
				}
				//i+1 > myParent.numberOfKeys�� ���� ����.
				else {
					for(i = i+1;i<myParent.numberOfKeys;i++) {
						myParent.array.get(i).setSecond(repointing);
						repointing = (DataNode)myParent.array.get(i).getSecond().pointer;	
					}
					myParent.pointer = repointing;
				}
				//�θ� �� á�ٸ� �θ� ���� �ɰ�.
				//�ɰ����� �ʴ� �θ� �߰��� ������ �̸� �ݺ���.
				if(myParent.isOverhead()) {
					myParent.parentPropagate(bp);
				}
				
				//root ������Ʈ���ֱ�
				IndexNode update = this.myParent;
				while(update.myParent != null) {
					update = update.myParent;
				}
				bp.setRoot(update);	
			}
		}

		/*//note
		System.out.println(">>>>>>>>> ���� key : " + key);
		bp.printTree(bp.getRoot());*/
	}

	public void delete(BpTree bp, int key) {
		boolean debug = false;
		int state = array.get(0).getFirst();
		//delete
		for(int i = 0; i < numberOfKeys; i++) {
			if(key == array.get(i).getFirst()) {
				array.remove(i);
				numberOfKeys--;
				break;
			}
		}
		
		if(myParent == null && numberOfKeys == 0) bp.setLeftMost(null);
		
		int overhead = -1;
		if(degree == 2) overhead = 1;
		else if(degree % 2 == 0) overhead = degree/2-1;
		else overhead = degree/2;
		
		//������ dataNode�� array���� numberOfKeys�� degree/2 < �̸�  parent ����
		if(numberOfKeys < (degree-1)/2 && myParent != null) {
			//���� dataNode�� ���� �����ʿ� ���� ��
			if(state >= myParent.array.get(myParent.numberOfKeys-1).getFirst()) {
				//redistribution
				if(leftNode.numberOfKeys > (degree-1)/2) {
					TwoTypePair<Integer, Integer> move = leftNode.array.get(leftNode.numberOfKeys-1);
					array.add(0, move);
					numberOfKeys++;
					leftNode.array.remove(leftNode.numberOfKeys-1);
					leftNode.numberOfKeys--;
					
					myParent.array.get(myParent.numberOfKeys-1).setFirst(move.getFirst());
				}
				//merge
				else if(leftNode.numberOfKeys == (degree-1)/2) {
					for(int k = 0; k < numberOfKeys; k++) {
						TwoTypePair<Integer, Integer> move = array.get(k);
						leftNode.array.add(move);
						leftNode.numberOfKeys++;
					}
					if(pointer != null) {
						leftNode.pointer = pointer;
						pointer.leftNode = leftNode;
					}
					else {
						leftNode.pointer = null;
					}
					//������ dataNode�� parent�� ���� key�� ����
					int find = myParent.array.get(myParent.numberOfKeys-1).getFirst();
					myParent.pointer = leftNode;
					myParent.array.remove(myParent.numberOfKeys-1);
					myParent.numberOfKeys--;
					
					//parent�� key���� üũ�ؼ� overhead �̸��̸� ������.
					if(myParent.numberOfKeys < overhead) {
						myParent.ifMerge(bp, find);
						debug = true;
					}
					
				//	if(leftNode.leftNode == null) bp.setLeftMost(this);
					
				}
			}
			//���� dataNode�� ���� ���ʿ� ���� ��
			else if(state < myParent.array.get(0).getFirst()) {
				DataNode newPointer = (DataNode)pointer;
				
				//redistribution
				if(pointer.numberOfKeys > (degree-1)/2) {
					TwoTypePair<Integer, Integer> move = newPointer.array.get(0);
					array.add(move);
					numberOfKeys++;
					newPointer.array.remove(0);
					newPointer.numberOfKeys--;
					
					//pointer ������Ʈ
					pointer = newPointer;
					
					myParent.array.get(0).setFirst(newPointer.array.get(0).getFirst());
					
				}
				//merge
				else if(pointer.numberOfKeys == (degree-1)/2){
					for(int k = numberOfKeys-1; k >= 0; k--) {
						TwoTypePair<Integer, Integer> move = array.get(k);
						newPointer.array.add(0, move);
						newPointer.numberOfKeys++;
					}
					if(leftNode != null) {
						newPointer.leftNode = leftNode;
						leftNode.pointer = newPointer;
					}
					else {
						newPointer.leftNode = null;
					}
					//������ dataNode�� parent�� ������ key�� ����
					int find = myParent.array.get(0).getFirst();
					myParent.array.remove(0);
					myParent.numberOfKeys--;
					
					//parent�� key���� üũ�ؼ� overhead�̸��̸� ������. root�� ���� �Լ� ������ ó���ϱ�!
					if(myParent.numberOfKeys < overhead) {
						myParent.ifMerge(bp, find);
						debug = true;
					}
					
					//bp.LeftMost ������Ʈ
			//		bp.setLeftMost(newPointer);
				}
			}
			else {
				int where = 0;
				for(; where < myParent.numberOfKeys; where++) {
					if(state < myParent.array.get(where).getFirst()) {
						break;
					}
				}
				DataNode newPointer = (DataNode)pointer; //������ node�� �̿��� �� ����ϱ� ����
				
				//���� node�� redistribution
				if(leftNode.numberOfKeys > (degree-1)/2) {
					TwoTypePair<Integer, Integer> move = leftNode.array.get(leftNode.numberOfKeys-1);
					array.add(0, move);
					numberOfKeys++;
					leftNode.array.remove(leftNode.numberOfKeys-1);
					leftNode.numberOfKeys--;
					
					myParent.array.get(where-1).setFirst(move.getFirst());
				}
				//������ node�� redistribution
				else if(pointer.numberOfKeys > (degree-1)/2) {
					TwoTypePair<Integer, Integer> move = newPointer.array.get(0);
					array.add(move);
					numberOfKeys++;
					newPointer.array.remove(0);
					newPointer.numberOfKeys--;
					pointer = newPointer;
					
					myParent.array.get(where).setFirst(newPointer.array.get(0).getFirst());
				}
				//���� node�� merge
				else if(leftNode.numberOfKeys == (degree-1)/2) {
					for(int k = 0; k < numberOfKeys; k++) {
						TwoTypePair<Integer, Integer> move = array.get(k);
						leftNode.array.add(move);
						leftNode.numberOfKeys++;
					}
					if(pointer != null) {
						leftNode.pointer = pointer;
						pointer.leftNode = leftNode;
					}
					else {
						leftNode.pointer = null;
					}
					myParent.array.get(where).setSecond(leftNode);
					
					//������ dataNode�� parent�� ���� key�� ����
					int find = myParent.array.get(where-1).getFirst();
					myParent.array.remove(where-1);
					myParent.numberOfKeys--;
					
					//parent�� key���� üũ�ؼ� overhead�̸��̸� ������.
					if(myParent.numberOfKeys < overhead) {
						myParent.ifMerge(bp, find);
						debug = true;
					}
				}
			}
		}
		
		if(debug) {
			/*//note
			System.out.println(">>>>>>>>> delete�� key : " + key);
			bp.printTree(bp.getRoot());*/
			
			return;
		}
		else {
			//root ������Ʈ���ֱ�
			IndexNode update = this.myParent;
			while(update.myParent != null) {
				update = update.myParent;
			}
			bp.setRoot(update);
		}
		
		
		/*//note
		System.out.println(">>>>>>>>> delete�� key : " + key);
		bp.printTree(bp.getRoot());*/
	}
}

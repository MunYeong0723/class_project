import java.util.ArrayList;

//implements Externalizable
public class IndexNode extends Node{
	ArrayList<TwoTypePair<Integer, Node>> array;
	
	public IndexNode(int degree) {
		super(degree);
		array = new ArrayList<TwoTypePair<Integer, Node>>();
	}
	
	/*@Override
	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
		this.array = (ArrayList<TwoTypePair<Integer, Node>>)input.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput output) throws IOException {
		output.writeObject(array);
	}*/
	
	public void parentPropagate(BpTree bp) {
		IndexNode newParent = new IndexNode(degree);	
		//indexNode를 2개로 쪼갬
		for(int k = degree/2; k < degree; k++) {
			TwoTypePair<Integer, Node> p = this.array.get(k);
			newParent.array.add(p);
			newParent.numberOfKeys++;
			if(k != degree/2) {
				this.array.get(k).getSecond().myParent = newParent;
			}
		}
		int fix = degree/2;
		for(int k = degree/2; k < degree; k++) {
			this.array.remove(fix);
			this.numberOfKeys--;
		}
		newParent.pointer = this.pointer;
		newParent.pointer.myParent = newParent;
		
		//indexNode parent가 myParent를 가지고 있지 않다면 root를 새로 바꿔줘야함.
		if(myParent == null) {
			IndexNode newRoot = new IndexNode(degree);
			TwoTypePair<Integer, Node> pair = newParent.array.get(0);
			newRoot.array.add(pair);
			newRoot.numberOfKeys++;
			newParent.array.remove(0);
			newParent.numberOfKeys--;
			
			newParent.myParent = newRoot;
			this.myParent = newRoot;
			
			//pointer 설정해주기
			this.pointer = newRoot.array.get(0).getSecond();
			newRoot.array.get(0).setSecond(this);
			newRoot.pointer = (IndexNode)newParent;
			
			bp.setRoot(newRoot);
		}
		else {
			//myParent에 add
			TwoTypePair<Integer, Node> pair = newParent.array.get(0);
			newParent.array.remove(0);
			newParent.numberOfKeys--;
			
			//myParent에 들어갈 곳 찾기(for sort)
			int parentKey = pair.getFirst();
			int j = 0;
			for(; j<myParent.numberOfKeys; j++) {
				if(parentKey < myParent.array.get(j).getFirst()) break;
			}
			myParent.array.add(j, pair);
			myParent.numberOfKeys++;
			
			//pointer 설정해주기
			//쪼갠 IndexNode들 pointer 재설정해주기
			this.pointer = pair.getSecond();
			newParent.myParent = myParent;
			
			//root에서 새로 넣은 pair가 맨 끝으로 들어갔을 때
			if(j+1 == myParent.numberOfKeys) {
				myParent.array.get(j).setSecond(myParent.pointer);
				myParent.pointer = newParent;
			}
			//j+1 > myParent.numberOfKeys일 수는 없음.
			else {
				myParent.array.get(j).setSecond(myParent.array.get(j+1).getSecond());
				myParent.array.get(j+1).setSecond(newParent);
			}
			
			//myParent가 꽉차면 다시 parentPropagate() 해줌
			if(myParent.isOverhead()) {
				myParent.parentPropagate(bp);
			}
			else {
				//root 업데이트해주기
				IndexNode update = this.myParent;
				while(update.myParent != null) {
					update = update.myParent;
				}
				bp.setRoot(update);
			}
		}
	}
	
	//find : 현재 indexNode에서 지워진 key
	public void ifMerge(BpTree bp, int find) {
		boolean debug = false;
		int overhead = -1;
		if(degree == 2) overhead = 1;
		else if(degree % 2 == 0) overhead = degree/2-1;
		else overhead = degree/2;
		
		if(myParent == null) {
			if(numberOfKeys == 0) {
				DataNode check = bp.getLeftMost();
				pointer.myParent = null;
				
				//root가 바뀜.
				//dataNode로
				if(pointer.getClass() == check.getClass()) {
					bp.setLeftMost((DataNode)pointer);
					bp.setRoot(null);
				}
				//이 경우 complete 1, 
				//다른 indexNode로
				else {
					bp.setRoot((IndexNode)pointer);
				}
			}
			
			return;
		}
		else {
			//현재 indexNode가 제일 오른쪽에 있을 때
			if(find >= myParent.array.get(myParent.numberOfKeys-1).getFirst()) {
				IndexNode leftSibling = (IndexNode)myParent.array.get(myParent.numberOfKeys-1).getSecond();
				
				//redistribution
				if(leftSibling.numberOfKeys > overhead) {
					TwoTypePair<Integer, Node> move = new TwoTypePair<Integer, Node>(myParent.array.get(myParent.numberOfKeys-1).getFirst(), leftSibling.pointer);
					array.add(0, move);
					numberOfKeys++;
					
					myParent.array.get(myParent.numberOfKeys-1).setFirst(leftSibling.array.get(leftSibling.numberOfKeys-1).getFirst());
					leftSibling.pointer = leftSibling.array.get(leftSibling.numberOfKeys-1).getSecond();
					
					leftSibling.array.remove(leftSibling.numberOfKeys-1);
					leftSibling.numberOfKeys--;
					
					//새로 들어온 pair의 parent 바꿔주기
					array.get(0).getSecond().myParent = this;
					
					//업데이트
					myParent.array.get(myParent.numberOfKeys-1).setSecond(leftSibling);
				}
				//merge
				else if(leftSibling.numberOfKeys == overhead) {
					int found = myParent.array.get(0).getFirst();
					
					//parent의 맨끝을 가져와서 leftSibling과  merge
					TwoTypePair<Integer, Node> newPair = new TwoTypePair<Integer, Node>(myParent.array.get(myParent.numberOfKeys-1).getFirst(), leftSibling.pointer);
					leftSibling.array.add(newPair);
					leftSibling.numberOfKeys++;
					
					//parent에서 제거
					myParent.array.remove(myParent.numberOfKeys-1);
					myParent.numberOfKeys--;
					
					for(int j = 0; j < numberOfKeys; j++) {
						TwoTypePair<Integer, Node> pair = array.get(j);
						pair.getSecond().myParent = leftSibling;
						leftSibling.array.add(pair);
						leftSibling.numberOfKeys++;
					}
					leftSibling.pointer = pointer;
					leftSibling.pointer.myParent = leftSibling;
					myParent.pointer = leftSibling;
					
					//parent의 key개수 체크해서 overhead 미만이면 고쳐줌.
					if(myParent.numberOfKeys < overhead) {
						myParent.ifMerge(bp, found);
						debug = true;
					}
					
				}
			}
			//현재 indexNode가 제일 왼쪽에 있을 때
			else if(find < myParent.array.get(0).getFirst()) {
				IndexNode rightSibling = null;

				if(myParent.numberOfKeys == 1) {
					rightSibling = (IndexNode)myParent.pointer;
				}
				else {
					rightSibling = (IndexNode)myParent.array.get(1).getSecond();
				}
				
				
				//redistribution
				if(rightSibling.numberOfKeys > overhead) {
					TwoTypePair<Integer, Node> move = new TwoTypePair<Integer, Node>(myParent.array.get(0).getFirst(), rightSibling.array.get(0).getSecond());
					array.add(move);
					numberOfKeys++;
					
					myParent.array.get(0).setFirst(rightSibling.array.get(0).getFirst());
					
					rightSibling.array.remove(0);
					rightSibling.numberOfKeys--;
					
					//새로 들어온 pair의 parent 바꿔주기
					array.get(numberOfKeys-1).getSecond().myParent = this;
					//pointer바꿔주기
					Node change = pointer;
					pointer = array.get(numberOfKeys-1).getSecond();
					array.get(numberOfKeys-1).setSecond(change);
				}
				//merge
				else if(rightSibling.numberOfKeys == overhead){
					int found = myParent.array.get(0).getFirst();
					TwoTypePair<Integer, Node> newPair = new TwoTypePair<Integer, Node>(myParent.array.get(0).getFirst(), pointer);
					
					
					rightSibling.array.add(0, newPair);
					rightSibling.numberOfKeys++;
					rightSibling.array.get(0).getSecond().myParent = rightSibling;
					
					//parent에서 제거
					myParent.array.remove(0);
					myParent.numberOfKeys--;
					
					for(int j = numberOfKeys-1; j >= 0; j--) {
						TwoTypePair<Integer, Node> pair = array.get(j);
						pair.getSecond().myParent = rightSibling;
						rightSibling.array.add(0, pair);
						rightSibling.numberOfKeys++;
					}
					
					if(myParent.numberOfKeys < overhead) {
						myParent.ifMerge(bp, found);
						debug = true;
					}
				}
			}
			else {
				IndexNode left = null;
				IndexNode right = null;
				
				int where = 0;
				for(; where < myParent.numberOfKeys; where++) {
					if(find < myParent.array.get(where).getFirst()) {
						break;
					}
				}
				left = (IndexNode)myParent.array.get(where-1).getSecond();
				if(where+1 == myParent.numberOfKeys) {
					right = (IndexNode)myParent.pointer;
				}
				else{
					right = (IndexNode)myParent.array.get(where+1).getSecond();
				}
				
				//왼쪽 node로 redistribution(왼쪽 node에 add)
				if(left.numberOfKeys > overhead) {
					TwoTypePair<Integer, Node> move = new TwoTypePair<Integer, Node>(myParent.array.get(where-1).getFirst(), left.pointer);
					array.add(0, move);
					numberOfKeys++;
					
					myParent.array.get(where-1).setFirst(left.array.get(left.numberOfKeys-1).getFirst());
					left.pointer = left.array.get(left.numberOfKeys-1).getSecond();
					
					left.array.remove(left.numberOfKeys-1);
					left.numberOfKeys--;
					
					//새로 들어온 pair의 parent 바꿔주기
					array.get(0).getSecond().myParent = this;
				}
				//오른쪽 node로 redistribution(오른쪽 node에서 가져옴)
				else if(right.numberOfKeys > overhead) {
					TwoTypePair<Integer, Node> move = new TwoTypePair<Integer, Node>(myParent.array.get(where).getFirst(), right.array.get(0).getSecond());
					array.add(move);
					numberOfKeys++;
					
					myParent.array.get(where).setFirst(right.array.get(0).getFirst());
					
					right.array.remove(0);
					right.numberOfKeys--;
					
					
					//새로 들어온 pair의 parent 바꿔주기
					array.get(numberOfKeys-1).getSecond().myParent = this;
					//pointer바꿔주기
					Node change = pointer;
					pointer = array.get(numberOfKeys-1).getSecond();
					array.get(numberOfKeys-1).setSecond(change);
				}
				//왼쪽 node와 merge
				else if(left.numberOfKeys == overhead) {
					int found = myParent.array.get(0).getFirst();
					
					//parent를 leftSibling과  merge
					TwoTypePair<Integer, Node> newPair = new TwoTypePair<Integer, Node>(myParent.array.get(where-1).getFirst(), left.pointer);
					left.array.add(newPair);
					left.numberOfKeys++;
					
					//myParent의 pointer 재설정
					myParent.array.get(where).setSecond(left);
					
					//parent에서 제거
					myParent.array.remove(where-1);
					myParent.numberOfKeys--;
					
					for(int j = 0; j < numberOfKeys; j++) {
						TwoTypePair<Integer, Node> pair = array.get(j);
						pair.getSecond().myParent = left;
						left.array.add(pair);
						left.numberOfKeys++;
					}
					left.pointer = pointer;
					left.pointer.myParent = left;
					
					//업데이트
					myParent.array.get(where-1).setSecond(left);
					
					//parent의 key개수 체크해서 overhead미만이면 고쳐줌.
					if(myParent.numberOfKeys < overhead) {
						myParent.ifMerge(bp, found);
						debug = true;
					}
				}
			}
			
			if(debug) {
				return;
			}
			else {
				//root 업데이트해주기
				IndexNode update = this.myParent;
				while(update.myParent != null) {
					update = update.myParent;
				}
				bp.setRoot(update);
			}
		}
		
	}
}

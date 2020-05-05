
//implements Externalizable
public class Node {
	protected int degree, numberOfKeys;
	protected Node pointer; //IndexNode->rightMost, DataNode->sibling
	protected IndexNode myParent;  //node의 parent를 담는 변수
	protected DataNode leftNode; //for delete
	
	public Node(int degree) {
		this.degree = degree;
		this.numberOfKeys = 0;
		this.pointer = null;
		this.myParent = null;
		this.leftNode = null;
	}
	
	/*@Override
	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
		this.degree = (int)input.readObject();
		this.numberOfKeys = (int)input.readObject();
		this.pointer = (Node)input.readObject();
		this.myParent = (IndexNode)input.readObject();
		this.leftNode = (DataNode)input.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput output) throws IOException {
		output.writeObject(degree);
		output.writeObject(numberOfKeys);
		output.writeObject(pointer);
		output.writeObject(myParent);
		output.writeObject(leftNode);
	}*/
	
	public boolean isOverhead() {
		return numberOfKeys == degree;
	}
}

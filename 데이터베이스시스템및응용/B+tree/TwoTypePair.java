
//implements Externalizable
public class TwoTypePair<T1, T2>{
	private T1 first;
	private T2 second;
	
	public TwoTypePair(){
		first = null;
		second = null;
	}
	
	public TwoTypePair(T1 firstItem, T2 secondItem) {
		first = firstItem;
		second = secondItem;
	}
	
	public void setFirst(T1 newFirst) {
		first = newFirst;
	}
	
	public void setSecond(T2 newSecond) {
		second = newSecond;
	}
	
	public T1 getFirst() {
		return first;
	}
	
	public T2 getSecond() {
		return second;
	}
	
	/*@Override
	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
		this.first = (T1)input.readObject();
		this.second = (T2)input.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput output) throws IOException {
		output.writeObject(first);
		output.writeObject(second);
	}*/
}

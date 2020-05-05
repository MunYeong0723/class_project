import java.util.StringTokenizer;

public class KeyValue {
	private String key, value;
	
	KeyValue(String line){
		StringTokenizer st = new StringTokenizer(line, "=");
		
		this.key = st.nextToken();
		this.value = st.nextToken();
	}
	KeyValue(String key, String value){
		this.key = key;
		this.value = value;
	}
	
	public String getKey() {
		return key;
	}
	public String getValue() {
		return value;
	}
}

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

public class LetterTransformator {
	
	public static void main(String[] args) {
		Scanner fileInProperties = null;
		Scanner fileIntemplate = null;
		PrintWriter ps = null;
		try {
			fileInProperties = new Scanner(new FileInputStream("properties.txt"));			
			fileIntemplate = new Scanner(new FileInputStream("template_file.txt"));			
			ps = new PrintWriter("output_file.txt");
			
			ArrayList<KeyValue> al = new ArrayList<>();
			
			while(fileInProperties.hasNextLine()) {
				KeyValue kv = new KeyValue(fileInProperties.nextLine());
				al.add(kv);
			}
			
			while(fileIntemplate.hasNextLine()) {
				String line = fileIntemplate.nextLine();
				int i = 0;
				while(i < al.size()) {
					line = line.replace("{" + al.get(i).getKey() + "}", al.get(i).getValue());
					i++;
				}		
				ps.println(line);
			}
			
			fileInProperties.close();
			fileIntemplate.close();
			ps.close();
		}
		catch(FileNotFoundException e) {
			System.out.println("File not found.");
			System.exit(0);
		}
	}
}

package utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;

public class Download_DeleteEmptyFiles {

    
    //also - delete empty folders - TODO!

    public static void main(String[] args) throws IOException, ParseException {
	G.initialize();
	deleteEmptySubfiles(G.dayDataDir);
    }

    private static void deleteEmptySubfiles(File root) throws IOException {

	for (File file : root.listFiles()) {
	    if (file.isDirectory()) {
		System.out.println(file.getCanonicalPath());
		deleteEmptySubfiles(file);
	    }

	    if (file.isFile()) {
		String line;
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
		    line = br.readLine();
		}
		if (line == null || line.isEmpty()){
		    file.delete();
		    System.out.println("deleted " + file.getCanonicalPath());
		}
	    }
	}
    }

}

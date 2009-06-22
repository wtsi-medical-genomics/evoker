import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class QCFilterData {

    Vector<String> toExclude;

    QCFilterData(String qcFilename) throws IOException{

        this.toExclude = new Vector<String>();

        BufferedReader qcReader = new BufferedReader(new FileReader(qcFilename));
        String currentLine;
        StringTokenizer st;
        while ((currentLine = qcReader.readLine()) != null){
            st = new StringTokenizer(currentLine);
            String name = st.nextToken();
            toExclude.add(name);	
        	Genoplot.ld.log("Exclude: " + name);
        }
        qcReader.close();
    }
    
    public boolean isExcluded (String sample) {
    	return toExclude.contains(sample);
    }
}
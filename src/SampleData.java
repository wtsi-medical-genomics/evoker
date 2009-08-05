import java.util.Vector;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SampleData {

    Vector<String> inds;

    SampleData(String famFilename) throws IOException{

        this.inds = new Vector<String>();

        BufferedReader famReader = new BufferedReader(new FileReader(famFilename));
        String currentLine;
        String[] bits;
        while ((currentLine = famReader.readLine()) != null){
            bits = currentLine.split("\\s");
            String sample = bits[1];
            inds.add(sample);
        }
        famReader.close();


    }

    public String getInd(int i){
        return inds.get(i);
    }

    public int getNumInds(){
        return inds.size();
    }

}

import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SampleData {

    Vector<String> inds;

    SampleData(String famFilename){

        this.inds = new Vector<String>();

        try{
            BufferedReader famReader = new BufferedReader(new FileReader(famFilename));
            String currentLine;
            StringTokenizer st;
            while ((currentLine = famReader.readLine()) != null){
                st = new StringTokenizer(currentLine);
                while (st.hasMoreTokens()){
                    st.nextToken();
                    inds.add(st.nextToken());
                    //todo: put remaining data somewhere
                    break;
                }
            }
            famReader.close();

        }catch (IOException ioe){
            //todo: complain
        }
    }

    public String getInd(int i){
        return inds.get(i);
    }

    public int getNumInds(){
        return inds.size();
    }

}


public abstract class RemoteBinaryData extends BinaryData{

    DataClient dc;

    RemoteBinaryData(DataClient dc, SampleData sd, MarkerData md, String collection){
        super (sd,md,collection);
        this.dc = dc;
    }

    
}

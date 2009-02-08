
public abstract class RemoteBinaryData extends BinaryData{

    DataClient dc;

    RemoteBinaryData(DataClient dc, int numInds, MarkerData md, String collection){
        super (numInds,md,collection);
        this.dc = dc;
    }

    protected void checkFile(byte[] header){
        //TODO: how to be able to run this check without unnecessarily slowing down the system?
    }
    
}

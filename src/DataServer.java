import java.io.*;
import java.net.*;
import java.util.StringTokenizer;
import java.math.BigInteger;

public class DataServer {

    static final int PORT = 2718;
    private DataDirectory dir;

    public static void main(String[] args) throws IOException {
        DataServer ds = new DataServer();
        ds.listen();
    }
    public void listen(){
        boolean listening = true;
        ServerSocket msgSocket, dataSocket;

        try {
            msgSocket = new ServerSocket(PORT);

            System.out.println("Server Active and Listening on port " + PORT);
            while (listening)
                new DataServerMessageThread(msgSocket.accept(), this).start();

            msgSocket.close();
        } catch (IOException e) {
            System.err.println("Could not listen:\n" + e.getMessage());
            System.exit(-1);
        }
    }

    public String loadDir(String dirName){
        try{
            dir = new DataDirectory(dirName);
            return("LOADED");
        }catch (IOException ioe){
            return ioe.getMessage();
        }
    }

    public String fetch(String snp){
        return null;
    }

    class DataServerMessageThread extends Thread {
        private Socket socket = null;
        private DataServer ds;

        public DataServerMessageThread(Socket socket, DataServer ds) {
            super("DataServerMessageThread");
            this.socket = socket;
            this.ds = ds;
        }

        public void run() {

            try {
                BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                System.out.println("New Connection from: " + ((InetSocketAddress)socket.getRemoteSocketAddress()).getHostName());

                StringTokenizer st;
                String currentLine;
                while ((currentLine = in.readLine()) != null){
                    System.out.println("currentLine = " + currentLine);

                    //TODO: the reply should be a success/failure btye
                    //followed by 4 bytes which form an integer length of the rest of the message
                    //followed by <length> bytes forming the message

                    int number = 2789812;
                    byte[] byteArray = new byte[4];
                    byteArray[0] = (byte)((number >> 24) & 0xFF);
                    byteArray[1] = (byte)((number >> 16) & 0xFF);
                    byteArray[2] = (byte)((number >> 8) & 0xFF);
                    byteArray[3] = (byte)(number & 0xFF);
                    out.write(byteArray);
                    out.flush();
                    /*st = new StringTokenizer(currentLine);
                    String message = st.nextToken();
                    if (message.equals("LOADDIR")){
                        //System.out.println(st.nextToken());
                        //out.write("zazz".getBytes());
                        for (int i = 0; i < 200000; i++){
                            out.write(1);
                        }
                        out.write(-1);
                        out.flush();
                        System.out.println("heez");
                        //out.close();
                    }else if (message.equals("FETCH")){
                        System.out.println(st.nextToken());
                    } */
                }

                out.close();
                in.close();
                socket.close();
            } catch (SocketException se){
                System.out.println("Socket message: " + se.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

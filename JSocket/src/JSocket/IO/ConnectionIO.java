package JSocket.IO;

import JSocket.Exceptions.ConnectionCloseException;
import JSocket.Exceptions.UnknownOPCodeException;
import JSocket.Utility.DataFrameMetadata;
import JSocket.Utility.OPCode;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

public class ConnectionIO {
    private boolean encrypted;
    private Socket connection;
    private final Random random = new Random();
    private DataFrameInputStream inputStream;
    private DataFrameOutputStream outputStream;
    private boolean previousFinished = true;
    private int[] RSVCodes = new int[]{0,0,0};
    public ConnectionIO(Socket connection) throws IOException {
        this.connection = connection;
        this.encrypted = false;
        try {
            this.inputStream = new DataFrameInputStream(connection.getInputStream());
            this.outputStream = new DataFrameOutputStream(connection.getOutputStream());
        }
        catch (IOException e){
            this.connection.close();
            throw new ConnectException();
        }
    }
    public ConnectionIO(Socket connection,boolean encrypted) throws IOException {
        this(connection);
        this.encrypted = encrypted;
    }
    public byte[] readBytes(boolean multiframe) throws ConnectionCloseException, IOException {
        OPCode opCode = inputStream.getCurrentDataFrameMetadata().OPCode;
        if (opCode == OPCode.CONNECTION_CLOSE_FRAME) {
                this.connection.close();
                this.inputStream.close();
                this.outputStream.close();
            throw new ConnectionCloseException();
        }
        else if (opCode == OPCode.PING) {
            try {
                outputStream.pong(inputStream.readAllBytes());
                inputStream.readDataFrameMetadata();
            } catch (UnknownOPCodeException | IOException e) {
                throw new ConnectionCloseException();
            }
        }
        else{
            if(!multiframe) {
                return this.inputStream.readAllBytes();
            }
            else{
                byte[] array  = this.inputStream.readAllBytes();
                while(this.inputStream.getCurrentDataFrameMetadata().isFinished != 1){
                    array = concatByteArrays(array,this.inputStream.readAllBytes());
                }
                return array;
            }
        }
        return new byte[]{-1};
    }

    /**
     *
     * @param multiframe if false reads single DataFrame worth of data, when true reads data until DatsFrameMetadata.isFinished is true
     * @return Bytes from input stream in form of a String
     * @throws ConnectionCloseException
     * @throws IOException
     */
    public String readString(boolean multiframe) throws ConnectionCloseException, IOException {
        return String.valueOf(toCharArray(readBytes(multiframe)));
    }

    /**
     * Checks whether there are bytes to be read in the input stream
     * @return true if there are bytes to be read, false otherwise
     * @throws IOException
     * @throws ConnectionCloseException
     */
    public boolean canRead() throws IOException, ConnectionCloseException {
        if (this.inputStream.getCurrentDataFrameMetadata().OPCode == OPCode.CONNECTION_CLOSE_FRAME) {
            this.connection.close();
            this.inputStream.close();
            this.outputStream.close();
            throw new ConnectionCloseException();
        }
        return this.inputStream.available() != 0;
    }
    private char[] toCharArray(byte[] bytes) {
        char[] array = new char[bytes.length];
        for (int i=0;i<bytes.length;i++){
            array[i] = (char)(bytes[i]);
        }
        return array;
    }

    /**
     * Closes connection
     * @throws IOException
     */
    public void close() throws IOException {
        this.inputStream.close();
        this.outputStream.close();
        this.connection.close();
    }

    /**
     * Writes given bytes to the output stream as a DataFrame. Continues parameter indicates
     * whether the OPCode should be {@code OPCode.CONTINUATION_FRAME}
     * @param data
     * @param continued indicates whether this portion of data will be continued by the next portion of data from a given source
     */
    public void writeBytes(byte[] data,boolean continued) throws IOException {
        setMetadata(data.length,continued);
        this.outputStream.write(data);
    }
    public void writeString(String data) throws IOException {
        setMetadata(data.length(),false);
        DataFrameMetadata dfm = this.outputStream.getMetadata();
        dfm.OPCode = OPCode.TEXT_FRAME;
        this.outputStream.setMetadata(dfm);
        this.outputStream.write(data.getBytes(StandardCharsets.UTF_8));
    }
    private int[] getMask(){
        byte[] mask = new byte[4];
        random.nextBytes(mask);
        return toIntArray(mask);
    }
    private int[] toIntArray(byte[] array) {
        int[] arr = new int[array.length];
        for (int i=0;i<array.length;i++){
            arr[i] = (int)array[i];
        }
        return arr;
    }
    private void setMetadata(int dataLength,boolean continued){
        int FIN;
        OPCode opCode;
        if(previousFinished){
            opCode = OPCode.BINARY_FRAME;
        }
        else {
            opCode = OPCode.CONTINUATION_FRAME;
        }
        if(continued){
            FIN = 0;
            this.previousFinished = false;
        }
        else {
            FIN = 1;
            this.previousFinished = true;
        }
        if(!encrypted) {
            this.outputStream.setMetadata(new DataFrameMetadata(FIN,opCode,RSVCodes[0],RSVCodes[1],RSVCodes[2], dataLength));
        }
        else{
            this.outputStream.setMetadata(new DataFrameMetadata(FIN, opCode,RSVCodes[0],RSVCodes[1],RSVCodes[2], dataLength,getMask()));
        }
    }

    public void setRSVCodes(int[] RSVCodes) {
        this.RSVCodes = RSVCodes;
    }

    public int[] getRSVCodes() {
        return RSVCodes;
    }
    private byte[] concatByteArrays(byte[] array1, byte[] array2) {
        byte[] result = Arrays.copyOf(array1, array1.length + array2.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }
}

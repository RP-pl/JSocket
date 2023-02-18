package JSocket.IO;

import JSocket.Utility.DataFrameMetadata;
import JSocket.Utility.Length;
import JSocket.Utility.OPCode;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;

public class DataFrameOutputStream extends OutputStream {
    private final OutputStream outputStream;

    private DataFrameMetadata metadata;
    public DataFrameOutputStream(OutputStream output) {
        this.outputStream = output;
        //Default implementation
        this.metadata = new DataFrameMetadata();
        this.metadata.OPCode = OPCode.TEXT_FRAME;
        this.metadata.hasMask = 0;
        this.metadata.isFinished = 1;
    }

    private int[] toIntArray(byte[] array) {
        int[] arr = new int[array.length];
        for (int i=0;i<array.length;i++){
            arr[i] = (int)array[i];
        }
        return arr;
    }
    private byte[] toByteArray(int[] array){
        byte[] arr = new byte[array.length];
        for (int i=0;i<array.length;i++){
            arr[i] = (byte)array[i];
        }
        return arr;
    }

    /**
     *
     * @param b   the {@code byte} of data to be written as a single DataFrame.
     * @throws IOException
     */
    @Override
    public void write(int b) throws IOException {
       this.outputStream.write(assembleDataFrame(new byte[]{(byte) b}));
    }

    private byte[] assembleDataFrame(byte[] data) {
        int initialLength = data.length + 2;
        Length l = Length.BYTE;
        if(this.metadata.hasMask == 1){
            initialLength += 4;
        }
        if(data.length < 256*256 && 125< data.length){
            initialLength +=2;
            l = Length.BYTE_2;
        } else if (data.length > 256*256) {
            initialLength += 8;
            l = Length.BYTE_8;
        }
        int currentLength = 2;
        byte[] dataFrame = new byte[initialLength];
        //FIN|RSV1|RSV2|RSV3|OPCode
        dataFrame[0] = (byte) (this.metadata.isFinished << 7 | this.metadata.RSV1 << 6 | this.metadata.RSV2 << 5 | this.metadata.RSV3 << 4 | returnOPCode(this.metadata.OPCode));
        //MASK|PAYLOAD
        if(l == Length.BYTE){
            dataFrame[1] = (byte) (this.metadata.hasMask<<7 |  data.length);
        }
        else if(l == Length.BYTE_2) {
            dataFrame[1] = (byte) (this.metadata.hasMask << 7 | 126);
            int g = data.length;
            for (int i = 0; i < 2; i++) {
                dataFrame[1 + (2 - i)] = (byte) (g & 255);
                g >>= 8;
            }
            currentLength+=2;
        }
        else{
            dataFrame[1] = (byte) (this.metadata.hasMask << 7 | 127);
            long g = data.length;
            for (int i = 0; i < 8; i++) {
                dataFrame[1 + (8 - i)] = (byte) (g & 255);
                g >>= 8;
            }
            currentLength+=8;
        }
        System.arraycopy(data, 0, dataFrame, currentLength, data.length);
        return dataFrame;
    }

    @Override
    public void write(byte @NotNull [] b) throws IOException {
        this.outputStream.write(assembleDataFrame(b));
    }
    public void pong(byte[] dataFrameBytes) throws IOException {
        byte[] df = assembleDataFrame(dataFrameBytes);
        df[0] = (byte) (((df[0]|0b1111))&(returnOPCode(OPCode.PONG)));
        this.outputStream.write(df);
    }

    public void setMetadata(DataFrameMetadata metadata) {
        this.metadata = metadata;
    }

    public DataFrameMetadata getMetadata() {
        return metadata;
    }

    /**
     * Returns binary representation of a given OPCode.
     * WARNING: NON_CONTROL_FRAME and FURTHER_CONTROL_FRAME not implemented!!!
     * @param opCode OPCode of the DataFrame
     * @return Binary representation of a given OPCode
     */
    private byte returnOPCode(OPCode opCode){
        switch (opCode){
            case CONTINUATION_FRAME -> {
                return 0b0;
            }
            case TEXT_FRAME -> {
                return 0b1;
            }
            case BINARY_FRAME -> {
                return 0b10;
            }
            case CONNECTION_CLOSE_FRAME -> {
                return 0b1000;
            }
            case PING -> {
                return 0b1001;
            }
            case PONG -> {
                return 0b1010;
            }
        }
        return -1;
    }
}

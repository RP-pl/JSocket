package JSocket.Common.IO.Utility;

import JSocket.Common.IO.ConnectionIO;
import JSocket.Common.IO.DataFrameInputStream;
import JSocket.Common.IO.DataFrameOutputStream;
import JSocket.Common.IO.Utility.OPCode;

import java.math.BigInteger;

/**
 * This class is used to store the metadata of the data frame
 *
 * @see ConnectionIO
 * @see DataFrameOutputStream
 * @see DataFrameInputStream
 */
public class DataFrameMetadata {
    public int isFinished;
    public int RSV1;
    public int RSV2;
    public int RSV3;
    public JSocket.Common.IO.Utility.OPCode OPCode;
    public int hasMask;
    public int[] mask;
    public BigInteger payloadLength;

    public DataFrameMetadata() {
        this.payloadLength = BigInteger.ZERO;
    }

    public DataFrameMetadata(int isFinished, OPCode opCode, int payloadLength) {
        this.isFinished = isFinished;
        this.hasMask = 0;
        this.OPCode = opCode;
        this.payloadLength = new BigInteger(String.valueOf(payloadLength));
    }

    public DataFrameMetadata(int isFinished, OPCode opCode, int payloadLength, int[] mask) {
        this(isFinished, opCode, payloadLength);
        this.mask = mask;
        this.hasMask = 1;
    }

    public DataFrameMetadata(int isFinished, OPCode opCode, int RSV1, int RSV2, int RSV3, int payloadLength, int[] mask) {
        this(isFinished, opCode, payloadLength, mask);
        this.RSV1 = RSV1;
        this.RSV2 = RSV2;
        this.RSV3 = RSV3;
    }

    public DataFrameMetadata(int isFinished, OPCode opCode, int RSV1, int RSV2, int RSV3, int payloadLength) {
        this(isFinished, opCode, payloadLength);
        this.RSV1 = RSV1;
        this.RSV2 = RSV2;
        this.RSV3 = RSV3;
    }
}

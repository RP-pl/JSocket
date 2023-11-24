package JSocket.Common.IO;

import JSocket.Common.IO.Utility.DataFrameMetadata;
import JSocket.Common.Exceptions.UnknownOPCodeException;

import java.io.*;
import java.math.BigInteger;

public class DataFrameInputStream extends InputStream {
    private final InputStream inputStream;
    private long extensionDataLength;
    private int currentMaskPosition = 0;
    private File extensionData;
    private DataFrameMetadata currentDataFrameMetadata;

    public DataFrameInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
        this.currentDataFrameMetadata = new DataFrameMetadata();
        this.extensionDataLength = 0;
    }

    public DataFrameInputStream(InputStream inputStream, long extensionDataLength) {
        this(inputStream);
        this.extensionDataLength = extensionDataLength;
    }

    /**
     * Reads a single byte from DataFrame's payload. The value byte is returned as an int in the range 0 to 255. If no byte is available because the end of the stream has been reached, the value -1 is returned.
     *
     * @return A byte of payload
     * @throws IOException
     */
    @Override
    public int read() throws IOException {
        if (currentDataFrameMetadata.payloadLength.equals(BigInteger.ZERO)) {
            try {
                readDataFrameMetadata();
            } catch (UnknownOPCodeException e) {
                this.inputStream.close();
                throw new IOException(e);
            }
        }
        if (currentDataFrameMetadata.payloadLength.equals(BigInteger.ZERO)) {
            return -1;
        }
        this.currentDataFrameMetadata.payloadLength = this.currentDataFrameMetadata.payloadLength.subtract(BigInteger.ONE);
        if (currentDataFrameMetadata.hasMask == 0) {
            return this.inputStream.read();
        } else {
            //Performs a xor decryption
            int data = this.inputStream.read();
            if (data == -1) {
                return -1;
            }
            data ^= this.currentDataFrameMetadata.mask[currentMaskPosition];
            this.currentMaskPosition++;
            this.currentMaskPosition %= 4;
            return data;
        }
    }

    @Override
    public byte[] readNBytes(int len) throws IOException {
        byte[] bytes;
        if (this.currentDataFrameMetadata.OPCode == null || this.currentDataFrameMetadata.payloadLength.equals(BigInteger.ZERO)) {
            try {
                readDataFrameMetadata();
            } catch (UnknownOPCodeException e) {
                throw new RuntimeException(e);
            }
        }
        if (this.currentDataFrameMetadata.payloadLength.compareTo(new BigInteger(String.valueOf(len))) == 1) {
            int length = this.currentDataFrameMetadata.payloadLength.intValue();
            bytes = new byte[length];
            for (int i = 0; i < length; i++) {
                int value = this.read();
                bytes[i] = (byte) value;
            }
        } else {
            bytes = new byte[len];
            for (int i = 0; i < len; i++) {
                bytes[i] = (byte) this.read();
            }
        }
        return bytes;
    }


    /**
     * Reads whole remaining DataFrame's payload. This method is not intended for fetching large amounts of data
     * (especially not larger than Integer.MAX_VALUE)
     */
    @Override
    public byte[] readAllBytes() throws IOException {
        return this.readNBytes(this.currentDataFrameMetadata.payloadLength.intValue());
    }


    /**
     * Skips n bytes from the DataFrame's payload. If n is greater than payload length skips only to the next frame
     *
     * @return Returns number of bytes skipped
     */
    @Override
    public long skip(long n) throws IOException {
        long skipped;
        if (this.currentDataFrameMetadata.payloadLength.compareTo(new BigInteger(String.valueOf(n))) == 1) {
            skipped = this.inputStream.skip(this.currentDataFrameMetadata.payloadLength.longValue());
            this.currentDataFrameMetadata.payloadLength = BigInteger.ZERO;
        } else {
            skipped = this.inputStream.skip(n);
            this.currentDataFrameMetadata.payloadLength = this.currentDataFrameMetadata.payloadLength.subtract(new BigInteger(String.valueOf(n)));

        }
        return skipped;
    }

    @Override
    public int available() throws IOException {
        if (currentDataFrameMetadata.payloadLength.equals(BigInteger.ZERO) && this.inputStream.available() != 0) {
            try {
                readDataFrameMetadata();
            } catch (UnknownOPCodeException ignored) {
            }
        }
        return this.inputStream.available();
    }

    public void readDataFrameMetadata() throws IOException, UnknownOPCodeException {
        this.currentDataFrameMetadata = new DataFrameMetadata();
        this.currentMaskPosition = 0;
        int first8bits = this.inputStream.read();
        if (first8bits == -1) {
            return;
        }
        this.currentDataFrameMetadata.isFinished = getNthBit(first8bits, 8);
        this.currentDataFrameMetadata.RSV1 = getNthBit(first8bits, 7);
        this.currentDataFrameMetadata.RSV2 = getNthBit(first8bits, 6);
        this.currentDataFrameMetadata.RSV3 = getNthBit(first8bits, 5);
        getOPCode(first8bits);
        int maskAndPayload = this.inputStream.read();
        this.currentDataFrameMetadata.hasMask = getNthBit(maskAndPayload, 8);
        getPayloadLength(maskAndPayload);
        if (this.currentDataFrameMetadata.hasMask == 1) {
            this.currentDataFrameMetadata.mask = new int[4];
            for (int i = 0; i < 4; i++) {
                this.currentDataFrameMetadata.mask[i] = this.inputStream.read();
            }
        }
        this.extensionData = File.createTempFile("ext", "");
        FileOutputStream extData = null;
        try {
            extData = new FileOutputStream(this.extensionData);
            for (long i = 0; i < extensionDataLength; i++) {
                extData.write(this.inputStream.read());
                this.currentDataFrameMetadata.payloadLength = this.currentDataFrameMetadata.payloadLength.subtract(BigInteger.ONE);
            }
        } finally {
            assert extData != null;
            extData.close();
        }
    }

    private void getPayloadLength(int maskAndPayload) throws IOException {
        int payloadLength = maskAndPayload & (0b1111111);
        if (payloadLength < 126) {
            this.currentDataFrameMetadata.payloadLength = this.currentDataFrameMetadata.payloadLength.add(new BigInteger(String.valueOf(payloadLength)));
        } else if (payloadLength == 126) {
            int first = this.inputStream.read();
            int second = this.inputStream.read();
            this.currentDataFrameMetadata.payloadLength = new BigInteger(String.valueOf(first));
            this.currentDataFrameMetadata.payloadLength = this.currentDataFrameMetadata.payloadLength.shiftLeft(8);
            this.currentDataFrameMetadata.payloadLength = this.currentDataFrameMetadata.payloadLength.add(new BigInteger(String.valueOf(second)));
        } else if (payloadLength == 127) {
            int first = this.inputStream.read();
            this.currentDataFrameMetadata.payloadLength = new BigInteger(String.valueOf(first));
            for (int i = 0; i < 7; i++) {
                int second = this.inputStream.read();
                this.currentDataFrameMetadata.payloadLength = this.currentDataFrameMetadata.payloadLength.shiftLeft(8);
                this.currentDataFrameMetadata.payloadLength = this.currentDataFrameMetadata.payloadLength.add(new BigInteger(String.valueOf(second)));
            }
        }
    }

    private int getNthBit(int data, int bit) {
        return ((data & (1 << (bit - 1))) >> (bit - 1));
    }

    private void getOPCode(int data) throws UnknownOPCodeException {
        int OPCode = data & 15;
        switch (OPCode) {
            case 0:
                this.currentDataFrameMetadata.OPCode = JSocket.Common.IO.Utility.OPCode.CONTINUATION_FRAME;
                return;
            case 1:
                this.currentDataFrameMetadata.OPCode = JSocket.Common.IO.Utility.OPCode.TEXT_FRAME;
                return;
            case 2:
                this.currentDataFrameMetadata.OPCode = JSocket.Common.IO.Utility.OPCode.BINARY_FRAME;
                return;
            case 8:
                this.currentDataFrameMetadata.OPCode = JSocket.Common.IO.Utility.OPCode.CONNECTION_CLOSE_FRAME;
                return;
            case 9:
                this.currentDataFrameMetadata.OPCode = JSocket.Common.IO.Utility.OPCode.PING;
                return;
            case 10:
                this.currentDataFrameMetadata.OPCode = JSocket.Common.IO.Utility.OPCode.PONG;
                return;
        }
        if (OPCode < 8) {
            this.currentDataFrameMetadata.OPCode = JSocket.Common.IO.Utility.OPCode.NON_CONTROL_FRAME;
        } else if (OPCode > 10) {
            this.currentDataFrameMetadata.OPCode = JSocket.Common.IO.Utility.OPCode.FURTHER_CONTROL_FRAME;
        } else {
            throw new UnknownOPCodeException("Unknown OPCode passed by a client");
        }
    }

    /**
     * Closes input stream
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        this.inputStream.close();
        super.close();
    }

    /**
     * Returns extension data stream
     *
     * @return Stream of bytes of extension data if extensionDataLength is not zero and null if extensionDataLength is zero
     */
    public InputStream getExtensionDataStream() {
        try {
            return new FileInputStream(extensionData);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public DataFrameMetadata getCurrentDataFrameMetadata() {
        return currentDataFrameMetadata;
    }
}

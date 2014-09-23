package piechota.datacontrol;

/**
 * Created by Konrad on 2014-09-19.
 */
public class ValidateData implements IValidateData {

    /*INFORMATION TYPE*/
    public static final byte CONTACT    = 0;
    public static final byte NUMBER     = 1;
    public static final byte NAME       = 2;
    public static final byte MSG        = 3;
    public static final byte TEXT       = 4;
    /*INFORMATION TYPE*/

    private HandleTheData _handler;
    private byte[][] obj;

    public ValidateData(HandleTheData handler){
        _handler = handler;
    }

    @Override
    public void validateData(byte[] msg) {
        byte type = getType(msg);

        switch(type){
            case CONTACT:
                break;
            case NUMBER:
                break;
            case NAME:
                break;
            case MSG:
                break;
            case TEXT:
                break;
        }

    }

    @Override
    public void validateData(byte[][] msg) {

    }

    private byte getType(byte[] msg){
        return msg[0];
    }
    private long getLength(byte[] msg){
        byte[] length = new byte[4];
        length[0] = msg[1];
        length[1] = msg[2];
        length[2] = msg[3];
        length[3] = msg[4];

        return ByteConverter.bytesToLong(length);
    }

    private long getChecksum(byte[] msg){
        byte[] checksum = new byte[8];
        checksum[0] = msg[5];
        checksum[1] = msg[6];
        checksum[2] = msg[7];
        checksum[3] = msg[8];
        checksum[4] = msg[9];
        checksum[5] = msg[10];
        checksum[6] = msg[11];
        checksum[7] = msg[12];

        return ByteConverter.bytesToLong(checksum);
    }
}

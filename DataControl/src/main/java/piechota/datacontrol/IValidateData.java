package piechota.datacontrol;

/**
 * Created by Konrad on 2014-09-19.
 */
public interface IValidateData {

    public void validateData(byte[] msg);
    public void validateData(byte[][] msg);
}

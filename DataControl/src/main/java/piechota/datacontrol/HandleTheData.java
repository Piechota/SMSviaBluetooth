package piechota.datacontrol;

/**
 * Created by Konrad on 2014-09-19.
 */
public abstract class HandleTheData {
    private IValidateData _parent;

    public IValidateData getParent(){return _parent;}
    public void setParent(IValidateData parent){_parent = parent;}

    public abstract void handleTheData(byte[] msg);
}

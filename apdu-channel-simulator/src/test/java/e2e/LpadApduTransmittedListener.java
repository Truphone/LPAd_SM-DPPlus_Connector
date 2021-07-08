package e2e;


import com.truphone.lpa.ApduTransmittedListener;

public class LpadApduTransmittedListener implements ApduTransmittedListener {

    private boolean apduTransmitted;

    public LpadApduTransmittedListener() {
        this.apduTransmitted = false;
    }

    public void onApduTransmitted() {
        this.apduTransmitted = true;
    }

    public void reset() {
        this.apduTransmitted = false;
    }

    public boolean isApduTransmitted() {
        return apduTransmitted;
    }
}

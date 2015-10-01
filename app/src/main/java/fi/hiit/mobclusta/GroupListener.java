package fi.hiit.mobclusta;

public interface GroupListener {
    void setMasterAndConnected(boolean master);
    // computation parameters
    int getWidth();
    int getHeight();
    int getTasks();
    int getSubsamples();
    int getMaxIterations();
    CompParams getCompParams();
    void computationDone();
}

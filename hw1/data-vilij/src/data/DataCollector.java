package data;

public class DataCollector {
    private DataSet dataSet;
    private boolean empty = true;

    synchronized DataSet take(){
        while(empty) {
            try { wait(); } catch (InterruptedException ignored) { }
        }
        empty = true;
        notifyAll();
        return dataSet;
    }

    public synchronized void put(DataSet dataSet){
        while(!empty){
            try { wait(); } catch (InterruptedException ignored) { }
        }
        empty = false;
        this.dataSet = dataSet;
        notifyAll();
    }
}

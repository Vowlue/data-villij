package data;

import java.util.List;

public class ListCollector {
    private List dataList;
    private boolean empty = true;

    synchronized List take(){
        while(empty) {
            try { wait(); } catch (InterruptedException ignored) { }
        }
        empty = true;
        notifyAll();
        return dataList;
    }

    public synchronized void put(List dataList){
        while(!empty){
            try { wait(); } catch (InterruptedException ignored) { }
        }
        empty = false;
        this.dataList = dataList;
        notifyAll();
    }
}

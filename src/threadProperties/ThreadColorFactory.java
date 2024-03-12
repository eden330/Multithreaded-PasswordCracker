package threadProperties;

import java.util.concurrent.ThreadFactory;

public class ThreadColorFactory implements ThreadFactory {
    private ThreadColor threadColor;
    private int counter = 1;

    public ThreadColorFactory() {
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        String threadName;
        if (threadColor == null) {
            threadName = ThreadColor.values()[counter].name();
        } else {
            threadName = threadColor.name();
        }
        if (++counter > ThreadColor.values().length - 1) {
            counter = 1;
        }
        thread.setName(threadName);
        return thread;
    }

}
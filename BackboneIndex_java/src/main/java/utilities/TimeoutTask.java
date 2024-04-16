package utilities;

import Comparison.BaselineMethods.Baseline;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class TimeoutTask extends TimerTask {
    private Thread t;
    private Timer timer;
    private Baseline baseline;
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public TimeoutTask(Thread t, Timer timer, Baseline baseline) {
        this.t = t;
        this.timer = timer;
        this.baseline = baseline;
    }

    public void run() {
        if (t != null && t.isAlive()) {

            t.interrupt();
            System.out.println(t.isInterrupted()+" "+t.isAlive()+" "+t.isDaemon()+" "+t.getName());
            timer.cancel();

            LOGGER.info("Thread time out in " + ParsedOptions.timeout + " ms");
            LOGGER.info("bbs|" + baseline.current_src + ">" + baseline.current_dest + "|Query time : " + 9999999);
            if (baseline.neo4j != null) {
                baseline.neo4j.closeDB();
            }


        }
    }
}
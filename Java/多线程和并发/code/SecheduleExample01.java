import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @Auther mengchen
 * @Date 2019-09-01 13:16:39
 */
public class SecheduleExample01 {
    static class LongRunningTask extends TimerTask {

        @Override
        public void run() {
            System.out.println("我被执行结束了: " + LocalDateTime.now());
        }
    } 

    static class FixedDelayTask extends TimerTask {
        @Override
        public void run() {
            System.out.println("我又被执行了一次，执行的时间: " + LocalDateTime.now());
        }
    }

    public static void main(String[] args) {
        Timer time = new Timer();
        System.out.println("现在的时间是: " + LocalDateTime.now());
        time.schedule(new LongRunningTask(), 1000);
        time.schedule(new FixedDelayTask(), 100, 1000);
        
    }
}
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Date;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * SchedledThreadPoolExecutor 示例
 * 
 * @Auther mengchen
 * @Date 2019-09-11 22:14:06
 */
public class ScheduledThreadPoolExecutorExample {

    public static void main(String[] args) throws Exception {
        DelayQueue
        ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(10);
        Instant start = Instant.now();
        System.out.println("开始时间：" + start);
        scheduledExecutor.schedule(() -> {
            System.out.println("Hello ScheduledThreadPoolExecutor!");
            System.out.println("执行完毕：" + Duration.between(start, Instant.now()).getSeconds() + "s");
        }, 5, TimeUnit.SECONDS);
        Instant s = Instant.now();
        ScheduledFuture<String> scheduledFuture = scheduledExecutor.schedule(() -> "Hello ScheduledFuture", 5,
                TimeUnit.SECONDS);
        System.out.println("开始时间：" + start);
        System.out.println("结果输出: " + scheduledFuture.get());
        System.out.println("执行完毕：" + Duration.between(s, Instant.now()).getSeconds() + "s");
        scheduledExecutor.shutdown();
    }
}
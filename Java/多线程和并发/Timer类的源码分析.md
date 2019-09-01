# Timer, TimerTask 类的源码分析

[TOC]

## Timer 类中的常用 API

1. `public void cancel()` 终止计时器，丢弃当前已安排的任务。
2. `int purge()` 从计时器的任务队列中移除所有已取消的任务。
3. `void schedule(TimeerTask task, Date date)` 安排在指定的时间执行指定的任务。
4. `void schedule(TimerTask task, long delay)` 安排任务在指定的延时后开始。
5. `void schedule(TimerTask task, Date firstTime, long period)` 安排指定的任务在指定的时间开始重复的固定延迟执行。
6. `void schedule(TimerTask task, long delay, long peried)` 安排指定的任务在指定的延时汉堡开始进行固定延迟执行。
7. `void scheduleAtFixedRate(TimerTask task, long delay, long period)` 安排指定的任务在指定的延迟后开始进行重复的固定的速率执行。
8. `void scheduleAtFixedRate(TimerTask task, Date firstTime, long period)` 安排指定的任务在指定指定的时间开始进行重复的固定速率执行。

## Timer 的例子

```java
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
```

上面的 `LongRunningTask` 是在指定的延时后开始执行。而 `FixedDelayTask` 是在指定的延迟后开始已固定的延迟重复执行。

下面我们来看看 TimerTask 的源码。

## TimerTask 的源码分析如下

```java
public abstract class TimerTask implements Runnable {
    /**
     * 这个 object 被用作同步锁
     * This object is used to control access to the TimerTask internals.
     */
    final Object lock = new Object();

    /**
     * 初始化当前状态为 为调用的
     * The state of this task, chosen from the constants below.
     */
    int state = VIRGIN;

    /**
     * 表示这个任务未调用
     * This task has not yet been scheduled.
     */
    static final int VIRGIN = 0;

    /**
     * 这个任务已经被安排执行，如果它是一个非重复的任务，则尚未执行
     * This task is scheduled for execution.  If it is a non-repeating task,
     * it has not yet been executed.
     */
    static final int SCHEDULED   = 1;

    /**
     * 一个非重复的任务已经被执行并且未必取消
     * This non-repeating task has already executed (or is currently
     * executing) and has not been cancelled.
     */
    static final int EXECUTED    = 2;

    /**
     * 这个任务已经被取消
     * This task has been cancelled (with a call to TimerTask.cancel).
     */
    static final int CANCELLED   = 3;

    /**
     * 如果才任务已经被安排执行，则已 System.currentTimeMillis 格式返回下一次次任务执行的时间。对于重复任务，次字段在每个任务执行之前更新
     * Next execution time for this task in the format returned by
     * System.currentTimeMillis, assuming this task is scheduled for execution.
     * For repeating tasks, this field is updated prior to each task execution.
     */
    long nextExecutionTime;

    /**
     * 重复执行任务的间隔时间段。正则表明一个固定比率执行的任务。负值表示固定延迟执行的任务。0值表示非重复任务
     * Period in milliseconds for repeating tasks.  A positive value indicates
     * fixed-rate execution.  A negative value indicates fixed-delay execution.
     * A value of 0 indicates a non-repeating task.
     */
    long period = 0;

    /**
     * Creates a new timer task.
     */
    protected TimerTask() {
    }

    /**
     * 抽象的 rune 方法等待子类实现
     * The action to be performed by this timer task.
     */
    public abstract void run();

    /**
     * 取消这个定时任务。如果这个任务已经被安排执行了至少一次并且现在没有被执行，或者没有被安排执行，这个任务将被取消，永远不会执行。（如果这个任务在调用 cancel() 方法的时候，这个在被执行，那个当这次任务执行完成后，就永远不会被执行了。
     * 如果这个任务的 state 为 SECHEDULE 那么将方轨 true，否则返回 false
     * Cancels this timer task.  If the task has been scheduled for one-time
     * execution and has not yet run, or has not yet been scheduled, it will
     * never run.  If the task has been scheduled for repeated execution, it
     * will never run again.  (If the task is running when this call occurs,
     * the task will run to completion, but will never run again.)
     *
     * <p>Note that calling this method from within the <tt>run</tt> method of
     * a repeating timer task absolutely guarantees that the timer task will
     * not run again.
     *
     * <p>This method may be called repeatedly; the second and subsequent
     * calls have no effect.
     *
     * @return true if this task is scheduled for one-time execution and has
     *         not yet run, or this task is scheduled for repeated execution.
     *         Returns false if the task was scheduled for one-time execution
     *         and has already run, or if the task was never scheduled, or if
     *         the task was already cancelled.  (Loosely speaking, this method
     *         returns <tt>true</tt> if it prevents one or more scheduled
     *         executions from taking place.)
     */
    public boolean cancel() {
        synchronized(lock) {
            boolean result = (state == SCHEDULED);
            state = CANCELLED;
            return result;
        }
    }

    /**
     * 返回下一次执行的时间
     * Returns the <i>scheduled</i> execution time of the most recent
     * <i>actual</i> execution of this task.  (If this method is invoked
     * while task execution is in progress, the return value is the scheduled
     * execution time of the ongoing task execution.)
     *
     * <p>This method is typically invoked from within a task's run method, to
     * determine whether the current execution of the task is sufficiently
     * timely to warrant performing the scheduled activity:
     * <pre>{@code
     *   public void run() {
     *       if (System.currentTimeMillis() - scheduledExecutionTime() >=
     *           MAX_TARDINESS)
     *               return;  // Too late; skip this execution.
     *       // Perform the task
     *   }
     * }</pre>
     * This method is typically <i>not</i> used in conjunction with
     * <i>fixed-delay execution</i> repeating tasks, as their scheduled
     * execution times are allowed to drift over time, and so are not terribly
     * significant.
     *
     * @return the time at which the most recent execution of this task was
     *         scheduled to occur, in the format returned by Date.getTime().
     *         The return value is undefined if the task has yet to commence
     *         its first execution.
     * @see Date#getTime()
     */
    public long scheduledExecutionTime() {
        synchronized(lock) {
            return (period < 0 ? nextExecutionTime + period
                               : nextExecutionTime - period);
        }
    }
}

```

`TimerTask` 类继承了 `Runnable` ，代码中主要定义了定时任务的集中状态:

1. VARGIN: 表示任务为表调用。
2. SCHEDULED: 表示这个任务已经被调度，但是还没有被执行。
3. EXECUTED: 任务正在被执行。
4. CANCEL: 任务被取消。

`period` 的正值表示固定比率，0表非重复任务，负值表示固延迟执行。

## Timer 源码分析

在 Timer 类中又一个队列来维护任务的执行顺序—— `TaskQueue`，也有一个线程来执行队列中的任务—— `TimerThread`，如下：

```java
/**
 * TimerTask 的队列
 * TimerTask 的队列和 TimerThread 来配合使用。
 * Timer 通过调用 scehdule 方法来将任务加入到任务队列中
 * TimerThread 在适当的时候从队列中取任务并执行
 * The timer task queue.  This data structure is shared with the timer
 * thread.  The timer produces tasks, via its various schedule calls,
 * and the timer thread consumes, executing timer tasks as appropriate,
 * and removing them from the queue when they're obsolete.
 */
private final TaskQueue queue = new TaskQueue();

/**
 * The timer thread.
 */
private final TimerThread thread = new TimerThread(queue);
```

下面我们来看看 Timer 的构造函数：

```java
// 一个原子的整数，用来生成 线程的名字序号
private final static AtomicInteger nextSerialNumber = new AtomicInteger(0);
private static int serialNumber() {
    return nextSerialNumber.getAndIncrement();
}

public Timer() {
    this("Timer-" + serialNumber());
}

// 是否守护线程
public Timer(boolean isDaemon) {
    this("Timer-" + serialNumber(), isDaemon);
}

// 指定线程的名字
public Timer(String name) {
    thread.setName(name);
    thread.start();
}

// 指定线程名字并且设置是否守护线程
public Timer(String name, boolean isDaemon) {
    thread.setName(name);
    thread.setDaemon(isDaemon);
    thread.start();
}
```

下面我们来看看 Timer 的 `schedule` 方法：

```java
// 指定延时
public void schedule(TimerTask task, long delay) {
    if (delay < 0)
        throw new IllegalArgumentException("Negative delay.");
    // 当前时间 + delay
    sched(task, System.currentTimeMillis()+delay, 0);
}

// 指定时间
public void schedule(TimerTask task, Date time) {
    sched(task, time.getTime(), 0);
}

// 指定延时和重复间断时间段，这里的负值，表示固定的延迟
public void schedule(TimerTask task, long delay, long period) {
    if (delay < 0)
        throw new IllegalArgumentException("Negative delay.");
    if (period <= 0)
        throw new IllegalArgumentException("Non-positive period.");
    sched(task, System.currentTimeMillis()+delay, -period);
}

public void schedule(TimerTask task, Date firstTime, long period) {
    if (period <= 0)
        throw new IllegalArgumentException("Non-positive period.");
    sched(task, firstTime.getTime(), -period);
}

public void scheduleAtFixedRate(TimerTask task, long delay, long period) {
    if (delay < 0)
        throw new IllegalArgumentException("Negative delay.");
    if (period <= 0)
        throw new IllegalArgumentException("Non-positive period.");
    // 这里 period 是正的，表示固定比率
    sched(task, System.currentTimeMillis()+delay, period);
}

public void scheduleAtFixedRate(TimerTask task, Date firstTime,
                                long period) {
    if (period <= 0)
        throw new IllegalArgumentException("Non-positive period.");
    sched(task, firstTime.getTime(), period);
}
```

上面的所有方法最终都调用的是下面的方法:

```java
private void sched(TimerTask task, long time, long period) {
    if (time < 0)
        throw new IllegalArgumentException("Illegal execution time.");

    // Constrain value of period sufficiently to prevent numeric
    // overflow while still being effectively infinitely large.
    if (Math.abs(period) > (Long.MAX_VALUE >> 1))
        period >>= 1;
    // 锁定任务
    synchronized(queue) {
        if (!thread.newTasksMayBeScheduled)
            throw new IllegalStateException("Timer already cancelled.");

        // 锁定任务
        synchronized(task.lock) {
            // 判断当前的状态是否是 VIRGIN
            if (task.state != TimerTask.VIRGIN)
                throw new IllegalStateException(
                    "Task already scheduled or cancelled");
            // 设置时间
            task.nextExecutionTime = time;
            task.period = period;
            task.state = TimerTask.SCHEDULED;
        }
        // 添加到队列中
        queue.add(task);
        // 判断当前任务是否是最小的
        if (queue.getMin() == task)
            queue.notify();
    }
}
```

下面我们来看看 `TaskQueue` 的源码：

```java
class TaskQueue {
    // 这里利用一个最小堆的算法，初始 128 个
    private TimerTask[] queue = new TimerTask[128];

    /**
     * The number of tasks in the priority queue.  (The tasks are stored in
     * queue[1] up to queue[size]).
     */
    private int size = 0;

    /**
     * Returns the number of tasks currently on the queue.
     */
    int size() {
        return size;
    }

    /**
     * Adds a new task to the priority queue.
     */
    void add(TimerTask task) {
        // Grow backing store if necessary
        if (size + 1 == queue.length)
            // 如果容量不够就扩容
            queue = Arrays.copyOf(queue, 2*queue.length);

        queue[++size] = task;
        // 修复最小堆
        fixUp(size);
    }

    /**
     * 返回 最小堆中的最小的一个任务
     * Return the "head task" of the priority queue.  (The head task is an
     * task with the lowest nextExecutionTime.)
     */
    TimerTask getMin() {
        return queue[1];
    }

    /**
     * Return the ith task in the priority queue, where i ranges from 1 (the
     * head task, which is returned by getMin) to the number of tasks on the
     * queue, inclusive.
     */
    TimerTask get(int i) {
        return queue[i];
    }

    /**
     * 删除最小的
     * Remove the head task from the priority queue.
     */
    void removeMin() {
        queue[1] = queue[size];
        queue[size--] = null;  // Drop extra reference to prevent memory leak
        // 修复
        fixDown(1);
    }

    /**
     * 快速删除
     * Removes the ith element from queue without regard for maintaining
     * the heap invariant.  Recall that queue is one-based, so
     * 1 <= i <= size.
     */
    void quickRemove(int i) {
        assert i <= size;

        queue[i] = queue[size];
        queue[size--] = null;  // Drop extra ref to prevent memory leak
    }

    /**
     * 重新调度最小的一个
     * Sets the nextExecutionTime associated with the head task to the
     * specified value, and adjusts priority queue accordingly.
     */
    void rescheduleMin(long newTime) {
        queue[1].nextExecutionTime = newTime;
        fixDown(1);
    }

    /**
     * Returns true if the priority queue contains no elements.
     */
    boolean isEmpty() {
        return size==0;
    }

    /**
     * Removes all elements from the priority queue.
     */
    void clear() {
        // Null out task references to prevent memory leak
        for (int i=1; i<=size; i++)
            queue[i] = null;

        size = 0;
    }

    /**
     * Establishes the heap invariant (described above) assuming the heap
     * satisfies the invariant except possibly for the leaf-node indexed by k
     * (which may have a nextExecutionTime less than its parent's).
     *
     * This method functions by "promoting" queue[k] up the hierarchy
     * (by swapping it with its parent) repeatedly until queue[k]'s
     * nextExecutionTime is greater than or equal to that of its parent.
     */
    private void fixUp(int k) {
        while (k > 1) {
            int j = k >> 1;
            if (queue[j].nextExecutionTime <= queue[k].nextExecutionTime)
                break;
            TimerTask tmp = queue[j];  queue[j] = queue[k]; queue[k] = tmp;
            k = j;
        }
    }

    /**
     * Establishes the heap invariant (described above) in the subtree
     * rooted at k, which is assumed to satisfy the heap invariant except
     * possibly for node k itself (which may have a nextExecutionTime greater
     * than its children's).
     *
     * This method functions by "demoting" queue[k] down the hierarchy
     * (by swapping it with its smaller child) repeatedly until queue[k]'s
     * nextExecutionTime is less than or equal to those of its children.
     */
    private void fixDown(int k) {
        int j;
        while ((j = k << 1) <= size && j > 0) {
            if (j < size &&
                queue[j].nextExecutionTime > queue[j+1].nextExecutionTime)
                j++; // j indexes smallest kid
            if (queue[k].nextExecutionTime <= queue[j].nextExecutionTime)
                break;
            TimerTask tmp = queue[j];  queue[j] = queue[k]; queue[k] = tmp;
            k = j;
        }
    }

    /**
     * Establishes the heap invariant (described above) in the entire tree,
     * assuming nothing about the order of the elements prior to the call.
     */
    void heapify() {
        for (int i = size/2; i >= 1; i--)
            fixDown(i);
    }
}

```

下面来看看 `TimerThread` 的源码：

```java
class TimerThread extends Thread {
    /**
     * 这个标志表示后面是否还会又新任务执行
     * This flag is set to false by the reaper to inform us that there
     * are no more live references to our Timer object.  Once this flag
     * is true and there are no more tasks in our queue, there is no
     * work left for us to do, so we terminate gracefully.  Note that
     * this field is protected by queue's monitor!
     */
    boolean newTasksMayBeScheduled = true;

    /**
     * Our Timer's queue.  We store this reference in preference to
     * a reference to the Timer so the reference graph remains acyclic.
     * Otherwise, the Timer would never be garbage-collected and this
     * thread would never go away.
     */
    private TaskQueue queue;

    TimerThread(TaskQueue queue) {
        this.queue = queue;
    }

    public void run() {
        try {
            // 重点
            mainLoop();
        } finally {
            // Someone killed this Thread, behave as if Timer cancelled
            synchronized(queue) {
                newTasksMayBeScheduled = false;
                queue.clear();  // Eliminate obsolete references
            }
        }
    }

    /**
     * The main timer loop.  (See class comment.)
     */
    private void mainLoop() {
        while (true) {
            try {
                TimerTask task;
                boolean taskFired;
                synchronized(queue) {
                    // Wait for queue to become non-empty
                    // 当前的队列为空并且可能又新的任务被调度
                    while (queue.isEmpty() && newTasksMayBeScheduled)
                        queue.wait();
                    // 如果没有新的任务被调度并且 queue 是空的，那么结束退出
                    if (queue.isEmpty())
                        break; // Queue is empty and will forever remain; die

                    // Queue nonempty; look at first evt and do the right thing
                    long currentTime, executionTime;
                    // 从队列中获取一个最近执行的一个任务
                    task = queue.getMin();
                    synchronized(task.lock) {
                        // 如果被取消了
                        if (task.state == TimerTask.CANCELLED) {
                            // 删除这个任务
                            queue.removeMin();
                            continue;  // No action required, poll queue again
                        }
                        // 记录当前时间
                        currentTime = System.currentTimeMillis();
                        // 执行时间
                        executionTime = task.nextExecutionTime;
                        if (taskFired = (executionTime<=currentTime)) {
                            // 不重复的任务，从队列中删除
                            if (task.period == 0) { // Non-repeating, remove
                                queue.removeMin();
                                // 状态设置为执行
                                task.state = TimerTask.EXECUTED;
                            } else { // Repeating task, reschedule
                                // 重复的任务，重新设置最小的调度
                                // 这里体现了固定速率和固定延时的不同
                                queue.rescheduleMin(
                                  task.period<0 ? currentTime   - task.period
                                                : executionTime + task.period);
                            }
                        }
                    }
                    // 时间还没到，必须得等待
                    if (!taskFired) // Task hasn't yet fired; wait
                        queue.wait(executionTime - currentTime);
                }
                // 如果时间到了，那么执行
                if (taskFired)  // Task fired; run it, holding no locks
                    task.run();
            // 不支持中断
            } catch(InterruptedException e) {
            }
        }
    }
}

```
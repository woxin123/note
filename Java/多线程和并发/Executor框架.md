# Executor 框架

## Executor 框架简介

### Executor 框架的两级调度模型

在 Hostspot JVM 的线程模型中，Java 线程 (java.lang.Thread) 被一对一映射为本地操作系统线程。Java 线程启动时会创建一个本地操作系统线程，当该 Java 线程终结时，这个操作系统线程也会被回收。操作系统会调度所有线程并将它们分配给可用的 CPU。

在上层，Java 多线程程序通常会把应用分解为若干个任务，然后使用用户级调度器（Executor 框架）将这些任务映射为固定数量的线程；在底层，操作系统内核将这些线程映射到硬件处理器上。这种两级调用模型的示意图如下图所示：

### Executor 框架的结构和成员

下面将 Executor 框架分成两部分介绍：Executor 的结构和 Executor 框架包含的组件成员：

+ 任务。包括被执行的任务需要实现的接口: Runnable 接口或 Callable 接口。
+ 任务的执行。包括任务执行机制的核心接口 Executor，以及继承自 Executor 的 ExecutorService 接口。Executor 框架有两个关键类实现了 ExecutorService 接口（ThreadPoolExecutor 和 ScheduledThreadPoolExecutor）。
+ 异步计算的结果。包括接口 Future 个实现 Future 接口的 FutureTask 类。

下面是这些类和接口的简介：

+ Executor 是一个接口，它是 Executor 框架的基础，他将任务的提交与任务的执行分离开来。
+ ThreadPoolExecutor 是线程池的核心实现，用来执行被提交的任务。
+ ScheduledThreadPollExecutor 是一个实现类，可以在给定的延时后运行命令，或者定期执行命令。ScheduledThreadPoolExecutor 比 Timer 更灵活，功能更强大。
+ Future 接口和实现 Future 接口的 FutureTask 类，代表异步计算的结果。
+ Runnable 接口和 Callable 接口的实现类，都可以被 ThreadPoolExecutor 或 ScheduledThreadPoolExecutor执行。

Executor 框架的使用示意图如下图：

![](http://img.mcwebsite.top/20190903133410.png)

![](http://img.mcwebsite.top/20190903135231.png)

主线程首先要创建实现的 Runnable 或者 Callable 接口的任务对象。工具类 Executors 可以把一个 Runnable 对象封装为一个 Callable 对象（`Executors.callable(Runnable task)` 或 `Executors.callable(Runnable, Object resule)`）。

然后可以把 `Runnable` 对象直接交给 `ExecutorService` 执行（`ExecutorService.execute(Runnable command)`）；或者可以把 `Runnable` 对象或者 `Callable` 对象交给 `ExecutorService` 执行 （`ExecutorService.submit(Runnable task)` 或 `ExecutorService.submit(Callable<T> task`）。

如果执行 `ExecutorService.submit(...)`，ExecutorService 将返回一个实现 Future 接口的对象（到目前为止的 JDK 中，返回的仍然是 FutureTask 对象）。由于 FutureTask 实现了 Runnable，最后程序员也可以创建 FutureTask，然后直接交给 ExecutorService 执行。

最后，主线程可以执行 `FutureTask.get()` 方法来等待任务的执行完成。主线程也可以执行 `FutureTask.cancle(boolean mayInterruptIfRunning)` 来取消此任务。

#### Executor 框架的成员

Executoor 框架的主要成员：`ThreadPoolExecutor`、`ScheduledThreadPoolExecutor`、`Future` 接口、`Runnable` 接口、`Callable` 接口和 `Executors`。

1. `ThreadPoolExecutor`

    `ThreadPoolExecutor` 通常使用工厂类 `Executors` 来创建。`Executors` 可以创建 3 种类型的 `ThreadPoolExecutor` : `SingleThreadExecutor`、`FixedThreadPool` 和 `CachedThreadPool`。

    1. `FixedThreadPool`。下面是 `Executors` 提供的，创建使用固定线程数的 `FixedThreadPool` 的 API。

    ```java
    public static ExecutorService newFixedThreadPool(int nThreads);
    public static ExecutorService newFixedThreadPool(int nThreads, ThreadFactory threadFactory);
    ```
    
    `FixedThreadPool` 使用于为了满足资源管理的需求，而需要限制当前线程数量的应用场景，它适用于负载比较重的服务器。

    2. `SingletonThreadExecutor`。下面是 `Executors` 提供的，创建使用单个线程的 `SingletonThreadExecutor` 的 API。

    ```java
    public static ExecutorService newSingleThreadExecutor()；
    public static ExecutorService newSingleThreadExecutor(ThreadFactory threadFactory);
    ```

    SingleThreadExecutor 适用于需要保证顺序地执行各个任务，并且在任意的时间点，不会有多个线程是活动线程。

    3. CachedThreadPool。下面是 Executors 提供的，创建一个会根据需要创建新线程的 `CachedThreadPool` 的 API。

    ```java
    public static ExecutorService newCachedThreadPool();
    public static ExecutorService newCahcedThreadPool(ThreadFactory threadFactory);
    ```

    CachedThreadPool 是大小无界的线程池，适用于执行很多短期的异步任务，或者负责较轻的服务器。

2. ScheduleThreadPoolExecutor

    ScheduleThreadPoolExecutor 通常使用工厂类 Executors 来创建。Executors 可以创建两种类型的 ScheudledThreadPoolExecutor，如下：

    + ScheudledThreadPoolExecutor。包含若干线程的 ScheudledThreadPoolExecutor。
    + SingleThreadScheudledExecutor。只包含一个线程的 ScheudleThreadPoolExecutor。

    下面分别介绍这两种 ScheduledThreadPoolExecutor。

    下面是工厂类 Executors 提供的，创建固定数量的 ScheudleThreadPoolExecutor 的 API。

    ```java
    public static ScheudleExecutorService newScheduledThreadPool(int corePoolSize);
    public static SechudleExecutorService newScheduledThreadPool(int corePoolSize, ThreadFactory threadFactory);
    ```

    ScheudledThreadPoolExecutor 适用于需要多个后台线程执行周期任务，同时为了满足资源管理的需求而需要限制后台线程数量的应用场景。下面是 Executors 提供的，创建单个线程的 SingleScheduleThreadPoolExecutor 的 API。

    ```java
    public static ScheduledExecutorService newSingleThreadScheduledExecutor();
    public static ScheduledExecutorService newSingleThreadScheduledExecutor(ThreadFactory threadFactory);
    ```

    SingleThreadScheduledExecutor 适用于需要单个后台线程执行周期任务，同时需要保证顺序地执行各个任务的应用场景。

3. Future 接口

    Future 接口和实现 Future 接口的 FutureTask 类用来表示异步计算的结果。当我们把 Runnable 接口或 Callable 接口的实现类提交 (submit) 给 ThreadPoolExecutor 或 ScheduledThreadPoolExecutor 时，ThreadPoolExecutor 或 ScheduledThreadPoolExecutor 会想我们返回一个 FutureTask 对象。下面是对应的 API。

    ```java
    <T> Future<T> submit(Callable<T> task);
    <T> Future<T> submit(Runnable task, T result);
    Future<?> submit(Runnable task);
    ```

    有一点需要注意，截至到 jdk 8 为止，Java 通过上述 API 返回的是一个实现了 Future 接口的对象。在将来的 JDK 实现中，返回的可能就不是 FutureTask 。

4. Runnable 接口和 Callable 接口

    Runnable 接口和 Callable 的实现类，都可以被 ThreadPoolExecutor 或 ScheduledThreadPoolExecutor 执行。它们的区别是 Runnbale 不会返回结果，而 Callable 可以返回结果。

    除了可以自己创建实现 Callable 接口的对象外，还可以使用工厂类 Executors 来把一个 Runnable 包装成一个 Callable。
    
    下面是 Executors 提供的，把一个 Runnable 包装成 Callable 的 API。

    下面是 Executor 提供的，把一个 Runnable 包装成 Callable 的 API。

    ```java
    public static Callable<Object> callable(Runnable task); // 假设返回对象 Callable1
    ```

    下面是 Executors 提供的，把一个 Runnable 和一个待返回的结果包装成一个 Callable 的 API。

    ```java
    public static <T> Callable<T> callable(Runnable task, T result); // 假设返回对象 Callable2
    ```

    前面我们讲过，当我们把一个 Callable 对象（比如上面的 Callable1 或 Callable2）提交给 ThreadPoolExecutor 执行时， submit(...)会向我们返回一个 FutureTask 对象。我们可以执行 FutureTask.get() 将返回该任务的结果。例如，如果提交的是对象 Callable1，FutureTask.get() 方法将返回 null；如果提交的是对象 Callable2，FutureTask.get() 方法将返回 result 对象。

### ThreadPoolExecutor 详解

Executor 框架最核心的类是 ThreadPoolExecutor，它是线程池的实现类，主要由下面的 4 个组件构成。

+ corePool: 核心线程池大小。
+ maximumPool: 最大线程池大小。
+ BlockingQueue: 用来暂时保存任务的工作队列。
+ RejectedExecutionHandler: 当 ThreadPoolExecutor 已经关闭或 ThreadPoolEXecutor 已经饱和时（达到了最大线程池大小且工作队列已满），execute() 方法将要调用的 Handler。

通过 Executor 框架的工具类 Executors，可以创建 3 中类型的 ThreadPoolExecutor。

+ FixedThreadPool。
+ SingleThreadExecutor。
+ CachedThreadPool。

#### 1. FixedThreadPool 详解

FixedThreadPool 被称为可重用固定线程数量的线程池。下面是 FixedThreadPool 的源码实现。

```java
public static ExecutorService newFixedThreadPool(int nThread) {
    return new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
}
```

FixedThreadPool 的 corePoolSize 和 maximumPoolSize 都被设置为创建 FixedThreadPool 时指定的参数 nThreads。

当线程池中的线程数大于 corePoolSize 时，keepAliveTime 为多余的空闲线程等待新任务的最长时间，超过这个时间后多余的线程将被终止。这里把 keepAvliveTime 设置为 0L，意味着多余的空闲线程会被立即终止。

FixedThreadPool 的 execute() 方法的运行示意图如下图所示：

![](http://img.mcwebsite.top/20190910234904.png)

1. 如果当前运行的线程数少于 corePoolSize，则创建新的线程来执行任务。
2. 当线程池完成预热之后（当前运行的线程数等于 corePoolSize），将任务加载到 LinkedBlockingQueue。
3. 线程执行完 1 中的任务后，会在循环中反复 LinkedBlockingQueue 获取任务来执行。

FixedThreadPool 使用无界队列 LinkedBlockingQueue 作为线程池的工作队列（队列的容量为 Integer,NAX_VALUE）。使用无界队列作为工作队列会对线程池带来如下影响。

1. 当线程池中的数量达到 corePoolSize 后，新任务将在无界队列中等待，因此线程池中的线程数不会超过 corePoolSize。
2. 由于 1，使用无界队列时 maximumPoolSize 将是一个无效的参数。
3. 由于 1 和 2，使用无界队列是 keepAliveTime 将时一个无效参数。
4. 由于使用无界队列，运行中的 FixedThreadPool（未执行 shutdown() 或 shutdownNow()）不会拒绝任务（不会调用 RejectedExecutionHandler.rejectedExecution 方法）。

#### 2. SingleThreadExecutor 详解

SingleThreadExecutor 是使用单个 worker 线程的 Executor。下面是 SingleThreadPoolExecutor 的代码实现。

```java
public static ExecutorService newSingleThreadExecutor() {
    return new FinalizableDelegatedExecutorService（new ThreadPoolExecutor(1, 1, OL, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>()));
}
```

SingleThreadExecutor 的 corePoolSize 和 maximumPoolSize 被设置为 1，其他参数与 FixedThreadPool 相同。SingleThreadExecutor 使用无界队列 LinkedBlockingQueue 作为线程池的工作队列（队列的容量为 Integer.MAX_VALUE）。SingleThreadExecutor 使用无界队列作为工作队列对线程池带来的影响与 FixedThreadPool 相同，这里就不赘述了。

SingleThreadExecutor 的运行示意图如下图所示：

![](http://img.mcwebsite.top/20190911112557.png)

1, 如果当前运行的线程数量少于 corePoolSize（即当前线程中无运行的线程），则创建一个新的线程来执行任务。
2. 在线程池完成预热之后（当前线程池中只有一个线程），将任务加入 LinkedBlockingQueue 中。
3. 线程执行完 1 中的任务后，会在一个无线循环中反复从 LinkedBlockingQueue 获取任务来执行。

#### 3. CachedThreadPool 详解

CachedThreadPool 是一个会根据需要创建新线程的线程池。下面时创建 CahcedThreadPool 的源代码。

```java
public static ExecuteService newCahcedThreadPool() {
    return new ThreadPoolExecute(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
    new SynchronousQueue<Runnable>());
}
```

CachedThreadPool 的 corePoolSize 被设置为 0，即 corePool 为空；maximumPoolSize 被设置为 Integer.MAX_VALUE，即 maximumPool 是无界的。这里把 keepAliveTime 设置为 60L ，意味着 CachedThreadPool 中的空闲线程等待新任务的最长时间为 60 秒，空闲线程超过 60 秒后会被终止。

FixedThreadPool 和 SingleThreadExecutor 使用无界队列 LinkedBlockingQueue 作为线程池的工作队列。CachedThreadPool 使用没有容量的 SynchronousQueue 作为线程池的工作队列，但 CachedThreadPool 的 maximumPool 是无界的。这意味着，如果主线程提交任务的速度高于 maximumPool 中线程处理任务的速度时，CachedThreadPool 会不断创建新的线程。极端情况下，CachedThreadPool 会因为线程创建过多而耗尽 CPU 和内存资源。

CachedThreadPool 的 execute() 方法的执行示意图如下图所示：

![](http://img.mcwebsite.top/20190911140506.png)
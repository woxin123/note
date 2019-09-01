# Java中的阻塞队列

阻塞队列（Blocking Queue）是一个支持两个附加操作的队列。这两个附加操作支持阻塞的插入和移除方法。

1. 支持阻塞的插入方法，意思是当队列满时，队列会阻塞插入元素的线程，知道队列不满。
2. 支持阻塞移出的方法：意思是在队列为空时，获取元素的线程对等待队列变为非空。

阻塞队列常用于生产者消费者的场景，生产者是向队列里添加元素的线程，消费者是从队列里取元素的线程。阻塞队列就是生产者用来存放元素、消费者用来获取元素的容器。

在阻塞队列不可用时，这两个附加操作提供了四种处理方式：

|方法/处理方式|抛出方式|返回特殊值|一直阻塞|超时退出|
|:----:|:----:|:----:|:----:|:------:|:-----:|
|插入方法|add(e)|offer(e)|put(e)|offer(e, time, unit)|
|移出方法|remove()|poll()|take()|put(time, out)|
|检查方法|element()|peek()|不可用|不可用|不可用|

+ 抛出异常：当队列满时，如果再往队列里面插入元素，就会抛出`IllegalStateException("Queue full")`异常。当队列空时，从队列中获取元素会抛出`NoSuchElementException`异常。
+ 返回特殊值：当往队列插入元素时，会返回元素是否插入成功，成功返回true。如果是移出方法，则是从队列里取出一个元素，如果没有则返回null。
+ 一直阻塞：当阻塞队列满时，如果生产者线程往队列里面put元素，队列会一直阻塞生产者线程，直到队列可用或者响应中断退出。当队列为空时，如果消费者从队列里面take元素，队列会阻塞住消费者线程，直到队列不为空。

注意：**如果是无界阻塞队列，队列不可能会出现满了的情况，所以使用put和offer方法永远不会被阻塞，而是使用offer方法时，该方法永远返回true。**

阻塞队列对应

```java
public interface BlockingQueue<E> extends Queue<E> {
    // 抛出异常
    boolean add(E e);

    // 返回特殊值
    boolean offer(E e);

    // 阻塞，可中断
    void put(E e) throws InterruptedException;

    // 超时退出
    boolean offer(E e, long timeout, TimeUnit unit)
        throws InterruptedException;

    // 阻塞，可中断
    E take() throws InterruptedException;

    // 返回特殊值，也就是删除的对象
    E poll(long timeout, TimeUnit unit)
        throws InterruptedException;

    // 剩余的容量
    int remainingCapacity();

    // 删除 抛出异常
    boolean remove(Object o);

    // 是否包含
    public boolean contains(Object o);

    // 从该队列中删除可用元素，并将它们添加到给定集合中。
    int drainTo(Collection<? super E> c);

    // 从该队列中删除maxElements可用元素，并将它们添加到给定集合中。
    int drainTo(Collection<? super E> c, int maxElements);
}

```

## Java里的阻塞队列

JDK提供了7个阻塞队列，如下：

+ ArrayBlockingQueue：一个由数组构成的有界阻塞队列。
+ LinkedBlockingQueue：一个由链表组成的有界阻塞队列。
+ PriorityBlockingQueue：一个支持优先级排列的无界阻塞队列。
+ DelayQueue：一个使用优先级队列实现的无界阻塞队列。
+ SynchronousQueue：一个不存储元素的阻塞队列。
+ LinkedTransferQueue：一个由链表结构组成的无界阻塞队列。
+ LinkedBlockingDeque：一个由链表结构组成的双向阻塞队列。

### 1. ArrayBlockingQueue

ArrayBlockingQueue是一个数组实现的有界队列。此队列按照先进先出（FIFO）的原则对元素进行排序。

下面的是一个ArrayBlockingQueue的一个例子：

```java
@Slf4j
public class ArrayBlockingQueueExample {

    private static ArrayBlockingQueue<Integer> blockingQueue = new ArrayBlockingQueue<>(10);

    public static void main(String[] args) throws InterruptedException {
        // 放入10个元素
        for (int i = 0; i < 10; i++) {
            log.info("往阻塞队列中放入：{}", i);
            blockingQueue.put(i);
        }
        if (blockingQueue.remainingCapacity() > 0) {
            log.info("阻塞队列中没有空间了");
        } else {
            log.info("阻塞队列中的剩余空间：{}", blockingQueue.remainingCapacity());
        }
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                Integer take = blockingQueue.take();
                log.info("从阻塞队列中取出一个数据：{}", take);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        log.info("往阻塞队列插入一个数据");

        try {
            blockingQueue.add(11);
        } catch (IllegalStateException e) {
            log.info("往阻塞队列中插入（add）数据的时候发生异常：{}", e);
        }

        boolean offer = blockingQueue.offer(11);
        log.info("往阻塞队列中插入(offer)数据的时候，返回一个特殊值，{}", offer);

        log.info("往阻塞队列中插入(put)数据的时候，一直阻塞");
        blockingQueue.put(11);

        for (int i = 0; i < 10; i++) {
            Integer take = blockingQueue.take();
            log.info("从阻塞队列中取出一个元素：{}", take);
        }

    }

}
```

默认情况下不保证线程公平的访问队列，所谓公平访问是指阻塞的线程，可以按照队列阻塞的先后顺序访问队列，即先阻塞线程先访问队列。非公平性是对等待的线程是非公平的，如果有一个线程刚好在队列可用的时候调用了take()方法，那么可能调用`take()`方法的线程会插队到等待队列中的第一个节点的前面。为了保证公平性，通常会降低吞吐量。我们可以使用一下代码创建一个非公平的阻塞队列。

```java
ArrayBlockingQueue<Integer> blockingQueue = new ArrayBlockingQueue<>(10, true);
```

访问者的公平锁是通过可重入锁实现的，代码如下：

```java
public ArrayBlockingQueue(int capacity, boolean fair) {
    if (capacity <= 0)
        throw new IllegalArgumentException();
    this.items = new Object[capacity];
    lock = new ReentrantLock(fair);
    notEmpty = lock.newCondition();
    notFull =  lock.newCondition();
}
```

可以看到`ArrayBlockingQueue`是通过`ReentrantLock`可重入锁和这个可重入锁的两个等待的`Condition`队列，`notEmpty`和`notFull`实现的。

下面来看看它的put和take方法的源码：

```java
public void put(E e) throws InterruptedException {
    Objects.requireNonNull(e);
    final ReentrantLock lock = this.lock;
    lock.lockInterruptibly();
    try {
        // 队列满了的时候进入notFull等待队列
        while (count == items.length)
            notFull.await();
        enqueue(e);
    } finally {
        lock.unlock();
    }
}

public E take() throws InterruptedException {
    final ReentrantLock lock = this.lock;
    lock.lockInterruptibly();
    try {
        // 队列为空的时候进入notEmpty等待队列
        while (count == 0)
            notEmpty.await();
        return dequeue();
    } finally {
        lock.unlock();
    }
}
```

### 2. LinkedBlockingQueue

`LinkedBlockingQueue`是一个基于链表的实现的有界队列。此队列的默认最大长度为`Integer.MAX_VALUE`。此队列按照先进先出的顺序对元素进行排序。

`LinkedBlockingQueue`的示例如下：

```java
@Slf4j
public class LinkedBlockingQueueExample {
    
    private static LinkedBlockingQueue<Integer> blockingQueue = new LinkedBlockingQueue<>(10);

    public static void main(String[] args) throws InterruptedException {
        // 放入10个元素
        for (int i = 0; i < 10; i++) {
            log.info("往阻塞队列中放入：{}", i);
            blockingQueue.put(i);
        }
        if (blockingQueue.remainingCapacity() > 0) {
            log.info("阻塞队列中没有空间了");
        } else {
            log.info("阻塞队列中的剩余空间：{}", blockingQueue.remainingCapacity());
        }
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                Integer take = blockingQueue.take();
                log.info("从阻塞队列中取出一个数据：{}", take);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        log.info("往阻塞队列插入一个数据");

        try {
            blockingQueue.add(11);
        } catch (IllegalStateException e) {
            log.info("往阻塞队列中插入（add）数据的时候发生异常：{}", e);
        }

        boolean offer = blockingQueue.offer(11);
        log.info("往阻塞队列中插入(offer)数据的时候，返回一个特殊值，{}", offer);

        log.info("往阻塞队列中插入(put)数据的时候，一直阻塞");
        blockingQueue.put(11);

        for (int i = 0; i < 10; i++) {
            Integer take = blockingQueue.take();
            log.info("从阻塞队列中取出一个元素：{}", take);
        }

    }
}
```

LinkedBlockingQueue是通过两个`ReentrantLock`，以及这两个`ReentrantLock`的等待队列`Condition`，以及一个`AtomicInteger`实现的。

```java
/** Current number of elements */
private final AtomicInteger count = new AtomicInteger();

/** Lock held by take, poll, etc */
private final ReentrantLock takeLock = new ReentrantLock();

/** Wait queue for waiting takes */
private final Condition notEmpty = takeLock.newCondition();

/** Lock held by put, offer, etc */
private final ReentrantLock putLock = new ReentrantLock();

/** Wait queue for waiting puts */
private final Condition notFull = putLock.newCondition();
```

下面我们来看看它的具体的put方法和take方法的实现：

```java
public void put(E e) throws InterruptedException {
    if (e == null) throw new NullPointerException();
    final int c;
    final Node<E> node = new Node<E>(e);
    final ReentrantLock putLock = this.putLock;
    final AtomicInteger count = this.count;
    putLock.lockInterruptibly();
    try {
        while (count.get() == capacity) {
            notFull.await();
        }
        enqueue(node);
        c = count.getAndIncrement();
        if (c + 1 < capacity)
            notFull.signal();
    } finally {
        putLock.unlock();
    }
    // 如果c == 0唤醒notEmpty等待队列上等待的线程。
    if (c == 0)
        signalNotEmpty();
}

private void signalNotEmpty() {
    final ReentrantLock takeLock = this.takeLock;
    takeLock.lock();
    try {
        notEmpty.signal();
    } finally {
        takeLock.unlock();
    }
}

public E take() throws InterruptedException {
    final E x;
    final int c;
    final AtomicInteger count = this.count;
    final ReentrantLock takeLock = this.takeLock;
    takeLock.lockInterruptibly();
    try {
        while (count.get() == 0) {
            notEmpty.await();
        }
        x = dequeue();
        c = count.getAndDecrement();
        if (c > 1)
            notEmpty.signal();
    } finally {
        takeLock.unlock();
    }
    if (c == capacity)
        signalNotFull();
    return x;
}
```

可以看到这里使用了两个锁，一个用于生产一个消费，因为生产在链表的一端，而消费在链表的另一端，操作互不影响。

### 3. PriorityBlockingQueue

`PriorityBlockingQueue`是一个支持优先级的无界阻塞队列，默认情况下采用自然顺序升序排列。也可以自定义类实现`Comparable`接口来指定元素的排序规则。或者初始化`PriorityBlockingQueue`时，指定构造参数Comparator来对元素进行排序。需要注意的是不能保证同优先级元素的顺序。

优先队列因为是无界的，所以不会出现元素已经满了的情况，只会出现元素空了的情况，所以优先队列的实现只用了一个`ReentrantLock`和它的等待队列`notEmpty`。

```java
private final ReentrantLock lock = new ReentrantLock();
/**
 * Condition for blocking when empty.
 */
private final Condition notEmpty = lock.newCondition();
```

具体在插入和删除的时候用到了优先队列，也就是堆排序的思想。在这里不做讨论。

### 4. DelayQueue

`DelayQueue`是一个支持延时获取元素的无界阻塞队列。队列使用PriorityQueue来实现。队列中的元素必须实现Delayed接口，在创建的时候才可以指定多久才能从队列中获取当前元素。只有在延迟期满时才能从队列中提取元素。

DelayQueue非常有用，可以将DelayQueue运用在如下的场景。

+ 缓存系统的设计：可以用DelayQueue保存缓存元素的有效期，使用一个线程来循环查询DelayQueue，一旦能从DelayQueue获取元素，表示缓存有效期到了。
+ 定时任务调度：使用DelayQueue保存当天将会执行的任务和时间，一旦从DelayQueue中获取到任务就开始执行，比如`TimerQueue`就是使用DelayQueue实现的。

#### 1. 如何实现Delay接口

DelayQueue队列的元素必须实现Delay接口。我们可以参考`ScheduledThreadPoolExecutor`里的`ScheduleFutureTask`类的实现，一共有三步。

第一步：在创建对象的时候，初始化基本数据。使用time记录当前对象延迟到什么时候可以使用，使用sequenceNumber标识元素在队列中的先后顺序。如下代码所示。

```java
/** Sequence number to break ties FIFO */
private final long sequenceNumber;
/** The nanoTime-based time when the task is enabled to execute. */
private volatile long time;

ScheduledFutureTask(Runnable r, V result, long triggerTime,
                    long sequenceNumber) {
    super(r, result);
    this.time = triggerTime;
    this.period = 0;
    this.sequenceNumber = sequenceNumber;
}
```

第二步：实现getDelay方法，该方法返回当前元素还需要延时多长时间，单位是纳秒，代码如下。

```java
public long getDelay(TimeUnit unit) {
    return unit.convert(time - System.nanoTime(), NANOSECONDS);
}
```

这里的延时时间triggerTime为纳秒，自己设计的时候最好使用纳秒，因为实现getDelay()方法时可以指定任意单位，一旦以秒作为单位，而延时时间又精确不到纳秒就麻烦了。使用时请注意当time小于当前时间单位的时，getDelay会返回负数。

第三步：实现compareTo方法来指定元素的顺序。例如，让延时时间最长的放在队列的末尾。实现代码如下。

```java
public int compareTo(Delayed other) {
    if (other == this) // compare zero if same object
        return 0;
    if (other instanceof ScheduledFutureTask) {
        ScheduledFutureTask<?> x = (ScheduledFutureTask<?>)other;
        long diff = time - x.time;
        if (diff < 0)
            return -1;
        else if (diff > 0)
            return 1;
        else if (sequenceNumber < x.sequenceNumber)
            return -1;
        else
            return 1;
    }
    long diff = getDelay(NANOSECONDS) - other.getDelay(NANOSECONDS);
    return (diff < 0) ? -1 : (diff > 0) ? 1 : 0;
}
```

#### 2. 如何实现延时阻塞队列

延时阻塞队列的实现非常简单，当消费者从队列里面去元素的时候，如果没有元素达到延时时间，就阻塞当前线程。

```java
public E take() throws InterruptedException {
    final ReentrantLock lock = this.lock;
    lock.lockInterruptibly();
    try {
        for (;;) {
            E first = q.peek();
            if (first == null)
                available.await();
            else {
                long delay = first.getDelay(NANOSECONDS);
                if (delay <= 0L)
                    return q.poll();
                first = null; // don't retain ref while waiting
                if (leader != null)
                    available.await();
                else {
                    Thread thisThread = Thread.currentThread();
                    leader = thisThread;
                    try {
                        available.awaitNanos(delay);
                    } finally {
                        if (leader == thisThread)
                            leader = null;
                    }
                }
            }
        }
    } finally {
        if (leader == null && q.peek() != null)
            available.signal();
        lock.unlock();
    }
}
```

代码中的变量leader是一个等待获取队列头部元素的线程。如果leader不等于空，表示已经有线程在等待获取队列的头元素。所以，使用`await()`方法让当前线程等待信号。如果leader等于空，则把当前线程设置成leader，并使用`awaitNanos()`方法让当前线程接收信号或等待delay时间。

一个简单的`DelayQueue`的demo。

```java
@Slf4j
public class DelayQueueExample {

    static class MyDelayedClass implements Delayed {
        private long time;
        private int value;

        public MyDelayedClass(long time, int value) {
            this.time = System.nanoTime() + time;
            this.value = value;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(time - System.nanoTime(), TimeUnit.NANOSECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            if (o == this) {
                return 0;
            }
            if (o instanceof MyDelayedClass) {
                MyDelayedClass x = (MyDelayedClass) o;
                long diff = time - x.time;
                if (diff < 0) {
                    return -1;
                } else if (diff > 0) {
                    return 1;
                } else {
                    return 0;
                }
            }

            long diff = getDelay(TimeUnit.NANOSECONDS) - o.getDelay(TimeUnit.NANOSECONDS);
            return (diff < 0) ? -1 : (diff > 0) ? 1 : 0;
        }
    }

    private static DelayQueue<MyDelayedClass> delayQueue = new DelayQueue<>();

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            delayQueue.put(new MyDelayedClass(TimeUnit.NANOSECONDS.convert((i + 1) * 10, TimeUnit.SECONDS), i));
        }

        for (int i = 0; i < 10; i++) {
            log.info("从DelayQueue中获取元素，时间为：{}", new Date());
            MyDelayedClass take = delayQueue.take();
            log.info("从DelayQueue中获取元素成功，时间为：{}", new Date());
        }
    }
}
```

#### 5. SynchronousQueue

`SynchronousQueue`是一个不存储元素的阻塞队列。每一个put操作必须等待一个take操作，否则不能继续添加元素。

它支持公平访问队列。默认情况下采用非公平性策略访问队列。使用如下构造器可以创建一个公平访问的`SynchronousQueue`，如果设置为true，则等待的线程会采用先进先出的顺序访问队列。

```java
public SynchronousQueue(boolean fair) {
    transferer = fair ? new TransferQueue<E>() : new TransferStack<E>();
}
```

`SynchronousQueue`可以看成一个传球手，负责把生产的线程处理的数据直接传递给消费者线程。队列本身并不存储任何元素，非常适合传递性场景。`SynchronousQueue`的吞吐量高于`LinkedBlockingQueue`和`ArrayBlockingQueue`。

#### 6. LinkedTransferQueue

LinkedTransferQueue是一个由链表结构组成的无界阻塞TransferQueue队列。相对于其他阻塞队列，LinkedQueue多了tryTransfer和transfer方法。

1. transfer方法

    如果当前有消费者正在等待接收元素（消费者使用take()方法或带时间限制的poll()方法时），transfer方法可以把生产者传入的元素立刻transfer（传输）给消费者。如果没有消费者在等待接收元素，transfer方法会将元素放在队列的tail节点，并等待该元素被消费者消费才返回。

2. tryTransfer方法

    tryTransfer方法是用来试探生产者传入的元素是否能直接传给消费者。如果没有消费者等待接收元素，则直接返回flase。和transfer方法的区别是tryTransfer方法无论是消费者是否接收，方法立即返回，而transfer方法是必须等到消费者消费了才返回。

    对于带有时间限制的`tryTransfer(E e, long timeout, TimeUnit unit)`方法，试图把生产者传入的元素直接传给消费者，但是如果没有消费者消费该元素则等待指定的时间在返回，如果超时还没有消费，则返回false，如果在超时时间内消费了该元素，则返回true。

### 7. LinkedBlockingDequeue

LinkedBlockingDequeue是一个由链表结构组成的双向阻塞队列。所谓双向阻塞队列。所谓双向阻塞队列指的是可以从队列的两端插入和移出元素。双向队列因为多了一个操作队列的入口，在多线程同时入队时，也就减少了一半的竞争。相比其他的阻塞队列，`LinkedBlockingDeque`多了`addFirst`、`addLast`、`offerLast`、`peekFirst`、`peekLast`等方法，以First单词结尾的方法，表示插入、获取或移除双端队列的第一个元素。以Last单词结尾的方法，表示插入、获取或移除双端队列的最后一个元素，另外，插入方法`add`等同于`addLast`，移除方法`remove`等效于`removeFirst`。但是`take`方法却等同于`takeFirst`。

在初始化`LinkedBlockingDeque`时可以设置容量防止其过度膨胀。另外，双向阻塞队列可以运用在`工作窃取`模式中。
# ReentantLock 源码分析

ReentrantLock 表示重入锁，重入锁指的是可以重复多次锁定，解锁的时候也需要多次。Synchronized 也是一个重入锁。

>当一个方法已经加锁了，但是这个方法又调用了一个加了同一把锁的方法，如果锁不支持重入，那么就陷入了死锁。

下面的是队列同步容器的一个示例：

```java
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ExampleReentrantLock {
    // request number
    public static final int clientTotal = 5000;
    // 允许并发的线程总数
    public static final int threadTotal = 200;

    public static int count = 0;

    public static final Lock lock = new ReentrantLock();

    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newCachedThreadPool();
        final Semaphore semaphore = new Semaphore(threadTotal);
        final CountDownLatch countDownLatch = new CountDownLatch(clientTotal);
        for (int i = 0; i < clientTotal; i++) {
            executorService.execute(() -> {
                try {
                    semaphore.acquire();
                    add();
                    semaphore.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        executorService.shutdown();
        System.out.println("count: " + count);
    }

    public static void add() {
        lock.lock();
        count++;
        lock.unlock();
    }
}
```

Lock 接口提供了 synchronized 不支持的功能的功能，如下：

1. 非阻塞的获取锁：当前线程尝试获取锁，如果这一时刻锁没有被其他线程的获取到，则成功获取并持有锁。
2. 能被中断的获取锁：与 synchronized 不同，获取到锁的线程能够响应中断，当获取到锁的线程被中断时，中断异常将被抛出，同时锁将被释放。
3. 超时获取锁：在指定使其前获取锁返回 true，否则在指定截止日期后返回 false。

上述的代码中 `Lock` 是一个接口，它定义了获取锁和释放锁的基本操作，`Lock` 的 API 如下：


|方法名称|描述|
|:----|:----|
|`void lock()` |获取锁，调用当前方法会获取锁，当锁获取后，从当前方法返回|
|`void lockInterruptibly()` |可中断的获取锁，和 `lock` 方法不同之处在于该方法会响应中断，即在锁的获取过程中可以中断当前线程|
|`boolean tryLock()` |尝试非阻塞的获取锁，调用该方法后立即返回，如果能获取到锁则返回 true，否则返回 false|
|`boolean tryLock(long time, TimeUnit unit)` |超时获取当前锁，当前线程在以下 3 中情况下会返回：<br/>1. 当前线程在超时时间内获取到锁<br/> 2. 当前线程在超时时间内被中断。<br/> 3. 超时时间结束，返回 false|
|`void unlock()` |释放锁|
|`Condition newCondition()` |获取等待通知组件，该组件和当前锁绑定，当前线程只有获取了锁，才能调用组件的 wait() 方法，而调用之后，当前线程将释放锁|

`ReentrantLock` 有的而 synchronized 不支持的功能：公平锁。`ReentrantLock` 提供了构造函数来设置公平锁或者非公平锁，默认的构造函数提供的是非公平锁，如下：

```java
/**
 * ReentrantLock 默认的是一个非公平的锁
 * This is equivalent to using {@code ReentrantLock(false)}.
 */
public ReentrantLock() {
    sync = new NonfairSync();
}

/**
 * true 表示公平锁
 * true 表示公平锁
 * false 表示非公平锁
 * given fairness policy.
 *
 * @param fair {@code true} if this lock should use a fair ordering policy
 */
public ReentrantLock(boolean fair) {
    sync = fair ? new FairSync() : new NonfairSync();
}
```

下面我们来看它的公平锁和非公平锁的实现，公平锁和非公平锁是基于 `ReentrantLock` 中的 `Sync` 来实现的， 而 `Sync` 又是基于 AQS 实现的。如下：

```java
/**
 * 使用 AQS 实现的公平锁和非公平锁的基类
 * Base of synchronization control for this lock. Subclassed
 * into fair and nonfair versions below. Uses AQS state to
 * represent the number of holds on the lock.
 */
abstract static class Sync extends AbstractQueuedSynchronizer {
    private static final long serialVersionUID = -5179523762034025860L;

    /**
    * 模板方法中的 加锁操作
    * Performs {@link Lock#lock}. The main reason for subclassing
    * is to allow fast path for nonfair version.
    */
    abstract void lock();

    /**
     * 非公平的尝试获取锁 
     * Performs non-fair tryLock.  tryAcquire is implemented in
     * subclasses, but both need nonfair try for trylock method.
     */ 
    final boolean nonfairTryAcquire(int acquires) {
        // 获取当前线程的 Thread
        final Thread current = Thread.currentThread();
        // 获取 aqs 中的 state
        int c = getState();
        // c == 0 独占锁，没有被其他线程获取
        if (c == 0) {
            // 通过 CAS 该便 state 的值来获取锁 （这里会有竞争，有可能失败）
            if (compareAndSetState(0, acquires)) {
                // 如果竞争成功，将当前线程设置为占有锁的线程
                setExclusiveOwnerThread(current);
                return true;
            }
        }
        else if (current == getExclusiveOwnerThread()) {    // 如果当前线程是占有锁的线程
            // 更新状态
            int nextc = c + acquires;
            // 如果小于 0，可能是多次解锁的问题
            if (nextc < 0) // overflow
                throw new Error("Maximum lock count exceeded");
            // 因为当前线程占有的锁，所以不会存在并发
            setState(nextc);
            return true;
        }
        return false;
    }

    protected final boolean tryRelease(int releases) {
        // 将 当前的 state - relaeses    --- 因为支持重入，但是相减后的结果也不能为 0
        int c = getState() - releases;
        // 判断当前线程是否是占用锁的线程
        if (Thread.currentThread() != getExclusiveOwnerThread())
            throw new IllegalMonitorStateException();
        boolean free = false;
        // 如果 c 等于0 表示锁被完全释放了  ----- 这里还是因为重入性
        if (c == 0) {
            free = true;
            // 设置当前独占锁的线程为 null，表示没有被独占
            setExclusiveOwnerThread(null);
        }
        // 设置 state
        setState(c);
        return free;
    }

    // 判断当前线程是否是获取独占锁的线程
    protected final boolean isHeldExclusively() {
        // While we must in general read state before owner,
        // we don't need to do so to check if current thread is owner
        return getExclusiveOwnerThread() == Thread.currentThread();
    }
        
    // 创建一个新的 Condition
    final ConditionObject newCondition() {
        return new ConditionObject();
    }

    // Methods relayed from outer class
    // 获取当前占有锁的线程
    final Thread getOwner() {
        return getState() == 0 ? null : getExclusiveOwnerThread();
    }

    // 获取锁重入的次数
    final int getHoldCount() {
        return isHeldExclusively() ? getState() : 0;
    }

    // 判断是否加锁
    final boolean isLocked() {
        return getState() != 0;
    }

    /**
     * Reconstitutes the instance from a stream (that is, deserializes it).
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        setState(0); // reset to unlocked state
    }
}

/**
 * 非公平锁
 * Sync object for non-fair locks
 */
static final class NonfairSync extends Sync {
    private static final long serialVersionUID = 7316153563782823691L;

    /**
     * 非公平锁的加锁
     * 非公平锁与公平锁的区别就是可以“插队“，在其他线程释放锁的时候，加入此时刚好有一个新的线程获取锁，那么此时这个线程就可以直接获取到锁，而不用排队
     * Performs lock.  Try immediate barge, backing up to normal
      * acquire on failure.
     */
    final void lock() {
        // 这里先尝试通过 CAS 获取设置一下，如果成功就表示获取成功
        if (compareAndSetState(0, 1))
            // 把当前线程设置为独占的线程
            setExclusiveOwnerThread(Thread.currentThread());
        else
            // 如果不行的话就规规矩矩的调用 acquire 获取 state
            acquire(1);
    }

    // 尝试获取资源
    protected final boolean tryAcquire(int acquires) {
        // 非公平的获取资源
        return nonfairTryAcquire(acquires);
    }
}

/**
 * 公平锁
 * Sync object for fair locks
 */
static final class FairSync extends Sync {
    private static final long serialVersionUID = -3000897897090466540L;

    // 公平的获取资源然后加锁
    final void lock() {
        acquire(1);
    }

    /**
     * 尝试获取资源
     * Fair version of tryAcquire.  Don't grant access unless
     * recursive call or no waiters or is first.
     */
    protected final boolean tryAcquire(int acquires) {
        // 获取当前线程
        final Thread current = Thread.currentThread();
        int c = getState();
        // 如果 state == 0 还要看当前线程的节点在 AQS 的队列中是否有前驱，如果有则等待
        if (c == 0) {
            // 如果没有，并且获取成功
            if (!hasQueuedPredecessors() &&
                compareAndSetState(0, acquires)) {
                // 设置独占
                setExclusiveOwnerThread(current);
                return true;
            }
        }
        // 如果当前线程是占有锁的线程，那么就进行 ”重入“
        else if (current == getExclusiveOwnerThread()) {
            int nextc = c + acquires;
            if (nextc < 0)
                throw new Error("Maximum lock count exceeded");
            setState(nextc);
            return true;
        }
        return false;
    }
}
```

这里我们将公平锁和非公平锁的区别之处拿出来看看：

```java
/**
 * 非公平锁的加锁
 * 非公平锁与公平锁的区别就是可以“插队“，在其他线程释放锁的时候，加入此时刚好有一个新的线程获取锁，那么此时这个线程就可以直接获取到锁，而不用排队
 * Performs lock.  Try immediate barge, backing up to normal
 * acquire on failure.
 */
final void lock() {
    // 这里先尝试通过 CAS 获取设置一下，如果成功就表示获取成功
    if (compareAndSetState(0, 1))
        // 把当前线程设置为独占的线程
        setExclusiveOwnerThread(Thread.currentThread());
    else
        // 如果不行的话就规规矩矩的调用 acquire 获取 state
        acquire(1);
}

// 公平的获取资源然后加锁
final void lock() {
    acquire(1);
}
```

可以看到唯一的区别就是非公平锁当 AQS 中的 state 为 0 的时候，直接可以通过 CAS 获取锁成功，从而”插队“成功。

还有就是在 AQS 的模板方法 `tryAcquire()` 中也是有区别的，如下：

```java
// 非公平
// 尝试获取资源
protected final boolean tryAcquire(int acquires) {
    // 非公平的获取资源
    return nonfairTryAcquire(acquires);
}

/**
 * 非公平的尝试获取锁 
 * Performs non-fair tryLock.  tryAcquire is implemented in
 * subclasses, but both need nonfair try for trylock method.
 */ 
final boolean nonfairTryAcquire(int acquires) {
    // 获取当前线程的 Thread
    final Thread current = Thread.currentThread();
    // 获取 aqs 中的 state
    int c = getState();
    // c == 0 独占锁，没有被其他线程获取
    if (c == 0) {
        // 通过 CAS 该便 state 的值来获取锁 （这里会有竞争，有可能失败）
        if (compareAndSetState(0, acquires)) {
            // 如果竞争成功，将当前线程设置为占有锁的线程
            setExclusiveOwnerThread(current);
            return true;
        }
    }
    else if (current == getExclusiveOwnerThread()) {    // 如果当前线程是占有锁的线程
        // 更新状态
        int nextc = c + acquires;
        // 如果小于 0，可能是多次解锁的问题
        if (nextc < 0) // overflow
            throw new Error("Maximum lock count exceeded");
        // 因为当前线程占有的锁，所以不会存在并发
        setState(nextc);
        return true;
    }
    return false;
}
/**
 * 尝试获取资源
 * Fair version of tryAcquire.  Don't grant access unless
 * recursive call or no waiters or is first.
 */
protected final boolean tryAcquire(int acquires) {
    // 获取当前线程
    final Thread current = Thread.currentThread();
    int c = getState();
    // 如果 state == 0 还要看当前线程的节点在 AQS 的队列中是否有前驱，如果有则等待
    if (c == 0) {
        // 如果没有，并且获取成功
        if (!hasQueuedPredecessors() &&
            compareAndSetState(0, acquires)) {
            // 设置独占
            setExclusiveOwnerThread(current);
            return true;
        }
    }
    // 如果当前线程是占有锁的线程，那么就进行 ”重入“
    else if (current == getExclusiveOwnerThread()) {
        int nextc = c + acquires;
        if (nextc < 0)
            throw new Error("Maximum lock count exceeded");
        setState(nextc);
        return true;
    }
    return false;
}
```

非公平的尝试获取锁时，判断如果 state 为 0，获取获取锁的线程是当前拥有锁的线程，那么直接就返回成功，而公平的尝试获取锁的时候会需要判断当前的 AQS 队列中是否有节点，因为公平的获取锁不允许“插队”。

其他的一些主要依靠 AQS 实现。
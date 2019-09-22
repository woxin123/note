# ReentrantLock 和 synchronized 的异同

<!-- TOC -->

- [ReentrantLock 和 synchronized 的异同](#reentrantlock和synchronized的异同)
    - [相同点](#相同点)
    - [区别1: 实现的层面不同](#区别1实现的层面不同)
    - [区别2：ReentrantLock 中支持的而 synchronized 中不支持的功能](#区别2：reentrantlock中支持的而synchronized中不支持的功能)

<!-- /TOC -->

## 相同点

1. 都具有互斥重入的功能。
2. 都可以进行 wait/await 和 notify/singal 进行线程间的协作。

## 区别1: 实现的层面不同

synchronized 是在 JVM 层面利用了 Java 对象头 实现的互斥锁，然后在后面的 jdk 版本中又进行优化。

ReentrantLock 是基于模板方法模式的类 AQS 来实现的一个互斥锁，是在 Java 层面实现的，或者说是 API 层面实现的。

## 区别2：ReentrantLock 中支持的而 synchronized 中不支持的功能

1. 中断：ReentrantLock 提供了支持中断的锁方法：`lockInterruptibly()`，而 synchronized 不支持。
2. 公平锁：ReentantLock 支持公平锁，而 synchronized 不支持公平锁。
3. 多个等待队列：利用 RenntrantLock 可以创建多个 Condition 等待队列，而 synchronized 中只有 ObjectMonitor 一个等待队列。
4. 尝试获取锁和显示等待获取锁：ReentrantLock 支持尝试获取锁，如果没有获取到，那么返回false 的 `tryLock()` 方法，以及限时等待获取锁的 `tryLock(long timeout, TimeUnit unit)` 。

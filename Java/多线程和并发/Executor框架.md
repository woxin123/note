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

主线程首先要创建实现的 Runnable 或者 Callable 接口的任务对象。工具类 Executors 可以把一个 Runnable 对象封装为一个 Callable 对象
# synchronized 关键字

synchronized 是 Java 中用于同步的关键字，利用锁机制实现的同步。Java 中的每一个对象都可以作为锁。

锁机制的两个特性：

1. 互斥性：即在同一时间内只允许一个线程持有某一个对象锁，通过这种机制来实现多线程中的协调机制，这样在同一时间内只能由一个线程对同步代码进行访问。

2. 可见性：必须保证在锁被释放前，对共享变量所做的修改，对随后获得的另一个线程是可见的（即在获得锁时应该获取的是最新共享变量的值）,否则另一个线程可能在本地缓存的某一个副本上继续操作而导致不一致。

synchronized 锁具体表现在以下三种形式：

1. 对于普通同步方法，锁的是当前实例对象，进入同步代码块前需要获当前对象的实例锁。
2. 对于静态同步方法，锁的是当前类的 Class 对象（类锁），在 Java 中每一个类都有一个 Class 对象。
3. 对于同步方法，锁的是当前 synchronized 括号里的对象，这里的对象可以是一个普通的对象，也可能会是一个 Class 对象，如果是 Class 对象的话，那么就是所谓的类锁了，而类锁是通过 Class 对象实现的。

## Synchronized的原理

JVM基于进入和退出Monitor对象来实现同步方法和同步代码块，但两者的实现细节是不一样的。

- 同步代码块是使用`monitorenter`和`monitorexit`指令实现的。
- synchronized 修饰的方法并没有 monitorenter 指令和 monitorexit 指令，取得代之的确实是 ACC_SYNCHRONIZED 标识，该标识指明了该方法是一个同步方法，JVM 通过该 ACC_SYNCHRONIZED 访问标志来辨别一个方法是否声明为同步方法，从而执行相应的同步调用。

`monitorenter`指令是在编译后插入到同步代码块的开始位置，而`monitorexit`是插入到方法结束处和异常处，JVM要保证每个monitorenter必须要与之相对的monitorexit与之配对。**任何一个对象都有一个`monitor`与之关联，且当一个`monitorenter`指令时，将会尝试获取对象所对应的`monitor`的所有权，即尝试获取对象。**

下面来看一个具体的例子：

```java
public class SynchronizedTest {
    
    public void readFile() throws IOException {
        synchronized(this) {
            System.out.println("同步代码块");
        }
    }
}
```

经过javap反编译之后，结果如下：

![](https://img-blog.csdnimg.cn/20181116232154217.png)

可以看出synchronized 同步语句块的实现使用的是 monitorenter 和 monitorexit 指令，其中 monitorenter 指令指向同步代码块的开始位置，monitorexit 指令则指明同步代码块的结束位置。 当执行 monitorenter 指令时，线程试图获取锁也就是获取 monitor(monitor对象存在于每个Java对象的对象头中，synchronized 锁便是通过这种方式获取锁的，也是为什么Java中任意对象可以作为锁的原因) 的持有权.当计数器为0则可以成功获取，获取后将锁计数器设为1也就是加1。相应的在执行 monitorexit 指令后，将锁计数器设为0，表明锁被释放。如果获取对象锁失败，那当前线程就要阻塞等待，直到锁被另外一个线程释放为止。

下面的是同步方法

```java
public class SynchronizedTest {
    public synchronized void readFile() throws IOException {
    	System.out.println("同步代码块");
    }
}
```

反编译之后如下图：

![](https://img-blog.csdnimg.cn/20181116233306387.png)

synchronized 修饰的方法并没有 monitorenter 指令和 monitorexit 指令，取得代之的确实是 ACC_SYNCHRONIZED 标识，该标识指明了该方法是一个同步方法，JVM 通过该 ACC_SYNCHRONIZED 访问标志来辨别一个方法是否声明为同步方法，从而执行相应的同步调用。

## Java对象头

synchronized用的锁是存在Java对象头里。如果对象是数组类型，则虚拟机用3个字节宽（Word）存储在对象头，如果对象是非数组类型，则用2个字节宽存储对象头。在32位虚拟机中，1字节宽等于4字节，即32bit。

|   长度   |          内容          | 说明                             |
| :------: | :--------------------: | -------------------------------- |
| 32/64bit |       Mark Word        | 存储对象的hashCode或者锁信息     |
| 32/64bit | Class Metadata Address | 存储到对象类型数据的指针         |
| 32/32bit |      Array Length      | 数组的长度（如果当前对象是数组） |

在运行期间，Mark Word里存储的数据会随着锁标志的变化而变化。Mark Word可能变化为以下4中数据。

  <table width="667" border="0" cellpadding="0" cellspacing="0" style='width:500.25pt;border-collapse:collapse;table-layout:fixed;'>
   <col width="115" style='mso-width-source:userset;mso-width-alt:3680;'/>
   <col width="97" style='mso-width-source:userset;mso-width-alt:3104;'/>
   <col width="72" span="3" style='width:54.00pt;'/>
   <col width="133" style='mso-width-source:userset;mso-width-alt:4256;'/>
   <col width="106" style='mso-width-source:userset;mso-width-alt:3392;'/>
   <tr height="17" style='height:12.75pt;'>
    <td class="xl65" height="34" width="115" rowspan="2" style='height:25.50pt;width:86.25pt;border-right:none;border-bottom:none;' x:str>锁状态</td>
    <td class="xl65" width="169" colspan="2" style='width:126.75pt;border-right:none;border-bottom:none;' x:str>25bit</td>
    <td class="xl65" width="144" colspan="2" rowspan="2" style='width:108.00pt;border-right:none;border-bottom:none;' x:str>4bit</td>
    <td class="xl66" width="133" style='width:99.75pt;' x:str>1bit</td>
    <td class="xl66" width="106" style='width:79.50pt;' x:str>2bit</td>
   </tr>
   <tr height="17" style='height:12.75pt;'>
    <td class="xl66" x:str>23bit</td>
    <td class="xl66" x:str>2bit</td>
    <td class="xl66" x:str>是否是偏向锁</td>
    <td class="xl66" x:str>锁标志位</td>
   </tr>
   <tr height="17" style='height:12.75pt;'>
    <td class="xl66" height="17" style='height:12.75pt;' x:str>轻量级锁</td>
    <td class="xl65" colspan="5" style='border-right:none;border-bottom:none;' x:str>指向栈中锁记录的指针</td>
    <td class="xl66" x:str>01</td>
   </tr>
   <tr height="17" style='height:12.75pt;'>
    <td class="xl66" height="17" style='height:12.75pt;' x:str>重量级锁</td>
    <td class="xl65" colspan="5" style='border-right:none;border-bottom:none;' x:str>指向互斥量（重量级）锁</td>
    <td class="xl66" x:num>10</td>
   </tr>
   <tr height="17" style='height:12.75pt;'>
    <td class="xl66" height="17" style='height:12.75pt;' x:str>GC标记</td>
    <td class="xl65" colspan="5" style='border-right:none;border-bottom:none;' x:str>空</td>
    <td class="xl66" x:num>11</td>
   </tr>
   <tr height="17" style='height:12.75pt;'>
    <td class="xl66" height="17" style='height:12.75pt;' x:str>偏向锁</td>
    <td class="xl66" x:str>线程ID</td>
    <td class="xl66" x:str>Epoch</td>
    <td class="xl66" colspan="2" style='border-right:none;border-bottom:none;' x:str>对象分代年龄</td>
    <td class="xl66" x:num>1</td>
    <td class="xl66" x:str>01</td>
   </tr>
  </table>

Java对象头里的Mark Word里默认存储对象的HashCode、分代年龄和锁标志记位。JVM的Mark Word默认存储结构如下表所示：

| 锁状态 |      25bit      |    31bit    |   1bit   |   4bit   |  1bit  |   2bit   |
| :----: | :-------------: | :---------: | :------: | :------: | :----: | :------: |
|        |                 |             | cms_free | 分代年龄 | 偏向锁 | 锁标志位 |
|  无锁  |     unused      |  hashCode   |          |          |   0    |    01    |
| 偏向锁 | ThreadId(54bit) | Epoch(2bit) |          |          |   1    |    01    |

  在运行期间，MarkWord里存储的数据会随着锁的标志位的变化而变化。MarkWord可能会变化为以下4种数据，如下表所示![img](https://img-blog.csdn.net/20151217151455512?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQv/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

对象头的最后两位存储了锁的标志为，01是初始状态，为加锁，其对象头里存储的是对象本身的哈希码，随着锁级别的不同，对象头里会存储不同的内容。偏向锁存储的是当前占用此对象的线程ID；而轻量级的锁则存储指向线程栈中锁记录的指针。从这里我们可以看到，“锁”这个东西，可能是个锁记录+对象头里的引用指针（判断线程是否拥有锁时将线程的锁记录地址和对象头里的地址比较），也可能是一个对象头里的线程ID（判断线程是否拥有锁时将线程的ID和对象头里存储的线程ID比较）。

## 对象头中Mark Word和线程中Lock Record

在线程进入同步代码块的时候，如果同步对象没有被锁定，即它的思标志是01，则虚拟机首先在当前线程的栈中创建我们称之为“锁记录（Lock Record）”的空间，用于存储锁对象的Mark Word的拷贝，官方把这个拷贝称之为Displaced Mark Word。这个Mark Word及其拷贝至关重要。

Lock Record是线程私有的数据结构，每一个线程都有一个可用的Lock Record列表，同时还有一个全局可用的列表。每一个被锁住的对象Mark Word都会和一个Lock Record关联（对象头的Mark Word中的Lock Word 指向Lock Record），同时Lock Record中有一个Owner字段存放用于该锁的线程唯一标识（或者object mark word），标识该锁被这个线程占用。如下为Lock Record的内部结构：

| Lock Record | 描述                                                         |
| ----------- | :----------------------------------------------------------- |
| Owner       | 初始时为NULL标识当前没有任何线程拥有该monitor record，当线程拥有该锁后保存线程的唯一标识，当锁被释放的时有被设置为NULL |
| EntryQ      | 关联一个互斥锁(semaphore)，阻塞所有试图锁住monitor record失败的线程 |
| RcThis      | 标识block或waiting在该monitor record上的所有线程的个数       |
| Nest        | 用来实现重入锁的计数                                         |
| HashCode    | 保存从对象头拷贝过来的HashCode值（可能还包括GC age）         |
| Candidate   | 用来避免不必要的组合或等待线程唤醒，因为每一次只有一个线程能够成功拥有锁，如果每次前一个释放锁的线程唤醒所有正在阻塞或等待的线程，会引起不必要的上下文切换（从阻塞到就绪然后因为锁竞争又阻塞）从而导致性能严重下降。**Candidate只有两种可能的值，0表示没有需要唤醒的线程，1表示需要唤醒一个继任线程来竞争。** |

## 监视器（Monitor）

任何一个对象都有一个Minitor与之关联，切当一个Monitor被持有后，它处于被锁定的状态。Synchronized在JVM里的实现都是基于进入和退出Monitor对象来实现对象同步方法和代码块同步，虽然实现细节不同，但是都是通过成对的MonitorEnter和MonitorExit指令来实现。

>1. MonitorEnter指令：插入在同步代码块开始的位置，当代码块执行到该指令时，将会尝试获取该对象Monitor的所有权，即尝试获得该对象的锁
>2. MinitorExit指令：插入在方法结束处和异常处，JVM保证每个MonitorEnter必须对应有MonitorExit

那么什么是Monitor？可以把它理解为一个同步工具，也可以描述为一种同步机制，它通常被描述为一个对象。

与一切皆对象一样，所有的Java对象是天生的Monitor，每一个对象是天生的Monitor，每一个Java对象都有称为Monitor的潜质，因为在Java的设计中，每一个对象自大娘胎里出来就带了一把看不见的锁，它叫做内部锁或者Minitor锁。

也就是通常说的Synchronized的对象锁，Mark Word锁标识为10，其中指针指向的是Monitor对象的其实地址。在HotSpot中，Monitor是又ObjectMonitor实现的，其主要的数据结构如下（位于HotSpot虚拟机源码的ObjectMonitor.hpp文件，C++实现的）：

```c++
ObjectMonitor() {
    _header       = NULL;
    _count        = 0; // 记录个数
    _waiters      = 0,
    _recursions   = 0;
    _object       = NULL;
    _owner        = NULL;
    _WaitSet      = NULL; // 处于wait状态的线程，会被加入到_WaitSet
    _WaitSetLock  = 0 ;
    _Responsible  = NULL ;
    _succ         = NULL ;
    _cxq          = NULL ;
    FreeNext      = NULL ;
    _EntryList    = NULL ; // 处于等待锁block状态的线程，会被加入到该列表
    _SpinFreq     = 0 ;
    _SpinClock    = 0 ;
    OwnerIsThread = 0 ;
}
```

ObjectMonitor中有两个队列，\_WaitSet和\_EntryList，用于保存ObjectWaiter对象列表（每一个等待锁的线程都被封装成了ObjectWaiter对象），_owner指向持有ObjectMonitor对象的线程，当多个线程同时访问一段同步代码的时：

> 1. 首先会进入\_Entry集合，当线程获取到对象的Monitor后，进入\_Owner区域并把monitor中的owner变量设置为当前线程，同时把monitor中的计数器count+1.
> 2. 若线程调用wait()方法，当释放当前线程持有的monitor，owner变量恢复为null，coount自间1，同时该线程进入WaitSet集合中等待被唤醒
> 3. 若当前线程执行完毕，也将释放monitor(锁)并复位count的值，以便其他线程进入获取monitor(锁)

同时，Monitor对象存在与每个Java对象头中的Mark Word中（存储的指针指向），Synchronized锁便是通过这种方式获取锁的，也是为什么Java中任意对象可以作为锁的原因，同时notify、notifyAll和wait等方法会使用到Monitor锁对象，所以必须在代码同步块中使用。

监视器Monitor有两种不同的同步方式，互斥与协作。多线程的环境下线程之间如果需要共享数据，需要解决互斥访问数据的问题，监视器可以确保监视器上数据在同一时刻只有一个线程访问。

什么时候需要协作，比如：

> 一个线程向缓冲区写数据，另一个线程从缓冲区读数据，如果读线程发现缓冲区为空就行等待，当写线程向缓冲区写入数据，就会唤醒读线程，这里读线程和写线程就是一个合作关系。JVM通过Object类的wait方法来使自己等待，在调用wait方法后，该线程就会释放它持有的监视器，直到其他线程通知它，它才会有机会执行。一个线程调用了notify方法通知正在等待的线程，这个线程并不会马上执行，而是通知线程释放监视器后，它重新获取监视器才有执行的机会。如果刚好唤醒的线程需要的监视器被其他线程抢占，那么这个线程就会继续等待。Object类中notifyAll方法可以解决这个问题，它会唤醒所有的线程，总有一个线程执行。

![img](https://user-gold-cdn.xitu.io/2018/7/27/164daccb3b88464e?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)

如上图所示，一个线程通过1号门进入了Entry Set(入口区)，如果入口区没有线程在等待，那么这个线程就会获取监视器成为监视器的Owner，然后执行监视器的代码，如果在入口区有其他线程在等待，那么新进来的线程也会和这些线程一起等待。线程在持有监视器的过程中，有两个选择：

1. 正常执行监视器区域的代码，释放监视器，通过5号门退出监视器
2. 可能等待某一个条件出现，于是它会通过3号门到Wait Set(等待区)休息，知道相应的条件满足后通过4号门重新获取监视器再执行。



## 锁的升级

Java SE1.6为了减少获取锁和释放锁带来的性能消耗，**引入了"偏向锁"和"轻量级锁"，锁一共有4中状态，级别从高到低依次是：无锁状态，偏向锁状态，轻量级锁状态和重量级锁状态，这几个状态会随着竞争情况逐渐升级。锁不但可以升级但不能降级。**

### 1. 偏向锁

HotSpot的作者经过研究发现，在大多数的情况下，锁不仅存在多线程竞争，**而且总是由同一线程多次获得**。为了让线程获得锁的代价更低而引入了偏向锁。当一个线程访问同步代码块并获取锁时，会在对象头和栈帧中锁记录里存储偏向锁的线程ID，以后线程在进入和退出同步块时不需要CAS操作来加锁和解锁，只需要简单测试一下对象头的Mark Word里是否存储着指向当前线程的偏向锁。如果测试成功，表示线程已经获得了锁。如果测试失败，则需要再测试一下Mark Word中偏向锁的标识是否被设置为1（表示当前是偏向锁）；如果没有设置，则使用CAS竞争锁；如果设置了，则尝试使用CAS将对象头的偏向锁指向当前线程。

**偏向锁的撤销**：

偏向锁使用了一种等到竞争才释放锁的机制，也就是说其他线程竞争偏向锁的时候，持有偏向锁的线程才会释放偏向锁。偏向锁的撤销需要等到全局安全点（在这个时间点上没有正在执行的字节码）。它首先暂停拥有偏向锁的线程，然后检查持有偏向锁的线程是否活着，如果线程不处于活动状态，则将偏向锁设置为无状态的锁；如果线程仍然活着，拥有偏向锁的栈会被执行，遍历偏向锁对象的锁记录，栈中的锁记录和对象头的Mark Word要么重新偏向于其他线程，要么恢复到无锁活着标记对象不适合作为偏向锁，最后唤醒暂停的线程。

### 2. 轻量级锁

**(1) 轻量级加锁**

线程在执行同步块之前，**JVM会在当前线程的栈帧中创建用于存储锁记录的空间**，**并将对象头中的Mark Word复制到锁记录中**，官方称之为Displaced Mark Word。然后线程尝试使用CAS将对象头中的Mark Word替换为指向锁记录的指针。如果成功，当前线程获得锁，如果失败，表示其他线程竞争锁，当前线程便尝试使用自旋来获取锁。

**(2) 轻量级解锁**

轻量级解锁时，会使用原子的CAS操作将Displaced Mark Word替换回到对象头，如果成功，则表示没有竞争发生。如果失败，则表示当前锁存在竞争，锁就会膨胀成重量级锁。

因为自旋会消耗CPU，为了避免无用的自旋（比如获取锁的线程被阻塞了），一旦锁升级为重量级锁，就不会再恢复到轻量级锁的状态。当锁处于这个状态下，其他线程获取锁的时，都会被阻塞住，当持有锁的线程释放锁之后会唤醒这些线程，被唤醒的线程就会进入下一轮的夺锁之争。

![img](https://images2015.cnblogs.com/blog/990532/201610/990532-20161017132239982-1213436822.jpg)

## 锁的对比

锁的优缺点对比：

|    锁    | 优点                                                         | 缺点                                          | 使用场景                                 |
| :------: | ------------------------------------------------------------ | --------------------------------------------- | ---------------------------------------- |
|  偏向锁  | 加锁和解锁不需要额外的消耗，和执行非同步方法相比仅存在纳秒级的差距 | 如果线程存在锁竞争，会带来额外的锁撤销的消耗  | 适用于只有一个线程访问同步块场景         |
| 轻量级锁 | 竞争锁的线程不会阻塞，提高了程序的响应速度                   | 如果始终得不到锁竞争的线程，使用自旋会消耗CPU | 追求响应时间，同步代码块的执行速度非常快 |
| 重量级锁 | 线程竞争不使用自旋，不会消耗CPU                              | 线程阻塞，响应时间缓慢                        | 追求吞吐量，同步块执行的速度较长         |


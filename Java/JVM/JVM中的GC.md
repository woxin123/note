# Java 中的GC

<!-- TOC -->

- [Java 中的GC](#java中的gc)
    - [对象已死吗](#对象已死吗)
        - [引用计数法](#引用计数法)
        - [可达性分析算法](#可达性分析算法)
    - [引用](#引用)
        - [生存还是死亡](#生存还是死亡)
        - [回收方法区](#回收方法区)
    - [垃圾收集算法](#垃圾收集算法)
        - [标记清除法](#标记清除法)
        - [复制算法](#复制算法)
        - [标记——整理法](#标记——整理法)
        - [分代收集算法](#分代收集算法)
    - [垃圾回收算法的实现](#垃圾回收算法的实现)
        - [枚举根节点](#枚举根节点)
                    - [好文：](#好文：)
        - [安全点](#安全点)
        - [抢先式中断](#抢先式中断)
        - [主动式中断](#主动式中断)
        - [安全区域](#安全区域)
    - [垃圾收集器](#垃圾收集器)
        - [Serial 收集器](#serial收集器)
    - [ParNew 收集器](#parnew收集器)
        - [Parallel Scavenge 收集器](#parallelscavenge收集器)
        - [Serial Old收集器](#serialold收集器)
        - [Parallel Old收集器](#parallelold收集器)
        - [CMS收集器](#cms收集器)
            - [优点](#优点)
            - [缺点](#缺点)
        - [G1收集器](#g1收集器)
    - [内存分配与回收策略](#内存分配与回收策略)
        - [对象优先在 Eden 分配](#对象优先在eden分配)
        - [大对象直接进入老年代](#大对象直接进入老年代)
        - [长期存货的对象将进入老年代](#长期存货的对象将进入老年代)
        - [对象动态年龄判断](#对象动态年龄判断)
        - [空间分配担保](#空间分配担保)

<!-- /TOC -->

垃圾收集（Garbage Collection, GC）需要完成那几件事呢？垃圾回收需要完成下面的三件事：

1. 那些内存需要被回收？
2. 什么时候回收？
3. 如何回收？

## 对象已死吗

在 Java 中哪些内存需要被回收？当然是哪些已经无用的变量和数据。所以一般需要被回收的是死亡的对象。那么如何判断一个对象是否死亡呢？

### 引用计数法

给对象添加一个引用计数器，每当一个对象被引用它时，计数器就加1，当引用失效时，计数器值就减1；任何时刻计数器为0的对象就不能被使用。

Java 中没有采用引用计数发的原因是引用计数法很难解决对象之间的循环引用。

下面的是一个循环引用的示例：

```java
/**
 * testGC() 方法执行后，objA 和 objB 会不会被 GC
 */
public class ReferenceCountingGC {
    
    public Object instance = null;
    /**
     * 这个成员的唯一意义就是占用一点内存，以便能在 GC 日志中请求地看到是否被回收过
     */
    private static int _1MB = 1024 * 1024;

    public static void testGC() {
        ReferenceCountingGC objA = new ReferenceCountingGC();
        ReferenceCountingGC objB = new ReferenceCountingGC();

        objA.instance = objB;
        objB.instance = objB;

        objA = null;
        objB = null;

        // 假设在这发生 GC，objA 和 objB 是否会被收回
        System.gc();
    }
}
```

在这里循环引用肯定是会被回收的。

### 可达性分析算法

当前的语言基本都是使用的是可达性分析算法来判断对象是否存活。**这个算法的基本思想是通过一系列的称为“GC Roots”的对象作为起始点，从这些节点开始向下搜索，搜索所走过的路径称为引用链(Reference Chain)，当一个对象到GC Roots没有任何引用链（用图论的话来说，就是从GC Roots 到这个对象不可达）时，则证明这个对象不可用**。

Java语言中的，可作为GC Roots的对象包括下面几种：

+ 虚拟机栈（栈中的本地变量表中的应用对象）中引用的对象。
+ 方法区中类的静态属性应用的对象。
+ 方法区中常量应用的对象。
+ 本地方法中JNI（即一般说的Native方法）引用的对象。

## 引用

JDK1.2以前，Java中引用的定义：如果reference类型的数据中存储的数值代表的是另一个块内存的起始地址，就称这块内存代表着一个引用。
在JDK1.2之后，Java对引用的概念进行了扩充，分为：
强引用、软引用、弱引用、虚引用。

+ 强引用：强引用就是在程序代码中普遍存在的 ，类似`Object obj = new Object()`这类的引用，只要强引用还存在，垃圾回收器就永远不会回收掉被引用的对象。
+ 软引用：是用来描述那些还有用但非必须的对象，对于软引用关联的对象，在系统将要发生内存溢出之前，将会把致谢对象列进回收的范围之中进行第二次回收，如果这次回收还没有足够的内存，才会抛出异常。在JDK1.2之后，提供了SoftReference类来实现软引用。软引用用来实现内存敏感的高速缓存，比如：网页缓存、图片缓存等。
+ 弱引用：也是用来描述非必须对象的，但是它的强度比软引用更弱一些，被弱引用关联的对象只能生存在下一次垃圾收集发生之前。当垃圾收集器工作时，无论内存是否足够，都会被回收掉只会被弱引用关联的对象。在JDK1.2之后，提供了WeakReference类来实现弱引用。
+ 需引用：虚引用也被称为幽灵引用或者幻影引用，它是最弱的一种引用关系。一个对象是否有虚引用的存在，完全不会对其生存构成影响，也无法通过虚引用来获取一个对象实例。作为一个对象设置虚引用关联的唯一的目的就是能在这个对象被垃圾收集器回收时收到一个系统通知。在JDK1.2之后提供了PlantomReference类来实现虚引用。

弱引用举例：

```java
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class WeakReferenceExample {
    public static void main (String[] args) {
    List<Integer> list = new ArrayList<>();
       WeakReference<List<Integer>> weakReference = new WeakReference<>(list);
       for (int i = 0; i < 10; i++) {
           list.add(i);
       }
       System.out.println(weakReference.get());
       list = null;
       while (true) {
           if (weakReference.get() != null) {
               System.gc();
           } else {
               break;
           }
       }
       System.out.println("弱引用已经被回收");
    }
}
```

### 生存还是死亡

可达性分析算法中一个对象的”死亡“，至少要经过两次标记的过程；**如果对象在经过可达性分析算法后没有与GC Roots相连的引用链，那么它将会被第一次标记并且进行一轮筛选，筛选的条件是此对象是否有必要执行finalize()方法**。当对象没有覆盖finalize()方法，或者finalze()方法已经被虚拟机调用过，虚拟机将这两种情况都视为”没有必要执行“。

如果这个对象被判定为有必要执行finalize()方法，那么这个对象将被放置在**一个叫F-Queue的队列中**，并在稍后有一个有虚拟机自动建立，**优先级低的Finalizer线程去执行它**，这里所谓的”执行“是指虚拟机会触发这个方法，但并不承诺会等待它运行结束。这样做的原因是，如果一个对象在finalize()方法中执行缓慢，或者发生了死循环，将很可能导致F-Queue队列中的其它对象永远处于等待，甚至导致整个内存回收崩溃。

**finalize()方法是对象逃脱死亡命运的最后一次机会**，稍后GC将对F-Queue中的对象进行第二次小规模标记，如果对象要在finalze()中成功拯救自己——只要成功与任何一个对象获得关联即可，那么在第二次标记的时，他将被移出”即将回收“的集合；如果对象这时候还没有逃脱，那么基本上就可以被收回了。

这里为什么需要两次标记，是因为可以让对象有一次逃脱的机会。

```java
public class FinalizeEscapeGC {

    public static FinalizeEscapeGC SAVE_HOOK = null;

    public void isAlive() {
        System.out.println("yes, I'm still alive");
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        System.out.println("finalize method executed!");
        FinalizeEscapeGC.SAVE_HOOK = this;
    }

    public static void main(String[] args) throws InterruptedException {
        SAVE_HOOK = new FinalizeEscapeGC();

        // 第一次成功拯救自己
        SAVE_HOOK = null;
        System.gc();
        // 因为finalize发方法的优先级很低，所以暂停0.5秒等待它
        Thread.sleep(500);
        if (SAVE_HOOK != null) {
            SAVE_HOOK.isAlive();
        } else {
            System.out.println("No, I'm dead!");
        }

        // 下面的这段代码和上面的一样，但却拯救自己失败了
        SAVE_HOOK = null;
        System.gc();
        // 因为finalize发方法的优先级很低，所以暂停0.5秒等待它
        Thread.sleep(500);
        if (SAVE_HOOK != null) {
            SAVE_HOOK.isAlive();
        } else {
            System.out.println("No, I'm dead!");
        }
    }

}
```
输入结果:

>finalize method executed!
>
>yes, I'm still alive
>
>No, I'm dead!

上面的代码中有两端完全一样的代码片段，执行结果确实一次成功，一次失败，这是因为任何一个finalize()方法都只能被系统自动调用一次。

### 回收方法区

Java虚拟机规范说过可以不要求虚拟机在方法去实现垃圾收集，而且在方法区中进行了垃圾收集的“性价比”一般比较低：在堆中，尤其是新生代中，常规应用一次垃圾收集一般可以回收70%~95%的空间，而方法去的垃圾收集效率远低于此。

方法区中的垃圾收集主要回收两部分：

+ 废弃常量
+ 无用的类

回收废弃的常量和回收Java堆中的对象非常类似，就是当没有一个对象持有这个常量时，这个常量就会被回收。

判断一个类是无用的类比较苛刻，必须满足下面的三个要求：

1. 该类的所有类实例都被回收，也就是收Java堆中不存该类的任何实例。
2. 加载该类的ClassLoader已经被收回。
3. 该类对应的java.lang.Class对象没有任何地方被引用，无法在任何地方通过反射访问该类的方法。

虚拟机对满足以上3个条件的无用的类进行回收，这里说的仅仅是可愚蠢，而并不是和对象一样，不使用了仅必然回收。是否对类进行回收，Hotspot提供了一些参数来进行控制。

在大量使用反射、动态代理、CGLib等byteCode框架、动态生成JSP以及OSGI这类频繁自定义ClassLoader的场景都需要虚拟机具备类卸载的功能，以保证方法去不会溢出。

## 垃圾收集算法

###  标记清除法

最基本的算法是”标记-清除“（Mark-Sweep）算法，算法分为两个阶段：”标记“，”清除“。首先标记所有需要回收的对象，在标记完成后统一回收被标记的对象。

有2个不足：

1. 效率问题，标记和清除两个过程效率都不高
2. 第二个是空间问题，标记清除后会产生大量不连续的碎片，空间碎片太多可能会导致以后在程序运行过程中需要分配较大对象时，无法找到足够的连续内存而不得不提前触发另一次垃圾收集的动作。

### 复制算法

将可用内存按大小划分为两块，每次使用其中的一块，当其中的一块使用完了，就将存活的的对象复制到另一块内存上面，然后再把已经使用过的内存空间一次性清理掉。这样使得每一次都是对整个半区的内存就行回收，内存分配时也就不用考虑内存碎片的问题了。只要移动堆顶的指针，按顺序分配内存即可。实现简单，运行效率高，但是这种算法的代价是将内存缩小到原来的一半。

现在的商业虚拟机都采用的是这种收集算法来回收新生代。

>+ Eden Space(伊甸园)
>+ Survivor Space(幸存者区)
>+ Old Gen(老年代)

Eden Space字面意思是伊甸园，对象被创建的时候首项被放在这个区域，进行垃圾回收后，不能被回收的对象被放到空的Survivor区域。

Survivor Space幸存者去，用于保存在eden space内存区域中经过垃圾回收后没有被收回的对象。Survivor有两个，分别为To Survivor、From Survivor，这两个区域的空间大小是一样的。执行垃圾回收的时候Eden区域不能被收回的对象被放到空的survivor(也就是To Survivor，同时Eden区域的内存会在垃圾回收的过程中全部释放)，另一个survivor（即From Survivor）里不能被回收的对象也会被放入这个survivor(即To Survivor)，然后To Survivor和From Survivor的标记会互换，始终保持一个survivor是空的。

![](https://img-blog.csdn.net/20160920101202448)

也就是说每一次使用Eden和From Survivor Space，然后垃圾回收的时候将有用的放在To Survivor中，之后把Eden和From Survivor一起清理掉。

HotSpot虚拟机中默认Eden和Survivor的大小比例是8:1，也就是每一次新生代中可用内存空间为整个新生代容量的90%(80% + 10%)，只有10%的内存会被“浪费”。当Survivor空间不够使用的时，需要依赖其他内存（这里指的是老年代）进行**分配担保** （Handle Promotion）。

**好文**

1. https://blog.csdn.net/shiyong1949/article/details/52585256

### 标记——整理法

根据老年代的特定，有人提出了另外的一种“标记——整理”（Mark-Compact）算法，标记过程仍然与“标记——清除”算法一样，但后续步骤不是直接对可回收对象进行整理，而是让所有存活的对象都向一段移动，然后直接清理掉段边界以外的内存，

### 分代收集算法

当前商用虚拟机的垃圾收集都采用“分代收集”算法，这种算法的思想是根据对象存活周期不同将内存划分为几块。一般是把Java堆分为新生代和老年代。

+ 新生代中，每一次垃圾回收时都有大批对象死去，只有少量存活，那么就选用复制算法，只需要付出少量存活对象的复制成本就可以完成收集。
+ 在老年代中对象的存活率高、没有额外空间对它进行分配担保，就必须使用“标记——清理”或者“标记——整理”算法来进行回收。

## 垃圾回收算法的实现

### 枚举根节点

可作为GCRoots的节点主要在全局的引用（例如常量或者静态属性）与执行上下文（例如栈帧中的本地变量表）中。

两个与时间有关的因素：

1. 现在很多应用的方法区就有数百兆，如果要逐个检查里面的引用，那么必然会消耗很多时间。
2. 可达性分析对执行时间的敏感还体现在GC停顿上，这项分析工作必须在一致性快照中进行——这里的“一致性”的意思是指整个分析期间整个系统被冻结在某个时间点上，不可以出现分析的过程中引用的关系还在不停的变化的情况，改点不满足的话分析的结果就无法保证正确性。这点是导致GC进行时必须停顿所有Java执行线程（Sun将这些事件称之为“Stop The World”）的其中一个重要原因，即使在号称几乎不会停顿的CMS收集器中，枚举根节点也是必须停顿的。

准确式GC和保守式GC

准确式(precise或exact)GC指GC能够知道一块内存区域是引用还是非引用，如一个32位的区域可以是一个int整形数字也可以是一个对象引用。当一个对象进行执行时，需要修改指向这个内存的引用的指，非准确式GC也就是保守式GC不能完成这个任务。

由于目前的Java虚拟机使用的都是准确式GC，所以当系统停顿下来后，并不需要一个不漏地检查完所有执行上下文和全局引用的所在位置，虚拟机还是有办法知道那些地方存储着对象的引用。

在HotSpot的实现中，是使用一组称为OopMap的数据结构来达到这个目的的，在类加载的时候，HotSpot就把对象是什么类型计算出来，在JIT编译过程中，也会在特定的位置记录下来栈和寄存器中哪些位置是引用，这样，GC扫描的时就可以直接得知这些信息。

###### 好文：

1. OOpMap https://blog.csdn.net/woaigaolaoshi/article/details/51439227

### 安全点

如果为每一条指令都生成一个OopMap，这样GC的成本就会很高。

实际上，HotSpot也没有为每一条指令都生成OopMap，只是在“特点的位置”记录这些信息，这些位置称之为安全点（Safepoint），也就是说程序不会在每一个位置都停下来开始GC，只有在到达安全点的时候开始暂停。

Safepoint的选定既不能太少以至于让GC等待的时间太长，也不能过于频繁以至于增大运行时的负荷。所以，安全点的选定基本上都是以“是否具有让程序长时间执行的特征“为标准进行选定——因为每一条指令的执行都非常的短暂，程序不太可能因为指令流太长这个原因而过长时间运行，”长时间的执行“最明显的特征就是指令序列的复用，例如方法调用，循环跳转、异常跳转等，所以具有这些功能的指令才会产生Safepoint。这里主要是为了防止循环或者其他的操作执行的时间过长而导致一直无法进入 GC。

对于Safepoint，另外一个需要考虑的问题就是如何在GC发生时让所有的线程（这里不包括执行JNI调用的线程）都跑在安全点上再停顿下来。这里有两种方案可供选择：

1. 抢先式中断（Preemptive Suspension）
2. 主动式中断（Voluntary Suspension）

### 抢先式中断

抢先式中断不需要线程的执行代码主动配合，在GC发生时，首先吧所有的线程先全部中断，如果发现有线程中断的地方不在安全点上，就恢复线程，让它”跑“在安全点上。现在基本上没有虚拟机采用抢先式中断来暂停线程从而响应GC。

### 主动式中断

而主动式中断的思想是当GC需要中断线程的时候，不直接对线程进行操作，仅仅简单的设置一个标志，各个线程执行时主动轮询这个标志，发现中断标志为真的时候就把自己中断挂起。轮询标志的地方和中断点是重合的，另外在加上创建线程需要分配内存的地方。

### 安全区域

当线程处于Sleep状态或者Blocked状态，这时候线程无法响应JVM的中断请求，”走“到安全的地方去中断挂起，JVM也显然不太可能等待线程重新分配CPU时间。对于这种情况，就需要安全区域来解决。

安全区域是指在一段代码片段之中，引用的关系不会发生太大的变化。在这个区域中的任何地方开始GC都是安全的。也可以把Safe Region看做是被扩展了的Safepoint。

当线程执行到Safe Region中的代码时，首先标识自己已经进入了Safe Region，那样，当在这段时间JVM要发起GC时，就不同管自己为Safe Region状态的线程了。在线程要离开Safe Region时，跟节点的枚举（或者是整个GC的过程），如果完成了，那么线程就继续执行，否则它就必须等待直到收到可以安全离开Safe Region的信号为止。

## 垃圾收集器

### Serial 收集器

Serial 收集器是最基本、发展历史最悠久的收集器，它是一个单线程收集器。
Serial在进行垃圾收集的时候，必须暂停其他所有工作的线程，知道它收集结束。停止其他工作线程这一步是在虚拟机后台自动发起和完成的，在用户不可见的情况下把用户的工作线程停掉。

![](https://pic.yupoo.com/crowhawk/6b90388c/6c281cf0.png)

+ Serial对于新生代采用的复制算法
+ Serial对于老年代采用的是标记——整理

## ParNew 收集器

ParNew收集器就是Serial收集器的多线程版本，除了使用多线程进行垃圾收集之外，其余行为包括Serial收集器可用的所有控制参数（例如：-XX:SurvivorRation、-XX:PretenureSizeThreshold、-XX:HandlePromotionFailure等）、收集算法、Stop The World对象分配规则、回收策略都与Serial收集器一样，在实现上，这两个收集器也共用了相当多的代码。ParNew收集器的工作如图所示：
![](https://pic.yupoo.com/crowhawk/605f57b5/75122b84.png)

ParNew 收集器有一个最大的特点就是目前可以和CMS搭配的只有它。

ParNew默认开启的线程与CPU的数量相等。可以使用-XX:ParallelGCThreads参数量限制垃圾回收器的线程数。

### Parallel Scavenge 收集器

Parallel Scavenge 收集器是一个新生代收集器，它也是使用复制算法的收集器，又是并行的线程收集器。

Parallel Scavenge 的目的是达到一个可控的吞吐量（Throughput）。所谓吞吐量就是CPU用于运行代码的时间与CPU总消耗时间的比例，即

```math
\text{吞吐量} =\frac{\text{运行用户代码的时间}} {\text{运行用户代码的时间}+\text{垃圾收集的时间}}
```

Parallel Scavenge收集器提供了两个参数用于精确控制吞吐量，分别是控制最大垃圾收集停顿时间-XX:MaxGCPauseMillis参数以及直接设置吞吐量大小的-XX:GCTimeRatio参数。 

Parallel Scavenge收集器除了会显而易见地提供可以精确控制吞吐量的参数，还提供了一个参数-XX:+UseAdaptiveSizePolicy，这是一个开关参数，打开参数后，就不需要手工指定新生代的大小（-Xmn）、Eden和Survivor区的比例（-XX:SurvivorRatio）、晋升老年代对象年龄（-XX:PretenureSizeThreshold）等细节参数了，虚拟机会根据当前系统的运行情况收集性能监控信息，动态调整这些参数以提供最合适的停顿时间或者最大的吞吐量，这种方式称为GC自适应的调节策略（GC Ergonomics）。自适应调节策略也是Parallel Scavenge收集器与ParNew收集器的一个重要区别。

### Serial Old收集器

Serial Old是Serial收集器的老年代版本，它同样是一个**单线程的收集器**，使用”**标记——整理**“（Mark-Compact）算法。

此收集器的作用主要是用于Client模式下的虚拟机使用。如果在Server模式下，它还有量大用途：

+ 在JDK1.5以及之前的版本（Parallel Old诞生之前）与Parallel Scavenge收集器搭配使用。
+ 作为CMS收集器的后台预案，在并发收集发生Concurrent Mode Failure时使用。

![](https://pic.yupoo.com/crowhawk/6b90388c/6c281cf0.png)

### Parallel Old收集器

Parallel Old收集器是Parallel Scanvenge收集器的老年代版本，使用**多线程**和**标记——整理**算法。前面已经提到过，这个收集器是在JDK1.6中才开始提供的，在此之前，如果新生代选择了Parallel Scavenge收集器，老年代除了Serial Old以外别无选择，所以在Parallel Old诞生以后，”吞吐量优先“收集器终于由于名副其实的组合，在注重吞吐量以及CPU资源敏感的场合，都可以优先考虑Parallel Scavenge和Parallel Old收集器。Parallel Old收集器的工作流程与Parallel Scavenge相同，这里给出Parallel Scavenge/Parallel Old收集器配合使用的流程图：

![](https://pic.yupoo.com/crowhawk/9a6b1249/b1800d45.png)

### CMS收集器

CMS(Concurrent Mark Sweep)收集器是一种以**最短时间回收停顿时间**为目标的收集器。

CMS是”标记——清除“算法实现的收集器。它的运作过程比前面的几个来说更复杂。整个过程分为如下4个步骤：

1. 初始标记（CMS initial mark）
2. 并发标记（CMS concurrent mark）
3. 重新标记（CMS remark）
4. 并发清除（CMS concurrent sweep）

其中，初始标记、重新标记这两个步骤仍然需要”Stop The World“。初始标记仅仅只是标记一下GC Roots能直接关联到的对象，速度很快，并发阶段就是进行GC Roots Tracing的过程，而重新标记阶段则是为了修正并发标记期间因用户程序继续运作而导致标记产生变动的那一部分对象标记记录，这个阶段的标记时间一般比初始标记阶段长一些，但远比并发标记短。

>tracing gc的基本思路是，以当前存活的对象集为root，遍历出他们（引用）关联的所有对象（Heap中的对象），没有遍历到的对象即为非存活对象，这部分对象可以gc掉。这里的初始存活对象集就是GC Roots。

由于整个过程中耗时最长的并发标记和并发清除过程收集器线程都可以与用户线程一起工作，所以，总体上来说，CMS收集器的内存回收的过程是与用户线程一起并发的执行。通过下图可以比较清除地看到CMS收集器的运作步骤中并发需要停顿的时间。

![](https://pic.yupoo.com/crowhawk/fffcf9a2/f60599b2.png)

#### 优点

CMS是一款优秀的收集器，它的主要优点在名字上已经体现出来了：并发收集、低停顿，因此CMS收集器也被称为并发低停顿收集器（Concurrent Low Pause Collector）。

#### 缺点

+ 对CPU资源非常敏感 其实，面向并发设计的程序都对CPU资源比较敏感。在并发阶段，它虽然不会导致用户线程停顿，但会因为占用了一部分线程（或者说CPU资源）而导致应用程序变慢，总吞吐量会降低。CMS默认启动的回收线程数是（CPU数量+3）/4，也就是当CPU在4个以上时，并发回收时垃圾收集线程不少于25%的CPU资源，并且随着CPU数量的增加而下降。但是当CPU不足4个时（比如2个），CMS对用户程序的影响就可能变得很大，如果本来CPU负载就比较大，还要分出一半的运算能力去执行收集器线程，就可能导致用户程序的执行速度忽然降低了50%，其实也让人无法接受。
+ 无法处理浮动垃圾（Floating Garbage） 可能出现“Concurrent Mode Failure”失败而导致另一次Full GC的产生。由于CMS并发清理阶段用户线程还在运行着，伴随程序运行自然就还会有新的垃圾不断产生。这一部分垃圾出现在标记过程之后，CMS无法再当次收集中处理掉它们，只好留待下一次GC时再清理掉。这一部分垃圾就被称为“浮动垃圾”。也是由于在垃圾收集阶段用户线程还需要运行，那也就还需要预留有足够的内存空间给用户线程使用，因此CMS收集器不能像其他收集器那样等到老年代几乎完全被填满了再进行收集，需要预留一部分空间提供并发收集时的程序运作使用。
+ 标记-清除算法导致的空间碎片 CMS是一款基于“标记-清除”算法实现的收集器，这意味着收集结束时会有大量空间碎片产生。空间碎片过多时，将会给大对象分配带来很大麻烦，往往出现老年代空间剩余，但无法找到足够大连续空间来分配当前对象。

### G1收集器

G1（Garbage-First）收集器是当今收集器技术发展最前沿的成果之一，它是一款面向服务端应用的垃圾收集器，HotSpot开发团队赋予它的使命是（在比较长期的）未来可以替换掉JDK 1.5中发布的CMS收集器。与其他GC收集器相比，G1具备如下特点：

+ 并发与并行：G1能充分利用多CPU、多核环境下的硬件优势，使用多个CPU来缩短“Stop the World”停顿时间，部分其他收集器原本需要停顿Java线程执行GC动作，G1收集器仍然可以通过并发的方式让Java程序继续执行。
+ 分代收集：和其他收集器一样，分代概念在G1中仍然得到保留。虽然G1可以不需要其它收集器的配合就能独立管理整个GC堆，但它能够采用不同的方式取处理新创建的对象和已经存货一段时间、熬过多次GC的旧对象以获取更好的收集效果。
+ 空间整合：与CMS的“标记——清理”算法不同，**G1从整体上看是基于“标记——整理”算法实现的收集器，从局部（两个Region之间）上看是基于“复制”算法实现的。**但是无论如何，这两个算法都意味着G1运行期间不会产生内存碎片，收集后能够提规整的可用内存。这种特性有利于程序长时间运行，分配大对象时不会因为无法找到连续的内存空间而提前触发下一次GC。
+ 可预测的停顿：这是G1相对于CMS的另一大优势，降低停顿时间是G1和CMS的共同关注点，但G1除了追求低停顿外，还能建立可预测的停顿的时间模型，能让使用者明确指定在一个M毫秒的时间片段内，消耗在垃圾手机上的时间不得超过N毫秒。

在G1之前的其他收集的范围都是整个新生代或者老年代，而G1不再是这样。使用G1收集器时，Java堆的内存布局就与其他收集器有很大差别，它将整个Java堆规划分为大小相等的独立区域（Region）,虽然还保留有新生代和老年代的概念，但新生代和老年代不再是物理隔离，它们都是一部分的Region(不需要连续)的集合。

G1收集器之所以能建立可预测的停顿的时间模型，是因为它可以有计划的避免在整个Java堆中进行安全域的垃圾收集。G1跟踪各个Region里面的垃圾堆积价值大小（回收锁获得的空间以及回收所需要时间的经验值），在后台维护一个优先列表，每次根据允许的收集时间，优先回收价值最大的Region(这也就是Garbage-First名称的由来)。这种使用Region划分内存空间以及有优先级的区域回收方式，保证了G1收集器在优先的时间内可以获取尽可能高的收集效率。

在G1收集器中，Region之间对象的互相引用以及其他收集器中的新生代和老年代之间的对象引用，虚拟机都是使用Remembered Set来避免堆扫描。G1中的每一个Region都有一个与之相对的Remembered Set，虚拟机发现程序在对Reference类型数据进行写操作时，会产生一个Write Barrier暂时中断写操作，检查Reference引用的对象是否处于不同的Region之中（在分代的例子中就是检查是否老年代中的对象引用了新生代中的对象），如果是，便通知CardTable把相关信息记录到被引用对象所属的Region的Remenbered Set之中。当进行内存回收的时，在GC根节点的枚举范围中加入Remembered Set即可保证不对全堆进行完全扫描也不会有遗漏。

如果不计算维护Remembered Set操作，G1收集器的运作大致可以划分为一下的几个步骤：

+ 初始标记(Initial Marking): 仅仅只是标记一下GC Roots 能直接关联到的对象，并且修改TAMS（Nest Top Mark Start）的值，让下一阶段用户程序并发运行时，能在正确可以的Region中创建对象，此阶段需要停顿线程，但耗时很短。
+ 并发标记（Concurrent Marking）： 从GC Root 开始对堆中对象进行可达性分析，找到存活对象，此阶段耗时较长，但可与用户程序并发执行。
+ 最终标记（Final Marking）：为了修正在并发标记期间因用户程序继续运作而导致标记产生变动的那一部分标记记录，虚拟机将这段时间对象变化记录在线程的Remembered Set Logs里面，最终标记阶段需要把Remembered Set Logs的数据合并到Remembered Set中，这阶段需要停顿线程，但是可并行执行。
+ 筛选收回（Live Data Counting and Evacuation）： 首先对各个Region中的回收价值和成本进行排序，根据用户所期望的GC 停顿是时间来制定回收计划。此阶段其实也可以做到与用户程序一起并发执行，但是因为只回收一部分Region，时间是用户可控制的，而且停顿用户线程将大幅度提高收集效率。

通过下图可以比较清楚地看到G1收集器的运作步骤中并发和需要停顿的阶段（Safepoint处）：

![](http://pic.yupoo.com/crowhawk/53b7a589/0bce1667.png)

|收集器|串行、并行or并发|新生代/老年代|算法|目标|适用场景|
|-----|---------------|----|-----|----|----|
|Serial|串行|新生代|复制算法|响应速度优先|单CPU环境下的Client模式|
|Serial Old|串行|老年代|标记-整理|响应速度优先|	单CPU环境下的Client模式、CMS的后备预案|
|ParNew|并行|新生代|复制算法|响应速度优先|	多CPU环境时在Server模式下与CMS配合|
|Parallel Scavenge|并行|新生代|复制算法|吞吐量优先|	在后台运算而不需要太多交互的任务|
Parallel Old|并行|老年代|标记-整理|吞吐量优先|	在后台运算而不需要太多交互的任务|
|CMS|并发|老年代|标记-清除|响应速度优先|	集中在互联网站或B/S系统服务端上的Java应用|
G1|并发|both|标记-整理+复制算法|响应速度优先|	面向服务端应用，将来替换CMS|

## 内存分配与回收策略

### 对象优先在 Eden 分配

这个很好理解，一般刚创建的对象被认为是新生代的对象，所以优先分配在 Eden。

### 大对象直接进入老年代

所谓的大对象是指，需要大量连续内存空间的 Java 对象，最典型的大对象就是那个很长的字符串以及数组。

虚拟机提供了一个 `-XX:PretenureSizeThread 参数，另大于这个设置的对象直接在老年代分配。这里做的目的是为了避免在 Eden 以及两个 Survivor 区之间发生大量的内存复制。

### 长期存货的对象将进入老年代

既然虚拟机采用了分代收集的思想，那么内存回收时就必须能意识到哪些对象应该放在新生代，哪些对象应该放在老年代。为了做到这一点，虚拟机给每一个对象定义了一个对象年龄（Age）。如果对象在 Eden 出生并经过第一次 Minor GC 后仍然存活，并且能被 Survivor 容纳的话，将被移动到 Survivor 空间中，并且对象的年龄就增加了 1 岁，当它的年龄增加到一定的程序（默认为15），将晋级到老年代。对象晋升老年代的阈值，可以通过参数 `-XX:MaxTenuringThreshold 设置。

### 对象动态年龄判断

为了能更好地适应不同程序的内存状况。虚拟机并不是永远地要求对象的年龄必须达到了 `MaxTenuringThreshold` 才能晋升老年代，**如果在 Survivor 空间中相同年龄所有对象大小的总和大于 Survivor 空间的一半，年龄大于或等于该年龄的对象就可以直接进入老年代，无需等到 `MaxTenuringThreshold` 中要求的年龄。**

### 空间分配担保

在发生 Minor GC 之前，虚拟机会先检查老年代的最大可用的连续内存是否大于新生代的所有对象的总空间，如果这个条件成立，那么 Minor GC 可以确保是安全的。如果不成立，那么 Minor GC 会查看是否设置允许担保失败。如果允许，那么会继续检查老年代的最大可用空间是否大于历次晋升到老年代的平均大小，如果大于，将尝试进行一次 Minor GC，尽管这次 GC 是有风险的；如果小于，或者设置不允许，那这时也要该为进行一个 Full GC。

# volatile 详解

<!-- TOC -->

- [volatile 详解](#volatile详解)
    - [volatile 特性](#volatile特性)

<!-- /TOC -->

volatile 是 Java 提供的一种弱同步机制，当一个变量被 volatile 修饰后，编译器和 JVM 将不会将该变量的操作其他内存操作进行重排序，并且保证了这个变量对于线程的可见性。

## volatile 特性

1. 可见性：当一条线程对 volatile 变量进行了修改操作时，其他线程能立即直到修改了该变量的值，也就是说当读取一个 volatile 变量时总会返回它最新修改的值。
2. 有序性：被 volatile 修饰的变量，会在运行和编译的过程中保证这个变量而的有序性，也就是会禁止指令重排序的优化。


# Golang 面试题

### 1. Go 的调度

#### 1.1 可增长的栈

每个 OS 线程都有一个固定大小的栈（通常为 2MB），栈内存区域保存在其他函数调用期间那些正在执行或临时暂停的局部变量。这个固定大小即太大又太小。对于一个小的 goroutine 面，2MB 的栈是一个巨大的浪费，比如有的 goroutine 仅仅等待一个 waitGroup 再关闭一个通道。在 Go 程序中，一次性创建十万左右的 goroutine 也不罕见，对于这种情况，栈就太大了。另外，对于复杂度和深度递归的函数，固定大小的栈始终不够大。改变这个固定大小可以提高空间效率并允许创建更多的线程，或者也可以容许更深大递归函数，但无法同时做到上面两点。

#### 1.2 goroutine 调度

OS 线程由 OS 内核来调度。每隔几毫秒，一个硬件中断发到 CPU，CPU 调用一个调度器的内核函数。这个函数暂停当前正在运行的线程，把它的寄存器保存到内存，查看线程列表并决定接下来运行哪一个线程，再从内存恢复线程的注册信息，最后继续执行选中的线程。因为 OS 线程有内核调度，所以控制权从一个线程到另一个线程需要一个完整的上下文切换（context switch）：即保存一个线程的状态到内存，再恢复另外一个线程的状态，最后更新调度器的数据结构。考虑这个操作涉及的内存局域性以及涉及的内存访问数量，还有访问的内存所需的 CPU 周期数量的增加，这个操作其实是很慢的。

Go 运行时包含了一个自己的调度器，这个调度器使用一个称为 m : n 调度的技术（因为它可以复用/调度 m 个 goroutine 到 n 个 OS 线程）。Go 的调度器和内核的工作类似，但 Go 语言的调度器只需关心单个 goroutine 调度问题。

与操作系统的线程调度不同的是，Go 调度器不是由硬件始终来定期触发的，而是又特定的 Go 语言结构来触发的。比如当一个 goroutine 调用 time.Sleep 或被通道阻塞或对互斥量操作时，调度器就会将这个 goroutine 设为休眠模式，并运行其他 goroutine 直到前一个 goroutine 可重新唤醒为止。因为它不需要切换到内核环境，所以调用一个 gotoutine 比调度说和线程成本低很多。

#### 1.3 GOMAXPROCS

Go 调度器使用 GOMAXPROCS 参数来确定需要使用多少个 OS 线程来同时执行 Go 代码。默认值是机器上的 CPU 的数量，所以在一个有 8 个 CPU 的机器上，调度器会把 Go 代码同时调度到 8 个 OS 线程上。（GOMAXPROCS 是 m : n 调度中的 n）。正在休眠或者正被通道阻塞的 goroutine 不需要占用线程。阻塞在 I/O 和其他系统调用非 Go 语言写的函数的 goroutine 需要一个独立的线程，但这个线程不给算在 GoMAXPROC 内。

### 2. Go struct 能不能比较。

```golang
package main

import "fmt"

type student struct {
	id   int
	name string
}

type computer struct {
	version string
	cpu     string
	gpu     string
	memory  string
}

func main() {
	s1 := student{1, "张三"}
	s2 := student{2, "李四"}
	s3 := student{1, "张三"}
	fmt.Println(s1 == s2) // false
	fmt.Println(s1 == s3) // true
	sp1 := &s1
	sp2 := &s2
	sp3 := &s3
	sp4 := &s1
	fmt.Println(sp1 == sp2) // false
	fmt.Println(sp1 == sp3) // false
	fmt.Println(sp1 == sp4) // true

	// c := computer{"外星人", "i7", "1080", "512g"}
	// fmt.Println(s1 == c) // invalid operation: s1 == c (mismatched types student and computer)
}
```

根据上面的程序有：

Go 语言的 struct 可以进行比较，但是只能同类型进行比较，同类型在进行比较的时候会比较 struct 中的每一项，如果全部相等返回 true，否则返回 false。在指针进行比较的时候，如果指向的同一个地址，那么结果为 true，否则为 false。

不通类型的 struct 不能进行比较，如果比较的话，编译时就能发现，在编译时会报错，如上面的程序被注释的部分。

### 3. go defer（for defer）

Go 语言中的 defer 是先进后出的，也就是后面的 defer 语句会先执行。

如下例：

```golang
package main

import "fmt"

func main() {
    var arr [5]int

	for i := range arr {
		defer fmt.Println(i)
	}
	//4
	//3
	//2
	//1
	//0
}
```

### 4. select 可以用于干什么

select 就像一个多路复用器一样，同时监听多个阻塞的事件，如果事件发生了，就执行响应的事件逻辑，如果都没有发生，并且有默认的事件，那么就执行默认的事件逻辑。

### 5. context 包的用途

context 是 go 语言 goroutine 的长下文，可以用于长下文控制的 goroutine 的结束，也可以用于传递上下文消息。

### 6. 主协程如何等其余协程完再操作

可以通过 `WaitGroup` 操作来完成。

当有任务来的时候 Add(1)，执行完毕 Done()，主协程通过 Wait() 方法来等待执行完毕。

```golang
package main

import (
	"fmt"
	"sync"
	"sync/atomic"
)

func main() {
	wg := sync.WaitGroup{}
	// 任务计算1 - 1000 的和，分为 5 个协程进行
	var res int64
	n := 0
	for i := 0; i < 5; i++ {
		wg.Add(1)
		go func (start, end int64) {
			var s int64
			var i int64
			for i = start; i <= end; i++ {
				s += i;
			}
			atomic.AddInt64(&res, s)
			wg.Done()
		}(int64(n), int64(n + 2000))
		n += 2000
	}
	wg.Wait()
	fmt.Println(res)
}
```

### 7. 实现set

```golang
package main

import "fmt"

type Empty struct{}

var empty Empty

type Set struct {
	m map[interface{}]Empty
}

func SetFactory() *Set {
	return &Set {
		m:map[interface{}]Empty{},
	}
}

func (s *Set) Add(val interface{}) {
	s.m[val] = empty
}

func (s *Set) Remove(val interface{}) {
	delete(s.m, val)
}

func (s *Set) Len() int {
	return len(s.m)
}

func (s *Set) Clear() {
	s.m = make(map[interface{}]Empty)
}

func (s *Set) Traverse(handler func (val interface{})) {
	for v := range s.m {
		handler(v)
	}
}

func main() {
	s := SetFactory()
	for i := 0; i < 100; i++ {
		s.Add(i)
	}
	s.Traverse(func (val interface{}) {
		fmt.Println(val)
	})
}
```

### 8. 生产者消费者队列

```golang
type BlockingQueue struct {
	pipe chan interface{}
}

func NewBlockingQueue(n int) *BlockingQueue {
	return &BlockingQueue{make(chan interface{}, n)}
}

func (b *BlockingQueue) Offer(data interface{}) {
	b.pipe <- data
}

func (b *BlockingQueue) Take() interface{} {
	return <-b.pipe
}
```

### slice 的底层原理，扩容机制

golang 中的 slice 数据类型，是利用指针指向某个连续片段的数组。一个 `slice` 在 golang 中占用 24 个 bytes。

在 runtime 的 slice.go 中，定义了 slice 的 struct。

```golang
type slice struct {
    array unsafe.Pointer  // 8 bytes
    len int    // 8 bytes
    cap int    // 8 bytes
}
```

- array 是指向真实数组的 ptr
- len 是指切片已有元素个数
- cap 是指当前分配的空间

slice 扩容

在对 slice 做 append 的时候，其实是调用了 `call runtime.growslice` ，看看做了什么：

```go
func growslice(et *_type, old slice, cap int) slice {
    if cap < old.cap {
        panic(errorString("growslice: cap out of range"))
    }

    if et.size == 0 {
        // append should not create a slice with nil pointer but non-zero len.
        // We assume that append doesn't need to preserve old.array in this case.
        return slice{unsafe.Pointer(&zerobase), old.len, cap}
    }

    newcap := old.cap
    doublecap := newcap + newcap
    if cap > doublecap {
        newcap = cap
    } else {
        if old.len < 1024 {
            newcap = doublecap
        } else {
            for 0 < newcap && newcap < cap {
                newcap += newcap / 4
            }
            if newcap <= 0 {
                newcap = cap
            }
        }
    }

    var overflow bool
    var lenmem, newlenmem, capmem uintptr
    // Specialize for common values of et.size.
    // For 1 we don't need any division/multiplication.
    // For sys.PtrSize, compiler will optimize division/multiplication into a shift by a constant.
    // For powers of 2, use a variable shift.
    switch {
    case et.size == 1:
        lenmem = uintptr(old.len)
        newlenmem = uintptr(cap)
        capmem = roundupsize(uintptr(newcap))
        overflow = uintptr(newcap) > maxAlloc
        newcap = int(capmem)
    case et.size == sys.PtrSize:
        lenmem = uintptr(old.len) * sys.PtrSize
        newlenmem = uintptr(cap) * sys.PtrSize
        capmem = roundupsize(uintptr(newcap) * sys.PtrSize)
        overflow = uintptr(newcap) > maxAlloc/sys.PtrSize
        newcap = int(capmem / sys.PtrSize)
    case isPowerOfTwo(et.size):
        var shift uintptr
        if sys.PtrSize == 8 {
            // Mask shift for better code generation.
            shift = uintptr(sys.Ctz64(uint64(et.size))) & 63
        } else {
            shift = uintptr(sys.Ctz32(uint32(et.size))) & 31
        }
        lenmem = uintptr(old.len) << shift
        newlenmem = uintptr(cap) << shift
        capmem = roundupsize(uintptr(newcap) << shift)
        overflow = uintptr(newcap) > (maxAlloc >> shift)
        newcap = int(capmem >> shift)
    default:
        lenmem = uintptr(old.len) * et.size
        newlenmem = uintptr(cap) * et.size
        capmem, overflow = math.MulUintptr(et.size, uintptr(newcap))
        capmem = roundupsize(capmem)
        newcap = int(capmem / et.size)
    }

    if overflow || capmem > maxAlloc {
        panic(errorString("growslice: cap out of range"))
    }

    var p unsafe.Pointer
    if et.ptrdata == 0 {
        // 申请内存
        p = mallocgc(capmem, nil, false)
        
        // 清除未使用的地址
        memclrNoHeapPointers(add(p, newlenmem), capmem-newlenmem)
    } else {
        p = mallocgc(capmem, et, true)
        if lenmem > 0 && writeBarrier.enabled {
            bulkBarrierPreWriteSrcOnly(uintptr(p), uintptr(old.array), lenmem)
        }
    }
    // 拷贝大小为 lenmem 个btyes，从old.array到p
    memmove(p, old.array, lenmem)

    return slice{p, old.len, newcap}
```

具体扩容策略：

- 如果申请的容量(cap) 大于 2 倍的原容量，那么新容量 = 申请容量。
- 如果新申请的容量小于 2 倍的原容量，并且原 slice 的长度小于 1024，那么新容量 = 原容量的 2 倍。否则不算计算 `newcap += newcap / 4` 直到不小于需要申请的容量（cap）。
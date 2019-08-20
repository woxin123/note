# goroutine 和通道

## goroutine

在 Go 里，每一个并发执行的活动称之为 goroutine。

当一个程序启动时，只有一个 goroutine 来调用 main 函数，称它为主 goroutine。新的 goroutine 通过 go 语句进行创建。语法上，一个 go 语句是在普通函数或者方法前加上 go 关键字前缀。go 语句是函数在一个创建的 goroutine 中调用。go 语句本身的执行立即完成：

```go
f()   // 调用 f(); 等待它的返回
go f() // 新建一个调用 f() 的 goroutine，不用等待
```

下面的例子中，主 goroutine 计算第 45 个斐波那契。因为它使用递归计算，所以它需要大量的时间来执行，再次期间我们提供了一个可见的提示，显示字符串 `spinner` 来指示程序依然在运行。

```
package main

import (
	"fmt"
	"time"
)

func main() {
	go spinner(100 * time.Millisecond)
	const n = 45
	fibN := fib(n) // slow
	fmt.Printf("\rFibonacci(%d) = %d\n", n, fibN)
}

func spinner(delay time.Duration) {
	for {
		for _, r := range `-\|/` {
			fmt.Printf("\r%c", r)
		}
	}
}

func fib(x int) int {
	if x < 2 {
		return x
	}
	return fib(x-1) + fib(x-2)
}
```

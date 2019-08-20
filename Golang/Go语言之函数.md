# Go语言之函数

## 1. 函数声明

每个函数声明都包含一个名字、一个形参列表、一个可选的返回列表以及函数体：

```go
func name(parameter-list) (result-list) {
		body
}
```

形参列表指定了一组变量的参数名和参数类型，这些局部变量都由调用者提供的参数传递而来。返回列表则指定了函数的返回值类型。当函数返回了一个未命令的返回值或者没有返回值的时候，返回列表的圆括号可以省略。如下所示：

```go
func hypot(x, y float64) float64 {
	return math.Sqrt(x*x + y*y)
}
fmt.Printf(hypot(3, 4)) // "5"
```

x，y是函数声明中的形参，3和4是函数调用时的实参，并且函数的返回值是一个类型为float64的值。

返回值也可以像形参一样命名。这个时候，每一个命名的返回值会声明为一个局部变量，并根据变量的类型初始化为相应的0值。

当函数存在返回值列表时，必须显式地以 `return` 语句结束，除非函数明确不会走完整个执行流程，比如在函数中抛出宕机异常处理或者函数体内存在一个没有break退出条件的无限循环。

在hypot函数中使用到一种简写，如果几个形参或者返回值类型相同，那么类型只需要写一次。一下是两个声明时完全相同的：

```go
func f(i, j, k int, s, t string)
func f(i int, j int, k int, s string, t string)
```

下面使用4中方式声明了一个带有两个形参和一个返回值的函数，所有变量都是int类型。空白标识符用来强调这个形参在函数中未使用。

```go
package main

import "fmt"

func add(x int, y int) int { return x + y }
func sub(x, y int) (z int) { z = x - y; return }
func first(x int, _ int) int { return x }
func zero(int, int) int { return 0 }

func main() {
	fmt.Printf("%T\n", add)	// "func(int, int) int"
	fmt.Printf("%T\n", sub)	// "func(int, int) int"
	fmt.Printf("%T\n", first) // "func(int, int) int"
	fmt.Printf("%T\n", zero)	// "func(int, int) int"
}

```

函数的类型称为函数签名。当两个函数拥有相同的形参列表和返回值列表时，认为这两个函数的类型或签名是相同的。而形参和返回值的名字不会影响到函数的类型，采用简写同样也不会影响到函数的类型。

每一次函数都需要提供实参来对应函数的每一个形参，包括函数的调用顺序也必须一致。Go语言中没有默认参数的概念也不能指定实参名，所以除了用于文档说明之外，形参和返回值命名不会对调用方有任何影响。

实参是按值传递的，所以函数接收到的是每个实参的副本；修改函数的形参变量并不会影响到调用者提供的实参。然而，如果提供的实参包含了引用类型，比如指针、slice、map、函数或者通道，那么当函数使用形参变量时就有可能会间接地修改实参变量。

你可能会偶尔可到有些函数的声明没有任何函数体，那么说明这个函数使用除了Go语言之外的语言实现的。这样的声明定义了该函数的签名。

```go
package math
func Sin(x float64) float64		// 使用汇编语言实现
```

## 2. 递归

函数可以递归调用，这意味着函数可以直接或者间接调用自己。

许多编程语言使用固定长度的调用栈；大小在64K到2MB直接。递归的深度受限于固定长度的栈大小，所以当进行深度递归调用时必须谨防栈溢出。固定长度的站甚至会造成一定的安全隐患。相比固定长度的栈，Go语言使用了可变长度的栈，栈的深度会随着我们的使用而增长，可达到1GB左右的上限。这使得我们可以安全地使用递归而不用担心溢出问题。



## 3. 多返回值

一个函数的返回不止一个结果。我们之前已经见过标准包内许多函数返回两个值，一个期望结果与一个错误值，或者一个表示函数调用是否正确的布尔值。

下面的程序中 `findlinks` 函数发送了HTTP请求，检查输入的 url 是否正确，然后在下面的 `visit` 函数中进行解析，其中请求和解析都有可能出错，所以 `findlinks` 声明了两个结果：一个是发现的链接列表；另一个是错误。另外，HTML的解析一般能够修正错误的输入以及构造一个存在节点的文档，所以Parse很少失败；通常情况下，出错都是由基本的 I/O 错误引起的。

```go
package main

import (
	"fmt"
	"golang.org/x/net/html"
	"net/http"
	"os"
)

func main() {
	for _, url := range os.Args[1:] {
		links, err := findLinks(url)
		if err != nil {
			fmt.Fprintf(os.Stderr, "findlinks2: %v\n", err)
			continue
		}
		for _, link := range links {
			fmt.Println(link)
		}
	}
}

// findLinks 发起一个HTTP的GET请求，解析返回的HTML页面，并返回所有链接
func findLinks(url string) ([]string, error) {
	resp, err := http.Get(url)
	if err != nil {
		return nil, err
	}
	if resp.StatusCode != http.StatusOK {
		resp.Body.Close()
		return nil, fmt.Errorf("getting %s: %s", url, resp.Status)
	}
	doc, err := html.Parse(resp.Body)
	resp.Body.Close()
	if err != nil {
		return nil, fmt.Errorf("paring %s as HTML: %v", url, err)
	}
	return visit(nil, doc), nil
}

// visit 函数会将 n 节点中的每个节点的链接添加到结果中
func visit(links []string, n *html.Node) []string {
	if n.Type == html.ElementNode && n.Data == "a" {
		for _, a := range n.Attr {
			if a.Key == "href" {
				links = append(links, a.Val)
			}
		}
	}
	for c := n.FirstChild; c != nil; c = c.NextSibling {
		links = visit(links, c)
	}
	return links
}

```

`findLinks` 函数有4条返回语句，每一条返回语句返回一对值。前三个返回语句将函数从 http 和 html 包中获得错误信息传递给调用者。第一个返回语句中，错误直接返回；第二个返回语句和第三个语句则使用了 `fmt.Errorf`  格式化处理过的附加上下文信息。如果 `findLinks` 调用成功，最后一个返回语句将返回链接的 `slice` ，且 `error` 为空。

我们必须保证 `resp.Body` 正确关闭使得网络资源正常释放，即使在发生错误的情况下也必须释放资源。Go的垃圾回收机制将回收为使用的内存，但不能指望它会释放未使用的操作系统资源，比如打开的文件以及网络连接。必须显示地关闭它们。

如果需要忽略其中的一个返回值，可以使用一个空标识符 `_` 代替，例如：

```go
links, _ := findLinks(url)
```

一个多值调用可以作为单独的实参传递给拥有多个形参的函数中。尽管很少在生产环境使用，但是这个特性有时候很方便调试，它使得我们仅仅可以使用一条语句就可以输出所有的结果。下面两个输出语句的效果是一致的。

```go
log.Fatal(findLinks(url))

links, err := findLinks(url)
log.Fatal(links, err)
```

一个函数如果有命令的返回值，可以省略 `return` 语句的操作数，这称为裸返回。

## 5.4 错误

有一些函数总是成功的。比如，`strings.Contains`和 `strconv.FormatBool` 对所有可能的参数变量都有定义好的结果，不会调用失败。但是对于其他函数，可能有时候回发生一些错误，比如在I/O处理的时候发生错误，或者在将字符串转换为数字的时候可能会出现转换出来的数字太大或者太小等错误，其中的一些错误可能是我们预期的。并且有一些错误是我们能够在程序中处理的。

因此错误处理是包的AIP设计或者应用程序用户接口的重要部分，发生错误是许多预料中的一种而已。

如果当函数调用发生错误是返回一个附加的结果作为错误值，习惯上将错误值作为最后一个结果返回。如果错误只有一种情况，结果通常设置为布尔类型。更多时候，尤其是对于I/O操作，错误的原因可能多种多样，而调用者则需要一些详细的信息。在这种情况下，错误的结果类型往往是error。

`error` 是内置的接口类型。目前我们已经了解到，一个错误可能是空值或者非空值，空值意味着成功而非空值意味着失败，且非空的错误类型有一个错误消息字符串，可以通过调用它的 ` Error ` 方法或者通过调用 `fmt.Println(err)` 或 ` fmt.Println("%v", err) ` 直接输出错误的消息。

与其他语言不通，Go语言可以说没有错误处理机制，`error`其实只是一个普通的对象，只要实现 ` Error ` 方法的结构体都可以成为 `error`类型，所以很多的错误处理都是自定义的。有一些函数在调用出错的情况下可能也会返回部分有用的结果。比如，如果在读取一个文件的时候发生了错误，调用 `Read` 函数返回能够读取成功的字节数与对应的错误值。正确的行为通常是在调用者处理错误钱先处理这些不完整的返回结果。因此在文档中需要清晰地说明返回值是很有意义的。

## 5. 函数变量

函数在Go语言中也是头等重要的值：就像其他值，函数变量也是有类型，而且它们可以赋值给变量或者传递或者从其他函数中返回。函数变量可以像其他函数一样调用。比如：

```go
func square(n int) int {
	return n * n
}
func negative(n int) int {
	return -n
}
func product(m, n int) int {
	return m * n
}
func main() {
	f := square
	fmt.Println(f(3)) // "9"
	f = negative
	fmt.Println(f(3)) // "-3"
	fmt.Printf("%T\n", f) // "func(int) int"
	
	// f = product // 编译错误: 不能把类型func(int, int) int 赋值 func(int) int
}
```

函数类型的零值是 `nil` (空值)，调用一个空的函数将导致宕机。

```go
var f func(int) int
f(3) // 宕机：调用空函数
```

函数变量可以和空值相比较：

```go
var f func(int) int
if f != nil {
	f(3)
}
```

但它们本身不可比较，所以不可以互相进行比较或者作为键值出现在 `map` 中。

函数变量使得函数不仅将数据进行参数化，还将函数的行为当做参数进行传递。标准库中蕴含大量的例子。比如，`strings.Map` 对字符串中的每一个字符使用一个函数，将结果连接起来变成另一个字符串。

```go
func add1(r rune) rune {
	return r + 1
}
fmt.Println(strings.Map(add1, "HAL-9000"))	// "IBM.:111"
		fmt.Println(strings.Map(add1, "VMS"))		// "WNT"
		fmt.Println(strings.Map(add1, "Admix"))		// "Benjy"
```

## 6. 匿名函数

命名函数只能在包级别的自用于进行声明，但我们能够使用函数字面量在任何表达式内指定函数变量。函数字面量就像函数声明一样，但在 `func` 关键字后面没有函数的名称。它是一个表达式，它的值称作匿名函数。

函数字面量在我们需要使用的时候才定义。就像下面的例子，之前的函数调用 `strings.Map` 可以写成：

```go
strings.Map(func(r rune) rune) rune { return r + 1 }, "HAL-9000")
```

更重要的是，以这种方式定义的函数能够获取到整个词法环境，因此里层的函数可以使用外层函数中是的变量，如下面这个示例：

```go
// squares 函数返回一个函数，后者包含下一次要用到的平方数
func squares() func() int {
	var x int
	return func() int {
		x++
		return x * x
	}
}

func main() {
	f := squares()
	fmt.Println(f()) // "1"
	fmt.Println(f()) // "2"
	fmt.Println(f()) // "9"
	fmt.Println(f()) // "16"
}
```

函数 `squares` 返回了另一个函数，类型是 `func() int` 。调用 `squres` 创建了一个局部变量 x 而且返回了一个匿名函数，每次调用 `square` 都会递增 x 的值然后返回 x 的平方。第二次调用 `squres` 函数将创建第二个变量 x ，然后返回一个递增 x 值得新匿名函数。

里层的匿名函数能够获取和更新外层 `squares` 函数的局部变量。这些隐藏的变量引用就是我们把函数归类为引用类型而且函数变量无法进行比较的原因。函数变量类似于使用闭包方法实现的变量，Go程序员通常把函数变量称为闭包。

这里例子里面变量的声明周期不是由它作用域所决定的：变量 x 在 main 函数中返回 squres 函数然后依旧存在 (虽然 x 在这个时候是隐藏在函数变量 f 中的)。

## 7. 变长函数

变长函数被调用的时候可以有可变的参数个数，最令人熟知的例子就是 `fmt.Printf()` 与其变种。 `Printf`需要在开头提供一个固定的参数，后续便可以接收任意数目的参数了。

在参数空闲表的最后的类型名称之前使用省略号"…" 表明一个变长函数，调用这个函数的时候可以传递任意数目的参数。

```go
func sum(vals ...int) int {
	total := 0
	for _, val := range vals {
		total += val
	}
	return total
}
```

上面这个 `sum` 函数返回零个或者多个 `int` 参数。在函数体内，vals是一个类型 int 的 slice 。调用 sum 的时候任何数量的参数都将提供给 vals 参数。

但是有时候参数比较多，或者参数是一个数组，在传递变长参数不可能一个一个把参数写出来。go 语言提供了一种方式，就是在传递的实参后来加省略号表示传递一个数组或者 slice 。就像下面这样。

```go
values := []int {1, 2, 3, 4,5}
fmt.Println(sum(values...))
fmt.Println(sum(values[1:]...))
```

尽管 …int 参数就像函数体内的 slice ，当变长函数的类型和一个带有普通 slice 参数的函数的类型不相同。

```go
func f(...int) {}
func g([]int) {}
fmt.Printf("%T\n", f)		// func(...int)
fmt.Printf("%T\n", g)		// func([]int)
```



## 8. 延迟函数调用

在一条普通的语句前加一个 `defer` ，表明这条语句无论实在函数正常结束或者出现宕机异常结束，它的执行都会推迟到所有其他语句执行完毕，函数即将要结束的之前执行。`defer` 语句没有限制使用次数，执行的时候调用 `defer` 语句次序是倒序执行。

defer语句也可以用来调试一个复杂的函数，在函数的“入口”和“出口”处设置调试行为。下面的 `bigSlowOperation` 函数在开头调用 `trace` 函数，在函数刚进入的时候执行输出，然后饭后一个函数变量，当其被调用的时候执行退出函数的操作。以这种方式推迟返回函数的调用，我们可以使用一个语句在函数的入口和所有出口添加处理，甚至可以传递一些有用的值，比如每个函数的开始时间。

```go
func bigSlowOperation() {
	defer trace("bigSlowOperation")()

	time.Sleep(10 * time.Second)
}

func trace(msg string) func() {
	start := time.Now()
	log.Printf("enter %s", msg)
	return func() {
		log.Printf("exit %s (%s)", msg, time.Since(start))
	}
}

func main() {
	bigSlowOperation()
}
```

延迟执行函数在 `return` 语句之后执行，并且可以更新函数的结果变量。因为匿名函数可以得到其外层函数作用域内的变量，所以延迟执行的匿名函数可以观察到函数的返回结果。

## 9. 宕机

Go语言的类型系统会捕获许多编译时错误，但有些其他的错误（比如数组越界访问或者解引用空指针）都需要在运行时进行检查。当Go语言运行时检测到这些错误，就会发生宕机。也就是发生一些运行时错误。

当宕机发生时，正常的程序执行会终止，`goroutine` 中的所有延迟函数会执行，然后程序会异常退出并留下一条日志信息。日志消息包括宕机的值，这往往代表某种错误消息，每一个goroutine都会在宕机的时候显示一个函数调用的栈跟踪消息。通常可以借助这条日志来判断问题的原因而不需要再一次运行该程序，因此报告一个发生宕机的程序 `bug` ，总是会加上这条消息。

有时候程序执行到不是我们预期的地方，需要进行宕机的时候，而这时候有没有产生宕机的运行时错误，可以通过Go语言内置的宕机函数；内置的宕机函数可以接受任何值作为参数。如果碰到“不可能发生”的状况，宕机是最好的处理方式，比如语句执行到逻辑上不可能到达的地方时：

```go
switch s:= suit(drawCard()); s {
	case "Spades":	// ...
	case "Hearts":	// ...
	case "Diamonds":	// ...
	case "Clubs":	// ...
	default:
		panic(fmt.Sprintf("invalid suit %q", s))
}
```

设置函数断言是一个良好的习惯，但是这也会带来多余的检查。除非你能够提供有效的错误信息或者能够很快的检测出错误，否则在运行时检测断言就毫无意义。

```go
func Reset(x *Buffer) {
	if x == nil {
		panic("x is nil")
	}
	x.elements = nil
}
```

尽管Go语言的宕机机制和其他语言的异常很相似，但宕机的使用场景不仅相同。由于宕机引起的程序异常退出，因此只有在发生严重的错误是才会使用宕机，比如遇到与预想的逻辑不一致的代码；用心的程序员会将所有可能会发生的异常退出的情况考虑在内已证实 bug 的存在。强健的代码会优雅的处理“预期的”错误，比如错误的输入、配置或者I/O失败等；这时最好能够使用错误值来加以区分。

当宕机发生时，所有的延迟函数以倒序执行，从栈上面的函数开始一直返回至 main 函数，如下面的程序所示：

当宕机发生时，所有的延迟函数以倒序执行，从栈最上面的函数开始一直返回至 main函数，如下面的程序：

```go
package main

import "fmt"

func main() {
	f(3)
}

func f(x int) {
	fmt.Printf("f(%d)\n", x+0/x) // panics if x == 0 则发生宕机
	defer fmt.Printf("defer %d\n", x)
	f(x - 1)
}
```

运行之后:

```
f(3)
f(2)
f(1)
defer 1
defer 2
defer 3
```

当调用 `f(0)` 的时候回发生宕机，会执行三个延迟的 `fmt.Printf` 调用，之后，运行时终止了这个程序，输出宕机信息与一个栈的转储信息到标准错误流：

```
panic: runtime error: integer divide by zero
goroutine 1 [running]:
main.f(0x0)
	/Users/mengchen/study/study_go/src/gopl/ch5/defer1/main.go:10 +0x174
main.f(0x1)
	/Users/mengchen/study/study_go/src/gopl/ch5/defer1/main.go:12 +0x14f
main.f(0x2)
	/Users/mengchen/study/study_go/src/gopl/ch5/defer1/main.go:12 +0x14f
main.f(0x3)
	/Users/mengchen/study/study_go/src/gopl/ch5/defer1/main.go:12 +0x14f
main.main()
	/Users/mengchen/study/study_go/src/gopl/ch5/defer1/main.go:6 +0x2a
```

`runtime` 包提供了转储栈的方法是程序可以诊断错误，下面的代码在 main 函数中延迟 printStack 的执行：

```go
func main() {
	defer printStack()
	f(3)
}

func printStack() {
	var buf [4096]byte
	n := runtime.Stack(buf[:], false)
	os.Stdout.Write(buf[:n])
}
```

`runtime.Stack` 能够输出函数的栈信息，因为栈已经存在了。Go语言的宕机机制让延迟执行的函数在栈清理之前调用。



## 5.10 恢复

退出程序通常是正确的处理宕机的方式，但也有例外。在一定的情况下是可以进行恢复的，至少有时候可以在退出之前清理当前混乱的情况。比如，当 web 服务器遇到了一个位置的错误时，可以先关闭所有连接，这总比让客户端阻塞在哪里要好，在开发过程中，也可以像客户端汇报当前遇到的错误。

如果内置的函数 `recover` 在延迟函数的内部调用，而且这个包含 defer 语句的函数发生在宕机，`recover` 会终止当前的宕机状态并返回宕机的值。函数不会从之前宕机的地方继续运行而是正常返回。如果 `recover` 在其他任何情况下的运行则它没有任何效果且返回 `nil` 。

为了说明这一点，假设我们开发的一种语言的解析器。即使他看起来运行正常，但考虑到工作的复杂性，还是会存在只在特殊情况下发生的 bug 。我们在这个时候回更喜欢讲本该宕机的错误看作一个解析错误，不要立即终止运行，而是将一些有用的附加消息提供给用户来报告这个 bug 。

```go
func Parse(input string) (s *Syntax, err error) {
	defer func() {
		if p := recover(); p != nil {
			err = fmt.Errorf("internal error: %v", p)
		}
	}()
	// ...解析器...
}
```

Parse函数中的延迟函数从宕机的状态恢复，并使用宕机值组成一条错误的消息；理想的写法是使用 `runtime.Stack` 将整个调用栈包含进来。延迟函数则将错误赋值给 err 结果变量，从而返回给调用者。

下面的是一个使用 `recover` 的例子。

```go
func soleTitle(doc *html.Node) (title string, err error) {
	type bailout struct{}
	defer func() {
		switch p := recover(); p {
		case nil:
		// 没有宕机
		case bailout{}:
			// 预期宕机
			err = fmt.Errorf("multiple title elements")
		default:
			panic(p)	// 未预期的宕机；继续宕机过程
		}
	}()
	// 如果发现多余一个非空标题，退出递归
	forEachNode(doc, func(n *html.Node) {
		if n.Type == html.ElementNode && n.Data == "title" &&
			n.FirstChild != nil {
			if title != "" {
				panic(bailout{}) // 多个标题元素
			}
			title = n.FirstChild.Data
		}
	}, nil)
	if title == "" {
		return "", fmt.Errorf("no title element")
	}
	return title, nil
}
```


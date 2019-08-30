# Golang Context

Golang 的 context 包是专门用来简化对于单个请求的多个 goroutine 之间的请求的数据、取消信号、截至时间等相关操作。

比如一个网络请求 Request，每个 Request 都需要开启一个 goroutine 做一些事情，这些 goroutine 有可能会开启其他的 goroutine。 这样的话，我们就可以通过 context 来跟踪这些 goroutine，并且通过 context 来控制它们。中文中 context 也被称为 ”上下文“。

context 包的核心是下面的这个 interface，声明如下：

```go
type Context interface {
    Deadline() (deadline time.Time, ok bool)

    Done() <- chan struct{}

    Err() error

    Value(key interface{}) interface{}
}
```

Context 定义很简单，一共就4个方法：

1. Deadline 方法是获取截至时间的意思，第一个返回是截至时间，到了这个时间点，Context 会自动发起球球；第二个返回值 `ok == false` 时表示没有设置截止时间，如果需要调用取消函数的话，需要调用取消函数进行取消。

2. Done 方法返回一个只读的 chan，类型为 struct{}，我们在 goroutine 中，如果该方法返回的 chan 可以读取，则意味着 parent context 已经发起了取消请求，我们收到这个信号后，就应该做清理操作，然后退出 goroutine，释放资源。之后，Err 方法话返回一个错误，告知为什么 Context 被取消。

3. Err 方法返回取消的错误原因，为什么 context 被取消。

4. Value方法获取该 Context 上绑定的值，是一个键值对，所以要通过一个 key 才可以获取对应的值，这个值一般是线程安全的。

### Context 的实现


Context 虽然是一个接口，但是并不需要我们实现，golang 内置的 context 包，已经帮我们实现 2 个方法，一般在代码中都是以这两个方法作为最顶层的 parent context，然后再衍生出子 context。这些 Context 对象形成一棵树：当一个 Context 对象被取消，继承自它的所有 Context 都会被取消。两个实现如下：

```go
type emptyCtx int

func (*emptyCtx) Deadline() (deadline time.Time, ok bool) {
	return
}

func (*emptyCtx) Done() <-chan struct{} {
	return nil
}

func (*emptyCtx) Err() error {
	return nil
}

func (*emptyCtx) Value(key interface{}) interface{} {
	return nil
}

func (e *emptyCtx) String() string {
	switch e {
	case background:
		return "context.Background"
	case todo:
		return "context.TODO"
	}
	return "unknown empty Context"
}

var (
	background = new(emptyCtx)
	todo       = new(emptyCtx)
)

func Background() Context {
	return background
}

func TODO() Context {
	return todo
}
```

一个 Background，主要用于 main 函数，初始化以及测试代码中，作为 Context 这个树结构的最顶层的 Context，也就是根 Context，它不能被取消。

一个是 TODO，如果我们不知道该什么时候用什么 Context 的时候，可以使用这个，但是实际应用中，暂时还没有使用过这个 TODO。

从上面可以看出它们的本质都是 emptyCtx 结构，是一个不可取消，没有设置截至时间，没有携带任何值的 Context。

有了如上的 Context，可以通过下面 context 包提供的 With 系列函数来衍生子 Context。

```go
func WithCancel(parent Context) (ctx Context, cancel CancelFunc)

func WithDeadline(parent Context, deadline time.Time) (Context, CancelFunc)

func WithTimeout(parent context, timeout time.Duration) (Context, CancelFunc)

func WithValue(parent Context, key val interface{}) Context
```

通过这些函数，就创建了一棵 Context 树，树的每个节点都可以有任意多个子节点，节点的层级可以有任意多个。

`WithCancel` 函数，传递一个父 Context 作为参数，返回子 Context，以及一个取消函数用来取消 Context。

`WithDeadline` 函数，和 `WithCancel` 差不多，它会多传递一个截至时间参数，意味着到了这个时间点，会自动取消 Context，当然我们也可以不等到这个时候，可以提前通过取消函数进进行取消。

`WithTimeout` 和 `WithDeadline` 基本上一样，它的第二个参数和 `WithDeadline` 的第二个参数不同，它是一个时间段类型的 `time.Duration` ，表示一段时间，在这段时间后会自动取消。

`WithValue` 函数和取消 `Context` 无关，它是为了一个生成绑定一个键值对的数据的 Context，这个绑定的数据可以通过 `Context.Value` 方法访问到，这是我们实际上经常用到的技巧，一般我们想要通过上下文来传递数据时，可以通过这个方法，如我们需要 trace 追踪系统调用栈的时候。

### With系列函数详解

WithCancel

context.WithCancel 生成了一个 WithCancel 的实力以及一个 cancelFunc，这个函数就是用来关闭 ctxWithCancel 中的 Done cancel 函数。

下面来分析下源码的实现，首先来看看初始化，如下：

```go
type CancelFunc func()

// newCancelCtx returns an initialized cancelCtx.
func newCancelCtx(parent Context) cancelCtx {
	return cancelCtx{Context: parent}
}

func WithCancel(parent Context) (ctx Context, cancel CancelFunc) {
	c := newCancelCtx(parent)
	propagateCancel(parent, &c)
	return &c, func() { c.cancel(true, Canceled) }
}
```

newCancelCtx 函数返回一个初始化的 cancelCtx，cancelCtx 结构提继承了 Context，实现了 canceler 方法：

```go
// A canceler is a context type that can be canceled directly. The
// implementations are *cancelCtx and *timerCtx.
// 实现了 canceler 的类型都可以直接 cancel
// *cancelCtx 和 timerCtx 都实现了 *canceler
type canceler interface {
	cancel(removeFromParent bool, err error)
	Done() <-chan struct{}
}

// closedchan is a reusable closed channel.
var closedchan = make(chan struct{})

func init() {
	close(closedchan)
}

// A cancelCtx can be canceled. When canceled, it also cancels any children
// that implement canceler.
// cancelCtx 能被取消，当被取消的时候，它的任意实现了 cancelCer 都能被取消
type cancelCtx struct {
	Context

	mu       sync.Mutex            // protects following fields
	done     chan struct{}         // created lazily, closed by first cancel call
	children map[canceler]struct{} // set to nil by the first cancel call
	err      error                 // set to non-nil by the first cancel call
}

func (c *cancelCtx) Done() <-chan struct{} {
    c.mu.Lock()
    // 如果 done 是 nil，则创建一个
	if c.done == nil {
		c.done = make(chan struct{})
	}
	d := c.done
	c.mu.Unlock()
	return d
}

func (c *cancelCtx) Err() error {
	c.mu.Lock()
	err := c.err
	c.mu.Unlock()
	return err
}

func (c *cancelCtx) String() string {
	return fmt.Sprintf("%v.WithCancel", c.Context)
}

// cancel closes c.done, cancels each of c's children, and, if
// removeFromParent is true, removes c from its parent's children.
// 核心是关闭 c.done
// 同时会设置 c.err = err, c.children = nil
// 依次便利 c.children，每个child 分别 cancel
// 如果设置了 removeFromParent，则将 c 从其 parent 的 children 中删除。
func (c *cancelCtx) cancel(removeFromParent bool, err error) {
	if err == nil {
		panic("context: internal error: missing cancel error")
	}
	c.mu.Lock()
	if c.err != nil {
		c.mu.Unlock()
		return // already canceled
	}
	c.err = err
	if c.done == nil {
		c.done = closedchan
	} else {
		close(c.done)
    }
    // 依次便利关闭它的 child
	for child := range c.children {
		// NOTE: acquiring the child's lock while holding parent's lock.
		child.cancel(false, err)
	}
	c.children = nil
	c.mu.Unlock()
    // 如果设置了 removeFromParent 则将它从parent 中删除掉
	if removeFromParent {
		removeChild(c.Context, c)
	}
}
```

可以看到，所有的 children 都在一个 map 中；

Done 方法会返回其中的 done channel，而另外的 cancel 方法会关闭 Done channel 并且逐层向下遍历，关闭 children 的 channel，并且将当前 canceler 从 parent 中移除。

WithCancel 初始化一个 cancelCtx 的同时，还会执行 protogateCancel 方法，最后返回一个 cancel function。

```go
// propagateCancel arranges for child to be canceled when parent is.
func propagateCancel(parent Context, child canceler) {
	if parent.Done() == nil {
		return // parent is never canceled
    }
    // 向上查找一个可以被 cancel 的 parent
	if p, ok := parentCancelCtx(parent); ok {
        p.mu.Lock()
        // 如果父节点已经被 cancel，则将刚才传入的 canceler 给 cancel 掉
		if p.err != nil {
			// parent has already been canceled
			child.cancel(false, p.err)
		} else {
            // 将子节点连接到 p 的 children 中
			if p.children == nil {
				p.children = make(map[canceler]struct{})
			}
			p.children[child] = struct{}{}
		}
		p.mu.Unlock()
	} else {
        // 如果没有就自动开启一个 goroutine 等待 parent 被 cancel
		go func() {
			select {
			case <-parent.Done():
				child.cancel(false, parent.Err())
			case <-child.Done():
			}
		}()
	}
}

// parentCancelCtx follows a chain of parent references until it finds a
// *cancelCtx. This function understands how each of the concrete types in this
// package represents its parent.
func parentCancelCtx(parent Context) (*cancelCtx, bool) {
	for {
		switch c := parent.(type) {
		case *cancelCtx:
			return c, true
		case *timerCtx:
			return &c.cancelCtx, true
		case *valueCtx:
			parent = c.Context
		default:
			return nil, false
		}
	}
}

```

protagateCancel 的含义就是传递 cancel，从当前传入的 parent 开始（包括该 parent），向上查找最近的一个可以被 cancel 的 parent，如果找到的 parent 已经被 cancel，则将方才传入的 child 也给 cancel 掉，否则， 将 child 节点直接连接到 parent 的 children 中；如果没有找到最近的可以被 cancel 的parent，其上都不可以被 cancel，则启动一个 goroutine 等待传入的 parent 终止，则 cancel 传入的 child，或者等待传入的 child 被 cancel。

WithDeadLine

在 WithCancel 的基础上进行的扩展，如果时间到了就进行 cancel 操作，具体的操作流程和 withCancel 一致，只不过控制 cancel 函数调用的时机是又一个 timeout 的 cancel 所控制的。

### Context 的常用方法实例

```go
package main

import (
	"context"
	"fmt"
	"time"
)

const key = "key"

func main() {
	ctx, cancel := context.WithCancel(context.Background())

	valueCtx := context.WithValue(ctx, key, "Playing games")
	go playGame(valueCtx)
	time.Sleep(10 * time.Second)
	cancel()

	time.Sleep(5 * time.Second)
}

func playGame(ctx context.Context) {
	for {
		select {
		case <-ctx.Done():
			// get value
			fmt.Println(ctx.Value(key), "is cancel")
			return
		default:
			// get value
			fmt.Println(ctx.Value(key), "geming")
			time.Sleep(2 * time.Second)
		}
	}
}
```
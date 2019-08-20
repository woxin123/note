# Go语言的错误处理方案

`errors` 包提供了简单地错误处理机制。你可以使用 `go get` 获取它。

```shell
go get github.com/pkg/errors
```

传统的 Go 语言的错误处理大致类似于下面这样：

```golang
if err != nil {
    return err
}
```

有可能会出现到会层次很深，而一直返回 `error`，因为没有上下文信息，仅仅只有一个字符串，所以导致很难发现是哪里出现的问题。`errors` 包允许程序员在不破坏原有错误值的基础上添加错误出现的路径和位置的上下文。

## 给一个 error 添加上下文

```golang
_, err := ioutil.ReadAll(r)
if err != nil {
    return err.Wrap(err, "read failed")
}
```

## 查看一个错误的原因

使用 `errors.Wrap` 构造一个错误栈，并且为之前的错误添加了上下文。也就是说 `errors.Wrap` 会被错误进行层层的封装，但是有时候需要获取的最原始的错误信息，那么就需要一个反转的操作去获取它。任何的错误只要实现了下面的这个接口就能返回最原始的错误。

```go
type causer interface{
    Cause() error
}
```

如果 `error` 没有实现 `cause`，`errors.Cause` 方法将递归地取出最顶层的错误。这个最顶层的错误将被认为是最原始的错误。

```go
switch err := errors.Cause(err).(type) {
case *MyError:
        // handle specifically
default:
        // unknown error
}
```

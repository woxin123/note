# Go语言中的工具

## go

### go build

### go install

### go run

### go test

### go doc

```
go doc <packageName>/<packageName+funcName/typeName>
```

返回一个包的文档或者某一个函数或者类型的文档。这里后面指定的路径不一定要完整或者正确。

例如，查看 `time` 包的文档。

```shell
$ go doc time
package time // import "time"

Package time provides functionality for measuring and displaying time.

The calendrical calculations always assume a Gregorian calendar, with no
leap seconds.


Monotonic Clocks

Operating systems provide both a “wall clock,” which is subject to changes
for clock synchronization, and a “monotonic clock,” which is not. The
general rule is that the wall clock is for telling time and the monotonic
```

或者是一个方法：

```shell
$ go doc time.Since
func Since(t Time) Duration
    Since returns the time elapsed since t. It is shorthand for
    time.Now().Sub(t).
```

有或者输入一个不完整的名称：

```shell
$ go doc json.decode
func (dec *Decoder) Decode(v interface{}) error
    Decode reads the next JSON-encoded value from its input and stores it in the
    value pointed to by v.

    See the documentation for Unmarshal for details about the conversion of JSON
    into a Go value.
```

## godoc

godoc 提供了一个相互链接的 HTML 页面服务。在 "https://golang.org/pkg" 的 godoc 服务器覆盖了标准库。

在 "https://godoc.org" 的 godoc 服务器提供了数千个可供搜索的开源包索引。

如果你想浏览自己的包，也可以在你的工作区间运行一个 `godoc` 实例。在执行下面的命令后，在浏览器中访问 `http://localhost:8000/pkg`:

```shell
godoc -http :8000
```

加上 `-analysis=type` 和 `-analysis=pointer` 标记是文档的内容更加丰富。同时提供源代码的高级静态分析结果。

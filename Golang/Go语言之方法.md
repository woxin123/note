# Go语言之方法

方法是Go语言为了支持面向对象（OOP）的编程思想。

尽管没有同意的面向对象编程的定义，对我们来说，对象就是简单的一个值或者变量，并且拥有方法，而方法是某一种特定类型的函数。面向对象的编程就是使用方法来描述每个数据结构的属性和操作，于是，使用者不需要了解对象本身的实现。

后面我们会讲到在GO语言中的关于面向对象编程的封装和继承。在Go语言中没有继承和函数或者方法的重载，所以也就是不存在多态的说法。

## 1. 方法的声明

方法的声明和普通的函数的声明很类似，这时在函数名字前面多写了一个参数。这个参数把这个方法绑定到这个参数对应的类型上。

下面是一个简单的方法的示例:

```go
package main

import "math"

type Point struct {
X, Y float64
}

// 普通函数
func Distance(p, q Point) float64 {
return math.Hypot(q.X-p.X, q.Y-p.Y)
}

// Point类型的方法
func (p Point) Distance(q Point) float64 {
return math.Hypot(q.X-p.X, q.X-p.Y)
}
```
附加信息P称为方法的接受者，它源自早先的面向对象的语言，用来描述主调方法就像对象发送消息。

Go语言中，接受者不使用特殊名（比如`this` 或者 `self` ）；而是我们自己选择接受者的名字，就像其他的参数变量一样。由于接受者会频繁地使用，因此我们最好能够选择简单且在整个方法中名称始终保持一致的名字。最常用的方法就是去类型名称的首字母。这样就和声明保持一致。

```
p := Point{1, 2}
q := Point{4, 5}
fmt.Println(Distance(p, q))
fmt.Println(p.Distance(q))
```

上面两个 `Distance` 函数并没有声明冲突。第一个声明一个包级别的函数（称为 `geometry.Distance` )。第二个声明时一个类型的 `Point` 的方法，因此它的名字是 `Point.Distance` 。

选择表达式 `p.Distacne` 称为选择子（selector），因为它的接受者p选择合适的 `Distance` 方法，选择子也用于选择结构类型中的某些字段值，就像 `p.x` 中的字段值。由于方法和字段来自于同一个命名空间，因此在 `Point` 结构类型中声明一个叫做 x 的方法会与字段 x 冲突，编译器会报错。

因为每一个类型都有它自己的命令空间，所以我们能够在其他不同的类型中使用名字 `Distance` 作为方法名。定义一个 `Path` 类型表示一条线，同样也使用 `Distance` 作为方法名。

```ge
// Path 是连接多个点的直线段
type Path []Point

func (path Path) Distance() float64 {
	sum := 0.0
	for i := range path {
		if i > 0 {
			sum += path[i-1].Distance(path[i])
		}
	}
	return sum
}
```

## 2. 指针接受者的方法

由于主调函数会赋值每一个实参变量，如果函数需要更新一个变量，或者如果一个实参太大而我们希望避免复制整个实参，因此我们必须使用指针来传递变量的地址。这也同样适用于更新接受者：我们将它绑定到指针类型，比如 `*Point` 。

```go
func (p *Point) ScaleBy (factor float64) {
	p.X *= factor
	p.Y *= factor
}
```

这个方法的名字是 ` (*Point).ScaleBy) ` 。圆括号是必须的；没有圆括号，表达式会被解析成 ` *(Point.ScaleBy) `。

在真实程序中，习惯上遵循如果 `Point` 的任何一个方法使用指针接受者，那么所有的 `Point` 方法都应该使用指针接受者，即使有些方法并不一定需要。

命名类型（Point）与指向它们的指针（\*Point）是唯一可以出现在接受者声明处的类型。而且，**为了防止混淆，不允许本身是指针的类型方法进行声明。**

```go
type P *int
func (P) f() {/* ... */ } // 编译错误：非法的接受者类型
```

通过提供的 \*Point 能够调用 `(*Point).ScaleBy` 方法，比如：

```go
r := &Point{1, 2}
r.ScaleBy(2)
fmt.Println(*r) // "{2, 4}"
```

或者：

```go
p := Point{1, 2}
pptr := &p
pptr.ScaleBy(2)
fmt.Println(p) // "{2, 4}"
```

或者：

```go
p := Point{1, 2}
(&p).ScaleBy(2)
fmt.Println(p) // "{2, 4}"
```

虽然，最后两个方法看上起比较的别扭，但也是合法的。如果接受者 p 是 Point 类型的变量，但方法要求一个 `*Point` 接受者，我们可以简写：

```go
p.ScaleBy(2)
```

实际上编译器会对变量进行 `&p` 的隐式转换。只有变量才允许这么做，包括结构体字段，像 `p.X` 和数组或者 slice 元素，比如 perim[0]。不能够对一个取地址的 Point 接受者参数调用 `*Point` 方法，因为无法获取临时地址变量。

这里必须强调一点，不能对一个不能取地址的Point接受者参数调用 `*Point` 方法，因为无法获取变量的地址。例如：

```go
Point{1, 2}.ScaleBy(2) // 编译出错：不能获得 Point 类型的字面量的地址
```

如果接受者的实参是一个指针，也就是说如果是一个指针类型的变量调用它对应结构体的方法也是合法的，因为我们可以通过给指针解引用从而获取指针所指向的变量，然后调用。所以在这个过程中编译器会隐式的对这里的指针进行解引用。也就是说下面的两种方式调用的结果是一致的。

```go
pptr.Distance(q)
(*pptr).Distance(q)
```

go语言的编译器帮助我们擦除了指针和普通变量在这儿调用的差异。

吐过类型 T 的方法的接收者是T，而不是他的指针类型 `*T`，那么这个方法肯定是安全的，因为在调用的时候回复制一份实例，但是当他的接受者是它的指针类型也就是 `*T` ，那么在方法内尽量避免传递参数的时候将接受者传递出去，这样可能会导致破坏内部原本的数据。

方法还有另一种调用方式，通过它的类型和它的方法名进行调用，这样调用时它的第一个参数就是这个方法的接受者，其他参数是方法定义的实参。例如，对于 `Point.Distance()` 方法可以这样调用：

```go
p := Point{1, 2}
q := Point{2, 4}
res := Point.Distance(p, q) // 相当于 res := p.Distance(q)
```

### nil 是一个合法的接收者

没错在Go语言中 `nil` 也是一个合法的方法接收者。有的时候当一个变量是 `nil` 的时候是有意义的。例如下面的例子。

```go
// IntList 是整型链表
// *IntList 的类型 nil 代表空链表
type IntList struct {
	Value int
	Tail *IntList
}

// Sum 返回列表元素的总和
func (list *IntList) Sum() int {
	if list == nil {
		return 0
	}
	return list.Value + list.Tail.Sum()
}
```

在上面的例子中 `nil` 表示一个空的链表。

当定义一个类型允许 `nil` 作为接受者时，应在文档注释中显示地标明，就像上面的例子那样。

### 3. 通过结构体内嵌组成类型

结构体的内嵌能够让我们快速的访问内嵌结构体的字段，对于方法也是同样的。例如：

```go
type ColoredPoint struct {
	Point
	color.RGBA
}

red := color.RGBA{255, 0, 0, 255}
blue := color.RGBA{0, 0, 255, 255}
var p = ColoredPoint{Point{1, 1}, red}
var q = ColoredPoint{Point{5, 4}, blue}
fmt.Println(p.Distance(q.Point)) // "5"
p.ScaleBy(2)
q.ScaleBy(2)
fmt.Println(p.Distance(q.Point)) // "10"
```

这里的结构体内嵌只是为了方便我们使用，可以说是一个语法糖，和继承一点关系都没有。

对于一个结构体它查询是否有这个方法的时候，首先查询该结构体是否拥有这个方法，再依次查询它的内嵌结构体，然后在依次查询该内嵌结构体的内嵌结构体，这时一个递归的过程。

匿名的结构体可以通过内嵌一个结构体来实现匿名结构体拥有方法的功能。

```go
var ppp = struct {
	Point
}{
	Point: Point{1, 2},
}
ppp.ScaleBy(0.2)
fmt.Println(ppp)
```

## 4. 方法变量和方法表达式

在Go语言里可以把某一个接受者的方法赋值给另一个方法变量，例如可以将 `p.Distance()` 赋值给一个方法变量，这个方法变量是一个函数，把方法( `Point.Distance` ) 绑定到一个接受者 p 上。函数只需要提供实参而不需要提供接受者就能够调用。

```go
p := Point{1, 2}
q := Point{4, 6}
distanceFromP := p.Distance
fmt.Println(distanceFromP(q))
var origin Point
fmt.Println(distanceFromP(origin))
scaleP := p.ScaleBy
scaleP(2)
scaleP(3)
scaleP(10)
```

方法表达式，像调用函数一样调用方法，可以通过 T.f 或者 `(*T).f` （其中 T 是类型，f是函数）来调用，其中第一个参数就是原来的方法接受者，其余的参数就是原来的方法参数。当然也可以把T.f 或者 `(*T).f` 赋值给一个变量，这个变量就成为方法表达式。 例如：

```
p := Point{1, 2}
q := Point{4, 6}
fmt.Println(Point.Distance(p, q))

pointDistance := Point.Distance
fmt.Println(pointDistance(p, q))
```
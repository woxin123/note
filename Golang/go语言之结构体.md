# Go语言之结构体

**结构体是将零个或者任意类型的命名变量组合在一起的聚合数据类型。每个变量都叫做结构体的成员变量。** Go语言的结构体和C语言的结构体类似。

下面的语句定义了一个叫做`Employee`的结构体和一个结构体变量`dilbert` :

```go
type Employee struct {
	ID			int
	Name 		string
	Address		string
	DoB			time.Time
	Position 	string
	Salary		int
	ManagerID	int
}

var dilbert Employee
```

dilbert的每一个成员变量都通过`.`来访问，就像`dilbert.Name`和`dilber.DoB`。下面演示了结构体成员变量的赋值和通过指针访问等操作：

```go
dilbert.Salary -= 5000
position := &dilbert.Position
*position = "Senior " + *position
// 点号同样也可以用在结构体指针上
var employeeOfTheMonth *Employee = &dilbert
employeeOfTheMonth.Position += " (proactive team player)"
// 上面的语句等价于
// (*employeeOfTheMonth).Position += " (proactive team player)"
```

为了减少结构体指针书写的麻烦，所以使用结构体指针可以向使用结构体一样直接使用`.`来获取指针所指向的结构体的某一个变量或者方法。

结构体的成员变量通常一行写一个，变量名在前，类型在后，但是相同类型的变量也可以写在一行上。例如下面的：

```go
type Employee struct {
	ID				int
	Name, Address	string
	DoB				time.Time
	Position 		string
	Salary			int
	ManagerID		int
}
```

如果一个结构体类型的一个成员变量的首字母是大写的，那么这个变量是可导出的，这时Go语言的最主要的控制机制。一个结构体可以同时包含可导出和不可导出的成员变量。这里的可导出的意思就是可以在除了该结构体所在包之外的其他包可以访问的意思。

和C语言类似，命名结构体s不可以定义一个拥有相同结构体类型s的成员变量，也就是一个聚合类型不可以包含它自己（同样的限制也对数组也适用）。但是结构体s中可以定义一个s的指针类型，即`*s`，这样我们就可以创建一些递归数据结构，比如链表和树。下面是一个简单的示例：

```go
//type s struct {
//	a, b, c 	int
//	d 			s
//}	// invalid recursive (递归) type s

type s struct {
	a, b, c		int
	d 			*s
}
```

下面给出了一个利用二叉树实现插入排序的例子：

```go
package main

import "fmt"

type tree struct {
	value       int
	left, right *tree
}

// 就地排序
func Sort(values []int) {
	var root *tree
	for _, v := range values {
		root = add(root, v)
	}
	appendValues(values[:0], root)
}

// appendValues将元素按照顺序追加到values里面，然后返回结构slice
func appendValues(values []int, t *tree) []int {
	if t != nil {
		values = appendValues(values, t.left)
		values = append(values, t.value)
		values = appendValues(values, t.right)
	}
	return values
}

func add(t *tree, value int) *tree {
	if t == nil {
		// 等于返回&tree{value: value}
		t = new(tree)
		t.value = value
		return t
	}
	if value < t.value {
		t.left = add(t.left, value)
	} else {
		t.right = add(t.right, value)
	}
	return t
}
```

**结构体的零值有结构体的成员的零值组成。通常情况下，我们希望零值是一个默认自然的、合理的值。 **

** 没有任何成员变量的结构体称为空结构体，写作`struct{}`。它没有长度，也不携带任何信息，但是有时候会很有用。有一些Go的程序员用它来代替别当做集合使用的`map`中的布尔值，来强调只有键是有用的，但是由于这种方式节约的内存很少并且语法复杂，所以，应该避免这样使用。**

```go
seen := make(map[string]struct{})	// 字符串集合
// ...
if _, ok := seen[s]; !ok {
	seen[s] = struct{}{}
	// ...首次出现s...
}
```

PS: 这里为什么会是`struct{}{}`呢？因为`struct{}`是一个结构体的类型，表示空的结构体，所以`struct{}{}`是一个`struct{}`类型的一个变量。就比如a结构体的一个变量可以是`a{}`。

## 结构体字面量

结构体类型的值可以通过结构体字面量来设置，即通过设置结构体成员来设置。

```go
type Point struct {
	x, y	int
}
p := Point{1, 2}
```

有两种格式的结构体字面量：

1. 第一种格式就像上面代码中的一样，它要求按照正确的顺序为每个成员变量指定一个值。这种方式的缺点就是必须为每个变量都赋值并且在阅读的时候必须记住每个变量出现的顺序，这种方式还会是的扩容或者重新排列的时候维护性差。所以，这种方式适合哪种成员变量少，有明显顺序的约定的小结构体中，比如上面的Point{x, y}。
2. 通过成员变量的名称指定全部或者部分成员变量的值来初始化结构体变量。如下面的：

	```go
	p := Point {
		x: 1,
		y: 2,
	}
	```

	这种方式中如果有的成员变量的值没有指定，那么它的初始值就是该成员变量类型的零值。因为这里通过名字来初始化，所以顺序也就无法为了。

这两种初始化方法不可以混用。第一种初始化方法也不能绕过不可导出的规则。

```go
package p

type T struct {
	a, b	int // a 和 b 都是不可导出的
}
package q

import "gopl/ch4/struct/p"

func Test() {
	var _ = p.T{a: 1, b: 2}	// 编译错误，无法引用 a, b 
	var _ = p.T{1, 2}		// 编译错误，无法引用 a, b
}
```

虽然上面的最后一行代码没有显示的提到不可导出变量，但是他们被隐式地引用了，所以这也是不允许的。

对于结构体的传参，和C语言类似，传参的时候会复制实际参数的一个副本，如果想要通过函数修改结构体的成员变量或者修改结构体变量，可以通过传递一个结构体指针的方式来完成。这里需要指出的一点就是尽量使用结构体指针来进行参数传递，因为结构体在传递的过程中需要复制一个副本，对于成员变量比较多的结构体是比较耗时的。

在大多数情况下结构体都是通过指针的方式使用，因此这里有一种简单的方式来创建、初始化一个结构体类型的变量并获取它的地址：

```go
pp := &Point{1, 2}
```

这个等价于

```go
pp := new(Point)
*pp = Point{1, 2}
```

但是&Point{1, 2} 这种方式可以直接使用在一个表达式中，例如函数调用。

## 结构体比较

如果结构体的并且结构体所有成员变量是可比较的，那么这个结构体就是可比较的，两个结构体比较的前提是两个结构体变量是同一类型的。两个结构体使用`==` 或者 `!=` 。其中 `==` 操作符按照顺序比较两个结构体变量的成员变量，所以下面的两个输出语句是等价的。

```go
ppp := Point{1, 2}
qqq := Point{1, 2}
fmt.Println(ppp.x == qqq.x && ppp.y == qqq.y)	// true
fmt.Println(p == q)									// true
```

和其他可比较类型一样，可比较的结构体类型都可以作为 `map` 键类型。

```go
type address struct {
	hostname string
	port	 int
}
hits := make(map[address]int)
hits[address{"golang.org", 433}]++
```

## 结构体嵌套和匿名成员

结构体嵌套机制可以让我们将一个命令结构体当做另一个结构体类型的匿名成员使用；并提供了一种方便的语法，使用简单的表达式就可以代表连续成员的值。

```go
type Circle struct {
	X, Y ,Radius int
}

type Wheel struct {
	X, Y, Radius, Spokes int
}
```

Circle类型定义了圆心的坐标和半径。Wheel拥有Circle类型的所有属性，还有一个Spokes属性，即车轮条幅的数量。

看了上面的代码就会发现 `Circle` 和 `Wheel` 是继承关系，但是在Go语言中没有继承，而Go语言取而代之的使用了一种匿名嵌套的方法来实现类型继承的功能。下面我们来说一下如何使用匿名嵌套来使得上面的两个结构体之间有“继承”关系。

为了理解匿名嵌套的含义，我们首先来使用组合的方式来重构上面的代码：

```go
type Point {
	X, Y
}

type Circle {
	Center Point
	Radius int
}
type Wheel {
	Circle Circle
	Spocks int
}
```

上面的代码结构看起来很清晰，但是如果需要访问 `Wheel` 的成员变量的时候就会变得比较麻烦：

```go
var w Wheel
w.Circle.Center.X = 8
w.Circle.Center.Y = 9
w.Circle.Radius = 5
w.Spocks = 20
```

下面的代码使用了匿名嵌套的方式重构了上面的代码：

```go
type Circle struct {
	Point
	Radius int
}

type Wheel struct {
	Circle
	Spokes int
}
```

Go语言可以定义不带名称的结构体成员，只需要指定类型即可；这种结构体成员称作匿名成员。这个结构体命名类型或者指向命令类型的指针。

```go
var w Wheel
w.X = 8
w.Y = 8
w.Radius = 9
w.Spokes = 20
```

在这里 ”匿名“成员的说法或许不合适，这是因为上面的 `Point` 和 `Circle` 始终是有名字的，名字就是对应的类型名，这时在通过 `.` 访问的时候可能省略中间所有的匿名成员。例如，下面的访问也是合法的：

```go
var ww Wheel
ww.Circle.Point.X = 6
ww.Circle.Y = 9
ww.Radius = 7
ww.Spokes = 1
```

匿名嵌套仅仅只是在使用 `.` 访问的时候比较方便，而在初始化的时候还是得按照普通结构体成员的方式来初始化。如下：

```go
// w2 : Wheel{8, 8, 10, 20}							// 编译错误
// w2 := Wheel{X: 8, Y: 8, Radius: 10, Spokes: 20}	// 编译错误
w2 := Wheel {
	Circle: Circle{
		Point: Point{5, 6},
		Radius: 10,
	},
	Spokes: 11,	// 注意这里的最后一个也必须是 ","
}
fmt.Println(w2)  // main.Wheel{Circle:main.Circle{Point:main.Point{X:5, Y:6}, Radius:10}, Spokes:11}
```

所以从这里我们可以很清楚的知道，匿名嵌套只是在访问的时候很方便，但是在初始化和其他方面和组合的方式没有什么不同。

“匿名成员”拥有隐式的名字，所以不能定义两个相同的匿名成员，否则就会起冲突。

** 匿名嵌套 只是在访问使用 `.` 访问的时候比较方便，其他的个组合毫无差异。 **


# Json

Json的全称是 `JavaScript Object Notation` ， Json是JavaScript对象的表示方法(JSON)是一种发送和接收格式化信息的标准。当然Json不是唯一的标准，常见的标准有XML，ASP.1和Google的Protocol Buffer等都是相似的标准，各有各的使用场景。但是因为Json的简单、客户性强且广泛支持，所以使用的最多。

Go语言的标准库 `encoding/json`、 `encoding/xml`、`encoding/asn1`和其他库对这些格式的编码提供了非常好的支持，这些库拥有相同的API。下面我们将对Json库的使用进行简单的介绍。

Json中的类型都能在Go语言中找到与之对应的，所以在Go语言中Json的使用时非常简单的。

下面来看一个简单的场景：

```go
type Movie struct {
	Title	string
	Year	int		`json:"released"`
	Color	bool	`json:"color,omitempty"`
	Actors	[]string
}

func main() {
	var movies = []Movie {
		{Title: "Casablance", Year: 1942, Color: false,
			Actors: []string{"Humphrey Bogart", "Ingrid Bergman"}},
		{Title: "Color Hand Luke", Year: 1967, Color: true,
			Actors: []string{"Paul Newman"}},
		{Title: "Bullitt", Year: 1968, Color: true,
			Actors: []string{"Steve McQueue", "Jacqueline Bisset"}},
	}
}
```

在Go语言中将一个Go语言的结构体变量转换成Json的操作称为 `marshal`。下面演示了将上面的 `movies` 转换成json对象。

```go
if err != nil {
	log.Fatal("JSON marshaling failed: %s", data)
}
fmt.Printf("%s\n", data)
```

Marshal生成了一个自己类型的slice，其中包含一个不带有任何多余空白的很长的字符串。如下：

```
[{"Title":"Casablance","released":1942,"Actors":["Humphrey Bogart","Ingrid Bergman"]},{"Title":"Color Hand Luke","released":1967,"color":true,"Actors":["Paul Newman"]},{"Title":"Bullitt","released":1968,"color":true,"Actors":["Steve McQueue","Jacqueline Bisset"]}]
```

为了方便阅读，有一个格式化的 `MarshalIndent`的变体可以输出整齐的格式化字符串。这和函数有两个参数，一个是定义每行的一个输出的一个前缀字符串，另一个是定义缩进的字符串。

```go
data2, err := json.MarshalIndent(movies, "#", "    ")
if err != nil {
	log.Fatalf("JSON marshaling failed: %s", err)
}
fmt.Printf("%s\n", data2)
```

输出结果如下：

```go
[
#    {
#        "Title": "Casablance",
#        "released": 1942,
#        "Actors": [
#            "Humphrey Bogart",
#            "Ingrid Bergman"
#        ]
#    },
#    {
#        "Title": "Color Hand Luke",
#        "released": 1967,
#        "color": true,
#        "Actors": [
#            "Paul Newman"
#        ]
#    },
#    {
#        "Title": "Bullitt",
#        "released": 1968,
#        "color": true,
#        "Actors": [
#            "Steve McQueue",
#            "Jacqueline Bisset"
#        ]
#    }
#]
```

`marshal`使用Go结构体(通过反射的方式)。只有可导出的成员可以转换为JSON字段。上面的结构体成员Year对应的转换为released，另外Color转换为color。这个是通过成员标签定义（field tag）实现的。成员标签定义是结构体在编译期间关联的一些元信息。

```go
Year int `json:"released"`
Color bool `json:"color,omitempty"
```

成员标签可以任意的字符串，但是按照习惯，是一组由空格分开的标签键值对`key:"value"`组成的；因为标签的值使用双引号括起来，所以一般标签都是原生的字符串的字面量。键的json控制包`encoding/json`的行为，其他的`encoding/…`包也类似。Color的标签还有一个额外的属性 `omitempty`，表示这个成员的值是零值或者空，则不输出这个成员到JSON中。

`marshal`的逆操作将JSON字符串进行解码为Go的数据结构，这个过程叫做`unmarshal`，这个是由 `json.Unmarshal`实现的。下面的代码演示了逆过程。

```go
var movies2 []Movie
if err := json.Unmarshal(data, &movies2); err != nil {
	log.Fatalf("JSON umarshaling failed: %s", err)
}
fmt.Println(movies2)
```

通过合理的定义Go的数据结构，我们可以选择将哪部分JSON数据解码到对应的结构体中。下面我们定义了一个结构体，这个结构体中只有一个 `Title`字段，所以在函数 `Unmarshal` 调用的时候，它只会将匹配到的 `Title`数据进行填充，而其余的将会被舍弃。

```go
var titles []struct {
		Title string
	}
	if err := json.Unmarshal(data, &titles); err != nil {
		log.Fatalf("JSON umarshal failed: %s", err)
	}
	fmt.Println(titles)	// [{Casablance} {Color Hand Luke} {Bullitt}]
```


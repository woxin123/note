# Java 中的注解


## 元注解

元注解的作用就是负责注解其他注解，Java 中的元注解如下：

1. @Target
2. @Rentention
3. @Documented
4. @Inherited
5. @Repteatable

### 1. @Target

@Traget 说明了 Annoation 所修饰的对象范围：Annotation 可被用于 packages、types（类、接口、枚举、Annotation类型）、类型成员（方法、构造方法、成员变量、枚举值）、方法参数和本地变量（如循环变量、catch 参数）。在 Annotation 类型的声明中使用了 target 可更加明晰其修饰的目标。

作用：用于描述注解的使用范围（即：被描述的注解可以用在什么地方）。

取值（ElementType）有：

1. CONSTRUCTOR: 用户描述构造器。
2. FIELD: 用于描述域即类成员变量。
3. LOCAL_VARIABLE: 用于描述局部变量。
4. METHOD: 用于描述方法。
5. PACKAGE: 用于描述包。
6. PARAMETER: 用于描述参数。
7. TYPE: 用于描述类、接口（包括注解类型）或 enum 声明。

### 2. Retention

@Retentation 定义了该 Annotation 被保留的事件长短: 某些 Annotation 进出现在源代码中，而被编译器对其；而另一写却被编译器编译到 class 文件中；编译在 class 文件中的 Annotation 可能会被虚拟机忽略，而另一写在 class 被装载时将被读取（注意并不影响 class 的执行，因为 Annotation 与 class 在使用上是分离的）。使用这个 meta-Annotation 可以对 Annotation 的 "声明周期" 限制。

作用：表示在需要什么级别保存该注解信息，用于描述注解的声明周期（即：被注解描述的注解在什么范围内有效），取值（RententionPoicy）有：

1. SOURCE: 在源文件中有效（即源文件保留）
2. CLASS: 在 class 文件中有效（即 class 保留）
3. RUNTIME: 在运行时有效（即运行时保留）

### 3. Documented

@Documented 用于描述其他类型的 annotation 应该被作为被标注的程序员的公共 API，因此可以被例如 javadoc 此类的工具文档化。Documented 时一个注解标记，没有成员。

### 4. Inherited

@Inherited 元注解时一个标记注解，@Inherited 阐述了某个被标注的类型时可以被继承的。如果一个使用了 @Inherited 修饰的 Annotation 类型被用于一个 class，则这个 Annotation 将被用于该 class 子类。

### 5. Repeatable

@Reapeatable 表示一个注解可以多次修饰同一个对象，比较多次使用在一个类上面。
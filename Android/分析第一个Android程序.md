# 分析你的第一个 Android 程序

HelloWorld 项目结构如下：

![Android项目结构](http://img.mcwebsite.top/20190923133826.png)

任何新建一个项目都会默认使用 Android 默认的项目结构，但这并不是真实的目录结构，而是被 Android Studio 转换过的。这种项目结构简单明了，适合进行快速开发。下图是 Android Studio 支持的项目结构。

![Android支持的项目结构](http://img.mcwebsite.top/20190923135522.png)

这里我们将项目结构切换称 Project，就是项目的真实结构了，如下图：

![Android 真实的项目结构](http://img.mcwebsite.top/20190923142156.png)

下面对 Android 的项目结构进行讲解：

1. .gradle 和 .idea

这两个目录都是 Android Studio 自动生成的一些文件，我们无须关心，也不需要手动编辑。

2. app

项目中的代码、资源等内容几乎都是放在这个目录下的，我们后面的开发基本也是在这个目录下进行的，下面还会展开介绍。

3. .gitignore

这个文件是用来将指定的目录或者文件排除在版本控制之外的。

4. gradle

这个目录下包含了 gradle wrapper 的配置文件，使用 gradle wrapper 的方式不需要提前将 gradle 下载好，而是会自动根据本地的缓存情况决定是否需要联网下载 gradle。Android Studio 默认没有启用 gradle wrapper 的方式，如果需要打开，可以点击 Android Studio 导航栏 -> File -> settting -> Build, Execution, Deployment -> Gradle，进行配置更改。

5. build.gradle

这个是项目的全局的 gradle 构建脚本，通常这个文件中的内容是不需要修改的。下面我们会将详细分析 gradle 脚本中的具体内容。

6. gradle.properties

这个文件是全局的 gradle 配置文件，在这里配置文件的属性将响应到项目中所有的 gradle 编译脚本。

7. gradlew 和 gradlew.bat

这两个文件是用来在命令行界面中执行 gradle 命令的，其中 gradlew 是在 linux 或者 Mac 系统中使用，gradlew.bat 是在 Windows 系统中使用的。

8. HelloWorld.iml

iml 文件是所有 InteliJ IDEA 项目都会自动生成的一个文件（Android Studio 是基于 InteliJ IDEA 开发的），用于标识这是一个 InteliJ IDEA 项目，我们不需要修改这个文件中的任何东西。

9. local.properties

这个文件用于指定本机中的 Android SDK 路径，通常内容都是自动生成的，我们并不需要修改。除非你本机中的 Android SDK 位置发生变化，那么这个文件中的路径该成新的位置即可。

11. settings.gradle

这个文件用于指定项目中所有引入的模块。由于 HelloWorld 项目中就只有一个 app 模块。因此该文件中也就只引入了 app 这一个模块。通常情况下模块的引入都是自动完成的，需要我们手动去修改这个文件的场景可能比较少。

现在整个项目的外层目录结构已经介绍完了。你会发现，除了 app 目录外，大多数的文件和目录都是自动生成的，我们并不需要修改。我们以后大部分的开发都会在 app 下完成，app 展开后的结构如下图所示：

![app 结构](http://img.mcwebsite.top/20190923150318.png)

下面我们对 app 目录下的内容进行跟为详细的分析：

1. build

这个目录主要包含了一些在编译时自动生成的文件，它里面的内容比较复杂，我们不需要过多关心。

2. libs

如果你的项目中要到了第三方 jar 包，就需要把这些 jar 包都放在 libs 目录下，放在这个目录下的 jar 包都会被自动添加到构建路径里去。

3. androidTest

此处是用来编写 Android Test 测试用例的，可以对项目进行一些自动测试。

4. java

毫无疑问，java 目录下放置我们所有 Java 代码的地方，展开目录，你将看到我们刚刚创建的 HelloWorldActivity 文件就在里面。

5. res

这个目录下的内容就有点多了。简单来说，就是你在项目中使用到的所有图片、布局、字符串等资源都要存在这个目录下。当然这个目录下还有需要子目录，图片放在 drawable 目录下，布局放在 layout 目录下，字符串放在 values 目录下，所以你不用担心会把这个 res 目录弄得乱糟糟的。

6. AndroidManifest.xml

这是你整个 Android 项目的配置文件，你在程序中定义的所有组件都需要在这个文件中注册，另外还可以在这个文件中添加程序添加权限声明。

7. test

此处是用来编写 Unit Test 测试用例的，是对项目进行测试的另一种方式。

9. app.iml

InteliJ IDEA 项目自动生成的文件，我们不需要关心或修改这个文件中的内容。

10. build.gradle

这是 app 模块进行项目构建脚本，这个文件中会指定很多项目构建相关的配置。

11. proguard-rules.pro

这个文件用于指定项目代码的混淆规则，当代码开发完成后打成安装包文件，如果不希望代码被别人破解，通常会对代码进行混淆，从而让破解者难以破解。

# HTTPS 详解

<!-- TOC -->

- [HTTPS 详解](#https详解)
    - [HTTP 的缺点](#http的缺点)
    - [加密与数字签名](#加密与数字签名)
    - [HTTPS 过程](#https过程)
        - [数字证书](#数字证书)
        - [SSL/TLS](#ssltls)
            - [SSL 握手](#ssl握手)

<!-- /TOC -->

再说 HTTPS 之前可以简单先说明一下 HTTP 的缺点。

##  HTTP 的缺点

1. 通信使用的是明文，内容可能被窃取。
2. 不验证通信方的身份，因为可能被窃听。
3. 无法正文报文的完整性，所以可能会遭到篡改。

HTTPS 通过加密和证书的方式解决了上面的问题。

## 加密与数字签名

1. 对称加密

    加密和解密使用同一个密钥。
    例如：DES、AES-GCM、Chacha20-Poly1305等。

2. 非对称加密

    加密使用的密钥和解密使用的密钥是不相同的，分别称为：公钥、私钥，公钥和算法都是公开的，但是私钥是保密的。对称加密算法的性能比较地，但是安全性高，非对称加密算法的数据长度也是有限制的。对于非对称加密，公钥加密的数据，私钥可以解开；私钥加密的数据，公钥可以解开。
    常见的非对称加密有：RSA、DSA、ECDSA、DH 等。

3. 哈希算法

    将任意长度的信息转换为比较短的固定长度的值，通常其长度要比信息小的多，且算法不可逆。严格意义上将哈希算法不能被称为加密算法。
    常见的哈希算法有：MD5、SHA1、SHA2、SHA256等。

4. 数字签名

    数字签名的作用是：
    1. 证明文档或者数据没有被篡改。
    2. 防止被人伪造数据。

    签名就是在信息后面再加上一段内孙，加上的这一步分可以证明这个文档没有被修改，而加上的这一步分是通过将文档使用哈希算法（如 MD5、SHA 等）生成一个摘要，然后使用非对称加密的私钥对这个经过哈希算法生成的摘要进行加密生成最终的签名。

    当被人拿到这个文档的时候需要验证这个文档是否被篡改或者验证这个文档是不是某人或者某个机构（进行数字签名）发布的，只需要使用这个机构或者人发布的公钥堆签名进行解密，然后获得文档的摘要，然后通过对原文档使用哈希加密算法生成新的摘要，将这个新的摘要和加密出来的摘要进行对比，如果相同的话，证明文档没有被修改。

## HTTPS 过程

HTTPS 中既使用了非对称加密也使用了对称加密。因为对称加密中在密钥传输的阶段不太安全，所以就在 HTTPS 的密钥传输阶段使用了非对称加密来提高密钥传输的安全性。

### 数字证书

证书包含了某个受信任的组织担保的用户或公司的相关信息。

数字证书中包含了一组信息，所有的这些信息是由一个官方的**证书颁发机构（CA）**以数字签名方法签名。

而且，数字证书还包含了颁发机构的公开密钥，以及颁发机构和所用签名的算法的描述信息。任何人都可以创建一个数字证书，但是不是所有人创建的证书都是安全的，因为黑客也可以创建证书，所以一般的证书都是受信任的机构颁发的。

![](http://img.mcwebsite.top/20190917150826.png)

X.509 v3 证书

通过 HTTPS 建立了一个安全 Web 事务后，现代的浏览器都会自动获取所连接服务器的数字证书。如果服务器没有数字证书，安全连接就会失败。

浏览器回收证书时会对证书进行验证，验证是否是受信任的权威机构颁发的。

如果堆数字证书的颁发机构一无所知，浏览器就无法确定是否应该信任这个签名颁发机构，它通常会被用户显示一个对话框，看看它是否信任这个签名的颁发者。

证书的作用就是防伪造。

### SSL/TLS

使用 HTTPS 需要在所有的请求和响应到达网络之前，都要进行加密。HTTPS 在 HTTP 下面提供了一个位于传输层和应用层之前的安全层——SSL，以及它的升级演进的后继者——传输安全层（Transport Layer Security, TSL）。

![](http://img.mcwebsite.top/20190917155938.png)

#### SSL 握手

握手分为 5 个步骤：

1. Client Hello

    客户端向服务端发送 Client Hello 消息，这个消息里包含了一个客户端的随机数 Random1、客户端支持的加密套件（Support Cliphers）和 SSL Version 等信息。通过 Wireshark 抓包，我们可以看到如下信息：

    ![Client Hello](http://img.mcwebsite.top/20190917160402.png)

2. Server Hello

    服务器端向客户端发送 Server Hello 消息，这个消息会从 Client Hello 传过来的 Support Ciphers 里选择服务端也支持的加密套件，这个套件决定了后续加密和生成摘要使用那些算法，另外还会生成一份随机数 Random2。注意，至此客户端和服务器端都用有了两个随机数（Random1 Random2），这两个随机数会在后续的生成对称密钥的时候用到。

    ![Server Hello](http://img.mcwebsite.top/20190917175033.png)

3. Certifcate

    这一步是服务端将自己的证书发给客户端，让客户端验证自动的身份，客户端通过验证后取出证书中的密钥。

    ![Certifcate](http://img.mcwebsite.top/20190917175354.png)

4. Server Key Exchange

    如果 DH 算法，这里发送给服务器使用的 DH 参数。RSA 算法不需要这一步。

    ![Server Key Exchange](http://img.mcwebsite.top/20190917175500.png)

5. Certificate Request

    Certificate Request 是服务器端要求客户端上传证书，这一步是可选的，对于安全性要求较高的场景会用到。

6. Server Hello Done

    Server Hello Done 通知客户端 Server Hello 过程结束。

    ![Server Hello Done](http://img.mcwebsite.top/20190917180005.png)

7. Certificate Verify

    客户端收到服务器端发送过来的证书，先从 CA 验证证书的合法性，验证通过后取出证书中的服务器端公钥，再生成一个随机数 Random3 ，在用服务器端的公钥非对称加密 Random3 生成 PreMaster Key。

8. Client Key Exchange

    上面客户端根据服务器端传过来的公钥生成 PreMaster Key，Client Key Excahnge 就是将这个 key 传送给服务端，服务端在用自己的私钥解密出这个 PreMaster Key 得到客户端生成的 Random3，至此，客户端和服务器端都有了 Random1 + Random2 + Random3，两边再根据同样的算法就可以生成一份密钥，握手结束后的应用层数据都是使用所这个密钥就行对称加密。

    ![Client Key Exchange](http://img.mcwebsite.top/20190917184516.png)

9. Change Clipher Spec(Client)

    这一步是客户端通知服务器端后面再发送消息都会使用前面协商出去的密钥加密了，是一条消息事件。

    ![Change Clipher Spec(Client)](http://img.mcwebsite.top/20190917184749.png)

10. Encrypted Handshake Message(Client)

    这一步对应的是 Client Finish 消息，客户端将签名的握手消息生成的摘要在用协商好的密钥进行加密，这是客户端发出的第一条消息。服务器端接收到后会用密钥解密，能解出来说明前面协商出来的密钥是一致的。

    ![Encrypted Handshake Message(Client)](http://img.mcwebsite.top/20190917185040.png)

11. Change Cipher Spec(Server)

    这一步是服务器通知客户端后面发送的消息都会加密，也是一条消息事件。

12. Encrypted Handshake Message(Server)

    这一步对应的是 Server Finish 消息，服务器端也会将握手过程中生成的消息摘再要密钥加密，这是服务器端发出的第一条消息。客户端也会使用密钥解密，能解出来说明协商的密钥是一致的。

    ![Encrypted Handshake Message(Server)](http://img.mcwebsite.top/20190917185245.png)
# secure-trans
信息安全传输工具
# 信息的安全传输，实现信息的安全传输工具



## 概要设计及需求
设计与实现一款局域网中点到点（即一台计算机到另外一台计算机）的信息传输工具，要求能够保证信息在传输过程中的保密性、完整性和发送/接收方的不可否认性，以防止通信线路上的窃听、泄露、破坏、篡改等不良操作。

## 程序工作流程及原理
##### 流程图

<img src="https://img-blog.csdnimg.cn/20210112125845728.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MzkzNTIxNQ==,size_16,color_FFFFFF,t_70" alt="img" style="zoom: 80%;" />

##### 包结构图

<img src="https://img-blog.csdnimg.cn/20210112130022845.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MzkzNTIxNQ==,size_16,color_FFFFFF,t_70" alt="img" style="zoom:80%;" />

##### 程序原理：
FileTransferServer类封装了WebSocket的服务器端，即为接受方B
FileTransferClient类封装了WebSocket的客户端，即为发送方A。
业务层实现了程序的主体功能，即 对于A：建立连接，加密，签名，打包文件，发送文件；对于B：接收文件，解密，验证签名。

整体工程量较大，首先要用WebSocket完善A与B的正常通信，其次涉及到密码学的知识，基本完成了 RSA 、DES和MD5的功能，包括加密字符串，加密文件，生成密钥，对文件生成摘要，生成签名等等，涉及到了大量的I/O操作；同时为使服务端能高效处理接受的文件，采用了多线程的方案。

对JAVA的GUI编程不是很熟悉，这个项目中只写了个软件界面，但并没有实现监听接口，赋予任何功能。

其实对于这个项目，掌握WebSocket网络编程很重要，特别是 结合密码学做到的安全传输，包括字符串加密，文件加密等等，保障了机密性，完整性以及不可否认性。

## 环境搭建
JDK1.8 / Eclipse / Windows 10


## 关键代码及结果展示

 1. 先开启服务端（演示是本机，所以IP为127.0.0.1）：
```java
FileTransferServer server = new FileTransferServer(); // 启动服务端
server.load();
```

对于load：

```java
public void load() throws Exception {
		while (true) {
			// server尝试接收其他Socket的连接请求，server的accept方法是阻塞式的
			Socket socket = this.accept();
			/**
			 * 我们的服务端处理客户端的连接请求是同步进行的， 每次接收到来自客户端的连接请求后， 都要先跟当前的客户端通信完之后才能再处理下一个连接请求。
			 * 这在并发比较多的情况下会严重影响程序的性能， 为此，我们可以把它改为如下这种异步处理与客户端通信的方式
			 */
			// 每接收到一个Socket就建立一个新的线程来处理它
			new Thread(new Task(socket)).start();
	}
}
```

 2. 打开客户端，建立连接
	
```java
public FileTransferClient() throws Exception {
		super(SERVER_IP, SERVER_PORT);
		this.client = this;
		System.out.println("Cliect[port:" + client.getLocalPort() + "] 成功连接服务端");
}
```
实现是多线程的，所以开启了三个客户端来发送文件：

```java
// 启动客户端连接
FileTransferClient client1 = new FileTransferClient();
FileTransferClient client2 = new FileTransferClient();
FileTransferClient client3 = new FileTransferClient();
// 开始传输文件
client1.sendFile(packageDesCipher); // 加密的DES密钥
// 三个线程之间一定要暂停，不会改变B服务端接收文件的顺序，但是会改变B对文件名的判定。
// （因为线程是异步的，B的if还没判断完毕，下个线程就改变了变量）
Thread.sleep(3000);
client2.sendFile(packageSigA); // 签名
Thread.sleep(3000);
client3.sendFile(fileCipher); // 加密的文件
```
如图：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210112131420721.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MzkzNTIxNQ==,size_16,color_FFFFFF,t_70)
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210112131429874.png)

 3. A接收到B的公钥，B接收到A的公钥

```java
System.out.println("【B】获得A的公钥为:" + FileTransferClient.mapA.get(0));
System.out.println("【A】获得B的公钥为:" + FileTransferServer.mapB.get(0));
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210112131514306.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210112131523294.png)

 4. A生成DES对称密钥

```java
String desKey = "923533706";
```

 5. A使用B的公钥对DES密钥进行加密

```java
String desCipher = RSAUtil.encrypt(desKey, FileTransferServer.mapB.get(0));
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210112131710499.png)

 6. A指定待传输的文件

```java
String filePath = "E:\\exp1\\tmpA\\mingwen.txt";
```
文件可以是任意类型，这里实例文本文件

 7. A用MD5对文件生成摘要A

```java
String md5AClient = MD5Util.getFileMD5String(filePath);
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210112131835702.png)

 8. A用A的私钥对摘要A生成签名

```java
String sigA = RSAUtil.sign(md5AClient, mapA.get(1));
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210112131900136.png)

 9. A用DES对称密钥对文件进行加密

```java
String destFile = "E:\\exp1\\tmpA\\miwen.txt";
String fileCipher = DESUtil.encryptFile(desKey, file.getAbsolutePath(), destFile);
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210112131929160.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210112131941865.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MzkzNTIxNQ==,size_16,color_FFFFFF,t_70)
![在这里插入图片描述](https://img-blog.csdnimg.cn/2021011213194934.png) 

10. A将加密后的DES密钥和签名写入文件

```java
String packageDesCipher = "E:\\exp1\\tmpA\\PackDesCipher.txt";
FileWriter fwriter2 = new FileWriter(packageDesCipher);
fwriter2.write(desCipher);

String packageSigA = "E:\\exp1\\tmpA\\PackSigA.txt";
FileWriter fwriter1 = new FileWriter(packageSigA);
fwriter1.write(sigA);
```

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210112132235837.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MzkzNTIxNQ==,size_16,color_FFFFFF,t_70)
![在这里插入图片描述](https://img-blog.csdnimg.cn/2021011213224482.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210112132257372.png)


 11. A发送文件

```java
client1.sendFile(packageDesCipher); // 加密的DES密钥
client2.sendFile(packageSigA); // 签名
client3.sendFile(fileCipher); // 加密的文件
```

 12. B接收文件并存入本地

```java
dis = new DataInputStream(socket.getInputStream());
//读取socket中的文件流，存入本地文件
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210112132354823.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MzkzNTIxNQ==,size_16,color_FFFFFF,t_70)



 13. B使用B的私钥来解密DES密钥

```java
desKeyB = RSAUtil.decrypt(tempDesKey, mapB.get(1));// 使用B的私钥解密
```

 14. B从文件中读取A的签名

```java
sigA = bReader.readLine();// 因为签名只有一行，且为字符串
```

 15. B使用解密得到的DES密钥来解密文件

```java
DESUtil.decryptFile(desKeyB, file.getAbsolutePath(), destFile);// 解密文件
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210112132504833.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MzkzNTIxNQ==,size_16,color_FFFFFF,t_70)


 16. B读取解密的文件并用MD5生成摘要B

```java
String md5BClient = MD5Util.getFileMD5String(destFile);
```
从上帝视角来说，摘要B与摘要A相同
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210112132543568.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210112132549244.png)

 17. B验证签名是否匹配
本该是 将解密的文件生成摘要，再同和 用RSA公钥解密签名生成的 摘要比较；（**逆向解密思维**）
将解密的文件生成摘要 和 读取的A的签名 和 A的公钥 验证是否匹配（**正向加密思维**）

```java
boolean check = RSAUtil.verify(md5BClient, sigA, FileTransferClient.mapA.get(0));
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210112132659157.png)

 
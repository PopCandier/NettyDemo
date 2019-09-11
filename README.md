# Netty

### TCP 粘包/拆包问题

TCP是个“流”协议，所谓流，就是没有界限的一大串数据，因为全部连在一起，像是一条河流，所以并没有明确的分界线。TCP底层并不了解上层业务数据的具体含义，它会根据TCP缓冲区的实际情况进行包的划分，所以在业务上认为，一个完整的包会被TCP拆成多个包进行发送，也有可能把多个小包封装成一个大的数据包发送，这就是所谓的TCP粘包和拆包问题。



#### TCP粘包/拆包发生的原因

* 应用程序write写入的字节大小大于套接口发送的缓冲区大小
* 进行MSS大小的TCP分段
* 以太网帧的payload大于MTU进行IP分片

#### 粘包问题的解决策略

由于底层的TCP无法理解上层业务的数据，所以在底层是无法保证数据包不被拆分和重组的，这个问题只能通过上层协议来解决。

* 消息定长，例如每个报文的大小固定为200字节，如果不够，空位补空格
* 在包尾增加回车符号进行分割，例如FTP协议
* 将消息分为消息头，消息体，消息头中包含消息总长度（或者消息体长度）的字段，通常设计思路为消息头的第一个字段使用int32来表示消息的总长度



#### Netty 的半包解码器解决TCP粘包/拆包问题

我们发送信息是这样的

```java
"QUERY TIME ORDER"+System.getProperty("line.separator")
```

这是一个带换行的标志，意味着`QUERY TIME ORDER`后面会接一个换行。

当我们加上

```java
 socketChannel.pipeline().addLast(new LineBasedFrameDecoder(1024));
 socketChannel.pipeline().addLast(new StringDecoder());
```

后将不会发生粘包现象。

`LineBasedFrameDecoder`工作原理依次遍历`ByteBuff`中可读字节，判断是否有]`\n` 

`\r\n`,如果有将会以此结束位置，从可读索引到结束位置组成一行。

所以，他是以换行符为结束标志的解码器。同时支持携带结束符和不带结束符两种解码方式，同时支持配置单行的配置长度，如果读取到最大长度后还是没有发现结束符，将会抛出异常，忽略掉之前读到了异常码流。

`StringDecoder`就是将接受到的对象转换为字符串。

所以`LineBasedFrameDecoder`雨`StringDecoder`就是非常简单按行切换的文本解码器。



#### 分隔符和定长解码器的应用

TCP 以流的方式进行数据传输，上层应用协议为了对消息进行区分，往往采用如下4种方式

* 消息长度固定，累计读取到长度综合为定长的LEN的报文后，就认为读取到了一个完整的消息；将计数器重置，重新开始读取下一个数据。
* 将回车换行符作为消息结束符，例如FTP协议，这种方式在文本协议中应用比较广泛
* 将特殊的分隔符作为消息的结束标志，回车换行符就是一种特殊的结束分隔符
* 通过在消息头定义长度字段来表示消息的总长度。

##### DelimiterBasedFrameDecoder

这个可以自己定义分隔符，当检测到内容的时候，将会截断。

```java
ByteBuf delimiter = Unpooled.copiedBuffer("$_".getBytes());
                          ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1024,delimiter));
```

##### FixedLengthFrameDecoder

```java
 ch.pipeline().addLast(new FixedLengthFrameDecoder(4));
```

固定长度，无论字符多长都会按照指定的数目截断

我们可以用cmd命令打开控制台输入`telnet localhost 8080`，连接本地服务进行代码的解释

#### 流行的序列化框架

`protobuf`谷歌、thrift（facebook）、Marshalling（JBoss）

##### 自定义的Netty序列化

|  字段名称   | 字段类型 |       备注       |
| :---------: | :------: | :--------------: |
|  subReqID   |   整型   |     订购编号     |
|  userName   |  字符串  |      用户名      |
| productName |  字符串  |   订阅产品名称   |
| phoneNumber |  字符串  |   订阅电话号码   |
|   address   |  字符串  | 订阅者的家庭住址 |

服务端接受到请求消息，对用户名进行合法校验，如果合法，则构造订购成功的应答消息返回给客户端。

| 字段名称 | 字段类型 |         备注         |
| :------: | :------: | :------------------: |
| subReqID |   整型   |       订购编号       |
| respCode |   整型   | 订购结果：0 表示成功 |
|   desc   |  字符串  |  可选的详细描述信息  |

我们将使用Netty的`ObjectEncoder`和`ObjectDecoder`对订购请求和应答消息进行序列化。

#### Google Protobuf 编解码


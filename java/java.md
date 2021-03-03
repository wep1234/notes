## hashmap



## 多线程

### 	1. synchronized

```shell
1.synchronized为jdk提供的java关键字,重量级锁
2.synchronized关键字解决的是多个线程之间访问资源的同步性， synchronized关键字可以保证被它修饰
的⽅法或者代码块在任意时刻只能有⼀个线程执⾏。
3.底层原理:通过获取monitor对象来获取本对象锁, 编译后的代码实际使用的是同步语句块的实现使⽤的是 monitorenter 和 monitorexit 指令，指向代码的开头和结尾。
```

### 	2.volatile

```shell
1.volatile关键字只能⽤于变量，⽽synchronized关键字可以修饰⽅法以及代码块
2.标识属性的可见性，线程每次更新属性、获取数据时，都会从主内存中获取最新的属性值。
3.volatile关键字是线程同步的轻量级实现，所以volatile性能肯定⽐synchronized关键字要好。
```

### 3.线程池

```she
1.使⽤线程池的好处:使⽤线程池的好处、使⽤线程池的好处、使⽤线程池的好处。
2.Runnable 接⼝不会返回结果或抛出检查异常，但是 Callable 接⼝可以。
```

### 	4.线程池使用

![](spring-security.assets/微信截图_20210208111834.png)

```shell
1.线程池构造器：ThreadPoolExecutor
2.corePoolSize、maximumPoolSize、workQueue、handler（饱和策略）、keepAliveTime（线程池中的线程数量⼤corePoolSize 的时候，如果这时没有新的任务提交，核⼼线程外的线程不会⽴即销毁，⽽是会等待，直到等待的时间超过）
3.队列类型：SynchronousQueue（阻塞队列）、LinkedBlockingQueue（无界队列） 和ArrayBlockingQueue（有界队列）
4.AbortPolicy（拒接新的任务）、CallerRunsPolicy（由主线程处理）、DiscardPolicy（丢弃任务）、DiscardOldestPolicy（丢弃最早的未处理的任务）
```

## mysql

### 	1.引擎 MyISAM、InnoDB

```she;ll
1.MyISAM只有表级锁⽽InnoDB⽀持⾏级锁和表级锁,默认为⾏级锁
2.InnoDB支持事务、外键、崩溃修复能力
```

### 	2.字段类型选择

```shell
1.尽量用整数代替字符串
2.定长和非定长的选择、默认值的设定
3.char、varchar 与 timestamp、datetime 选择
4.三大范式（1NF是对属性的原子性、2NF消除部分依赖、3NF是对字段的冗余性、消除传递依赖）、与反范式
```

```shell
1.时间类型
DATE 4 9999-12-31
TIMESTAMP 4 2038-01-19 03:14:07
DATETIME 8 9999-12-31 23:59:59
datetime 和 timestamp 类型所占的存储空间不同，前者8字节，后者4字节，这样造成的后果是两者能表示的时间范围不同。前者范围是 1000-01-01 00:00:00 ~ 9999-12-31 23:59:59，后者范围是 1970-01-01 8:00:01 ~ 2038-01-19 11:14:07. 所以 timestamp 支持的范围比 datetime 要小。
```

### 	3.索引

```shell
1.聚集索引：给表上了主键，那么表在磁盘上的存储结构就由整齐排列的结构转变成了B+树状结构,形成一个索引树，所以为聚集索引。
2.普通索引：最基本的索引，没有任何限制，是我们大多数情况下使用到的索引。业务逻辑简单时使用的比较多。
3.唯一索引：确定某个、或多个数据列将只包含彼此各不相同的值时，我们可以使用唯一所有unique index
	这么做的好处：a.一是简化了MySQL对这个索引的管理工作。b.自动检查新记录的这个字段的值是否已经在某个记录的这个字段里出现过。c.事实上，在许多场合，     创建唯一索引的目的往往不是为了提高访问速度，而只是为了避免数据出现重复。
4.覆盖索引：一般我在开发中用的比较多，select、where查询条件中的字段作为联合索引，这样就可以在查询的时候减少回表的操作，加上最左匹配原则。可很大程度上优化查询效率
5.索引下推：mysql5.6版本对覆盖索引做了进一步优化，即在索引内部首先会进行一系列的过滤，对于不满足条件的记录直接跳过，从而减少回表次数。
6.explain 查看sql执行计划
7.数据量庞大，数据分实现PreciseShardingAlgorithm的doSharding
```

## spring

#### 1.bean的创建流程

```she
1.读取class信息
2.创建beandefinition信息，beanClass、bean的抽象、bean的作用域（scope）、bean的注入模型、bean是否是懒加载等信息
3.刷新BeanFactory
4.执行BeanFactoryPostProcessor（修改beanName、给属性赋默认值）
5.实例化
6.填充属性
7.初始化
8.BeanPostProcessor（AutowiredAnnotationBeanPostProcessor）
9.单例池
```

## mq

### 1.rocketmq

```shell
1.如何保证rocketmq的消息顺序?
	1.producer：确保消息投递到同一queue，发送时，实现MessageQueueSelector接口的select方法，实现自己对key的算法
	2.消费端实现MessageListenerOrderly的consumeMessage方法，或者实现MessageListenerConcurrently接口设置并发数为1
2.消费消息是push还是pull?
	pull 如果broker主动推送消息的话有可能push速度快，消费速度慢的情况，那么就会造成消息在consumer端堆积过多，同时又不能被其他consumer消费的情	况。而pull的方式可以根据当前自身情况来pull，不会造成过多的压力而造成瓶颈。所以采取了pull的方式。
3.RocketMQ保证消息不丢失?
	1.从Producer分析：checkSendStatus，状态是OK，表示消息一定成功的投递到了Broker，状态超时或者失败，则会触发默认的2次补偿重试，如果还是发送失		败则报错。
	2.从Broker分析：
		a.消息支持持久化到Commitlog里面，并且Broker自身支持同步刷盘、异步刷盘的策略。
		b.Broker集群支持 1主N从的策略，支持同步复制和异步复制的方式，同步复制可以保证即使Master 磁盘崩溃，消息仍然不会丢失
	3.Consumer自身维护一个持久化的offset（对应MessageQueue里面的min offset），标记已经成功消费或者已经成功发回到broker的消息下标。
      如果Consumer消费失败，那么它会把这个消息发回给Broker，发回成功后，再更新自己的offset。
      如果Consumer消费失败，发回给broker时，broker挂掉了，那么Consumer会定时重试这个操作。RocketMQ可在broker.conf文件中配置Consumer端的重       试次数和重试时间间隔。
	  如果Consumer和broker一起挂了，消息也不会丢失，因为consumer 里面的offset是定时持久化的，重启之后，继续拉取offset之前的消息到本地。
4.rocketmq事务消息
	1.首先发送prepare消息给broker，对consumer不可见
	2.预发消息发送成功后执行本地事务
	3.根据本地事务执行的结果在返回给mq-server，Commit或Rollback
	4.如果是Commit，MQ把消息下发给Consumer端，如果是Rollback，直接删掉prepare消息。
	5.第3步的执行结果如果没响应，或是超时的，启动定时任务回查事务状态（最多重试15次，超过了默认丢弃此消息），处理结果同第4步。
```

### 2.RocketMQ与Kafka对比

```shell
1.RocketMQ支持异步实时刷盘，同步刷盘,Kafka使用异步刷盘方式
2.Kafka消费失败不支持重试，RocketMQ消费失败支持定时重试，每次重试间隔时间顺延
```

## 事务

### 1.Spring事务失效

```she
不带事务的方法调用该类中带事务的方法，不会回滚。因为spring的回滚是用过代理模式生成的，如果是一个不带事务的方法调用该类的带事务的方法，直接通过this.xxx()调用，而不生成代理事务，所以事务不起作用
```

### 2.cap理论

```she
一致性：在分布式系统完成某写操作后任何读操作，都应该获取到该写操作写入的那个最新的值。相当于要求分布式系统中的各节点时时刻刻保持数据的一致性。
可用性： 一直可以正常的做读写操作。简单而言就是客户端一直可以正常访问并得到系统的正常响应。
分区容错性：分布式系统中会有bug,硬件，网络等各种原因造成的故障，所以即使部分节点或者网络出现故障，我们要求整个系统还是要继续使用的。



```

### 3.seata

```she
1.下载seata-server服务
2.registry.conf 
	注册中心的配置：指定registry.conf中registry块中的的type：file、nacos、zk等
	seata配置中心：config块中的type：file、nacos
3.file.conf
	store 事务日志存储：file、db
4.启动nacos、seata服务
5.项目引入spring-cloud-starter-alibaba-seata
6.在事务的管理者上添加@globalTransctional注解用来管理全局事务

```

## spring-security

```she
1.spring security提供了userDetailsService,其定义了唯一的认证方法：loadUserByUsername
2.spring security内部自动匹配密码是否正确的时候，一定要进行加密和解密。要求spring容器中一定要存在一个passwordEncoder实现类对象，对象中一定要有加密、解密的逻辑
```

## nacos

### 1.使用nacos做为配置中心

 ```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
    <version>2.1.0.RELEASE</version>
</dependency>
 ```

### 2. 配置

```she
0.命名空间（微服务隔离）
1.默认新增的命名空间都在public空间
2.配置集：所有配置的集合
3.配置集   id： 配置文件名 data ID
4.配置分组:   group （利用分组来区分不同的环境 dev、test、prod）
 
```

### 性能压测

```she
1.jmeter安装  http://jmeter.apache.org/download_jmeter.cgi 
2.执行jmeter.bat
3.jemter压测时，会出现地址端口被占用错误，可以再windows注册表中设置maxuserport最大端口数65534，tcptimedwaitdelay时间30（tcp端口回收时间）
```

### 性能监控

​	1.jvisualvm







​    
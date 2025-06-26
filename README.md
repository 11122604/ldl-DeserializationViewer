# ldl-DeserializationViewer

> A powerful visualization tool for Redis serialized data that converts Java serialized cache data to readable JSON format without requiring original DTO classes.

一款强大的Redis序列化数据可视化工具，能够将Java序列化的缓存数据转换为可读的JSON格式，无需原始DTO类定义。

---

## 🤔 Why - 为什么需要这个工具

### 痛点问题
在实际开发和运维过程中，我们经常遇到以下问题：

- **Redis数据不可读**：存储在Redis中的Java序列化数据以二进制形式存在，无法直接查看内容
- **调试困难**：排查缓存相关问题时，无法快速查看缓存对象的具体内容和结构
- **依赖缺失**：生产环境中往往缺少原始的DTO类定义，传统反序列化方法失效
- **版本兼容性**：不同版本的serialVersionUID导致反序列化失败
- **运维盲区**：运维人员无法直观了解缓存数据的业务含义

### 解决方案
ldl-DeserializationViewer 通过**动态字节码生成技术**，实现了：
- ✅ **无依赖反序列化**：无需原始DTO类，无需serialVersionUID
- ✅ **实时数据查看**：直接将序列化数据转换为JSON格式
- ✅ **多环境支持**：支持开发、测试、生产等多环境配置
- ✅ **零侵入部署**：既可以作为独立工具，也可以集成到现有系统

---

## 📋 What - 这是什么工具

### 核心功能

#### 🔍 序列化数据可视化
- 将Redis中的Java序列化字节数据转换为可读JSON
- 支持复杂对象结构的完整还原
- 自动处理基本数据类型和对象引用

#### 🛠️ 双模式运行
- **Web模式**：Spring Boot + Swagger UI，提供友好的Web界面
- **命令行模式**：独立JAR包，支持脚本化操作

#### 🏗️ 智能类生成
- 基于**Javassist**和**ASM**的双重字节码生成引擎
- 动态创建类结构，完全兼容Java序列化规范
- 智能类型推断和字段映射

#### 🌐 多环境管理
- 支持预配置的环境切换（DEV/TEST/PROD）
- 灵活的Redis连接配置（单机/集群）
- 密码认证和安全连接支持

### 技术架构

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Web Interface │    │  Command Line    │    │   Redis Cluster │
│   (Swagger UI)  │    │     Tool         │    │   / Standalone  │
└─────────┬───────┘    └────────┬─────────┘    └─────────┬───────┘
          │                     │                        │
          └──────────┬──────────┘                        │
                     │                                   │
          ┌──────────▼──────────┐                        │
          │  DeserializationViewer │◄──────────────────┘
          │    Business Logic     │
          └──────────┬──────────┘
                     │
          ┌──────────▼──────────┐
          │  ByteCode Generator  │
          │  ┌────────────────┐ │
          │  │   Javassist    │ │
          │  └────────────────┘ │
          │  ┌────────────────┐ │
          │  │      ASM       │ │
          │  └────────────────┘ │
          └─────────────────────┘
```

### 核心技术栈
- **Spring Boot 2.3.3** - Web框架
- **Jedis 2.9.0** - Redis客户端
- **Javassist 3.28.0** - 字节码生成
- **ASM 9.2** - 字节码操作
- **FastJSON 1.2.68** - JSON序列化
- **Swagger 2.9.2** - API文档

---

## 🚀 How - 如何使用

### 快速开始

#### 1. 项目构建

```bash
# 克隆项目
git clone <repository-url>
cd ldl-DeserializationViewer

# 编译项目
mvn clean package
```

#### 2. 配置环境

创建环境配置文件：

**Windows**: `C:\Users\{username}\Desktop\hostconfig.properties`  
**Linux**: `/data/cdp/read_redis/hostconfig.properties`

配置格式：
```properties
# 环境名#Redis地址@密码
DEV#127.0.0.1:6379@password123
TEST#192.168.1.100:6379
PROD#192.168.1.200:6379,192.168.1.201:6379@prodpass
```

### 使用方式

#### 🌐 Web模式

1. **启用Spring Boot打包**
   ```xml
   <!-- 在pom.xml中启用 -->
   <plugin>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-maven-plugin</artifactId>
   </plugin>
   ```

2. **启动服务**
   ```bash
   java -jar deserialization-viewer-0.0.1-SNAPSHOT.jar &
   ```

3. **访问界面**
   ```
   http://localhost:8087/deserialization/swagger-ui.html
   ```

4. **API使用**
   
   **根据主机查询**：
   - 接口：`GET /search`
   - 参数：`ipAndPort`, `password`(可选), `key`
   - 示例：`/search?ipAndPort=127.0.0.1:6379&key=user:1001`

   **根据环境查询**：
   - 接口：`GET /searchByEnv`
   - 参数：`envName`, `key`
   - 示例：`/searchByEnv?envName=TEST&key=user:1001`

#### 💻 命令行模式

1. **启用命令行打包**
   ```xml
   <!-- 在pom.xml中启用 -->
   <plugin>
       <artifactId>maven-assembly-plugin</artifactId>
   </plugin>
   ```

2. **直接IP访问**
   ```bash
   java -jar deserialization-viewer-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
        192.168.1.100:6379@password123 user:1001
   ```

3. **环境配置访问**
   ```bash
   java -jar deserialization-viewer-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
        TEST user:1001
   ```

### 输出示例

```json
{
  "success": true,
  "data": {
    "userId": 1001,
    "userName": "张三",
    "email": "zhangsan@example.com",
    "createTime": "2024-01-15T10:30:00",
    "profile": {
      "age": 28,
      "city": "北京",
      "interests": ["编程", "阅读", "旅行"]
    }
  },
  "message": null
}
```

### 高级配置

#### 应用配置 (`application.properties`)
```properties
# 服务配置
spring.application.name=DeserializationViewer
server.port=8087
server.servlet.context-path=/deserialization

# 环境配置
env.names=dev,test,product
host.config.windows=C:\\Users\\config\\hostconfig.properties
host.config.linux=/data/config/hostconfig.properties
```

#### 集群Redis支持
```bash
# 集群地址用逗号分隔
CLUSTER#192.168.1.100:7000,192.168.1.101:7000,192.168.1.102:7000@clusterpass
```

### 故障排除

#### 常见问题

1. **类加载失败**
   ```
   问题：自定义加载失败 com.example.UserDTO
   解决：检查序列化数据完整性，确认Redis连接正常
   ```

2. **配置文件未找到**
   ```
   问题：配置文件读取失败
   解决：确认hostconfig.properties路径正确，文件格式符合要求
   ```

3. **Redis连接失败**
   ```
   问题：查询redis失败
   解决：检查网络连接、Redis服务状态、密码配置
   ```

#### 日志配置
项目使用Log4j2，可通过`log4j2-spring.xml`调整日志级别：
```xml
<Logger name="com.datalight.tools.deserialization" level="DEBUG"/>
```

---

## 📊 性能特性

- **零依赖反序列化**：无需原始类定义
- **内存高效**：流式处理，支持大对象
- **并发安全**：无状态设计，支持多线程访问
- **错误降级**：反序列化失败时返回原始字符串

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支：`git checkout -b feature/AmazingFeature`
3. 提交更改：`git commit -m 'Add some AmazingFeature'`
4. 推送分支：`git push origin feature/AmazingFeature`
5. 提交Pull Request

## 📄 许可证

本项目基于 [LICENSE](LICENSE) 许可证开源。

---

## 💡 技术原理

### 动态类生成原理
1. 解析`ObjectStreamClass`获取类元信息
2. 使用Javassist/ASM动态生成字节码
3. 创建自定义ClassLoader加载生成的类
4. 通过标准Java序列化API完成反序列化

### 兼容性保证
- 支持JDK 1.8+
- 兼容Redis 2.x - 6.x
- 支持Spring Boot 2.x

**让Redis中的序列化数据不再是黑盒！** 🎯




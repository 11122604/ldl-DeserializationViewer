# ldl-DeserializationViewer

> A powerful visualization tool for Redis serialized data that converts Java serialized cache data to readable JSON format without requiring original DTO classes.

A powerful Redis serialized data visualization tool that converts Java serialized cache data into readable JSON format without requiring original DTO class definitions.

---

## 🤔 Why - Why Do We Need This Tool

### Pain Points
In actual development and operations, we often encounter the following problems:

- **Unreadable Redis Data**: Java serialized data stored in Redis exists in binary form and cannot be viewed directly
- **Debugging Difficulties**: When troubleshooting cache-related issues, we cannot quickly view the specific content and structure of cached objects
- **Missing Dependencies**: Production environments often lack original DTO class definitions, causing traditional deserialization methods to fail
- **Version Compatibility**: Different versions of serialVersionUID lead to deserialization failures
- **Operations Blind Spots**: Operations personnel cannot intuitively understand the business meaning of cached data

### Solution
ldl-DeserializationViewer achieves the following through **dynamic bytecode generation technology**:
- ✅ **Dependency-free Deserialization**: No need for original DTO classes or serialVersionUID
- ✅ **Real-time Data Viewing**: Directly converts serialized data to JSON format
- ✅ **Multi-environment Support**: Supports development, testing, production and other multi-environment configurations
- ✅ **Zero-intrusion Deployment**: Can be used as a standalone tool or integrated into existing systems

---

## 📋 What - What Is This Tool

### Core Features

#### 🔍 Serialized Data Visualization
- Converts Java serialized byte data in Redis to readable JSON
- Supports complete restoration of complex object structures
- Automatically handles primitive data types and object references

#### 🛠️ Dual Mode Operation
- **Web Mode**: Spring Boot + Swagger UI, providing a friendly web interface
- **Command Line Mode**: Standalone JAR package, supporting scripted operations

#### 🏗️ Intelligent Class Generation
- Dual bytecode generation engine based on **Javassist** and **ASM**
- Dynamically creates class structures, fully compatible with Java serialization specifications
- Intelligent type inference and field mapping

#### 🌐 Multi-environment Management
- Supports pre-configured environment switching (DEV/TEST/PROD)
- Flexible Redis connection configuration (standalone/cluster)
- Password authentication and secure connection support

### Technical Architecture

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

### Core Technology Stack
- **Spring Boot 2.3.3** - Web framework
- **Jedis 2.9.0** - Redis client
- **Javassist 3.28.0** - Bytecode generation
- **ASM 9.2** - Bytecode manipulation
- **FastJSON 1.2.68** - JSON serialization
- **Swagger 2.9.2** - API documentation

---

## 🚀 How - How to Use

### Quick Start

#### 1. Project Build

```bash
# Clone the project
git clone <repository-url>
cd ldl-DeserializationViewer

# Compile the project
mvn clean package
```

#### 2. Environment Configuration

Create environment configuration file:

**Windows**: `C:\Users\{username}\Desktop\hostconfig.properties`  
**Linux**: `/data/cdp/read_redis/hostconfig.properties`

Configuration format:
```properties
# EnvironmentName#RedisAddress@Password
DEV#127.0.0.1:6379@password123
TEST#192.168.1.100:6379
PROD#192.168.1.200:6379,192.168.1.201:6379@prodpass
```

### Usage Methods

#### 🌐 Web Mode

1. **Enable Spring Boot Packaging**
   ```xml
   <!-- Enable in pom.xml -->
   <plugin>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-maven-plugin</artifactId>
   </plugin>
   ```

2. **Start Service**
   ```bash
   java -jar deserialization-viewer-0.0.1-SNAPSHOT.jar &
   ```

3. **Access Interface**
   ```
   http://localhost:8087/deserialization/swagger-ui.html
   ```

4. **API Usage**
   
   **Query by Host**:
   - Endpoint: `GET /search`
   - Parameters: `ipAndPort`, `password`(optional), `key`
   - Example: `/search?ipAndPort=127.0.0.1:6379&key=user:1001`

   **Query by Environment**:
   - Endpoint: `GET /searchByEnv`
   - Parameters: `envName`, `key`
   - Example: `/searchByEnv?envName=TEST&key=user:1001`

#### 💻 Command Line Mode

1. **Enable Command Line Packaging**
   ```xml
   <!-- Enable in pom.xml -->
   <plugin>
       <artifactId>maven-assembly-plugin</artifactId>
   </plugin>
   ```

2. **Direct IP Access**
   ```bash
   java -jar deserialization-viewer-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
        192.168.1.100:6379@password123 user:1001
   ```

3. **Environment Configuration Access**
   ```bash
   java -jar deserialization-viewer-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
        TEST user:1001
   ```

### Output Example

```json
{
  "success": true,
  "data": {
    "userId": 1001,
    "userName": "John Doe",
    "email": "johndoe@example.com",
    "createTime": "2024-01-15T10:30:00",
    "profile": {
      "age": 28,
      "city": "New York",
      "interests": ["Programming", "Reading", "Travel"]
    }
  },
  "message": null
}
```

### Advanced Configuration

#### Application Configuration (`application.properties`)
```properties
# Service configuration
spring.application.name=DeserializationViewer
server.port=8087
server.servlet.context-path=/deserialization

# Environment configuration
env.names=dev,test,product
host.config.windows=C:\\Users\\config\\hostconfig.properties
host.config.linux=/data/config/hostconfig.properties
```

#### Redis Cluster Support
```bash
# Cluster addresses separated by commas
CLUSTER#192.168.1.100:7000,192.168.1.101:7000,192.168.1.102:7000@clusterpass
```

### Troubleshooting

#### Common Issues

1. **Class Loading Failure**
   ```
   Issue: Custom loading failed com.example.UserDTO
   Solution: Check serialized data integrity, confirm Redis connection is normal
   ```

2. **Configuration File Not Found**
   ```
   Issue: Configuration file reading failed
   Solution: Confirm hostconfig.properties path is correct, file format meets requirements
   ```

3. **Redis Connection Failure**
   ```
   Issue: Redis query failed
   Solution: Check network connection, Redis service status, password configuration
   ```

#### Log Configuration
The project uses Log4j2, you can adjust log levels through `log4j2-spring.xml`:
```xml
<Logger name="com.datalight.tools.deserialization" level="DEBUG"/>
```

---

## 📊 Performance Features

- **Zero-dependency Deserialization**: No need for original class definitions
- **Memory Efficient**: Stream processing, supports large objects
- **Concurrency Safe**: Stateless design, supports multi-threaded access
- **Error Degradation**: Returns original string when deserialization fails

## 🤝 Contributing

1. Fork this repository
2. Create a feature branch: `git checkout -b feature/AmazingFeature`
3. Commit your changes: `git commit -m 'Add some AmazingFeature'`
4. Push to the branch: `git push origin feature/AmazingFeature`
5. Submit a Pull Request

## 📄 License

This project is open source under the [LICENSE](LICENSE) license.

---

## 💡 Technical Principles

### Dynamic Class Generation Principle
1. Parse `ObjectStreamClass` to get class metadata
2. Use Javassist/ASM to dynamically generate bytecode
3. Create custom ClassLoader to load generated classes
4. Complete deserialization through standard Java serialization API

### Compatibility Guarantee
- Supports JDK 1.8+
- Compatible with Redis 2.x - 6.x
- Supports Spring Boot 2.x

### Project Structure

```
src/main/java/com/datalight/tools/deserialization/
├── controller/          # REST API controllers
├── service/            # Business logic layer
├── model/              # Data models
├── core/               # Core utilities (bytecode generation)
├── config/             # Configuration classes (Swagger etc.)
└── DeserializationViewerApplication.java  # Command line entry
```

### Author Information
- **Author**: 1053459255@qq.com
- **Since**: 2025-06-26

**Make serialized data in Redis no longer a black box!** 🎯

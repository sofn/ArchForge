# 通用JSON转换器使用说明

## 概述

本项目提供了一个通用的JSON转换器，可以避免为每个需要JSON转换的DTO类型都手动创建Converter类。该转换器使用Spring Boot框架配置的统一ObjectMapper，确保整个应用的JSON处理一致性。

## 方案一：使用AbstractJsonConverter（推荐）

### 1. 创建具体的Converter类

为每个需要JSON转换的DTO类型创建一个对应的Converter类：

```java
@Converter
public class MetaInfoConverter extends AbstractJsonConverter<MetaDTO> {
    
    @Override
    protected Class<MetaDTO> getTargetType() {
        return MetaDTO.class;
    }
}
```

### 2. 在Entity中使用

```java
@Entity
public class SysMenu extends BaseEntity<SysMenu> {
    
    @Convert(converter = MetaInfoConverter.class)
    @Column(columnDefinition = "TEXT")
    private MetaDTO metaInfo;
    
    // 其他字段...
}
```

### 3. 为新类型创建Converter

当需要为新的DTO类型添加JSON转换功能时，只需创建对应的Converter类：

```java
@Converter
public class ExtraIconConverter extends AbstractJsonConverter<ExtraIconDTO> {
    
    @Override
    protected Class<ExtraIconDTO> getTargetType() {
        return ExtraIconDTO.class;
    }
}
```

然后在Entity中使用：

```java
@Convert(converter = ExtraIconConverter.class)
@Column(columnDefinition = "TEXT")
private ExtraIconDTO extraIcon;
```

## 方案二：使用JsonConverter工厂类

如果需要动态获取转换器，可以使用工厂类：

```java
// 获取指定类型的转换器
AttributeConverter<MetaDTO, String> converter = JsonConverter.forType(MetaDTO.class);

// 手动转换
String jsonString = converter.convertToDatabaseColumn(metaDTO);
MetaDTO metaDTO = converter.convertToEntityAttribute(jsonString);
```

## 优势

1. **代码复用**：所有JSON转换逻辑集中在AbstractJsonConverter中
2. **类型安全**：编译时类型检查，避免运行时错误
3. **易于维护**：统一的错误处理和日志记录
4. **简单易用**：为新类型创建Converter只需要几行代码
5. **配置统一**：使用Spring Boot配置的统一ObjectMapper，包括：
   - Java 8时间类型支持（LocalDateTime、LocalDate等）
   - 统一的日期格式化
   - 空值处理（NON_NULL策略）
   - 未知属性忽略策略
   - 与整个应用JSON配置保持一致
6. **性能优化**：共享Spring管理的ObjectMapper实例，避免重复创建

## 注意事项

1. 所有需要JSON转换的DTO类必须有无参构造函数
2. DTO类应该符合Jackson的序列化/反序列化要求
3. 建议在数据库中使用TEXT类型存储JSON数据
4. 复杂对象可能需要自定义Jackson配置
5. **重要**：SpringContextHolder依赖于Spring上下文初始化，确保在Spring应用启动后再使用JSON转换器
6. ObjectMapper配置在`JacksonConfig`中统一管理，包含Java 8时间模块、空值处理等配置

## 示例

完整的Entity字段配置示例：

```java
@Entity
@Table(name = "sys_menu")
public class SysMenu extends BaseEntity<SysMenu> {
    
    // 基本字段
    private String menuName;
    private Integer menuType;
    
    // JSON字段
    @Convert(converter = MetaInfoConverter.class)
    @Column(columnDefinition = "TEXT")
    private MetaDTO metaInfo;
    
    // 另一个JSON字段
    @Convert(converter = ExtraIconConverter.class)
    @Column(columnDefinition = "TEXT")
    private ExtraIconDTO extraIcon;
    
    // getters and setters...
}
```

这样配置后，JPA会自动处理MetaDTO和ExtraIconDTO与JSON字符串之间的转换，无需手动处理序列化逻辑。
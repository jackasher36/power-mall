# 动力商场项目

这是一个SpringBoot加SpringCloud项目, 为了巩固SpringBoot基础和学习Springcloud, 还是觉得以项目驱动为学习, 选择该项目因为有提供数据库表的设计, 经过观察, 项目很细致, 甚至讲解了utf8和utf8mb4有什么区别,

### 后端服务器(Power-Mall)

下载该项目需要 mysql8,redis6, nacos2, 后端主要配置一下 nacos 就可以启动,nacos 配置已经在配置文件里面注解

### 管理页面前端(mall4v)

我是 mac 电脑,下载的时候npm启动出现问题,好像什么 Mac 不支持这个库, 将 node-sass 改为 sass,package.json 和 lock 文件都改了之后,用 yarn 下载, yarn dev 启动

### 微信小程序前端(mall4m)

这个改下端口号就可以,默认好像就是 127.0.0.1



以下是项目心得:

## 关于权限限定

基于RPAC0,采用用户-角色-权限

![image-20240924024029187](https://p.ipic.vip/3a8mgn.png)

![image-20240924024101728](https://p.ipic.vip/tq94fe.png)

## 数据库设计

### sys系统表

![image-20240924025448462](https://p.ipic.vip/xwmsvi.png)

### prod表

![image-20240924025627447](https://p.ipic.vip/gj9e41.png)

### Notice index_img area 表是独立的表,可以认为是pojo表

### 会员表

![image-20240924043129770](https://p.ipic.vip/8tbxbs.png)

### 订单

![image-20240924043958542](https://p.ipic.vip/hk7tzs.png)

## Maven的体验

以前一直使用idea, 从来没有感受Maven本身的作用,现在我们用文本编辑器写一个java应用试试

![image-20240924050446759](https://p.ipic.vip/4tb3q8.png)

Maven很神奇的可以帮我们编译运行打包

### Maven插件

这个插件可以帮我们把依赖也打进去,

![image-20240924181103385](https://p.ipic.vip/smek1m.png)

## Nacos

## 什么是Nacos

##### Nacos是一个注册服务和拉取配置的组件,是一个系统组件,需要单独下载运行,

```yml
spring:
  application:
    name: gateway-server  # 微服务名称
  cloud:
    nacos:
      server-addr: 127.0.0.1:8848
      username: nacos
      password: nacos
      discovery:  # nacos注册中心配置
        namespace: 3fe590e0-91a7-4926-91e3-8bc86e78b4d8
        group: A_GROUP
        service: ${spring.application.name}
      config: # nacos配置中心配置
        namespace: ${spring.cloud.nacos.discovery.namespace}
        group: ${spring.cloud.nacos.discovery.group}
        prefix: ${spring.application.name}
        file-extension: yml
  profiles:
    active: dev # 多环境配置::
```

```cmd
#发现一个有意思的命令, 可以跟踪文件末尾,用于查看日志
tail -f 
```

## 关于无法添加命名空间问题

成功了!!!!!!!好开心, Nacos需要数据库来存储数据,首先我们看看官方怎么说明配置

![image-20240924183811149](https://p.ipic.vip/toa2gu.png)

那么我们需要找到配置文件配置数据源,然后在数据库里面运行好sql数据

![image-20240924183924845](https://p.ipic.vip/gq08n9.png)

![image-20240924183941951](https://p.ipic.vip/kq3oea.png)

## 启动后就可以使用了!

## Java注册Nacos

我真的会谢,先是netty出错,什么找不到Macos的DNS解析,我找了个Netty的包导入,解决了

```xml
     <dependency>
            <groupId>io.netty</groupId>
            <artifactId>netty-all</artifactId>
        </dependency>
```

然后, 配置完后,一直没法注册服务,结果是左上角有个命名空间选择,害得我研究了好久

![image-20240924212126808](https://p.ipic.vip/m4fjh8.png)

Nacos我可以理解为能够用服务名找ip, 以及公共配置,如图SpringBoot配置所有组件, nacos配置所有服务

## Nacos配置拉取

奇怪,这是在是太奇怪了,原本的项目只是多加了一个Netty好像就无法拉取配置,重新解压后就可以读取了,可以看到端口正常运行80

![image-20240925004313523](https://p.ipic.vip/kix3uv.png)



对比后,发现原来是配置文件这里没有命名空间,这样一个错误却让我麻烦了这么久

![image-20240925004611104](https://p.ipic.vip/3y6vw5.png)

而且拉取配置,在主程序无法生效,只有Controller才可以,不知道怎么做的,必须是@Value注解

![image-20240925004835010](https://p.ipic.vip/f3soxa.png)

## 网关模块

在网关模块,配置网关,只放行白名单,需要的常量放在common包里面, 如Token名字, bear名字,

![image-20240925160300156](https://p.ipic.vip/6efc9n.png)

常量采用接口,是因为接口不再需要使用static了

```java
package com.powernode.constant;

/**
 * 认证授权常量类
 */
public interface AuthConstants {

    /**
     * 在请求头中存放token值的KEY
     */
    String AUTHORIZATION = "Authorization";

    /**
     * token值的前缀
     */
    String BEARER = "bearer ";

    /**
     * token值存放在redis中的前缀
     */
    String LOGIN_TOKEN_PREFIX = "login_token:";


    /**
     * 登录URL
     */
    String LOGIN_URL = "/doLogin";

    /**
     * 登出URL
     */
    String LOGOUT_URL = "/doLogout";

    /**
     * 登录类型
     */
    String LOGIN_TYPE = "loginType";

    /**
     * 登录类型值：商城后台管理系统用户
     */
    String SYS_USER_LOGIN = "sysUserLogin";

    /**
     * 登录类型值：商城用户购物车系统用户
     */
    String MEMBER_LOGIN = "memberLogin";


    /**
     * TOKEN有效时长（单位：秒，4个小时）
     */
    Long TOKEN_TIME = 14400L;

    /**
     * TOKEN的阈值：3600秒（1个小时）
     */
    Long TOKEN_EXPIRE_THRESHOLD_TIME = 60*60L;
}

```

### `Result`设计

result里面包括了数据data,以及响应码和msg,为了快速生成Result, 使用静态方法,提前对一些常见的Result进行封装,

```java
package com.powernode.model;

import com.powernode.constant.BusinessEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 项目统一响应结果对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("项目统一响应结果对象")
public class Result<T> implements Serializable {

    @ApiModelProperty("状态码")
    private Integer code = 200;

    @ApiModelProperty("消息")
    private String msg = "ok";

    @ApiModelProperty("数据")
    private T data;

    /**
     * 操作成功
     * @param data
     * @return
     * @param <T>
     */
    public static <T> Result<T> success(T data) {
        Result result = new Result<>();
        result.setData(data);
        return result;
    }

    /**
     * 操作失败
     * @param code
     * @param msg
     * @return
     * @param <T>
     */
    public static <T> Result<T> fail(Integer code,String msg) {
        Result result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(null);
        return result;
    }

    /**
     * 操作失败
     * @param businessEnum
     * @return
     * @param <T>
     */
    public static <T> Result<T> fail(BusinessEnum businessEnum) {
        Result result = new Result();
        result.setCode(businessEnum.getCode());
        result.setMsg(businessEnum.getDesc());
        result.setData(null);
        return result;
    }

    /**
     * 处理用户的操作
     * @param flag
     * @return
     */
    public static Result<String> handle(Boolean flag) {
        if (flag) {
            return Result.success(null);
        }
        return Result.fail(BusinessEnum.OPERATION_FAIL);
    }
}

```

## auth-server模块

这是一个认证,模块,网关负责过滤器的功能, 而auth-server则是身份识别,列如我们要指定请求头携带的内容,auth会自动生成登入页面

![image-20240927154402317](https://p.ipic.vip/d6rj2n.png)

这是Spring Security的验证功能, 验证功能似乎是由框架完成的,不太理解呢

如果使用 Spring Security，它通常会自动处理身份验证和密码检查。以下是其基本工作流程：

​	•	**用户输入**：用户在登录表单中输入用户名和密码。

​	•	**AuthenticationManager**：Spring Security 会使用 AuthenticationManager 来处理认证请求。

​	•	**UserDetailsService**：UserDetailsService 实现会通过用户名查询数据库中的用户信息。

​	•	**PasswordEncoder**：使用 PasswordEncoder 接口的实现（如 BCryptPasswordEncoder）来对用户输入的密码进行哈希，并与存储在数据库中的密码进行比较。

![image-20240927155245297](https://p.ipic.vip/xsy98x.png)

我们对这个认证做一个简单的分析,首先是进入WebSevurityConfigurerAdpater, 走configure方法,

![image-20240927162411132](https://p.ipic.vip/kiro6n.png)

定义处理验证的方法

![image-20240927162542552](https://p.ipic.vip/3wutwu.png)

配置单独的设置,成功处理器和失败处理器

```java
 @Override
    protected void configure(HttpSecurity http) throws Exception {
        System.out.println("进入configure方法");
        // 关闭跨站请求伪造
        http.cors().disable();
        // 关闭跨域请求
        http.csrf().disable();
        // 关闭session使用策略
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // 配置登录信息
        http.formLogin()
                .loginProcessingUrl(AuthConstants.LOGIN_URL)// 设置登录URL
                .successHandler(authenticationSuccessHandler())   // 设置登录成功处理器
                .failureHandler(authenticationFailureHandler());  // 调协登录失败处理器

        // 配置登出信息
        http.logout()
                .logoutUrl(AuthConstants.LOGOUT_URL)// 设置登出URL
                .logoutSuccessHandler(logoutSuccessHandler());// 设置登出成功处理器

        // 要求所有请求都需要进行身份的认证
        http.authorizeH
```

### 查看处理验证的方法,这里运用了策略模式,根据loginType选择不同的登入验证处理方法

```java
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private LoginStrategyFactory loginStrategyFactory;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 获取请求对象
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        // 从请求头中获取登录类型
        String loginType = request.getHeader(AuthConstants.LOGIN_TYPE);
        // 判断请求来自于哪个系统
        /*if (AuthConstants.SYS_USER_LOGIN.equals(loginType)) {
            // 商城后台管理系统流程
        } else {
            // 商城用户购物系统流程
        }*/
        if (!StringUtils.hasText(loginType)) {
            throw new InternalAuthenticationServiceException("非法登录，登录类型不匹配");
        }
        // 通过登录策略工厂获取具体的登录策略对象
        LoginStrategy instance = loginStrategyFactory.getInstance(loginType);
        return instance.realLogin(username);
    }

```

### 登入验证方法(目前也不知道是怎么完成的)

```java
@Service(AuthConstants.SYS_USER_LOGIN)
public class SysUserLoginStrategy implements LoginStrategy {

    @Autowired
    private LoginSysUserMapper loginSysUserMapper;

    @Override
    public UserDetails realLogin(String username) {
        System.out.println("进入realLogin");
        // 根据用户名称查询用户对象
        LoginSysUser loginSysUser = loginSysUserMapper.selectOne(new LambdaQueryWrapper<LoginSysUser>()
                .eq(LoginSysUser::getUsername, username)
        );
        /*LoginSysUser loginSysUser = loginSysUserMapper.selectOne(new QueryWrapper<LoginSysUser>()
                .eq("username", username)
        );*/
        if (ObjectUtil.isNotNull(loginSysUser)) {
            // 根据用户标识查询用户的权限集合
            Set<String> perms = loginSysUserMapper.selectPermsByUserId(loginSysUser.getUserId());
            // 创建安全用户对象SecurityUser
            SecurityUser securityUser = new SecurityUser();
            securityUser.setUserId(loginSysUser.getUserId());
            securityUser.setPassword(loginSysUser.getPassword());
            securityUser.setShopId(loginSysUser.getShopId());
            securityUser.setStatus(loginSysUser.getStatus());
            securityUser.setLoginType(AuthConstants.SYS_USER_LOGIN);
            // 判断用户权限是否有值
            if (CollectionUtil.isNotEmpty(perms) && perms.size() != 0) {
                securityUser.setPerms(perms);
            }
            return securityUser;
        }

        return null;
    }
}
```

## 公共核心业务

core里面的config,配置redis的缓存,过期时间,swagger配置信息,Mybatis的分页

![image-20240927170039468](/Users/leojackasher/Desktop/Markdown/动力商场项目.assets/image-20240927170039468.png)

### 续签

续签就是再次设置 expire

```java
  if (expire < AuthConstants.TOKEN_EXPIRE_THRESHOLD_TIME) {
                    // 给当前用户的token续签（本质就是增加token在redis中的存活时长）
                    stringRedisTemplate.expire(AuthConstants.LOGIN_TOKEN_PREFIX+token,AuthConstants.TOKEN_TIME, TimeUnit.SECONDS);
                }

```

## 前端项目

运行前端项目遇到 sass 的问题, node-sass 无法支持 arm, 必须转成 sass, 把缓存文件删了, 然后 package 文件改了就可以运行了



## 菜单树

又来到了菜单数生成环节我们来看看这个菜单树是怎么生成的, 首先进入 Controller 层

```java
@ApiOperation("查询用户的菜单权限和操作权限")
    @GetMapping("nav")
    public Result<MenuAndAuth> loadUserMenuAndAuth() {
        // 获取当前登录用户的标识
//        SecurityUser securityUser = (SecurityUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        Long userId = securityUser.getUserId();
        Long loginUserId = AuthUtils.getLoginUserId();

        // 根据用户标识查询操作权限集合
        Set<String> perms = AuthUtils.getLoginUserPerms();
        // 根据用户标识查询菜单权限集合
        Set<SysMenu> menus = sysMenuService.queryUserMenuListByUserId(loginUserId);

        // 创建菜单和操作权限对象
        MenuAndAuth menuAndAuth = new MenuAndAuth(menus,perms);
        return Result.success(menuAndAuth);
    }
```

这个 SpringSecrity 似乎会形成一个作用域来管理用户对象, 存储数据, 就像 Spring 存储类一样, 可以获取这个用户, 操作权限集合就是查询权限表,这个是用户类自带的获取权限方法

```java
Set<String> perms = AuthUtils.getLoginUserPerms();
```

```java
  public Set<String> getPerms() {
        HashSet<String> finalPermsSet = new HashSet<>();
        // 循环遍历用户权限集合
        perms.forEach(perm -> {
            // 判断是否包含,
            if (perm.contains(",")) {
                // 包含：说明一条权限里有多个权限
                // 根据,号进行分隔处理
                String[] realPerms = perm.split(",");
                // 循环遍历
                for (String realPerm : realPerms) {
                    finalPermsSet.add(realPerm);
                }
            } else {
                // 不包含，即一条权限
                finalPermsSet.add(perm);
            }
        });
        return finalPermsSet;
    }
```

然后根据 ID 查权限

```java
     // 根据用户标识查询菜单权限集合
        Set<SysMenu> menus = sysMenuService.queryUserMenuListByUserId(loginUserId);
```

```java
  @Override
    @Cacheable(key = "#loginUserId")
    public Set<SysMenu> queryUserMenuListByUserId(Long loginUserId) {
        // 根据用户标识查询菜单权限集合
        Set<SysMenu> menus = sysMenuMapper.selectUserMenuListByUserId(loginUserId);
        // 将菜单权限集合的数据转换为树结构（即：数据结构应该为层级关系的）
        return transformTree(menus,0L);
    }

```

查出来的权限要转换为权限树,第一遍遍历, 获取根节点, 然后递归把根节点 id 作为父节点重复调用

```java
private Set<SysMenu> transformTree(Set<SysMenu> menus, Long pid) {
        // 已知菜单深度<=2
        // 从菜单集合中获取根节点集合
        /*Set<SysMenu> roots = menus.stream()
                .filter(m -> m.getParentId().equals(pid))
                .collect(Collectors.toSet());
        // 循环遍历根节点集合
        roots.forEach(root -> {
            // 从菜单集合中过滤出它的父节点值与当前根节点的id值一致的菜单集合
            Set<SysMenu> child = menus.stream()
                    .filter(m -> m.getParentId().equals(root.getMenuId()))
                    .collect(Collectors.toSet());
            root.setList(child);
        });*/

        // 未知菜单深度
        // 获取根节点集合
        Set<SysMenu> roots = menus.stream()
                .filter(m -> m.getParentId().equals(pid))
                .collect(Collectors.toSet());
        // 循环节点集合
        roots.forEach(r -> r.setList(transformTree(menus,r.getMenuId())));
        return roots;
    }
```

原来注释会被同步在这里

![image-20241005212942531](https://p.ipic.vip/h9pqfl.png)

### sql 语句

一直困扰我的是这个 Type 是啥, `t1.type = 0 OR t1.type = 1`这个表示菜单项的权限

```sql
 SELECT
      t1.*
    FROM
      sys_menu t1
        JOIN sys_role_menu t2
        JOIN sys_user_role t3 ON ( t1.menu_id = t2.menu_id AND t2.role_id = t3.role_id )
    WHERE
      t3.user_id = 1
      AND ( t1.type = 0 OR t1.type = 1 );
```

不加 Type 会把表单项也添加进去

![image-20241005215423884](https://p.ipic.vip/td51mv.png)

## ApiFox使用

哎,真是能学到很多东西, 只是每一次都搞得我很暴躁, 微服务的的服务名可以代替端口号

![image-20241005234835815](https://p.ipic.vip/ev92l7.png)

## Mybatis plus

mybatis 的分页插件使用

```java
/**
 * 多条件分页查询系统管理员
 * @param current   页码
 * @param size      每页显示条件
 * @param username  管理员名称
 * @return
 */
@ApiOperation("多条件分页查询系统管理员")
@GetMapping("page")
@PreAuthorize("hasAuthority('sys:user:page')")
public Result<Page<SysUser>> loadSysUserPage(@RequestParam Long current,
                                             @RequestParam Long size,
                                             @RequestParam(required = false) String username) {
    // 创建Mybatisplus的分页对象
    Page<SysUser> page = new Page<>(current,size);
    // 多条件分页查询系统管理员
    page = sysUserService.page(page,new LambdaQueryWrapper<SysUser>()
            /*<if Test ="username != null and username != ''">
                username like %xx%
            </if>*/
            .like(StringUtils.hasText(username),SysUser::getUsername,username)
            .orderByDesc(SysUser::getCreateTime)
    );

    return Result.success(page);
}
```

## Apifox

Apifox 可以自动生成 JSON 数据, 还是挺不错的, 而且前面搞了很久的配置的前置接口居然只是因为没有保存,真的是很无语了

![image-20241008211508734](https://p.ipic.vip/iy3n2e.png)

## 缓存

如果你的缓存数据已经成功存储在 Redis 中，而你并没有明确配置缓存提供者，那么可能是 Spring Boot 的自动配置机制起了作用。

###  **Spring Boot 的自动配置**

Spring Boot 对常见的缓存提供者（如 Redis）提供了自动配置支持。只要你在项目中引入了 Redis 相关的依赖，Spring Boot 会自动配置 Redis 作为默认的缓存提供者，而不需要你手动配置过多细节。

通常情况下，Spring Boot 会根据类路径中的依赖，自动推断出使用哪种缓存提供者。例如：

- 如果项目中有 Redis 相关的依赖，Spring Boot 会默认使用 Redis 作为缓存存储。
- 如果没有指定其他缓存类型，Spring Boot 还可能使用默认的基于内存的 `ConcurrentMapCacheManager` 作为缓存管理器。

## 表查询

当时学 Mybatis 的时候, 有个多表查询的知识点, 当时就感觉复杂,麻烦, 其实实战根本不可能用外键, 而且是通过查出一个表的对象, 取其 关联id 来查询另一个表来实现多表查询的

## 商品修改

这个商品修改,只能修改属性,不能添加,因为 `boolean flag=prodPropValueService.updateBatchById(prodPropValues);`只能修改已有的 id,而属性表数量固定了,应该把以前的属性删掉,重新添加来覆盖

```java
 @Override
    @CacheEvict(key = ProductConstants.PROD_PROP_KEY)
    @Transactional(rollbackFor = Exception.class)
    public Boolean modifyProdSpec(ProdProp prodProp) {
        // 获取新的属性值对象集合
        List<ProdPropValue> prodPropValues = prodProp.getProdPropValues();
        // 批量修改属性值对象
        boolean flag = prodPropValueService.updateBatchById(prodPropValues);
        if (flag) {
            // 修改属性对象
            prodPropMapper.updateById(prodProp);
        }
        return flag;
    }
```

**只能根据 id修改**

![image-20241009010558938](https://p.ipic.vip/dgh8m2.png)

### **改进**

很开心,我把它改进了一下,可以在修改时添加属性

```java
  @Override
    @CacheEvict(key = ProductConstants.PROD_PROP_KEY)
    @Transactional(rollbackFor = Exception.class)
    public Boolean modifyProdSpec(ProdProp prodProp) {
        //删除原有的 prop 属性
        Boolean flag = removeProdSpecByPropId(prodProp.getPropId());
        if (flag) {
            // 增加 prop
            saveProdSpec(prodProp);
        }
        return flag;
    }

```

## 经典新增代码

首先这个传入的 prop 属性,很多是类没有的,需要额外添加, 然后数据库字段更多,例如时间 ShopId, 本质是传入的值和数据库字段数量不匹配需要再次处理, 处理完后再进行添加操作` prodTagReferenceService.saveBatch(prodTagReferenceList);`而且是一对多的关系, 对多个对象的处理也是为什么要进行这些操作的原因,如果属性值对应,或者时间记录和自增交给数据库来做的话, 直接就可以把 prod 存入数据库, 取出属性再存

```java
@Override
@Transactional(rollbackFor = Exception.class)
public Boolean saveProd(Prod prod) {
    // 新增商品
    prod.setShopId(1L);
    prod.setSoldNum(0);
    prod.setCreateTime(new Date());
    prod.setUpdateTime(new Date());
    prod.setPutawayTime(new Date());
    prod.setVersion(0);
    Prod.DeliveryModeVo deliveryModeVo = prod.getDeliveryModeVo();
    prod.setDeliveryMode(JSONObject.toJSONString(deliveryModeVo));
    int i = prodMapper.insert(prod);
    if (i > 0) {
        Long prodId = prod.getProdId();
        // 处理商品与分组标签的关系
        // 获取商品分组标签
        List<Long> tagIdList = prod.getTagList();
        // 判断是否有值
        if (CollectionUtil.isNotEmpty(tagIdList) && tagIdList.size() != 0) {
            // 创建商品与分组标签关系集合
            List<ProdTagReference> prodTagReferenceList = new ArrayList<>();
            // 循环遍历分组标签id集合
            tagIdList.forEach(tagId -> {
                // 创建商品与分组标签的关系记录
                ProdTagReference prodTagReference = new ProdTagReference();
                prodTagReference.setProdId(prodId);
                prodTagReference.setTagId(tagId);
                prodTagReference.setCreateTime(new Date());
                prodTagReference.setShopId(1L);
                prodTagReference.setStatus(1);
                prodTagReferenceList.add(prodTagReference);
            });
            // 批量添加商品与分组标签的关系记录
            prodTagReferenceService.saveBatch(prodTagReferenceList);
        }

        // 处理商品与商品sku的关系
        // 获取商品sku对象集合
        List<Sku> skuList = prod.getSkuList();
        // 判断是否有值
        if (CollectionUtil.isNotEmpty(skuList) && skuList.size() != 0) {
            // 循环遍历商品sku对象集合
            skuList.forEach(sku -> {
                sku.setProdId(prodId);
                sku.setCreateTime(new Date());
                sku.setUpdateTime(new Date());
                sku.setVersion(0);
                sku.setActualStocks(sku.getStocks());
            });
            // 批量添加商品sku对象集合
            skuService.saveBatch(skuList);
        }
    }
    return i>0;
}
```

传入的参数多了,会自动忽略,少了,接受对象属性设为默认值

## ObjectUtil

就是`!= null`只是为了可读性????

## StringUtil

`StringUtils.hasText` 是 Spring 框架中的一个工具方法，用于判断一个字符串是否 **既不为 `null`，也不是空白字符串**（包括空字符串或只包含空白字符的字符串）

## ConnectionUtil

### 作用

**检查集合是否为空**：`CollectionUtil.isNotEmpty(prods)` 方法返回 `true` 表示 `prods` 集合中有元素，返回 `false` 表示集合为空。

**使用场景**

在你的代码中，这个检查用于确保在尝试访问 `prods` 集合中的元素之前，该集合确实包含至少一个元素。这样可以避免在访问集合的第一个元素时出现 `IndexOutOfBoundsException`（索引越界异常），因为如果集合为空，尝试访问 `prods.get(0)` 会导致程序崩溃。



## Openfeign Sentinel

### Openfeign 设置

这个看上去非常像 Controller,不过是用于发送请求的

```java
@FeignClient(value = "product-service",fallback = StoreProdFeignSentinel.class)
public interface StoreProdFeign {

    @GetMapping("prod/prod/getProdListByIds")
    public Result<List<Prod>> getProdListByIds(@RequestParam List<Long> prodIdList);

}
```

`@FeignClient(value = "product-service", fallback = StoreProdFeignSentinel.class)` 是一个 Spring Cloud Feign 客户端的注解，主要用于简化服务之间的 HTTP 请求调用。它将远程服务的调用抽象为接口方法调用，而不需要手动处理 HTTP 请求。具体来说，这个注解的作用包括以下几点：

### 1. **value 属性（服务名）**

- `value = "product-service"`：指定要调用的服务的名称（即被调用服务在注册中心中的名称）。在 Spring Cloud 微服务架构中，服务通过注册中心（如 Eureka 或者 Consul）进行注册，`product-service` 是一个已经注册在服务注册中心中的服务名称。
- Feign 客户端会根据这个服务名称找到对应的微服务，并生成访问该服务的 HTTP 请求。

### 2. **fallback 属性（降级处理）**

- `fallback = StoreProdFeignSentinel.class`：指定了当调用远程服务失败时的**降级处理类**，也就是服务降级的实现类。
- 当 `product-service` 这个服务无法正常响应，比如服务不可用、超时、或者网络异常时，Spring Cloud 会调用 `StoreProdFeignSentinel` 中实现的降级逻辑，避免整个系统因为一个微服务的故障而崩溃。

### 3. **Feign 客户端的功能**

- Feign 是一种声明式的 HTTP 客户端，简化了微服务之间的 HTTP 通信。开发者只需要定义一个接口，接口方法与远程服务的 API 对应，Feign 会自动将接口的方法映射成 HTTP 请求。
- 在这个例子中，`StoreProdFeign` 是一个 Feign 客户端，用于调用 `product-service` 中的 `getProdListByIds` API。

### 4. **结合 Hystrix 或 Sentinel 进行服务降级**

- `fallback` 属性中的 `StoreProdFeignSentinel` 类用于处理当远程调用失败时的**容错逻辑**。这通常是在使用 Hystrix 或 Sentinel 进行服务保护时添加的功能，以保证当某个服务宕机时系统依然能够正常运转。
- `StoreProdFeignSentinel` 是一个实现了 `StoreProdFeign` 接口的类，提供了当调用 `getProdListByIds` 方法失败时的默认处理逻辑。

## 修改

ApiOperation 是标记目录文件名称,`boolean removed = memberService.updateBatchById(memberList);`将数据库修改为传入的对象集合属性

```java
@ApiOperation("批量删除会员")
@DeleteMapping
@PreAuthorize("hasAuthority('admin:user:delete')")
public Result<String> removeMembers(@RequestBody List<Integer> ids) {
    // 创建会员对象集合
    List<Member> memberList = new ArrayList<>();
    // 循环遍历会员id集合
    ids.forEach(id -> {
        Member member = new Member();
        member.setId(id);
        member.setStatus(-1);
        memberList.add(member);
    });
    boolean removed = memberService.updateBatchById(memberList);
    return Result.handle(removed);
}
```

## 订单查询

这里也用到了 Openfeign, 根据订单编号, 可以查到 order 和 orderItem, 然后从 order 里面的 addrId获取地址, 类的 order 拥有更多的属性值

```java
@Override
public Order queryOrderDetailByOrderNumber(Long orderNumber) {
    // 根据订单编号查询订单信息
    Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
            .eq(Order::getOrderNumber, orderNumber)
    );
    // 根据订单编号查询订单商品条目对象集合
    List<OrderItem> orderItemList = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
            .eq(OrderItem::getOrderNumber, orderNumber)
    );
    order.setOrderItems(orderItemList);
    // 从订单记录中获取订单收货地址标识
    Long addrOrderId = order.getAddrOrderId();
    // 远程调用：根据收货地址标识查询地址详情
    Result<MemberAddr> result = orderMemberFeign.getMemberAddrById(addrOrderId);
    // 判断结果
    if (result.getCode().equals(BusinessEnum.OPERATION_FAIL.getCode())) {
        throw new BusinessException("远程接口调用失败：根据收货地址标识查询收货地址信息");
    }
    // 获取数据
    MemberAddr memberAddr = result.getData();
    order.setUserAddrOrder(memberAddr);

    // 远程接口调用：根据会员openid查询会员昵称
    Result<String> result1 = orderMemberFeign.getNickNameByOpenId(order.getOpenId());
    if (result1.getCode().equals(BusinessEnum.OPERATION_FAIL.getCode())) {
        throw new BusinessException("远程接口调用失败：根据会员openId查询会员昵称");
    }
    // 获取数据
    String nickName = result1.getData();
    order.setNickName(nickName);

    return order;
}
```

### Feign 的关键功能：

1. **声明式 HTTP 客户端：** 通过注解的方式声明要发送的 HTTP 请求，而不是手动编写 HTTP 请求。你只需要定义一个接口，并在接口的方法上使用 HTTP 请求注解，如 `@GetMapping`、`@PostMapping` 等。
2. **服务发现集成：** 通过 `@FeignClient` 注解，可以将这个接口与服务名关联。Feign 会通过注册中心（如 Eureka）根据服务名称找到实际的服务地址，而不用你手动指定。
3. **熔断机制：** 在你的例子中，`fallback = OrderMemberFeignSentinel.class` 表示当调用 `member-service` 失败时，会调用 `OrderMemberFeignSentinel` 类中的方法，来防止整个服务因下游服务不可用而崩溃。这种机制称为**熔断**，它是微服务架构中的一个关键设计，能够提高系统的健壮性。

## EasyExcel

首先导入依赖

```xml
<!--easyexcel依赖-->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>easyexcel</artifactId>
</dependency>
```

这调用也够简单的, 连配置文件都不用, 指定文件名和类就可以

```java
 @ApiOperation("导出销售记录")
    @GetMapping("soldExcel")
    @PreAuthorize("hasAuthority('order:order:soldExcel')")
    public Result<String> exportSoleOrderRecordExcel() {
        // 查询所有销售记录
        List<Order> list = orderService.list(new LambdaQueryWrapper<Order>()
                .orderByDesc(Order::getCreateTime)
        );

        String fileName = "/Users/leojackasher/tmp/" + System.currentTimeMillis() + ".xlsx";
        // 这里 需要指定写用哪个class去写，然后写到第一个sheet，名字为模板 然后文件流会自动关闭
        // 如果这里想使用03 则 传入excelType参数即可
        EasyExcel.write(fileName, Order.class).sheet("模板111").doWrite(list);
        return Result.success(null);
    }

}
```

类大概长这样`@ExcelProperty("订单ID")`就是行属性

```java
@ApiModel(value="com-powernode-domain-Order")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "`order`")
public class Order implements Serializable {
    /**
     * 订单ID
     */
    @ExcelProperty("订单ID")
    @TableId(value = "order_id", type = IdType.AUTO)
    @ApiModelProperty(value="订单ID")
    private Long orderId;

    /**
     * 订购用户ID
     */
    @TableField(value = "open_id")
    @ApiModelProperty(value="订购用户ID")
    @ExcelProperty("订购用户ID")
    private String openId;
```

成功在指定位置导出

![image-20241011032051283](https://p.ipic.vip/s5tfea.png)

## 微信小程序

### 登入

微信小程序登录的流程如下：

1. **调用 `wx.login()`**：开发者在小程序端调用 `wx.login()` 方法以获取临时登录凭证（`code`）。该凭证是一次性使用的，主要用来验证用户身份。
2. **发送请求到开发者服务器**：小程序将获取到的 `code` 发送到开发者的服务器。
3. **服务器请求微信接口**：开发者的服务器使用这个 `code` 调用微信的 登录凭证校验接口，传递 `appid`、`secret`和 `code`，以获取用户的 `openid` 和 `session_key`。
4. **返回结果**：微信会返回用户的 `openid`（用户唯一标识）以及 `session_key`（用于数据加密的密钥）。开发者可以根据这些信息处理用户登录状态。

因此，微信小程序在登录时**不会自动**向服务器发送请求，登录过程需要开发者编写代码来实现登录逻辑，调用相关的微信接口来完成。

## 阿里云短信验证

申请资质,签名,模版,然后 Maven 引入后, 使用模版代码,就可以调用,将敏感信息保存到 nacos 就好,这就是为什么 nacos 只能局域网访问

```java
@Autowired
private StringRedisTemplate stringRedisTemplate;
@Override
public void sendPhoneMsg(Map<String, Object> map) {
    // 准备配置对象
    com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
            // 必填，请确保代码运行环境设置了环境变量 ALIBABA_CLOUD_ACCESS_KEY_ID。
            .setAccessKeyId(aliyunDxConfig.getAccessKeyID())
            // 必填，请确保代码运行环境设置了环境变量 ALIBABA_CLOUD_ACCESS_KEY_SECRET。
            .setAccessKeySecret(aliyunDxConfig.getAccessKeySecret());
    // Endpoint 请参考 https://api.aliyun.com/product/Dysmsapi
    config.endpoint = aliyunDxConfig.getEndpoint();
    try {
        // 创建客户端对象
        com.aliyun.dysmsapi20170525.Client client = new com.aliyun.dysmsapi20170525.Client(config);
        // 获取手机号码
        String phonenum = (String) map.get("phonenum");
        // 生成一个随机数字
        String randomNumber = RandomUtil.randomNumbers(4);
        // 将生成的随机数字存放到redis中
        stringRedisTemplate.opsForValue().set(MemberConstants.MSG_PHONE_PREFIX+phonenum, randomNumber, Duration.ofMinutes(30));
        // 创建模版参数
        String templateParam = "{\"code\":\""+randomNumber+"\"}";
        // 创建请求参数对象
        com.aliyun.dysmsapi20170525.models.SendSmsRequest sendSmsRequest = new com.aliyun.dysmsapi20170525.models.SendSmsRequest()
                .setPhoneNumbers(phonenum)
                .setSignName(aliyunDxConfig.getSignName())
                .setTemplateCode(aliyunDxConfig.getTemplateCode())
                .setTemplateParam(templateParam);
        // 发送请求
        client.sendSmsWithOptions(sendSmsRequest, new com.aliyun.teautil.models.RuntimeOptions());
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}
```

## Lombok Builder 注解

`@Builder` 是 Lombok 库中的一个注解，用于简化对象创建过程。使用 `@Builder` 注解后，你可以通过流式 API 的方式构建对象，避免传统 Java 中使用构造函数或 setter 方法的繁琐写法。

具体来说，`@Builder` 允许你通过链式调用的方式来创建对象。例如，使用 `OrderStatusCount` 类的 `@Builder` 方式时，你可以这样创建一个对象：

```java
OrderStatusCount statusCount = OrderStatusCount.builder()
    .unPay(5L)
    .payed(10L)
    .consignment(3L)
    .build();
```

## 修改默认地址

Mybatis 有个好处就是, 更改是,只更改赋值了的属性

```java
 @Override
    @CacheEvict(key = "#openId")
    @Transactional(rollbackFor = Exception.class)
    public Boolean modifyMemberDefaultAddr(String openId, Long newAddrId) {
        // 根据收货地址标识查询收货地址对象
        MemberAddr newDefaultMemberAddr = memberAddrMapper.selectById(newAddrId);
        // 判断新的默认收货地址是否为原有的默认收货地址
        if (newDefaultMemberAddr.getCommonAddr().equals(1)) {
            // 是：结束
            return true;
        }
        // 不是：将之前的默认收货地址修改为非默认，并更新当前新的默认收货地址
        // 将会员原有的默认收货地址设置为非默认
        MemberAddr oldDefaultMemberAddr = new MemberAddr();
        oldDefaultMemberAddr.setCommonAddr(0);
        oldDefaultMemberAddr.setUpdateTime(new Date());
        memberAddrMapper.update(oldDefaultMemberAddr, new LambdaUpdateWrapper<MemberAddr>()
                .eq(MemberAddr::getOpenId,openId)
        );

        // 将当前收货地址设置的新的默认收货地址
        newDefaultMemberAddr.setCommonAddr(1);
        newDefaultMemberAddr.setUpdateTime(new Date());

        return memberAddrMapper.updateById(newDefaultMemberAddr)>0;
    }
}
```

## Mybatis Plus

MyBatis Plus 是 MyBatis 的增强工具，它在 MyBatis 的基础上增加了许多常用的 CRUD 操作方法，简化了开发流程。以下是 MyBatis Plus 中一些常用的方法：

### 1. **CRUD 基础方法**

这些方法通常是由 `BaseMapper` 接口提供的，无需手动编写 SQL。

- `insert(T entity)`：插入一条数据，返回影响的行数。
- `deleteById(Serializable id)`：根据 ID 删除一条记录。
- `deleteBatchIds(Collection<? extends Serializable> idList)`：批量删除记录。
- `updateById(T entity)`：根据 ID 更新记录。
- `selectById(Serializable id)`：根据 ID 查询一条记录。
- `selectBatchIds(Collection<? extends Serializable> idList)`：根据 ID 列表批量查询。
- `selectList(Wrapper<T> queryWrapper)`：查询满足条件的记录列表。
- `selectPage(Page<T> page, Wrapper<T> queryWrapper)`：分页查询。

### 2. **条件构造器方法**

使用 `QueryWrapper` 或 `LambdaQueryWrapper` 进行条件查询：

- `eq(String column, Object val)`：等值查询。
- `ne(String column, Object val)`：不等值查询。
- `gt(String column, Object val)`：大于查询。
- `lt(String column, Object val)`：小于查询。
- `between(String column, Object val1, Object val2)`：区间查询。
- `like(String column, Object val)`：模糊查询。
- `orderByAsc(String... columns)`：升序排序。
- `orderByDesc(String... columns)`：降序排序。

### 3. **分页查询**

使用 `Page` 类进行分页操作：

```java
Page<T> page = new Page<>(current, size);
IPage<T> result = baseMapper.selectPage(page, queryWrapper);
```

- `current`：当前页。
- `size`：每页显示条数。
- `total`：总记录数。

### 4. **批量操作**

MyBatis Plus 支持批量插入、批量更新等操作：

- `updateBatchById(Collection<T> entityList)`：批量更新数据。
- `saveBatch(Collection<T> entityList)`：批量插入数据。

### 5. **逻辑删除**

MyBatis Plus 支持逻辑删除，通过注解 `@TableLogic` 进行配置。删除时并不物理删除数据，而是通过标记的方式。

```java
@TableLogic
private Integer deleted;
```

这些方法极大简化了日常的增删改查操作，减少了重复 SQL 的编写。

## 评论处理

先根据商品 id 获取评论, 根据评论 id 获取所有用户 id,然后对名字脱敏后展示

```java
@Override
public Page<ProdComm> queryWxProdCommPageByProd(Long current, Long size, Long prodId, Long evaluate) {
    // 创建评论分页对象
    Page<ProdComm> page = new Page<>(current,size);
    // 根据商品id分页查询单个商品的评论
    page = prodCommMapper.selectPage(page,new LambdaQueryWrapper<ProdComm>()
            .eq(ProdComm::getProdId,prodId)
            .eq(ProdComm::getStatus,1)
            .eq(0==evaluate||1==evaluate||2==evaluate,ProdComm::getEvaluate,evaluate)
            .isNotNull(3==evaluate,ProdComm::getPics)
            .orderByDesc(ProdComm::getCreateTime)
    );
    // 从分页对象中获取评论记录
    List<ProdComm> prodCommList = page.getRecords();
    // 判断是否有值
    if (CollectionUtils.isEmpty(prodCommList) || prodCommList.size() == 0) {
        return page;
    }
    // 从商品评论集合中获取会员openId集合
    List<String> openIdList = prodCommList.stream().map(ProdComm::getOpenId).collect(Collectors.toList());
    // 远程调用：根据会员openId集合查询会员对象集合
    Result<List<Member>> result = prodMemberFeign.getMemberListByOpenIds(openIdList);
    // 判断操作结果
    if (result.getCode().equals(BusinessEnum.OPERATION_FAIL.getCode())) {
        throw new BusinessException("远程调用：根据会员openId集合查询会员对象集合失败");
    }
    // 获取数据
    List<Member> memberList = result.getData();
    // 循环遍历评论集合
    prodCommList.forEach(prodComm -> {
        // 从会员对象集合中过滤出与当前会员对象的openId一致的会员对象
        Member member = memberList.stream()
                .filter(m -> m.getOpenId().equals(prodComm.getOpenId()))
                .collect(Collectors.toList()).get(0);
        // 将会员昵称进行脱敏操作
        StringBuilder stringBuilder = new StringBuilder(member.getNickName());
        StringBuilder replaceNickName = stringBuilder.replace(1, stringBuilder.length() - 1, "***");
        prodComm.setNickName(replaceNickName.toString());
        prodComm.setPic(member.getPic());
    });
    return page;
}
```

## 购物车类设计

如果是我设计的话, 我大概是把需要的商品和 sku 数量等信息查询到后直接封装到一个对象然后返回, 但是这里是用 CartVo 包裹了 ShopCart, 因为要区分店铺, 如果店铺已经存在, 直接添加到店铺里面, 没有的话就在创建一个.最后返回的 CartVo,你看这个设计就真的很让人看不懂, 明明展示的数据是CartItem,但是返回的是 CartVo,

![image-20241012172440033](https://p.ipic.vip/4gs77j.png)

### 总金额计算

传入的是 basketId,那么就可以拿到 SKU,根据 sku 计算价格即可

```java
@Override
public CartTotalAmount calculateMemberCheckedBasketTotalAmount(List<Long> basketIds) {
    // 创建购物车总金额对象
    CartTotalAmount cartTotalAmount = new CartTotalAmount();
    // 判断会员是否有选中的购物车记录
    if (CollectionUtils.isEmpty(basketIds) || basketIds.size() == 0) {
        // 购物车id集合为空 -> 说明会员没有选中购物车记录 -> 购物车商品总金额为0
        return cartTotalAmount;
    }
    // 购物车id集合不为空 -> 说明会员有选中的购物车记录 -> 计算金额
    // 根据购物车id集合查询购物车对象集合
    List<Basket> basketList = basketMapper.selectBatchIds(basketIds);
    // 从购物车对象集合中获取商品skuId集合
    List<Long> skuIdList = basketList.stream().map(Basket::getSkuId).collect(Collectors.toList());
    // 远程调用：根据商品skuId集合查询商品sku对象集合
    Result<List<Sku>> result = basketProdFeign.getSkuListBySkuIds(skuIdList);
    // 判断查询结果
    if (result.getCode().equals(BusinessEnum.OPERATION_FAIL.getCode())) {
        throw new BusinessException("远程调用：根据商品skuId集合查询商品sku对象集合失败");
    }
    // 获取返回数据
    List<Sku> skuList = result.getData();
    // 创建所有单个商品总金额的集合
    List<BigDecimal> oneSkuTotalAmounts = new ArrayList<>();
    // 循环遍历购物车对象集合
    basketList.forEach(basket -> {
        // 获取购物车记录的商品skuId
        Long skuId = basket.getSkuId();
        // 获取购物车记录中商品购买的数量
        Integer prodCount = basket.getProdCount();
        // 从商品sku对象集合中获取与当前购物车记录的skuId一致的购物车记录对象
        Sku sku1 = skuList.stream()
                .filter(sku -> sku.getSkuId().equals(skuId))
                .collect(Collectors.toList()).get(0);
        // 获取商品单价
        BigDecimal price = sku1.getPrice();
        // 计算单个商品总金额
        BigDecimal oneSkuTotalAmount = price.multiply(new BigDecimal(prodCount));
        // 添加到单个商品总金额集合中
        oneSkuTotalAmounts.add(oneSkuTotalAmount);
    });
    // 计算所有单个商品总金额的和
    BigDecimal allSkuTotalAmount = oneSkuTotalAmounts.stream().reduce(BigDecimal::add).get();

    // 填充数据
    cartTotalAmount.setTotalMoney(allSkuTotalAmount);
    cartTotalAmount.setFinalMoney(allSkuTotalAmount);
    // 运费：商品总金额超过99元，免运费，如果小于99元，运费6元
    if (allSkuTotalAmount.compareTo(new BigDecimal(99)) == -1) {
        cartTotalAmount.setTransMoney(new BigDecimal(6));
        cartTotalAmount.setFinalMoney(allSkuTotalAmount.add(new BigDecimal(6)));
    }
    return cartTotalAmount;
}
```

### 增添购物车

openid 查询当前用户的购物车, 看看有没有这个 basket,如果有就直接加数量, 没有就插入,以前使用 session 存储用户信息的, 现在是 SpringSecruity

```java
@Override
public Boolean changeCartItem(Basket basket) {
    // 获取会员openid
    String openId = AuthUtils.getMemberOpenId();
    // 根据会员openId和商品skuId查询购物车记录
    Basket beforeBasket = basketMapper.selectOne(new LambdaQueryWrapper<Basket>()
            .eq(Basket::getOpenId, openId)
            .eq(Basket::getSkuId, basket.getSkuId())
    );
    // 判断购物车记录是否有值
    if (ObjectUtil.isNotNull(beforeBasket)) {
        // 购物车记录不为空 -> 当前会员添加到购物车中的商品是存在的 -> 修改存在购物车中商品的数量
        // 计算商品最终数量
        int finalCount = beforeBasket.getProdCount() + basket.getProdCount();
        beforeBasket.setProdCount(finalCount);
        return basketMapper.updateById(beforeBasket)>0;
    }

    // 购物车记录为空 -> 当前会员添加到购物车中的商品是不存在的 -> 添加商品到购物车记录
    basket.setCreateTime(new Date());
    basket.setOpenId(openId);
    return basketMapper.insert(basket)>0;
}
```

## 提交订单

我好像找到了点写代码的感觉了, 后端的本质就是数据的处理,把合适的数据格式传给前端, 我自己的思路的话就是,前端应该会传入商品的 id,然后根据 id 查询地址,basket, 总金额,然后封装为一个对象返回, 左边情况采用创建 basket, 右边应该可以直接调用前面生成 CartVo 的方法

![image-20241012184630836](https://p.ipic.vip/np84le.png)

商铺提交的主方法

```java
@Override
@Transactional(rollbackFor = Exception.class)
public String submitOrder(OrderVo orderVo) {
    // 获取会员openId
    String openId = AuthUtils.getMemberOpenId();
    // 获取订单请求来源标识
    Integer source = orderVo.getSource();
    // 判断请求来源
    if (1 == source) {
        // 说明：提交订单的请求来源于购物车页面 -> 删除会员购买商品在购物车中的记录
        clearMemberCheckedBasket(openId, orderVo);
    }
    // 修改商品prod和sku库存数量
    ChangeStock changeStock = changeProdAndSkuStock(orderVo);
    // 生成一个全局唯一的订单编号（使用雪花算法）
    String orderNumber = generateOrderNumber();
    // 写订单（写订单表order和订单商品条目表order_item）
    writeOrder(openId,orderNumber,orderVo);
    // 解决超时未支付问题，写延迟队列
    sendMsMsg(orderNumber,changeStock);

    return orderNumber;
}
```

### 商品库存减少

核心方法是`orderProdFeign.changeProdAndSkuStock(changeStock);`而 changeStock 需要 prod和 sku, 做这么多是因为要区分商铺,同一商铺的商品要在一起展示, 同一商品不同的 sku 也要计算

```java
private ChangeStock changeProdAndSkuStock(OrderVo orderVo) {

    try{
        /**
         * 封装修改商品prod和sku库存数量对象
         */
        // 创建商品prod购买数量对象集合
        List<ProdChange> prodChangeList = new ArrayList<>();
        // 创建商品sku购买数量对象集合
        List<SkuChange> skuChangeList = new ArrayList<>();
        // 获取订单店铺对象集合
        List<ShopOrder> shopOrderList = orderVo.getShopCartOrders();

        System.out.println("shopOrderList:" + shopOrderList);

        // 空值检查
        if (orderVo == null || orderVo.getShopCartOrders() == null) {
            throw new IllegalArgumentException("订单信息不能为空");
        }

        // 循环遍历订单店铺对象集合
        shopOrderList.forEach(shopOrder -> {
            // 获取店铺的订单商品条目对象集合
            List<OrderItem> orderItemList = shopOrder.getShopOrderItems();
            // 循环订单商品条目对象集合
            orderItemList.forEach(orderItem -> {
                // 获取商品prodId
                Long prodId = orderItem.getProdId();
                // 获取商品skuId
                Long skuId = orderItem.getSkuId();
                // 获取商品购买数量
                Integer prodCount = orderItem.getProdCount();

                System.out.println("prodChangeList:" + prodChangeList);

                // 判断当前商品prodId是否在prodChangeList集合中出现过
                List<ProdChange> oneProdChange = prodChangeList.stream()
                        .filter(prodChange -> prodChange.getProdId().equals(prodId))
                        .collect(Collectors.toList());

                System.out.println("oneProdChange:" + oneProdChange);

                if (CollectionUtils.isEmpty(oneProdChange) || oneProdChange.size() == 0) {
                    // 说明：当前订单商品条目对象的商品prodId没有出现过
                    // 创建商品prod购买数量对象
                    ProdChange prodChange = new ProdChange(prodId, prodCount);
                    // 创建商品sku购买数量对象
                    SkuChange skuChange = new SkuChange(skuId, prodCount);

                    prodChangeList.add(prodChange);
                    skuChangeList.add(skuChange);
                } else {
                    // 说明：当前订单商品条目对象的商品prodId在之前出现过
                    // 获取之前商品prodChange
                    ProdChange beforeProdChange = oneProdChange.get(0);
                    // 计算商品prod一共购买的数量
                    int finalCount = beforeProdChange.getCount() + prodCount;
                    beforeProdChange.setCount(finalCount);
                    // 创建商品sku购买数量对象
                    SkuChange skuChange = new SkuChange(skuId,prodCount);
                    skuChangeList.add(skuChange);
                }
            });
        });

        // 创建商品购买数量对象
        ChangeStock changeStock = new ChangeStock(prodChangeList, skuChangeList);
        // 远程调用：修改商品prod和sku库存数量
        Result<Boolean> result = orderProdFeign.changeProdAndSkuStock(changeStock);
        // 判断操作结果
        if (result.getCode().equals(BusinessEnum.OPERATION_FAIL.getCode())) {
            throw new BusinessException("远程调用：修改商品prod和sku库存数量失败");
        }
        Boolean resultData = result.getData();
        if (!resultData) {
            throw new BusinessException("远程调用：修改商品prod和sku库存数量失败");
        }
        return changeStock;
    }catch (Exception e){
        throw new BusinessException("远程调用：修改商品prod和sku的库存数量 非 Openfeign失败");
    }

}
```

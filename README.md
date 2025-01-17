
<h1 align="center" style="margin: 30px 0 30px; font-weight: bold;">SWAK v2.3.x</h1>
<h4 align="center">基于SpringBoot多组件集成的Java快速开发框架</h4>
<p align="center">
	<a href="https://search.maven.org/search?q=g:io.gitee.mcolley%20a:swak*"><img src="https://img.shields.io/maven-central/v/io.gitee.mcolley/swak-core.svg"></a>
	<a href="https://github.com/colley/swak/blob/main/LICENSE"><img src="https://img.shields.io/github/license/mashape/apistatus.svg"></a>
</p>

## 简介

SWAK架构基于 Spring Boot2构建微服务架构，集成通用的基础技术模块和业务模块，快速搭建可直接部署。统一的接口出入参和公共规范，支持多环境多机房，支持多业务多场景扩展点，支持容错，限流，隔离，兜底，支持接口监控业务监控。集成通用工具类，支持白名单，穿越预演能力。

SWAK分为三个部分，SWAK架构、SWAK组件以及SWAK脚手架。

文档地址：https://colley.github.io

### Latest Version: [![Maven Central](https://img.shields.io/maven-central/v/io.gitee.mcolley/swak-core.svg)](https://search.maven.org/search?q=g:io.gitee.mcolley%20a:swak*)

``` xml
 <dependency>
    <groupId>io.gitee.mcolley</groupId>
    <artifactId>swak-bom</artifactId>
    <version>Latest Version</version>
    <type>pom</type>
    <scope>import</scope>
 </dependency>
 <dependency>
    <groupId>io.gitee.mcolley</groupId>
     <artifactId>swak-spring-boot-starter</artifactId>
     <version>Latest Version</version>
 </dependency>

```

### 依赖中间件版本

中间件名称 | 版本              |备注
------ |-----------------| ----
`spring boot`  | 2.7.18	 | spring boot


### SWAK脚手架
我们提供了两个`archetype`，[SWAK脚手架使用](https://colley.github.io/#/archetype) 点击了解详情。


### 通用ErrorCode
ErrorCode由通用Code和私有Code组成，支持国际化。

通用ErrorCode
通用ErrorCode由Foundation Core统一维护，各个应用直接引用下方的cn.getech.cim.foundation.core.bean.ResultCode枚举即可。
- 0-900为成功的通用ErrorCode
- 901-2000为失败的通用ErrorCode
- 
  ResultName | name | Code     | Message                             |备注
  ------ |-------| ----|-------------------------------------| ----
  BasicErrCode | SUCCESS | 0 | OK                                  |
  BasicErrCode | USER_NO_LOGIN | 401 | 您未登录，请先登录~                          |
  BasicErrCode | UNAUTHENTICATED | 401 | Unauthenticated                     |
  BasicErrCode | ACCESS_DENIED | 402 | 登录态失效，无法访问该资源                       |
  BasicErrCode | NOT_HAVE_PERMISSION | 403 | 无权限访问，请联系管理员申请对应资源权限！               |
  BasicErrCode | SWAK_LICENSE | 600 | 您的试用期授权已失效，请核查系统是否取得授权试用或申请正式授权书！   |
  BasicErrCode | SWAK_OPERA_REPEAT | 601 | 你点击的太快啦,稍后再试~                       |
  BasicErrCode | SWAK_REQ_LIMIT | 602 | 系统繁忙，已优先为您接入快速通道，稍等片刻~              |
  BasicErrCode | SWAK_ERROR | 901 | 系统异常，稍后再试~                          |
  BasicErrCode | BIZ_ERROR | 902 | 业务异常，请稍后再试~                         |
  BasicErrCode | INVALID_PARAMETER | 1000 | 您输入的参数异常~                           |
  BasicErrCode | PARAMETER_NOT_BLANK | 1001 | 参数不能为空                                |
  BasicErrCode | PARAMETER_NOT_EMPTY | 1002 | 参数不能为空                                |
  BasicErrCode | PARAMETER_NOT_NULL | 1003 | 参数不能为null                             |
  BasicErrCode | PARAMETER_IS_NULL | 1004 | 参数为null                               |
  BasicErrCode | SAVE_ERROR | 1005 | 保存失败，请稍后再试~                         |
  BasicErrCode | UPDATE_ERROR | 1006 | 更新失败，请稍后再试~                         |
  BasicErrCode | DELETE_ERROR | 1007 | 删除失败，请稍后再试~                         |

### SWAK Components
此外，我们还提供了一些非常有用的通用组件，这些组件可以帮助我们提升研发效率。

这些功能组件被收拢在[`Swak components`目录](https://colley.github.io/#/components/swak-extension) 下面。



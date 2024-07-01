
<h1 align="center" style="margin: 30px 0 30px; font-weight: bold;">Swak v3.4.0</h1>
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


### SWAK Components
此外，我们还提供了一些非常有用的通用组件，这些组件可以帮助我们提升研发效率。

这些功能组件被收拢在[`Swak components`目录](https://colley.github.io/#/components/swak-extension) 下面。
# ServiceInstaller

### 配置 serviceInstaller/config.properties
```
#要安装引用的 JAR 包文件。
jar=../test.jar


# 系统运行时的服务名称。
serviceName=TestService
# 服务的描述。
serviceDescription=This is a test Service.
# 服务启动类型。
# auto、manual
startup=auto

# 停止服务时报告的类名。
# 如 com.rt.Stop
stopClass=


# 初始化内存大小，单位 MB。
# 默认 32
xms=
# 最大内存大小，单位 MB
# 默认 2048
xmx=512


# 安装及服务支持时的目录。
# CURRENT、PARENT、其它完整路径
root=PARENT
```


### 安装
![安装](https://github.com/ttluowen/ServiceInstaller/blob/master/images/1.png "安装")

### 卸载
![卸载](https://github.com/ttluowen/ServiceInstaller/blob/master/images/2.png "卸载")
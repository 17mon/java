java
====

17mon IP库解析代码

##基本用法
```java

IP.enableFileWatch = true; // 默认值为：false，如果为true将会检查ip库文件的变化自动reload数据

IP.load("IP库本地绝对路径");

IP.find("8.8.8.8");//返回字符串数组["GOOGLE","GOOGLE"]

```

IPExt的用法与IP的用法相同，只是用来解析datx格式文件。

##特别说明
```java
IP.java 类仅适用于免费版dat与收费版每周每日版本的dat文件；
IPExt.java 类适用于收费版每日版本的datx文件；
```
区县库代码请查看 https://github.com/17mon/quxianku/

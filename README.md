# Atom SQL Spring

## 概要
Atom SQL Springは、[Atom SQL](https://github.com/ats-jp/atom-sql)を[Spring Framework](https://spring.io/)上で使用できるようにします。  

## 使い方
### インストール
Mavenを使用する場合  

pom.xmlに以下を追加  

repositoriesに  

```
<repository>
    <id>atom-sql</id>
    <url>https://ats-jp.github.io/atom-sql/</url>
</repository>
<repository>
    <id>atom-sql-spring</id>
    <url>https://ats-jp.github.io/atom-sql-spring/</url>
</repository>
```

を追加する

dependenciesに

```
<dependency>
    <groupId>jp.ats</groupId>
    <artifactId>atom-sql-spring</artifactId>
    <version>n.n.n</version>
</dependency>
```

を追加する  
versionのn.n.nには最新のバージョン番号を設定すること  

### AtomSqlInitializerの追加
例として、Spring Bootでの使用方法を例示する  

```java
@SpringBootApplication
public class Application {

    public static void run(Class<?> applicationClass, String[] args) {
        var spring = new SpringApplication(applicationClass);

        spring.addInitializers(new AtomSqlInitializer());
        spring.run(args);
    }
}
```

このようにすることで、通常のコンポーネントと同様、`@Autowired`によって`@SqlProxy`を付与したインターフェイスのインスタンスが取得可能となる  
また、Atom SQLの中心となるクラス`jp.ats.atomsql.AtomSql`のインスタンスも`@Autowired`で取得可能  

### SQLのデバッグ出力設定
SQLをログに出力する設定は、例として`application.properties`を使用している場合

```
atomsql.enable-log=true
atomsql.log-stacktrace-pattern=自アプリケーションのパッケージ名
```

とすることで設定可能

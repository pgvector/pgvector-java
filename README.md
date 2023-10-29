# pgvector-java

[pgvector](https://github.com/pgvector/pgvector) support for Java, Kotlin, Groovy, and Scala

Supports [JDBC](https://jdbc.postgresql.org/), [Spring JDBC](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/jdbc/core/JdbcTemplate.html), and [Slick](https://github.com/slick/slick)

[![Build Status](https://github.com/pgvector/pgvector-java/workflows/build/badge.svg?branch=master)](https://github.com/pgvector/pgvector-java/actions)

## Getting Started

For Maven, add to `pom.xml` under `<dependencies>`:

```xml
<dependency>
  <groupId>com.pgvector</groupId>
  <artifactId>pgvector</artifactId>
  <version>0.1.3</version>
</dependency>
```

For sbt, add to `build.sbt`:

```sbt
libraryDependencies += "com.pgvector" % "pgvector" % "0.1.3"
```

For other build tools, see [this page](https://central.sonatype.com/artifact/com.pgvector/pgvector).

And follow the instructions for your database library:

- Java - [JDBC](#jdbc-java), [Spring JDBC](#spring-jdbc)
- Kotlin - [JDBC](#jdbc-kotlin)
- Groovy - [Groovy SQL](#groovy-sql)
- Scala - [JDBC](#jdbc-scala), [Slick](#slick)

## JDBC (Java)

Import the `PGvector` class

```java
import com.pgvector.PGvector;
```

Enable the extension

```java
Statement setupStmt = conn.createStatement();
setupStmt.executeUpdate("CREATE EXTENSION IF NOT EXISTS vector");
```

Register the vector type with your connection

```java
PGvector.addVectorType(conn);
```

Create a table

```java
Statement createStmt = conn.createStatement();
createStmt.executeUpdate("CREATE TABLE items (id bigserial PRIMARY KEY, embedding vector(3))");
```

Insert a vector

```java
PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO items (embedding) VALUES (?)");
insertStmt.setObject(1, new PGvector(new float[] {1, 1, 1}));
insertStmt.executeUpdate();
```

Get the nearest neighbors

```java
PreparedStatement neighborStmt = conn.prepareStatement("SELECT * FROM items ORDER BY embedding <-> ? LIMIT 5");
neighborStmt.setObject(1, new PGvector(new float[] {1, 1, 1}));
ResultSet rs = neighborStmt.executeQuery();
while (rs.next()) {
    System.out.println((PGvector) rs.getObject("embedding"));
}
```

Add an approximate index

```java
Statement indexStmt = conn.createStatement();
indexStmt.executeUpdate("CREATE INDEX ON items USING ivfflat (embedding vector_l2_ops) WITH (lists = 100)");
// or
indexStmt.executeUpdate("CREATE INDEX ON items USING hnsw (embedding vector_l2_ops)");
```

Use `vector_ip_ops` for inner product and `vector_cosine_ops` for cosine distance

See a [full example](src/test/java/com/pgvector/JDBCJavaTest.java)

## Spring JDBC

Import the `PGvector` class

```java
import com.pgvector.PGvector;
```

Enable the extension

```java
jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
```

Create a table

```java
jdbcTemplate.execute("CREATE TABLE items (id bigserial PRIMARY KEY, embedding vector(3))");
```

Insert a vector

```java
Object[] insertParams = new Object[] { new PGvector(new float[] {1, 1, 1}) };
jdbcTemplate.update("INSERT INTO items (embedding) VALUES (?)", insertParams);
```

Get the nearest neighbors

```java
Object[] neighborParams = new Object[] { new PGvector(new float[] {1, 1, 1}) };
List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM items ORDER BY embedding <-> ? LIMIT 5", neighborParams);
for (Map row : rows) {
    System.out.println(row.get("embedding"));
}
```

Add an approximate index

```java
jdbcTemplate.execute("CREATE INDEX ON items USING ivfflat (embedding vector_l2_ops) WITH (lists = 100)");
// or
jdbcTemplate.execute("CREATE INDEX ON items USING hnsw (embedding vector_l2_ops)");
```

Use `vector_ip_ops` for inner product and `vector_cosine_ops` for cosine distance

See a [full example](src/test/java/com/pgvector/SpringJDBCTest.java)

## JDBC (Kotlin)

Import the `PGvector` class

```kotlin
import com.pgvector.PGvector
```

Enable the extension

```kotlin
val setupStmt = conn.createStatement()
setupStmt.executeUpdate("CREATE EXTENSION IF NOT EXISTS vector")
```

Register the vector type with your connection

```kotlin
PGvector.addVectorType(conn)
```

Create a table

```kotlin
val createStmt = conn.createStatement()
createStmt.executeUpdate("CREATE TABLE items (id bigserial PRIMARY KEY, embedding vector(3))")
```

Insert a vector

```kotlin
val insertStmt = conn.prepareStatement("INSERT INTO items (embedding) VALUES (?)")
insertStmt.setObject(1, PGvector(floatArrayOf(1.0f, 1.0f, 1.0f)))
insertStmt.executeUpdate()
```

Get the nearest neighbors

```kotlin
val neighborStmt = conn.prepareStatement("SELECT * FROM items ORDER BY embedding <-> ? LIMIT 5")
neighborStmt.setObject(1, PGvector(floatArrayOf(1.0f, 1.0f, 1.0f)))
val rs = neighborStmt.executeQuery()
while (rs.next()) {
  println(rs.getObject("embedding") as PGvector?)
}
```

Add an approximate index

```kotlin
val indexStmt = conn.createStatement()
indexStmt.executeUpdate("CREATE INDEX ON items USING ivfflat (embedding vector_l2_ops) WITH (lists = 100)")
// or
indexStmt.executeUpdate("CREATE INDEX ON items USING hnsw (embedding vector_l2_ops)")
```

Use `vector_ip_ops` for inner product and `vector_cosine_ops` for cosine distance

See a [full example](src/test/kotlin/com/pgvector/JDBCKotlinTest.kt)

## Groovy SQL

Import the `PGvector` class

```groovy
import com.pgvector.PGvector
```

Enable the extension

```groovy
sql.execute "CREATE EXTENSION IF NOT EXISTS vector"
```

Create a table

```groovy
sql.execute "CREATE TABLE items (id bigserial PRIMARY KEY, embedding vector(3))"
```

Insert a vector

```groovy
def params = [new PGvector([1, 1, 1] as float[])]
sql.executeInsert "INSERT INTO items (embedding) VALUES (?)", params
```

Get the nearest neighbors

```groovy
def params = [new PGvector([1, 1, 1] as float[])]
sql.eachRow("SELECT * FROM items ORDER BY embedding <-> ? LIMIT 5", params) { row ->
    println row.embedding
}
```

Add an approximate index

```groovy
sql.execute "CREATE INDEX ON items USING ivfflat (embedding vector_l2_ops) WITH (lists = 100)"
// or
sql.execute "CREATE INDEX ON items USING hnsw (embedding vector_l2_ops)"
```

Use `vector_ip_ops` for inner product and `vector_cosine_ops` for cosine distance

See a [full example](src/test/groovy/com/pgvector/GroovySqlTest.groovy)

## JDBC (Scala)

Import the `PGvector` class

```scala
import com.pgvector.PGvector
```

Enable the extension

```java
val setupStmt = conn.createStatement()
setupStmt.executeUpdate("CREATE EXTENSION IF NOT EXISTS vector")
```

Register the vector type with your connection

```scala
PGvector.addVectorType(conn)
```

Create a table

```scala
val createStmt = conn.createStatement()
createStmt.executeUpdate("CREATE TABLE items (id bigserial PRIMARY KEY, embedding vector(3))")
```

Insert a vector

```scala
val insertStmt = conn.prepareStatement("INSERT INTO items (embedding) VALUES (?)")
insertStmt.setObject(1, new PGvector(Array[Float](1, 1, 1)))
insertStmt.executeUpdate()
```

Get the nearest neighbors

```scala
val neighborStmt = conn.prepareStatement("SELECT * FROM items ORDER BY embedding <-> ? LIMIT 5")
neighborStmt.setObject(1, new PGvector(Array[Float](1, 1, 1)))
val rs = neighborStmt.executeQuery()
while (rs.next()) {
  println(rs.getObject("embedding").asInstanceOf[PGvector])
}
```

Add an approximate index

```scala
val indexStmt = conn.createStatement()
indexStmt.executeUpdate("CREATE INDEX ON items USING ivfflat (embedding vector_l2_ops) WITH (lists = 100)")
// or
indexStmt.executeUpdate("CREATE INDEX ON items USING hnsw (embedding vector_l2_ops)")
```

Use `vector_ip_ops` for inner product and `vector_cosine_ops` for cosine distance

See a [full example](src/test/scala/com/pgvector/JDBCScalaTest.scala)

## Slick

Import the `PGvector` class

```scala
import com.pgvector.PGvector
```

Enable the extension

```java
db.run(sqlu"CREATE EXTENSION IF NOT EXISTS vector")
```

Add a vector column

```scala
class Items(tag: Tag) extends Table[(String)](tag, "items") {
  def embedding = column[String]("embedding", O.SqlType("vector(3)"))
  def * = (embedding)
}
```

Insert a vector

```scala
val embedding = new PGvector(Array[Float](1, 1, 1)).toString
db.run(sqlu"INSERT INTO items (embedding) VALUES ($embedding::vector)")
```

Get the nearest neighbors

```scala
val embedding = new PGvector(Array[Float](1, 1, 1)).toString
db.run(sql"SELECT * FROM items ORDER BY embedding <-> $embedding::vector LIMIT 5".as[(String)])
```

Add an approximate index

```scala
db.run(sqlu"CREATE INDEX ON items USING ivfflat (embedding vector_l2_ops) WITH (lists = 100)")
// or
db.run(sqlu"CREATE INDEX ON items USING hnsw (embedding vector_l2_ops)")
```

Use `vector_ip_ops` for inner product and `vector_cosine_ops` for cosine distance

See a [full example](src/test/scala/com/pgvector/SlickTest.scala)

## History

View the [changelog](https://github.com/pgvector/pgvector-java/blob/master/CHANGELOG.md)

## Contributing

Everyone is encouraged to help improve this project. Here are a few ways you can help:

- [Report bugs](https://github.com/pgvector/pgvector-java/issues)
- Fix bugs and [submit pull requests](https://github.com/pgvector/pgvector-java/pulls)
- Write, clarify, or fix documentation
- Suggest or add new features

To get started with development:

```sh
git clone https://github.com/pgvector/pgvector-java.git
cd pgvector-java
createdb pgvector_java_test
mvn test
```

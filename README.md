# pgvector-java

[pgvector](https://github.com/pgvector/pgvector) support for Java and Scala

Supports [JDBC](https://jdbc.postgresql.org/) and [Slick](https://github.com/slick/slick)

[![Build Status](https://github.com/pgvector/pgvector-java/workflows/build/badge.svg?branch=master)](https://github.com/pgvector/pgvector-java/actions)

## Getting Started

For Java, add to `pom.xml` under `<dependencies>`:

```xml
<dependency>
  <groupId>com.pgvector</groupId>
  <artifactId>pgvector</artifactId>
  <version>0.1.2</version>
</dependency>
```

For Scala, add to `build.sbt`:

```sbt
libraryDependencies += "com.pgvector" % "pgvector" % "0.1.2"
```

And follow the instructions for your database library:

- [JDBC (Java)](#jdbc-java)
- [JDBC (Scala)](#jdbc-scala)
- [Slick](#slick)

## JDBC (Java)

Import the `PGvector` class

```java
import com.pgvector.PGvector;
```

Register the vector type with your connection

```java
PGvector.addVectorType(conn);
```

Create a table

```java
Statement createStmt = conn.createStatement();
createStmt.executeUpdate("CREATE TABLE items (embedding vector(3))");
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
    System.out.println((PGvector) rs.getObject(1));
}
```

Add an approximate index

```java
Statement indexStmt = conn.createStatement();
indexStmt.executeUpdate("CREATE INDEX my_index ON items USING ivfflat (embedding vector_l2_ops) WITH (lists = 100)");
```

Use `vector_ip_ops` for inner product and `vector_cosine_ops` for cosine distance

See a [full example](src/test/java/com/pgvector/JDBCJava.java)

## JDBC (Scala)

Import the `PGvector` class

```scala
import com.pgvector.PGvector
```

Register the vector type with your connection

```scala
PGvector.addVectorType(conn)
```

Create a table

```scala
val createStmt = conn.createStatement()
createStmt.executeUpdate("CREATE TABLE items (embedding vector(3))")
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
indexStmt.executeUpdate("CREATE INDEX my_index ON items USING ivfflat (embedding vector_l2_ops) WITH (lists = 100)")
```

Use `vector_ip_ops` for inner product and `vector_cosine_ops` for cosine distance

See a [full example](src/test/scala/com/pgvector/JDBCScala.scala)

## Slick

Import the `PGvector` class

```scala
import com.pgvector.PGvector
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
db.run(sqlu"CREATE INDEX my_index ON items USING ivfflat (embedding vector_l2_ops) WITH (lists = 100)")
```

Use `vector_ip_ops` for inner product and `vector_cosine_ops` for cosine distance

See a [full example](src/test/scala/com/pgvector/Slick.scala)

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
sbt test
```

# pgvector-java

[pgvector](https://github.com/pgvector/pgvector) examples for Java and Scala

Supports [JDBC](https://docs.oracle.com/javase/tutorial/jdbc/basics/index.html) and [Slick](https://github.com/slick/slick)

[![Build Status](https://github.com/pgvector/pgvector-java/workflows/build/badge.svg?branch=master)](https://github.com/pgvector/pgvector-java/actions)

## Getting Started

Follow the instructions for your database library:

- [JDBC (Java)](#jdbc-java)
- [JDBC (Scala)](#jdbc-scala)
- [Slick](#slick)

## JDBC (Java)

Create a table

```java
Statement createStmt = conn.createStatement();
createStmt.executeUpdate("CREATE TABLE items (embedding vector(3))");
```

Insert a vector

```java
PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO items (embedding) VALUES (?::vector)");
insertStmt.setString(1, "[1,1,1]");
insertStmt.executeUpdate();
```

Get the nearest neighbors

```java
PreparedStatement neighborStmt = conn.prepareStatement("SELECT * FROM items ORDER BY embedding <-> ?::vector LIMIT 5");
neighborStmt.setString(1, "[1,1,1]");
ResultSet rs = neighborStmt.executeQuery();
while (rs.next()) {
    System.out.println(rs.getString("embedding"));
}
```

Add an approximate index

```java
Statement indexStmt = conn.createStatement();
indexStmt.executeUpdate("CREATE INDEX my_index ON items USING ivfflat (embedding vector_l2_ops)");
```

Use `vector_ip_ops` for inner product and `vector_cosine_ops` for cosine distance

See a [full example](src/main/java/example1/JDBC.java)

## JDBC (Scala)

Create a table

```scala
val createStmt = conn.createStatement()
createStmt.executeUpdate("CREATE TABLE items (embedding vector(3))")
```

Insert a vector

```scala
val insertStmt = conn.prepareStatement("INSERT INTO items (embedding) VALUES (?::vector)")
insertStmt.setString(1, "[1,1,1]")
insertStmt.executeUpdate()
```

Get the nearest neighbors

```scala
val neighborStmt = conn.prepareStatement("SELECT * FROM items ORDER BY embedding <-> ?::vector LIMIT 5")
neighborStmt.setString(1, "[1,1,1]")
val rs = neighborStmt.executeQuery()
while (rs.next()) {
  println(rs.getString("embedding"))
}
```

Add an approximate index

```scala
val indexStmt = conn.createStatement()
indexStmt.executeUpdate("CREATE INDEX my_index ON items USING ivfflat (embedding vector_l2_ops)")
```

Use `vector_ip_ops` for inner product and `vector_cosine_ops` for cosine distance

See a [full example](src/main/scala/example2/JDBC.scala)

## Slick

Add a vector column

```scala
class Items(tag: Tag) extends Table[(String)](tag, "items") {
  def embedding = column[String]("embedding", O.SqlType("vector(3)"))
  def * = (embedding)
}
```

Insert a vector

```scala
object Pgvector {
  def toString(v: Array[Float]) = {
    "[" + v.mkString(",") + "]"
  }
}

val embedding = Pgvector.toString(Array(1, 1, 1))
db.run(sqlu"INSERT INTO items (embedding) VALUES ($embedding::vector)")
```

Get the nearest neighbors

```scala
val embedding = Pgvector.toString(Array(1, 1, 1))
db.run(sql"SELECT * FROM items ORDER BY embedding <-> $embedding::vector LIMIT 5".as[(String)])
```

Add an approximate index

```scala
db.run(sqlu"CREATE INDEX my_index ON items USING ivfflat (embedding vector_l2_ops)")
```

Use `vector_ip_ops` for inner product and `vector_cosine_ops` for cosine distance

See a [full example](src/main/scala/example2/Slick.scala)

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
sbt run
```

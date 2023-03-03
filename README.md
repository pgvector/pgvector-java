# pgvector-scala

[pgvector](https://github.com/pgvector/pgvector) examples for Scala

Supports [Slick](https://github.com/slick/slick)

[![Build Status](https://github.com/pgvector/pgvector-scala/workflows/build/badge.svg?branch=master)](https://github.com/pgvector/pgvector-scala/actions)

## Getting Started

Follow the instructions for your database library:

- [Slick](#slick)

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
val embedding = "[1,1,1]"
db.run(sqlu"INSERT INTO items (embedding) VALUES ($embedding::vector)")
```

Get the nearest neighbors

```scala
val embedding = "[1,1,1]"
db.run(sql"SELECT * FROM items ORDER BY embedding <-> $embedding::vector LIMIT 5".as[(String)])
```

Add an approximate index

```scala
db.run(sqlu"CREATE INDEX my_index ON items USING ivfflat (embedding vector_l2_ops)")
```

Use `vector_ip_ops` for inner product and `vector_cosine_ops` for cosine distance

See a [full example](src/main/scala/Slick.scala)

## Contributing

Everyone is encouraged to help improve this project. Here are a few ways you can help:

- [Report bugs](https://github.com/pgvector/pgvector-scala/issues)
- Fix bugs and [submit pull requests](https://github.com/pgvector/pgvector-scala/pulls)
- Write, clarify, or fix documentation
- Suggest or add new features

To get started with development:

```sh
git clone https://github.com/pgvector/pgvector-scala.git
cd pgvector-scala
createdb pgvector_scala_test
sbt run
```

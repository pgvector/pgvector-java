package com.pgvector

class HelloSpec extends munit.FunSuite {
  test("Hibernate") {
    Hibernate.example()
  }

  test("JDBCJava") {
    JDBCJava.example(false)
  }

  test("JDBCJava read binary") {
    JDBCJava.example(true)
  }

  test("SpringJDBC") {
    SpringJDBC.example()
  }

  test("JDBCScala") {
    JDBCScala.example()
  }

  test("Slick") {
    Slick.example()
  }
}

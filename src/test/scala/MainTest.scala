import org.scalatest.funsuite.AnyFunSuite

class MainTest extends AnyFunSuite {
  test("JDBCJava") {
    JDBCJava.example()
  }

  test("JDBCScala") {
    JDBCScala.example()
  }

  test("Slick") {
    Slick.example()
  }
}

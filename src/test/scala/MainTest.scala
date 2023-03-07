import org.scalatest.funsuite.AnyFunSuite

class MainTest extends AnyFunSuite {
  test("example1.JDBC") {
    example1.JDBC.example()
  }

  test("example2.JDBC") {
    example2.JDBC.example()
  }

  test("example2.Slick") {
    example2.Slick.example()
  }
}

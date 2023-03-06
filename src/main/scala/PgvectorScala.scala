object PgvectorScala {
  def toString(v: Array[Float]) = {
    "[" + v.mkString(",") + "]"
  }

  def parse(v: String) = {
    v.slice(1, v.length - 1).split(",").map(_.toFloat)
  }
}

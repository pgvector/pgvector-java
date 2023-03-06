object PgvectorScala {
  def toString(v: List[Float]) = {
    "[" + v.mkString(",") + "]"
  }

  def parse(v: String) = {
    v.slice(1, v.length - 1).split(",").map(_.toFloat).toList
  }
}

package math

case class Vec2(x: BigDecimal, y: BigDecimal) {
  def /(scalar: BigDecimal): Vec2 = this * (1.0 / scalar)

  def *(scalar: BigDecimal): Vec2 = copy(x * scalar, y * scalar)

  def distance(that: Vec2): BigDecimal = (this - that).length

  def -(that: Vec2): Vec2 = this + (that * -1)

  def +(that: Vec2): Vec2 = copy(x + that.x, y + that.y)

  def length: BigDecimal = BigDecimalMath.sqrt((x * x + y * y))

  override def toString: String = s"($x , $y)"

  override def equals(that: Any): Boolean = {
    that match {
      case v: Vec2 => x.equals(v.x) && y.equals(v.y)
      case _ => false
    }
  }
}

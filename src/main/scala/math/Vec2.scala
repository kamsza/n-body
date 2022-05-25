package math

case class Vec2(x: Double, y: Double) {
  def /(scalar: Double): Vec2 = this * (1.0 / scalar)

  def distance(that: Vec2): Double = (this - that).length

  def -(that: Vec2): Vec2 = this + (that * -1)

  def *(scalar: Double): Vec2 = Vec2(x * scalar, y * scalar)

  def +(that: Vec2): Vec2 = Vec2(x + that.x, y + that.y)

  def length: Double = Math.sqrt((x * x + y * y))

  override def toString: String = s"($x , $y)"

  override def equals(that: Any): Boolean = {
    that match {
      case v: Vec2 => x.equals(v.x) && y.equals(v.y)
      case _ => false
    }
  }
}

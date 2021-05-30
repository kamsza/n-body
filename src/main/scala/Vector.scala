case class Vector(x: Double, y: Double) {
  def -(that: Vector): Vector = this + (that * -1)
  def +(that: Vector): Vector = copy(x + that.x, y + that.y)
  def *(scalar: Double): Vector = copy(x * scalar, y * scalar)
  def /(scalar: Double): Vector = this * (1.0 / scalar)

  def distance(that: Vector): Double = (this - that).length
  def length: Double = math.sqrt(x * x + y * y)

  override def toString: String = s"($x , $y)"

  override def equals(that: Any): Boolean = {
    that match {
      case v: Vector => x.equals(v.x) && y.equals(v.y)
      case _ => false
    }
  }
}

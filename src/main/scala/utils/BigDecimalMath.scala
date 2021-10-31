package utils

import java.math.MathContext

object BigDecimalMath {

  val expectedInitialPrecision = 15

  val mathContext: MathContext = MathContext.DECIMAL128

  def round(value: java.math.BigDecimal): BigDecimal = value.round(mathContext)

  def sqrt(value: BigDecimal): BigDecimal = {
    val x = value.bigDecimal
    x.signum match {
      case 0 => return BigDecimal("0")
      case -1 => throw new ArithmeticException("Illegal sqrt(x) for x < 0: x = " + x)
      case 1 => // do nothing
    }
    val maxPrecision = mathContext.getPrecision + 6
    val acceptableError = new java.math.BigDecimal("1").movePointLeft(mathContext.getPrecision + 1)
    var result =  java.math.BigDecimal.valueOf(Math.sqrt(x.doubleValue))
    var adaptivePrecision = expectedInitialPrecision

    var last = new java.math.BigDecimal("0")
    if (adaptivePrecision < maxPrecision) {
      if (result.multiply(result).compareTo(x).equals(0)) return round(result) // early exit if x is a square number
      do {
        last = result
        adaptivePrecision <<= 1
        if (adaptivePrecision > maxPrecision) adaptivePrecision = maxPrecision
        val mc = new MathContext(adaptivePrecision, mathContext.getRoundingMode)
        result = x.divide(result, mc).add(last).multiply(new java.math.BigDecimal("0.5"), mc)
      } while ( {
        adaptivePrecision < maxPrecision || result.subtract(last).abs.compareTo(acceptableError) > 0
      })
    }
    round(result)
  }
}

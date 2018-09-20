package chylex.hee.system.util
import org.apache.commons.lang3.math.Fraction
import kotlin.math.ceil
import kotlin.math.floor

fun Float.floorToInt() = floor(this).toInt()
fun Float.ceilToInt() = ceil(this).toInt()

fun Double.floorToInt() = floor(this).toInt()
fun Double.ceilToInt() = ceil(this).toInt()

inline fun square(value: Int) = value * value
inline fun square(value: Float) = value * value
inline fun square(value: Double) = value * value

inline fun Float.toDegrees() = Math.toDegrees(this.toDouble())
inline fun Float.toRadians() = Math.toRadians(this.toDouble())

inline fun Double.toDegrees() = Math.toDegrees(this)
inline fun Double.toRadians() = Math.toRadians(this)

infix fun Int.over(denominator: Int): Fraction = Fraction.getFraction(this, denominator)

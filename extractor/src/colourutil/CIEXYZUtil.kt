package net.torvald.colourutil

import math.FastMath

/**
 * Created by minjaesong on 2017-01-12.
 */
object CIEXYZUtil {
    fun Color.brighterXYZ(scale: Float): Color {
        val xyz = this.toXYZ()
        xyz.X = xyz.X.times(1f + scale).clampOne()
        xyz.Y = xyz.Y.times(1f + scale).clampOne()
        xyz.Z = xyz.Z.times(1f + scale).clampOne()
        return xyz.toColor()
    }

    fun Color.darkerXYZ(scale: Float): Color {
        val xyz = this.toXYZ()
        xyz.X = xyz.X.times(1f - scale).clampOne()
        xyz.Y = xyz.Y.times(1f - scale).clampOne()
        xyz.Z = xyz.Z.times(1f - scale).clampOne()
        return xyz.toColor()
    }

    fun getGradient(scale: Float, fromCol: Color, toCol: Color): Color {
        val from = fromCol.toXYZ()
        val to = toCol.toXYZ()
        val newX = FastMath.interpolateLinear(scale, from.X, to.X)
        val newY = FastMath.interpolateLinear(scale, from.Y, to.Y)
        val newZ = FastMath.interpolateLinear(scale, from.Z, to.Z)
        val newAlpha = FastMath.interpolateLinear(scale, from.alpha, to.alpha)

        return CIEXYZ(newX, newY, newZ, newAlpha).toColor()
    }

    fun Color.toXYZ(): CIEXYZ = RGB(this).toXYZ()

    fun RGB.toXYZ(): CIEXYZ {
        val newR = if (r > 0.04045f)
            ((r + 0.055f) / 1.055f).powerOf(2.4f)
        else r / 12.92f
        val newG = if (g > 0.04045f)
            ((g + 0.055f) / 1.055f).powerOf(2.4f)
        else g / 12.92f
        val newB = if (b > 0.04045f)
            ((b + 0.055f) / 1.055f).powerOf(2.4f)
        else b / 12.92f

        val x = 0.4124564f * newR + 0.3575761f * newG + 0.1804375f * newB
        val y = 0.2126729f * newR + 0.7151522f * newG + 0.0721750f * newB
        val z = 0.0193339f * newR + 0.1191920f * newG + 0.9503041f * newB

        return CIEXYZ(x, y, z, alpha)
    }

    fun CIEXYZ.toRGB(): RGB {
        var r = 3.2404542f * X - 1.5371385f * Y - 0.4985314f * Z
        var g = -0.9692660f * X + 1.8760108f * Y + 0.0415560f * Z
        var b = 0.0556434f * X - 0.2040259f * Y + 1.0572252f * Z

        if (r > 0.0031308f)
            r = 1.055f * r.powerOf(1f / 2.4f) - 0.055f
        else
            r *= 12.92f
        if (g > 0.0031308f)
            g = 1.055f * g.powerOf(1f / 2.4f) - 0.055f
        else
            g *= 12.92f
        if (b > 0.0031308f)
            b = 1.055f * b.powerOf(1f / 2.4f) - 0.055f
        else
            b *= 12.92f

        return RGB(r, g, b, alpha)
    }

    fun CIEXYZ.toColor(): Color {
        val rgb = this.toRGB()
        return Color(rgb.r, rgb.g, rgb.b, rgb.alpha)
    }

    fun CIEYxy.toCIEXYZ(): CIEXYZ {
        return CIEXYZ(
                x * luma / y,
                luma,
                (1f - x - y) * luma / y
        )
    }

    fun CIEYxy.toColor(): Color {
        return this.toCIEXYZ().toColor()
    }

    fun colourTempToXYZ(temp: Float, Y: Float): CIEXYZ {
        val x = if (temp < 7000)
            -4607000000f / FastMath.pow(temp, 3f) + 2967800f / FastMath.pow(temp, 2f) + 99.11f / temp + 0.244063f
        else
            -2006400000f / FastMath.pow(temp, 3f) + 1901800f / FastMath.pow(temp, 2f) + 247.48f / temp + 0.237040f

        val y = -3f * FastMath.pow(x, 2f) + 2.870f * x - 0.275f

        return CIEXYZ(x * Y / y, Y, (1 - x - y) * Y / y)
    }

    private fun Float.powerOf(exp: Float) = FastMath.pow(this, exp)
    private fun Float.clampOne() = if (this > 1f) 1f else if (this < 0f) 0f else this
}

/** Range: X, Y, Z: 0 - 1.0+ (One-based-plus) */
data class CIEXYZ(var X: Float = 0f, var Y: Float = 0f, var Z: Float = 0f, var alpha: Float = 1f) {
    init {
        //if (X > 2f || Y > 2f || Z > 2f)
        //    throw IllegalArgumentException("Value range error - this version of CIEXYZ is one-based (0.0 - 1.0+): ($X, $Y, $Z)")
    }
}

data class CIEYxy(var luma: Float, var x: Float, var y: Float) {

}

/** Range: r, g, b: 0 - 1.0 (One-based) */
data class RGB(var r: Float = 0f, var g: Float = 0f, var b: Float = 0f, var alpha: Float = 1f) {
    constructor(color: Color) : this() {
        r = color.r; g = color.g; b = color.b; alpha = color.a
    }
}


data class Color(var r: Float, var g: Float, var b: Float, var a: Float) {
    private fun Float.truncate() = if (this < 0f) 0f else if (this > 1f) 1f else this

    fun toHTMLColorCode(): String {
        val intR = (r.truncate() * 255f).toInt().toString(16).toUpperCase().padStart(2, '0')
        val intG = (g.truncate() * 255f).toInt().toString(16).toUpperCase().padStart(2, '0')
        val intB = (b.truncate() * 255f).toInt().toString(16).toUpperCase().padStart(2, '0')

        return "#$intR$intG$intB"
    }
}
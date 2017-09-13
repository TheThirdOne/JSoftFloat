package types;

/**
 * Represents the Binary32 format
 */
public class Float32 extends Floating {
    // TODO: make a more abstract binary float class
    public static final Float32 Zero = new Float32(0),
            NegativeZero = new Float32(0x80000000),
            NaN = new Float32(0x7F800001),
            Infinity = new Float32(0x7F800000),
            NegativeInfinity = new Float32(0xFF800000);

    public final int bits;

    public Float32(int bits) {
        this.bits = bits;
    }

    public Float32(boolean sign, int exponent, int significand) {
        this(((sign) ? 0x80000000 : 0) | (((exponent + 127) & 0xFF) << 23) | (significand & 0x007FFFFF));
    }

    public int exponent() {
        return ((bits >>> 23) & 0xFF) - 127;
    }

    /**
     * @param num An integer to be converted to
     * @return
     */
    public static Float32 fromInteger(int num) {
        if (num == 0) return Zero;
        boolean sign = num < 0;
        num = sign ? -num : num;
        int exponent = 0, significand = 0;
        for (int i = 30; i >= 0; i--) {
            if (((num >> i) & 1) == 1) {
                exponent = i + 127;
                significand = (num << (32 - i)) >>> 9;
                break;
            }
        }
        int bits = ((sign) ? 0x80000000 : 0) | (exponent << 23) | significand;
        return new Float32(bits);
    }

    public Float32 negate() {
        return new Float32(bits ^ 0x80000000); // Flip the sign bit
    }

    public Float32 abs() {
        return new Float32(bits & 0x7FFFFFFF);
    }

    public Float32 copySign(Float32 signToTake) {
        return new Float32((bits & 0x7FFFFFFF) | (signToTake.bits & 0x80000000));
    }

    public boolean isSignMinus() {
        return (bits >>> 31) == 1;
    }

    public boolean isInfinite() {
        return exponent() == 128 && (bits & 0x007FFFFF) == 0;
    }

    public boolean isNormal() {
        return exponent() != -127 && exponent() != 128;
    }

    public boolean isSubnormal() {
        return exponent() == -127 && !isZero();
    }

    public boolean isNaN() {
        return exponent() == 128 && !isInfinite();
    }

    public boolean isSignalling() {
        // TODO: implement
        return false;
    }

    public boolean isCanonical() {
        // TODO: implement
        return true;
    }

    public boolean isZero() {
        return bits == 0 || bits == 0x80000000;
    }
}

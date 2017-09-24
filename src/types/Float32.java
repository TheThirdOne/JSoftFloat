package types;

import internal.ExactFloat;
import main.Environment;
import main.Flags;
import main.RoundingMode;

import java.math.BigInteger;

/**
 * Represents the Binary32 format
 */
public class Float32 extends Floating<Float32> {
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

    @Override
    public Float32 NaN() {
        return NaN;
    }

    @Override
    public Float32 Zero() {
        return Zero;
    }

    @Override
    public Float32 NegativeZero() {
        return NegativeZero;
    }

    @Override
    public Float32 Infinity() {
        return Infinity;
    }

    @Override
    public Float32 NegativeInfinity() {
        return NegativeInfinity;
    }

    @Override
    public Float32 fromExactFloat(ExactFloat ef, Environment env) {
        // TODO: move some rounding code out so that implementing new types is easier
        if (ef.isZero()) {
            return ef.sign ? Float32.NegativeZero : Float32.Zero;
        }
        int normalizedExponent = ef.exponent + ef.significand.bitLength();
        if (normalizedExponent <= -150) {
            // Section 7.5
            env.flags.add(Flags.underflow);
            env.flags.add(Flags.inexact);
            return ef.sign ? Float32.NegativeZero : Float32.Zero;
        } else if (normalizedExponent <= -126) {
            // Subnormal
            ExactFloat f = ef.normalize();
            if (f.exponent > -150) {
                assert f.significand.bitLength() <= 23 : "Its actually normal";
                return new Float32(f.sign, -127, f.significand.shiftLeft(149 + f.exponent).intValueExact());
            }

            env.flags.add(Flags.inexact);
            int bitsToRound = -149 - f.exponent;
            BigInteger mainBits = f.significand.shiftRight(bitsToRound).shiftLeft(bitsToRound);
            BigInteger roundedBits = f.significand.subtract(mainBits);

            Float32 towardsZero = new Float32(ef.sign, -127, f.significand.shiftRight(bitsToRound).intValueExact());
            Float32 awayZero;
            BigInteger upBits = f.significand.shiftRight(bitsToRound).add(BigInteger.valueOf(1));
            if (upBits.testBit(0) || upBits.bitLength() < 23) {
                awayZero = new Float32(ef.sign, -127, upBits.intValueExact());
            } else {
                awayZero = new Float32(ef.sign, -126, upBits.intValueExact() & 0x007FFFFF);
            }

            switch (env.mode) {
                case zero:
                    return towardsZero;
                case max:
                case min:
                    if (ef.sign != (env.mode == RoundingMode.max)) {
                        return awayZero;
                    } else {
                        return towardsZero;
                    }
            }

            if (roundedBits.equals(BigInteger.ONE.shiftLeft(bitsToRound - 1))) {
                if (env.mode == RoundingMode.away || (awayZero.bits & 1) == 0) {
                    return awayZero;
                } else {
                    return towardsZero;
                }
            } else if (roundedBits.compareTo(BigInteger.ONE.shiftLeft(bitsToRound - 1)) > 0) {
                return awayZero;
            } else {
                return towardsZero;
            }
        } else if (normalizedExponent > 128) { // TODO: check off by one
            // Section 7.4
            env.flags.add(Flags.overflow);
            env.flags.add(Flags.inexact);
            switch (env.mode) {
                case zero:
                    return new Float32(ef.sign, 127, -1); // Largest finite number
                case min:
                case max:
                    if (ef.sign != (env.mode == RoundingMode.max)) {
                        return ef.sign ? Float32.NegativeInfinity : Float32.Infinity;
                    } else {
                        return new Float32(ef.sign, 127, -1); // Largest finite number
                    }
                case away:
                case even:
                    return ef.sign ? Float32.NegativeInfinity : Float32.Infinity;
            }
            assert false : "Not reachable";
            return ef.sign ? Float32.NegativeInfinity : Float32.Infinity;
        } else {
            ExactFloat f = ef.normalize();
            if (f.significand.bitLength() <= 24) {
                // No rounding needed
                assert f.exponent + f.significand.bitLength() - 1 > -127 : "Its actually subnormal";
                Float32 a = new Float32(f.sign, f.exponent + f.significand.bitLength() - 1, f.significand.shiftLeft(24 - f.significand.bitLength()).intValueExact() & 0x007FFFFF);

                return a;
            }
            env.flags.add(Flags.inexact);
            int bitsToRound = f.significand.bitLength() - 24;
            BigInteger mainBits = f.significand.shiftRight(bitsToRound).shiftLeft(bitsToRound);
            BigInteger roundedBits = f.significand.subtract(mainBits);
            Float32 awayZero;
            BigInteger upBits = f.significand.shiftRight(bitsToRound).add(BigInteger.valueOf(1));

            Float32 towardsZero = new Float32(f.sign, f.exponent + 23 + bitsToRound, f.significand.shiftRight(bitsToRound).intValueExact() & 0x007FFFFF);
            if (upBits.testBit(0) || upBits.bitLength() <= 24) {
                awayZero = new Float32(f.sign, f.exponent + 23 + bitsToRound, upBits.intValueExact() & 0x007FFFFF);
            } else {
                awayZero = new Float32(f.sign, f.exponent + 24 + bitsToRound, upBits.shiftRight(1).intValueExact() & 0x007FFFFF);
            }

            switch (env.mode) {
                case zero:
                    return towardsZero;
                case max:
                case min:
                    if (f.sign != (env.mode == RoundingMode.max)) {
                        return awayZero;
                    } else {
                        return towardsZero;
                    }
            }

            if (roundedBits.equals(BigInteger.ONE.shiftLeft(bitsToRound - 1))) {
                if (env.mode == RoundingMode.away || (awayZero.bits & 1) == 0) {
                    return awayZero;
                } else {
                    return towardsZero;
                }
            } else if (roundedBits.compareTo(BigInteger.ONE.shiftLeft(bitsToRound - 1)) > 0) {
                return awayZero;
            } else {
                return towardsZero;
            }
        }
    }

    public static Float32 fromExact(ExactFloat ef, Environment e){
        return Zero.fromExactFloat(ef,e);
    }

    @Override
    public ExactFloat toExactFloat() {
        assert !isInfinite() : "Infinity is not exact";
        assert !isNaN() : "NaNs are not exact";
        assert !isZero() : "Zeros should be handled explicitly";

        boolean sign = isSignMinus();
        int exponent;
        BigInteger significand;
        if (isZero()) {
            exponent = 0;
            significand = BigInteger.ZERO;
        } else if (isNormal()) {
            exponent = exponent() - 23;
            significand = BigInteger.valueOf((bits & 0x007FFFFF) + 0x00800000); // Add back the implied one
        } else if (isSubnormal()) {
            exponent = exponent() - 22;
            significand = BigInteger.valueOf(bits & 0x007FFFFF);
        } else {
            assert false : "This should not be reachable";
            return null;
        }
        return new ExactFloat(sign, exponent, significand);
    }

    @Override
    public int maxPrecision() {
        // TODO: make a tight bound around actual required precision
        return 30;
    }
}

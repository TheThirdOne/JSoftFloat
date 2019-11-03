package jsoftfloat.types;

import jsoftfloat.Environment;
import jsoftfloat.Flags;
import jsoftfloat.RoundingMode;
import jsoftfloat.internal.ExactFloat;

import java.math.BigInteger;

/**
 * Represents the Binary32 format
 */
public class Float32 extends Floating<Float32> {
    // TODO: make a more abstract binary float class
    public static final Float32 Zero = new Float32(0),
            NegativeZero = new Float32(0x80000000),
            NaN = new Float32(0x7FC00000),
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

    // Section 6.2.1
    public boolean isSignalling() {
        if (!isNaN()) return false;
        return (bits & 0x400000) == 0;
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

    // Some constants that allow fromExactFloat to be mostly copied
    private static final int sigbits = 23, expbits = 8,
            maxexp = 1 << (expbits - 1),
            minexp = -(1 << (expbits - 1)) + 1,
            sigmask = (1 << sigbits) - 1;

    @Override
    public Float32 fromExactFloat(ExactFloat ef, Environment env) {
        if (ef.isZero()) {
            return ef.sign ? Float32.NegativeZero : Float32.Zero;
        }
        ef = ef.normalize();
        int normalizedExponent = ef.exponent + ef.significand.bitLength();

        // Used to calculate how to round at the end
        Float32 awayZero, towardsZero;
        BigInteger roundedBits;
        int bitsToRound;

        if (normalizedExponent <= minexp + 1) {
            // Subnormal

            if (ef.exponent > minexp - sigbits) {
                assert ef.significand.bitLength() <= sigbits : "Its actually normal";
                return new Float32(ef.sign, minexp, ef.significand.shiftLeft(-(minexp - sigbits + 1) + ef.exponent).intValueExact());
            }

            env.flags.add(Flags.inexact);
            env.flags.add(Flags.underflow); // Section 7.5
            bitsToRound = (minexp - sigbits + 1) - ef.exponent;
            BigInteger mainBits = ef.significand.shiftRight(bitsToRound).shiftLeft(bitsToRound);
            roundedBits = ef.significand.subtract(mainBits);

            towardsZero = new Float32(ef.sign, minexp, ef.significand.shiftRight(bitsToRound).intValueExact());
            BigInteger upBits = ef.significand.shiftRight(bitsToRound).add(BigInteger.valueOf(1));
            if (upBits.testBit(0) || upBits.bitLength() <= sigbits) {
                assert upBits.bitLength() <= sigbits;
                awayZero = new Float32(ef.sign, minexp, upBits.intValueExact());
            } else {
                awayZero = new Float32(ef.sign, minexp + 1, upBits.intValueExact() & sigmask);
            }
        } else if (normalizedExponent > maxexp) {
            // Section 7.4
            env.flags.add(Flags.overflow);
            env.flags.add(Flags.inexact);
            switch (env.mode) {
                case zero:
                    return new Float32(ef.sign, maxexp - 1, -1); // Largest finite number
                case min:
                case max:
                    if (ef.sign != (env.mode == RoundingMode.max)) {
                        return ef.sign ? Float32.NegativeInfinity : Float32.Infinity;
                    } else {
                        return new Float32(ef.sign, maxexp - 1, -1); // Largest finite number
                    }
                case away:
                case even:
                    return ef.sign ? Float32.NegativeInfinity : Float32.Infinity;
            }
            assert false : "Not reachable";
            return ef.sign ? Float32.NegativeInfinity : Float32.Infinity;
        } else {
            if (ef.significand.bitLength() <= (sigbits + 1)) {
                // No rounding needed
                assert ef.exponent + ef.significand.bitLength() - 1 > minexp : "Its actually subnormal";
                Float32 a = new Float32(ef.sign, ef.exponent + ef.significand.bitLength() - 1, ef.significand.shiftLeft((sigbits + 1) - ef.significand.bitLength()).intValueExact() & sigmask);

                return a;
            }
            env.flags.add(Flags.inexact);
            bitsToRound = ef.significand.bitLength() - (sigbits + 1);
            BigInteger mainBits = ef.significand.shiftRight(bitsToRound).shiftLeft(bitsToRound);
            roundedBits = ef.significand.subtract(mainBits);

            BigInteger upBits = ef.significand.shiftRight(bitsToRound).add(BigInteger.valueOf(1));

            towardsZero = new Float32(ef.sign, ef.exponent + sigbits + bitsToRound, ef.significand.shiftRight(bitsToRound).intValueExact() & sigmask);
            if (upBits.testBit(0) || upBits.bitLength() <= sigbits + 1) {
                awayZero = new Float32(ef.sign, ef.exponent + sigbits + bitsToRound, upBits.intValueExact() & sigmask);
            } else {
                awayZero = new Float32(ef.sign, ef.exponent + (sigbits + 1) + bitsToRound, upBits.shiftRight(1).intValueExact() & sigmask);
            }

        }

        // Either round towards or away from zero based on rounding mode
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

        // See which result is closer to the non-rounded version
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

    public static Float32 fromExact(ExactFloat ef, Environment e) {
        return Zero.fromExactFloat(ef, e);
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

package internal;


import main.Environment;
import main.RoundingMode;

import java.math.BigInteger;

/**
 * A helper type to generalize floating point exact operations. This helps to reduce the extra rounding code into just
 * one place. It isn't able to refer to every exact float though. 1/3 is not representable as it requires infinite
 * precision, but any finite significand is representable.
 * <p>
 * Square Root and Division cannot really use this because they cannot avoid the precision issue. They have to stop
 * computing digits once they get past the maximum length of the significand.
 */
public class ExactFloat implements Comparable<ExactFloat> {
    // Value = (-1)^sign * significand * 2^exponent
    public final boolean sign;
    public final int exponent;
    public final BigInteger significand;

    public ExactFloat(boolean sign, int exp, BigInteger sig) {
        this.sign = sign;
        exponent = exp;
        significand = sig;
    }

    public ExactFloat add(ExactFloat other) {
        int expoDiff = exponent - other.exponent;
        int finalExpo = Math.min(exponent, other.exponent);

        if (sign != other.sign) {
            int comp = this.abs().compareTo(other.abs());
            // Section 6.3
            if (comp == 0) {
                // Caller should handle what to do in the event a zero pops out
                return new ExactFloat(false, 0, BigInteger.ZERO);
            }

            boolean finalSign = (comp > 0) ? sign : other.sign;
            if (expoDiff < 0) {
                return new ExactFloat(finalSign, finalExpo, significand.subtract(other.significand.shiftLeft(-expoDiff)).abs());
            } else {
                return new ExactFloat(finalSign, finalExpo, significand.shiftLeft(expoDiff).subtract(other.significand).abs());
            }
        } else {
            if (expoDiff < 0) {
                return new ExactFloat(sign, finalExpo, significand.add(other.significand.shiftLeft(-expoDiff)).abs());
            } else {
                return new ExactFloat(sign, finalExpo, significand.shiftLeft(expoDiff).add(other.significand).abs());
            }
        }
    }

    public ExactFloat multiply(ExactFloat other) {
        // 0 * x = 0
        // Sign is the xor of the input signs - Section 6.3
        if (isZero() || other.isZero()) {
            return new ExactFloat(sign != other.sign, 0, BigInteger.ZERO);
        }
        return new ExactFloat(sign != other.sign, exponent + other.exponent, significand.multiply(other.significand));
    }

    /**
     * Divides this number by another
     * <p>
     * This method uses simple long division rather than a more sophisticated process to compute a/b. This is mainly
     * because it needs to be completely correct for a specific amount of bytes to be able to round correctly, and
     * because exact divisions such as 1/2 will be completely exact. This is not a guarantee with many other methods.
     * <p>
     * Many implementation use Newton–Raphson division because it is much faster, but the analysis to guarantee
     * correct rounding behavior is beyond me.
     *
     * @param other    the dividend
     * @param accuracy the number of bits to compute
     * @return An exact float that equals this/other with the first accuracy bits correct
     * @author Benjamin Landers
     */
    public ExactFloat divide(ExactFloat other, int accuracy) {
        assert accuracy > 0 : "Accuracy must be a positive number";
        assert !other.isZero() : "Divide by Zero is not valid";
        ExactFloat a = normalize(), b = other.normalize();
        BigInteger divisor = a.significand, dividend = b.significand;
        BigInteger outbits = BigInteger.ZERO;
        int expChange = dividend.bitLength() - divisor.bitLength();
        // Line up the numbers
        if (divisor.bitLength() > dividend.bitLength()) {
            dividend = dividend.shiftLeft(divisor.bitLength() - dividend.bitLength());
        } else {
            divisor = divisor.shiftLeft(dividend.bitLength() - divisor.bitLength());
        }


        int count = 0;
        // Perform long division
        while (outbits.bitLength() < accuracy) {
            outbits = outbits.shiftLeft(1);
            if (divisor.compareTo(dividend) >= 0) {
                divisor = divisor.subtract(dividend);
                outbits = outbits.add(BigInteger.ONE);

            }
            count++;
            divisor = divisor.shiftLeft(1);
            if (divisor.equals(BigInteger.ZERO)) {
                break;
            }

        }
        return new ExactFloat(a.sign != b.sign, a.exponent - b.exponent - count - expChange + 1, outbits);

    }

    public ExactFloat normalize() {
        if (isZero()) return new ExactFloat(sign, 0, BigInteger.ZERO);
        return new ExactFloat(sign, exponent + significand.getLowestSetBit(),
                significand.shiftRight(significand.getLowestSetBit()));
    }

    public ExactFloat roundToIntegral(Environment env) {
        if (isZero()) return this;
        ExactFloat f = normalize();
        if (f.exponent >= 0) return f;

        int bitsToRound = -f.exponent;
        BigInteger mainBits = f.significand.shiftRight(bitsToRound).shiftLeft(bitsToRound);
        BigInteger roundedBits = f.significand.subtract(mainBits);

        ExactFloat zeroRounded = new ExactFloat(f.sign, f.exponent, mainBits).normalize();
        ExactFloat oneRounded = zeroRounded.add(new ExactFloat(f.sign, 0, BigInteger.valueOf(1)));
        if (env.mode == RoundingMode.zero) {
            return zeroRounded;
        }

        if (env.mode == RoundingMode.max || env.mode == RoundingMode.min) {
            if ((env.mode == RoundingMode.max) == f.sign) {
                // if we are rounding towards zero (max & < 0 or min & > 0)
                return zeroRounded;
            } else {
                // Alternatively away from zero (round to zero + 1)
                return oneRounded;
            }
        }

        if (roundedBits.equals(BigInteger.ONE.shiftLeft(bitsToRound - 1))) {
            // If there is a tie round according to the rounding mode
            if (env.mode == RoundingMode.away || zeroRounded.significand.testBit(0)) {
                return oneRounded;
            } else {
                return zeroRounded;
            }
        } else if (roundedBits.compareTo(BigInteger.ONE.shiftLeft(bitsToRound - 1)) > 0) { // TODO: check sign
            return oneRounded;
        } else {
            return zeroRounded;
        }
    }

    public int toIntegral(Environment env) {
        if (isZero()) return 0;

        ExactFloat f = roundToIntegral(env).normalize();

        assert f.exponent >= 0 : "There can't be any fractions at this point";
        if (f.significand.bitLength() + f.exponent > 31) {
            return f.sign ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        }
        return (sign ? -1 : 1) * f.significand.shiftLeft(f.exponent).intValueExact();
    }

    @Override
    public int compareTo(ExactFloat other) {
        if (isZero()) {
            if (other.isZero()) {
                return 0;
            }
            return other.sign ? 1 : -1;
        }
        if (sign != other.sign) {
            return sign ? -1 : 1;
        }
        if (exponent - other.exponent + significand.bitLength() - other.significand.bitLength() > 0) {
            return sign ? -1 : 1;
        } else if (exponent - other.exponent + significand.bitLength() - other.significand.bitLength() < 0) {
            return sign ? 1 : -1;
        } else {
            if (exponent > other.exponent) {
                return significand.compareTo(other.significand.shiftLeft(exponent - other.exponent)) * (sign ? -1 : 1);
            } else {
                return significand.shiftLeft(other.exponent - exponent).compareTo(other.significand) * (sign ? -1 : 1);
            }
        }
    }

    public ExactFloat abs() {
        return new ExactFloat(false, exponent, significand);
    }

    public ExactFloat negate() {
        return new ExactFloat(!sign, exponent, significand);
    }

    public boolean isZero() {
        return significand.equals(BigInteger.ZERO);
    }
}

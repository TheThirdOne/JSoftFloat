package types;


import java.math.BigInteger;
import main.Environment;
import main.Flags;
import main.RoundingMode;

/**
 * A helper type to generalize floating point exact operations. This helps to reduce the extra rounding code into just
 * one place. It isn't able to refer to every exact float though. 1/3 is not representable as it requires infinite
 * precision, but any finite significand is representable.
 *
 * Square Root and Division cannot really use this because they cannot avoid the precision issue. They have to stop
 * computing digits once they get past the maximum length of the significand.
 */
public class ExactFloat extends Floating implements Comparable<ExactFloat> {
    // Value = (-1)^sign * significand * 2^exponent
    private boolean sign;
    private int exponent;
    private BigInteger significand;
    public ExactFloat(Float32 f){
        assert !f.isInfinite() : "Infinity is not exact";
        assert !f.isNaN() : "NaNs are not exact";
        assert !f.isZero(): "Zeros should be handled explicitly";
        sign = f.isSignMinus();
        if(f.isZero()){
            exponent = 0;
            significand = BigInteger.ZERO;
        }else if(f.isNormal()){
            exponent = f.exponent()-23;
            significand = BigInteger.valueOf((f.bits & 0x007FFFFF)+0x00800000); // Add back the implied one
        }else if(f.isSubnormal()){
            exponent = f.exponent()-23;
            significand = BigInteger.valueOf(f.bits & 0x007FFFFF);
        }else{
            assert false : "This should not be reachable";
        }
    }
    public ExactFloat(boolean sign, int exp, BigInteger sig){
        this.sign = sign;
        exponent = exp;
        significand = sig;
    }
    public Float32 toFloat32(Environment env){
        if(isZero()){
            return (sign)?Float32.NegativeZero:Float32.Zero;
        }
        int normalizedExponent = exponent + significand.bitLength();
        if(normalizedExponent <= -150){
            // Section 7.5
            env.flags.add(Flags.underflow);
            env.flags.add(Flags.inexact);
            return sign?Float32.NegativeZero:Float32.Zero;
        }else if(normalizedExponent <= -127){
            // Subnormal
            ExactFloat f = normalize();
            if(f.exponent >= -150){
                assert f.significand.bitLength() <= 23 : "Its actually normal";
                return new Float32(f.sign,-127, f.significand.shiftLeft(150+f.exponent).intValueExact());
            }

            env.flags.add(Flags.inexact);
            int bitsToRound = -150 - f.exponent;
            BigInteger mainBits = f.significand.shiftRight(bitsToRound).shiftLeft(bitsToRound);
            BigInteger roundedBits = f.significand.subtract(mainBits);

            Float32 towardsZero = new Float32(sign, -127, f.significand.shiftRight(bitsToRound).intValueExact());
            Float32 awayZero;
            BigInteger upBits = f.significand.shiftRight(bitsToRound).add(BigInteger.valueOf(1));
            if(upBits.testBit(0) || upBits.bitLength() < 23){
                awayZero = new Float32(sign,-127,upBits.intValueExact());
            }else{
                awayZero = new Float32(sign, -126,upBits.intValueExact()&0x007FFFFF);
            }

            switch (env.mode){
                case zero:
                    return towardsZero;
                case max:
                case min:
                    if(sign != (env.mode == RoundingMode.max)){
                        return awayZero;
                    }else{
                        return towardsZero;
                    }
            }

            if(roundedBits.equals(BigInteger.ONE.shiftLeft(bitsToRound-1))){
                if(env.mode == RoundingMode.away || (awayZero.bits & 1) == 0){
                    return awayZero;
                }else{
                    return towardsZero;
                }
            }else if(roundedBits.compareTo(BigInteger.ONE.shiftLeft(bitsToRound-1)) > 0 ){
                return awayZero;
            }else {
                return towardsZero;
            }
        }else if(normalizedExponent > 128){ // TODO: check off by one
            // Section 7.4
            env.flags.add(Flags.overflow);
            env.flags.add(Flags.inexact);
            switch (env.mode){
                case zero:
                    return new Float32(sign, 127, -1); // Largest finite number
                case min:
                case max:
                    if(sign != (env.mode == RoundingMode.max)){
                        return sign?Float32.NegativeInfinity:Float32.Infinity;
                    }else{
                        return new Float32(sign, 127, -1); // Largest finite number
                    }
                case away:
                case even:
                    return sign?Float32.NegativeInfinity:Float32.Infinity;
            }
            assert false : "Not reachable";
            return sign?Float32.NegativeInfinity:Float32.Infinity;
        }else {
            ExactFloat f = normalize();
            if(f.significand.bitLength() <= 24){
                // No rounding needed
                Float32 a = new Float32(sign, f.exponent+f.significand.bitLength()-1, f.significand.shiftLeft(24-f.significand.bitLength()).intValueExact() & 0x007FFFFF);

                return a;
            }
            env.flags.add(Flags.inexact);
            int bitsToRound = f.significand.bitLength() - 24;
            BigInteger mainBits = f.significand.shiftRight(bitsToRound).shiftLeft(bitsToRound);
            BigInteger roundedBits = f.significand.subtract(mainBits);
            Float32 awayZero;
            BigInteger upBits = f.significand.shiftRight(bitsToRound).add(BigInteger.valueOf(1));

            Float32 towardsZero = new Float32(sign, f.exponent+23+bitsToRound, f.significand.shiftRight(bitsToRound).intValueExact()& 0x007FFFFF);
            if(upBits.testBit(0) || upBits.bitLength() < 24){
                awayZero = new Float32(sign,f.exponent+23+bitsToRound,upBits.intValueExact()& 0x007FFFFF);
            }else{
                awayZero = new Float32(sign, f.exponent+24+bitsToRound, upBits.shiftRight(1).intValueExact()&0x007FFFFF);
            }

            switch (env.mode){
                case zero:
                    return towardsZero;
                case max:
                case min:
                    if(sign != (env.mode == RoundingMode.max)){
                        return awayZero;
                    }else{
                        return towardsZero;
                    }
            }

            if(roundedBits.equals(BigInteger.ONE.shiftLeft(bitsToRound-1))){
                if(env.mode == RoundingMode.away || (awayZero.bits & 1) == 0){
                    return awayZero;
                }else{
                    return towardsZero;
                }
            }else if(roundedBits.compareTo(BigInteger.ONE.shiftLeft(bitsToRound-1)) > 0 ){
                return awayZero;
            }else {
                return towardsZero;
            }

        }
    }
    public ExactFloat add(ExactFloat other) {
        int expoDiff = exponent - other.exponent;
        int finalExpo = Math.min(exponent,other.exponent);

        if(sign != other.sign){
            int comp = this.abs().compareTo(other.abs());
            // Section 6.3
            if(comp == 0){
                // Caller should handle what to do in the event a zero pops out
                return new ExactFloat(false, 0, BigInteger.ZERO);
            }

            boolean finalSign = (comp > 0)?sign:other.sign;
            if(expoDiff < 0){
                return new ExactFloat(finalSign, finalExpo, significand.subtract(other.significand.shiftLeft(-expoDiff)).abs());
            }else{
                return new ExactFloat(finalSign, finalExpo, significand.shiftLeft(expoDiff).subtract(other.significand).abs());
            }
        } else {
            if(expoDiff < 0){
                return new ExactFloat(sign, finalExpo, significand.add(other.significand.shiftLeft(-expoDiff)).abs());
            }else{
                return new ExactFloat(sign, finalExpo, significand.shiftLeft(expoDiff).add(other.significand).abs());
            }
        }
    }

    public ExactFloat multiply(ExactFloat other){
        // 0 * x = 0
        // Sign is the xor of the input signs - Section 6.3
        if(isZero() || other.isZero()){
            return new ExactFloat(sign != other.sign, 0, BigInteger.ZERO);
        }
        return new ExactFloat(sign != other.sign,exponent+other.exponent, significand.multiply(other.significand));
    }

    /**
     * Divides this number by another
     *
     * This method uses simple long division rather than a more sophisticated process to compute a/b. This is mainly
     * because it needs to be completely correct for a specific amount of bytes to be able to round correctly, and
     * because exact divisions such as 1/2 will be completely exact. This is not a guarantee with many other methods.
     *
     * Many implementation use Newton–Raphson division because it is much faster, but the analysis to guarantee
     * correct rounding behavior is beyond me.
     *
     * @param other the dividend
     * @param accuracy the number of bits to compute
     * @return An exact float that equals this/other with the first accuracy bits correct
     * @author Benjamin Landers
     */
    public ExactFloat divide(ExactFloat other, int accuracy){
        assert accuracy > 0 : "Accuracy must be a positive number";
        assert !other.isZero() : "Divide by Zero is not valid";
        ExactFloat a = normalize(), b = other.normalize();
        BigInteger divisor = a.significand, dividend = b.significand;
        BigInteger outbits = BigInteger.ZERO;

        // Line up the numbers
        if(divisor.bitLength() > dividend.bitLength()){
            dividend = dividend.shiftLeft(divisor.bitLength() - dividend.bitLength());
        }else{
            divisor = divisor.shiftLeft(dividend.bitLength() - divisor.bitLength());
        }

        // Perform long division
        while (outbits.bitLength() < accuracy){
            outbits = outbits.shiftLeft(1);
            if(divisor.compareTo(dividend) >= 0){
                divisor = divisor.subtract(dividend);
                outbits = outbits.add(BigInteger.ONE);
            }
            divisor = divisor.shiftLeft(1);
            if(divisor.equals(BigInteger.ZERO)){
                break;
            }
        }
        return new ExactFloat(a.sign != b.sign, a.exponent - b.exponent, outbits);
    }

    public ExactFloat normalize(){
        if(isZero())return new ExactFloat(sign,0,BigInteger.ZERO);
        return new ExactFloat(sign,exponent+significand.getLowestSetBit(),
                significand.shiftRight(significand.getLowestSetBit()));
    }

    public ExactFloat roundToIntegral(Environment env){
        if(isZero())return this;
        ExactFloat f = normalize();
        if(f.exponent >= 0)return f;

        int bitsToRound = -f.exponent;
        BigInteger mainBits = f.significand.shiftRight(bitsToRound).shiftLeft(bitsToRound);
        BigInteger roundedBits = f.significand.subtract(mainBits);

        ExactFloat zeroRounded = new ExactFloat(f.sign,f.exponent,mainBits).normalize();
        ExactFloat oneRounded = zeroRounded.add(new ExactFloat(f.sign,0,BigInteger.valueOf(1)));
        if(env.mode == RoundingMode.zero){
            return zeroRounded;
        }

        if (env.mode == RoundingMode.max || env.mode == RoundingMode.min){
            if((env.mode == RoundingMode.max) == f.sign){
                // if we are rounding towards zero (max & < 0 or min & > 0)
                return zeroRounded;
            }else{
                // Alternatively away from zero (round to zero + 1)
                return oneRounded;
            }
        }

        if(roundedBits.equals(BigInteger.ONE.shiftLeft(bitsToRound-1))){
            // If there is a tie round according to the rounding mode
            if(env.mode == RoundingMode.away || zeroRounded.significand.testBit(0)){
                return oneRounded;
            }else{
                return zeroRounded;
            }
        }else if(roundedBits.compareTo(BigInteger.ONE.shiftLeft(bitsToRound-1)) > 0){ // TODO: check sign
            return oneRounded;
        }else{
            return zeroRounded;
        }
    }

    public int toIntegral(Environment env){
        if(isZero())return 0;

        ExactFloat f = roundToIntegral(env).normalize();

        assert f.exponent >= 0 : "There can't be any fractions at this point";
        if(f.significand.bitLength() + f.exponent > 31){
            return f.sign?Integer.MIN_VALUE:Integer.MAX_VALUE;
        }
        return (sign?-1:1)*f.significand.shiftLeft(f.exponent).intValueExact();
    }

    @Override
    public int compareTo(ExactFloat other) {
        if(isZero()){
            if(other.isZero()){
                return 0;
            }
            return other.sign?1:-1;
        }
        if(sign != other.sign){
            return sign?-1:1;
        }
        if(exponent - other.exponent + significand.bitLength() - other.significand.bitLength() > 0){
            return sign?-1:1;
        }else if(exponent - other.exponent + significand.bitLength() - other.significand.bitLength() < 0){
            return sign?1:-1;
        }else{
            if(exponent > other.exponent){
                return significand.compareTo(other.significand.shiftLeft(exponent-other.exponent))*(sign?-1:1);
            }else{
                return significand.shiftLeft(other.exponent-exponent).compareTo(other.significand)*(sign?-1:1);
            }
        }
    }

    public ExactFloat abs(){
        return new ExactFloat(false, exponent, significand);
    }

    public ExactFloat negate(){
        return new ExactFloat(!sign, exponent, significand);
    }

    @Override
    public boolean isSignMinus() {
        return sign;
    }

    @Override
    public boolean isInfinite() {
        return false;
    }

    @Override
    public boolean isNormal() {
        return true; // TODO: does this make sense
    }

    @Override
    public boolean isSubnormal() {
        return false; // TODO: does this make sense
    }

    @Override
    public boolean isNaN() {
        return false;
    }

    @Override
    public boolean isSignalling() {
        return false;
    }

    @Override
    public boolean isCanonical() {
        return true; // TODO: does this make sense
    }

    @Override
    public boolean isZero() {
        return significand.equals(BigInteger.ZERO);
    }
}

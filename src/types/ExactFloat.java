package types;


import java.math.BigInteger;
import main.Environment;
import main.RoundingMode;
import sun.security.pkcs.SigningCertificateInfo;

/**
 * A helper type to generalize floating point exact operations. This helps to reduce the extra rounding code into just
 * one place. It isn't able to refer to every exact float though. 1/3 is not representable as it requires infinite
 * precision, but any finite significand is representable.
 *
 * Square Root and Division cannot really use this because they cannot avoid the precision issue. They have to stop
 * computing digits once they get past the maximum length of the significand.
 */
public class ExactFloat implements Comparable<ExactFloat>{
    // Value = (-1)^sign * significand * 2^exponent
    private boolean sign;
    private int exponent;
    private BigInteger significand;
    public ExactFloat(Float32 f){
        assert !f.isInfinite() : "Infinity is not exact";
        assert !f.isNaN() : "NaNs are not exact";
        assert !f.isZero(): "Zeros are not exact (-0 vs +0)";
        sign = f.isSignMinus();
        if(f.isZero()){
            exponent = 0;
            significand = BigInteger.ZERO;
        }else if(f.isNormal()){
            exponent = f.exponent()+23;
            significand = BigInteger.valueOf((f.bits & 0x007FFFFF)+0x00800000); // Add back the implied one
        }else if(f.isSubnormal()){
            exponent = f.exponent()+23;
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
        if(significand.equals(BigInteger.ZERO)){
            return (sign)?Float32.NegativeZero:Float32.Zero;
        }
        int normalizedExponent = exponent + significand.bitLength();
        if(normalizedExponent < -127){ // TODO: check off by one
            // Subnormal or underflow to zero
            // TODO: handle
        }else if(normalizedExponent > 128){ // TODO: check off by one
            // Overflow to infinity
            // TODO: handle
        }else {
            // normal
            // TODO: handle
        }
        return Float32.Zero;
    }
    public ExactFloat add(ExactFloat other) {
        int expoDiff = exponent - other.exponent;
        int finalExpo = Math.max(exponent,other.exponent);

        if(sign != other.sign){
            int comp = this.abs().compareTo(other.abs());
            // Section 6.3
            if(comp == 0){
                // Caller should handle what to do in the event a zero pops out
                return new ExactFloat(false, 0, BigInteger.ZERO);
            }

            boolean finalSign = (comp > 0)?sign:other.sign;
            if(expoDiff > 0){
                return new ExactFloat(finalSign, finalExpo, significand.subtract(other.significand.shiftLeft(expoDiff)).abs());
            }else{
                return new ExactFloat(finalSign, finalExpo, significand.shiftLeft(-expoDiff).subtract(other.significand).abs());
            }
        } else {
            if(expoDiff > 0){
                return new ExactFloat(sign, finalExpo, significand.add(other.significand.shiftLeft(expoDiff)).abs());
            }else{
                return new ExactFloat(sign, finalExpo, significand.shiftLeft(-expoDiff).add(other.significand).abs());
            }
        }
    }
    public ExactFloat normalize(){
        if(significand.equals(BigInteger.ZERO))return this;
        return new ExactFloat(sign,exponent+significand.getLowestSetBit(),
                significand.shiftRight(significand.getLowestSetBit()));
    }

    public ExactFloat roundToIntegral(Environment env){
        if(significand.equals(BigInteger.ZERO))return this;
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

    @Override
    public int compareTo(ExactFloat other) {
        if(significand.equals(BigInteger.ZERO)){
            if(other.significand.equals(BigInteger.ZERO)){
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

    public class NotExact extends Exception {}
}

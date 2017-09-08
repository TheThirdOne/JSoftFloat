package types;

import com.sun.org.apache.bcel.internal.generic.FLOAD;

public class Float32 extends Floating{
    public static final Float32 Zero = new Float32(0),
                                NegativeZero = new Float32(0x80000000),
                                NaN = new Float32(0x7F800000);

    public final int bits;
    private Float32(int bits){
        this.bits = bits;
    }

    public int exponent(){
        return ((bits >>> 23) & 0xFF)-127;
    }

    public static Float32 fromSignMagnitude(boolean sign, int mag){
        assert mag > 0 : "Magnitude cannot be less than zero";
        return fromInteger((sign?-1:1)*mag);
    }
    /**
     * @param num An integer to be converted to
     * @return
     */
    public static Float32 fromInteger(int num){
        if(num == 0)return Zero;
        boolean sign = num < 0;
        num = sign ? -num : num;
        int exponent = 0, significand = 0;
        for(int i = 30; i >= 0; i--){
            if(((num >> i) & 1) == 1){
                exponent = i + 127;
                significand = (num << (32 - i))>>>9;
                break;
            }
        }
        int bits = ((sign)?0x80000000:0)|(exponent << 23)|significand;
        return new Float32(bits);
    }



    public boolean isSignMinus(){
        return (bits >>> 31) == 1;
    }
    public boolean isInfinite(){
        return exponent() == 128 && (bits & 0x007FFFFF) == 0;
    }
    public boolean isNormal(){
        return exponent() != -127 && exponent() != 128;
    }
    public boolean isSubnormal(){
        return exponent() == -127 && !isZero();
    }
    public boolean isNaN(){
        return exponent() == 128 && !isInfinite();
    }
    public boolean isSignalling(){
        // TODO: implement
        return false;
    }
    public boolean isCanonical(){
        // TODO: implement
        return true;
    }
    public boolean isZero(){
        return bits == 0 || bits == 0x80000000;
    }
}

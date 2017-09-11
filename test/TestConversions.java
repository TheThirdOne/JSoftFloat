import main.Environment;
import main.Flags;
import main.RoundingMode;
import operations.Conversions;
import org.junit.jupiter.api.Test;
import types.ExactFloat;
import types.Float32;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by benjamin on 9/8/17.
 */
public class TestConversions{
        @Test
        public void Float32fromInteger() {
            assertEquals(0x0, Float32.fromInteger(0).bits);
            assertEquals(0x3f800000, Float32.fromInteger(1).bits);
            assertEquals(0x40000000, Float32.fromInteger(2).bits);
            assertEquals(0xbf800000, Float32.fromInteger(-1).bits);
            assertEquals(0x44000000, Float32.fromInteger(512).bits);
            assertEquals(0x4e5693a4, Float32.fromInteger(900000000).bits);
            // TODO: boundary size test
        }
        @Test
        public void IntegerfromFloat32(){
            assertEquals(0, Conversions.convertToIntegral(Float32.Zero, new Environment()));
            assertEquals(1, Conversions.convertToIntegral(Float32.fromInteger(1), new Environment()));
            assertEquals(-1, Conversions.convertToIntegral(Float32.fromInteger(-1), new Environment()));
        }

        @Test
        void Float32vsExactFloat(){
            assertEquals(0x00000001, new ExactFloat(new Float32(0x00000001)).toFloat32(new Environment()).bits);
            assertEquals(0x00000010, new ExactFloat(new Float32(0x00000010)).toFloat32(new Environment()).bits);
            assertEquals(0x00400000, new ExactFloat(new Float32(0x00400000)).toFloat32(new Environment()).bits);


            Environment e = new Environment();
            assertEquals(0x00000000, new ExactFloat(false, -151, BigInteger.ONE).toFloat32(e).bits);
            assertTrue(e.flags.contains(Flags.inexact));
            assertTrue(e.flags.contains(Flags.underflow));

            ExactFloat small = new ExactFloat(false, -151, BigInteger.valueOf(3));
            assertEquals(0x00000001, small.toFloat32(new Environment(RoundingMode.zero)).bits);
            assertEquals(0x00000001, small.toFloat32(new Environment(RoundingMode.min)).bits);
            assertEquals(0x00000002, small.toFloat32(new Environment(RoundingMode.even)).bits);
            assertEquals(0x00000002, small.toFloat32(new Environment(RoundingMode.max)).bits);
            assertEquals(0x00000002, small.toFloat32(new Environment(RoundingMode.away)).bits);

        }
        @Test
        void RoundToIntegral(){
            assertEquals(Float32.Zero.bits, Conversions.roundToIntegral(Float32.Zero, new Environment()).bits);
            assertEquals(Float32.NegativeZero.bits, Conversions.roundToIntegral(Float32.NegativeZero, new Environment()).bits);

            // 0x3F000000 = 0.5, 0x3F800000 = 1
            Float32 half = new Float32(0x3F000000);
            assertEquals(Float32.Zero.bits,Conversions.roundToIntegral(half, new Environment(RoundingMode.min)).bits);
            assertEquals(Float32.Zero.bits,Conversions.roundToIntegral(half, new Environment(RoundingMode.zero)).bits);
            assertEquals(Float32.Zero.bits,Conversions.roundToIntegral(half, new Environment(RoundingMode.even)).bits);
            assertEquals(0x3F800000,Conversions.roundToIntegral(half, new Environment(RoundingMode.away)).bits);
            assertEquals(0x3F800000,Conversions.roundToIntegral(half, new Environment(RoundingMode.max)).bits);

            half = half.negate();
            assertEquals(0xBF800000,Conversions.roundToIntegral(half, new Environment(RoundingMode.min)).bits);
            assertEquals(Float32.NegativeZero.bits,Conversions.roundToIntegral(half, new Environment(RoundingMode.zero)).bits);
            assertEquals(Float32.NegativeZero.bits,Conversions.roundToIntegral(half, new Environment(RoundingMode.even)).bits);
            assertEquals(0xBF800000,Conversions.roundToIntegral(half, new Environment(RoundingMode.away)).bits);
            assertEquals(Float32.NegativeZero.bits,Conversions.roundToIntegral(half, new Environment(RoundingMode.max)).bits);

            Float32 threeHalfs = new Float32(0x3F400000);
            assertEquals(0x3F800000,Conversions.roundToIntegral(threeHalfs, new Environment(RoundingMode.even)).bits);
            assertEquals(0x3F800000,Conversions.roundToIntegral(threeHalfs, new Environment(RoundingMode.away)).bits);

            Float32 oneFourth = new Float32(0x3EB00000);
            assertEquals(Float32.Zero.bits,Conversions.roundToIntegral(oneFourth, new Environment(RoundingMode.even)).bits);
            assertEquals(Float32.Zero.bits,Conversions.roundToIntegral(oneFourth, new Environment(RoundingMode.away)).bits);
        }
}

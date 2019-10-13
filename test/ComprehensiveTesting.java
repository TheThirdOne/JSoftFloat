import jsoftfloat.Environment;
import jsoftfloat.Flags;
import jsoftfloat.operations.Arithmetic;
import jsoftfloat.operations.ArithmeticF32;
import jsoftfloat.types.Float32;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ComprehensiveTesting {
    @Test
    void IdempotenceOfRounding() {
        Environment env = new Environment();
        for (int i = Integer.MIN_VALUE; i < Integer.MAX_VALUE; i++) {
            Float32 f = new Float32(i);
            if (f.isZero() || f.isNaN() || f.isInfinite()) continue;
            assertEquals(i, Float32.fromExact(f.toExactFloat(), env).bits);
            assertFalse(env.flags.contains(Flags.inexact));
        }
    }

    @Test
    void AdditiveIdentity() {
        for (int i = Integer.MIN_VALUE; i < Integer.MAX_VALUE; i++) {
            Float32 f = new Float32(i);
            if (f.isZero() || f.isNaN()) continue;
            assertEquals(i, Arithmetic.add(f, Float32.Zero, new Environment()).bits);
        }
        for (int i = Integer.MIN_VALUE; i < Integer.MAX_VALUE; i++) {
            Float32 f = new Float32(i);
            if (f.isZero() || f.isNaN()) continue;
            assertEquals(i, Arithmetic.add(Float32.Zero, f, new Environment()).bits);
        }
    }

    @Test
    void compareToFLoatSqrt() {
        for (int i = Integer.MAX_VALUE / 4 * 3; i < Integer.MAX_VALUE; i++) {
            Float32 f = new Float32(i);
            float F = Float.intBitsToFloat(i);
            if (f.isZero() || f.isNaN()) continue;
            float s = (float) Math.sqrt(F);

            int rawBits = Float.floatToRawIntBits(s);
            Float32 out = Arithmetic.squareRoot(f, new Environment());
            assertEquals(out.bits, rawBits);
        }
    }

    @Test
    void compareToFloatAdd() {
        for (int j = 0; j < 64; j++) {
            int aBits = ThreadLocalRandom.current().nextInt();
            Float32 a = new Float32(aBits);
            Float aF = Float.intBitsToFloat(aBits);
            if (a.isNaN()) continue;
            for (int i = 0; i < Integer.MAX_VALUE / 64; i++) {
                int bBits = ThreadLocalRandom.current().nextInt();
                Float32 b = new Float32(bBits);
                if (b.isNaN()) continue;
                Environment e = new Environment();
                Float32 c = ArithmeticF32.add(a, b, e);
                Float bF = Float.intBitsToFloat(bBits);
                Float cF = aF + bF;
                int rawBits = Float.floatToRawIntBits(cF);
                assertEquals(c.bits, rawBits);
            }
        }
    }

    @Test
    void compareToFloatMult() {
        for (int j = 0; j < 64; j++) {
            int aBits = ThreadLocalRandom.current().nextInt();
            Float32 a = new Float32(aBits);
            Float aF = Float.intBitsToFloat(aBits);
            if (a.isNaN()) continue;
            for (int i = 0; i < Integer.MAX_VALUE / 64; i++) {
                int bBits = ThreadLocalRandom.current().nextInt();
                Float32 b = new Float32(bBits);
                if (b.isNaN()) continue;
                Environment e = new Environment();
                Float32 c = Arithmetic.multiplication(a, b, e);
                Float bF = Float.intBitsToFloat(bBits);
                Float cF = aF * bF;
                int rawBits = Float.floatToRawIntBits(cF);
                assertEquals(c.bits, rawBits);
            }
        }
    }

    @Test
    void CommutativeityOfAdd() {
        for (int j = 0; j < 64; j++) {
            int aBits = ThreadLocalRandom.current().nextInt();
            Float32 a = new Float32(aBits);
            if (a.isNaN()) continue;
            for (int i = 0; i < Integer.MAX_VALUE / 256; i++) {
                int bBits = ThreadLocalRandom.current().nextInt();
                Float32 b = new Float32(bBits);
                if (b.isNaN()) continue;
                Environment e = new Environment();
                Float32 c = Arithmetic.add(a, b, e);
                Float32 c2 = Arithmetic.add(b, a, e);
                assertEquals(c.bits, c2.bits);
            }
        }
    }

    @Test
    void CommutativeityOfMult() {
        for (int j = 0; j < 64; j++) {
            int aBits = ThreadLocalRandom.current().nextInt();
            Float32 a = new Float32(aBits);
            if (a.isNaN()) continue;
            for (int i = 0; i < Integer.MAX_VALUE / 256; i++) {
                int bBits = ThreadLocalRandom.current().nextInt();
                Float32 b = new Float32(bBits);
                if (b.isNaN()) continue;
                Environment e = new Environment();
                Float32 c = Arithmetic.multiplication(a, b, e);
                Float32 c2 = Arithmetic.multiplication(b, a, e);
                assertEquals(c.bits, c2.bits);
            }
        }
    }
}

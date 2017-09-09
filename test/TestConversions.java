import main.Environment;
import operations.Conversions;
import org.junit.jupiter.api.Test;
import types.Float32;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}

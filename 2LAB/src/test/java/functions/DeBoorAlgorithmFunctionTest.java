package functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeBoorAlgorithmFunctionTest {

    @Test
    void testLinearFun(){

        double[][] cp = {{0.0, 0.0}, {1.0, 1.0}};
        double[] kn = {0.0, 0.0, 1.0, 1.0};
        DeBoorAlgorithmFunction fun = new DeBoorAlgorithmFunction(cp, kn, 1);

        assertEquals(0.0, fun.apply(0));
        assertEquals(0.5, fun.apply(0.5));
        assertEquals(1, fun.apply(1));

    }
}
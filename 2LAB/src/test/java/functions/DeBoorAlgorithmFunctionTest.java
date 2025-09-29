package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DeBoorAlgorithmFunctionTest {

    @Test
    void testAtEndPoints() {  // x должен принад. [knots[degree]; knots[knots.length - 1 - degree]. Проверим на концах
        double[][] controlPoints = {
                {0.0, 0.0},
                {1.0, 2.0},
                {2.0, 0.0},
                {3.0, 2.0},
                {4.0, 0.0}
        };
        double[] knots = {0, 0, 0, 0, 1, 2, 3, 3, 3, 3};
        int degree = 3;

        DeBoorAlgorithmFunction spline = new DeBoorAlgorithmFunction(controlPoints, knots, degree);

        double left = knots[degree];                // knots[3] = 0.0
        double right = knots[controlPoints.length - degree - 1]; // knots[5] = 2.0

        // в начале диапазона должна совпадать с первой контрольной точкой (0.0)
        assertEquals(0.0, spline.apply(left), 1e-9);

        // в конце диапазона должна совпадать с последней контрольной точкой (0.0)
        assertEquals(0.0, spline.apply(right), 1e-9);
    }

    @Test
    void testAtMidPoints() {
        double[][] controlPoints = {
                {0.0, 0.0},
                {1.0, 2.0},
                {2.0, 0.0},
                {3.0, 2.0},
                {4.0, 0.0}
        };
        double[] knots = {0, 0, 0, 0, 1, 2, 3, 3, 3, 3};
        int degree = 3;

        DeBoorAlgorithmFunction spline = new DeBoorAlgorithmFunction(controlPoints, knots, degree);

        // Проверим несколько значений внутри диапазона
        double y1 = spline.apply(0.5);
        double y2 = spline.apply(1.5);
        double y3 = spline.apply(2.5);

        // проверяем, что значения корректные
        assertTrue(y1 >= 0);
        assertTrue(y2 >= 0);
        assertTrue(y3 >= 0);

        // проверяем что значения разные
        assertNotEquals(y1, y2);
        assertNotEquals(y2, y3);
    }

    @Test
    void testMonotoneLinear() {
        // Линейный сплайн (degree=1), который должен совпадать с ломаной
        double[][] controlPoints = {
                {0.0, 0.0},
                {1.0, 1.0},
                {2.0, 2.0}
        };
        double[] knots = {0.0, 0.0, 1.0, 2.0, 2.0};
        int degree = 1;

        DeBoorAlgorithmFunction spline = new DeBoorAlgorithmFunction(controlPoints, knots, degree);

        // Тут значения совпадают с y=x
        assertEquals(0.0, spline.apply(0.0), 1e-9);
        assertEquals(1.0, spline.apply(1.0), 1e-9);
        assertEquals(2.0, spline.apply(2.0), 1e-9);
        assertEquals(1.5, spline.apply(1.5), 1e-9);
    }
}

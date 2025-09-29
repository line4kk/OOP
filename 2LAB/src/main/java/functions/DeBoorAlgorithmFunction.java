package functions;

public class DeBoorAlgorithmFunction implements MathFunction {
    private final double[][] controlPoints;  // Контрольные точки
    private final double[] knots;  // Узлы
    private final int degree;  // Степень (p)

    public DeBoorAlgorithmFunction(double[][] controlPoints, double[] knots, int degree) {
        // ДОЛЖНО ВЫПОЛНЯТЬСЯ knots.length = controlPoints.length + degree + 1.
        // ИНАЧЕ АЛГОРИТМ НЕ РАБОТАЕТ
        this.controlPoints = controlPoints;
        this.knots = knots;
        this.degree = degree;
    }

    private int findSegment(double x) {  // Находим номер отрезка k, такой, что x принад. [t[k]; t[k+1])
        int k = -1;
        int n = controlPoints.length - 1;

        for (int i = degree; i <= n; i++) {
            if (x >= knots[i] && x < knots[i + 1]) {
                k = i;
                break;
            }
        }
        if (k == -1)
            k = n;

        return k;

    }

    private double alpha(double x, int k, int j, int r) {
        double denom = knots[j + 1 + k - r] - knots[j + k - degree];
        if (denom == 0.0) {
            return 0.0; // по определению
        }
        return (x - knots[j + k - degree]) / denom;
    }

    @Override
    public double apply(double x) {
        double[] resultPoints = new double[controlPoints.length];
        int k = findSegment(x);
        for (int j = 0; j <= degree; j++)
            resultPoints[j] = controlPoints[j + k - degree][1];

        for (int r = 1; r <= degree; r++) {
            for (int j = degree; j >= r; j--) {
                resultPoints[j] = (1 - alpha(x, k, j, r)) * resultPoints[j - 1] + alpha(x, k, j, r) * resultPoints[j];
            }
        }
        return resultPoints[degree];

    }

}

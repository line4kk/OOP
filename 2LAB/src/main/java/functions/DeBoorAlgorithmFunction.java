package functions;

public class DeBoorAlgorithmFunction implements MathFunction {
    private final double[][] controlPoints;  // Контрольные точки
    private final double[] knots;  // Узлы
    private final int degree;  // Степень (p)

    public DeBoorAlgorithmFunction(double[][] controlPoints, double[] knots, int degree) {
        this.controlPoints = controlPoints;
        this.knots = knots;
        this.degree = degree;
    }

    private int findSegment(double x) {  // Находим номер отрезка k, такой, что x принад. [t[k]; t[k+1])
        /*if (x == knots[knots.length - 1])
            return knots.length - 2;*/
        int k = 0;

        while ((k < knots.length - 1) && !(x >= knots[k] && x < knots[k + 1]) && !(knots[k] != knots[k+1]))
            k++;

        return k;
    }

    private double alpha(double x, int k, int j) {
        return (x - knots[j + k - degree]) / (knots[j + 1 + k - degree] - knots[j + k - degree]);
    }

    public double apply(double x) {
        double[] resultPoints = new double[controlPoints.length];
        int k = findSegment(x);
        for (int j = 0; j <= degree; j++)
            resultPoints[j] = controlPoints[j + k - degree][1];

        for (int r = 1; r <= degree; r++) {
            for (int j = degree; j >= r; j--) {
                resultPoints[j] = (1 - alpha(x, k, j)) * resultPoints[j - 1] + alpha(x, k, j) * resultPoints[j];
            }
        }
        return resultPoints[degree];

    }

}

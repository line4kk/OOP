package functions;

// Метод простых итераций
public class SimpleIterationMethodFunction implements MathFunction {
    private final MathFunction function;
    private final MathFunction d_function;
    private final double accuracy;
    private final int maxIterations;

    // Конструктор, в котором определяется функция, производная функции, точность и максимальное количество итераций
    public SimpleIterationMethodFunction(MathFunction function, MathFunction d_function, double accuracy, int maxIterations) {
        this.function = function; // функция x = g(x)
        this.d_function = d_function; // производная функции g'(x)
        this.accuracy = accuracy; // точность
        this.maxIterations = maxIterations; // максимальное количество итераций
    }

    @Override
    public double apply(double assumption) {

        double currentX = assumption;
        int iterationCount = 0;

        // реализация метода простых итераций
        while (iterationCount < maxIterations) {

            // Проверка на соответствие условию |g'(x) < 1|
            double convergenceAtPoint = Math.abs(d_function.apply(currentX));
            if (convergenceAtPoint >= 1.0) {
                throw new ArithmeticException("Модуль производной больше 1, метод простых итераций не сработает");
            }

            double nextX = function.apply(currentX);
            iterationCount++;

            if (Double.isNaN(nextX) || Double.isInfinite(nextX)) {
                throw new ArithmeticException("Во время итерации x принимает невозможные значения");
            }


            // Проверка на соответствие точности
            if (Math.abs(nextX - currentX) < accuracy) {
                return nextX;
            }
            currentX = nextX;
        }
        return currentX;
    }
}

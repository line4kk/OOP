package functions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Метод простых итераций
public class SimpleIterationMethodFunction implements MathFunction {
    private static final Logger logger = LoggerFactory.getLogger(SimpleIterationMethodFunction.class);
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
        logger.debug("Создан метод простых итераций: точность={}, макс. итераций={}", accuracy, maxIterations);
    }

    @Override
    public double apply(double assumption) {
        logger.debug("Запуск метода простых итераций с начальным приближением: {}", assumption);

        double currentX = assumption;
        int iterationCount = 0;

        // реализация метода простых итераций
        while (iterationCount < maxIterations) {

            // Проверка на соответствие условию |g'(x) < 1|
            double convergenceAtPoint = Math.abs(d_function.apply(currentX));
            if (convergenceAtPoint >= 1.0) {
                logger.error("Модуль производной {} >= 1, метод расходится", convergenceAtPoint);
                throw new ArithmeticException("Модуль производной больше 1, метод простых итераций не сработает");
            }

            double nextX = function.apply(currentX);
            iterationCount++;

            if (Double.isNaN(nextX) || Double.isInfinite(nextX)) {
                logger.error("Недопустимое значение x на итерации {}: {}", iterationCount, nextX);
                throw new ArithmeticException("Во время итерации x принимает невозможные значения");
            }


            // Проверка на соответствие точности
            if (Math.abs(nextX - currentX) < accuracy) {
                logger.info("Метод сошелся за {} итераций, результат: {}", iterationCount, nextX);
                return nextX;
            }
            currentX = nextX;
        }
        logger.warn("Достигнут лимит {} итераций, возвращаем последнее значение: {}", maxIterations, currentX);
        return currentX;
    }
}

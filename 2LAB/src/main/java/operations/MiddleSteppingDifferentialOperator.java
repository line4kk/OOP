package operations;

import functions.MathFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiddleSteppingDifferentialOperator extends SteppingDifferentialOperator {

    private static final Logger logger = LoggerFactory.getLogger(MiddleSteppingDifferentialOperator.class);
    public MiddleSteppingDifferentialOperator(double step) {
        super(step);
        logger.debug("Создан центральный разностный оператор с шагом: {}", step);
    }

    @Override
    public MathFunction derive(MathFunction function) {
        if (function == null) {
            logger.error("Попытка вычислить производную от null функции");
            throw new IllegalArgumentException("Функция не может быть пустой");
        }
        logger.debug("Вычисление центральной производной для функции {}", function.getClass().getSimpleName());
        return new MathFunction() {
            @Override
            public double apply(double x) {
                return (function.apply(x + step) - function.apply(x - step)) / (2 * step);
            }
        };
    }
}

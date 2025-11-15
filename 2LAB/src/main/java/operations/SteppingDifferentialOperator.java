package operations;

import functions.MathFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SteppingDifferentialOperator implements DifferentialOperator<MathFunction>{
    private static final Logger logger = LoggerFactory.getLogger(SteppingDifferentialOperator.class);
    protected double step;

    public SteppingDifferentialOperator(double step) {
        if (step <= 0 || Double.isInfinite(step) || Double.isNaN(step)) {
            logger.error("Попытка создать оператор с недопустимым шагом: {}", step);
            throw new IllegalArgumentException("Шаг должен быть целым положительным числом");
        }
        this.step = step;
        logger.debug("Создан дифференциальный оператор с шагом: {}", step);
    }

    public double getStep() {
        return step;
    }

    public void setStep(double step) {
        if (step <= 0 || Double.isInfinite(step) || Double.isNaN(step)) {
            logger.error("Попытка установить недопустимый шаг: {}", step);
            throw new IllegalArgumentException("Шаг должен быть целым положительным числом");
        }
        logger.debug("Изменение шага: {} -> {}", this.step, step);
        this.step = step;
    }
}

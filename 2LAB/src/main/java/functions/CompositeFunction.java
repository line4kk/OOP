package functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompositeFunction implements MathFunction {
    private static final Logger logger = LoggerFactory.getLogger(CompositeFunction.class);
    private final MathFunction internalFunction, externalFunction;  // Внутренняя(первая) и внешняя(вторая) функции

    public CompositeFunction(MathFunction internalFunction, MathFunction externalFunction){
        this.internalFunction = internalFunction;
        this.externalFunction = externalFunction;
    }

    public double apply(double x){
        logger.debug("Вычисление композитной функции для x = {}", x);

        double internalResult = internalFunction.apply(x);
        logger.debug("Результат внутренней функции: {} -> {}", x, internalResult);

        double result = externalFunction.apply(internalResult);
        logger.debug("Результат внешней функции: {} -> {}", internalResult, result);

        return result;
    }

}

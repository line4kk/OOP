package functions;

public class CompositeFunction implements MathFunction {
    private MathFunction internalFunction, externalFunction;  // Внутренняя и внешняя функции

    public CompositeFunction(MathFunction internalFunction, MathFunction externalFunction){
        this.internalFunction = internalFunction;
        this.externalFunction = externalFunction;
    }

    public double apply(double x){
        return externalFunction.apply(internalFunction.apply(x));
    }

}

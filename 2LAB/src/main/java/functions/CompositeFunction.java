package functions;

public class CompositeFunction implements MathFunction {
    private MathFunction internalFunction, externalFunction;  // Внутренняя(первая) и внешняя(вторая) функции

    public CompositeFunction(MathFunction internalFunction, MathFunction externalFunction){
        this.internalFunction = internalFunction;
        this.externalFunction = externalFunction;
    }

    public double apply(double x){
        return externalFunction.apply(internalFunction.apply(x));
    }

}

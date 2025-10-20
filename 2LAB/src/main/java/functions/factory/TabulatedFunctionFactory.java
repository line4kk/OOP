package functions.factory;

import functions.StrictTabulatedFunction;
import functions.TabulatedFunction;
import functions.UnmodifiableTabulatedFunction;

public interface TabulatedFunctionFactory {
    TabulatedFunction create(double[] xValues, double[] yValues);

    default TabulatedFunction createStrict(double[] xValues, double[] yValues) {
        TabulatedFunction function = create(xValues, yValues);
        return new StrictTabulatedFunction(function);
    }

    default TabulatedFunction createUnmodifiable(double[] xValues, double[] yValues) {
        TabulatedFunction function = create(xValues, yValues);
        return new UnmodifiableTabulatedFunction(function);
    }

    default TabulatedFunction createUnmodifiableStrict(double[] xValues, double[] yValues) {
        TabulatedFunction function = create(xValues, yValues);
        TabulatedFunction unmodif = new UnmodifiableTabulatedFunction(function);
        return new StrictTabulatedFunction(unmodif);
    }
}

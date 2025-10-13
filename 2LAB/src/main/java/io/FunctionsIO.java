package io;

import functions.Point;
import functions.TabulatedFunction;
import java.io.*;

public final class FunctionsIO {
    private FunctionsIO() {
        throw new UnsupportedOperationException("Нельзя создать экземпляр служебного класса");
    }

    public static void writeTabulatedFunction(BufferedOutputStream outputStream, TabulatedFunction function) throws IOException {
        try (DataOutputStream stream = new DataOutputStream(outputStream)) {
            Point[] points = TabulatedFunctionOperationService.asPoints(function);
            stream.writeInt(function.getCount());
            for (Point point : points) {
                stream.writeDouble(point.x);
                stream.writeDouble(point.y);
            }
            stream.flush();
        }
    public static void writeTabulatedFunction(BufferedWriter writer, TabulatedFunction function) throws IOException {
        PrintWriter printWriter = new PrintWriter(writer);

        printWriter.println(function.getCount());

        for (Point point : function) {
            printWriter.printf("%f %f\n", point.x, point.y);
        }

        printWriter.flush();
    }
}

package io;

import functions.Point;
import functions.TabulatedFunction;
import functions.factory.TabulatedFunctionFactory;

import java.io.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public final class FunctionsIO {
    private FunctionsIO() {
        throw new UnsupportedOperationException("Нельзя создать экземпляр служебного класса");
    }

    public static void writeTabulatedFunction(BufferedWriter writer, TabulatedFunction function) throws IOException {
        PrintWriter printWriter = new PrintWriter(writer);

        printWriter.println(function.getCount());

        for (Point point : function) {
            printWriter.printf("%f %f\n", point.x, point.y);
        }

        printWriter.flush();
    }

    public static TabulatedFunction readTabulatedFunction(BufferedReader reader, TabulatedFunctionFactory factory)
            throws IOException {

        try {
            String countLine = reader.readLine();
            if (countLine == null) {
                throw new IOException("Файл пустой");
            }

            int count = Integer.parseInt(countLine.trim());


            double[] xValues = new double[count];
            double[] yValues = new double[count];

            NumberFormat numberFormat = NumberFormat.getInstance(Locale.forLanguageTag("ru"));

            for (int i = 0; i < count; i++) {
                String line = reader.readLine();
                if (line == null) {
                    throw new IOException("Неожиданный конец в файле");
                }

                String[] parts = line.split(" ");
                if (parts.length != 2) {
                    throw new IOException("Неправильные данные в строке: " + line);
                }

                try {
                    xValues[i] = numberFormat.parse(parts[0].trim()).doubleValue();
                    yValues[i] = numberFormat.parse(parts[1].trim()).doubleValue();
                } catch (ParseException e) {
                    throw new IOException("Ошибка парсинга чисел в строке: " + line, e);
                }
            }

            return factory.create(xValues, yValues);
        } catch(NumberFormatException e) {
            throw new IOException("Некорректный формат числа", e);
        }
    }
}

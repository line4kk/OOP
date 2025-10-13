package io;

import functions.*;
import java.io.*;

public class TabulatedFunctionFileWriter {

    public static void main(String[] args) {
        try (
                BufferedWriter arrayWriter = new BufferedWriter(
                        new FileWriter("output/array_function.txt")
                );
                BufferedWriter listWriter = new BufferedWriter(
                        new FileWriter("output/linked_list_function.txt")
                )
        ) {

            double[] xValues = {22.0, 44.0, 66.0, 88.0};
            double[] yValues = {9.0, 99.0, 999.0, 9999.0};
            // Табулированная функция на основе массива
            TabulatedFunction arrayFunction = new ArrayTabulatedFunction(xValues, yValues);

            // Табулированная функция на основе массива
            TabulatedFunction linkedListFunction = new LinkedListTabulatedFunction(xValues, yValues);

            // Записываем в файлы
            FunctionsIO.writeTabulatedFunction(arrayWriter, arrayFunction);
            FunctionsIO.writeTabulatedFunction(listWriter, linkedListFunction);

        } catch (IOException error) {
            error.printStackTrace(System.err);
        }
    }
}

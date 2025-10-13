package io;

import functions.TabulatedFunction;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.LinkedListTabulatedFunctionFactory;
import functions.factory.TabulatedFunctionFactory;
import operations.TabulatedDifferentialOperator;

import java.io.*;

public class TabulatedFunctionFileInputStream {
    static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream("input/binary function.bin"); BufferedInputStream bis = new BufferedInputStream(fis)) {
            TabulatedFunctionFactory arrayFactory = new ArrayTabulatedFunctionFactory();

            TabulatedFunction functionFromFile = FunctionsIO.readTabulatedFunction(bis, arrayFactory);

            System.out.println("Функция из файла:");
            System.out.println(functionFromFile.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Введите размер и значения функции");

        try {
            // Используем BufferedReader для текстового ввода из консоли
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            TabulatedFunctionFactory listFactory = new LinkedListTabulatedFunctionFactory();

            TabulatedFunction functionFromConsole = FunctionsIO.readTabulatedFunction(reader, listFactory);

            TabulatedDifferentialOperator differentialOperator = new TabulatedDifferentialOperator();
            TabulatedFunction derivative = differentialOperator.derive(functionFromConsole);

            System.out.println("Производная функции:");
            System.out.println(derivative.toString());
        }

        catch (IOException e) {
            e.printStackTrace();
        }


    }
}

package io;

import functions.ArrayTabulatedFunction;
import functions.LinkedListTabulatedFunction;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class TabulatedFunctionFileOutputStream {
    void main(String[] args) {
        try (FileOutputStream fileOutputStreamArray = new FileOutputStream("output/array function.bin");
             FileOutputStream fileOutputStreamList = new FileOutputStream("output/linked list function.bin");
             BufferedOutputStream bufferedStreamArray = new BufferedOutputStream(fileOutputStreamArray);
             BufferedOutputStream bufferedStreamList = new BufferedOutputStream(fileOutputStreamList);)
        {
            ArrayTabulatedFunction arrayTabulatedFunction = new ArrayTabulatedFunction(new double[] {1.0, 2.0, 3.0, 4.0}, new double[] {2.0, 4.0, 6.0, 8.0});
            LinkedListTabulatedFunction linkedListTabulatedFunction = new LinkedListTabulatedFunction(new double[] {1.0, 2.0, 3.0, 4.0}, new double[] {0.5, 1.0, 1.5, 2.0});

            // Записываем функции в соответствующие потоки.
            FunctionsIO.writeTabulatedFunction(bufferedStreamArray, arrayTabulatedFunction);
            FunctionsIO.writeTabulatedFunction(bufferedStreamList, linkedListTabulatedFunction);
        }

        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

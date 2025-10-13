package io;

import functions.TabulatedFunction;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.LinkedListTabulatedFunctionFactory;
import java.io.*;

public class TabulatedFunctionFileReader {
    public static void main(String[] args) {

        try (
                BufferedReader reader1 = new BufferedReader(new FileReader("2LAB/input/function.txt"));
                BufferedReader reader2 = new BufferedReader(new FileReader("2LAB/input/function.txt"))
        ) {
            TabulatedFunction arrayFunction = FunctionsIO.readTabulatedFunction(
                    reader1, new ArrayTabulatedFunctionFactory()
            );
            TabulatedFunction linkedListFunction = FunctionsIO.readTabulatedFunction(
                    reader2, new LinkedListTabulatedFunctionFactory()
            );

            System.out.println("ArrayTabulatedFunction:");
            System.out.println(arrayFunction.toString());

            System.out.println("\nLinkedListTabulatedFunction:");
            System.out.println(linkedListFunction.toString());

        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }
}

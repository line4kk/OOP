package io;

import functions.TabulatedFunction;
import functions.ArrayTabulatedFunction;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.TabulatedFunctionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.*;
import java.util.Comparator;

public class FunctionsIOTest {

    @TempDir
    static Path tempDir;

    @BeforeAll
    static void setUp() {
        System.out.println("Тестирование в директории: " + tempDir.toString());
    }

    @AfterAll
    static void tearDown() throws IOException {
        if (Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        System.out.println("Тестирование завершено, директория очищена");
    }
    @Test
    public void testAllMethods() throws IOException, ClassNotFoundException {
        double[] xValues = {1.0, 2.0};
        double[] yValues = {3.0, 4.0};
        TabulatedFunction original = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();

        // 1. Тест writeTabulatedFunction (BufferedWriter)
        StringWriter stringWriter = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);
        FunctionsIO.writeTabulatedFunction(bufferedWriter, original);

        String textResult = stringWriter.toString();
        String[] textLines = textResult.split("\n");

        assertEquals("2", textLines[0].trim());
        assertEquals("1,000000 3,000000", textLines[1].trim());
        assertEquals("2,000000 4,000000", textLines[2].trim());

        // 2. Тест writeTabulatedFunction (BufferedOutputStream)
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        BufferedOutputStream bufferedOut = new BufferedOutputStream(byteOut);
        FunctionsIO.writeTabulatedFunction(bufferedOut, original);

        ByteArrayInputStream binaryIn = new ByteArrayInputStream(byteOut.toByteArray());
        DataInputStream dataIn = new DataInputStream(binaryIn);

        int binaryCount = dataIn.readInt();
        assertEquals(2, binaryCount);

        assertEquals(1.0, dataIn.readDouble(), 1e-10);
        assertEquals(3.0, dataIn.readDouble(), 1e-10);
        assertEquals(2.0, dataIn.readDouble(), 1e-10);
        assertEquals(4.0, dataIn.readDouble(), 1e-10);

        // 3. Тест readTabulatedFunction (BufferedReader)
        String input = "2\n1,0 3,0\n2,0 4,0\n";
        StringReader stringReader = new StringReader(input);
        BufferedReader bufferedReader = new BufferedReader(stringReader);
        TabulatedFunction readFunc = FunctionsIO.readTabulatedFunction(bufferedReader, factory);

        assertEquals(2, readFunc.getCount());
        assertEquals(1.0, readFunc.getX(0), 1e-10);
        assertEquals(3.0, readFunc.getY(0), 1e-10);
        assertEquals(2.0, readFunc.getX(1), 1e-10);
        assertEquals(4.0, readFunc.getY(1), 1e-10);

        // 4. Тест readTabulatedFunction (BufferedInputStream)
        ByteArrayOutputStream dataOut = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(dataOut);
        dataStream.writeInt(2);
        dataStream.writeDouble(1.0);
        dataStream.writeDouble(3.0);
        dataStream.writeDouble(2.0);
        dataStream.writeDouble(4.0);

        ByteArrayInputStream dataInStream = new ByteArrayInputStream(dataOut.toByteArray());
        BufferedInputStream bufferedIn = new BufferedInputStream(dataInStream);
        TabulatedFunction readBinaryFunc = FunctionsIO.readTabulatedFunction(bufferedIn, factory);

        assertEquals(2, readBinaryFunc.getCount());
        assertEquals(1.0, readBinaryFunc.getX(0), 1e-10);
        assertEquals(3.0, readBinaryFunc.getY(0), 1e-10);
        assertEquals(2.0, readBinaryFunc.getX(1), 1e-10);
        assertEquals(4.0, readBinaryFunc.getY(1), 1e-10);

        // 5. Тест serialize и deserialize
        ByteArrayOutputStream serOut = new ByteArrayOutputStream();
        BufferedOutputStream serBuffered = new BufferedOutputStream(serOut);
        FunctionsIO.serialize(serBuffered, original);

        ByteArrayInputStream deserIn = new ByteArrayInputStream(serOut.toByteArray());
        BufferedInputStream deserBuffered = new BufferedInputStream(deserIn);
        TabulatedFunction deserialized = FunctionsIO.deserialize(deserBuffered);

        assertEquals(2, deserialized.getCount());
        assertEquals(1.0, deserialized.getX(0), 1e-10);
        assertEquals(3.0, deserialized.getY(0), 1e-10);
        assertEquals(2.0, deserialized.getX(1), 1e-10);
        assertEquals(4.0, deserialized.getY(1), 1e-10);
    }
    @Test
    public void testPrivateConstructor() throws Exception {
        Constructor<FunctionsIO> constructor = FunctionsIO.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThrows(InvocationTargetException.class, () -> constructor.newInstance());
    }
}
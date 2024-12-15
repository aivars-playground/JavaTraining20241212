package advanced_features.part9_trywith;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Part9Test {

    public static void copyNonEmptyLines(String fileIn, String fileOut) throws IOException {

        try(
            BufferedReader br = new BufferedReader(new FileReader(fileIn));
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileOut))
        ) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isBlank()) {
                    bw.write(line);
                    bw.newLine();
                }
            }
        }

    }

    @Test
    void test_multi_resource() {
        try {
            copyNonEmptyLines("abc","cde");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    void test_autoclosable() {
        TheResource test1 = new TheResource(1); //as long as constructor does not fail !!!
        TheResource test2 = new TheResource(2);
        try( test1; test2) {
            test1.tryTask();
            test2.tryTask();
        } catch (Exception e) {
            System.out.println("testing main Error: " + e.getMessage());

            System.out.println("!!! Suppressed Errors !!!"+ List.of(e.getSuppressed()));
        }
    }

}

class TheResource implements AutoCloseable {

    private final int id;

    TheResource(int i) {
        id = i;
        System.out.println("init TheResource:" + id);
    }

    @Override
    public void close() throws Exception {
        System.out.println("about to fail closing resource:" + id);
        throw new Exception("cannot close resource:" + id );
    }

    public void tryTask() {
        System.out.println("about to fail task:" + id);
        throw new NullPointerException("this is a test:" + id);
    }
}

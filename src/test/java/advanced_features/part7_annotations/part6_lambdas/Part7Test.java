package advanced_features.part7_annotations.part6_lambdas;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Part7Test {

    @Test
    void test_annotations() {

        Set<Class> classNames = findAllClassesUsingClassLoader("advanced_features.part7_annotations.part6_lambdas");
        for (Class<?> clazz : classNames) {
            System.out.println(clazz.getSimpleName());
            Commands annotation = clazz.getAnnotation(Commands.class);
            if (annotation != null) {
                System.out.println("   --->" +annotation);
            }
        }
    }


    @Command(value = "login", description = "describing" , order = 100)
    @Command(value = "login_repeated", description = "describing" , order = 2)
    class LoginCommand{
    }

    @Target(ElementType.TYPE)   //meta annotation  - has Target(ElementType.ANNOTATION_TYPE)
    @Retention(RetentionPolicy.RUNTIME) //source - mostly for tools, class - i.e analyze classes, runtime- - for comptler
    @Documented                         //javadoc
    @Inherited                          //applies to subclass
    @Repeatable(Commands.class)       //need container, allows repeat same type
    @interface Command {
        String value();
        String description();
        int order();
        String optionalDescription() default "no description";
    }

    @Target(ElementType.TYPE)   //meta annotation  - has Target(ElementType.ANNOTATION_TYPE)
    @Retention(RetentionPolicy.RUNTIME) //source - mostly for tools, class - i.e analyze classes, runtime- - for comptler
    @Documented                         //javadoc
    @Inherited
    @interface Commands {
        Command[] value();
    }




    public Set<Class> findAllClassesUsingClassLoader(String packageName) {
        InputStream stream = ClassLoader.getSystemClassLoader()
                .getResourceAsStream(packageName.replaceAll("[.]", "/"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        return reader.lines()
                .filter(line -> line.endsWith(".class"))
                .map(line -> getClass(line, packageName))
                .collect(Collectors.toSet());
    }

    private Class<?> getClass(String className, String packageName) {
        try {
            return Class.forName(packageName + "."
                    + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException e) {
            // handle the exception
        }
        return null;
    }
}







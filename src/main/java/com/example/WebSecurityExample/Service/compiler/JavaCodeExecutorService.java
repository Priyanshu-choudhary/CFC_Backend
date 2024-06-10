package com.example.WebSecurityExample.Service.compiler;

import org.springframework.stereotype.Service;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

@Service
public class JavaCodeExecutorService {

    public Object executeJavaCode(String javaCode) throws Exception {
        // Define the file name and path
        String fileName = "DynamicClass";
        String filePath = System.getProperty("java.io.tmpdir") + File.separator + fileName + ".java";

        // Write the Java code to a file
        writeJavaCodeToFile(filePath, javaCode);

        // Compile the Java code
        compileJavaCode(filePath);

        // Load the compiled class
        Class<?> compiledClass = loadCompiledClass(fileName);

        // Execute the compiled class's main method
        return executeCompiledClass(compiledClass);
    }

    private void writeJavaCodeToFile(String filePath, String javaCode) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(javaCode);
        }
    }

    private void compileJavaCode(String filePath) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("Cannot find the system Java compiler. Check that your class path includes tools.jar");
        }

        int result = compiler.run(null, null, null, filePath);
        if (result != 0) {
            throw new IllegalStateException("Compilation failed. Result code: " + result);
        }
    }

    private Class<?> loadCompiledClass(String className) throws Exception {
        URL classUrl = new File(System.getProperty("java.io.tmpdir")).toURI().toURL();
        URLClassLoader classLoader = new URLClassLoader(new URL[]{classUrl}, this.getClass().getClassLoader());
        return Class.forName(className, true, classLoader);
    }

    private Object executeCompiledClass(Class<?> compiledClass) throws Exception {
        Method mainMethod = compiledClass.getDeclaredMethod("main", String[].class);
        if (mainMethod != null) {
            String[] args = new String[]{};
            return mainMethod.invoke(null, (Object) args);
        } else {
            throw new IllegalStateException("No main method found in the compiled class");
        }
    }
}

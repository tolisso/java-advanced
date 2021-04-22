package info.kgeorgiy.ja.malko.implementor;

import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * Class for creating default implementation for classes and interfaces.
 */
public class Implementor implements JarImpler {

    /**
     *  Separator between lines in generated implementations
     */
    private static final String LINE_SEPARATOR = System.lineSeparator();

    /**
     * Space separator in generated implementations
     */
    private static final String SPACE = " ";

    /**
     * Tabulation in generated implementations
     */
    private static final String TAB = "\t";

    /**
     * Constructor of {@link Implementor} which can implement as many tokens as you want
     */
    public Implementor() {
    }

    /**
     * Converts {@code str} from {@code Unicode} to {@code Ascii}
     *
     * @param str what convert
     * @return converted {@link String}
     */
    static String getUnicode(String str) {
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (c < 128) {
                sb.append(c);
            } else {
                sb.append(String.format("\\u%04X", (int) c));
            }
        }
        return sb.toString();
    }

    /**
     * Make directories in the way to parent of {@code path}. If {@code path} hasn't got parent do
     * nothing.
     *
     * @param path to which parent directories are creating
     * @throws ImplementorException if {@link #makeDirs(Path)} throws {@link ImplementorException}
     */
    private void makeParentDirs(Path path) throws ImplementorException {
        if (path.getParent() != null) {
            makeDirs(path.getParent());
        }
    }

    /**
     * Create directories to the {@code path}.
     *
     * @param path to which directories are creating
     * @throws ImplementorException if {@link Files#createDirectories(Path, FileAttribute[])} throws
     *                              {@link IOException}
     */
    private void makeDirs(Path path) throws ImplementorException {
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new ImplementorException("Can't create directories on path to \"" + path + "\"",
                e);
        }
    }

    /**
     * Convert sequence of {@link String} to {@link Path}.
     *
     * @param first required first part of resulted {@link Path}
     * @param more  other parts of resulted {@link Path}
     * @return {@link Path} made of parameters
     * @throws ImplementorException if can't convert parameters to {@link Path}
     */
    private static Path getPath(String first, String... more) throws ImplementorException {
        try {
            return Path.of(first, more);
        } catch (InvalidPathException exc) {
            throw new ImplementorException("Can't parse path", exc);
        }
    }

    /**
     * Get {@link String} representing package path to {@code token} with {@code separator} between
     * sections and with {@code extension} in the end.
     *
     * @param token     token to which path is considering
     * @param extension end of returning value
     * @param separator separator between sections of path
     * @return {@link String} representing result path.
     */
    private String getImplRelativePath(Class<?> token, String extension, String separator) {

        StringBuilder fullPath = new StringBuilder();

        Package pack = token.getPackage();
        if (pack != null) {
            fullPath.append(pack.getName().replace(".", separator));
            fullPath.append(separator);
        }

        fullPath.append(getImplName(token));
        fullPath.append(extension);
        return fullPath.toString();
    }

    /**
     * Get simple name of implementing {@code token}.
     *
     * @param token implementing {@link Class}
     * @return implementation name {@link String}
     */
    private String getImplName(Class<?> token) {
        return token.getSimpleName() + "Impl";
    }

    /**
     * Check if {@link Implementor} can implement {@code token}.
     *
     * @param token {@link Class} which are checking
     * @throws ImplementorException if {@link Implementor} can't implement {@code token}
     */
    private void checkToken(Class<?> token) throws ImplementorException {
        if (token.isPrimitive() || token == Enum.class || token.isArray() ||
            token.isAnonymousClass()) {
            throw new ImplementorException("Token is not local or member class or interface");
        }
        if (Modifier.isFinal(token.getModifiers())
            || Modifier.isPrivate(token.getModifiers())) {
            throw new ImplementorException("Unable to extend class with suggested modifiers");
        }
    }

    /**
     * Get {@link Path} to implementation of {@code token} with {@code extension} end.
     *
     * @param root      {@link Path} to root unnamed package containing {@code token}
     * @param token     {@link Class} to which implementation result {@link Path} are returning
     * @param extension {@link String} extension of implementation
     * @return {@link Path} to implementation of {@code token}
     * @throws ImplementorException if can't parse resulted path
     */
    private Path getImplFullPath(Path root, Class<?> token, String extension)
        throws ImplementorException {
        return getPath(root != null ? root.toString() + File.separator : "",
            getImplRelativePath(token, extension, File.separator));
    }

    /**
     * Create implementation of {@code token} in {@code root} path with name of token plus {@code
     * Impl} suffix. Result is {@code .java} file lays in {@code root} destination.
     *
     * @param token type token to create implementation for.
     * @param root  root directory.
     * @throws ImplementorException if predictable in implementing time exception is occurred
     */
    public void implement(Class<?> token, Path root) throws ImplementorException {
        checkToken(token);
        checkAccessibleConstructor(token);

        Path outputPath = getImplFullPath(root, token, ".java");
        makeParentDirs(outputPath);

        try (BufferedWriter output = Files.newBufferedWriter(outputPath)) {
            output.write(getUnicode(getImplementation(token)));
        } catch (IOException | SecurityException exc) {
            throw new ImplementorException("Problems with writing output file", exc);
        }
    }

    /**
     * Checks if {@code token} has accessible constructor.
     *
     * @param token which constructor is checking
     * @throws ImplementorException if {@code token} hasn't got accessible constructor
     */
    private void checkAccessibleConstructor(Class<?> token) throws ImplementorException {
        if (getConstructors(token).equals("") && token.getDeclaredConstructors().length != 0) {
            throw new ImplementorException("Parent has no accessible constructor");
        }
    }

    /**
     * Get {@link String} full representation of implementation of {@code token}.
     *
     * @param token which implementation generating
     * @return {@link String} generated implementation
     */
    public String getImplementation(Class<?> token) {
        return getPackage(token) +
            getClass(token);
    }

    /**
     * Get extension part of implemented token.
     *
     * @param token which implementation generating
     * @return {@link String} extension part
     */
    private String getExtension(Class<?> token) {
        StringBuilder sb = new StringBuilder();
        if (token.isInterface()) {
            sb.append("implements");
        } else {
            sb.append("extends");
        }
        sb.append(SPACE);
        sb.append(token.getCanonicalName());

        return sb.toString();
    }

    /**
     * Get {@link String} representing class implementing {@code token}.
     *
     * @param token which implementation generating
     * @return {@link String} class part
     */
    private String getClass(Class<?> token) {
        String name =
            "class" +
                SPACE +
                getImplName(token) +
                SPACE +
                getExtension(token);

        return getNamedBlock(0, name,
            getConstructors(token),
            getMethods(token)
        );
    }

    /**
     * Get {@link String} representing all constructors in implementation of {@code token}.
     *
     * @param token which implementation generating
     * @return {@link String} of constructors
     */
    private String getConstructors(Class<?> token) {
        Constructor<?>[] constructors = token.getDeclaredConstructors();
        StringBuilder sb = new StringBuilder();

        for (Constructor<?> constructor : constructors) {
            if (Modifier.isPrivate(constructor.getModifiers())) {
                continue;
            }
            sb.append(getConstructor(token, constructor));
            sb.append(LINE_SEPARATOR);
        }
        return sb.toString();
    }

    /**
     * Return block of {@code strs} and {@code name} before it.
     *
     * @param depth number of tabulations before first and last lines of block
     * @param name  {@link String} before block
     * @param strs  contents of block
     * @return {@link String result}
     */
    private String getNamedBlock(int depth, String name, String... strs) {
        return TAB.repeat(depth) + name + SPACE + "{" + LINE_SEPARATOR +
            string(strs) +
            TAB.repeat(depth) + "}" + LINE_SEPARATOR;
    }

    /**
     * Join {@code strs} together in on {@link String}.
     *
     * @param strs {@link String[]} to join
     * @return {@link String} consist of joined {@code strs}
     */
    private String string(String... strs) {
        StringBuilder sb = new StringBuilder();
        for (String str : strs) {
            sb.append(str);
        }
        return sb.toString();
    }

    /**
     * Makes signature without name from {@code arguments} types and generated names embraced with
     * parenthesis.
     *
     * @param arguments array of {@link Class} of arguments type
     * @return {@link String} embraced arguments
     */
    private String getArguments(Class<?>[] arguments) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < arguments.length; i++) {
            sb.append(arguments[i].getCanonicalName());
            sb.append(SPACE);
            sb.append("a").append(i);
            sb.append(",");
            sb.append(SPACE);
        }
        if (arguments.length != 0) {
            sb.delete(sb.length() - 2, sb.length());
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * Get embraced generated values separated with comas.
     *
     * @param length number of generated values
     * @return {@link String} of embraced values
     */
    private String getArgumentsNames(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append("a").append(i);
            sb.append(",");
            sb.append(SPACE);
        }
        if (length != 0) {
            sb.delete(sb.length() - 2, sb.length());
        }
        return sb.toString();
    }

    /**
     * Generate implementation version of {@code construcror}.
     *
     * @param token       which implementation generating
     * @param constructor super constructor of generating one
     * @return {@link String} representing generated {@code constructor}.
     */
    private String getConstructor(Class<?> token, Constructor<?> constructor) {
        Class<?>[] parameters = constructor.getParameterTypes();

        String name = "public" +
            SPACE +
            getImplName(token) +
            getArguments(parameters) +
            getExceptions(constructor.getExceptionTypes());

        String line = getLine(
            "super(", getArgumentsNames(parameters.length), ")"
        );

        return getNamedBlock(1, name, line);
    }

    /**
     * Get line of code ends with {@code ";"}.
     *
     * @param strs contents of code
     * @return {@link String} line of code
     */
    private String getLine(String... strs) {
        return TAB.repeat(2) +
            string(strs) +
            ";" +
            LINE_SEPARATOR;
    }

    /**
     * Get in code java's package declaration.
     *
     * @param token which implementation generating
     * @return {@link String} package representation.
     */
    private String getPackage(Class<?> token) {
        Package pack = token.getPackage();
        if (pack != null) {
            return "package" + SPACE + pack.getName() + ";" +
                LINE_SEPARATOR +
                LINE_SEPARATOR;
        }
        return "";
    }

    /**
     * Generate all methods which need to override to implement {@code token}.
     *
     * @param token which implementation generating
     * @return {@link String} of generated methods
     */
    private String getMethods(Class<?> token) {
        StringBuilder sb = new StringBuilder();
        for (Method method : getAbstractMethods(token)) {
            sb.append(getMethod(method));
        }
        return sb.toString();
    }

    /**
     * Get "throws" part after signature.
     *
     * @param exceptions classes of throwing exceptions
     * @return {@link String} result "throws" part
     */
    private String getExceptions(Class<?>[] exceptions) {
        if (exceptions.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(SPACE);
        sb.append("throws");
        sb.append(SPACE);
        for (Class<?> exception : exceptions) {
            sb.append(exception.getCanonicalName());
            sb.append(",");
            sb.append(SPACE);
        }
        sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
    }

    /**
     * Generates method default representation for implementation
     *
     * @param method from which implementation generate
     * @return {@link String} of generated method
     */
    private String getMethod(Method method) {
        String name =
            getAccessModifier(method) + method.getReturnType().getCanonicalName() + SPACE +
                method.getName() + getArguments(method.getParameterTypes()) +
                getExceptions(method.getExceptionTypes());
        String body = getLine("return", getDefaultReturn(method.getReturnType()));
        return getNamedBlock(1, name, body);
    }

    /**
     * Returns {@link String} with default for {@code clazz} value.
     *
     * @param clazz for which default value generates
     * @return {@link String} with default value
     */
    private String getDefaultReturn(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            if (clazz.getName().equals("boolean")) {
                return SPACE + "false";
            }
            if (clazz.getName().equals("void")) {
                return "";
            } else {
                return SPACE + "0";
            }
        } else {
            return SPACE + "null";
        }
    }

    /**
     * {@link String} represents access modifier of {@code method} plus {@link #SPACE} if it is not
     * package-private.
     *
     * @param method which method access modifier got
     * @return {@link String} represents access modifier of {@code method}
     */
    private String getAccessModifier(Method method) {
        if (isPublic(method)) {
            return "public" + SPACE;
        } else if (isProtected(method)) {
            return "protected" + SPACE;
        } else if (isPrivate(method)) {
            return "private" + SPACE;
        } else {
            return "";
        }
    }

    /**
     * Get {@link Predicate<Integer>#test} result from {@code pred} on modifiers of {@code method}.
     *
     * @param method which get tested
     * @param pred   {@link Predicate<Integer>#test} which tests
     * @return {@link boolean} test result
     */
    private boolean isModifier(Method method, Predicate<Integer> pred) {
        return pred.test(method.getModifiers());
    }

    /**
     * Return if method is abstract.
     *
     * @param method which method is checked
     * @return {@link boolean} result
     */
    private boolean isAbstract(Method method) {
        return isModifier(method, Modifier::isAbstract);
    }

    /**
     * Return if method is private.
     *
     * @param method which method is checked
     * @return {@link boolean} result
     */
    private boolean isPrivate(Method method) {
        return isModifier(method, Modifier::isPrivate);
    }

    /**
     * Return if method is protected.
     *
     * @param method which method is checked
     * @return {@link boolean} result
     */
    private boolean isProtected(Method method) {
        return isModifier(method, Modifier::isProtected);
    }

    /**
     * Return if method is public.
     *
     * @param method which method is checked
     * @return {@link boolean} result
     */
    private boolean isPublic(Method method) {
        return isModifier(method, Modifier::isPublic);
    }

    /**
     * Return if method is static.
     *
     * @param method which method is checked
     * @return {@link boolean} result
     */
    private boolean isStatic(Method method) {
        return isModifier(method, Modifier::isStatic);
    }

    /**
     * Get all abstract methods of {@code token} need to override.
     *
     * @param token which methods are considering
     * @return {@link Method[]} of all appropriate methods
     */
    private Method[] getAbstractMethods(Class<?> token) {
        Set<MethodWrapper> implementedMethods = new HashSet<>();
        Set<MethodWrapper> abstractMethods = new HashSet<>();

        for (Class<?> clazz = token; clazz.getSuperclass() != null; clazz = clazz.getSuperclass()) {
            for (Method method : clazz.getDeclaredMethods()) {
                MethodWrapper wrappedMethod = new MethodWrapper(method);
                if (isPrivate(method) || isStatic(method) ||
                    implementedMethods.contains(wrappedMethod) ||
                    abstractMethods.contains(wrappedMethod)) {
                    continue;
                }
                if (isAbstract(method)) {
                    abstractMethods.add(wrappedMethod);
                } else {
                    implementedMethods.add(wrappedMethod);
                }
            }
        }
        for (Method method : token.getMethods()) {
            MethodWrapper wrappedMethod = new MethodWrapper(method);

            if (isStatic(method)) {
                continue;
            }
            if (isAbstract(method) && !implementedMethods.contains(wrappedMethod)) {
                abstractMethods.add(wrappedMethod);
            }
        }
        return abstractMethods.stream()
            .map(MethodWrapper::getMethod)
            .toArray(Method[]::new);
    }

    /**
     * Create jar file with implemented class. Arguments:
     *
     * @param args {@link String[]} of arguments
     *             <ol>
     *                  <li>Type name of class need to implement</li>
     *                  <li>Path where jar to create</li>
     *             </ol>
     */
    public static void main(String[] args) {
        try {
            checkArgs(args);

            Class<?> token = getToken(args[0]);
            Path jarFile = getPath(args[1]);

            new Implementor().implementJar(token, jarFile);

        } catch (ImplementorException exc) {
            System.err.println("Exception occurred: \n" + exc.getMessage());
            exc.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Get {@link Class} by {@code classPath}.
     *
     * @param classPath which we {@link Class} get
     * @return result {@link Class}
     * @throws ImplementorException if load class
     */
    private static Class<?> getToken(String classPath) throws ImplementorException {
        try {
            return Class.forName(classPath);
        } catch (ClassNotFoundException | ExceptionInInitializerError e) {
            throw new ImplementorException("Can't load class: " + classPath, e);
        }
    }

    /**
     * Check if provided two non null arguments.
     *
     * @param args which are checking
     * @throws ImplementorException if arguments are not passed check
     */
    private static void checkArgs(String[] args) throws ImplementorException {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            throw new ImplementorException("You need to provide 2 non-null arguments.");
        }
    }

    /**
     * Get {@link Path} consists of root and son. Or only son if {@code root} is {@code null}.
     *
     * @param root first part of result
     * @param son  second part of result
     * @return result {@link Path}
     * @throws ImplementorException if can't resolve result {@link Path}.
     */
    private Path resolve(Path root, String son) throws ImplementorException {
        try {
            if (root != null) {
                return root.resolve(son);
            } else {
                return getPath(son);
            }
        } catch (InvalidPathException exc) {
            throw new ImplementorException("Can't resolve path consist of " +
                root + " and " + son, exc);
        }
    }

    /**
     * Compiles file with the class-path consists of {@code root} and {@link #getClassPath}{@code
     * (token)}.
     *
     * @param token for second part of class-path
     * @param root  for first part of class-path
     * @param file  name of compiled file
     * @throws ImplementorException if
     *                              <ul>
     *                                  <li>Can't get java compiler</li>
     *                                  <li>Can't compile file</li>
     *                              </ul>
     */
    public static void compileFile(Class<?> token, final Path root, final String file)
        throws ImplementorException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new ImplementorException(
                "Could not find java compiler, include tools.jar to classpath");
        }
        final String classpath = root + File.pathSeparator + getClassPath(token);
        String[] args = {file, "-cp", classpath};
        final int exitCode = compiler.run(null, null, null, args);
        if (exitCode != 0) {
            throw new ImplementorException(
                "Can't compile file. Compiler returned exit code: " + exitCode);
        }
    }

    /**
     * Get class-path of token.
     *
     * @param token which path to finding
     * @return {@link String} class-path
     * @throws ImplementorException if can't convert {@code url} of token location to {@code uri}.
     */
    private static String getClassPath(Class<?> token) throws ImplementorException {
        try {
            return Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI())
                .toString();
        } catch (final URISyntaxException e) {
            throw new ImplementorException("Compile file exception: Can't convert URL to URI", e);
        }
    }

    /**
     * Create jar with implemented {@code token} file.
     *
     * @param token   type token to create implementation for.
     * @param jarFile target <var>.jar</var> file.
     * @throws ImplementorException when implementation cannot be generated.
     */
    @Override
    public void implementJar(Class<?> token, final Path jarFile) throws ImplementorException {
        Path tempDir = resolve(jarFile.getParent(), ".impl-temp");
        try {
            makeDirs(tempDir);
            implement(token, tempDir);

            Path javaPath = getImplFullPath(tempDir, token, ".java");
            compileFile(token, tempDir, javaPath.toString());

            makeJar(token, jarFile, tempDir);
        } finally {
            deleteDir(tempDir);
        }
    }

    /**
     * Deletes recursively temporary dirs specified by {@code tempDir}.
     *
     * @param tempDir {@link Path} to deleting dir
     * @throws ImplementorException if {@link IOException} occurred while deleting
     */
    private void deleteDir(Path tempDir) throws ImplementorException {
        try {
            Files.walkFileTree(tempDir, new DirRemover());
        } catch (IOException e) {
            throw new ImplementorException("Can't delete temporary directories.", e);
        }
    }

    /**
     * Make {@code .jar} file with file with implementation of {@code token} lays in {@code
     * tempDir}.
     *
     * @param token   which class implemented
     * @param jarFile jar file result location
     * @param tempDir directory where compiled file locates which will be in {@code jar}
     * @throws ImplementorException if {@link IOException} occurs in time of writing {@code .jar}
     *                              file
     */
    private void makeJar(Class<?> token, Path jarFile, Path tempDir) throws ImplementorException {
        String inJarImplPath = getImplRelativePath(token, ".class", "/");
        Path fullImplPath = getImplFullPath(tempDir, token, ".class");

        Manifest manifest = new Manifest();
        Attributes attr = manifest.getMainAttributes();
        attr.put(Attributes.Name.MANIFEST_VERSION, "1.0");

        try (JarOutputStream jarWriter =
            new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
            jarWriter.putNextEntry(new ZipEntry(inJarImplPath));
            Files.copy(fullImplPath.toAbsolutePath(), jarWriter);
        } catch (IOException e) {
            throw new ImplementorException("Can't write to jar file", e);
        }
    }
}

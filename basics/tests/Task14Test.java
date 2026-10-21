import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

// Task 14: 14-calculator/Calculator.java
// Run: javac 14-calculator/Calculator.java && java tests/Task14Test.java
public class Task14Test {
    static final String DIR = "14-calculator";
    static final String USAGE = "Usage: java Calculator <number> <operator> <number>";

    static T.Run calc(String... args) {
        return T.run(DIR, "Calculator", "", args);
    }

    static void success(String expectedLine, String... args) {
        T.Run r = calc(args);
        T.eq("output of " + String.join(" ", args), List.of(expectedLine), r.lines());
        T.eq("exit code of " + String.join(" ", args), 0, r.exit());
        T.isTrue("stderr must be empty", r.err().isEmpty());
    }

    static void failure(String expectedMessage, String... args) {
        T.Run r = calc(args);
        String label = args.length == 0 ? "(no argument)" : String.join(" ", args);
        T.eq("stderr of " + label, List.of(expectedMessage), r.errLines());
        T.eq("exit code of " + label, 1, r.exit());
        T.isTrue("stdout must be empty for " + label + ", got: " + r.out().strip(), r.out().isEmpty());
    }

    public static void main(String[] args) {
        T.check("addition: 12 + 8 = 20 (and large numbers as long)", () -> {
            success("12 + 8 = 20", "12", "+", "8");
            success("3000000000 + 1 = 3000000001", "3000000000", "+", "1");
        });

        T.check("subtraction and negative numbers", () -> {
            success("5 - 9 = -4", "5", "-", "9");
            success("-3 + -4 = -7", "-3", "+", "-4");
        });

        T.check("multiplication: x operator, and * when it arrives literally", () -> {
            success("6 x 7 = 42", "6", "x", "7");
            success("6 * 7 = 42", "6", "*", "7");
        });

        T.check("integer division: 7 / 2 = 3 (like in Java)", () -> {
            success("7 / 2 = 3", "7", "/", "2");
            success("-7 / 2 = -3", "-7", "/", "2");
            success("100 / 5 = 20", "100", "/", "5");
        });

        T.check("division by zero: message on stderr, exit code 1, nothing on stdout", () ->
                failure("Error: division by zero", "5", "/", "0"));

        T.check("non-numeric argument: Error: invalid number", () -> {
            failure("Error: invalid number: abc", "abc", "+", "1");
            failure("Error: invalid number: 2.5", "1", "+", "2.5");
        });

        T.check("unknown operator: Error: unknown operator", () -> {
            failure("Error: unknown operator: %", "4", "%", "2");
            failure("Error: unknown operator: ^", "2", "^", "8");
        });

        T.check("wrong number of arguments: Usage message", () -> {
            failure(USAGE);
            failure(USAGE, "1", "+");
            failure(USAGE, "1", "+", "2", "3");
        });

        T.done();
    }
}


// ============================================================
// Test harness: DO NOT MODIFY.
// It loads your compiled classes, runs your programs and
// prints [OK] / [KO] for each test.
// ============================================================
final class T {
    interface Body {
        void run() throws Throwable;
    }

    record Run(int exit, String out, String err) {
        List<String> lines() {
            return out.lines().toList();
        }

        List<String> errLines() {
            return err.lines().toList();
        }
    }

    private static int ok = 0;
    private static int ko = 0;
    private static final Map<String, Run> CACHE = new HashMap<>();

    static void check(String name, Body body) {
        try {
            body.run();
            ok++;
            System.out.println("[OK] " + name);
        } catch (Throwable t) {
            ko++;
            System.out.println("[KO] " + name + " -> " + describe(t));
        }
    }

    static void done() {
        System.out.println(ok + "/" + (ok + ko) + " tests passed");
        System.exit(ko == 0 ? 0 : 1);
    }

    static String describe(Throwable t) {
        if (t instanceof AssertionError) {
            return String.valueOf(t.getMessage());
        }
        return t.getClass().getSimpleName() + (t.getMessage() == null ? "" : ": " + t.getMessage());
    }

    // ---------- assertions ----------
    static void eq(Object expected, Object actual) {
        if (!Objects.deepEquals(expected, actual)) {
            throw new AssertionError("expected " + show(expected) + ", got " + show(actual));
        }
    }

    static void eq(String what, Object expected, Object actual) {
        if (!Objects.deepEquals(expected, actual)) {
            throw new AssertionError(what + ": expected " + show(expected) + ", got " + show(actual));
        }
    }

    static void near(double expected, Object actual) {
        if (!(actual instanceof Double d) || Math.abs(d - expected) > 1e-9) {
            throw new AssertionError("expected " + expected + ", got " + show(actual));
        }
    }

    static void isTrue(String what, boolean condition) {
        if (!condition) {
            throw new AssertionError(what);
        }
    }

    static String show(Object o) {
        if (o == null) {
            return "null";
        }
        if (o instanceof int[] a) {
            return Arrays.toString(a);
        }
        if (o instanceof Object[] a) {
            return Arrays.deepToString(a);
        }
        if (o instanceof String s) {
            return "\"" + s.replace("\n", "\\n") + "\"";
        }
        return o + " (" + o.getClass().getSimpleName() + ")";
    }

    // ---------- run a program (java -cp dir Class args) ----------
    static void needClass(String dir, String cls) {
        if (!new File(dir, cls + ".class").isFile()) {
            throw new AssertionError(cls + ".class not found in " + dir + "/: did you run javac "
                    + dir + "/" + cls + ".java?");
        }
    }

    static Run run(String dir, String cls, String stdin, String... args) {
        needClass(dir, cls);
        String key = dir + "|" + cls + "|" + stdin + "|" + String.join("\u0001", args);
        Run cached = CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        try {
            List<String> cmd = new ArrayList<>();
            cmd.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
            cmd.add("-cp");
            cmd.add(dir);
            cmd.add(cls);
            cmd.addAll(Arrays.asList(args));
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.environment().remove("JAVA_TOOL_OPTIONS");
            pb.environment().remove("JDK_JAVA_OPTIONS");
            pb.environment().remove("_JAVA_OPTIONS");
            Process p = pb.start();
            CompletableFuture<String> out = CompletableFuture.supplyAsync(() -> readAll(p.getInputStream()));
            CompletableFuture<String> err = CompletableFuture.supplyAsync(() -> readAll(p.getErrorStream()));
            try (OutputStream os = p.getOutputStream()) {
                os.write(stdin.getBytes(StandardCharsets.UTF_8));
            } catch (IOException ignored) {
                // the program may have exited without reading its input: not an error
            }
            if (!p.waitFor(10, TimeUnit.SECONDS)) {
                p.destroyForcibly();
                throw new AssertionError("timeout after 10 s (infinite loop? is the program waiting for input?)");
            }
            Run r = new Run(p.exitValue(), out.get(), err.get());
            CACHE.put(key, r);
            return r;
        } catch (AssertionError e) {
            throw e;
        } catch (Exception e) {
            throw new AssertionError("cannot run " + cls + ": " + e);
        }
    }

    private static String readAll(InputStream in) {
        try (in) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    // ---------- load a compiled class (reflection) ----------
    // Each call creates a new class loader: static fields start from zero again.
    static Class<?> load(String dir, String cls) {
        needClass(dir, cls);
        try {
            URLClassLoader loader = new URLClassLoader(new URL[] {new File(dir).toURI().toURL()},
                    ClassLoader.getPlatformClassLoader());
            Class<?> c = Class.forName(cls, true, loader);
            if (!Modifier.isPublic(c.getModifiers())) {
                throw new AssertionError("class " + cls + " must be public");
            }
            return c;
        } catch (ClassNotFoundException e) {
            throw new AssertionError("class " + cls + " not found (class name different from the file name?)");
        } catch (MalformedURLException e) {
            throw new AssertionError(e.toString());
        }
    }

    static String sig(String name, Class<?>... types) {
        StringBuilder sb = new StringBuilder(name).append("(");
        for (int i = 0; i < types.length; i++) {
            sb.append(i > 0 ? ", " : "").append(types[i].getSimpleName());
        }
        return sb.append(")").toString();
    }

    static Method method(Class<?> c, String name, Class<?>... types) {
        try {
            Method m = c.getDeclaredMethod(name, types);
            m.setAccessible(true);
            return m;
        } catch (NoSuchMethodException e) {
            throw new AssertionError("method not found: " + sig(name, types) + " in " + c.getSimpleName());
        }
    }

    // "public static" method
    static Method sm(Class<?> c, String name, Class<?>... types) {
        Method m = method(c, name, types);
        if (!Modifier.isPublic(m.getModifiers()) || !Modifier.isStatic(m.getModifiers())) {
            throw new AssertionError(sig(name, types) + " must be declared 'public static'");
        }
        return m;
    }

    static Field field(Class<?> c, String name) {
        try {
            Field f = c.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {
            throw new AssertionError("field not found: " + name + " in " + c.getSimpleName());
        }
    }

    static boolean packagePrivate(int modifiers) {
        return !Modifier.isPublic(modifiers) && !Modifier.isProtected(modifiers) && !Modifier.isPrivate(modifiers);
    }

    static Object call(Method m, Object target, Object... args) {
        Object[] box = new Object[2];
        Thread th = new Thread(() -> {
            try {
                box[0] = m.invoke(target, args);
            } catch (InvocationTargetException e) {
                box[1] = e.getCause();
            } catch (Throwable t) {
                box[1] = t;
            }
        });
        th.setDaemon(true);
        th.start();
        try {
            th.join(10_000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (th.isAlive()) {
            throw new AssertionError(m.getName() + " does not terminate (infinite loop?)");
        }
        if (box[1] != null) {
            throw new AssertionError(m.getName() + " threw " + describe((Throwable) box[1]));
        }
        return box[0];
    }

    private static Class<?> primitiveOf(Object o) {
        if (o instanceof Integer) return int.class;
        if (o instanceof Long) return long.class;
        if (o instanceof Double) return double.class;
        if (o instanceof Boolean) return boolean.class;
        if (o instanceof Character) return char.class;
        return o.getClass();
    }

    // instantiate c with the constructor whose types match the arguments
    static Object make(Class<?> c, Object... args) {
        Class<?>[] types = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            types[i] = primitiveOf(args[i]);
        }
        try {
            Constructor<?> k = c.getDeclaredConstructor(types);
            k.setAccessible(true);
            return k.newInstance(args);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("constructor not found: " + sig(c.getSimpleName(), types));
        } catch (InvocationTargetException e) {
            throw new AssertionError("the constructor threw " + describe(e.getCause()));
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e.toString());
        }
    }

    // the program must exit normally and print this line
    static void has(Run r, String expected) {
        if (r.exit() != 0) {
            throw new AssertionError("the program exited with code " + r.exit()
                    + (r.err().isBlank() ? "" : " (stderr: " + r.err().strip() + ")"));
        }
        if (!r.lines().contains(expected)) {
            throw new AssertionError("expected line not found: " + show(expected));
        }
    }

    // output lines without the program's prompts
    static List<String> without(List<String> lines, String prompt) {
        List<String> result = new ArrayList<>(lines);
        result.removeIf(prompt::equals);
        return result;
    }
}

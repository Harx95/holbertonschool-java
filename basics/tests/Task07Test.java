import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

// Task 7: 7-arrays/ArrayTools.java
// Run: javac 7-arrays/ArrayTools.java && java tests/Task07Test.java
public class Task07Test {
    static final String DIR = "7-arrays";

    static Class<?> cls() {
        return T.load(DIR, "ArrayTools");
    }

    public static void main(String[] args) {
        T.check("min and max of an array", () -> {
            Class<?> c = cls();
            Method max = T.sm(c, "max", int[].class);
            Method min = T.sm(c, "min", int[].class);
            T.eq(7, T.call(max, null, (Object) new int[] {3, -1, 7, 7, 0}));
            T.eq(-1, T.call(min, null, (Object) new int[] {3, -1, 7, 7, 0}));
            T.eq(5, T.call(max, null, (Object) new int[] {5}));
            T.eq(-2, T.call(max, null, (Object) new int[] {-9, -2, -5}));
            T.eq(-9, T.call(min, null, (Object) new int[] {-9, -2, -5}));
        });

        T.check("average: mean as a double (0.0 for an empty array)", () -> {
            Method m = T.sm(cls(), "average", int[].class);
            T.near(2.5, T.call(m, null, (Object) new int[] {1, 2, 3, 4}));
            T.near(5.0, T.call(m, null, (Object) new int[] {5}));
            T.near(-1.0, T.call(m, null, (Object) new int[] {-3, 1}));
            T.near(0.0, T.call(m, null, (Object) new int[] {}));
        });

        T.check("reverseInPlace: reverses the array it receives, without creating another one", () -> {
            Method m = T.sm(cls(), "reverseInPlace", int[].class);
            T.eq("the return type must be void", void.class, m.getReturnType());
            int[] even = {1, 2, 3, 4};
            T.call(m, null, (Object) even);
            T.eq(new int[] {4, 3, 2, 1}, even);
            int[] odd = {1, 2, 3, 4, 5};
            T.call(m, null, (Object) odd);
            T.eq(new int[] {5, 4, 3, 2, 1}, odd);
            int[] empty = {};
            T.call(m, null, (Object) empty);
            T.eq(new int[] {}, empty);
        });

        T.check("indexOf: first position, -1 if absent", () -> {
            Method m = T.sm(cls(), "indexOf", int[].class, int.class);
            int[] t = {4, 8, 15, 8, 23};
            T.eq(0, T.call(m, null, t, 4));
            T.eq("first occurrence", 1, T.call(m, null, t, 8));
            T.eq(4, T.call(m, null, t, 23));
            T.eq(-1, T.call(m, null, t, 99));
            T.eq(-1, T.call(m, null, new int[] {}, 1));
        });

        T.check("identity: n x n identity matrix (2D array)", () -> {
            Method m = T.sm(cls(), "identity", int.class);
            T.eq(new int[][] {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}}, T.call(m, null, 3));
            T.eq(new int[][] {{1}}, T.call(m, null, 1));
            T.eq(new int[][] {{1, 0}, {0, 1}}, T.call(m, null, 2));
            T.eq("identity(0)", 0, ((int[][]) T.call(m, null, 0)).length);
        });

        T.check("format: lines separated by \\n, values by a space", () -> {
            Method m = T.sm(cls(), "format", int[][].class);
            T.eq("1 0 0\n0 1 0\n0 0 1", T.call(m, null, (Object) new int[][] {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}}));
            T.eq("1 2\n3 4", T.call(m, null, (Object) new int[][] {{1, 2}, {3, 4}}));
            T.eq("42", T.call(m, null, (Object) new int[][] {{42}}));
            T.eq("", T.call(m, null, (Object) new int[][] {}));
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

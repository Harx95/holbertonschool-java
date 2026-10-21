public class Variables {
	public static void main (String[] args) {
		System.out.printf("byte: %d %d %n", Byte.MIN_VALUE, Byte.MAX_VALUE);
		System.out.printf("short: " + Short.MIN_VALUE + " " + Short.MAX_VALUE);
		System.out.printf("int: " + Integer.MIN_VALUE + " " + Integer.MAX_VALUE);
		System.out.printf("long: " + Long.MIN_VALUE + " " + Long.MAX_VALUE);
		System.out.printf("float: " + Float.MIN_VALUE + " " + Float.MAX_VALUE);
		System.out.printf("double: " + Double.MIN_VALUE + " " + Double.MAX_VALUE);
		System.out.printf("char: " + (int) Character.MIN_VALUE + " " + (int) Character.MAX_VALUE);
		System.out.printf("boolean: %b %B", 1, 1);
		System.out.printf("overflow: " + (Integer.MAX_VALUE + 1));
		System.out.printf("precision: " + (0.1 + 0.2));
		System.out.printf("truncation: " + (int) 3.99);
		System.out.printf("promotion: " + (Integer.MAX_VALUE + 1L));
	}
}

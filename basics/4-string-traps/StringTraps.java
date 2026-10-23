public class StringTraps {
	public static void main (String[] args) {
		System.out.printf("literals ==: %b%n", sameContent(null, "Axel"));
		System.out.printf("new String ==: %b%n", sameObject("Axel", "Axel"));
		System.out.printf("%s%n", repeat("a", 2, ", "));
		System.out.printf("%s%n", reverse("axel"));

		long beginSlow = System.nanoTime();
		buildSlow(100_000);
		long endSlow = System.nanoTime();
		long durationSlow = endSlow - beginSlow;
		System.out.printf("slow: %d ms%n", durationSlow / 1000000);

		long beginFast = System.nanoTime();
		buildSlow(100_000);
		long endFast = System.nanoTime();
		long durationFast = endFast - beginFast;
		System.out.printf("fast: %d ms%n", durationFast / 1000000);
	}

	public static boolean sameContent(String a, String b) {
		return (a == null && b == null) || (a != null && a.equals(b));
	}

	public static boolean sameObject(String a, String b) {
		return a == b;
	}

	public static String repeat(String s, int times, String separator) {
		StringBuilder sb = new StringBuilder();

		if (times <= 0)
			return sb.append("").toString();
		else if (times == 1)
			return sb.append(s).toString();
		else
			for (int i = 0; i < times; i++) {
				sb.append(s);
				sb.append(separator);
			}

		return sb.delete(sb.length() - separator.length(), sb.length()).toString();
	}

	public static String reverse(String s) {
		StringBuilder sb = new StringBuilder();
		return sb.append(s).reverse().toString();
	}

	public static String buildSlow(int n) {
		String str = "";
		for (int i = 0; i < n; i++)
			str += (char) ('a' + i % 26);

		return str;
	}

	public static String buildFast(int n) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; i++)
			sb.append((char) ('a' + i % 26));

		return sb.toString();
	}
}

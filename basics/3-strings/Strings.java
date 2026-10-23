public class Strings {
	public static void main(String[] args) {
		System.out.printf("%s%n", capitalize("hELLO wORLD"));
		System.out.printf("%d%n", countWords("    "));
		System.out.printf("%s%n", initials("  ada    lovelace "));
		System.out.printf("%b%n", isPalindrome("A man a plan a canal Panama"));
		System.out.printf("%s%n", extractDomain("student@holbertonschool.com"));
	}

	public static String capitalize (String s) {
		String output = s.toLowerCase();

		if (s.length() == 0)
			return "";

		return output.substring(0, 1).toUpperCase() + output.substring(1);
	}

	public static int countWords (String s) {
		String trimmed = s.trim();

		if (trimmed.isEmpty())
			return 0;

		int countWords = trimmed.split("\\s+").length;

		if (countWords == 0)
			return 0;
		else
			return countWords;
	}

	public static String initials(String s) {
		String trimmed = s.trim();

		if (trimmed.isEmpty())
			return "";

		String initials = "";

		for (String word : trimmed.split("\\s+"))
			initials+=word.charAt(0);

		return initials.toUpperCase();
	}

	public static boolean isPalindrome(String s) {
        s = s.toLowerCase().trim().replaceAll("\\s+","");

        String rev = "";
        for (int i = s.length() - 1; i >= 0; i--) {
            rev = rev + s.charAt(i);
        }

        return s.equals(rev);
	}

	public static String extractDomain(String email) {
		if (email.contains("@") == false)
			return "";

		return email.substring(email.indexOf("@") + 1);

	}

}

public class Greeter {
	public static void main(String[] args) {
		if (args.length == 0)
			System.out.println("Hello, stranger!");
		else {
			for (String name : args) {
				System.out.println("Hello, " + name + "!");
			}
		}
	}
}

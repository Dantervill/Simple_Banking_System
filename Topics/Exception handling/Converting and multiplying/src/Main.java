import java.util.Scanner;
class Main {
    public static void main(String[] args) {
        Scanner scn = new Scanner(System.in);
        do {
            String line = scn.nextLine();
            if (line.equals("0")) {
                break;
            } else {
                try {
                    System.out.println(Integer.parseInt(line) * 10);
                } catch (Exception e) {
                    System.out.println("Invalid user input: " + line);
                }
            }
        } while (scn.hasNextLine());
    }
}

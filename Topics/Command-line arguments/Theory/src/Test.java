import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Test {
    public static void main(String[] args) {
        String pathToFile = "C:\\Users\\Vlas\\Downloads\\dataset_91065.txt";
        int counter = 0;
        int number;
        try (Scanner scanner = new Scanner(new File(pathToFile))) {
            while (scanner.hasNextLine()) {
                number = Integer.parseInt(scanner.nextLine());
                if (number == 0) {
                    break;
                } else if (number % 2 == 0) {
                    counter++;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println(counter);
    }
}

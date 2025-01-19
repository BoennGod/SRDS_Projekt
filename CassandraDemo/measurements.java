import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner;
import java.text.DecimalFormat;

class Measurements {
    public static void main(String[] args){
        double success = 0;
        double all = 0;
        double result = 0f;

        try {
            File myObj = new File("out.log");
            Scanner reader = new Scanner(myObj);
            while (reader.hasNextLine()) {
                String data = reader.nextLine();
                if (data.contains("SUCCESS")){
                    success += 1;
                    all += 1;
                }
                if (data.contains("FAIL")) {
                    all += 1;
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        result = (success/all)*100;
        System.out.println("S"+success);
        System.out.println("A"+all);
        DecimalFormat f = new DecimalFormat("##.00");
        System.out.println("Success rate: "+f.format(result)+"%");
    }
}
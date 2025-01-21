import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ArrayList;

class Measurements {
    public static void main(String[] args){
        double success = 0;
        double all = 0;
        double result = 0f;
        boolean anomalyFlag = false;

        List<String> succList = new ArrayList<>();
        List<String> anomalyList = new ArrayList<>();
        List<String> logsComparissonList = new ArrayList<>();

        try {
            File myObj = new File("out.log");
            Scanner reader = new Scanner(myObj);
            while (reader.hasNextLine()) {
                String data = reader.nextLine();
                if (data.contains("SUCCESS")){
                    succList.add(data);
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

        for (int i = 0; i < succList.size(); i++) {
            logsComparissonList.add(extractRelevantPart(succList.get(i), "locked"));
        }

        String log1;
        String log2;
        for (int i = 0; i < logsComparissonList.size(); i++) {
            log1 = logsComparissonList.get(i);
            for (int j = i; j < logsComparissonList.size(); j++) {
                if (i != j) {
                    log2 = logsComparissonList.get(j);
                    if (log1.equals(log2)) {
                        if (checkIfAnomalyByTime(succList.get(i), succList.get(j))){
                            anomalyFlag = true;
                            System.out.println("Anomaly found:");
                            System.out.println("Log1: " + succList.get(i));
                            System.out.println("Log2: " + succList.get(j));
                        }
                    }
                }
            }
        }

        if (!anomalyFlag){
            System.out.println("No anomalies found.");
        }

    }

    public static String getTimeByIndex(String text, int index) {

        String[] words = text.split("\\s+");

        int currentLength = 0;
        for (String word : words) {
            currentLength += word.length() + 1;
            if (currentLength > index) {
                return word;
            }
        }
        return "";
    }


    public static Boolean checkIfAnomalyByTime(String log1, String log2) {
        int time1 = Integer.valueOf(getTimeByIndex(log1, 0));
        int time2 = Integer.valueOf(getTimeByIndex(log2, 0));
        if (time2 < time1+300){
            return true;
        }
        return false;
    }


    public static String extractRelevantPart(String log, String keyword) {
        int index = log.indexOf(keyword);
        if (index != -1) {
            return log.substring(index);
        }
        return "";
    }
}
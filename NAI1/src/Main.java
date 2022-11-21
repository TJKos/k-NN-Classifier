import java.io.*;
import java.util.*;

public class Main {
    static int vecSize;
    static int testSize;
    public static void main(String[] args) throws IOException {
        Scanner scan = new Scanner(System.in);

        int k = Integer.parseInt(args[0]);
        File trainSet = new File(args[1]);
        File testSet = new File(args[2]);

        BufferedReader trainSetReader = new BufferedReader(new FileReader(trainSet.getAbsolutePath()));
        BufferedReader testSetReader = new BufferedReader(new FileReader(testSet.getAbsolutePath()));
        HashMap<String, List<List<Double>>> trainMap = new HashMap<>();
        HashMap<String, List<List<Double>>> testMap = new HashMap<>();

        final String ANSI_RESET = "\u001B[0m";
        final String ANSI_RED = "\u001B[31m";
        final String ANSI_GREEN = "\u001B[32m";
        while (trainSetReader.ready()) {
            String[] currVec = trainSetReader.readLine().split(",");

            if (trainMap.get(currVec[currVec.length-1]) == null) {
                List<List<Double>> list = new ArrayList<>();
                list.add(convertToList(currVec));
                trainMap.put(currVec[currVec.length - 1], list);
            }else{
                trainMap.get(currVec[currVec.length-1]).add(convertToList(currVec));
            }
        }

//        System.out.println(trainMap);

        while (testSetReader.ready()) {
            String[] currVec = testSetReader.readLine().split(",");

            if (testMap.get(currVec[currVec.length-1]) == null) {
                List<List<Double>> list = new ArrayList<>();
                list.add(convertToList(currVec));
                testMap.put(currVec[currVec.length - 1], list);
            }else{
                testMap.get(currVec[currVec.length-1]).add(convertToList(currVec));
            }
        }

        vecSize = testMap.get(testMap.entrySet().iterator().next().getKey()).get(0).size();


        int failures = 0;

        PriorityQueue<FeatureHolder> winners = new PriorityQueue<>();

        // przechodzi przez wektory testowe
        for (Map.Entry<String, List<List<Double>>> entry : testMap.entrySet()){
            for (List<Double> vec : entry.getValue()){

                // dla kazdego testowego wektora przechodzi przez wektory treningowe
                for (Map.Entry<String, List<List<Double>>> trainEntry : trainMap.entrySet()){
                    for (List<Double> trainVec : trainEntry.getValue()){
//                        System.out.println(trainVec);

                        if (winners.size() < k){
                            winners.add(new FeatureHolder(trainVec, trainEntry.getKey(), getDistance(trainVec, vec)));
                        }else{
//                            System.out.println(winners.peek().distance);
                            if (winners.peek().distance > getDistance(trainVec, vec)) {
                                winners.remove(winners.peek());
                                winners.add(new FeatureHolder(trainVec, trainEntry.getKey(), getDistance(trainVec, vec)));
                            }
                        }

                    }

                }

                HashMap<String, Integer> typeMap = new HashMap<>();
                for (FeatureHolder winner : winners){
                    typeMap.merge(winner.term, 1, Integer::sum);

                }

                String predicted = "";
                int maxN = 0;
                for (Map.Entry<String, Integer> typeEntry : typeMap.entrySet()){
                    if (typeEntry.getValue() > maxN){
                        maxN = typeEntry.getValue();
                        predicted = typeEntry.getKey();
                    }
                }



                System.out.print(vec + entry.getKey() + " ---> ");
                System.out.println(winners);
                System.out.println(typeMap);

                if (!predicted.equals(entry.getKey())){
                    System.out.println(ANSI_RED + "Failed prediction: " + predicted + ANSI_RESET);
                    failures++;
                }else{
                    System.out.println(ANSI_GREEN + "Correct prediction: " + predicted + ANSI_RESET);
                }

                System.out.println();
//                System.out.println(winners.peek());
                winners.clear();
//                System.out.println();
            }
//            System.out.println(entry.getValue());
        }

        testSize = getMapSize(testMap);
        System.out.println("Accuracy: " + (1 - (double)failures/testSize) * 100 + "%");
        System.out.println();

        while (true){
            int i = 0;
            List<Double> insertedList = new ArrayList<>();
            System.out.println("Insert 'q' to exit.");
            while (i++ < vecSize){
                System.out.print("Insert feature " + i + ": ");
                String inserted = scan.next();
                if (inserted.equals("q")){
                    return;
                }else{
                    try{
                        insertedList.add(Double.parseDouble(inserted));
                    }catch (NumberFormatException e){
                        i--;
                        System.out.println("Incorrect data inserted.");
                    }
                }
            }
            System.out.print("Insert term: ");
            String labelAttribute = scan.next();
            FeatureHolder featureHolder = new FeatureHolder(insertedList, labelAttribute);

            for (Map.Entry<String, List<List<Double>>> trainEntry : trainMap.entrySet()){
                for (List<Double> trainVec : trainEntry.getValue()){
//                        System.out.println(trainVec);

                    if (winners.size() < k){
                        winners.add(new FeatureHolder(trainVec, trainEntry.getKey(), getDistance(trainVec, featureHolder.vec)));
                    }else{
//                            System.out.println(winners.peek().distance);

                        if (winners.peek().distance > getDistance(trainVec, featureHolder.vec)) {
                            winners.remove(winners.peek());
                            winners.add(new FeatureHolder(trainVec, trainEntry.getKey(), getDistance(trainVec, featureHolder.vec)));
                        }
                    }

                }

            }

            HashMap<String, Integer> typeMap = new HashMap<>();
            for (FeatureHolder winner : winners){
                typeMap.merge(winner.term, 1, Integer::sum);

            }

            String predicted = "";
            int maxN = 0;
            for (Map.Entry<String, Integer> typeEntry : typeMap.entrySet()){
                if (typeEntry.getValue() > maxN){
                    maxN = typeEntry.getValue();
                    predicted = typeEntry.getKey();
                }
            }
            System.out.print(featureHolder.vec + featureHolder.term + " ---> ");
            System.out.println(winners);

            if (!predicted.equals(featureHolder.term)){
                System.out.println(ANSI_RED + "Failed prediction: " + predicted + ANSI_RESET);
                failures++;
            }else{
                System.out.println(ANSI_GREEN + "Correct prediction: " + predicted + ANSI_RESET);
            }
            System.out.println(typeMap);
            testSize++;
            System.out.println("Accuracy: " + (1 - (double)failures/testSize) * 100 + "%");
            System.out.println();
        }

    }
    public static List<Double> convertToList(String[] vec){
        List<Double> list = new ArrayList<>();
        for (int i = 0; i < vec.length-1; i++) {
            list.add(Double.parseDouble(vec[i]));
        }
        return list;
    }

    public static double getDistance(List<Double> first, List<Double> second){
        // first.size musi byc rowny second.size
        double distance = 0.0;
        double[] toSquare = new double[first.size()];

        for (int i = 0; i < first.size(); i++) {
            toSquare[i] = ((first.get(i) - second.get(i)) * (first.get(i) - second.get(i)));
        }
        for (Double aDouble : toSquare) {
            distance += aDouble;
        }

        return Math.sqrt(distance);
    }

    public static int getMapSize(HashMap<String, List<List<Double>>> map){
        int size = 0;
        for (Map.Entry<String, List<List<Double>>> entry : map.entrySet()){
            for (List<Double> vec : entry.getValue()){
                size++;
            }
        }
        return size;
    }

}

class FeatureHolder implements Comparable<FeatureHolder>{
    List<Double> vec;
    String term;
    double distance;

    public FeatureHolder(List<Double> vec, String term) {
        this.vec = vec;
        this.term = term;
    }

    public FeatureHolder(List<Double> vec, String term, Double distance) {
        this.vec = vec;
        this.term = term;
        this.distance = distance;
    }



    @Override
    public int compareTo(FeatureHolder o) {
        return Double.compare(o.distance, distance);
    }

    @Override
    public String toString() {
        return "{" + vec +
                ", " + term +
                ", " + distance + "}";
    }
}


import java.util.ArrayList;
import java.util.Random;

public class App {

    private static int temperature = 100;
    private static int tempStep = 1;
    private static final double P = 0.95;
    private static long seed = 12345;

    private static int[][] job = { { 3, 4, 6, 7 }, { 4, 5, 4, 6 }, { 8, 7, 2, 2 }, { 5, 3, 1, 5 }, { 7, 6, 8, 4 } };
    // a jobok elemei az adott műveletek hosszát jelölik

    /*
     * private static int[] job1 = { 3, 4, 6, 7 };
     * private static int[] job2 = { 4, 5, 4, 6 };
     * private static int[] job3 = { 8, 7, 2, 2 };
     * private static int[] job4 = { 5, 3, 1, 5 };
     * private static int[] job5 = { 7, 6, 8, 4 };
     */

    private static int[] break1 = { 7, 12 }; // a breakek elemei a kezdés és végzés időpontját jelölik
    private static int[] break2 = { 18, 23 };

    private static int[] finalStartPositions = { 0, 0, 0, 0 };

    private static final double K = 1.380649 * (Math.pow(10.0, -23.0));
    private static final double E = 2.7182818284590452353602874713526624977572470936999595749669676277240766303535475945713821785251664274274663919320030599218174135966290435729003342952605956307381323286279434907632338298807531952510190115738341879307021540891499348841675092447614606680822648001684774118537423454424371075390777449920695517027618386062613313845830007520449338265602976067371132007093287091274437470472306969772093101416928368190255151086574637721112523897844250569536967707854499699679468644549059879316368892300987931;

    public static void main(String[] args) throws Exception {
        SimulatedAnnealing();

    }

    public static void SimulatedAnnealing() {
        ArrayList<Integer> remaining_jobList = new ArrayList<Integer>();
        for (int i = 0; i < 5; i++) {
            remaining_jobList.add(i);
        }

        int index = -1;
        int currentFinish = -2;
        int previousFinish = -3;
        boolean good = false;
        int counter = 0;
        int helper = 0;

        index = RandomNumber(remaining_jobList.size());
        previousFinish = SingleJobWalkThrough(remaining_jobList.get(index), finalStartPositions, true);
        helper = remaining_jobList.get(index) + 1;
        System.out.print("Job order: \n" + helper + "\t");
        remaining_jobList.remove(index);

        Task: for (int j = 0; j < 4; j++) {
            good = false;
            counter = 0;
            while (!good) {
                index = RandomNumber(remaining_jobList.size());
                currentFinish = SingleJobWalkThrough(remaining_jobList.get(index), finalStartPositions, false);
                if (currentFinish < previousFinish) {
                    previousFinish = currentFinish;
                    SingleJobWalkThrough(remaining_jobList.get(index), finalStartPositions, true);
                    helper = remaining_jobList.get(index) + 1;
                    System.out.print(helper + "\t");
                    remaining_jobList.remove(index);
                    temperature -= tempStep;
                    good = true;
                } else {
                    if (Probability(previousFinish, currentFinish, temperature) > P) {
                        previousFinish = currentFinish;
                        SingleJobWalkThrough(remaining_jobList.get(index), finalStartPositions, true);
                        helper = remaining_jobList.get(index) + 1;
                        System.out.print(helper + "\t");
                        remaining_jobList.remove(index);
                        temperature -= tempStep;
                        good = true;
                    } else {
                        counter++;
                        temperature += tempStep;
                    }
                }
                if (counter == 5) {
                    break Task;
                }
            }
        }
        if (counter == 5) {
            System.out.println("Couldn't find optimal order! Exiting...");
        } else {
            System.out.println("\nLast job ended at: " + finalStartPositions[finalStartPositions.length - 1]);
        }

    }

    private static int SingleJobWalkThrough(int index, int[] startPos, boolean ok) {// visszaadja, hogy mennyi idő
        int[] startPositions = { 0, 0, 0, 0 }; // alatt végez az adott munka
        for (int i = 0; i < startPos.length; i++) {
            startPositions[i] = startPos[i];
        }

        int position = 0;

        for (int i = 0; i < job[index].length; i++) {
            position = startPositions[i] + job[index][i];
            if (IsItBreakTime(break1, break2, startPositions[i], job[index][i])) {
                if (position <= break2[0]) {
                    startPositions[i] = break1[1];
                    position = startPositions[i] + job[index][i];
                    if (IsItBreakTime(break1, break2, startPositions[i], job[index][i])) {
                        startPositions[i] = break2[1];
                        position = startPositions[i] + job[index][i];
                    }
                } else {
                    startPositions[i] = break2[1];
                    position = startPositions[i] + job[index][i];
                }
            }
            startPositions[i] = position;
            if (i != job[index].length - 1) {
                if (startPositions[i + 1] < startPositions[i]) {
                    startPositions[i + 1] = startPositions[i];
                }
            }
        }
        if (ok) {
            for (int i = 0; i < startPositions.length; i++) {
                finalStartPositions[i] = startPositions[i];
            }
        }

        return startPositions[job[index].length - 1] - startPos[0];
    }

    private static double Probability(int bestValue, int currentValue, int temp) {
        return Math.pow(E, -(bestValue - currentValue) / (K * (double) temp));
    }

    private static int RandomNumber(int limit) {
        Random rand = new Random(seed);
        int random_number = rand.nextInt(limit);
        return random_number;

    }

    private static boolean IsItBreakTime(int[] break1, int[] break2, int currentPosition, int jobLength) {
        int endPosition = currentPosition + jobLength;
        if (currentPosition > break1[0] && currentPosition < break1[1]) {
            return true;
        } else if (currentPosition > break2[0] && currentPosition < break2[1]) {
            return true;
        } else if (endPosition > break1[0] && endPosition < break1[1]) {
            return true;
        } else if (endPosition > break2[0] && endPosition < break2[1]) {
            return true;
        } else if (currentPosition <= break1[0] && endPosition >= break1[1]) {
            return true;
        } else if (currentPosition <= break2[0] && endPosition >= break2[1]) {
            return true;
        } else {
            return false;
        }

    }
}

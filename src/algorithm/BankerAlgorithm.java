package algorithm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.StringTokenizer;

public class BankerAlgorithm {

    private final int numberOfProcesses = 5;
    private final int numberOfResources = 3;
    int[] available;
    int[][] need;
    int[][] max;
    int[][] allocation;
    int[] work;
    boolean[] finish;

    public static void main(String[] args) {
        BankerAlgorithm running = new BankerAlgorithm();
        running.run();
    }
    private int[] processSequence;

    public void readData() {
        available = new int[numberOfResources];
        max = new int[numberOfProcesses][numberOfResources];
        allocation = new int[numberOfProcesses][numberOfResources];
        need = new int[numberOfProcesses][numberOfResources];
        try {
            BufferedReader r = new BufferedReader(new FileReader("data.txt"));
            // read Available Resources
            String line = r.readLine();
            StringTokenizer s = new StringTokenizer(line, ",");
            for (int i = 0; i < 3; i++) {
                available[i] = Integer.parseInt(s.nextToken());
            }
            int i = 0;
            // read max matrix
            while ((line = r.readLine()) != null) {
                s = new StringTokenizer(line, ",");
                for (int j = 0; j < 3; j++) {
                    max[i][j] = Integer.parseInt(s.nextToken());
                }
                i++;
            }
            // read allocation matrix
            i = 0;
            while ((line = r.readLine()) != null) {
                s = new StringTokenizer(line, ",");
                for (int j = 0; j < 3; j++) {
                    allocation[i][j] = Integer.parseInt(s.nextToken());
                }
                i++;
            }
            // compute the need matrix need = max - available
            for (i = 0; i < numberOfProcesses; i++) {
                for (int j = 0; j < numberOfResources; j++) {
                    need[i][j] = max[i][j] - allocation[i][j];
                }
            }

        } catch (IOException | NumberFormatException e) {

        }
    }

    public void run() {
        readData();
        String sequence;
        if (!isSafeState()) {
            System.out.println("System is in Safe State!");
        } else {
            int[] request = new int[4];
            boolean found = false;
            do {
                System.out.println("Enter Your Request:");
                Scanner sc = new Scanner(System.in);
                for (int i = 0; i < 4; i++) {
                    request[i] = sc.nextInt();
                }
                goToStepOne();
                if (checkNeed(request) == false) {
                    System.out.println("Error: the process has exceeded its maximum claim.");
                    continue;
                } else {
                    goToStepTow();
                }
                if (checkAvailable(request) == false) {
                    System.out.format("P%d must wait, since the resources are not available", request[0]);
                    continue;
                } else {
                    goToStepThree(request[0]);
                }
                int[] oldAvailable = new int[numberOfResources];
                int[][] oldAllocation = new int[numberOfProcesses][numberOfResources];
                int[][] oldNeed = new int[numberOfProcesses][numberOfResources];
                System.arraycopy(available, 0, oldAvailable, 0, numberOfResources);
                for (int i = 0; i < numberOfProcesses; i++) {
                    for (int j = 0; j < numberOfResources; j++) {
                        oldAllocation[i][j] = allocation[i][j];
                        oldNeed[i][j] = need[i][j];
                    }
                }
                for (int i = 0; i < numberOfResources; i++) {
                    available[i] = available[i] - request[i + 1];
                }
                for (int i = 0; i < numberOfResources; i++) {
                    allocation[request[0]][i] = allocation[request[0]][i] + request[i + 1];
                }
                for (int i = 0; i < numberOfResources; i++) {
                    need[request[0]][i] = need[request[0]][i] - request[i + 1];
                }
                sequence = goToStepTow();
                if (goToStepFour()) {
                    System.out.format("Process%d is allocated its resources.", request[0]);
                    System.out.println("Safe State Sequence is: <" + sequence + ">");
                } else {
                    System.out.format("Process%d is must wait until it is allocated its resources.", request[0]);
                    System.arraycopy(oldAvailable, 0, available, 0, numberOfResources);
                    for (int i = 0; i < numberOfProcesses; i++) {
                        for (int j = 0; j < numberOfResources; j++) {
                            allocation[i][j] = oldAllocation[i][j];
                            need[i][j] = oldNeed[i][j];
                        }
                    }
                }
            } while (found);
        }
    }

    private boolean checkAvailable(int[] request) {
        for (int i = 0; i < 3; i++) {
            if (available[i] < request[i + 1]) {
                return false;
            }
        }
        return true;
    }

    private boolean checkNeed(int[] request) {
        for (int i = 0; i < 3; i++) {
            if (need[request[0]][i] < request[i + 1]) {
                return false;
            }
        }
        return true;
    }

    public void goToStepOne() {
        // initialize the work vector
        work = new int[numberOfResources];
        System.arraycopy(available, 0, work, 0, numberOfResources);
        // initialize the finiah vector
        finish = new boolean[numberOfProcesses];
        for (int i = 0; i < numberOfProcesses; i++) {
            finish[i] = false;
        }
    }

    public String goToStepTow() {
        StringBuilder build = new StringBuilder();
        boolean found;
        do {
            found = false;
            int i = 0;
            for (; i < numberOfProcesses; i++) {
                if (finish[i] == false) {
                    boolean good = true;
                    for (int j = 0; i < numberOfResources; j++) {
                        if (need[i][j] > work[j]) {
                            good = false;
                            break;
                        }
                    }
                    if (!good) {
                        continue;
                    }
                    found = true;
                    break;
                }
            }
            if (found) {
                goToStepThree(i);
                build.append("p").append(String.valueOf(i)).append(',');
            }
        } while (found);
        return build.toString();
    }

    public void goToStepThree(int index) {
        finish[index] = true;
        for (int k = 0; k < numberOfResources; k++) {
            work[k] += allocation[index][k];
        }
    }

    public boolean goToStepFour() {
        for (int i = 0; i < numberOfProcesses; i++) {
            if (finish[i] == false) {
                return false;
            }
        }
        return true;
    }

    public boolean isSafeState() {
        boolean found;
        do {
            found = false;
            int i = 0;
            for (; i < numberOfProcesses; i++) {
                if (finish[i] == false) {
                    boolean good = true;
                    for (int j = 0; i < numberOfResources; j++) {
                        if (need[i][j] > work[j]) {
                            good = false;
                            break;
                        }
                    }
                    if (!good) {
                        continue;
                    }
                    found = true;
                    break;
                }
            }
            if (found) {
                finish[i] = true;
                for (int j = 0; j < numberOfResources; j++) {
                    work[i] += allocation[i][j];
                }
            }
        } while (found);
        return goToStepFour();
    }
}

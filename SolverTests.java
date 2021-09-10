import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class SolverTests {
    public int N_ROWS = 10;
    public int N_COLS = 10;
    public int N_MINES = 10;
    public MineField mf = new MineField(N_ROWS, N_COLS, N_MINES);

    public static void main(String[] args) throws NoSuchAlgorithmException, TimeoutException, ContradictionException {
        SolverTests solverTests = new SolverTests();
        //solverTests.level1Test(solverTests.N_ROWS, solverTests.N_COLS, solverTests.N_MINES, 100000);
        //solverTests.level2Test(solverTests.N_ROWS, solverTests.N_COLS, solverTests.N_MINES, 10);
        solverTests.level3Test(solverTests.N_ROWS, solverTests.N_COLS, solverTests.N_MINES, 1000);
    }

    public int[][] createBoard(int N_ROWS, int N_COLS) {
        int[][] field = new int[N_ROWS][N_COLS];
        for (int i = 0; i < N_ROWS; i++) {
            for (int j = 0; j < N_COLS; j++) {
                field[i][j] = 10;
            }
        }
        return field;
    }


    public boolean checkIfComplete(int[][] field) {
        int remainingTiles = 0;
        for (int i = 0; i < N_ROWS; i++) {
            for (int j = 0; j < N_COLS; j++) {
                if (field[i][j] == 10 || field[i][j] == 11 || field[i][j] == 13 || field[i][j] == 14) {
                    remainingTiles++;
                }
            }
        }
        return remainingTiles == N_MINES;
    }

    public MineField createMineField(int N_ROWS, int N_COLS, int N_MINES) {
        MineField mf = new MineField(N_ROWS, N_COLS, N_MINES);
        return mf;
    }

    public void level1Test(int N_ROWS, int N_COLS, int N_MINES, int N_Tests) throws NoSuchAlgorithmException {
        int testPassed = 0;
        int cellsInBoard = N_COLS * N_ROWS;
        int testfailed = 0;
        int[][] fieldBefore = new int[N_ROWS][N_COLS];
        int firstTileMinesHit = 0;
        for (int i = 0; i < N_Tests; i++) {
            int iterations = 0;
            int[][] field = createBoard(N_ROWS, N_COLS);
            mf = createMineField(N_ROWS, N_COLS, N_MINES);


            while (!checkIfComplete(field)) {
                if (mf.uncover(0, 0) == -1) {
                    mf.open("hello");
                    System.out.println("First tile mine hit");
                    //testfailed++;
                    firstTileMinesHit++;
                    i--;
                    break;


                }
                field[0][0] = mf.uncover(0, 0);

                for (int j = 0; j < N_ROWS; j++) {
                    for (int k = 0; k < N_COLS; k++) {
                        fieldBefore[j][k] = field[j][k];
                        if (field[j][k] == 0) {
                            //field[j][k]= mf.uncover(j,k);
                            for (int x = j - 1; x <= j + 1; ++x) {
                                for (int y = k - 1; y <= k + 1; ++y) {
                                    if (inField(x, y)) {
                                        field[x][y] = mf.uncover(x, y);

                                    }
                                }
                            }
                        }
                    }
                }

                field = safeCellUncover(level1Solve(level1MineFinder(field)));
                int checker = 0;
                if (checkIfComplete(field)) {
                    testPassed++;
                    break;
                }
                for (int j = 0; j < N_ROWS; j++) {
                    for (int k = 0; k < N_COLS; k++) {
                        if (fieldBefore[j][k] == field[j][k]) {
                            //System.out.println("Something changed");
                            checker++;
                        }
                    }
                }


                if (checker == cellsInBoard) {
                    System.out.println("No progress made");
                    System.out.println("Iterations: " + iterations);
                    testfailed++;
                    break;

                }
                iterations++;
            }

        }
        System.out.println("Tests carried out:" + N_Tests);
        System.out.println("First tile mines hit:" + firstTileMinesHit);
        System.out.println("Tests failed:" + testfailed);
        System.out.println("Tests passed:" + testPassed);
    }

    public int[][] level1MineFinder(int[][] fieldForAnalysis) {
        for (int i = 0; i < N_ROWS; i++) {
            for (int j = 0; j < N_COLS; j++) {
                if (fieldForAnalysis[i][j] >= 1 && fieldForAnalysis[i][j] <= 8) {
                    //System.out.println("Button works");
                    int markedAdjacents = 0;
                    //need search every cell up to where safe move is
                    //ensure adjacents add up to number in cell
                    //if add to value
                    for (int k = i - 1; k <= i + 1; ++k) {
                        for (int l = j - 1; l <= j + 1; ++l) {
                            if (inField(k, l)) {
                                if (fieldForAnalysis[k][l] == 10 || fieldForAnalysis[k][l] == 11 || fieldForAnalysis[k][l] == 14) {
                                    markedAdjacents++;
                                }
                            }
                        }
                    }
                    if (markedAdjacents == fieldForAnalysis[i][j]) {
                        for (int k = i - 1; k <= i + 1; ++k) {
                            for (int l = j - 1; l <= j + 1; ++l) {
                                if (inField(k, l)) {
                                    if (fieldForAnalysis[k][l] == 10) {
                                        fieldForAnalysis[k][l] = 14;
                                    }
                                }
                            }
                        }

                    }
                }

            }
        }
        return fieldForAnalysis;
    }

    public int[][] safeCellUncover(int[][] fieldForAnalysis) {
        for (int i = 0; i < N_ROWS; i++) {
            for (int j = 0; j < N_COLS; j++) {
                if (fieldForAnalysis[i][j] == 13) {
                    if (mf.uncover(i,j) ==-1){
                        return null;
                    }
                    fieldForAnalysis[i][j] = mf.uncover(i, j);

                }
            }
        }
        return fieldForAnalysis;
    }

    public int[][] level1Solve(int[][] fieldForAnalysis) {
        for (int i = 0; i < N_ROWS; i++) {
            for (int j = 0; j < N_COLS; j++) {

                if (fieldForAnalysis[i][j] >= 0 && fieldForAnalysis[i][j] <= 8) {
                    //System.out.println("Button works");
                    int markedAdjacents = 0;
                    //need search every cell up to where safe move is
                    //ensure adjacents add up to number in cell
                    //if add to value
                    for (int k = i - 1; k <= i + 1; ++k) {
                        for (int l = j - 1; l <= j + 1; ++l) {
                            if (inField(k, l)) {
                                if (fieldForAnalysis[k][l] == 11 || fieldForAnalysis[k][l] == 14) {
                                    markedAdjacents++;
                                }
                            }
                        }
                    }
                    if (markedAdjacents == fieldForAnalysis[i][j]) {
                        for (int k = i - 1; k <= i + 1; ++k) {
                            for (int l = j - 1; l <= j + 1; ++l) {
                                if (inField(k, l)) {
                                    if (fieldForAnalysis[k][l] == 10) {
                                        fieldForAnalysis[k][l] = 13;
                                    }
                                }
                            }
                        }


                    }


                }
            }
        }
        return fieldForAnalysis;
    }

    private void uncoverEmptyCells(int cRow, int cCol, int[][] fieldForAnalysis) throws NoSuchAlgorithmException {
        //middle bottom
        for (int i = cRow - 1; i <= cRow + 1; ++i) {
            for (int j = cCol - 1; j <= cCol + 1; ++j) {
                if (inField(i, j) &&
                        (fieldForAnalysis[i][j] == 10 ||
                                fieldForAnalysis[i][j] == 13)
                        && mf.uncover(i, j) == 0) {
                    fieldForAnalysis[i][j] = mf.uncover(i, j);

                    ////

                    if (fieldForAnalysis[i][j] == -1) {
                        fieldForAnalysis[i][j] = 9;
                    }

                    uncoverEmptyCells(i, j, fieldForAnalysis);
                }
                if (inField(i, j) && fieldForAnalysis[cRow][cCol] == 0 && (fieldForAnalysis[i][j] == 10 || fieldForAnalysis[i][j] == 13)) {
                    fieldForAnalysis[i][j] = mf.uncover(i, j);
                    if (fieldForAnalysis[i][j] == -1) {
                        fieldForAnalysis[i][j] = 9;
                    }
                }

            }
        }

    }

    public boolean inField(int i, int j) {
        return i >= 0 && i < N_ROWS && j >= 0 && j < N_COLS;
    }


    public void level2Test(int N_ROWS, int N_COLS, int N_MINES, int N_Tests) throws NoSuchAlgorithmException, TimeoutException, ContradictionException {
        int testPassed = 0;
        int cellsInBoard = N_COLS * N_ROWS;
        int testfailed = 0;
        int[][] fieldBefore = new int[N_ROWS][N_COLS];
        int firstTileMinesHit = 0;
        for (int i = 0; i < N_Tests; i++) {
            int iterations = 0;
            int[][] field = createBoard(N_ROWS, N_COLS);
            mf = createMineField(N_ROWS, N_COLS, N_MINES);



            while (!checkIfComplete(field)) {
                if (mf.uncover(0, 0) == -1) {
                    mf.open("hello");
                    System.out.println("First tile mine hit");
                    //testfailed++;
                    firstTileMinesHit++;
                    i--;
                    break;

                }
                field[0][0] = mf.uncover(0, 0);

                for (int j = 0; j < N_ROWS; j++) {
                    for (int k = 0; k < N_COLS; k++) {
                        fieldBefore[j][k] = field[j][k];
                        if (field[j][k] == 0) {
                            //field[j][k]= mf.uncover(j,k);
                            for (int x = j - 1; x <= j + 1; ++x) {
                                for (int y = k - 1; y <= k + 1; ++y) {
                                    if (inField(x, y)) {
                                        field[x][y] = mf.uncover(x, y);

                                    }
                                }
                            }
                        }
                    }
                }

                field = safeCellUncover(level1Solve(level1MineFinder(field)));
                if(field == null){
                    testfailed++;
                    break;
                }

                safeCellUncover(field);
                int checker = 0;
                if (checkIfComplete(field)) {
                    testPassed++;
                    break;
                }
                for (int j = 0; j < N_ROWS; j++) {
                    for (int k = 0; k < N_COLS; k++) {
                        if (fieldBefore[j][k] == field[j][k]) {

                            //System.out.println("Something changed");
                            checker++;
                        }
                    }
                }


                if (checker == cellsInBoard) {
                    checker = 0;
                    System.out.println("No progress made");
                    System.out.println("Iterations: " + iterations);
                    Map<Integer, Boolean> safeCells = new HashMap<>();
                    SatSolver satSolver = new SatSolver(field, N_COLS, N_ROWS, N_MINES);
                    safeCells = satSolver.getSafeCells();
                    field = safeCellSetter(safeCells, field);
                    for (int j = 0; j < N_ROWS; j++) {
                        for (int k = 0; k < N_COLS; k++) {
                            if (field[j][k]==13) {
                                mf.uncover(j,k);
                            }
                            }
                        }
                    for (int j = 0; j < N_ROWS; j++) {
                        for (int k = 0; k < N_COLS; k++) {
                            if (fieldBefore[j][k] == field[j][k]) {

                                //System.out.println("Something changed");
                                checker++;
                            }
                        }
                    }
                    if (checkIfComplete(field)) {
                        testPassed++;
                        break;
                    }
                    if (checker == cellsInBoard) {
                        testfailed++;
                        break;

                    }
                }
                iterations++;
            }

        }
        System.out.println("Tests carried out:" + N_Tests);
        System.out.println("First tile mines hit:" + firstTileMinesHit);
        System.out.println("Tests failed:" + testfailed);
        System.out.println("Tests passed:" + testPassed);
    }

    public void level3Test(int N_ROWS, int N_COLS, int N_MINES, int N_Tests) throws NoSuchAlgorithmException, TimeoutException, ContradictionException {
        int testPassed = 0;
        int cellsInBoard = N_COLS * N_ROWS;
        int testfailed = 0;
        int[][] fieldBefore = new int[N_ROWS][N_COLS];
        int firstTileMinesHit = 0;
        for (int i = 0; i < N_Tests; i++) {
            int iterations = 0;
            int[][] field = createBoard(N_ROWS, N_COLS);
            mf = createMineField(N_ROWS, N_COLS, N_MINES);


            while (!checkIfComplete(field)) {
                if (mf.uncover(0, 0) == -1) {
                    mf.open("hello");
                    System.out.println("First tile mine hit");
                    //testfailed++;
                    firstTileMinesHit++;
                    i--;
                    break;

                }
                field[0][0] = mf.uncover(0, 0);
                for (int j = 0; j < N_ROWS; j++) {
                    for (int k = 0; k < N_COLS; k++) {
                        fieldBefore[j][k] = field[j][k];
                        if (field[j][k] == 0) {
                            //field[j][k]= mf.uncover(j,k);
                            for (int x = j - 1; x <= j + 1; ++x) {
                                for (int y = k - 1; y <= k + 1; ++y) {
                                    if (inField(x, y)) {
                                        field[x][y] = mf.uncover(x, y);

                                    }
                                }
                            }
                        }
                    }
                }
                if (safeCellUncover(level1Solve(level1MineFinder(field))) == null){
                    testfailed++;
                    break;
                }
                field = safeCellUncover(level1Solve(level1MineFinder(field)));

                if(field==null) {
                    testfailed++;
                    break;
                }
                int checker = 0;
                if (checkIfComplete(field)) {
                    testPassed++;
                    break;
                }
                for (int j = 0; j < N_ROWS; j++) {
                    for (int k = 0; k < N_COLS; k++) {
                        if (fieldBefore[j][k] == field[j][k]) {

                            //System.out.println("Something changed");
                            checker++;
                        }
                    }
                }


                if (checker == cellsInBoard) {
                    checker = 0;
                    System.out.println("No progress made");
                    System.out.println("Iterations: " + iterations);
                    Map<Integer, Boolean> safeCells = new HashMap<>();
                    SatSolver satSolver = new SatSolver(field, N_COLS, N_ROWS, N_MINES);
                    safeCells = satSolver.getSafeCells();
                    field = safeCellSetter(safeCells, field);
                    for (int j = 0; j < N_ROWS; j++) {
                        for (int k = 0; k < N_COLS; k++) {
                            if (fieldBefore[j][k] == field[j][k]) {

                                //System.out.println("Something changed");
                                checker++;
                            }
                        }
                    }
                    if (checkIfComplete(field)) {
                        testPassed++;
                        break;
                    }
                    if (checker == cellsInBoard) {
                        checker = 0;
                        Probability probability = new Probability(field, N_COLS, N_ROWS, N_MINES);
                        Integer cellID = probability.safestCell();
                        if (cellID != null) {
                            int[] address = getCellAddress(cellID);
                            field[address[0]][address[1]] = 13;
                            if(safeCellUncover(field) == null){
                                testfailed++;
                                break;
                            }
                            field = safeCellUncover(field);
                        }
                        for (int j = 0; j < N_ROWS; j++) {
                            for (int k = 0; k < N_COLS; k++) {
                                if (fieldBefore[j][k] == field[j][k]) {

                                    //System.out.println("Something changed");
                                    checker++;
                                }
                            }
                        }
                        if (checkIfComplete(field)) {
                            testPassed++;
                            break;
                        }
                        if (checker == cellsInBoard) {
                            testfailed++;
                            break;
                        }

                    }
                    if (checkIfComplete(field)) {
                        testPassed++;
                        break;
                    }
                }
                iterations++;
            }

        }
        System.out.println("Tests carried out:" + N_Tests);
        System.out.println("First tile mines hit:" + firstTileMinesHit);
        System.out.println("Tests failed:" + testfailed);
        System.out.println("Tests passed:" + testPassed);
    }

    public int[][] safeCellSetter(Map<Integer, Boolean> safeCells, int[][] fieldForAnalysis) {
        //System.out.println(safeCells);
        //System.out.println(safeCells.size());
        for (Map.Entry<Integer, Boolean> cell : safeCells.entrySet()) {
            if (!cell.getValue()) {
                //System.out.println(cell);

                int[] address = getCellAddress(cell.getKey());
                if (fieldForAnalysis[address[0]][address[1]] == 10 || fieldForAnalysis[address[0]][address[1]] == 14) {
                    //System.out.println(address[0]);
                    //System.out.println(address[1]);
                    fieldForAnalysis[address[0]][address[1]] = 13;
                    //mf.uncover(address[0],address[1]);
                    //break;
                }
            } else if (cell.getValue()) {
                int[] address = getCellAddress(cell.getKey());
                if (fieldForAnalysis[address[0]][address[1]] == 10 || fieldForAnalysis[address[0]][address[1]] == 14) {
                    //System.out.println(address[0]);
                    //System.out.println(address[1]);
                    fieldForAnalysis[address[0]][address[1]] = 11;
                    //break;
                }
            }
        }
        return fieldForAnalysis;
    }

    public int[] getCellAddress(int id) {
        int[] address = new int[2];
        int cellID = id < 0 ? id * -1 : id;
        //to get j
        address[1] = (cellID - 1) % N_COLS;
        //to get i
        address[0] = ((cellID - 1) - address[1]) / N_COLS;


        return address;
    }
}

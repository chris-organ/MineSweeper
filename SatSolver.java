import org.sat4j.core.VecInt;
import org.sat4j.pb.SolverFactory;
import org.sat4j.pb.core.PBSolver;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

import java.util.*;

public class SatSolver implements Runnable {

    public int[][] fieldForAnalysis;
    public int N_COLS;
    public int N_ROWS;
    public int minesLeft;
    //public static boolean satSolverSemaphore = false;
    Semaphores semaphores = Semaphores.getInstance();
    IVecInt literals = new VecInt();
    IVecInt coefficients = new VecInt();


    public SatSolver(int[][] fieldForAnalysis, int N_COLS, int N_ROWS, int minesLeft) {
        this.fieldForAnalysis = fieldForAnalysis;
        this.N_COLS = N_COLS;
        this.N_ROWS = N_ROWS;
        this.minesLeft = minesLeft;

    }

    @Override
    public void run() {
        while (semaphores.getLvl2Semaphore()) {
            try {
                System.out.println("running");
                safeCellSetter(getSafeCells());
                System.out.println("SatSolverDone");
                semaphores.setLvl2Semaphore(false);
            } catch (TimeoutException | ContradictionException e) {
                e.printStackTrace();
            }

        }
    }


    //Essentially maxvar constraint
    public void createBoardConstraint(PBSolver solver) throws ContradictionException {
        //Mines must total all cells on board
        for (int i = 0; i < N_COLS; i++) {
            for (int j = 0; j < N_ROWS; j++) {
                literals.push(giveCellID(i, j));
                coefficients.push(1);
            }
        }
        //Create a constraint of at most  literals must be satisfied, takes 2 IVecInt + int of degree
        solver.addAtMost(literals, coefficients, GameWindow.N_MINES);
        //Create a constraint of at least
        solver.addAtLeast(literals, coefficients, GameWindow.N_MINES);
        //Reset constraints for reuse
        literals.clear();
        coefficients.clear();
    }
    //Checks cell is valid
    private boolean inField(int i, int j) {
        return i >= 0 && i < N_ROWS && j >= 0 && j < N_COLS;
    }
//    public int getNumberOfClosedNeighbours(Integer cell){
//        int[] address = getCellAddress((int)cell);
//        int k = address[0];
//        int l = address[1];
//        int numberOfNeighbours = 0;
//        for (int i = k - 1; i <= k + 1; ++i) {
//            for (int j = l - 1; j <= l + 1; ++j) {
//                if (inField(i, j) && !(i == k && j == l) ) {
//                    if(fieldForAnalysis[k][l]==10){
//                        numberOfNeighbours++;
//                    }
//                }
//            }
//
//        }return numberOfNeighbours;
//    }

    public List getNeighbouringCells(int cRow, int cCol) {
        List neighbours = new ArrayList<>();
        for (int i = cRow - 1; i <= cRow + 1; ++i) {
            for (int j = cCol - 1; j <= cCol + 1; ++j) {
                if (inField(i, j) && !(i == cRow && j == cCol)) {
                    neighbours.add(giveCellID(i, j));
                }
            }
        }
        return neighbours;
    }

    // Appears to work as intended
    public void createNeighbourConstraints(PBSolver solver) throws ContradictionException {
        //If cell opened with value greater than 0 then it has neighbours

        List<Integer> neighbours;
        for (int i = 0; i < N_ROWS; i++) {
            for (int j = 0; j < N_COLS; j++) {
                if (fieldForAnalysis[i][j] >= 0 && fieldForAnalysis[i][j] <= 8) {
                    //guarantees value of zero for open numbered cells
                    literals.push(giveCellID(i, j));
                    coefficients.push(1);
                    solver.addAtMost(literals, coefficients, 0);
                    solver.addAtLeast(literals, coefficients, 0);
                    literals.clear();
                    coefficients.clear();
                    neighbours = getNeighbouringCells(i, j);
                    for (Integer neighbour : neighbours) {
                        literals.push(neighbour);
                        //Must have coefficient of 1 to prevent error
                        coefficients.push(1);
                    }
                    //Neighbours added to the list must sum to value of cell being checked
                    solver.addAtLeast(literals, coefficients, fieldForAnalysis[i][j]);
                    solver.addAtMost(literals, coefficients, fieldForAnalysis[i][j]);
                    literals.clear();
                    coefficients.clear();
                    neighbours.clear();

                }
            }
        }
    }

    //    public HashMap<Integer, Boolean> findMines(PBSolver solver) throws TimeoutException {
//        solver.isSatisfiable();
//    }
    public PBSolver createConstraints() throws ContradictionException {

        PBSolver solver = SolverFactory.newDefault();

        createBoardConstraint(solver);
        createNeighbourConstraints(solver);

        return solver;
    }

//    public int[] solve(PBSolver solver) {
//        return solver.model();
//
//    }

    public Optional<Boolean> isCellUnSafe(PBSolver solver, int cellID, int mine) throws ContradictionException, TimeoutException {
        //boolean unsafe = true;
        literals.push(cellID);
        coefficients.push(1);
        IConstr atMost = null;
        IConstr atLeast = null;
        Optional<Boolean> mineHere = Optional.empty();
        boolean mineTrue;
        mineTrue = mine != 1;
        try {
            //Must add both so cell must be exactly a mine or not
            atLeast = solver.addAtLeast(literals, coefficients, mine);
            atMost = solver.addAtMost(literals, coefficients, mine);
            //If making the uncovered cell a mine stops the solver working we know it must be safe, as the problem should always be solvable
            if (!solver.isSatisfiable()) {
                mineHere = Optional.of(mineTrue);
            }
            //Catch to avoid adding empty constraint, causing loop
        } catch (ContradictionException e) {
            mineHere = Optional.of(mineTrue);
        }
        //Prevents constraint issue when trying to remove null constraint
        if (!(atLeast == null)) {
            solver.removeConstr(atLeast);
        }
        if (!(atMost == null)) {
            solver.removeConstr(atMost);
        }
        literals.clear();
        coefficients.clear();
        return mineHere;
    }

    //Essentially shore cells
    //Appears to work fine
    public ArrayList<Integer> getUnopenedAdjacents() {
        ArrayList openedAdjacents = new ArrayList<>();
        openedAdjacents.clear();
        for (int i = 0; i < N_ROWS; i++) {
            for (int j = 0; j < N_COLS; j++) {
                if (fieldForAnalysis[i][j] == 10
                        || fieldForAnalysis[i][j] == 11
                ) {
                    ArrayList neighbours = (ArrayList) getNeighbouringCells(i, j);
                    for (int m = 0; m < neighbours.size(); m++) {

                        int[] address = getCellAddress((int) neighbours.get(m));
                        int k = address[0];
                        int l = address[1];
                        if (fieldForAnalysis[k][l] >= 0 &&
                                fieldForAnalysis[k][l] <= 8 && !openedAdjacents.contains(giveCellID(i, j))) {
                            openedAdjacents.add(giveCellID(i, j));
                            break;
                        }
                    }
                }
            }

        }
        return openedAdjacents;
    }


    public Map<Integer, Boolean> getSafeCells() throws TimeoutException, ContradictionException {
        PBSolver solver = createConstraints();
        List<Integer> unopenedAdjacents = getUnopenedAdjacents();
        Map<Integer, Boolean> cellIDs = new HashMap<>();
        for (Integer cellID : unopenedAdjacents) {
            //Means both values can be checked at the same time, therefore flags placed also
            for (int mine = 0; mine < 2; mine++) {
                //Puts a mine or not mine inside if confirmed , otherwise null
                Optional<Boolean> mineHere =
                        isCellUnSafe(solver, cellID, mine);
                //Checks if not null to add
                if (mineHere.isPresent()) {
                    cellIDs.put(cellID, mineHere.get());
                    //moves to next step if previous complete
                    break;
                }
            }
        }
        //must reset to prevent solver holding values
        solver.reset();
        return cellIDs;
    }

    public void safeCellSetter(Map<Integer, Boolean> safeCells) {
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
    }

    //works
//    public int giveCellID(int row, int col) {
//        return (row * N_COLS + col) + 1;
//    }
    //Create unique cellID
    public int giveCellID(int row, int col) {
        return (row * N_COLS + col) + 1;
    }

    //works
    public int[] getCellAddress(int id) {
        int[] address = new int[2];

        int cellID = id;
        if (cellID < 0) {
            cellID = id * -1;
        }
        //to get j
        address[1] = (cellID - 1) % N_COLS;
        //to get i
        address[0] = ((cellID - 1) - address[1]) / N_COLS;


        return address;
    }
//    public Integer getCellWithLeastNeighbours(ArrayList<Integer> cells){
//        int neighbours = 8;
//        Integer bestCell = null;
//        for(Integer cell : cells){
//            if(getNumberOfClosedNeighbours(cell)<neighbours){
//                bestCell = cell;
//            }
//        }return bestCell;
//    }


//    public static void main(String[] args) {
//        SatSolver s = new SatSolver();
//        int cellID = s.giveCellID(0,6);
//        int[] cellAd = s.getCellAddress(cellID);
//        int row = cellAd[0];
//        int col = cellAd[1];
//        System.out.println(Arrays.toString(cellAd));
//        System.out.println(cellID);
//        System.out.println(Arrays.toString(s.getCellAddress(cellID)));
//    }


}

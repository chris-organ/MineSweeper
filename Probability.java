
import com.google.common.math.BigIntegerMath;
import org.apache.commons.math3.fraction.BigFraction;
import org.sat4j.core.VecInt;
import org.sat4j.pb.core.PBSolver;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

import java.math.BigInteger;
import java.util.*;

public class Probability extends SatSolver implements Runnable{
    public Probability(int[][] fieldForAnalysis, int N_COLS, int N_ROWS, int minesLeft) {
        super(fieldForAnalysis, N_COLS, N_ROWS, minesLeft);
    }
    public void setSafestCell(Integer safestCellID){
        if(safestCellID != null){
            int[] address = getCellAddress(safestCellID);
            if (fieldForAnalysis[address[0]][address[1]] == 10 || fieldForAnalysis[address[0]][address[1]] == 14) {
                fieldForAnalysis[address[0]][address[1]] = 13;
            }else{
                System.out.println("Mine already hit, cannot find suitable safe cell");
            }

            //break;
        }

    }
    @Override
    public void run() {

        while (semaphores.getLvl2Semaphore()) {
            try {
                System.out.println("running");
                setSafestCell(safestCell());
                System.out.println("Probability search complete");
                //System.out.println("SatSolverDone");
                semaphores.setLvl2Semaphore(false);
            } catch (TimeoutException | ContradictionException e) {
                e.printStackTrace();
            }

        }
    }
    //Working as intended
    public Integer safestCell() throws TimeoutException, ContradictionException {
        Map<Integer, BigFraction> cellProbabilities = getCellProbabilites();
        BigFraction currentSafestValue = BigFraction.ONE;
        Integer safestCell = null;
        for (Map.Entry<Integer, BigFraction> cell : cellProbabilities.entrySet())
        if (cell.getValue().percentageValue()<currentSafestValue.percentageValue()) {
            currentSafestValue = cell.getValue();
            safestCell = cell.getKey();
        }
        if (safestCell == null){
            System.out.println("Cannot find a safe cell, constraint issue has occurred. Perhaps a mine has already been hit?");
        }
        ArrayList<Integer> bestCells = new ArrayList<>();
        for (Integer cell : cellProbabilities.keySet()){
            if(cellProbabilities.get(cell).equals(currentSafestValue)){
                bestCells.add(cell);
            }
        }
        Integer bestCell = null;
        //bestCell = getCellWithLeastNeighbours(bestCells);
        System.out.println("The safest cell is cell: " + safestCell + "\nWith a probability of being a mine of: " + currentSafestValue +
                "\nAnd a percentage chance of: " + currentSafestValue.percentageValue());
        return safestCell;
    }

    public Map<Integer, BigFraction> getCellProbabilites() throws TimeoutException, ContradictionException {
        //Looking for total models in which any given tile can be a mine
        //Use Binomial coeff to find total models
        PBSolver probabilitySolver = createConstraints();
        Map<Integer, BigInteger> totalMinesAtLocation = new HashMap<>();
        Map<Integer, BigFraction> probabilityOfMine = new HashMap<>();
        ArrayList<Integer> shoreCells = getUnopenedAdjacents();
        ArrayList<Integer> seaCells = getCellsInSea();
        BigInteger numberOfPossibleModels = BigInteger.valueOf(0);
        BigInteger modelsToAdd = BigInteger.valueOf(0);
        BigInteger totalMinesInSea = BigInteger.valueOf(0);
        while (probabilitySolver.isSatisfiable()) {
            int shoreMines = 0;
            int[] currentModel = probabilitySolver.model();
            for (Integer shoreCell : shoreCells) {
                for (int i = 0; i < currentModel.length; i++) {

                    if (shoreCell == currentModel[i]){
                        shoreMines++;
                    }
                }
            }
            int minesInSea = GameWindow.N_MINES - shoreMines;
            //System.out.println(minesInSea);

            if(0 < minesInSea && !(seaCells.isEmpty()) && (seaCells.size())>minesInSea){
                modelsToAdd = BigIntegerMath.binomial(seaCells.size(),minesInSea);
            }else{
                modelsToAdd = BigInteger.valueOf(1);
            }
            List<Integer> minesInModelList = new ArrayList<>(currentModel.length);
            for (int cell : currentModel){
                minesInModelList.add(cell);
            }
            numberOfPossibleModels = numberOfPossibleModels.add(modelsToAdd);
            for (Integer shoreCell : shoreCells){
                //Put in all shore cells if not present
                if (!totalMinesAtLocation.containsKey(shoreCell)){
                    totalMinesAtLocation.put(shoreCell, BigInteger.valueOf(0));
                }
                //If shorecell present and contained in model
                if(totalMinesAtLocation.containsKey(shoreCell) && minesInModelList.contains(shoreCell)){
                    BigInteger currentMines = BigInteger.ZERO;
                    currentMines = totalMinesAtLocation.get(shoreCell);
                    //
                BigInteger newMinesAtLocation = currentMines.add(modelsToAdd);
                totalMinesAtLocation.put(shoreCell,newMinesAtLocation);
                }}
            totalMinesInSea = totalMinesInSea.add(modelsToAdd.multiply(BigInteger.valueOf(minesInSea)));
            /////This needs fixing as it only blocks the specific model
            int[] modelToRemove = currentModel;
            //Alter model so all of the shore cells currently mines are not allowed to be mines in the same model
            List<Integer> cellsToBeRemoved = new ArrayList<Integer>();
            for (int i = 0; i < modelToRemove.length; i++) {
                //Because a mine is positive value in the model we can just check to see if it's contained within this
                //int[] and make it negative
                if( shoreCells.contains(modelToRemove[i])){
                    cellsToBeRemoved.add(-modelToRemove[i]);
                }
            }
            int[] cellsToRemoveAsArray = new int[cellsToBeRemoved.size()];
            for (int i = 0; i < cellsToBeRemoved.size(); i++) {
                cellsToRemoveAsArray[i] = cellsToBeRemoved.get(i);
            }
            if (!(cellsToRemoveAsArray.length==0)) {
                IVecInt usedModel = new VecInt(cellsToRemoveAsArray);
                if (usedModel.isEmpty()){
                    break;
                }else
                try {
                    if (!usedModel.isEmpty()) {
                        probabilitySolver.addBlockingClause(usedModel);
                    }
                }catch (ContradictionException e){
                    e.printStackTrace();
                }
            } else{
                break;
            }
        }
        ////Need to use BigFraction as BigInt will return zero
        ///Works fine
        BigInteger cellsInSea = BigInteger.valueOf(seaCells.size());
        //This was a bad idea
//        if (cellsInSea.equals(BigInteger.ZERO)){
//            cellsInSea = BigInteger.ONE;
//        }
        //To ensure models are correct
        if(numberOfPossibleModels.equals(BigInteger.ZERO)){
            numberOfPossibleModels = BigInteger.ONE;
        }
        BigFraction seaCellProb;
        if(totalMinesInSea.equals(BigInteger.ZERO) || cellsInSea.equals(BigInteger.ZERO)){
            seaCellProb = BigFraction.ZERO;
        }else{
            seaCellProb =  new BigFraction(totalMinesInSea,numberOfPossibleModels.multiply(cellsInSea));
        }
        for(Integer seaCell : seaCells){
            probabilityOfMine.put(seaCell,seaCellProb);
        }
        for(Integer shoreCell: totalMinesAtLocation.keySet()){
            probabilityOfMine.put(shoreCell,( new BigFraction(totalMinesAtLocation.get(shoreCell),(numberOfPossibleModels))));
        }
        probabilitySolver.reset();
        return probabilityOfMine;
    }

//    public BigInteger factorial(BigInteger f){
//        if (f.compareTo(BigInteger.ZERO) < 1) {
//            return BigInteger.ONE;
//        }
//        else
//            return(f.multiply(factorial(f.subtract(BigInteger.valueOf(1)))));
//    }
//    public BigInteger binomial(BigInteger n, BigInteger k){
//        return factorial(n).divide(factorial(k).multiply(factorial(n.subtract(k))));
//    }



    public ArrayList getCellsInSea() {
        ArrayList<Integer> cellsInSea = new ArrayList<>();
        for (int i = 0; i < N_ROWS; i++) {
            for (int j = 0; j < N_COLS; j++) {
                if (fieldForAnalysis[i][j] == 10) {
                    ArrayList neighbours = (ArrayList) getNeighbouringCells(i, j);
                    int closedAdjacents = 0;
                    for (int m = 0; m < neighbours.size(); m++) {

                        int[] address = getCellAddress((int) neighbours.get(m));
                        int k = address[0];
                        int l = address[1];
                        if (fieldForAnalysis[k][l] == 10
                                || fieldForAnalysis[k][l] == 11 || fieldForAnalysis[k][l] == 13
                        ) {
                            closedAdjacents++;
                        }
                    }
                    if (neighbours.size() == closedAdjacents) {
                        cellsInSea.add(giveCellID(i, j));
                    }
                }
            }

        }
        return cellsInSea;
    }

}

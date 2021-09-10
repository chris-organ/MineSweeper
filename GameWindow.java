import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Scanner;

public class GameWindow extends JPanel implements Runnable{
    private final int CELL_TYPES = 15;
    private final int CELL_SIZE = 25;
    private final int BOARD_WIDTH = 30 * CELL_SIZE + 1;
    private final int BOARD_HEIGHT = 16 * CELL_SIZE + 1;
    private final JLabel statusbar;
    public final static int N_MINES = 99;
    public static int N_ROWS = 16;
    public static int N_COLS = 30;
    private Image[] img;
    public static int minesLeft = N_MINES;
    private int allCells;
    private int[] coveredField;
    private boolean inGame;
    Semaphores semaphores = Semaphores.getInstance();


    private int[][] fieldForAnalysis;
    private String password;

    private MineField mf = new MineField(N_ROWS, N_COLS, N_MINES);

    JButton assistButton = new JButton("Single-Point!");
    JButton newGameButton = new JButton("New Game");
    JButton iterateButton = new JButton("Click All");
    JButton pBAssistButton = new JButton("Psuedo Boolean");
    JButton threadStopperButton = new JButton("Stop Running Threads");
    JButton probabilityButton = new JButton("Probability");


    public GameWindow(JLabel statusbar, int N_ROWS, int N_COLS) {

        this.statusbar = statusbar;
        initGameWindow(N_ROWS, N_COLS);

    }
    private boolean inField(int i, int j){
        return i>=0 && i<N_ROWS && j>=0 && j<N_COLS;
    }

    private void initGameWindow(int N_ROWS, int N_COLS) {
        setLayout(null);
        setPreferredSize(new Dimension(BOARD_WIDTH+100, BOARD_HEIGHT));
        img = new Image[CELL_TYPES];

        for (int i = 0; i < CELL_TYPES; i++) {

            var path = "src/resources/" + i + ".png";
            img[i] = new ImageIcon(new ImageIcon(path).getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH)).getImage();
        }

        assistButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                level1MineFinder();
                level1Solve();
                repaint();
            }
        });
        newGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newGame(N_ROWS,N_COLS,N_MINES);
                repaint();
            }
        });
        iterateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < N_ROWS; i++) {
                    for (int j = 0; j < N_COLS; j++) {
                        if(fieldForAnalysis[i][j]==-1){
                            System.out.println("Mine hit\nEnter password:");
                            Scanner sc = new Scanner(System.in);
                            String password = sc.nextLine();
                            ////
                            //password = "hello";
                            ////
                            try {
                                mf.open(password);
                            } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
                                noSuchAlgorithmException.printStackTrace();
                            }
                            fieldForAnalysis[i][j]=9;
                            try {
                                mf.open(password);
                            } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
                                noSuchAlgorithmException.printStackTrace();
                            }
                            System.out.println("Iterator Mine hit");
                        }
                        if(fieldForAnalysis[i][j]==13){
                            if (mf.uncover(i,j)==-1){
                                System.out.println("Mine hit\nEnter password:");
                                Scanner sc = new Scanner(System.in);
                                String password = sc.nextLine();
                                try {
                                    mf.open(password);
                                } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
                                    noSuchAlgorithmException.printStackTrace();

                                }
                                fieldForAnalysis[i][j] =9;
                            }else {
                                fieldForAnalysis[i][j] = mf.uncover(i, j);
                            }
                            if(fieldForAnalysis[i][j]==0){
                                try {
                                    uncoverEmptyCells(i,j);
                                } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
                                    noSuchAlgorithmException.printStackTrace();
                                }
                            }
                            if (fieldForAnalysis[i][j]==14){
                                fieldForAnalysis[i][j] = 10;
                            }

                            repaint();
                            //return;
                        }

                    }

                }
            }
        });
        pBAssistButton.addActionListener( new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e){

                    SatSolver satSolver = new SatSolver(fieldForAnalysis,N_COLS,N_ROWS,N_MINES);
                    semaphores.setLvl2Semaphore(true);

                    Thread satThread = new Thread(satSolver);
                    if(!semaphores.getLvl2Semaphore()){
                        satThread.interrupt();
                    }
                    satThread.start();
                    statusbar.setText("Thinking");
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
//                    try {
//                        satSolver.safeCellSetter(satSolver.getSafeCells());
//                    } catch (TimeoutException timeoutException) {
//                        timeoutException.printStackTrace();
//                    } catch (ContradictionException contradictionException) {
//                        contradictionException.printStackTrace();
//                    }
//                    try {
//                        satThread.join();
//                    } catch (InterruptedException interruptedException) {
//                        interruptedException.printStackTrace();
//                    }

//                        try {
//                            Thread.sleep(100);
//                        } catch (InterruptedException interruptedException) {
//                            interruptedException.printStackTrace();
//                        }

                    repaint();
                    int tilesLeft = 0;
                    for (int i = 0; i < N_ROWS; i++) {
                        for (int j = 0; j < N_COLS; j++) {
                            if (fieldForAnalysis[i][j] == 10){
                                tilesLeft++;
                            }
                        }}
                    int mineConfirmedTiles = 0;
                    for (int i = 0; i < N_ROWS; i++) {
                        for (int j = 0; j < N_COLS; j++) {
                            if (fieldForAnalysis[i][j] == 11 || fieldForAnalysis[i][j] == 14){
                                mineConfirmedTiles++;
                            }
                        }}
                    int numberedTiles = 0;
                    for (int i = 0; i < N_ROWS; i++) {
                        for (int j = 0; j < N_COLS; j++) {
                            if (fieldForAnalysis[i][j] >=0 && fieldForAnalysis[i][j] <= 8){
                                numberedTiles++;
                            }
                        }}
//                    System.out.println("Uncovered and Unmodified tiles remaining: " + tilesLeft);
//                    System.out.println("confirmed mines by PBSolver: " + mineConfirmedTiles);
//                    System.out.println("Numbered tiles uncovered: " + numberedTiles);
                    minesLeft = getMinesLeft();
                    statusbar.setText(String.valueOf(minesLeft));

                    //printFieldForAnalysis();
                    //System.out.println(Arrays.toString(pbSolver.model()));

                }
        });
        threadStopperButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                repaint();
                if(semaphores.getLvl2Semaphore()){
                    semaphores.setLvl2Semaphore(false);
                }
            }
        });
        probabilityButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Probability probability = new Probability(fieldForAnalysis,N_COLS,N_ROWS,N_MINES);
                try {
                    minesLeft = getMinesLeft();
                    if (!(minesLeft ==0)) {
                        semaphores.setLvl2Semaphore(true);
                        Thread probThread = new Thread(probability);
                        probThread.start();
                        if(!semaphores.getLvl2Semaphore()){
                            probThread.interrupt();
                        }
                        Thread.sleep(100);
                        repaint();
                    }else{
                        statusbar.setText("You've already uncovered all mines");
                    }
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
        });
        add(assistButton);
        assistButton.setBounds(BOARD_WIDTH,0,150,50);
        add(newGameButton);
        newGameButton.setBounds(BOARD_WIDTH,50,150,50);
        add(iterateButton);
        iterateButton.setBounds(BOARD_WIDTH,100,150,50);
        add(pBAssistButton);
        pBAssistButton.setBounds(BOARD_WIDTH,150,150,50);
        add(threadStopperButton);
        threadStopperButton.setBounds(BOARD_WIDTH,200,150,50);
        add(probabilityButton).setBounds(BOARD_WIDTH,250,150,50);




        addMouseListener(new CellAdapter());
        newGame(N_ROWS, N_COLS, N_MINES);
    }


    private void newGame(int N_ROWS, int N_COLS, int N_MINES) {
        //int cell;
        minesLeft = N_MINES;
        mf = new MineField(N_ROWS, N_COLS, N_MINES);
//        allCells = N_COLS * N_ROWS;
//        coveredField = new int[allCells];
//        for (int i = 0; i < allCells; i++){
//            coveredField[i] = 10;
//        }
        fieldForAnalysis = new int[N_ROWS][N_COLS];
        for (int i = 0; i < N_ROWS; i++) {
            for (int j = 0; j < N_COLS; j++) {
                fieldForAnalysis[i][j] = 10;
            }
        }
        statusbar.setText(Integer.toString(N_MINES));
    }

    private void uncoverEmptyCells(int cRow, int cCol) throws NoSuchAlgorithmException {
        //middle bottom
        for(int i=cRow-1;i<=cRow+1;++i){
            for(int j=cCol-1;j<=cCol+1;++j){
                if(inField(i,j) && (fieldForAnalysis[i][j]==10 || fieldForAnalysis[i][j]==13) && mf.uncover(i,j) == 0 ){
                    fieldForAnalysis[i][j] = mf.uncover(i,j);
                    password = "hello";
                    ////
                    mf.open(password);
                    if (fieldForAnalysis[i][j]==-1){
                        fieldForAnalysis[i][j]=9;
                    }

                    uncoverEmptyCells(i,j);
                }
                if(inField(i,j) && fieldForAnalysis[cRow][cCol]==0 && (fieldForAnalysis[i][j]==10 || fieldForAnalysis[i][j] == 13)){
                    fieldForAnalysis[i][j] = mf.uncover(i,j);
                    if (fieldForAnalysis[i][j]==-1){
                        fieldForAnalysis[i][j]=9;
                    }
                }

            }
        }

        }




    public void level1MineFinder(){
        for (int i = 0; i < N_ROWS; i++) {
            for (int j = 0; j < N_COLS; j++) {
                if (fieldForAnalysis[i][j] >= 1 && fieldForAnalysis[i][j] <= 8 ){
                    //System.out.println("Button works");
                    int markedAdjacents = 0;
                    //need search every cell up to where safe move is
                    //ensure adjacents add up to number in cell
                    //if add to value
                    for (int k = i - 1; k <= i + 1; ++k) {
                        for (int l = j - 1; l <= j + 1; ++l) {
                            if (inField(k,l)){
                                if (fieldForAnalysis[k][l] == 10 || fieldForAnalysis[k][l] == 11 || fieldForAnalysis[k][l] == 14){
                                    markedAdjacents++;
                                }}
                            }
                        }
                    if(markedAdjacents == fieldForAnalysis[i][j]){
                        for (int k = i - 1; k <= i + 1; ++k) {
                            for (int l = j - 1; l <= j + 1; ++l) {
                                if (inField(k,l)){
                                    if (fieldForAnalysis[k][l] ==10){
                                        fieldForAnalysis[k][l] =14;
                                    }
                                }
                            }
                        }
                        repaint();
                    }
            }

        }
    }}
    public int getMinesLeft(){
        int jPanelMinesLeft = N_MINES;
        int coveredTiles = 0;
        for (int i = 0; i < N_ROWS; i++) {
            for (int j = 0; j < N_COLS; j++) {
                if(fieldForAnalysis[i][j]==11 || fieldForAnalysis[i][j] == 9){
                    jPanelMinesLeft--;
                }
            }
        }
        for (int i = 0; i < N_ROWS; i++) {
            for (int j = 0; j < N_COLS; j++) {
                if(fieldForAnalysis[i][j]==10||fieldForAnalysis[i][j]==11){
                    coveredTiles++;
                }
            }

        }
        if (coveredTiles == N_MINES){
            jPanelMinesLeft = 0;
        }
        return jPanelMinesLeft;
    }
    public void printFieldForAnalysis(){
        System.out.println(Arrays.deepToString(fieldForAnalysis));
    }
    public void level1Solve(){
        for (int i = 0; i < N_ROWS; i++){
            for (int j = 0; j < N_COLS; j++) {

                if (fieldForAnalysis[i][j] >= 1 && fieldForAnalysis[i][j] <= 8 ){
                    //System.out.println("Button works");
                    int markedAdjacents = 0;
                    //need search every cell up to where safe move is
                    //ensure adjacents add up to number in cell
                    //if add to value
                    for (int k = i - 1; k <= i + 1; ++k) {
                        for (int l = j - 1; l <= j + 1; ++l) {
                            if (inField(k,l)){
                                if(fieldForAnalysis[k][l] == 11 || fieldForAnalysis[k][l] == 14){
                                    markedAdjacents++;
                                }
                            }}}
                    if(markedAdjacents == fieldForAnalysis[i][j]){
                        for (int k = i - 1; k <= i + 1; ++k) {
                            for (int l = j - 1; l <= j + 1; ++l) {
                        if (inField(k,l)){
                            if(fieldForAnalysis[k][l]==10){
                                fieldForAnalysis[k][l]=13;
                            }
                        }}}
                        repaint();


                    }


                }
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {

        for (int i = 0; i < N_ROWS; i++) {
            for (int j = 0; j < N_COLS; j++) {
                int cellValue = fieldForAnalysis[i][j];

                if (inGame && cellValue == 9) {

                    inGame = false;
                }
                g.drawImage(img[cellValue], (j * CELL_SIZE),
                        (i * CELL_SIZE), this);

            }
        }

    }

    @Override
    public void run() {

    }


    private class CellAdapter extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();

            int cCol = x / CELL_SIZE;
            int cRow = y / CELL_SIZE;

            boolean doRepaint = false;
            minesLeft = N_MINES;
            for (int i = 0; i < N_ROWS; i++) {
                for (int j = 0; j < N_COLS; j++) {
                    if (fieldForAnalysis[i][j] ==11){
                    minesLeft--;
                }}
            }
            statusbar.setText(String.valueOf(getMinesLeft()));
            for (int i = 0; i < N_ROWS; i++) {
                for (int j = 0; j < N_COLS; j++) {
                    if (fieldForAnalysis[i][j]==14){
                        fieldForAnalysis[i][j]=10;
                    }
                }
            }



            if ((x < N_COLS * CELL_SIZE) && (y < N_ROWS * CELL_SIZE)) {

                if (e.getButton() == MouseEvent.BUTTON3) {

                    if (fieldForAnalysis[cRow][cCol] == 10 || fieldForAnalysis[cRow][cCol] == 13 || fieldForAnalysis[cRow][cCol] == 14) {
                        //fieldForAnalysis[cRow][cCol] = mf.uncover(cRow, cCol);
                        fieldForAnalysis[cRow][cCol] = 11;
                        minesLeft--;
                        String msg = Integer.toString(minesLeft);
                        statusbar.setText(msg);
                        doRepaint = true;
                    } else if (fieldForAnalysis[cRow][cCol] == 11) {
                        fieldForAnalysis[cRow][cCol] = 10;
                        minesLeft++;
                        String msg = Integer.toString(minesLeft);
                        statusbar.setText(msg);

                        doRepaint = true;
                    }
                } else if (e.getButton() == MouseEvent.BUTTON1) {
                    if(fieldForAnalysis[cRow][cCol]==10 ||fieldForAnalysis[cRow][cCol]==13||fieldForAnalysis[cRow][cCol]==14){
                    if(mf.uncover(cRow,cCol) == -1){
                        try {
                            System.out.println("Mine hit\nEnter password:");
                            Scanner sc = new Scanner(System.in);
                            String password = sc.nextLine();
                            ////
                            //password = "hello";
                            ////
                            mf.open(password);
                        } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
                            noSuchAlgorithmException.printStackTrace();
                        }
                        fieldForAnalysis[cRow][cCol] = 9;

                        statusbar.setText("Loser");
                    }
                    if (fieldForAnalysis[cRow][cCol] == 10 || fieldForAnalysis[cRow][cCol] == 14 || fieldForAnalysis[cRow][cCol] == 13 && mf.uncover(cRow, cCol) != -1) {
                        fieldForAnalysis[cRow][cCol] = mf.uncover(cRow, cCol);
                        if (fieldForAnalysis[cRow][cCol] == 0) {
                            try {
                                uncoverEmptyCells(cRow, cCol);
                            } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
                                noSuchAlgorithmException.printStackTrace();
                            }
                            repaint();
                        }


                    }}

                        doRepaint = true;
                    }

                }

                if (doRepaint) {
                    repaint();
                }
            }

        }
    }


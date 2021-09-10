public class Semaphores {
    public static boolean lvl2Semaphore = false;
    //public static boolean ready
    public static boolean repaintSemaphore = false;
    private static Semaphores highlander = null;
    public boolean getRepaintSemaphore(){
        return repaintSemaphore;
    }
    public void setRepaintSemaphore(boolean repaintSemaphore){
        Semaphores.repaintSemaphore = repaintSemaphore;
    }

    public boolean getLvl2Semaphore(){
        return lvl2Semaphore;
    }

    public  void setLvl2Semaphore(boolean lvl2Semaphore) {
        Semaphores.lvl2Semaphore = lvl2Semaphore;
    }

    public static Semaphores getInstance()
    {
        if (highlander == null)
            highlander = new Semaphores();

        return highlander;
    }
}

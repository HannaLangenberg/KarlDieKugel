package Game;

/**
 * @author Julian Riedel
 */
public class Stoppuhr {

    protected long start, stop;
    protected boolean active;

    /**
     * Die Methode, um die Stoppuhr zu starten.
     */
    public void starten(){
        if (!active){
            active = true;
            start = System.currentTimeMillis();
        }
    }
    /**
     * Die Methode, um die Stoppuhr zu stoppen.
     */
    public void stoppen(){
        if (active){
            active = false;
            stop = System.currentTimeMillis();
        }
    }
    /**
     * Die Methode, um die Laufzeit zu ermitteln.
     */
    public long getLaufzeit(){
        return (active ? System.currentTimeMillis() : stop) - start;
    }
    /**
     * Die Methode zur Prüfung, ob die Stoppuhr aktiv ist.
     */
    public boolean istAktiv(){
        return active;
    }

}

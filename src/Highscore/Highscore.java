package Highscore;

import java.util.LinkedHashMap;
import DB.DatenbankEintrag;

/**
 * @author Julian Riedel
 */
public class Highscore implements DatenbankEintrag {

    /**
     * Einmaliger Schlüssel, der das Addressieren in der Datenbank erlaubt.
     * Sollte kleiner 0 sein, wenn das Objekt noch nicht in der Datenbank gespeichert ist.
     */
    protected String Spieler;
    protected String Punktzahl;

    /**
     * Erstellt ein Highscore Objekt
     * @param Spieler Der zur zeit spielende Benutzer.
     * @param Punktzahl Die erreichte Punktzahl.
     */
    public Highscore(String Spieler, String Punktzahl) {
        this.Spieler = Spieler;
        this.Punktzahl = Punktzahl;
    }

    /**
     * Mit dem Aufruf der Methode werden die benötigten Schlüssel-Werte zum Abspeichern in die Datenbank generiert.
     * @return Alle Schlüssel-Werte, kann auch leere Strings und Null enthalten.
     */
    @Override
    public LinkedHashMap<String, String> dbSchluesselWerte() {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        map.put("Spieler", getSpieler());
        map.put("Punktzahl", String.valueOf(getPunktzahl()));
        return map;
    }

    public String getPunktzahl() {
        return Punktzahl;
    }

    public String getSpieler() {
        return Spieler;
    }

}
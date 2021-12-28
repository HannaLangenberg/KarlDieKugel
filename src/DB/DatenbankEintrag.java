package DB;

import java.util.LinkedHashMap;

public interface DatenbankEintrag
{
    /**
     * Mit dem Aufruf der Methode werden die benötigten Schlüssel-Werte zum Abspeichern in die Datenbank generiert.
     * @return Alle Schlüssel-Werte, kann auch leere Strings und Null enthalten, als einziges die ID wird evtl leer gelassen, wenn sie kleiner 1 ist. Das Filtern von leeren Einträgen geschieht in der Datenbank.
     */
    public LinkedHashMap<String, String> dbSchluesselWerte();
}
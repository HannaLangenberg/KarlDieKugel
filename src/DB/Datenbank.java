package DB;

import java.io.File;
import java.sql.*;

/**
 * Laden in einer Microsoft Access Datenbank via SQL
 * @author Julian Riedel
 */
public class Datenbank
{
    /** Verbindung zur Datenbank */
    private Connection conn;

    /**
     * Verbindet sich mit der Datenbank am angegebenen Pfad.
     * @param pfad Der Pfad auf der Festplatte oder vlt. auch Server?
     */
    public Datenbank(File pfad)
    {
        this(pfad.getAbsolutePath());
    }
    /**
     * Verbindet sich mit der Datenbank am angegebenen Pfad.
     * @param pfad Der Pfad auf der Festplatte oder vlt. auch Server?
     */
    public Datenbank(String pfad)
    {
        try
        {
            // UCanAccess als Datenbank Treiber laden
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");

            // Verbindung zur Datenbank Datei herstellen
            conn = DriverManager.getConnection("jdbc:ucanaccess://" + pfad);

            System.out.println("Verbindung zur Datenbank hergestellt!");

        } catch (ClassNotFoundException e)
        {
            System.err.println("Konnte UCanAccess nicht laden!");
            e.printStackTrace();
        } catch (SQLException e)
        {
            System.err.println("Fehler beim Öffnen der Access Datei");
            e.printStackTrace();
        }
    }

    /**
     * Die Methode, um Einträge aus der Datenbank auszulesen.
     * @param query Der SQL-Query String.
     * @return Das Ergebniss der Abfrage als Tabelle, um z.B. alle Einträge mit einem Iterator auszulesen.
     */
    public ResultSet dbAbfrage(String query)
    {
        try
        {
            Statement st = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            ResultSet rs = st.executeQuery(query);
            return rs;
        } catch (SQLException e)
        {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Gibt die gesamte Tabelle auf der Konsole aus, eher unübersichtlich...
     * @param tabellenname Der Name der Tabelle, die ausgegeben werden soll.
     */
    public void druckeTabelle(String tabellenname)
    {
        try
        {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM " + tabellenname + " ORDER BY ID ASC"); // ASC = Sortieren
            // von A-Z, DESC
            // sortieren von
            // Z-A
            System.out.println(rs.toString());

            ResultSetMetaData rsmd = rs.getMetaData();
            System.out.println("querying SELECT * FROM " + tabellenname);
            int columnsNumber = rsmd.getColumnCount();
            while (rs.next())
            {
                for (int i = 1; i <= columnsNumber; i++)
                {
                    if (i > 1)
                        System.out.print(",  ");
                    System.out.printf("%-20s", rs.getString(i));
                    // System.out.print(columnValue + " " + rsmd.getColumnName(i));
                }
                System.out.println("");
            }
        } catch (SQLException e)
        {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Gibt die 10 höchsten Einträge, gemessen an der Punktzahl, aus.
     * @param tabellenname Der Name der Tabelle, die ausgegeben werden soll.
     */
    public void druckeTop10(String tabellenname)
    {
        try
        {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM " + tabellenname + " ORDER BY Punktzahl DESC LIMIT 10"); // DESC = Sortieren

            System.out.println(rs.toString());

            ResultSetMetaData rsmd = rs.getMetaData();
            System.out.println("querying SELECT * FROM " + tabellenname);
            int columnsNumber = rsmd.getColumnCount();
            while (rs.next())
            {
                for (int i = 1; i <= columnsNumber; i++)
                {
                    if (i > 1)
                        System.out.print(",  ");
                    System.out.printf("%-20s", rs.getString(i));
                }
                System.out.println("");
            }
        } catch (SQLException e)
        {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Die Methode, um Einträge in diw Datenbank einzutragen.
     * @param tabellenname Der Name der Tabelle, die ausgegeben werden soll.
     * @param spieler Der zur Zeit spielende Benutzer.
     * @param points Die erreichten Punktzahlen.
     * @return Das Ergebniss der Abfrage als Tabelle, um z.B. alle Einträge mit einem Iterator auszulesen.
     */
    public int eintraghinzufügen(String tabellenname, String spieler, int points)
    {
        int updated = -1;

        try
        {
            Statement statement = conn.createStatement();
            updated = statement.executeUpdate("INSERT INTO "+ tabellenname +"(Spieler,Punktzahl) VALUES('"+spieler+"','"+points+"')");
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return updated;
    }
}
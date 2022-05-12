package ch.mobility.mobocpp.ui;

import ch.mobility.mobocpp.kafka.AvroProsumer;
import ch.mobility.ocpp2mob.CSStatusConnected;
import ch.mobility.ocpp2mob.CSStatusConnectedResponse;
import ch.mobility.ocpp2mob.ChargingStateEnum;
import ch.mobility.ocpp2mob.ConnectorStatusEnum;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

@WebServlet("/list")
public class ListServlet extends HttpServlet {

    private static boolean doAddFake = false;
    private boolean isFakeAdded = false;

    private static String UNBEKANNT = "Unbekannt";
    private static int LAST_CONTACT_OK_IN_SECONDS = 5 * 60;
    final static String RED = "#f03";          // F1
    final static String YELLOW_LIGHT = "#fcfc66"; // W1
    final static String YELLOW_DARK = "#d3c70b";  // W2
    final static String BLUE = "#00f";         // O1
    final static String GREEN_DARK = "#06b251";   // O2
    final static String GREEN_LIGHT = "#78f1ad";  // O3

    private static String KeyLadestationBekanntVerbindungBestehtA = "JCME321202921192";
    private static String KeyLadestationBekanntVerbindungBestehtB = "JC310001";
    private static String KeyLadestationBekanntVerbindungBestehtC = "1311222009";

    // Simuliert die Stammdaten zu den Ladestationen
    private static List<CSStammdaten> stammdatenMap = new ArrayList<>();
    private static Map<String, Instant> lastContact = new HashMap<>();
    static {
        if (doAddFake) {
            //stammdatenMap.add(CSStammdaten.of(KeyLadestationOhneStammdaten, "47.5475926,7.5874733", "Basel Hauptbahnhof"));
            stammdatenMap.add(CSStammdaten.of(FakeCSStatusConnected.KeyLadestationBekanntKeineVerbindungLetzerKontaktKleinerN, "47.4458578,9.1400634", "Raiffeisenbank", "9500", "Wil"));
            stammdatenMap.add(CSStammdaten.of(FakeCSStatusConnected.KeyLadestationBekanntKeineVerbindungLetzerKontaktGroesserN, "47.1442198,8.4349035", "Hauptsitz", "6343", "Rotkreuz"));
            stammdatenMap.add(CSStammdaten.of(FakeCSStatusConnected.KeyLadestationBekanntVerbindungBestehtLadestationMeldetFehler, "46.5160055,6.6277126", "Bahnhof", "1003", "Lausanne"));
            stammdatenMap.add(CSStammdaten.of(FakeCSStatusConnected.KeyLadestationBekanntVerbindungBestehtKeinFahrzeugAngeschlossen, "46.1726474,8.7994749", "SBB Locarno", "6600", "Locarno"));
            stammdatenMap.add(CSStammdaten.of(FakeCSStatusConnected.KeyLadestationBekanntVerbindungBestehtFahrzeugAngeschlossenNichtAmLaden, "46.8482,9.5311401", "Jochstrasse", "7000", "Chur"));
            stammdatenMap.add(CSStammdaten.of(FakeCSStatusConnected.KeyLadestationBekanntVerbindungBestehtFahrzeugAngeschlossenAmLaden, "47.3776673,8.5323237", "Europaallee", "8004", "Zurich"));
        }
        stammdatenMap.add(CSStammdaten.of(KeyLadestationBekanntVerbindungBestehtA, "46.9585416,8.3640993", "Bahnhof", "6370", "Stans"));
        stammdatenMap.add(CSStammdaten.of(KeyLadestationBekanntVerbindungBestehtB, "46.8974001,8.2461243", "Raiffeisenbank", "6060", "Sarnen"));
        stammdatenMap.add(CSStammdaten.of(KeyLadestationBekanntVerbindungBestehtC, "46.2937841,7.8794028", "Migros", "3930", "Visp"));
    }

    private AvroProsumer getAvroProsumer() {
        return AvroProsumer.get();
    }

    @Override
    public void init() {
    }

    @Override
    public void destroy() {
        AvroProsumer.get().close();
    }

    private CSStammdaten getStammdaten(String key) {
        for (CSStammdaten csStammdaten : stammdatenMap) {
            if (csStammdaten.getId().equalsIgnoreCase(key)) {
                return csStammdaten;
            }
        }
        return null;
    }

    private CSStatusConnected getStatusConnected(List<CSStatusConnectedResponse> receiveConnected, String id) {
        for (CSStatusConnectedResponse csStatusConnectedResponse : receiveConnected) {
            for (CSStatusConnected csStatusConnected : csStatusConnectedResponse.getCSStatusList()) {
                if (csStatusConnected.getId().equalsIgnoreCase(id)) {
                    return csStatusConnected;
                }
            }
        }
        return null;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        final List<CSStatusConnectedResponse> receiveConnected = getAvroProsumer().getStatusConnected();
        for (CSStatusConnectedResponse csStatusConnectedResponse : receiveConnected) {
            for (CSStatusConnected csStatusConnected : csStatusConnectedResponse.getCSStatusList()) {
                lastContact.put(csStatusConnected.getId(), DateTimeHelper.parse(csStatusConnected.getLastContact()));
//                getAvroProducer().requestStatusForId(str(csStatusConnected.getId()), null, 1);
//                List<CSStatusForIdResponse> receiveDetail = getAvroConsumer().receive(CSStatusForIdResponse.class, 10000, 1);
            }
        }

        if (doAddFake) {
            if (!isFakeAdded) {
                receiveConnected.add(FakeCSStatusConnected.addFakeCS(lastContact));
                isFakeAdded = true;
            }
        }

//        final List<CSStatusConnected> keineStammdaten = new ArrayList<>(); // A1
        for (CSStatusConnectedResponse csStatusConnectedResponse : receiveConnected) {
            for (CSStatusConnected csStatusConnected : csStatusConnectedResponse.getCSStatusList()) {
                final CSStammdaten stammdaten = getStammdaten(csStatusConnected.getId());
                if (stammdaten == null) {
                    stammdatenMap.add(CSStammdaten.of(csStatusConnected.getId(), UNBEKANNT, UNBEKANNT, UNBEKANNT, UNBEKANNT));
//                    keineStammdaten.add(csStatusConnected);
                }
            }
        }

        response.setContentType("text/html;");
        response.getWriter().println("<!DOCTYPE html>");
        response.getWriter().println("<html>");
        response.getWriter().println(getHead());
        response.getWriter().println("<body>");
        response.getWriter().println("<script>");
        response.getWriter().println(getJS());
        response.getWriter().println("</script>");
        response.getWriter().println(getBody(receiveConnected));
        response.getWriter().println("</body>");
        response.getWriter().println("</html>");
    }

//    final static String RED = "#f03";          // F1
//    final static String YELLOW_LIGHT = "#ff9"; // W1
//    final static String YELLOW_DARK = "#ff0";  // W2
//    final static String BLUE = "#00f";         // O1
//    final static String GREEN_DARK = "#0f0";   // O2
//    final static String GREEN_LIGHT = "#0f9";  // O3

//    Blau: Online / Verfügbar, keine Auto angeschlossen
//    Dunkel-Grün: Online / Verfügbar, Auto angeschlossen, nicht am Laden
//    Dunkel-Grün blinkend: Online / Verfügbar, Auto angeschlossen und am Laden
//    Gelb: Problem, Hinweis zum Problem anzeigbar sofern vorhanden
//    Rot: Fehler, Hinweis zum Fehler anzeigbar sofern vorhanden

    private String calcColor(CSStammdaten stammdaten, CSStatusConnected statusConnected) {

        if (stammdaten == null) throw new IllegalArgumentException("Stammdaten darf nicht leer sein");

        final String result;
        final Instant lastContactValue = lastContact.get(stammdaten.getId());
        if (lastContactValue == null) {
            result = RED;
        } else {
            boolean lastContactOverLimit = LastContactHelper.isLastContactOverLimit(LAST_CONTACT_OK_IN_SECONDS, lastContactValue);
            if (lastContactOverLimit) {
                result = RED;
            } else {
                if (statusConnected == null) {
                    result = YELLOW_LIGHT;
                } else {
                    if (ConnectorStatusEnum.Faulted.name().equalsIgnoreCase(statusConnected.getCPConnectorStatus())) {
                        result = YELLOW_DARK;
                    } else if (ConnectorStatusEnum.Reserved.name().equalsIgnoreCase(statusConnected.getCPConnectorStatus())) {
                        result = YELLOW_DARK;
                    } else if (ConnectorStatusEnum.Unavailable.name().equalsIgnoreCase(statusConnected.getCPConnectorStatus())) {
                        result = YELLOW_DARK;
                    } else if (matchConnectorStatus(ConnectorStatusEnum.Available, statusConnected.getCPConnectorStatus())) {
                        result = BLUE;
                    } else if (ConnectorStatusEnum.Occupied.name().equalsIgnoreCase(statusConnected.getCPConnectorStatus())) {
                        if (matchChargingState(ChargingStateEnum.Charging, statusConnected.getCPChargingState())) {
                            result = GREEN_DARK;
                        } else {
                            result = GREEN_LIGHT;
                        }
                    } else {
                        throw new IllegalStateException("stammdaten=" + stammdaten + ", statusConnected=" + statusConnected);
                    }
                }
            }
        }
        return result;
    }

    private boolean matchConnectorStatus(ConnectorStatusEnum wanted, String value) {
        return wanted.name().equalsIgnoreCase(value);
    }

    private boolean matchChargingState(ChargingStateEnum wanted, String value) {
        return wanted.name().equalsIgnoreCase(value);
    }

    private String getHead() {
        return "<head>\n" +
                "    <title>MobOCPP UI</title>\n" +
                "    <link rel=\"stylesheet\" type=\"text/css\" href=\"liststyle.css\">\n" +
                "</head>";
    }

    private String getJS() {
        return "function filterFunction() {\n" +
                "  var input, filter, table, tr, td, i;\n" +
                "  input = document.getElementById(\"filterInput\");\n" +
                "  filter = input.value.toUpperCase();\n" +
                "  table = document.getElementById(\"cstable\");\n" +
                "  var tbody = table.getElementsByTagName(\"tbody\")[0];\n" +
                "  var rows = tbody.getElementsByTagName(\"tr\");\n" +
                "  for (i = 0; i < rows.length; i++) {\n" +
                "    var cells = rows[i].getElementsByTagName(\"td\");\n"  +
                "    var j;\n" +
                "    var rowContainsFilter = false;\n" +
                "    for (j = 0; j < cells.length; j++) {\n" +
                "      if (cells[j]) {\n" +
                "        if (cells[j].innerHTML.toUpperCase().indexOf(filter) > -1) {\n" +
                "           rowContainsFilter = true;\n" +
                "           continue;\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "    \n" +
                "    if (! rowContainsFilter) {\n" +
                "      rows[i].style.display = \"none\";\n" +
                "    } else {\n" +
                "      rows[i].style.display = \"\";\n" +
                "    }\n" +
                "  }\n" +
                "}\n" +
                "function reload() {\n" +
                "  window.location.reload();\n" +
                "}\n" +
                "function clearFilter() {\n" +
                "  input = document.getElementById(\"filterInput\");\n" +
                "  input.value = '';" +
                "  filterFunction();" +
                "}\n" +
                "function sortTable(n) {\n" +
                "  var table, rows, switching, i, x, y, shouldSwitch, dir, switchcount = 0;\n" +
                "  table = document.getElementById(\"cstable\");\n" +
                "  switching = true;\n" +
                "  // Set the sorting direction to ascending:\n" +
                "  dir = \"asc\";\n" +
                "  /* Make a loop that will continue until\n" +
                "  no switching has been done: */\n" +
                "  while (switching) {\n" +
                "    // Start by saying: no switching is done:\n" +
                "    switching = false;\n" +
                "    rows = table.rows;\n" +
                "    /* Loop through all table rows (except the\n" +
                "    first, which contains table headers): */\n" +
                "    for (i = 1; i < (rows.length - 1); i++) {\n" +
                "      // Start by saying there should be no switching:\n" +
                "      shouldSwitch = false;\n" +
                "      /* Get the two elements you want to compare,\n" +
                "      one from current row and one from the next: */\n" +
                "      x = rows[i].getElementsByTagName(\"TD\")[n];\n" +
                "      y = rows[i + 1].getElementsByTagName(\"TD\")[n];\n" +
                "      /* Check if the two rows should switch place,\n" +
                "      based on the direction, asc or desc: */\n" +
                "      if (dir == \"asc\") {\n" +
                "        if (x.innerHTML.toLowerCase() > y.innerHTML.toLowerCase()) {\n" +
                "          // If so, mark as a switch and break the loop:\n" +
                "          shouldSwitch = true;\n" +
                "          break;\n" +
                "        }\n" +
                "      } else if (dir == \"desc\") {\n" +
                "        if (x.innerHTML.toLowerCase() < y.innerHTML.toLowerCase()) {\n" +
                "          // If so, mark as a switch and break the loop:\n" +
                "          shouldSwitch = true;\n" +
                "          break;\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "    if (shouldSwitch) {\n" +
                "      /* If a switch has been marked, make the switch\n" +
                "      and mark that a switch has been done: */\n" +
                "      rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);\n" +
                "      switching = true;\n" +
                "      // Each time a switch is done, increase this count by 1:\n" +
                "      switchcount ++;\n" +
                "    } else {\n" +
                "      /* If no switching has been done AND the direction is \"asc\",\n" +
                "      set the direction to \"desc\" and run the while loop again. */\n" +
                "      if (switchcount == 0 && dir == \"asc\") {\n" +
                "        dir = \"desc\";\n" +
                "        switching = true;\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

//    private String getJS() {
//        return "function filterFunction() {\n" +
//                "  var input, filter, table, tr, td, i, txtValue;\n" +
//                "  input = document.getElementById(\"filterInput\");\n" +
//                "  filter = input.value.toUpperCase();\n" +
//                "  table = document.getElementById(\"cstable\");\n" +
//                "  tr = table.getElementsByTagName(\"tr\");\n" +
//                "  for (i = 0; i < tr.length; i++) {\n" +
//                "    td = tr[i].getElementsByTagName(\"td\")[0];\n" +
//                "    if (td) {\n" +
//                "      txtValue = td.textContent || td.innerText;\n" +
//                "      if (txtValue.toUpperCase().indexOf(filter) > -1) {\n" +
//                "        tr[i].style.display = \"\";\n" +
//                "      } else {\n" +
//                "        tr[i].style.display = \"none\";\n" +
//                "      }\n" +
//                "    }\n" +
//                "  }\n" +
//                "}";
//    }

    private String getTD(String value) {
        return "    <td>" + value + "</td>";
    }

    // https://stackoverflow.com/questions/14607695/flashing-table-row
    private String getTDWithColor(String value, String color) {
        return "    <td style=\"background:" + color + "; color:" + color + ";\">" + value + "</td>";
    }

    private String getTDWithLink(String id, String value) {
        return "    <td><a href=\"detail?id=" + id + "\">" + value + "</a></td>";
    }

    private String getVendor(CSStatusConnected statusConnected) {
        if (statusConnected != null && statusConnected.getVendor() != null) {
            return statusConnected.getVendor();
        }
        return UNBEKANNT;
    }

    private String getModel(CSStatusConnected statusConnected) {
        if (statusConnected != null && statusConnected.getModel() != null) {
            return statusConnected.getModel();
        }
        return UNBEKANNT;
    }

    private String getStatusCS(CSStatusConnected statusConnected) {
        if (statusConnected != null && statusConnected.getCPConnectorStatus() != null) {
            return statusConnected.getCPConnectorStatus().toString();
        }
        return UNBEKANNT;
    }

    private String getStatusLadung(CSStatusConnected statusConnected) {
        if (statusConnected != null && statusConnected.getCPChargingState() != null) {
            return statusConnected.getCPChargingState().toString();
        }
        return UNBEKANNT;
    }

    private String getLetztenKontakt(Instant lastContact) {
        if (lastContact != null) {
            final long sec = getSekundenSeitLetztemKontakt(lastContact);
            return DateTimeHelper.humanReadable(lastContact) + " (" + sec + "s)";
        }
        return UNBEKANNT;
    }
//
//    private long getSekundenSeitLetztemKontakt(CSStatusConnected statusConnected) {
//        if (statusConnected != null && statusConnected.getLastContact() != null) {
//            return DateTimeHelper.getSecondsSince(DateTimeHelper.parse(statusConnected.getLastContact()));
//        }
//        return -1L;
//    }

    private long getSekundenSeitLetztemKontakt(Instant lastContact) {
       return DateTimeHelper.getSecondsSince(lastContact);
    }

    private String getInfolabelText(int anzahl) {
        return "Letzte Aktualisierung: " + DateTimeHelper.humanReadable(Instant.now()) + " - Anzahl: " + anzahl;
    }

    // https://www.w3schools.com/howto/howto_js_filter_table.asp
    // https://www.w3schools.com/css/css_table.asp
    // https://www.digitalocean.com/community/tutorials/how-to-style-a-table-with-css
    private String getBody(List<CSStatusConnectedResponse> receiveConnected) {
        //stammdatenMap
        String result =
                "  <div class=\"controlInput\">" +
                "     <input class=\"filterInput\" type=\"text\" id=\"filterInput\" onkeyup=\"filterFunction()\" placeholder=\"Filter...\" title=\"Filterbegriff eingeben\">" +
                "     <button class=\"button\" onClick=\"clearFilter()\">Filter zur&uuml;cksetzen</button>" +
                "     <button class=\"button\" onClick=\"reload()\">Ansicht Aktualisieren</button>" +
                "     <label class=\"infolabel\">" + getInfolabelText(stammdatenMap.size()) + "</label>" +
                "  </div>" +
                "<table class=”sortable” id=\"cstable\">\n" +
                " <thead>\n" +
                "   <tr>\n" +
                "     <th onclick=\"sortTable(0)\">Kennung</th>\n" +
                "     <th onclick=\"sortTable(1)\">Hersteller</th>\n" +
                "     <th onclick=\"sortTable(2)\">Modell</th>\n" +
                "     <th onclick=\"sortTable(3)\">PLZ</th>\n" +
                "     <th onclick=\"sortTable(4)\">Ort</th>\n" +
                "     <th onclick=\"sortTable(5)\">Standort</th>\n" +
                "     <th onclick=\"sortTable(6)\">Status</th>\n" +
                "     <th onclick=\"sortTable(7)\">Farbe</th>\n" +
                "     <th onclick=\"sortTable(8)\">Laden</th>\n" +
                "     <th onclick=\"sortTable(9)\">Letzter Kontakt</th>\n" +
                "  </tr>\n" +
                " </thead>\n" +
                " <tbody>\n";

        for (CSStammdaten stammdaten : stammdatenMap) {
            final CSStatusConnected statusConnected = getStatusConnected(receiveConnected, stammdaten.getId());
            final Instant lastContactValue = lastContact.get(stammdaten.getId());
            final String id = stammdaten.getId();
            final String color = calcColor(stammdaten, statusConnected);
            result += "   <tr>\n";
            result += getTDWithLink(id, id);
            result += getTD(getVendor(statusConnected));
            result += getTD(getModel(statusConnected));
            result += getTD(stammdaten.getPlz());
            result += getTD(stammdaten.getOrt());
            result += getTD(stammdaten.getName());
            result += getTD(getStatusCS(statusConnected));
//            result += getTDWithColor2(getStatusCS(statusConnected), color);
            result += getTDWithColor(color, color);
            result += getTD(getStatusLadung(statusConnected));
            result += getTD(getLetztenKontakt(lastContactValue));
            result += "   </tr>\n";
        }

        result += " </tbody>\n" +
                "</table>";
        return result;
    }
}

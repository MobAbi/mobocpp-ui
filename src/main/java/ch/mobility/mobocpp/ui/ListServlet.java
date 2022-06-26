package ch.mobility.mobocpp.ui;

import ch.mobility.mobocpp.kafka.AvroProsumer;
import ch.mobility.mobocpp.stammdaten.StammdatenAccessor;
import ch.mobility.mobocpp.stammdaten.StammdatenLadestation;
import ch.mobility.mobocpp.stammdaten.StammdatenStandort;
import ch.mobility.ocpp2mob.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet("/list")
public class ListServlet extends HttpServlet {

    private static long SEC_TAG = 86400;
    private static long SEC_STUNDE = 3600;
    private static long SEC_MINUTE = 60;

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

    // Text-Colors:
    final static String BLACK = "#000000";
    final static String WHITE = "#FFFFFF";

    private static Map<String, Instant> lastContact = new HashMap<>();
    static {
        if (doAddFake) {
            //stammdatenMap.add(CSStammdaten.of(KeyLadestationOhneStammdaten, "47.5475926,7.5874733", "Basel Hauptbahnhof"));
            StammdatenAccessor.get().getStandorte().add(StammdatenStandort.of("001", "Raiffeisenbank", "Bahnhofstrasse 1", "9500", "Wil", "SG", "47.4458578","9.1400634"));
            StammdatenAccessor.get().getStandorte().add(StammdatenStandort.of("002", "Hauptsitz", "Hauptstrasse 33", "6343", "Rotkreuz", "ZG", "47.1442198","8.4349035"));
            StammdatenAccessor.get().getStandorte().add(StammdatenStandort.of("003", "Bahnhof", "Genferstrasse 12", "1003", "Lausanne", "GE", "46.5160055","6.6277126"));
            StammdatenAccessor.get().getStandorte().add(StammdatenStandort.of("004", "SBB Locarno", "Lago Maggiore 44", "6600", "Locarno", "TI", "46.1726474","8.7994749"));
            StammdatenAccessor.get().getStandorte().add(StammdatenStandort.of("005", "Jochstrasse", "Churfirstgasse 2","7000", "Chur","GR", "46.8482","9.5311401"));
            StammdatenAccessor.get().getStandorte().add(StammdatenStandort.of("006", "Europaallee", "Prollstrasse 53","8004", "Zurich", "ZH","47.3776673","8.5323237"));

            StammdatenAccessor.get().getLadestationen().add(StammdatenLadestation.of(FakeCSStatusConnected.KeyLadestationBekanntKeineVerbindungLetzerKontaktKleinerN, "001", ""));
            StammdatenAccessor.get().getLadestationen().add(StammdatenLadestation.of(FakeCSStatusConnected.KeyLadestationBekanntKeineVerbindungLetzerKontaktGroesserN,  "002", ""));
            StammdatenAccessor.get().getLadestationen().add(StammdatenLadestation.of(FakeCSStatusConnected.KeyLadestationBekanntVerbindungBestehtLadestationMeldetFehler,  "003", ""));
            StammdatenAccessor.get().getLadestationen().add(StammdatenLadestation.of(FakeCSStatusConnected.KeyLadestationBekanntVerbindungBestehtKeinFahrzeugAngeschlossen,  "004", ""));
            StammdatenAccessor.get().getLadestationen().add(StammdatenLadestation.of(FakeCSStatusConnected.KeyLadestationBekanntVerbindungBestehtFahrzeugAngeschlossenNichtAmLadenA,  "005", ""));
            StammdatenAccessor.get().getLadestationen().add(StammdatenLadestation.of(FakeCSStatusConnected.KeyLadestationBekanntVerbindungBestehtFahrzeugAngeschlossenNichtAmLadenB,  "005", ""));
            StammdatenAccessor.get().getLadestationen().add(StammdatenLadestation.of(FakeCSStatusConnected.KeyLadestationBekanntVerbindungBestehtFahrzeugAngeschlossenNichtAmLadenC,  "005", ""));
            StammdatenAccessor.get().getLadestationen().add(StammdatenLadestation.of(FakeCSStatusConnected.KeyLadestationBekanntVerbindungBestehtFahrzeugAngeschlossenAmLaden,  "006", ""));
        }
        final String standortIds = StammdatenAccessor.get().getStandorte().stream().map(e -> e.getStandortId()).collect(Collectors.joining(","));
        System.out.println(StammdatenAccessor.get().getStandorte().size()  + " Standorte aus der Stammdatendatei gelesen: " + standortIds);

        final String ladestationIds = StammdatenAccessor.get().getLadestationen().stream().map(e -> e.getLadestationId()).collect(Collectors.joining(","));
        System.out.println(StammdatenAccessor.get().getLadestationen().size()  + " Ladestationen aus der Stammdatendatei gelesen: " + ladestationIds);
    }

    private AvroProsumer getAvroProsumer() {
        return AvroProsumer.get();
    }

    @Override
    public void init() {
        final List<CSRecentlyConnectedResponse> recentlyConnected = getAvroProsumer().getRecentlyConnected(Integer.valueOf(1));
        for (CSRecentlyConnectedResponse csRecentlyConnectedResponse : recentlyConnected) {
            for (CSRecentlyConnected csRecentlyConnected : csRecentlyConnectedResponse.getCSRecentlyList()) {
                lastContact.put(csRecentlyConnected.getId(), DateTimeHelper.parse(csRecentlyConnected.getLastContact()));
                System.out.println("lastContact hinzugefuegt: " + csRecentlyConnected.getId() + " => " + DateTimeHelper.parse(csRecentlyConnected.getLastContact()));
            }
        }
    }

    @Override
    public void destroy() {
        AvroProsumer.get().close();
    }

    private StammdatenLadestation getStammdatenLadestation(String key) {
        for (StammdatenLadestation stammdatenLadestation : StammdatenAccessor.get().getLadestationen()) {
            if (stammdatenLadestation.getLadestationId().equalsIgnoreCase(key)) {
                return stammdatenLadestation;
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
                final StammdatenLadestation stammdaten = getStammdatenLadestation(csStatusConnected.getId());
                if (stammdaten == null) {
                    StammdatenAccessor.get().getLadestationen().add(StammdatenLadestation.of(csStatusConnected.getId(), UNBEKANNT, null));
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

    private interface ColorResult {
        String getBackgroundColor();
        String getTextColor();
        String getDescription();
    }

    private ColorResult calcBackgroundColor(StammdatenLadestation stammdaten, CSStatusConnected statusConnected) {

        if (stammdaten == null) throw new IllegalArgumentException("Stammdaten darf nicht leer sein");

        final String backgroundColor;
        final String textColor;
        final String resultDescription;
        final Instant lastContactValue = lastContact.get(stammdaten.getLadestationId());
        if (lastContactValue == null) {
            backgroundColor = RED;
            textColor = WHITE;
            resultDescription = "Letzter Kontaktzeitpunkt unbekannt";
        } else {
            boolean lastContactOverLimit = LastContactHelper.isLastContactOverLimit(LAST_CONTACT_OK_IN_SECONDS, lastContactValue);
            if (lastContactOverLimit) {
                backgroundColor = RED;
                textColor = WHITE;
                resultDescription = "Letzter Kontakt l&auml;nger als " + LAST_CONTACT_OK_IN_SECONDS + " Sekunden her";
            } else {
                if (statusConnected == null) {
                    backgroundColor = YELLOW_LIGHT;
                    textColor = BLACK;
                    resultDescription = "Unbekannter Verbindungsstatus zur Ladestation";
                } else {
                    if (ConnectorStatusEnum.Faulted.name().equalsIgnoreCase(statusConnected.getCPConnectorStatus())) {
                        backgroundColor = YELLOW_DARK;
                        textColor = WHITE;
                        resultDescription = "Ladestation meldet Problem, siehe Details";
                    } else if (ConnectorStatusEnum.Reserved.name().equalsIgnoreCase(statusConnected.getCPConnectorStatus())) {
                        backgroundColor = YELLOW_DARK;
                        textColor = WHITE;
                        resultDescription = "Ladestation ist reserviert";
                    } else if (ConnectorStatusEnum.Unavailable.name().equalsIgnoreCase(statusConnected.getCPConnectorStatus())) {
                        backgroundColor = YELLOW_DARK;
                        textColor = WHITE;
                        resultDescription = "Ladestation meldet keine Verf&uuml;gbarkeit, siehe Details";
                    } else if (matchConnectorStatus(ConnectorStatusEnum.Available, statusConnected.getCPConnectorStatus())) {
                        backgroundColor = BLUE;
                        textColor = WHITE;
                        resultDescription = "Ladestation frei, kein EV verbunden";
                    } else if (ConnectorStatusEnum.Occupied.name().equalsIgnoreCase(statusConnected.getCPConnectorStatus())) {
                        if (matchChargingState(ChargingStateEnum.Idle, statusConnected.getCPChargingState())) {
                            backgroundColor = BLUE;
                            textColor = WHITE;
                            resultDescription = "Ladestation belegt, kein EV verbunden";
                        } else if (matchChargingState(ChargingStateEnum.EVConnected, statusConnected.getCPChargingState())) {
                            backgroundColor = GREEN_LIGHT;
                            textColor = BLACK;
                            resultDescription = "Ladestation belegt, EV verbunden, kein Ladevorgang aktiv";
                        } else if (matchChargingState(ChargingStateEnum.Charging, statusConnected.getCPChargingState())) {
                            backgroundColor = GREEN_DARK;
                            textColor = WHITE;
                            resultDescription = "Ladestation belegt, EV verbunden, Ladevorgang aktiv";
                        } else if (matchChargingState(ChargingStateEnum.SuspendedEV, statusConnected.getCPChargingState())) {
                            backgroundColor = GREEN_LIGHT;
                            textColor = BLACK;
                            resultDescription = "Ladestation belegt, EV verbunden, Ladevorgang aktiv, angehalten durch EV";
                        } else if (matchChargingState(ChargingStateEnum.SuspendedEVSE, statusConnected.getCPChargingState())) {
                            backgroundColor = GREEN_LIGHT;
                            textColor = BLACK;
                            resultDescription = "Ladestation belegt, EV verbunden, Ladevorgang aktiv, angehalten durch EVSE";
                        } else {
                            throw new IllegalStateException("ChargingStateEnum: stammdaten=" + stammdaten + ", statusConnected=" + statusConnected);
                        }
                    } else {
                        throw new IllegalStateException("ConnectorStatusEnum: stammdaten=" + stammdaten + ", statusConnected=" + statusConnected);
                    }
                }
            }
        }
        return new ColorResult() {
            @Override
            public String getBackgroundColor() {
                return backgroundColor;
            }

            @Override
            public String getTextColor() {
                return textColor;
            }

            @Override
            public String getDescription() {
                return resultDescription;
            }
        };
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
                "    <link rel=\"icon\" href=\"static/images/favicon.ico\" type=\"image/x-icon\" />" +
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
                "function clearFilter() {\n" +
                "  input = document.getElementById(\"filterInput\");\n" +
                "  input.value = '';\n" +
                "  filterFunction();\n" +
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
                "}" +
                "function reload() {\n" +
                "  window.location.reload();\n" +
                "}\n" +
                "window.onload = function() {" +
                "  filterFunction();" +
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
    private String getTDWithColor(String value, ColorResult colorResult) {
        return "    <td title=\"" + colorResult.getDescription() + "\" style=\"background:" + colorResult.getBackgroundColor() + "; color:" + colorResult.getTextColor() + ";\">" + value + "</td>";
    }

    private String getTDWithLink(String ref, String value, String title) {
        return "    <td title=\"" + title + "\"><a href=" + ref + ">" + value + "</a></td>";
//        return "    <td><a href=\"detail?id=" + ref + "\">" + value + "</a></td>";
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
//            final long sec = getSekundenSeitLetztemKontakt(lastContact);
//            return DateTimeHelper.humanReadable(lastContact) + " (" + sec + "s)";
            return DateTimeHelper.humanReadable(lastContact);
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

//    private String getInfolabelText(int anzahl) {
//        return "Letzte Aktualisierung: " + DateTimeHelper.humanReadable(Instant.now()) + " - Anzahl: " + anzahl;
//    }

    private String getInfolabelText(String msg) {
        return "Letzte Aktualisierung: " + DateTimeHelper.humanReadable(Instant.now()) + " - " + msg;
    }

    private void calcStatus(Map<String, Integer> statusMap, String status) {
        Integer count = statusMap.get(status);
        if (count == null) {
            count = Integer.valueOf(0);
        }
        count = count + 1;
        statusMap.put(status, count);
    }

    // https://www.w3schools.com/howto/howto_js_filter_table.asp
    // https://www.w3schools.com/css/css_table.asp
    // https://www.digitalocean.com/community/tutorials/how-to-style-a-table-with-css
    private String getBody(List<CSStatusConnectedResponse> receiveConnected) {
        Map<String, Integer> statusMap = new HashMap<>();
        String tableBody = "";
        for (StammdatenLadestation stammdatenLadestation : StammdatenAccessor.get().getLadestationen()) {
            final StammdatenStandort stammdatenStandort = StammdatenAccessor.get().getStammdatenStandortForLadestation(stammdatenLadestation);
            final CSStatusConnected statusConnected = getStatusConnected(receiveConnected, stammdatenLadestation.getLadestationId());
            final String status = getStatusCS(statusConnected);
            calcStatus(statusMap, status);
            final Instant lastContactValue = lastContact.get(stammdatenLadestation.getLadestationId());
            final long secSinceLastContact = getSekundenSeitLetztemKontakt(lastContactValue);
            final String secSinceLastContactText = getKontaktstatusText(secSinceLastContact);
            final String id = stammdatenLadestation.getLadestationId();
            final ColorResult colorResult = calcBackgroundColor(stammdatenLadestation, statusConnected);
            final String detailLink = "\"detail?id=" + id + "\"";
            final String googleMapsLink = "\"https://maps.google.com/?q=" + stammdatenStandort.getLatitude() + "," + stammdatenStandort.getLongitude() + "\" target=\"_blank\"";
            tableBody += "   <tr>\n";
            tableBody += getTDWithLink(detailLink, id, "Details zu Ladestation '" + id + "' anzeigen");
            tableBody += getTD(getVendor(statusConnected));
            tableBody += getTD(getModel(statusConnected));
//            tableBody += getTD(stammdatenStandort.getStandortId());
            tableBody += getTDWithLink(googleMapsLink, stammdatenStandort.getStandortId(), "&Ouml;ffne Standort in neuem Google-Maps Fenster");
            tableBody += getTD(stammdatenStandort.getStrasse());
            tableBody += getTD(stammdatenStandort.getPlz());
            tableBody += getTD(stammdatenStandort.getOrt());
//            tableBody += getTD(stammdatenStandort.getKanton());
            tableBody += getTD(stammdatenStandort.getBezeichnung() + stammdatenLadestation.getBezeichnungWithSeparator());
            tableBody += getTD(status);
//            tableBody += getTDWithColor2(getStatusCS(statusConnected), color);
            tableBody += getTD(getStatusLadung(statusConnected));
            tableBody += getTD(getLetztenKontakt(lastContactValue));
            tableBody += getTDWithColor(secSinceLastContactText, colorResult);
            tableBody += "   </tr>\n";
        }
        tableBody += " </tbody>\n" +
                "</table>";

        final SortedSet<String> keys = new TreeSet<>(statusMap.keySet());
        String statusLabel = null;
        for (String key : keys) {
            Integer count = statusMap.get(key);
            if (statusLabel == null) {
                statusLabel = key + ": " + count;
            } else {
                statusLabel += ", " + key + ": " + count;
            }
        }

        String tableHead =
                "  <div class=\"controlInput\">" +
                        "     <input class=\"filterInput\" type=\"text\" id=\"filterInput\" onkeyup=\"filterFunction()\" placeholder=\"Filter...\" title=\"Filterbegriff eingeben\">" +
                        "     <button class=\"button\" onClick=\"clearFilter()\">Filter zur&uuml;cksetzen</button>" +
                        "     <button class=\"button\" onClick=\"reload()\">Ansicht Aktualisieren</button>" +
                        //"     <label class=\"infolabel\">" + getInfolabelText(statusLabel) + "</label>" +
                        "     <label class=\"infolabel\">" + getInfolabelText("Ladestationen: " + StammdatenAccessor.get().getLadestationen().size()) + "</label>" +
                        "  </div>" +
                        "<table class=”sortable” id=\"cstable\">\n" +
                        " <thead>\n" +
                        "   <tr>\n" +
                        "     <th onclick=\"sortTable(0)\">Kennung</th>\n" +
                        "     <th onclick=\"sortTable(1)\">Hersteller</th>\n" +
                        "     <th onclick=\"sortTable(2)\">Modell</th>\n" +
                        "     <th onclick=\"sortTable(3)\">Standort</th>\n" +
                        "     <th onclick=\"sortTable(4)\">Strasse</th>\n" +
                        "     <th onclick=\"sortTable(5)\">PLZ</th>\n" +
                        "     <th onclick=\"sortTable(6)\">Ort</th>\n" +
//                        "     <th onclick=\"sortTable(7)\">KT</th>\n" +
                        "     <th onclick=\"sortTable(7)\">Bezeichnung</th>\n" +
                        "     <th onclick=\"sortTable(8)\">Status</th>\n" +
                        "     <th onclick=\"sortTable(9)\">Laden</th>\n" +
                        "     <th onclick=\"sortTable(10)\">Letzter Kontakt</th>\n" +
                        "     <th onclick=\"sortTable(11)\">Ampel</th>\n" +
                        "  </tr>\n" +
                        " </thead>\n" +
                        " <tbody>\n";

        return tableHead + tableBody;
    }

    private String getKontaktstatusText(long secSinceLastContact) {
        if (secSinceLastContact < 0L) {
            return UNBEKANNT;
        }

        if (secSinceLastContact > SEC_TAG ) {
            return secSinceLastContact/SEC_TAG + " d";
        }

        if (secSinceLastContact > SEC_STUNDE) {
            return secSinceLastContact/SEC_STUNDE + " h";
        }

        if (secSinceLastContact > SEC_MINUTE) {
            return secSinceLastContact/SEC_MINUTE + " min";
        }
        return secSinceLastContact + " s";
    }
}

package ch.mobility.mobocpp.ui;

import ch.mobility.mobocpp.kafka.AvroProsumer;
import ch.mobility.ocpp2mob.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@WebServlet("/detail")
public class DetailServlet extends HttpServlet {

    private static String PARAM_ID = "id";
    private static String NO_ERROR = "NoError";
    private static String NO_INFO = "Status Update";
//    private String PARAM_CONNECTOR_ID = "connectorId";

    private static String UNBEKANNT = "Unbekannt";

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

    private void checkParams(HttpServletRequest request) {
//        if (!request.getParameterMap().containsKey(PARAM_ID) || !request.getParameterMap().containsKey(PARAM_CONNECTOR_ID)) {
//            throw new IllegalArgumentException("Parameter " + PARAM_ID + " and " + PARAM_CONNECTOR_ID + " expected." +
//                    " Received: " + request.getParameterMap().keySet());
//        }
        if (!request.getParameterMap().containsKey(PARAM_ID)) {
            throw new IllegalArgumentException("Parameter " + PARAM_ID + " expected." +
                    " Received: " + request.getParameterMap().keySet());
        }
    }

    private String getInfolabelText() {
        return "Letzte Aktualisierung: " + DateTimeHelper.humanReadable(Instant.now());
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        checkParams(request);
        final String id = request.getParameter(PARAM_ID);
//        final Integer connectorId = Integer.valueOf(request.getParameter(PARAM_CONNECTOR_ID));
        final List<CSStatusForIdResponse> receivedStatusForIdResponse = getAvroProsumer().getStatusForId(id, null, Integer.valueOf(5));
        if (receivedStatusForIdResponse.size() > 1) {
            throw new IllegalStateException("Expected only one Response, received: " + receivedStatusForIdResponse.size());
        }
        final CSStatusForIdResponse csStatusForIdResponse = receivedStatusForIdResponse.isEmpty() ? null : receivedStatusForIdResponse.get(0);

        response.setContentType("text/html;");
        response.getWriter().println("<!DOCTYPE html>");
        response.getWriter().println("<html>");
        response.getWriter().println(getHead());
        response.getWriter().println("<body>");
        response.getWriter().println("<script>");
        response.getWriter().println(getJS());
        response.getWriter().println("</script>");
        response.getWriter().println(" <div class=\"top\">");
        response.getWriter().println("   <button class=\"button\" onClick=\"window.location.href = '/list';\"> < Zur&uuml;ck zur &Uuml;bersicht</button>");
        response.getWriter().println(" </div>");
        response.getWriter().println(" <div class=\"maincontrol\">");
        response.getWriter().println("   <button class=\"button\" onClick=\"\">Neustart</button>");
        response.getWriter().println("   <button class=\"button\" onClick=\"\">Kabel freigeben</button>");
        response.getWriter().println("   <button class=\"button\" onClick=\"\">Trigger Statusupdate</button>");
        response.getWriter().println("   <button class=\"button\" onClick=\"reload()\">Ansicht Aktualisieren</button>");
        response.getWriter().println("     <label class=\"infolabel\">" + getInfolabelText() + "</label>");
        response.getWriter().println(" </div>");
        response.getWriter().println(" <div class=\"main\">");
        response.getWriter().println("   <div class=\"mainleft\">");
        response.getWriter().println(getMainLeft(id, csStatusForIdResponse));
        response.getWriter().println("   </div>");
        response.getWriter().println("   <div class=\"mainright\">");
        response.getWriter().println(getMainRight(csStatusForIdResponse));
        response.getWriter().println("   </div>");
        response.getWriter().println(" </div>");
        response.getWriter().println(" <div class=\"bottom\">");
        response.getWriter().println(getBottom(csStatusForIdResponse));
        response.getWriter().println(" </div>");
        response.getWriter().println("</body>");
        response.getWriter().println("</html>");
    }

    private String getHead() {
        return "<head>\n" +
                "    <title>MobOCPP UI</title>\n" +
                "    <link rel=\"stylesheet\" type=\"text/css\" href=\"detailstyle.css\">\n" +
                "</head>";
    }

    private String getJS() {
        return "function reload() {\n" +
               "  window.location.reload();\n" +
               "}\n";
    }

    private String addLine(String key, int value) {
        return addLine(key, String.valueOf(value));
    }

    private String addLine(String key, String value) {
        if (value == null) {
            return key + ": " + UNBEKANNT + "<br>";
        }
        return key + ": " + value + "<br>";
    }

    private String getMainLeft(String id, CSStatusForIdResponse csStatusForIdResponse) {
        String result = "Keine Daten empfangen f&uuml;r " + id;
        if (csStatusForIdResponse != null) {
            if (csStatusForIdResponse.getResponseInfo().getError() != null) {
                result = addLine("Error", csStatusForIdResponse.getResponseInfo().getError());
            } else {
                final CSStatusDetail status = csStatusForIdResponse.getStatus();
                result = addLine("<b>Kennung</b>", status.getId());
                result+= addLine("Backendstatus", status.getBackendStatus().toString());
                result+= addLine("Vendor", status.getVendor());
                result+= addLine("Model", status.getModel());
                result+= addLine("FW-Version", status.getFirmwareversion());
                result+= addLine("OCPP-Version", status.getOCPPVersion());
                result+= addLine("First contact", DateTimeHelper.humanReadable(DateTimeHelper.parse(status.getFirstContact())));
                result+= addLine("Last contact", DateTimeHelper.humanReadable(DateTimeHelper.parse(status.getLastContact())));
                result+= addLine("CS IP Address", status.getIPAddress());
                result+= addLine("Backend IP Adress", csStatusForIdResponse.getResponseInfo().getOCPPBackendId());
            }
        }
        return result;
    }

    private String getMainRight(CSStatusForIdResponse csStatusForIdResponse) {
        String result = "";
        if (csStatusForIdResponse != null) {
            final CSStatusDetail status = csStatusForIdResponse.getStatus();
            for (CPStatus cpStatus : status.getCPStatusList()) {
                if (cpStatus.getConnectorId() != 0) {
                    result += addLine("<b>Connector</b>", cpStatus.getConnectorId());
                    result += addLine("Connector Status", cpStatus.getConnectorStatus().toString());
                    result += addLine("Charging State", cpStatus.getChargingState().toString());
                    result += addLine("Current charged energy", cpStatus.getCurrentChargedEnergy());
                    result += addLine("Current charging Ampere L1", cpStatus.getCurrentChargingAmpereL1());
                    result += addLine("Current charging Ampere L2", cpStatus.getCurrentChargingAmpereL2());
                    result += addLine("Current charging Ampere L3", cpStatus.getCurrentChargingAmpereL3());
                    result += addLine("Error code", (NO_ERROR.equals(cpStatus.getErrorCode()) ? "" : cpStatus.getErrorCode()));
                    result += addLine("Error info", (NO_INFO.equals(cpStatus.getErrorInfo())) ? "" : cpStatus.getErrorInfo());
                }
            }
        }
        return result;
    }

    private String getBottom(CSStatusForIdResponse csStatusForIdResponse) {
        String result = "";
        if (csStatusForIdResponse != null) {
            final CSStatusDetail status = csStatusForIdResponse.getStatus();
            for (CPStatus cpStatus : status.getCPStatusList()) {
                if (cpStatus.getConnectorId() != 0) {
                    if (!cpStatus.getCPStatusHistoryList().isEmpty()) {
                        result += "<br>Status History";
                        for (CPStatusHistoryEntry hentry : cpStatus.getCPStatusHistoryList()) {
                            String line = "Zeitstempel: " + DateTimeHelper.humanReadable(DateTimeHelper.parse(hentry.getTimestamp())) +
                                    ", Connector Status: " + hentry.getConnectorStatus() +
                                    ", Charging State: " + hentry.getChargingState() +
                                    ", Error code: " + (NO_ERROR.equals(hentry.getErrorCode()) ? "" : hentry.getErrorCode()) +
                                    ", Error info: " + (NO_INFO.equals(hentry.getErrorInfo()) ? "" : hentry.getErrorInfo());
                            result += line + "<br>";
                        }
                    }
                    if (!cpStatus.getCPTransactionHistoryList().isEmpty()) {
                        result += "<br>Transakton History";
                        for (CPTransactionHistoryEntry tentry : cpStatus.getCPTransactionHistoryList()) {
                            String line = "Start: " + DateTimeHelper.humanReadable(DateTimeHelper.parse(tentry.getStartTimestamp())) +
                                    ", Stop: " + DateTimeHelper.humanReadable(DateTimeHelper.parse(tentry.getStopTimestamp())) +
                                    ", Startwert: " + tentry.getStartValue() +
                                    ", Stopwert: " + tentry.getStopValue();
                            result += line + "<br>";
                        }
                    }
                }
            }
        }
        return result;
    }
}

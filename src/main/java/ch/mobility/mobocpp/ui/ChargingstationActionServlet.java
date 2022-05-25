package ch.mobility.mobocpp.ui;

import ch.mobility.mobocpp.kafka.AvroProsumer;
import ch.mobility.ocpp2mob.*;
import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/action")
public class ChargingstationActionServlet extends HttpServlet {
    private static String PARAM_ACTIONTYP = "actiontyp";
    private static String PARAM_CS = "cs";
    private static String PARAM_LIMIT = "limit";
    private static String ACTION_RESET = "reset";
    private static String ACTION_UNLOCK = "unlock";
    private static String ACTION_TRIGGERSTATUS = "triggerstatus";
    private static String ACTION_TRIGGERMETER = "triggermeter";
    private static String ACTION_CURRENT = "current";
    private static String ACTION_CHARGE_START = "start";
    private static String ACTION_CHARGE_STOP = "stop";

    private Gson gson = new Gson();

    private static class Result {
        final String status;
        private Result(String status) {
            this.status = status;
        }
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

    private String getError(CSResponse responseInfo) {
        if (responseInfo != null && responseInfo.getError() != null) {
            return responseInfo.getError();
        }
        return null;
    }

    private String getRequestResult(CSRequestResultEnum requestResult) {
        if (requestResult != null) {
            return requestResult.name();
        }
        return null;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        final String actiontyp = request.getParameter(PARAM_ACTIONTYP);
        final String csId = request.getParameter(PARAM_CS);

        PrintWriter out = response.getWriter();
        response.setContentType("application/json");

        final Result result;
        if (actiontyp == null || "".equals(actiontyp) || csId == null || "".equals(csId)) {
            result = new Result("Error: Missing parameter <" + PARAM_ACTIONTYP + "> or <" + PARAM_CS + ">");
        } else {
            final String dt = "";//DateTimeHelper.humanReadable(Instant.now());
            if (ACTION_RESET.equalsIgnoreCase(actiontyp)) {
                final List<CSChangeChargingCurrentResponse> actionResponse = getAvroProsumer().doReset(csId);
                final String prefix = "Neustart" + dt + ": ";
                if (actionResponse.isEmpty()) {
                    result = new Result(prefix + "Keine Daten empfangen");
                } else {
                    final String error = getError(actionResponse.get(0).getResponseInfo());
                    final String reqResult = getRequestResult(actionResponse.get(0).getRequestResult());
                    result = new Result(prefix + (error == null ? reqResult : error));
                }
            } else if (ACTION_UNLOCK.equalsIgnoreCase(actiontyp)) {
                final List<CSUnlockResponse> actionResponse = getAvroProsumer().doUnlock(csId);
                final String prefix = "Kabel freigeben" + dt + ": ";
                if (actionResponse.isEmpty()) {
                    result = new Result(prefix + "Keine Daten empfangen");
                } else {
                    final String error = getError(actionResponse.get(0).getResponseInfo());
                    final String reqResult = getRequestResult(actionResponse.get(0).getRequestResult());
                    result = new Result(prefix + (error == null ? reqResult : error));
                }
            } else if (ACTION_TRIGGERSTATUS.equalsIgnoreCase(actiontyp)) {
                final List<CSTriggerResponse> actionResponse = getAvroProsumer().doTriggerStatus(csId);
                final String prefix = "Statusupdate anfordern" + dt + ": ";
                if (actionResponse.isEmpty()) {
                    result = new Result(prefix + "Keine Daten empfangen");
                } else {
                    final String error = getError(actionResponse.get(0).getResponseInfo());
                    final String reqResult = getRequestResult(actionResponse.get(0).getRequestResult());
                    result = new Result(prefix + (error == null ? reqResult : error));
                }
            } else if (ACTION_TRIGGERMETER.equalsIgnoreCase(actiontyp)) {
                final List<CSTriggerResponse> actionResponse = getAvroProsumer().doTriggerMeterValues(csId);
                final String prefix = "Messwerte anfordern" + dt + ": ";
                if (actionResponse.isEmpty()) {
                    result = new Result(prefix + "Keine Daten empfangen");
                } else {
                    final String error = getError(actionResponse.get(0).getResponseInfo());
                    final String reqResult = getRequestResult(actionResponse.get(0).getRequestResult());
                    result = new Result(prefix + (error == null ? reqResult : error));
                }
            } else if (ACTION_CHARGE_START.equalsIgnoreCase(actiontyp)) {
                final List<CSStartChargingResponse> actionResponse = getAvroProsumer().doStart(csId);
                final String prefix = "Ladevorgang Start" + dt + ": ";
                if (actionResponse.isEmpty()) {
                    result = new Result(prefix + "Keine Daten empfangen");
                } else {
                    final String error = getError(actionResponse.get(0).getResponseInfo());
                    final String reqResult = getRequestResult(actionResponse.get(0).getRequestResult());
                    result = new Result(prefix + (error == null ? reqResult : error));
                }
            } else if (ACTION_CHARGE_STOP.equalsIgnoreCase(actiontyp)) {
                final List<CSStopChargingResponse> actionResponse = getAvroProsumer().doStop(csId);
                final String prefix = "Ladevorgang Stop" + dt + ": ";
                if (actionResponse.isEmpty()) {
                    result = new Result(prefix + "Keine Daten empfangen");
                } else {
                    final String error = getError(actionResponse.get(0).getResponseInfo());
                    final String reqResult = getRequestResult(actionResponse.get(0).getRequestResult());
                    result = new Result(prefix + (error == null ? reqResult : error));
                }
            } else if (ACTION_CURRENT.equalsIgnoreCase(actiontyp)) {
                final String limit = request.getParameter(PARAM_LIMIT);
                if (limit == null || "".equals(limit)) {
                    result = new Result("Error: Missing parameter <" + PARAM_LIMIT + ">");
                } else {
                    final int limitInt = Integer.valueOf(limit);
                    final List<CSChangeChargingCurrentResponse> actionResponse = getAvroProsumer().doProfile(csId, Integer.valueOf(limitInt*10));
                    final String prefix = "Ladestrom setzen " + limitInt + "A" + dt + ": ";
                    if (actionResponse.isEmpty()) {
                        result = new Result(prefix + "Keine Daten empfangen");
                    } else {
                        final String error = getError(actionResponse.get(0).getResponseInfo());
                        final String reqResult = getRequestResult(actionResponse.get(0).getRequestResult());
                        result = new Result(prefix + (error == null ? reqResult : error));
                    }
                }
            } else {
                result = new Result("Error: Invalid parameter value <" + PARAM_ACTIONTYP + ">: " + actiontyp);
            }
        }

        String json = this.gson.toJson(result);
        System.out.println("Sende Antwort: " + json);
        out.print(json);
        out.flush();
    }
}

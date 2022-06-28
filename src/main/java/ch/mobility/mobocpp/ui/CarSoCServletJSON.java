package ch.mobility.mobocpp.ui;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/carsoc2")
public class CarSoCServletJSON extends HttpServlet {

    private Gson gson = new Gson();

//    private AvroProsumer getAvroProsumer() {
//        return AvroProsumer.get();
//    }

    @Override
    public void init() {
    }

    @Override
    public void destroy() {
//        AvroProsumer.get().close();
    }

    public static class Error {

        private String error;

        public Error(String error) {
            this.error = error;
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        out.flush();
    }
}

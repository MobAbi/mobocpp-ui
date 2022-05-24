package ch.mobility.mobocpp.ui;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/modal")
public class ModalServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("text/html;");
        response.getWriter().println("<!DOCTYPE html>");
        response.getWriter().println("<html>");
//        response.getWriter().println(getHeadInline());
        response.getWriter().println(getHeadLink());
        response.getWriter().println("<body>");

         // Trigger/Open The Modal
        response.getWriter().println("<button id=\"myBtn\">Open Modal</button>");
        response.getWriter().println("<div id=\"myModal\" class=\"modal\">");
        response.getWriter().println(" <div class=\"modal-content\">");
        response.getWriter().println("  <span class=\"close\">&times;</span>");
        response.getWriter().println("  <p>Some text in the Modal..</p>");
        response.getWriter().println(" </div>");
        response.getWriter().println("</div>");

        response.getWriter().println("<script>");

        response.getWriter().println("var modal = document.getElementById(\"myModal\");");
        response.getWriter().println("var btn = document.getElementById(\"myBtn\");\n");
        response.getWriter().println("var span = document.getElementsByClassName(\"close\")[0];");
        response.getWriter().println("btn.onclick = function() {");
        response.getWriter().println("  modal.style.display = \"block\";");
        response.getWriter().println("}");
        response.getWriter().println("span.onclick = function() {");
        response.getWriter().println("  modal.style.display = \"none\";");
        response.getWriter().println("}");
        response.getWriter().println("window.onclick = function(event) {");
        response.getWriter().println("  if (event.target == modal) {");
        response.getWriter().println("    modal.style.display = \"none\";");
        response.getWriter().println("  }");
        response.getWriter().println("}");

        response.getWriter().println("</script>");
        response.getWriter().println("</body>");
        response.getWriter().println("</html>");
    }

    private String getHeadLink() {
        return "<head>\n" +
                "<title>Modaltest</title>\n" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"modalstyle.css\">\n" +
                "<link rel=\"icon\" href=\"static/images/favicon.ico\" type=\"image/x-icon\" />" +
                "</head>";
    }

    private String getHeadInline() {
        return "<head>\n" +
                "<title>Modaltest</title>\n" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "<style>" +
                "body {font-family: Arial, Helvetica, sans-serif;}" +
                ".modal {\n" +
                "  display: none; /* Hidden by default */\n" +
                "  position: fixed; /* Stay in place */\n" +
                "  z-index: 1; /* Sit on top */\n" +
                "  padding-top: 100px; /* Location of the box */\n" +
                "  left: 0;\n" +
                "  top: 0;\n" +
                "  width: 100%; /* Full width */\n" +
                "  height: 100%; /* Full height */\n" +
                "  overflow: auto; /* Enable scroll if needed */\n" +
                "  background-color: rgb(0,0,0); /* Fallback color */\n" +
                "  background-color: rgba(0,0,0,0.4); /* Black w/ opacity */\n" +
                "}" +
                ".modal-content {\n" +
                "  background-color: #fefefe;\n" +
                "  margin: auto;\n" +
                "  padding: 20px;\n" +
                "  border: 1px solid #888;\n" +
                "  width: 80%;\n" +
                "}" +
                ".close {\n" +
                "  color: #aaaaaa;\n" +
                "  float: right;\n" +
                "  font-size: 28px;\n" +
                "  font-weight: bold;\n" +
                "}" +
                ".close:hover,\n" +
                ".close:focus {\n" +
                "  color: #000;\n" +
                "  text-decoration: none;\n" +
                "  cursor: pointer;\n" +
                "}" +
                "</style>" +
                "</head>";
    }
}

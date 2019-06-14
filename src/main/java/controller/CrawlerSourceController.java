package controller;

import com.google.gson.Gson;
import com.googlecode.objectify.ObjectifyService;
import entity.CrawlerSource;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;

public class CrawlerSourceController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(CrawlerSourceController.class.getSimpleName());

    static {
        ObjectifyService.register(CrawlerSource.class);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        String body = getBody(req);
        CrawlerSource crawlerSource = new Gson().fromJson(body, CrawlerSource.class);
        crawlerSource.setStatus(CrawlerSource.Status.ACTIVE.getValue());
        LOGGER.info(new Gson().toJson(crawlerSource));
        ofy().save().entity(crawlerSource).now();
        Gson gson = new Gson();
        resp.getWriter().print(gson.toJson(crawlerSource));
    }

    private String getBody(HttpServletRequest request) throws IOException {

        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        return buffer.toString();
    }
}

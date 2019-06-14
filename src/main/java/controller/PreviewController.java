package controller;

import com.google.gson.Gson;
import entity.Article;
import entity.CrawlerSource;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

public class PreviewController extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        String body = getBody(req);

        CrawlerSource sourcePreview = new Gson().fromJson(body, CrawlerSource.class);
        Document dom = Jsoup.connect(sourcePreview.getUrl()).method(Connection.Method.GET).execute().parse();

        Article article = new Article();
        article.setLink(sourcePreview.getUrl());
        article.setTitle(dom.select(sourcePreview.getTitleSelector()).html());
        article.setContent(dom.select(sourcePreview.getContentSelector()).html());

        resp.getWriter().print(new Gson().toJson(article));
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

package cronjob;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.googlecode.objectify.ObjectifyService;
import config.GAEConstant;
import entity.Article;
import entity.CrawlerSource;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;

public class CrawlerController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(CrawlerController.class.getName());

    private static Queue queue = QueueFactory.getQueue(GAEConstant.GAE_QUEUE_NAME);

    static {
        ObjectifyService.register(Article.class);
        ObjectifyService.register(CrawlerSource.class);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<CrawlerSource> sourceList = ofy().load().type(CrawlerSource.class).list();

        for (CrawlerSource source :
                sourceList) {
            Document dom = Jsoup.connect(source.getUrl()).method(Connection.Method.GET).execute().parse();
            for (Element e : dom.select(source.getLinkSelector())) {
                Article existArticle = ofy().load().type(Article.class).id(e.attr("href")).now();
                if (existArticle == null) {
                    Article article = new Article();
                    article.setLink(e.attr("href"));
                    article.setSource(source.getUrl());
                    article.setCreatedAtMLS(Calendar.getInstance().getTimeInMillis());
                    article.setStatus(Article.Status.PENDING.getStatus());
                    ofy().save().entity(article).now();
                    addToQueue(article.getLink());
                }
            }
        }
    }

    private static void addToQueue(String articleId) {
        queue.add(
                TaskOptions.Builder.withMethod(TaskOptions.Method.PULL).payload(articleId));
    }
}
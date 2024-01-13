package searchengine.utils;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

@RequiredArgsConstructor
public class UrlParser extends RecursiveAction {
    private static final Set<String> set = ConcurrentHashMap.newKeySet();
    private static final Random random = new Random();
    private final String url;
    private final Integer siteId;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final String path;


    @Override
    protected void compute() {
        List<ForkJoinTask<Void>> tasks = new ArrayList<>();
        if (set.contains(path)) {
            return;
        }
        set.add(path);
        try {
            Thread.sleep(100 + Math.abs(random.nextInt()) % 50);
            Connection.Response response = Jsoup.connect(url).maxBodySize(0).execute();
            Document document = response.parse();
            Page page = Page.builder().site(getSite()).code(response.statusCode()).content(document.html()).path(path).build();
            pageRepository.save(page);
            Elements select = document.select("a[href]");
            List<Element> filteredLinks = select.stream()
                    .filter(element -> !element.attr("href").contains("#"))
                    .filter(element -> element.attr("href").startsWith("/"))
                    .toList();
            List<String> collect = filteredLinks.stream().map(element -> element.attr("href")).toList();
            collect.forEach(path -> {
                UrlParser task = new UrlParser(url, siteId, pageRepository, siteRepository, path);
                tasks.add(task.fork());
            });
            tasks.forEach(ForkJoinTask::join);
        } catch (Exception e) {
        }
    }

    private Site getSite() {
        return siteRepository.findById(siteId).orElseThrow(() -> new RuntimeException("Site not found"));

    }
}

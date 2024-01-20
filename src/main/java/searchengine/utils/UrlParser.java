package searchengine.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.SiteStatus;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

@RequiredArgsConstructor
@Slf4j
public class UrlParser extends RecursiveAction {
    private static final Random random = new Random();
    private final String url;
    private final Integer siteId;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final String path;
    private final boolean isFirstCall;

    public UrlParser(String url, Integer siteId, PageRepository pageRepository, SiteRepository siteRepository, String path) {
        this(url, siteId, pageRepository, siteRepository, path, true);
    }

    @Override
    protected void compute() {
        List<ForkJoinTask<Void>> tasks = new ArrayList<>();
        log.info(path);
        updateStatusTime();
        log.info("ya tut 0");
        if (exists() || isFailedStatus()) {
            return;
        }
        try {
            log.info("ya tut");
            Thread.sleep(100 + Math.abs(random.nextInt()) % 50);
            Connection.Response response = Jsoup.connect(url)
                    .userAgent("HeliontSearchBot")
                    .referrer("http://www.google.com").maxBodySize(0).execute();
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
                UrlParser task = new UrlParser(url, siteId, pageRepository, siteRepository, path, false);
                tasks.add(task.fork());
            });
            tasks.forEach(ForkJoinTask::join);
            if (isFirstCall) {
                indexed();
            }
        } catch (Exception e) {
            log.error("Indexing error", e);
            failed();
        }
    }

    private Site getSite() {
        return siteRepository.findById(siteId).orElseThrow(() -> new RuntimeException("Site not found"));

    }

    private void updateStatusTime() {
        Site site = getSite();
        log.info("ya tut 3");
        site.setStatusTime(LocalDateTime.now());
        siteRepository.save(site);
    }

    private void indexed() {
        Site site = getSite();
        site.setStatus(SiteStatus.INDEXED);
        siteRepository.save(site);
    }

    private void failed() {
        Site site = getSite();
        site.setStatus(SiteStatus.FAILED);
        site.setLastError("Error indexing page: " + url + path);
        siteRepository.save(site);
    }

    private boolean isFailedStatus() {
        return getSite().getStatus() == SiteStatus.FAILED;
    }

    private boolean exists() {
        return pageRepository.existsBySiteIdAndPath(siteId, path);
    }
}

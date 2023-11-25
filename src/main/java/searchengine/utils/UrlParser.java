//package searchengine.utils;
//
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ForkJoinTask;
//import java.util.concurrent.RecursiveAction;
//import java.util.stream.Collectors;
//
//public class UrlParser extends RecursiveAction {
//    private static final Set<String> set = ConcurrentHashMap.newKeySet();
//    private static final Random random = new Random();
//    private final String url;
//    private final int level;
//
//    public UrlParser(String url) {
//        this(url, 0);
//    }
//
//    public UrlParser(String url, int level) {
//        this.url = url;
//        this.level = level;
//    }

//      @Override
//    protected void compute() {
//        List<String> list = new ArrayList<>();
//        List<ForkJoinTask<List<String>>> tasks = new ArrayList<>();
//        set.add(url.replaceAll("/$", ""));
//        try {
//            Thread.sleep(100 + Math.abs(random.nextInt()) % 50);
//            Document document = Jsoup.connect(url).maxBodySize(0).get();
//            Elements select = document.select("a[href]");
//            List<Element> filteredLinks = select.stream()
//                    .filter(element -> !element.attr("href").contains("#"))
//                    .filter(element -> element.attr("href").startsWith("/"))
//                    .collect(Collectors.toList());
//            List<String> collect = filteredLinks.stream().map(element -> element.attr("abs:href"))
//                    .filter(s -> !set.contains(s.replaceAll("/$", ""))).collect(Collectors.toList());
//            collect.forEach(s -> {
//                set.add(s.replaceAll("/$", ""));
//                UrlParser task = new UrlParser(s, level + 1);
//                tasks.add(task.fork());
//            });
//        } catch (Exception e) {
//        }
//        list.add("\t".repeat(level) + url);
//        tasks.forEach(task -> list.addAll(task.join()));
//        return list;
//    }
//}

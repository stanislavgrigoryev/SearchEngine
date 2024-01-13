package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.SitesList;
import searchengine.dto.indexing.ResultResponse;
import searchengine.model.Site;
import searchengine.model.SiteStatus;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.utils.UrlParser;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IndexingService {
    private final SitesList sitesList;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;


    @Transactional
    public ResultResponse startIndexing() {
        sitesList.sites().forEach(site -> siteRepository.findByUrlIgnoreCase(site.getUrl())
                .ifPresent(siteRepository::delete));
        siteRepository.flush();
        List<Site> siteList = sitesList.sites().stream().map(site -> siteRepository.save(Site.builder()
                .url(site.getUrl())
                .status(SiteStatus.INDEXING)
                .statusTime(LocalDateTime.now())
                .name(site.getName())
                .build())).collect(Collectors.toList());
        siteList.forEach(site -> new UrlParser(site.getUrl(), site.getId(), pageRepository, siteRepository, "/").fork());

        return new ResultResponse(true, null);
    }
}

package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.SitesList;
import searchengine.dto.indexing.ResultResponse;
import searchengine.model.Site;
import searchengine.model.SiteStatus;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class IndexingService {
    private final SitesList sitesList;
    private final SiteRepository siteRepository;

    @Transactional
    public ResultResponse startIndexing() {
        sitesList.sites().forEach(site -> siteRepository.findByUrlIgnoreCase(site.getUrl())
                .ifPresent(siteRepository::delete));
        siteRepository.flush();
        sitesList.sites().forEach(site -> siteRepository.save(Site.builder()
                .url(site.getUrl())
                .status(SiteStatus.INDEXING)
                .statusTime(LocalDateTime.now())
                .name(site.getName())
                .build()));


        return new ResultResponse(true, null);
    }
}

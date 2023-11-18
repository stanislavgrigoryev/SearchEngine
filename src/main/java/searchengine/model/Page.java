package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "page", indexes = @javax.persistence.Index(columnList = "path"), uniqueConstraints = @UniqueConstraint(columnNames = {"site_id", "path"}))
@Getter
@Setter
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT")
    private Integer id;

    @ManyToOne
    @JoinColumn(nullable = false, columnDefinition = "INT")
    private Site site;

    @Column(nullable = false, columnDefinition = "VARCHAR(511)", length = 511)
    private String path;

    @Column(nullable = false, columnDefinition = "INT")
    private Integer code;

    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Page page = (Page) o;
        return Objects.equals(site, page.site) && Objects.equals(path, page.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(site, path);
    }
}

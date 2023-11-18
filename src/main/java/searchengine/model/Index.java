package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "`index`")
@Getter
@Setter
public class Index {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT")
    private Integer id;

    @ManyToOne
    @JoinColumn(nullable = false, columnDefinition = "INT")
    private Page page;

    @ManyToOne
    @JoinColumn(nullable = false, columnDefinition = "INT")
    private Lemma lemma;

    @Column(nullable = false, name = "`rank`", columnDefinition = "FLOAT")
    private Float rank;


}

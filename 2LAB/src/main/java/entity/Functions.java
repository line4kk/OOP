package entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "functions")
public class Functions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "source", nullable = false, length = 50)
    private String source;

    @OneToMany(mappedBy = "function", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FunctionPoints> points = new ArrayList<>();

    @OneToMany(mappedBy = "composite", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CompositeFunctionElements> compositeElements = new ArrayList<>();

    @OneToMany(mappedBy = "function", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CompositeFunctionElements> asElementInComposites = new ArrayList<>();

    public Functions(Users user, String name, String type, String source) {
        this.user = user;
        this.name = name;
        this.type = type;
        this.source = source;
    }
}

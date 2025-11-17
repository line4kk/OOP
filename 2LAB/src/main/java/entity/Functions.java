package entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
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

    public Functions() {}

    public Functions(Users user, String name, String type, String source) {
        this.user = user;
        this.name = name;
        this.type = type;
        this.source = source;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Users getUser() { return user; }
    public void setUser(Users user) { this.user = user; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public List<FunctionPoints> getPoints() { return points; }
    public void setPoints(List<FunctionPoints> points) { this.points = points; }

    public List<CompositeFunctionElements> getCompositeElements() { return compositeElements; }
    public void setCompositeElements(List<CompositeFunctionElements> compositeElements) { this.compositeElements = compositeElements; }

    public List<CompositeFunctionElements> getAsElementInComposites() { return asElementInComposites; }
    public void setAsElementInComposites(List<CompositeFunctionElements> asElementInComposites) { this.asElementInComposites = asElementInComposites; }
}

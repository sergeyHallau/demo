package com.svaps.demo.models;


import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileMeta {

    @Id
    @GeneratedValue(generator="uuid")
    @GenericGenerator(name="uuid", strategy = "uuid2")
    private String id;
    private String path;

    @ManyToOne
    @JoinColumn(name ="owner_id")
    private User owner;

    @ManyToMany
    @JoinTable(
        name = "user_file_sharing",
        joinColumns = @JoinColumn(name="file_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> sharedForUsers;

    public boolean hasAccess(String userId) {
        return owner.getEmail().equals(userId) || sharedForUsers.stream().anyMatch(u -> u.getEmail().equals(userId));
    }

    public FileMeta(String path, User owner) {
        this.path = path;
        this.owner = owner;
    }
}

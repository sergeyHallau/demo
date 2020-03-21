package com.svaps.demo.models;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;
import java.util.stream.Collectors;


@Entity
@Getter
@Setter
public class User {

    @Id
    private String email;
    private String password;

    @OneToMany(mappedBy="owner", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<FileMeta> ownedFiles;

    @ManyToMany
    @JoinTable(
        name = "user_file_sharing",
        joinColumns = @JoinColumn(name="user_id"),
        inverseJoinColumns = @JoinColumn(name = "file_id")
    )
    private Set<FileMeta> sharedFiles;

    public void addSharedFile(FileMeta fileMeta) {
       sharedFiles.add(fileMeta);
    }

    public void addOwnedFile(FileMeta fileMeta) {
       ownedFiles.add(fileMeta);
    }

    public Set<String> getSharedFileIds() {
       return sharedFiles
            .stream()
            .map(FileMeta::getId)
            .collect(Collectors.toSet());
    }

    public Set<String> getOwnedFileIds() {
       return ownedFiles
            .stream()
            .map(FileMeta::getId)
            .collect(Collectors.toSet());
    }

}

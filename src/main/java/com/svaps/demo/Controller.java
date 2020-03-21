package com.svaps.demo;

import com.svaps.demo.models.FileMeta;
import com.svaps.demo.models.ShareRequest;
import com.svaps.demo.models.User;
import com.svaps.demo.models.responses.FileCreatedResponse;
import com.svaps.demo.models.responses.UserFilesResponse;
import com.svaps.demo.repositories.FileMetaRepository;
import com.svaps.demo.repositories.UserRepository;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.Instant;
import java.util.Optional;

import static com.svaps.demo.SecurityConfig.passwordEncoder;

@RestController
@RequestMapping("/api")
public class Controller {

    @Autowired
    UserRepository userRepository;

    @Autowired
    FileMetaRepository fileMetaRepository;

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity register(@RequestBody User userToCreate) {
        userToCreate.setPassword(passwordEncoder().encode(userToCreate.getPassword()));
        userRepository.save(userToCreate);
        return new ResponseEntity(HttpStatus.CREATED);
    }

    @RequestMapping(value = "/file", method = RequestMethod.GET)
    public ResponseEntity<UserFilesResponse> getUserFiles(Principal principal) {
        User user = userRepository.getOne(principal.getName());
        return ResponseEntity.ok(new UserFilesResponse(user.getOwnedFileIds(), user.getSharedFileIds()));
    }

    @RequestMapping(value = "/file/{id}", method = RequestMethod.GET)
    public ResponseEntity getUserFiles(@PathVariable("id") String id, Principal principal) throws IOException {
        Optional<FileMeta> fileMeta = fileMetaRepository.findById(id);

        if(!fileMeta.isPresent())
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        if(!fileMeta.get().hasAccess(principal.getName()))
            return new ResponseEntity(HttpStatus.FORBIDDEN);

        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(Paths.get(fileMeta.get().getPath())));
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/octet-stream"))
            .body(resource);
    }

    @RequestMapping(value = "/file", method = RequestMethod.POST)
    @Transactional
    public ResponseEntity<FileCreatedResponse> uploadFile(@RequestParam("file") MultipartFile file, Principal principal) throws IOException {

        User currentUser = userRepository.getOne(principal.getName());
        val filePath = "/home/hallau/dev/" + Instant.now().toString() + "_" + file.getOriginalFilename();
        file.transferTo(new File(filePath));
        FileMeta fileMeta = fileMetaRepository.save(new FileMeta(filePath, currentUser));

        return new ResponseEntity<>(new FileCreatedResponse(fileMeta.getId()), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/share", method = RequestMethod.POST)
    @Transactional
    public ResponseEntity shareFile(@RequestBody ShareRequest shareRequest, Principal principal) {
        User user = userRepository.getOne(shareRequest.email);
        return fileMetaRepository.findById(shareRequest.fileId).map(fileMeta -> {

            if(!fileMeta.getOwner().getEmail().equals(principal.getName()))
                return new ResponseEntity(HttpStatus.FORBIDDEN);

            user.addSharedFile(fileMeta);
            userRepository.save(user);
            return new ResponseEntity(HttpStatus.OK);
        })
            .orElse(new ResponseEntity(HttpStatus.NOT_FOUND));

    }



}

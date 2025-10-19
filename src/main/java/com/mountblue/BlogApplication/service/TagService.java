package com.mountblue.BlogApplication.service;
import com.mountblue.BlogApplication.entity.Tag;
import com.mountblue.BlogApplication.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TagService {
    private TagRepository tagRepository;

    @Autowired
    public TagService(TagRepository tagRepository){
        this.tagRepository = tagRepository;
    }
    public Optional<Tag> findByName(String name){
        return tagRepository.findByName(name);
    }
}

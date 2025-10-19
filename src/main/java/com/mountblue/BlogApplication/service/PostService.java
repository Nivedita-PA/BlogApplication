package com.mountblue.BlogApplication.service;
import com.mountblue.BlogApplication.Security.CustomUserDetails;
import com.mountblue.BlogApplication.entity.Post;
import com.mountblue.BlogApplication.entity.Tag;
import com.mountblue.BlogApplication.entity.User;
import com.mountblue.BlogApplication.repository.CommentRepository;
import com.mountblue.BlogApplication.repository.PostRepository;
import com.mountblue.BlogApplication.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class PostService{

        private PostRepository postRepository;
        private TagService tagService;
        private CommentRepository commentRepository;
        private UserRepository userRepository;

        @Autowired
        public PostService(PostRepository postRepository, TagService tagService, CommentRepository commentRepository,UserRepository userRepository){
           this.postRepository = postRepository;
           this.tagService = tagService;
           this.commentRepository = commentRepository;
           this.userRepository = userRepository;
        }

        public Post savePost(HttpServletRequest req){
           String title = req.getParameter("title");
           String tags = req.getParameter("tags");
           Set<Tag> tagged = addTag(tags);
           String excerpt = req.getParameter("excerpt");
           String content = req.getParameter("content");
           String author = req.getParameter("author");

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails loggedInUser = (CustomUserDetails) authentication.getPrincipal();
            String username = loggedInUser.getUsername();
            User user = userRepository.findByName(username);
            if (user==null) throw new RuntimeException("User is not present");
            Post p = new Post();
            p.setTitle(title);
            p.setTags(tagged);
            p.setExcerpt(excerpt);
            p.setContent(content);
            if(!user.getName().equalsIgnoreCase(author)) throw new RuntimeException("Author name mismatched for "+user.getName());
            p.setAuthor(author);
            p.setUser(user);
            p.setPublished(true);
            p.setPublishedAt(LocalDateTime.now());

            return postRepository.save(p);
        }

        public Post updatePost(HttpServletRequest req, Long id) {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails loggedInUser = (CustomUserDetails) authentication.getPrincipal();
            String username = loggedInUser.getUsername();
            User user = userRepository.findByName(username);

            Optional<Post> post = postRepository.findById(id);
           String title = req.getParameter("title");
           String tags = req.getParameter("tags");
           Set<Tag> tagged = addTag(tags);
           String excerpt = req.getParameter("excerpt");
           String content = req.getParameter("content");
           String author = req.getParameter("author");
           String publishedAtStr = req.getParameter("publishedAt");
           LocalDateTime publishedDate = null;
            if (publishedAtStr != null && !publishedAtStr.isBlank()) {
                publishedDate = LocalDate.parse(publishedAtStr).atStartOfDay();
            }
            if (post.isPresent()) {
               Post existingPost = post.get();
                if(existingPost.getUser() == null) throw new RuntimeException("User is not there...");
                boolean isAdmin = user.isAdmin();
                boolean isAuthorOfPost = existingPost.getUser().getEmail().equalsIgnoreCase(user.getEmail());
                if (!isAdmin && !isAuthorOfPost) {
                    throw new RuntimeException("You are not authorized to update this post");
                }
               existingPost.setTitle(title);
               existingPost.setTags(tagged);
               existingPost.setExcerpt(excerpt);
               existingPost.setContent(content);
               existingPost.setPublished(true);
               existingPost.setPublishedAt(publishedDate);
               existingPost.setUpdatedAt(LocalDateTime.now());
                if (user.isAdmin() && author != null && !author.isBlank()) {
                    existingPost.setAuthor(author);
                    User u = userRepository.findByName(author);
                    existingPost.setUser(u);
                } else {
                    existingPost.setAuthor(existingPost.getAuthor());
                    existingPost.setUser(existingPost.getUser());
                }
               return postRepository.save(existingPost);
           }
            throw new RuntimeException("No post is present");
        }

        public Post findById(Long id){
           Optional<Post> existingPost = postRepository.findById(id);
           if(existingPost.isPresent()) {
               Post post = existingPost.get();
               return post;
           }
           return null;
        }

        @Transactional
        public void deleteById(Long id){
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails loggedInUser = (CustomUserDetails) authentication.getPrincipal();
            String username = loggedInUser.getUsername();
            User user = userRepository.findByName(username);
            Optional<Post> post = postRepository.findById(id);
           if(!user.isAdmin() && !user.getEmail().equalsIgnoreCase(post.get().getUser().getEmail())) throw new RuntimeException("User unauthorised");

           post.get().getTags().clear();
           commentRepository.deleteByPostId(id);
           postRepository.deleteById(id);
        }

        public Set<Tag> addTag(String t){
            Set<Tag> tagSet = new HashSet<>();
            String[] tags = t.split(",");
            for(String tag : tags){
                String trimmed = tag.trim();
                if(trimmed.isEmpty()) continue;
                Optional<Tag> existingTag = tagService.findByName(trimmed);
                if(existingTag.isPresent()){
                    Tag tg = existingTag.get();
                    tagSet.add(tg);
                }else{
                     Tag tg = new Tag();
                      tg.setName(trimmed);
                      tagSet.add(tg);
                }
            }
            return tagSet;
        }

        public List<Post> findAllPosts(){
           return postRepository.findAll();
        }

        public Page<Post> getAllPosts(Pageable pageable){
           return postRepository.findAll(pageable);
        }

        public Page<Post> getPaginatedPosts(int page, int size) {
            Pageable pageable = PageRequest.of(page, size);
            return getAllPosts(pageable);
        }

        public Page<Post> getPostsByAuthorName(String author, int page, int pageSize){
           Pageable pageable = PageRequest.of(page, pageSize);
           return postRepository.findByAuthorIgnoreCase(author,pageable);
        }

        public Page<Post> getPostsByPublishedDate(LocalDate date, int page, int pageSize){
           if(date==null) throw new RuntimeException("Enter a date");
           LocalDateTime start = date.atStartOfDay();
           LocalDateTime end = date.plusDays(1).atStartOfDay();
           Pageable pageable = PageRequest.of(page, pageSize);
           return postRepository.findByPublishedAtGreaterThanEqualAndPublishedAtLessThan(start,end,pageable);
        }
        public Page<Post> getPostsByTag(String tagName, int page, int pageSize) {
            Pageable pageable = PageRequest.of(page, pageSize);
            return postRepository.findByTags_NameAndIsPublishedTrue(tagName, pageable);
        }

        public Page<Post> getPosts(String author, String tag, LocalDate publishedDate, String search, int page, int size, boolean sortAsc) {
            LocalDateTime startOfDay = null;
            LocalDateTime endOfDay = null;
            if (publishedDate != null) {
                startOfDay = publishedDate.atStartOfDay();
                endOfDay = publishedDate.plusDays(1).atStartOfDay();
            }
            Pageable pageable = PageRequest.of(page, size, sortAsc ? Sort.by("publishedAt").ascending() : Sort.by("publishedAt").descending());
            return postRepository.findByFilters(author, tag, publishedDate, startOfDay, endOfDay, search, pageable);
        }

        public Page<Post> getPostsSortedByDateAsc(int page, int size) {
            Pageable pageable = PageRequest.of(page, size);
            return postRepository.findAllByOrderByPublishedAtAsc(pageable);
        }

        public Page<Post> getPostsSortedByDateDesc(int page, int size) {
            Pageable pageable = PageRequest.of(page, size);
            return postRepository.findAllByOrderByPublishedAtDesc(pageable);
        }

        public Page<Post> getPostsByAuthorsAndTags(Set<String> authors, Set<String> tags, String search, int page, int size) {
            if (authors != null && authors.isEmpty()) authors = null;
            if (tags != null && tags.isEmpty()) tags = null;
            Pageable pageable = PageRequest.of(page, size);
            return postRepository.findByFilters(authors, tags, search, pageable);
        }

        public Set<String> getAllTags(){
           return postRepository.findAllTags();
        }

        public Set<String> getAllAuthors(){
           return postRepository.findAllAuthors();
        }
}

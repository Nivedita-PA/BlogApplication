package com.mountblue.BlogApplication.controller;
import com.mountblue.BlogApplication.entity.Post;
import com.mountblue.BlogApplication.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class PostController {
        private PostService postService;

        @Autowired
        public PostController(PostService postService){
            this.postService = postService;
        }

        @GetMapping("/newPost")
        public String newPost(Model model,Principal principal){
            if(principal==null) model.addAttribute("username", "guest");
            else model.addAttribute("username",principal.getName());
            return "newPost";
        }

        @PostMapping("/post")
        public String createPost(HttpServletRequest req, Model model){
            Post savedPost = postService.savePost(req);
            model.addAttribute("post",savedPost);
            return "published";
        }


        @PostMapping("/updatePost/{id}")
        public String editPost(@PathVariable Long id, Model model, Principal principal){
            Post post = postService.findById(id);
            if(principal==null) model.addAttribute("username", "guest");
            else model.addAttribute("username",principal.getName());
            model.addAttribute("post",post);
            if(principal==null) model.addAttribute("username", "guest");
            else model.addAttribute("username",principal.getName());
            return "editPost";
        }


        @PostMapping("/update")
        public String updatePost(HttpServletRequest req,Long id, Model model){
            Post post = postService.updatePost(req,id);
            model.addAttribute("post", post);
            return "published";
        }

        @DeleteMapping("/deletePost/{id}")
        public String deletePost(@PathVariable Long id, Model model){
            postService.deleteById(id);
            List<Post> postList = postService.findAllPosts();
            model.addAttribute("posts", postList);
            return "redirect:/postsPages";
        }

        @GetMapping("/post/{id}")
        public String showPost(@PathVariable Long id, Model model,Principal principal){
            Post post = postService.findById(id);
            if(principal==null) model.addAttribute("username", "guest");
            else model.addAttribute("username",principal.getName());
            model.addAttribute("post", post);
            return "published";
        }

        @GetMapping("/posts")
        public String getViewAllPosts(Model model){
            List<Post> postList = postService.findAllPosts();
            model.addAttribute("posts", postList);
            return "posts";
        }

        @GetMapping("/postsPages")
        public String getPaginatedPosts(@RequestParam(defaultValue = "0") int page, Model model, Principal principal) {
            Page<Post> postsPage = postService.getPaginatedPosts(page, 10);
            model.addAttribute("posts", postsPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", postsPage.getTotalPages());
            if(principal==null) model.addAttribute("username", "guest");
            else model.addAttribute("username",principal.getName());
            return "paginationPosts";
        }

        @GetMapping("/filterByAuthor")
        public String filterByAuthor(@RequestParam(required = false) String name, @RequestParam(required = false, defaultValue = "0") int page, Model model){
            Page<Post> filteredPosts = postService.getPostsByAuthorName(name, page, 10);
            model.addAttribute("posts", filteredPosts.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", filteredPosts.getTotalPages());
            model.addAttribute("name", name);
            model.addAttribute("currentAction","/filterByAuthor");
            return "paginationPosts";
        }

        @GetMapping("/filterByPublishedAt")
        public String filterByPublishedAt(@RequestParam(required = false) LocalDate date, @RequestParam(required = false, defaultValue = "0") int page, Model model, Principal principal){
            Page<Post> filteredPosts = postService.getPostsByPublishedDate(date,page,10);
            model.addAttribute("posts", filteredPosts.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", filteredPosts.getTotalPages());
            model.addAttribute("date", date);
            model.addAttribute("currentAction","/filterByPublishedAt");
            if(principal==null) model.addAttribute("username", "guest");
            else model.addAttribute("username",principal.getName());
            return "paginationPosts";
        }

        @GetMapping("/filterByTagName")
        public String filterByTagName(@RequestParam(required = false) String tagName, @RequestParam(required = false, defaultValue = "0") int page, Model model){
            Page<Post> filteredPosts = postService.getPostsByTag(tagName,page,10);
            model.addAttribute("posts", filteredPosts.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", filteredPosts.getTotalPages());
            model.addAttribute("tagName", tagName);
            model.addAttribute("currentAction","/filterByTagName");
            return "paginationPosts";
        }

        @GetMapping("/searchPosts")
        public String getSearchPosts(
                @RequestParam(required = false) String author,
                @RequestParam(required = false) String tag,
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate publishedDate,
                @RequestParam(required = false) String search,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size,
                @RequestParam(defaultValue = "true") boolean sortAsc,
                Model model,
                Principal principal
        ) {
            Page<Post> posts = postService.getPosts(author, tag, publishedDate, search, page, size, sortAsc);
            model.addAttribute("posts", posts.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", posts.getTotalPages());
            model.addAttribute("author", author);
            model.addAttribute("tag", tag);
            model.addAttribute("publishedDate", publishedDate);
            model.addAttribute("search", search);
            model.addAttribute("sortAsc", sortAsc);
            model.addAttribute("currentAction", "/searchPosts");
            if(principal==null) model.addAttribute("username", "guest");
            else model.addAttribute("username",principal.getName());
            return "paginationPosts";
        }

        @GetMapping("/posts/sort/asc")
        public String getPostsByDateAsc(@RequestParam(defaultValue = "0") int page,
                                        Model model, Principal principal) {
            Page<Post> postsPage = postService.getPostsSortedByDateAsc(page, 10);
            model.addAttribute("posts", postsPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", postsPage.getTotalPages());
            model.addAttribute("currentAction", "/posts/sort/asc");
            if(principal==null) model.addAttribute("username", "guest");
            else model.addAttribute("username",principal.getName());
            return "paginationPosts";
        }

        @GetMapping("/posts/sort/desc")
        public String getPostsByDateDesc(@RequestParam(defaultValue = "0") int page,
                                         Model model, Principal principal) {
            Page<Post> postsPage = postService.getPostsSortedByDateDesc(page, 10);
            model.addAttribute("posts", postsPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", postsPage.getTotalPages());
            model.addAttribute("currentAction","/posts/sort/desc");
            if(principal==null) model.addAttribute("username", "guest");
            else model.addAttribute("username",principal.getName());
            return "paginationPosts";
        }

        @GetMapping("/getFilteredPosts")
        public String getPostsBasedOnTagsAndAuthors(
                @RequestParam(required = false) String authors,   // comma-separated authors
                @RequestParam(required = false) String tags,      // comma-separated tags
                @RequestParam(required = false) String search,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size,
                Model model,
                Principal principal
        ) {
            Set<String> authorList = (authors != null && !authors.isBlank())
                    ? Arrays.stream(authors.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet())
                    : null;

            Set<String> tagList = (tags != null && !tags.isBlank())
                    ? Arrays.stream(tags.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet())
                    : null;

            Page<Post> posts = postService.getPostsByAuthorsAndTags(authorList, tagList, search, page, size);
            Set<String> listOfAuthors = postService.getAllAuthors();
            Set<String> listOfTags = postService.getAllTags();
            model.addAttribute("posts", posts.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", posts.getTotalPages());
            model.addAttribute("authors", authorList);
            model.addAttribute("tags", tagList);
            model.addAttribute("search", search);
            model.addAttribute("allAuthors", listOfAuthors);
            model.addAttribute("allTags", listOfTags);
            model.addAttribute("currentAction", "/getFilteredPosts");
            if(principal==null) model.addAttribute("username", "guest");
            else model.addAttribute("username",principal.getName());

            return "paginationPosts";
        }
}

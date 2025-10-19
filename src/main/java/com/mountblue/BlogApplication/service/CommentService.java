package com.mountblue.BlogApplication.service;

import com.mountblue.BlogApplication.Security.CustomUserDetails;
import com.mountblue.BlogApplication.entity.Comment;
import com.mountblue.BlogApplication.entity.Post;
import com.mountblue.BlogApplication.entity.User;
import com.mountblue.BlogApplication.repository.CommentRepository;
import com.mountblue.BlogApplication.repository.PostRepository;
import com.mountblue.BlogApplication.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CommentService {
    private CommentRepository commentRepository;
    private PostService postService;
    private UserRepository userRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository, PostService postService,UserRepository userRepository){
        this.commentRepository = commentRepository;
        this.postService = postService;
        this.userRepository = userRepository;
    }
    public List<Comment> findCommentsPerPost(Long id){
        return commentRepository.findByPostId(id);
    }
    public Comment addComment(HttpServletRequest req,Long id){
        String comment = req.getParameter("comment");
        String name = req.getParameter("name");
        String email = req.getParameter("email");
        Post post = postService.findById(id);
        Comment comment1 = new Comment();
        comment1.setComment(comment);
        comment1.setName(name);
        comment1.setEmail(email);
        comment1.setPost(post);
        comment1.setCreatedAt(LocalDateTime.now());
        return commentRepository.save(comment1);
    }

    public Comment updateComment(Long id, HttpServletRequest req){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails loggedInUser = (CustomUserDetails) authentication.getPrincipal();
        String username = loggedInUser.getUsername();
        User user = userRepository.findByName(username);
        Optional<Comment> comment = findCommentById(id);
        if(!user.getEmail().equalsIgnoreCase(comment.get().getPost().getUser().getEmail()))
            throw new RuntimeException("User unauthorized!");
        if(comment.isPresent()){
            String name = req.getParameter("name");
            String email = req.getParameter("email");
            String comment1 = req.getParameter("comment");
            Comment comment2 = comment.get();
            comment2.setName(name);
            comment2.setEmail(email);
            comment2.setComment(comment1);
            comment2.setUpdatedAt(LocalDateTime.now());
            return commentRepository.save(comment2);
        }
        return null;
    }

    public void deleteById(Long id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails loggedInUser = (CustomUserDetails) authentication.getPrincipal();
        String username = loggedInUser.getUsername();
        User user = userRepository.findByName(username);
        Optional<Comment> comment = findCommentById(id);
        if(!user.getEmail().equalsIgnoreCase(comment.get().getPost().getUser().getEmail()))
            throw new RuntimeException("User unauthorized!");
        commentRepository.delete(comment.get());
    }

    public Optional<Comment> findCommentById(Long id){
        return commentRepository.findById(id);
    }

}

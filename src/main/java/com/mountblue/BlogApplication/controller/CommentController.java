package com.mountblue.BlogApplication.controller;

import com.mountblue.BlogApplication.entity.Comment;
import com.mountblue.BlogApplication.entity.Post;
import com.mountblue.BlogApplication.service.CommentService;
import com.mountblue.BlogApplication.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.List;
import java.util.Optional;

@Controller
public class CommentController {

    private CommentService commentService;
    private PostService postService;

    @Autowired
    public CommentController(CommentService commentService, PostService postService){
        this.commentService = commentService;
        this.postService = postService;
    }

    @PostMapping("/addComment/{id}")
    public String addComment(@PathVariable Long id, Model model, HttpServletRequest req){
        Comment comment = commentService.addComment(req,id);
        model.addAttribute("comment", comment);
        model.addAttribute("post",postService.findById(id));
        return "published";
    }

    @GetMapping("/allComments/{id}")
    public String viewComments(@PathVariable Long id, Model model){
        List<Comment> comments = commentService.findCommentsPerPost(id);
        model.addAttribute("comments", comments);
        Post post = postService.findById(id);
        model.addAttribute("post", post);
        return "published";
    }

    @PostMapping("/updateComments/{id}")
    public String update(@PathVariable Long id, Model model){
        Optional<Comment> c = commentService.findCommentById(id);
        Comment comment = c.get();
        model.addAttribute("comment", comment);
        return "editComment";
    }
    @PostMapping("/updateComment/{id}")
    public String updateComment(@PathVariable Long id,Model model, HttpServletRequest req){
        Comment comment = commentService.updateComment(id,req);
        model.addAttribute("comment", comment);
        Post post = comment.getPost();
        model.addAttribute("post", post);
        return "published";
    }
    @DeleteMapping("/deleteComments/{id}")
    public String delete(@PathVariable Long id, Model model){
        Optional<Comment> c = commentService.findCommentById(id);
        Comment comment = c.get();
        Post post = comment.getPost();
        model.addAttribute("comment", comment);
        model.addAttribute("post", post);
        commentService.deleteById(id);
        return "published";
    }
}

import com.mountblue.BlogApplication.entity.Post;
import com.mountblue.BlogApplication.service.PostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;

@SpringBootTest(classes = com.mountblue.BlogApplication.BlogApplication.class)
public class test {


    @Autowired
    private PostService postService;

    @Test
    void printPageSize() {
        Page<Post> page = postService.getPostsByTag("java", 0, 10);
        System.out.println("Page size: " + page.getContent().size());
    }
}

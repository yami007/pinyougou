import com.pinyougou.pojo.TbContent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/applicationContext-redis.xml")
public class redisTest {
    @Autowired
    private RedisTemplate redisTemplate;
    @Test
    public void testredis(){
        redisTemplate.boundValueOps("ceshi2").set("yami1");
    }
    @Test
    public void getredis(){
        List<TbContent> contentList = (List<TbContent>) redisTemplate.boundHashOps("content").get(1l);
        for (TbContent tbContent : contentList) {
            System.out.println(tbContent);
        }

    }
}

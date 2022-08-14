package xyz.ls.sjtuforum.service;

import xyz.ls.sjtuforum.mapper.AdMapper;
import xyz.ls.sjtuforum.model.Ad;
import xyz.ls.sjtuforum.model.AdExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdService {
    @Autowired
    private AdMapper adMapper;

    public List<Ad> list(String pos) {
        AdExample navExample = new AdExample();
        navExample.createCriteria()
                .andStatusEqualTo(1)
                .andPosEqualTo(pos)
                .andGmtStartLessThan(System.currentTimeMillis())
                .andGmtEndGreaterThan(System.currentTimeMillis());
        return adMapper.selectByExample(navExample);
    }
}

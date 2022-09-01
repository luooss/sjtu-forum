package xyz.ls.sjtuforum.service;

import xyz.ls.sjtuforum.cache.PostCache;
import xyz.ls.sjtuforum.dto.PaginationDTO;
import xyz.ls.sjtuforum.dto.PostDTO;
import xyz.ls.sjtuforum.dto.PostQueryDTO;
import xyz.ls.sjtuforum.enums.SortEnum;
import xyz.ls.sjtuforum.exception.SFErrorCode;
import xyz.ls.sjtuforum.exception.SFException;
import xyz.ls.sjtuforum.mapper.PostExtMapper;
import xyz.ls.sjtuforum.mapper.PostMapper;
import xyz.ls.sjtuforum.mapper.UserMapper;
import xyz.ls.sjtuforum.model.Post;
import xyz.ls.sjtuforum.model.PostExample;
import xyz.ls.sjtuforum.model.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Autowired
    private PostMapper PostMapper;

    @Autowired
    private PostExtMapper PostExtMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostCache postCache;

    public PaginationDTO list(String search, String tag, String sort, Integer page, Integer size) {

        if (StringUtils.isNotBlank(search)) {
            String[] tags = StringUtils.split(search, " ");
            search = Arrays.stream(tags)
                           .filter(StringUtils::isNotBlank)
                           .map(t -> t.replace("+", "").replace("*", "").replace("?", ""))
                           .filter(StringUtils::isNotBlank)
                           .collect(Collectors.joining("|"));
        }

        PaginationDTO paginationDTO = new PaginationDTO();

        Integer totalPage;

        PostQueryDTO PostQueryDTO = new PostQueryDTO();
        PostQueryDTO.setSearch(search);
        if (StringUtils.isNotBlank(tag)) {
            tag = tag.replace("+", "").replace("*", "").replace("?", "");
            PostQueryDTO.setTag(tag);
        }

        for (SortEnum sortEnum : SortEnum.values()) {
            if (sortEnum.name().toLowerCase().equals(sort)) {
                PostQueryDTO.setSort(sort);

                if (sortEnum == SortEnum.HOT7) {
                    PostQueryDTO.setTime(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 7);
                }
                if (sortEnum == SortEnum.HOT30) {
                    PostQueryDTO.setTime(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30);
                }
                break;
            }
        }

        Integer totalCount = PostExtMapper.countBySearch(PostQueryDTO);

        if (totalCount % size == 0) {
            totalPage = totalCount / size;
        } else {
            totalPage = totalCount / size + 1;
        }

        if (page < 1) {
            page = 1;
        }
        if (page > totalPage) {
            page = totalPage;
        }

        paginationDTO.setPagination(totalPage, page);
        Integer offset = page < 1 ? 0 : size * (page - 1);
        PostQueryDTO.setSize(size);
        PostQueryDTO.setPage(offset);
        List<Post> Posts = PostExtMapper.selectBySearch(PostQueryDTO);
        List<PostDTO> PostDTOList = new ArrayList<>();

        for (Post Post : Posts) {
            User user = userMapper.selectByPrimaryKey(Post.getCreator());
            PostDTO PostDTO = new PostDTO();
            BeanUtils.copyProperties(Post, PostDTO);
            PostDTO.setDescription("");
            PostDTO.setUser(user);
            PostDTOList.add(PostDTO);
        }
        List<PostDTO> stickies = postCache.getStickies();
        if (stickies != null && stickies.size() != 0) {
            PostDTOList.addAll(0, stickies);
        }
        paginationDTO.setData(PostDTOList);
        return paginationDTO;
    }

    public PaginationDTO list(Long userId, Integer page, Integer size) {
        PaginationDTO paginationDTO = new PaginationDTO();

        Integer totalPage;

        PostExample PostExample = new PostExample();
        PostExample.createCriteria()
                .andCreatorEqualTo(userId);
        Integer totalCount = (int) PostMapper.countByExample(PostExample);

        if (totalCount % size == 0) {
            totalPage = totalCount / size;
        } else {
            totalPage = totalCount / size + 1;
        }

        if (page < 1) {
            page = 1;
        }
        if (page > totalPage) {
            page = totalPage;
        }

        paginationDTO.setPagination(totalPage, page);

        // size*(page-1)
        Integer offset = size * (page - 1);
        PostExample example = new PostExample();
        example.createCriteria()
                .andCreatorEqualTo(userId);
        List<Post> Posts = PostMapper.selectByExampleWithRowbounds(example, new RowBounds(offset, size));
        List<PostDTO> PostDTOList = new ArrayList<>();

        for (Post Post : Posts) {
            User user = userMapper.selectByPrimaryKey(Post.getCreator());
            PostDTO PostDTO = new PostDTO();
            BeanUtils.copyProperties(Post, PostDTO);
            PostDTO.setUser(user);
            PostDTOList.add(PostDTO);
        }

        paginationDTO.setData(PostDTOList);
        return paginationDTO;
    }

    public PostDTO getById(Long id) {
        Post Post = PostMapper.selectByPrimaryKey(id);
        if (Post == null) {
            throw new SFException(SFErrorCode.POST_NOT_FOUND);
        }
        PostDTO PostDTO = new PostDTO();
        BeanUtils.copyProperties(Post, PostDTO);
        User user = userMapper.selectByPrimaryKey(Post.getCreator());
        PostDTO.setUser(user);
        return PostDTO;
    }

    public void createOrUpdate(Post Post) {
        if (Post.getId() == null) {
            // 创建
            Post.setGmtCreate(System.currentTimeMillis());
            Post.setGmtModified(Post.getGmtCreate());
            Post.setViewCount(0);
            Post.setLikeCount(0);
            Post.setCommentCount(0);
            Post.setSticky(0);
            PostMapper.insert(Post);
        } else {
            // 更新

            Post dbPost = PostMapper.selectByPrimaryKey(Post.getId());
            if (dbPost == null) {
                throw new SFException(SFErrorCode.POST_NOT_FOUND);
            }

            if (dbPost.getCreator().longValue() != Post.getCreator().longValue()) {
                throw new SFException(SFErrorCode.INVALID_OPERATION);
            }

            Post updatePost = new Post();
            updatePost.setGmtModified(System.currentTimeMillis());
            updatePost.setTitle(Post.getTitle());
            updatePost.setDescription(Post.getDescription());
            updatePost.setTag(Post.getTag());
            PostExample example = new PostExample();
            example.createCriteria()
                    .andIdEqualTo(Post.getId());
            int updated = PostMapper.updateByExampleSelective(updatePost, example);
            if (updated != 1) {
                throw new SFException(SFErrorCode.POST_NOT_FOUND);
            }
        }
    }

    public void incView(Long id) {
        Post Post = new Post();
        Post.setId(id);
        Post.setViewCount(1);
        PostExtMapper.incView(Post);
    }

    public List<PostDTO> selectRelated(PostDTO queryDTO) {
        if (StringUtils.isBlank(queryDTO.getTag())) {
            return new ArrayList<>();
        }
        String[] tags = StringUtils.split(queryDTO.getTag(), ",");
        String regexpTag = Arrays
                .stream(tags)
                .filter(StringUtils::isNotBlank)
                .map(t -> t.replace("+", "").replace("*", "").replace("?", ""))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining("|"));
        Post Post = new Post();
        Post.setId(queryDTO.getId());
        Post.setTag(regexpTag);

        List<Post> Posts = PostExtMapper.selectRelated(Post);
        List<PostDTO> PostDTOS = Posts.stream().map(q -> {
            PostDTO PostDTO = new PostDTO();
            BeanUtils.copyProperties(q, PostDTO);
            return PostDTO;
        }).collect(Collectors.toList());
        return PostDTOS;
    }
}

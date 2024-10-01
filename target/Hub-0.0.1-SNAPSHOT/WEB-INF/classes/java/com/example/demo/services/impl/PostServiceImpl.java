package com.example.demo.services.impl;

import com.example.demo.dto.MentionDto;
import com.example.demo.dto.PostDto;
import com.example.demo.dto.UserDto;
import com.example.demo.entity.*;
import com.example.demo.repository.LikeRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.TopicRepository;
import com.example.demo.services.*;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service @Slf4j
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final CommentService commentService;
    private final GroupService groupService;
    private final UserService userService;
    private final PostTopicService postTopicService;
    private final TopicService topicService;
    private final MentionService mentionService;

    @Override
    public List<Post> findByUserName(String userName) {
        return postRepository.findByUserName(userName);
    }

    @Override
    public List<Post> findByTopicName(String topicName) {
        return postRepository.findByTopicName(topicName);
    }

    @Override
    public Post findById(Long postId) {
        return postRepository.findById(postId).orElse(null);
    }

    @Override
    public PostDto findByPostDtoId(Long postId) {
        Post post =  postRepository.findById(postId).orElse(null);
        String staffId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByStaffId(staffId);
        PostDto postDto = transformToPostDto(post,user.getId());
        postDto.setMentionUserList(getMentionUser(post));
        return postDto;
    }

    @Override
    public Long postsCountByTopic(String topicName) {
        return postRepository.countByTopic(topicName);
    }

    @Override
    public List<Post> findAllPosts() {
        return postRepository.findAllPosts();
    }

    @Override
    public List<Post> findByGroupId(Long groupId) {
        return postRepository.findByGroupId(groupId);
    }

    @Override
    public PostDto findByIdPost(Long postId,Long userId) {
        Post post = postRepository.findById(postId).orElse(null);
        PostDto postDto = transformToPostDto(post,userId);
        return postDto;
    }

    @Override
    public Long totalPostByUser(Long userId) {
        return postRepository.countByUser(userId);
    }

    @Override
    public Page<PostDto> findBySpecification(String topicName, String search, Long userId, int page, int size, Sort sort) {
        Specification<Post> userAccessSpec = hasGroupAccess(userId);
        Specification<Post> spec = buildSpecification(topicName, search);
        Specification<Post> combinedSpec = spec.and(userAccessSpec);

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Post> posts = postRepository.findAll(combinedSpec, pageable);
        log.info("POSTS {}", posts);
        return posts.map(post -> transformToPostDto(post, userId));
    }

    private Specification<Post> buildSpecification(String topicName, String search) {
        return (Root<Post> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Ensure the status is checked as a Boolean
            predicates.add(builder.isFalse(root.get("status")));

            if (topicName != null) {
                Join<Post, PostTopic> postTopicJoin = root.join("postTopics", JoinType.INNER);
                Join<PostTopic, Topic> topicJoin = postTopicJoin.join("topic", JoinType.INNER);
                predicates.add(builder.equal(topicJoin.get("name"), topicName));
            }

            if (search != null) {
                String[] searchTerms = search.split("\\s+");
                for (String term : searchTerms) {
                    log.info("SearchTerm {}",term);

                    Join<Post, User> userJoin = root.join("user", JoinType.LEFT);
                    Join<Post, Content> contentJoin = root.join("content", JoinType.LEFT);

                    predicates.add(builder.or(
                            builder.like(userJoin.get("name"), "%" + term + "%"),
                            builder.like(contentJoin.get("text"), "%" + term + "%")
                    ));
                }
            }

            return predicates.isEmpty() ? builder.conjunction() : builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<Post> hasGroupAccess(Long userId) {
        return (Root<Post> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
            // Join to the group table
            Join<Post, Group> postGroupJoin = root.join("group", JoinType.LEFT);

            // Join to the userHasGroups table
            Join<Group, UserHasGroup> groupUserHasGroupJoin = postGroupJoin.join("userHasGroups", JoinType.LEFT);

            // Prepare predicates for checking user membership in the group or if the post doesn't belong to any group
            List<Predicate> predicates = new ArrayList<>();

            // Check if the post belongs to a group where the user is a member and the group is not deleted, or if the post does not belong to any group (public post)
            predicates.add(builder.or(
                    builder.and(
                            builder.equal(groupUserHasGroupJoin.get("user").get("id"), userId),
                            builder.isFalse(postGroupJoin.get("deleted"))
                    ),
                    builder.isNull(postGroupJoin.get("id"))
            ));

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
    @Override
    public void deletePost(Long id) {
        Post exitPost= postRepository.findById(id).orElse(null);
        exitPost.setStatus(true);
        postRepository.save(exitPost);
    }

    public PostDto transformToPostDto(Post post, Long userId) {
        PostDto postDto = new PostDto(post);
        postDto.setLikedByCurrentUser(isPostLikedByUser(post, userId));
        postDto.setLikes(likeRepository.totalLikePost(post.getId()).orElse(0L).intValue());
        postDto.setBookMark(isPostBookmarkedByUser(post, userId));
        postDto.setCommentCount(commentService.countCommentByPostId(post.getId()));
        postDto.setMentionUserList(getMentionUser(post));
        postDto.setTopicList(fetchAllTopic(post.getId()));
        return validateUser(post,postDto);
    }

    private PostDto validateUser(Post post,PostDto postDto){
        String staffId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByStaffId(staffId);
        if(Objects.equals(post.getUser().getStaffId(),staffId)){
            postDto.setOwner(true);
        }
        if(user.getRole().name().equals("ADMIN")){
            postDto.setAdmin(true);
        }
        return postDto;
    }
    private List<String> fetchAllTopic(Long postId){
        return postTopicService.getAllTopicName(postId);
    }

    @Override
    public Long totalTopicPost(Long topicId) {
        return postRepository.countByTopic(topicId);
    }

    @Override
    public List<PostDto> getAllPostByUser() {
        String staffId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByStaffId(staffId);
        List<Post> postList = postRepository.findByUser(user.getId());
        return postList.stream().map(post -> transformToPostDto(post,user.getId())).toList();
    }

    @Override
    public Post saveGroup(Post post,Group group) {
        Post getPost = postRepository.findById(post.getId()).orElse(null);
        assert getPost != null;
        getPost.setGroup(group);
        return postRepository.save(getPost);
    }

    @Override
    public List<PostDto> getPostFromGroup(Long groupId, Principal principal) {
        List<Post> groupPost = postRepository.getPostFromGroup(groupId);
        User logUser = userService.findAuthenticatedUser(principal);
        return groupPost.stream().map(post -> transformToPostDto(post,logUser.getId())).toList();
    }


    private List<MentionDto> getMentionUser(Post post){
        return mentionService.getUserMentionList(post);
    }

    private boolean isPostLikedByUser(Post post, Long userId) {
        return post.getLikes().stream()
                .anyMatch(like -> like.getUser().getId().equals(userId) && like.isLikeStatus());
    }

    private boolean isPostBookmarkedByUser(Post post, Long userId) {
        boolean result = post.getBookMarks().stream()
//                .peek(bookMark -> System.out.println("Checking bookmark for user: " + bookMark.getUser().getId() +
//                        ", post: " + bookMark.getPost().getId() +
//                        ", status: " + bookMark.isStatus()))
                .anyMatch(bookMark -> bookMark.getUser().getId().equals(userId) && bookMark.isStatus());
        return result;
    }
    @Override
    public Post createPost(Content content,String text) {
        Post post = new Post();

        post.setUser(userService.findByStaffId(SecurityContextHolder.getContext().getAuthentication().getName()));
        post.setContent(content);
        Post savedPost = postRepository.save(post);
        topicService.extractTopicList(savedPost,text);

        return savedPost;
    }

    @Override
    public List<PostDto> getTrendingPostsInOneWeek() {
        // Get the current date and time
        LocalDateTime now = LocalDateTime.now();
        // Subtract one week from the current date and time
        LocalDateTime oneWeekAgo = now.minusWeeks(1);
        List<Post> postList = postRepository.findTop5TrendingPosts(oneWeekAgo);
        return postList.stream().map(post -> {
            PostDto postDto = new PostDto(post);
            postDto.setLikes(likeRepository.totalLikePost(post.getId()).orElse(0L).intValue());
            postDto.setCommentCount(commentService.countCommentByPostId(post.getId()));
            return postDto;
        }).toList();
    }

    @Override
    public List<PostDto> getTrendingPostsInOneMonth() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthAgo = now.minusMonths(1);
        List<Post> postList = postRepository.findTop5TrendingPostsInOneMonth(oneMonthAgo);
        return  postList.stream().map(post -> {
            PostDto postDto = new PostDto(post);
            postDto.setLikes(likeRepository.totalLikePost(post.getId()).orElse(0L).intValue());
            postDto.setCommentCount(commentService.countCommentByPostId(post.getId()));
            return postDto;
        }).toList();
    }

    @Override
    public List<PostDto> getTrendingPostsInOneYear() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneYearAgo = now.minusYears(1);
        List<Post> postList = postRepository.findTop5TrendingPostsInOneYear(oneYearAgo);
        return postList.stream().map(post->{
            PostDto postDto = new PostDto(post);
            postDto.setLikes(likeRepository.totalLikePost(post.getId()).orElse(0L).intValue());
            postDto.setCommentCount(commentService.countCommentByPostId(post.getId()));
            return postDto;
        }).toList();
    }

    @Override
    public long getTotalPostsForCurrentDay() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        return  postRepository.countPostsFromDayOrMonthOrYear(startOfDay,endOfDay);
    }

    @Override
    public long getTotalPostsForCurrentMonth() {
        LocalDate now = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(now);
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(LocalTime.MAX);
        return postRepository.countPostsFromDayOrMonthOrYear(startOfMonth, endOfMonth);
    }

    @Override
    public long getTotalPostsForCurrentYear() {
        LocalDate now = LocalDate.now();
        Year currentYear = Year.from(now);
        LocalDateTime startOfYear = currentYear.atDay(1).atStartOfDay();
        LocalDateTime endOfYear = currentYear.atMonth(12).atEndOfMonth().atTime(LocalTime.MAX);
        return postRepository.countPostsFromDayOrMonthOrYear(startOfYear, endOfYear);
    }

    @Override
    public List<PostDto> findTopPostsInGroup(Long groupId) {

        List<Post> postList = postRepository.findTopPostsByLikesAndCommentsInGroup(groupId);
        String staff = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByStaffId(staff);
        return postList.stream().map(post -> transformToPostDto(post,user.getId())).toList();
    }

    @Override
    public List<PostDto> findByUserId(Long userId){
        List<Post> postList = postRepository.findPostByUserId(userId);
       return postList.stream().map(post -> transformToPostDto(post,userId)).toList();
    }

}

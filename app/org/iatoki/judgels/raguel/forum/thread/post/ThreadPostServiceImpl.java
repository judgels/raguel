package org.iatoki.judgels.raguel.forum.thread.post;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.raguel.forum.Forum;
import org.iatoki.judgels.raguel.forum.ForumDao;
import org.iatoki.judgels.raguel.forum.ForumLastPostDao;
import org.iatoki.judgels.raguel.forum.ForumModel;
import org.iatoki.judgels.raguel.forum.ForumServiceUtils;
import org.iatoki.judgels.raguel.forum.module.ForumModuleDao;
import org.iatoki.judgels.raguel.forum.module.ForumModuleFactory;
import org.iatoki.judgels.raguel.forum.thread.ForumThread;
import org.iatoki.judgels.raguel.forum.thread.ForumThreadDao;
import org.iatoki.judgels.raguel.forum.thread.ForumThreadModel;
import org.iatoki.judgels.raguel.forum.thread.ForumThreadServiceUtils;
import org.iatoki.judgels.raguel.user.postcount.UserPostCountDao;
import org.iatoki.judgels.raguel.user.postcount.UserPostCountModel;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

@Singleton
public final class ThreadPostServiceImpl implements ThreadPostService {

    private final ForumDao forumDao;
    private final ForumLastPostDao forumLastPostDao;
    private final ForumModuleDao forumModuleDao;
    private final ForumModuleFactory forumModuleFactory;
    private final ForumThreadDao forumThreadDao;
    private final ThreadPostDao threadPostDao;
    private final PostContentDao postContentDao;
    private final UserPostCountDao userPostCountDao;

    @Inject
    public ThreadPostServiceImpl(ForumDao forumDao, ForumLastPostDao forumLastPostDao, ForumModuleDao forumModuleDao, ForumModuleFactory forumModuleFactory, ForumThreadDao forumThreadDao, ThreadPostDao threadPostDao, PostContentDao postContentDao, UserPostCountDao userPostCountDao) {
        this.forumDao = forumDao;
        this.forumLastPostDao = forumLastPostDao;
        this.forumModuleDao = forumModuleDao;
        this.forumModuleFactory = forumModuleFactory;
        this.forumThreadDao = forumThreadDao;
        this.threadPostDao = threadPostDao;
        this.postContentDao = postContentDao;
        this.userPostCountDao = userPostCountDao;
    }

    @Override
    public ThreadPost findThreadPostById(long threadPostId) {
        ThreadPostModel threadPostModel = threadPostDao.findById(threadPostId);
        ForumThreadModel forumThreadModel = forumThreadDao.findByJid(threadPostModel.threadJid);
        ForumModel forumModel = forumDao.findByJid(forumThreadModel.forumJid);

        Forum forum = ForumServiceUtils.createForumWithParentFromModel(forumDao, forumLastPostDao, forumModuleDao, forumModuleFactory, forumModel);
        ForumThread forumThread = ForumThreadServiceUtils.createForumThreadFromModelAndForum(forumThreadModel, forum);
        List<PostContentModel> postContentModels = postContentDao.findSortedByFiltersEq("timeCreate", "asc", "", ImmutableMap.of(PostContentModel_.postJid, threadPostModel.jid), 0, -1);
        List<PostContent> postContents = postContentModels.stream().map(m -> new PostContent(m.id, m.postJid, m.subject, m.content, new Date(m.timeCreate))).collect(Collectors.toList());
        long userPostCount = getUserPostCountByUserJid(threadPostModel.userCreate);

        return ThreadPostServiceUtils.createThreadPostFromModel(threadPostModel, forumThread, postContents, userPostCount);
    }

    @Override
    public Map<String, Long> getThreadPostsJidToIdMap(List<String> threadPostJids) {
        List<ThreadPostModel> threadPostModels = threadPostDao.getByJids(threadPostJids);
        return threadPostModels.stream().collect(Collectors.toMap(m -> m.jid, m -> m.id));
    }

    @Override
    public Map<String, String> getThreadPostsJidToUserJidMap(List<String> threadPostJids) {
        List<ThreadPostModel> threadPostModels = threadPostDao.getByJids(threadPostJids);
        return threadPostModels.stream().collect(Collectors.toMap(m -> m.jid, m -> m.userCreate));
    }

    @Override
    public long getUserPostCountByUserJid(String userJid) {
        return userPostCountDao.getByUserJid(userJid).postCount;
    }

    @Override
    public long countThreadPost(ForumThread forumThread) {
        return threadPostDao.countByFiltersEq("", ImmutableMap.of(ThreadPostModel_.threadJid, forumThread.getJid()));
    }

    @Override
    public List<ThreadPostWithLevel> getAllThreadPostsWithLevel(ForumThread forumThread) {
        List<ThreadPostModel> threadPostModels = threadPostDao.findSortedByFiltersEq("timeCreate", "asc", "", ImmutableMap.of(ThreadPostModel_.threadJid, forumThread.getJid()), 0, -1);
        ThreadPostModel firstThreadPostModel = threadPostModels.get(0);
        Map<String, Stack<ThreadPostModel>> threadPostsModelMap = Maps.newHashMap();
        Map<String, Long> userJidToPostCountMap = getUserJidToPostCountMap(threadPostModels.stream().map(m -> m.userCreate).collect(Collectors.toList()));

        for (ThreadPostModel model : threadPostModels) {
            if ((model.replyJid != null) && !model.replyJid.isEmpty()) {
                Stack<ThreadPostModel> threadPostsModelStack;
                if (!threadPostsModelMap.containsKey(model.replyJid)) {
                    threadPostsModelStack = new Stack<>();
                } else {
                    threadPostsModelStack = threadPostsModelMap.get(model.replyJid);
                }

                threadPostsModelStack.push(model);

                threadPostsModelMap.put(model.replyJid, threadPostsModelStack);
            }
        }

        ImmutableList.Builder<ThreadPostWithLevel> threadPostsWithLevelBuilder = ImmutableList.builder();
        Map<String, ThreadPost> threadPostsMap = Maps.newHashMap();
        Stack<ThreadPostModelWithLevel> threadPostsModelStack = new Stack<>();
        threadPostsModelStack.push(new ThreadPostModelWithLevel(firstThreadPostModel, 0));
        while (!threadPostsModelStack.isEmpty()) {
            ThreadPostModelWithLevel threadPostModelWithLevel = threadPostsModelStack.pop();
            int childLevel = threadPostModelWithLevel.level + 1;

            if (threadPostsModelMap.containsKey(threadPostModelWithLevel.threadPostModel.jid)) {
                Stack<ThreadPostModel> threadPostsModelStack1 = threadPostsModelMap.get(threadPostModelWithLevel.threadPostModel.jid);
                while (!threadPostsModelStack1.isEmpty()) {
                    threadPostsModelStack.push(new ThreadPostModelWithLevel(threadPostsModelStack1.pop(), childLevel));
                }
            }

            List<PostContentModel> postContentModels = postContentDao.findSortedByFiltersEq("timeCreate", "asc", "", ImmutableMap.of(PostContentModel_.postJid, threadPostModelWithLevel.threadPostModel.jid), 0, -1);
            List<PostContent> postContents = postContentModels.stream().map(m -> new PostContent(m.id, m.postJid, m.subject, m.content, new Date(m.timeCreate))).collect(Collectors.toList());

            ThreadPost threadPost = ThreadPostServiceUtils.createThreadPostFromModel(threadPostModelWithLevel.threadPostModel, forumThread, postContents, userJidToPostCountMap.get(threadPostModelWithLevel.threadPostModel.userCreate));
            threadPostsWithLevelBuilder.add(new ThreadPostWithLevel(threadPost, threadPostModelWithLevel.level));

            threadPostsMap.put(threadPost.getJid(), threadPost);
        }

        return threadPostsWithLevelBuilder.build();
    }

    @Override
    public Page<ThreadPost> getPageOfThreadPosts(ForumThread forumThread, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalRowsCount = threadPostDao.countByFiltersEq(filterString, ImmutableMap.of(ThreadPostModel_.threadJid, forumThread.getJid()));
        List<ThreadPostModel> threadPostModels = threadPostDao.findSortedByFiltersEq(orderBy, orderDir, filterString, ImmutableMap.of(ThreadPostModel_.threadJid, forumThread.getJid()), pageIndex * pageSize, pageSize);

        ImmutableList.Builder<ThreadPost> threadPostBuilder = ImmutableList.builder();
        Map<String, Long> userJidToPostCountMap = getUserJidToPostCountMap(threadPostModels.stream().map(m -> m.userCreate).collect(Collectors.toList()));
        for (ThreadPostModel threadPostModel : threadPostModels) {
            List<PostContentModel> postContentModels = postContentDao.findSortedByFiltersEq("timeCreate", "asc", "", ImmutableMap.of(PostContentModel_.postJid, threadPostModel.jid), 0, -1);
            List<PostContent> postContents = postContentModels.stream().map(m -> new PostContent(m.id, m.postJid, m.subject, m.content, new Date(m.timeCreate))).collect(Collectors.toList());

            threadPostBuilder.add(ThreadPostServiceUtils.createThreadPostFromModel(threadPostModel, forumThread, postContents, userJidToPostCountMap.get(threadPostModel.userCreate)));
        }
        return new Page<>(threadPostBuilder.build(), totalRowsCount, pageIndex, pageSize);
    }

    @Override
    public void createPost(ForumThread forumThread, String userJid, String subject, String body, String userIpAddress) {
        ThreadPostModel threadPostModel = new ThreadPostModel();
        threadPostModel.threadJid = forumThread.getJid();

        threadPostDao.persist(threadPostModel, userJid, userIpAddress);

        PostContentModel postContentModel = new PostContentModel();
        postContentModel.postJid = threadPostModel.jid;
        postContentModel.subject = subject;
        postContentModel.content = body;

        postContentDao.persist(postContentModel, userJid, userIpAddress);

        UserPostCountModel userPostCountModel;
        if (userPostCountDao.existsByUserJid(userJid)) {
            userPostCountModel = userPostCountDao.getByUserJid(userJid);
            userPostCountModel.postCount++;
            userPostCountDao.edit(userPostCountModel, userJid, userIpAddress);
        } else {
            userPostCountModel = new UserPostCountModel();
            userPostCountModel.userJid = userJid;
            userPostCountModel.postCount = postContentDao.getCountByUserJid(userJid);

            userPostCountDao.persist(userPostCountModel, userJid, userIpAddress);
        }

        updateThreadAndParents(forumThread, userJid, userIpAddress);
    }

    @Override
    public void editPost(ThreadPost threadPost, String userJid, String subject, String body, String userIpAddress) {
        ThreadPostModel threadPostModel = threadPostDao.findByJid(threadPost.getJid());

        PostContentModel postContentModel = new PostContentModel();
        postContentModel.postJid = threadPostModel.jid;
        postContentModel.subject = subject;
        postContentModel.content = body;

        postContentDao.persist(postContentModel, userJid, userIpAddress);

        threadPostDao.edit(threadPostModel, userJid, userIpAddress);

        if (threadPostModel.replyJid == null) {
            ForumThreadModel forumThreadModel = forumThreadDao.findByJid(threadPostModel.threadJid);
            forumThreadModel.name = postContentModel.subject;

            forumThreadDao.edit(forumThreadModel, userJid, userIpAddress);

            ForumServiceUtils.updateForumAndParents(forumDao, forumLastPostDao, threadPost.getThread().getParentForum(), userJid, userIpAddress);
        } else {
            updateThreadAndParents(threadPost.getThread(), userJid, userIpAddress);
        }
    }

    @Override
    public void replyPost(ThreadPost threadPost, String userJid, String subject, String body, String userIpAddress) {
        ThreadPostModel threadPostModel = new ThreadPostModel();
        threadPostModel.threadJid = threadPost.getThread().getJid();
        threadPostModel.replyJid = threadPost.getJid();

        threadPostDao.persist(threadPostModel, userJid, userIpAddress);

        PostContentModel postContentModel = new PostContentModel();
        postContentModel.postJid = threadPostModel.jid;
        postContentModel.subject = subject;
        postContentModel.content = body;

        postContentDao.persist(postContentModel, userJid, userIpAddress);

        UserPostCountModel userPostCountModel;
        if (userPostCountDao.existsByUserJid(userJid)) {
            userPostCountModel = userPostCountDao.getByUserJid(userJid);
            userPostCountModel.postCount++;
            userPostCountDao.edit(userPostCountModel, userJid, userIpAddress);
        } else {
            userPostCountModel = new UserPostCountModel();
            userPostCountModel.userJid = userJid;
            userPostCountModel.postCount = postContentDao.getCountByUserJid(userJid);

            userPostCountDao.persist(userPostCountModel, userJid, userIpAddress);
        }

        updateThreadAndParents(threadPost.getThread(), userJid, userIpAddress);
    }

    private Map<String, Long> getUserJidToPostCountMap(List<String> userJids) {
        List<UserPostCountModel> postCountModelList = userPostCountDao.getByUserJids(userJids);
        Set<String> allUsers = userJids.stream().collect(Collectors.toSet());
        Set<String> existInDb = postCountModelList.stream().map(m -> m.userJid).collect(Collectors.toSet());
        allUsers.removeAll(existInDb);

        for (String userJid : allUsers) {
            long postCount = postContentDao.getCountByUserJid(userJid);

            UserPostCountModel userPostCountModel = new UserPostCountModel();
            userPostCountModel.postCount = postCount;
            userPostCountModel.userJid = userJid;
            // TODO inject userJid and userIpAddress
            userPostCountDao.persist(userPostCountModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

            postCountModelList.add(userPostCountModel);
        }
        return postCountModelList.stream().collect(Collectors.toMap(m -> m.userJid, m -> m.postCount));
    }

    private void updateThreadAndParents(ForumThread forumThread, String userJid, String userIpAddress) {
        ForumThreadModel forumThreadModel = forumThreadDao.findByJid(forumThread.getJid());
        forumThreadDao.edit(forumThreadModel, userJid, userIpAddress);

        ForumServiceUtils.updateForumAndParents(forumDao, forumLastPostDao, forumThread.getParentForum(), userJid, userIpAddress);
    }

    private class ThreadPostModelWithLevel {
        private final ThreadPostModel threadPostModel;
        private final int level;

        public ThreadPostModelWithLevel(ThreadPostModel threadPostModel, int level) {
            this.threadPostModel = threadPostModel;
            this.level = level;
        }
    }
}

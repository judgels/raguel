package org.iatoki.judgels.raguel.services.impls;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.raguel.Forum;
import org.iatoki.judgels.raguel.ForumThread;
import org.iatoki.judgels.raguel.PostContent;
import org.iatoki.judgels.raguel.ThreadPost;
import org.iatoki.judgels.raguel.ThreadPostWithLevel;
import org.iatoki.judgels.raguel.models.daos.ForumDao;
import org.iatoki.judgels.raguel.models.daos.ForumLastPostDao;
import org.iatoki.judgels.raguel.models.daos.ForumModuleDao;
import org.iatoki.judgels.raguel.models.daos.ForumThreadDao;
import org.iatoki.judgels.raguel.models.daos.PostContentDao;
import org.iatoki.judgels.raguel.models.daos.ThreadPostDao;
import org.iatoki.judgels.raguel.models.entities.ForumModel;
import org.iatoki.judgels.raguel.models.entities.ForumThreadModel;
import org.iatoki.judgels.raguel.models.entities.PostContentModel;
import org.iatoki.judgels.raguel.models.entities.PostContentModel_;
import org.iatoki.judgels.raguel.models.entities.ThreadPostModel;
import org.iatoki.judgels.raguel.models.entities.ThreadPostModel_;
import org.iatoki.judgels.raguel.modules.forum.ForumModuleFactory;
import org.iatoki.judgels.raguel.services.ThreadPostService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

@Singleton
@Named("threadPostService")
public final class ThreadPostServiceImpl implements ThreadPostService {

    private final ForumDao forumDao;
    private final ForumLastPostDao forumLastPostDao;
    private final ForumModuleDao forumModuleDao;
    private final ForumModuleFactory forumModuleFactory;
    private final ForumThreadDao forumThreadDao;
    private final ThreadPostDao threadPostDao;
    private final PostContentDao postContentDao;

    @Inject
    public ThreadPostServiceImpl(ForumDao forumDao, ForumLastPostDao forumLastPostDao, ForumModuleDao forumModuleDao, ForumModuleFactory forumModuleFactory, ForumThreadDao forumThreadDao, ThreadPostDao threadPostDao, PostContentDao postContentDao) {
        this.forumDao = forumDao;
        this.forumLastPostDao = forumLastPostDao;
        this.forumModuleDao = forumModuleDao;
        this.forumModuleFactory = forumModuleFactory;
        this.forumThreadDao = forumThreadDao;
        this.threadPostDao = threadPostDao;
        this.postContentDao = postContentDao;
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

        return ThreadPostServiceUtils.createThreadPostFromModel(threadPostModel, forumThread, postContents);
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
    public long countThreadPost(ForumThread forumThread) {
        return threadPostDao.countByFiltersEq("", ImmutableMap.of(ThreadPostModel_.threadJid, forumThread.getJid()));
    }

    @Override
    public List<ThreadPostWithLevel> getAllThreadPostsWithLevel(ForumThread forumThread) {
        List<ThreadPostModel> threadPostModels = threadPostDao.findSortedByFiltersEq("timeCreate", "asc", "", ImmutableMap.of(ThreadPostModel_.threadJid, forumThread.getJid()), 0, -1);
        ThreadPostModel firstThreadPostModel = threadPostModels.get(0);
        Map<String, Stack<ThreadPostModel>> threadPostsModelMap = Maps.newHashMap();

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

            ThreadPost threadPost = ThreadPostServiceUtils.createThreadPostFromModel(threadPostModelWithLevel.threadPostModel, forumThread, postContents);
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
        for (ThreadPostModel threadPostModel : threadPostModels) {
            List<PostContentModel> postContentModels = postContentDao.findSortedByFiltersEq("timeCreate", "asc", "", ImmutableMap.of(PostContentModel_.postJid, threadPostModel.jid), 0, -1);
            List<PostContent> postContents = postContentModels.stream().map(m -> new PostContent(m.id, m.postJid, m.subject, m.content, new Date(m.timeCreate))).collect(Collectors.toList());

            threadPostBuilder.add(ThreadPostServiceUtils.createThreadPostFromModel(threadPostModel, forumThread, postContents));
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

        updateThreadAndParents(threadPost.getThread(), userJid, userIpAddress);
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

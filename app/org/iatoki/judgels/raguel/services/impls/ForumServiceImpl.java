package org.iatoki.judgels.raguel.services.impls;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.raguel.Forum;
import org.iatoki.judgels.raguel.ForumNotFoundException;
import org.iatoki.judgels.raguel.ForumWithStatus;
import org.iatoki.judgels.raguel.models.daos.ForumDao;
import org.iatoki.judgels.raguel.models.daos.ForumModuleDao;
import org.iatoki.judgels.raguel.models.daos.ForumThreadDao;
import org.iatoki.judgels.raguel.models.daos.UserItemDao;
import org.iatoki.judgels.raguel.models.entities.ForumModel;
import org.iatoki.judgels.raguel.models.entities.ForumModel_;
import org.iatoki.judgels.raguel.models.entities.ForumThreadModel;
import org.iatoki.judgels.raguel.models.entities.ForumThreadModel_;
import org.iatoki.judgels.raguel.models.entities.UserItemModel;
import org.iatoki.judgels.raguel.models.entities.UserItemModel_;
import org.iatoki.judgels.raguel.modules.forum.ForumModuleFactory;
import org.iatoki.judgels.raguel.modules.forum.ForumModules;
import org.iatoki.judgels.raguel.services.ForumService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

@Singleton
@Named("forumService")
public final class ForumServiceImpl implements ForumService {

    private final ForumDao forumDao;
    private final ForumModuleDao forumModuleDao;
    private final ForumModuleFactory forumModuleFactory;
    private final ForumThreadDao forumThreadDao;
    private final UserItemDao userItemDao;

    @Inject
    public ForumServiceImpl(ForumDao forumDao, ForumModuleDao forumModuleDao, ForumModuleFactory forumModuleFactory, ForumThreadDao forumThreadDao, UserItemDao userItemDao) {
        this.forumDao = forumDao;
        this.forumModuleDao = forumModuleDao;
        this.forumModuleFactory = forumModuleFactory;
        this.forumThreadDao = forumThreadDao;
        this.userItemDao = userItemDao;
    }

    @Override
    public boolean forumExistsByJid(String forumJid) {
        return forumDao.existsByJid(forumJid);
    }

    @Override
    public List<Forum> getAllForums() {
        Stack<Forum> forumStack = new Stack<>();
        forumStack.add(null);

        ImmutableList.Builder<Forum> forumBuilder = ImmutableList.builder();

        while (!forumStack.isEmpty()) {
            Forum currentForum = forumStack.pop();

            String currentJid;
            if (currentForum != null) {
                currentJid = currentForum.getJid();
                forumBuilder.add(currentForum);
            } else {
                currentJid = "";
            }

            List<ForumModel> forumModels = forumDao.findSortedByFiltersEq("id", "asc", "", ImmutableMap.of(ForumModel_.parentJid, currentJid), 0, -1);
            for (ForumModel forumModel : forumModels) {
                Forum forum = ForumServiceUtils.createForumFromModel(forumModuleFactory, forumModel, currentForum, forumModuleDao.getEnabledInForum(forumModel.jid));
                if (currentForum != null) {
                    currentForum.getSubforums().add(forum);
                }
                forumStack.push(forum);
            }
        }

        return forumBuilder.build();
    }

    @Override
    public List<Forum> getChildForums(String parentJid) {
        List<ForumModel> forumModels = forumDao.findSortedByFiltersEq("id", "asc", "", ImmutableMap.of(ForumModel_.parentJid, parentJid), 0, -1);

        List<Forum> forums = forumModels.stream().map(m -> createForumWithParentAndSubforumsFromModel(m)).collect(Collectors.toList());

        return forums;
    }

    @Override
    public List<ForumWithStatus> getChildForumsWithStatus(String parentJid, String userJid) {
        List<Forum> forums = getChildForums(parentJid);

        ImmutableList.Builder<ForumWithStatus> forumWithStatusesBuilder = ImmutableList.builder();
        for (Forum forum : forums) {
            Stack<Forum> forumStack = new Stack<>();
            forumStack.push(forum);

            boolean hasNewPost = false;
            while (!hasNewPost && !forumStack.isEmpty()) {
                Forum currentForum = forumStack.pop();

                if (currentForum.containsModule(ForumModules.THREAD)) {
                    List<ForumThreadModel> forumThreadModels = forumThreadDao.findSortedByFiltersEq("id", "asc", "", ImmutableMap.of(ForumThreadModel_.forumJid, currentForum.getJid()), 0, -1);
                    List<String> forumThreadJids = forumThreadModels.stream().map(t -> t.jid).collect(Collectors.toList());
                    List<UserItemModel> userItemModels = userItemDao.findSortedByFilters("id", "asc", "", ImmutableMap.of(UserItemModel_.userJid, userJid), ImmutableMap.of(UserItemModel_.itemJid, forumThreadJids), 0, -1);
                    ImmutableMap.Builder<String, UserItemModel> mapItemJidToUserItemModelBuilder = ImmutableMap.builder();
                    for (UserItemModel userItemModel : userItemModels) {
                        mapItemJidToUserItemModelBuilder.put(userItemModel.itemJid, userItemModel);
                    }
                    Map<String, UserItemModel> mapItemJidToUserItemModel = mapItemJidToUserItemModelBuilder.build();

                    for (int i = 0; !hasNewPost && (i < forumThreadModels.size()); ++i) {
                        UserItemModel userItemModel = mapItemJidToUserItemModel.get(forumThreadModels.get(i).jid);
                        hasNewPost = (userItemModel == null) || (userItemModel.timeUpdate < forumThreadModels.get(i).timeUpdate);
                    }
                }

                if (!hasNewPost) {
                    for (Forum childForum : currentForum.getSubforums()) {
                        forumStack.push(childForum);
                    }
                }
            }

            forumWithStatusesBuilder.add(new ForumWithStatus(forum, hasNewPost));
        }

        return forumWithStatusesBuilder.build();
    }

    @Override
    public Forum findForumById(long forumId) throws ForumNotFoundException {
        ForumModel intendedForumModel = forumDao.findById(forumId);

        if (intendedForumModel == null) {
            throw new ForumNotFoundException("Forum Not Found.");
        }

        return createForumWithParentAndSubforumsFromModel(intendedForumModel);
    }

    @Override
    public void createForum(String parentJid, String name, String description) {
        ForumModel forumModel = new ForumModel();
        forumModel.parentJid = parentJid;
        forumModel.name = name;
        forumModel.description = description;

        forumDao.persist(forumModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void updateForum(String forumJid, String parentJid, String name, String description) {
        ForumModel forumModel = forumDao.findByJid(forumJid);
        forumModel.parentJid = parentJid;
        forumModel.name = name;
        forumModel.description = description;

        forumDao.persist(forumModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    private Forum createForumWithParentAndSubforumsFromModel(ForumModel intendedForumModel) {
        Stack<ForumModel> forumModelStack = new Stack<>();
        forumModelStack.push(intendedForumModel);
        while (!forumModelStack.peek().parentJid.equals("")) {
            forumModelStack.push(forumDao.findByJid(forumModelStack.peek().parentJid));
        }

        Forum parentForum = null;
        Forum intendedForum = null;
        while (!forumModelStack.isEmpty()) {
            ForumModel currentForumModel = forumModelStack.pop();

            if (currentForumModel.jid.equals(intendedForumModel.jid) && (intendedForum == null)) {
                Forum currentForum = ForumServiceUtils.createForumFromModel(forumModuleFactory, currentForumModel, parentForum, forumModuleDao.getEnabledInForum(currentForumModel.jid));
                intendedForum = currentForum;
            } else {
                Forum currentForum = ForumServiceUtils.createForumFromModel(forumModuleFactory, currentForumModel, parentForum, forumModuleDao.getEnabledInForum(currentForumModel.jid));

                if (parentForum != null) {
                    parentForum.getSubforums().add(currentForum);
                }
                parentForum = currentForum;
            }
        }

        Stack<Forum> forumStack = new Stack<>();
        forumStack.add(intendedForum);

        while (!forumStack.isEmpty()) {
            Forum currentForum = forumStack.pop();

            List<ForumModel> forumModels = forumDao.findSortedByFiltersEq("id", "asc", "", ImmutableMap.of(ForumModel_.parentJid, currentForum.getJid()), 0, -1);
            for (ForumModel forumModel : forumModels) {
                Forum forum = ForumServiceUtils.createForumFromModel(forumModuleFactory, forumModel, currentForum, forumModuleDao.getEnabledInForum(forumModel.jid));
                if (currentForum != null) {
                    currentForum.getSubforums().add(forum);
                }
                forumStack.push(forum);
            }
        }

        return intendedForum;
    }
}

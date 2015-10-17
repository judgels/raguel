package org.iatoki.judgels.raguel.services.impls;

import com.beust.jcommander.internal.Maps;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.iatoki.judgels.raguel.Forum;
import org.iatoki.judgels.raguel.models.daos.ForumDao;
import org.iatoki.judgels.raguel.models.daos.ForumLastPostDao;
import org.iatoki.judgels.raguel.models.daos.ForumModuleDao;
import org.iatoki.judgels.raguel.models.entities.ForumLastPostModel;
import org.iatoki.judgels.raguel.models.entities.ForumLastPostModel_;
import org.iatoki.judgels.raguel.models.entities.ForumModel;
import org.iatoki.judgels.raguel.models.entities.ForumModel_;
import org.iatoki.judgels.raguel.models.entities.ForumModuleModel;
import org.iatoki.judgels.raguel.models.entities.ForumModuleModel_;
import org.iatoki.judgels.raguel.modules.forum.ForumModule;
import org.iatoki.judgels.raguel.modules.forum.ForumModuleFactory;
import org.iatoki.judgels.raguel.modules.forum.ForumModules;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

public final class ForumServiceUtils {

    private ForumServiceUtils() {
        //prevent instantiation
    }

    static Forum createPlainForumFromModel(ForumModel forumModel, Forum parentForum) {
        return new Forum(forumModel.id, forumModel.jid, parentForum, Lists.newArrayList(), forumModel.name, forumModel.description, null, null, "-");
    }

    static Forum createForumFromModel(ForumModuleFactory forumModuleFactory, ForumModel forumModel, Forum parentForum, List<ForumModuleModel> forumModulesModel, Date timeUpdate, String userUpdate) {
        ImmutableMap.Builder<ForumModules, ForumModule> forumModuleBuilder = ImmutableMap.builder();
        for (ForumModuleModel forumModuleModel : forumModulesModel) {
            ForumModules forumModules = ForumModules.valueOf(forumModuleModel.name);
            forumModuleBuilder.put(forumModules, forumModuleFactory.parseFromConfig(forumModules, forumModuleModel.config));
        }

        return new Forum(forumModel.id, forumModel.jid, parentForum, Lists.newArrayList(), forumModel.name, forumModel.description, forumModuleBuilder.build(), timeUpdate, userUpdate);
    }

    static Forum createForumWithParentFromModel(ForumDao forumDao, ForumLastPostDao forumLastPostDao, ForumModuleDao forumModuleDao, ForumModuleFactory forumModuleFactory, ForumModel intendedForumModel) {
        Stack<ForumModel> forumModelStack = new Stack<>();
        forumModelStack.push(intendedForumModel);
        while (!forumModelStack.peek().parentJid.isEmpty()) {
            forumModelStack.push(forumDao.findByJid(forumModelStack.peek().parentJid));
        }

        List<String> forumJids = forumModelStack.stream().map(m -> m.jid).collect(Collectors.toList());

        List<ForumModuleModel> forumModuleModels = forumModuleDao.findSortedByFilters("id", "asc", "", ImmutableMap.of(ForumModuleModel_.enabled, Boolean.valueOf(true)), ImmutableMap.of(ForumModuleModel_.forumJid, forumJids), 0, -1);
        Map<String, List<ForumModuleModel>> mapForumJidsToForumModuleModel = Maps.newHashMap();
        for (ForumModuleModel forumModuleModel : forumModuleModels) {
            List<ForumModuleModel> value;
            if (mapForumJidsToForumModuleModel.containsKey(forumModuleModel.forumJid)) {
                value = mapForumJidsToForumModuleModel.get(forumModuleModel.forumJid);
            } else {
                value = Lists.newArrayList();
            }
            value.add(forumModuleModel);
            mapForumJidsToForumModuleModel.put(forumModuleModel.forumJid, value);
        }

        List<ForumLastPostModel> forumLastPostModels = forumLastPostDao.findSortedByFiltersIn("id", "asc", "", ImmutableMap.of(ForumLastPostModel_.forumJid, forumJids), 0, -1);
        ImmutableMap.Builder<String, ForumLastPostModel> mapForumJidsToLastPostModelBuilder = ImmutableMap.builder();
        for (ForumLastPostModel forumLastPostModel : forumLastPostModels) {
            mapForumJidsToLastPostModelBuilder.put(forumLastPostModel.forumJid, forumLastPostModel);
        }
        Map<String, ForumLastPostModel> mapForumJidsToLastPostModel = mapForumJidsToLastPostModelBuilder.build();

        Forum parentForum = null;
        Forum intendedForum = null;
        while (!forumModelStack.isEmpty()) {
            ForumModel currentForumModel = forumModelStack.pop();

            List<ForumModuleModel> currentForumModuleModels = mapForumJidsToForumModuleModel.get(currentForumModel.jid);
            if (currentForumModuleModels == null) {
                currentForumModuleModels = ImmutableList.of();
            }

            Date timeUpdate = null;
            String userUpdate = "-";
            ForumLastPostModel forumLastPostModel = mapForumJidsToLastPostModel.get(currentForumModel.jid);
            if (forumLastPostModel != null) {
                timeUpdate = new Date(forumLastPostModel.timeUpdate);
                userUpdate = forumLastPostModel.userUpdate;
            }

            Forum currentForum = ForumServiceUtils.createForumFromModel(forumModuleFactory, currentForumModel, parentForum, currentForumModuleModels, timeUpdate, userUpdate);

            if (currentForumModel.jid.equals(intendedForumModel.jid) && (intendedForum == null)) {
                intendedForum = currentForum;
            } else {
                if (parentForum != null) {
                    parentForum.getSubforums().add(currentForum);
                }
                parentForum = currentForum;
            }
        }

        return intendedForum;
    }

    static Forum createForumWithParentAndSubforumsUntilCertainDepthFromModel(ForumDao forumDao, ForumLastPostDao forumLastPostDao, ForumModuleDao forumModuleDao, ForumModuleFactory forumModuleFactory, ForumModel intendedForumModel, long maxDepth) {
        Forum intendedForum = createForumWithParentFromModel(forumDao, forumLastPostDao, forumModuleDao, forumModuleFactory, intendedForumModel);

        intendedForum = createForumWithSubforumsUntilCertainDepthFromModel(forumDao, forumLastPostDao, forumModuleDao, forumModuleFactory, intendedForum, 0, maxDepth);

        return intendedForum;
    }

    static Forum createForumWithSubforumsUntilCertainDepthFromModel(ForumDao forumDao, ForumLastPostDao forumLastPostDao, ForumModuleDao forumModuleDao, ForumModuleFactory forumModuleFactory, Forum currentForum, long currentDepth, long maxDepth) {
        if (currentDepth > maxDepth) {
            return currentForum;
        }
        List<ForumModel> forumModels = forumDao.findSortedByFiltersEq("id", "asc", "", ImmutableMap.of(ForumModel_.parentJid, currentForum.getJid()), 0, -1);

        List<String> forumJids = forumModels.stream().map(m -> m.jid).collect(Collectors.toList());

        List<ForumModuleModel> forumModuleModels = forumModuleDao.findSortedByFilters("id", "asc", "", ImmutableMap.of(ForumModuleModel_.enabled, Boolean.valueOf(true)), ImmutableMap.of(ForumModuleModel_.forumJid, forumJids), 0, -1);
        Map<String, List<ForumModuleModel>> mapForumJidsToForumModuleModel = Maps.newHashMap();
        for (ForumModuleModel forumModuleModel : forumModuleModels) {
            List<ForumModuleModel> value;
            if (mapForumJidsToForumModuleModel.containsKey(forumModuleModel.forumJid)) {
                value = mapForumJidsToForumModuleModel.get(forumModuleModel.forumJid);
            } else {
                value = Lists.newArrayList();
            }
            value.add(forumModuleModel);
            mapForumJidsToForumModuleModel.put(forumModuleModel.forumJid, value);
        }

        List<ForumLastPostModel> forumLastPostModels = forumLastPostDao.findSortedByFiltersIn("id", "asc", "", ImmutableMap.of(ForumLastPostModel_.forumJid, forumJids), 0, -1);
        ImmutableMap.Builder<String, ForumLastPostModel> mapForumJidsToLastPostBuilder = ImmutableMap.builder();
        for (ForumLastPostModel forumLastPostModel : forumLastPostModels) {
            mapForumJidsToLastPostBuilder.put(forumLastPostModel.forumJid, forumLastPostModel);
        }
        Map<String, ForumLastPostModel> mapForumJidsToLastPost = mapForumJidsToLastPostBuilder.build();

        for (ForumModel forumModel : forumModels) {
            List<ForumModuleModel> currentForumModuleModels = mapForumJidsToForumModuleModel.get(forumModel.jid);
            if (currentForumModuleModels == null) {
                currentForumModuleModels = ImmutableList.of();
            }

            Date timeUpdate = null;
            String userUpdate = "-";
            ForumLastPostModel forumLastPostModel = mapForumJidsToLastPost.get(forumModel.jid);
            if (forumLastPostModel != null) {
                timeUpdate = new Date(forumLastPostModel.timeUpdate);
                userUpdate = forumLastPostModel.userUpdate;
            }
            Forum forum = ForumServiceUtils.createForumFromModel(forumModuleFactory, forumModel, currentForum, currentForumModuleModels, timeUpdate, userUpdate);
            forum = createForumWithSubforumsUntilCertainDepthFromModel(forumDao, forumLastPostDao, forumModuleDao, forumModuleFactory, forum, currentDepth + 1, maxDepth);
            currentForum.getSubforums().add(forum);
        }

        return currentForum;
    }

    static void updateForumAndParents(ForumDao forumDao, Forum forum, String userJid, String userIpAddress) {
        ImmutableList.Builder<String> forumJids = ImmutableList.builder();
        Forum currentForum = forum;
        while (currentForum != null) {
            forumJids.add(currentForum.getJid());
            currentForum = currentForum.getParentForum();
        }

        List<ForumModel> forumModels = forumDao.findSortedByFiltersIn("id", "asc", "", ImmutableMap.of(ForumModel_.jid, forumJids.build()), 0, -1);

        for (ForumModel forumModel : forumModels) {
            forumDao.edit(forumModel, userJid, userIpAddress);
        }
    }

    static void updateForumAndParents(ForumDao forumDao, ForumLastPostDao forumLastPostDao, Forum forum, String userJid, String userIpAddress) {
        ImmutableList.Builder<String> forumJids = ImmutableList.builder();
        Forum currentForum = forum;
        while (currentForum != null) {
            forumJids.add(currentForum.getJid());
            currentForum = currentForum.getParentForum();
        }

        List<ForumModel> forumModels = forumDao.findSortedByFiltersIn("id", "asc", "", ImmutableMap.of(ForumModel_.jid, forumJids.build()), 0, -1);

        for (ForumModel forumModel : forumModels) {
            forumDao.edit(forumModel, userJid, userIpAddress);

            ForumLastPostModel forumLastPostModel;
            if (forumLastPostDao.existsByForumJid(forum.getJid())) {
                forumLastPostModel = forumLastPostDao.findByForumJid(forum.getJid());
                forumLastPostDao.edit(forumLastPostModel, userJid, userIpAddress);
            } else {
                forumLastPostModel = new ForumLastPostModel();
                forumLastPostModel.forumJid = forum.getJid();
                forumLastPostDao.persist(forumLastPostModel, userJid, userIpAddress);
            }
        }
    }
}

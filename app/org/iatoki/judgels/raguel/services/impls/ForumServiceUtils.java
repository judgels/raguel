package org.iatoki.judgels.raguel.services.impls;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.raguel.Forum;
import org.iatoki.judgels.raguel.models.daos.ForumDao;
import org.iatoki.judgels.raguel.models.daos.ForumModuleDao;
import org.iatoki.judgels.raguel.models.entities.ForumModel;
import org.iatoki.judgels.raguel.models.entities.ForumModel_;
import org.iatoki.judgels.raguel.models.entities.ForumModuleModel;
import org.iatoki.judgels.raguel.modules.ForumModule;
import org.iatoki.judgels.raguel.modules.ForumModuleFactory;
import org.iatoki.judgels.raguel.modules.ForumModules;

import java.util.Date;
import java.util.List;
import java.util.Stack;

public final class ForumServiceUtils {

    private ForumServiceUtils() {
        //prevent instantiation
    }

    static Forum createForumFromModel(ForumModuleFactory forumModuleFactory, ForumModel forumModel, Forum parentForum, List<ForumModuleModel> forumModulesModel) {
        ImmutableMap.Builder<ForumModules, ForumModule> forumModuleBuilder = ImmutableMap.builder();
        for (ForumModuleModel forumModuleModel : forumModulesModel) {
            ForumModules forumModules = ForumModules.valueOf(forumModuleModel.name);
            forumModuleBuilder.put(forumModules, forumModuleFactory.parseFromConfig(forumModules, forumModuleModel.config));
        }

        return new Forum(forumModel.id, forumModel.jid, parentForum, Lists.newArrayList(), forumModel.name, forumModel.description, forumModuleBuilder.build(), new Date(forumModel.timeUpdate), forumModel.userUpdate);
    }

    static Forum createForumWithParentsFromModel(ForumDao forumDao, ForumModuleDao forumModuleDao, ForumModuleFactory forumModuleFactory, ForumModel intendedForumModel) {
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

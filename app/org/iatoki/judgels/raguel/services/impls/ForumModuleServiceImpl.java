package org.iatoki.judgels.raguel.services.impls;

import com.google.inject.Inject;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.raguel.models.daos.ForumModuleDao;
import org.iatoki.judgels.raguel.models.entities.ForumModuleModel;
import org.iatoki.judgels.raguel.modules.ForumModuleFactory;
import org.iatoki.judgels.raguel.modules.ForumModules;
import org.iatoki.judgels.raguel.services.ForumModuleService;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("forumModuleService")
public final class ForumModuleServiceImpl implements ForumModuleService {

    private final ForumModuleDao forumModuleDao;
    private final ForumModuleFactory forumModuleFactory;

    @Inject
    public ForumModuleServiceImpl(ForumModuleDao forumModuleDao, ForumModuleFactory forumModuleFactory) {
        this.forumModuleDao = forumModuleDao;
        this.forumModuleFactory = forumModuleFactory;
    }

    @Override
    public void enableModule(String forumJid, ForumModules forumModule) {
        if (forumModuleDao.existsInForumByName(forumJid, forumModule.name())) {
            // TODO check by forum style
            ForumModuleModel forumModuleModel = forumModuleDao.findInForumByName(forumJid, forumModule.name());
            forumModuleModel.enabled = true;

            forumModuleDao.edit(forumModuleModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        } else {
            ForumModuleModel forumModuleModel = new ForumModuleModel();
            forumModuleModel.forumJid = forumJid;
            forumModuleModel.name = forumModule.name();
            forumModuleModel.config = forumModuleFactory.createDefaultForumModule(forumModule).toJSONString();
            forumModuleModel.enabled = true;

            forumModuleDao.persist(forumModuleModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        }
    }

    @Override
    public void disableModule(String forumJid, ForumModules forumModule) {
        if (forumModuleDao.existsInForumByName(forumJid, forumModule.name())) {
            ForumModuleModel forumModuleModel = forumModuleDao.findInForumByName(forumJid, forumModule.name());
            forumModuleModel.enabled = false;

            forumModuleDao.edit(forumModuleModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        }
    }
}

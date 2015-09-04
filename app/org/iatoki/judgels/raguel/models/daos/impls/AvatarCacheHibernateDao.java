package org.iatoki.judgels.raguel.models.daos.impls;

import org.iatoki.judgels.jophiel.models.daos.impls.AbstractAvatarCacheHibernateDao;
import org.iatoki.judgels.raguel.models.daos.AvatarCacheDao;
import org.iatoki.judgels.raguel.models.entities.AvatarCacheModel;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("avatarCacheDao")
public final class AvatarCacheHibernateDao extends AbstractAvatarCacheHibernateDao<AvatarCacheModel> implements AvatarCacheDao {

    public AvatarCacheHibernateDao() {
        super(AvatarCacheModel.class);
    }

    @Override
    public AvatarCacheModel createAvatarCacheModel() {
        return new AvatarCacheModel();
    }
}

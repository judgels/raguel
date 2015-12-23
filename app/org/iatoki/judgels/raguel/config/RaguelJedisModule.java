package org.iatoki.judgels.raguel.config;

import org.iatoki.judgels.raguel.RaguelModule;

public final class RaguelJedisModule extends RaguelModule {

    @Override
    protected String getDaosImplPackage() {
        return "org.iatoki.judgels.raguel.models.daos.jedishibernate";
    }
}

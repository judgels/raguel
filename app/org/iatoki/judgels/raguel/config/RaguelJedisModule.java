package org.iatoki.judgels.raguel.config;

public final class RaguelJedisModule extends RaguelModule {

    @Override
    protected String getDaosImplPackage() {
        return "org.iatoki.judgels.raguel.models.daos.jedishibernate";
    }
}

package org.iatoki.judgels.raguel.services.impls;

import org.iatoki.judgels.play.services.impls.AbstractBaseDataMigrationServiceImpl;

import java.sql.SQLException;

public final class RaguelDataMigrationServiceImpl extends AbstractBaseDataMigrationServiceImpl {

    @Override
    public long getCodeDataVersion() {
        return 1;
    }

    @Override
    protected void onUpgrade(long databaseVersion, long codeDatabaseVersion) throws SQLException {
    }
}

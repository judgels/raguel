package org.iatoki.judgels.raguel.services.impls;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import org.hibernate.Session;
import org.hibernate.internal.SessionImpl;
import org.iatoki.judgels.play.JidService;
import org.iatoki.judgels.play.services.impls.AbstractBaseDataMigrationServiceImpl;
import org.testng.collections.Maps;
import play.db.jpa.JPA;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public final class RaguelDataMigrationServiceImpl extends AbstractBaseDataMigrationServiceImpl {

    @Override
    public long getCodeDataVersion() {
        return 1;
    }

    @Override
    protected void onUpgrade(long databaseVersion, long codeDatabaseVersion) throws SQLException {
    }
}

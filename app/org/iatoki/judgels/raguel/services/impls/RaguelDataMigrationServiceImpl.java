package org.iatoki.judgels.raguel.services.impls;

import org.hibernate.Session;
import org.hibernate.internal.SessionImpl;
import org.iatoki.judgels.play.services.impls.AbstractBaseDataMigrationServiceImpl;
import play.db.jpa.JPA;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class RaguelDataMigrationServiceImpl extends AbstractBaseDataMigrationServiceImpl {

    @Override
    public long getCodeDataVersion() {
        return 2;
    }

    @Override
    protected void onUpgrade(long databaseVersion, long codeDatabaseVersion) throws SQLException {
        if (databaseVersion < 2) {
            migrateV1toV2();
        }
    }

    private void migrateV1toV2() throws SQLException {
        SessionImpl session = (SessionImpl) JPA.em().unwrap(Session.class);
        Connection connection = session.getJdbcConnectionAccess().obtainConnection();

        String forumTable = "raguel_forum";
        String forumThreadTable = "raguel_thread";
        String forumLastPostTable = "raguel_forum_last_post";

        Statement statement = connection.createStatement();
        String threadQuery = "SELECT * FROM " + forumThreadTable + " ORDER BY timeUpdate DESC;";
        ResultSet resultSet = statement.executeQuery(threadQuery);
        while (resultSet.next()) {
            String forumJid  = resultSet.getString("forumJid");
            String userUpdate = resultSet.getString("userUpdate");
            long timeUpdate = resultSet.getLong("timeUpdate");

            updateForumAndParentsLastPost(connection, forumTable, forumLastPostTable, forumJid, userUpdate, timeUpdate);
        }
    }

    private void updateForumAndParentsLastPost(Connection connection, String forumTable, String forumLastPostTable, String forumJid, String userUpdate, long timeUpdate) throws SQLException {
        Statement statement1 = connection.createStatement();
        String lastPostQuery = "SELECT COUNT(*) FROM " + forumLastPostTable + " WHERE forumJid = \"" + forumJid + "\";";
        ResultSet resultSet1 = statement1.executeQuery(lastPostQuery);
        resultSet1.next();
        long count = resultSet1.getLong(1);

        if (count == 0) {
            String updateQuery = "INSERT INTO " + forumLastPostTable + " (forumJid, userCreate, timeCreate, ipCreate, userUpdate, timeUpdate, ipUpdate) VALUES(?, ?, ?, ?, ?, ?, ?);";
            PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);
            preparedStatement.setString(1, forumJid);
            preparedStatement.setString(2, userUpdate);
            preparedStatement.setLong(3, timeUpdate);
            preparedStatement.setString(4, "localhost");
            preparedStatement.setString(5, userUpdate);
            preparedStatement.setLong(6, timeUpdate);
            preparedStatement.setString(7, "localhost");
            preparedStatement.executeUpdate();

            Statement statement2 = connection.createStatement();
            String forumQuery = "SELECT * FROM " + forumTable + " WHERE jid = \"" + forumJid + "\";";
            ResultSet resultSet2 = statement2.executeQuery(forumQuery);
            resultSet2.next();
            String parentJid = resultSet2.getString("parentJid");

            if (!parentJid.isEmpty()) {
                updateForumAndParentsLastPost(connection, forumTable, forumLastPostTable, parentJid, userUpdate, timeUpdate);
            }
        }
    }
}

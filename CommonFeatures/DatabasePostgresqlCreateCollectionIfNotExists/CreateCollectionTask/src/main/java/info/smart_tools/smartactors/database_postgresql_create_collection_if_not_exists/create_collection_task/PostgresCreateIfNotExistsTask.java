package info.smart_tools.smartactors.database_postgresql_create_collection_if_not_exists.create_collection_task;

import info.smart_tools.smartactors.database.database_storage.utils.CollectionName;
import info.smart_tools.smartactors.database.interfaces.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.database.interfaces.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.database.interfaces.istorage_connection.IStorageConnection;
import info.smart_tools.smartactors.database.interfaces.istorage_connection.exception.StorageException;
import info.smart_tools.smartactors.database_postgresql.postgres_connection.JDBCCompiledQuery;
import info.smart_tools.smartactors.database_postgresql.postgres_connection.QueryStatement;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.SerializeException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_field_names_storage.Keys;
import info.smart_tools.smartactors.task.interfaces.itask.exception.TaskExecutionException;

import java.sql.PreparedStatement;

/**
 * The database task which is create documents collection if not already exists in Postgres database.
 */
public final class PostgresCreateIfNotExistsTask implements IDatabaseTask {
    /**
     * Connection to the database.
     */
    private IStorageConnection connection;
    /**
     * Name of the collection.
     */
    private CollectionName collection;
    /**
     * Collection creation options.
     */
    private IObject options;
    /**
     * Query, prepared during prepare(), to be compiled during execute().
     */
    private QueryStatement preparedQuery;

    /**
     * Creates the task
     *
     * @param connection the database connection
     */
    public PostgresCreateIfNotExistsTask(final IStorageConnection connection) {
        this.connection = connection;
    }

    @Override
    public void prepare(final IObject query) throws TaskPrepareException {
        try {
            CreateIfNotExistsCollectionMessage message = IOC.resolve(Keys.getOrAdd(CreateIfNotExistsCollectionMessage.class.getCanonicalName()), query);
            collection = message.getCollectionName();
            options = message.getOptions();
            preparedQuery = new QueryStatement();
            CreateTableIfNotExistsSchema.createIfNotExists(preparedQuery, collection, options);
        } catch (Exception e) {
            throw new TaskPrepareException(e);
        }
    }

    @Override
    public void execute() throws TaskExecutionException {
        try {
            JDBCCompiledQuery compiledQuery = (JDBCCompiledQuery) connection.compileQuery(preparedQuery);
            PreparedStatement statement = compiledQuery.getPreparedStatement();
            statement.execute();
            connection.commit();
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (StorageException se) {
                // ignoring rollback failure
            }
            try {
                throw new TaskExecutionException("Create " + collection + " collection failed, options: " +
                        (options != null ? options.serialize() : "null"), e);
            } catch (SerializeException se) {
                throw new TaskExecutionException("Create " + collection + " collection failed", e);
            }
        }
    }
}

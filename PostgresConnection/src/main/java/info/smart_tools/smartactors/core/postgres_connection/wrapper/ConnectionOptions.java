package info.smart_tools.smartactors.core.postgres_connection.wrapper;

import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

/**
 * IObjectWrapper for connection parameters
 */
public interface ConnectionOptions {
    /**
<<<<<<< HEAD
     * @return url of database
=======
     * @return url to connect with database
>>>>>>> 981b6c84a553320f10396ad499c0dfa303cb4dcd
     * @throws ReadValueException if any errors occurred
     */
    String getUrl() throws ReadValueException;

    /**
     * @return username of db's user
     * @throws ReadValueException if any errors occurred
     */
    String getUsername() throws ReadValueException;

    /**
     * @return return password for user
     * @throws ReadValueException if any errors occurred
     */
    String getPassword() throws ReadValueException;

    /**
     * @return maximum of connections for this pool
     * @throws ReadValueException if any errors occurred
     */
    Integer getMaxConnections() throws ReadValueException;

    /**
     * @param url url to connect with database
     * @throws ChangeValueException if any errors occurred
     */
    void setUrl(String url) throws ChangeValueException;

    /**
     * @param username of database user
     * @throws ChangeValueException if any errors occurred
     */
    void setUsername(String username) throws ChangeValueException;

    /**
     * @param password od database user
     * @throws ChangeValueException if any errors occurred
     */
    void setPassword(String password) throws ChangeValueException;

    /**
     * @param maxConnections maximum of connections for this pool
     * @throws ChangeValueException if any errors occurred
     */
    void setMaxConnections(Integer maxConnections) throws ChangeValueException;
}

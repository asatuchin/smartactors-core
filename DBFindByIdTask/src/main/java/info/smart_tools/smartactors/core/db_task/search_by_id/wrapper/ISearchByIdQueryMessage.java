package info.smart_tools.smartactors.core.db_task.search_by_id.wrapper;

import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

/**
 * Wrapper for SearchById message
 */
public interface ISearchByIdQueryMessage {
    /**
     * Return the collectionName
     * @return String the name of collection where object is stored
     * @throws ReadValueException
     */
    CollectionName getCollectionName() throws ReadValueException;
    void setCollectionName(CollectionName collectionName) throws ChangeValueException;

    /**
     * Return the id of document
     * @return String the id of document should to be found
     * @throws ReadValueException
     */
    String getId() throws ReadValueException;
    void setId(String id) throws ChangeValueException;

    /**
     * Set the found object to message
     * @param object the found document
     * @throws ChangeValueException
     */
    void setSearchResult(IObject object) throws ChangeValueException;
    IObject getSearchResult() throws ReadValueException;
}
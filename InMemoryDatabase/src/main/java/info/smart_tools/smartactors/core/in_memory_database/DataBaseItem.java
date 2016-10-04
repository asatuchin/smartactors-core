package info.smart_tools.smartactors.core.in_memory_database;


import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;

public class DataBaseItem {
    private IFieldName idFieldName;
    private IObject document;
    private String collectionName;
    private Object id;

    public DataBaseItem(final IObject document, final String collectionName)
            throws ResolutionException, ReadValueException, InvalidArgumentException {
        this.document = document;
        this.collectionName = collectionName;
        idFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), collectionName + "ID");
        this.id = document.getValue(idFieldName);
    }

    void setId(final Object id) throws ChangeValueException, InvalidArgumentException {
        this.id = id;
        this.document.setValue(idFieldName, id);
    }

    public IObject getDocument() {
        return document;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public Object getId() {
        return id;
    }
}
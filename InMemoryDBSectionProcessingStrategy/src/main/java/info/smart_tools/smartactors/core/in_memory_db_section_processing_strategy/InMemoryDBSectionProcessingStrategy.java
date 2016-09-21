package info.smart_tools.smartactors.core.in_memory_db_section_processing_strategy;

import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.iconfiguration_manager.ISectionStrategy;
import info.smart_tools.smartactors.core.iconfiguration_manager.exceptions.ConfigurationProcessingException;
import info.smart_tools.smartactors.core.idatabase.IDatabase;
import info.smart_tools.smartactors.core.idatabase.exception.IDatabaseException;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.in_memory_database.InMemoryDatabase;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

import java.util.List;

/**
 * Processing strategy for create and fill {@link InMemoryDatabase}
 * <p>
 * <pre>
 *   {
 *       "inMemoryDb": [
 *             {
 *                 "name": "my_collection_name",
 *                 "documents": [
 *                      "{\"foo\": \"bar\"}",
 *                      "{\"foo1\": \"bar1\"}"
 *                 ]
 *             },
 *             {
 *                 // . . .
 *             }
 *         ]
 *     }
 * </pre>
 */
public class InMemoryDBSectionProcessingStrategy implements ISectionStrategy {

    private final IFieldName name;
    private final IFieldName nameFieldName;
    private final IFieldName documentsFieldName;

    /**
     * Constructor
     * @throws ResolutionException if fails to resolve any dependencies
     */
    InMemoryDBSectionProcessingStrategy() throws ResolutionException {
        this.name = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "inMemoryDb");
        this.nameFieldName = IOC.resolve(
                IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()),
                "name"
        );
        this.documentsFieldName = IOC.resolve(
                IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()),
                "documents"
        );
    }

    @Override
    public void onLoadConfig(final IObject config) throws ConfigurationProcessingException {
        try {
            List<IObject> databaseObject = (List<IObject>) config.getValue(name);
            IDatabase dataBase = IOC.resolve(Keys.getOrAdd(InMemoryDatabase.class.getCanonicalName()));
            for (IObject collection : databaseObject) {
                String collectionName = (String) collection.getValue(nameFieldName);
                dataBase.createCollection(collectionName);
                List<String> documents = (List<String>) collection.getValue(documentsFieldName);
                for (String document : documents) {
                    dataBase.insert(IOC.resolve(Keys.getOrAdd(DSObject.class.getCanonicalName()), document), collectionName);
                }
            }
        } catch (ReadValueException | InvalidArgumentException e) {
            throw new ConfigurationProcessingException("Error occurred loading \"inMemoryDb\" configuration section.", e);
        } catch (ResolutionException e) {
            throw new ConfigurationProcessingException("Error occurred resolving \"InMemoryDatabase\".", e);
        } catch (IDatabaseException e) {
            throw new ConfigurationProcessingException(e);
        }
    }

    @Override
    public IFieldName getSectionName() {
        return name;
    }
}

package info.smart_tools.smartactors.core.proof_of_assumption;

import info.smart_tools.smartactors.core.ds_object.FieldName;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.proof_of_assumption.old_generator.CollectionUtils;
import info.smart_tools.smartactors.core.proof_of_assumption.old_generator.Field;
import info.smart_tools.smartactors.core.proof_of_assumption.old_generator.IObjectWrapper;
import info.smart_tools.smartactors.core.proof_of_assumption.old_generator.ListField;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by sevenbits on 6/6/16.
 */
final class WrapperGenerator {

    private static Map<String, String> mapPrimitiveToClass = new HashMap<>(8);
    static {
        mapPrimitiveToClass.put(int.class.getName(), "Integer");
        mapPrimitiveToClass.put(long.class.getName(), "Long");
        mapPrimitiveToClass.put(float.class.getName(), "Float");
        mapPrimitiveToClass.put(double.class.getName(), "Double");
        mapPrimitiveToClass.put(byte.class.getName(), "Byte");
        mapPrimitiveToClass.put(boolean.class.getName(), "Boolean");
        mapPrimitiveToClass.put(char.class.getName(), "Character");
        mapPrimitiveToClass.put(short.class.getName(), "Short");
    }
    private static Map<String, String> mapPrimitivePostfixes = new HashMap<>(8);
    static {
        mapPrimitivePostfixes.put(int.class.getName(), ").intValue()");
        mapPrimitivePostfixes.put(long.class.getName(), ").longValue()");
        mapPrimitivePostfixes.put(boolean.class.getName(), ").booleanValue()");
        mapPrimitivePostfixes.put(byte.class.getName(), ").byteValue()");
        mapPrimitivePostfixes.put(float.class.getName(), ").floatValue()");
        mapPrimitivePostfixes.put(double.class.getName(), ").doubleValue()");
        mapPrimitivePostfixes.put(char.class.getName(), ").charValue()");
        mapPrimitivePostfixes.put(short.class.getName(), ").shortValue()");
    }

    private static Properties properties = new Properties();

    static {
        try(InputStream stream = WrapperGenerator.class
                .getResourceAsStream("/wrapperGenerator.properties")) {
            properties.load(stream);
        } catch (IOException e) {

        }
    }

    static public <T> Class<? extends T> generate(Class<T> targetInterface)
            throws InvalidArgumentException, WrapperGeneratorException {
        if (targetInterface == null) {
            throw new InvalidArgumentException("Target interface must not be null!");
        }
        if (!targetInterface.isInterface()) {
            throw new InvalidArgumentException("Target class must be an interface!");
        }

        try {
            CtClass resultClass = getCtClass(targetInterface);

            StringBuilder constructorBuilder = new StringBuilder();
            for (Method m : targetInterface.getDeclaredMethods()) {
                //regExp splits camelcase
                //IWrapperMethodHandler handler = wrpMethodHandler.get(m.getName().split("(?<!^)(?=[A-Z])")[0]);
                handleInterfaceMethod(resultClass, m, constructorBuilder);
            }
            handleWrapperSpecialMethods(resultClass);
            addConstructor(resultClass, constructorBuilder.toString());

            Class<? extends T> toClass = resultClass.toClass();
            return toClass;
        } catch (Throwable e) {
            throw new WrapperGeneratorException("Could not generate wrapper for target interface.", e);
        }
    }

    private static <T> CtClass getCtClass(Class<T> targetInterface)
            throws NotFoundException, CannotCompileException {
        ClassPool pool = ClassPool.getDefault();
        CtClass createdInterface = pool.get(targetInterface.getName());
        CtClass basisInterface = pool.get(IObjectWrapper.class.getName());

        CtClass resultClass = pool.makeClass(
                MessageFormat
                        .format(
                                properties.getProperty("clazz.name"),
                                targetInterface.getPackage().getName(),
                                targetInterface.getSimpleName()
                        )
        );
        resultClass.addInterface(createdInterface);
        resultClass.addInterface(basisInterface);
        resultClass.addConstructor(CtNewConstructor.defaultConstructor(resultClass));
        resultClass.addField(
                CtField
                        .make(
                                MessageFormat
                                        .format(
                                                properties.getProperty("clazz.field"),
                                                IObject.class.getName(),
                                                properties.getProperty("field.iobject.name")
                                        ),
                                resultClass)
        );
        return resultClass;
    }

    private static void addConstructor(CtClass resultClass, String bodyFromMethods) throws CannotCompileException, NotFoundException {
        resultClass.getDeclaredConstructor(new CtClass[]{}).setBody("{" + bodyFromMethods + "}");
    }

    private static void handleInterfaceMethod(CtClass resultClass, Method m, StringBuilder constructorBuilder)
            throws CannotCompileException, NotFoundException {

        String fieldNameByMethod = getFieldNameByMethod(m);

        boolean needToCreateField = Arrays.stream(resultClass.getDeclaredFields())
                .noneMatch(val -> val.getName().equals(fieldNameByMethod));
        if (needToCreateField) {
            //TODO: refactor this if-else.
            String fieldClassName;
            if (isListField(m)) {
                fieldClassName = ListField.class.getName();
            } else {
                fieldClassName = Field.class.getName();
            }
            resultClass.addField(CtField
                    .make(MessageFormat
                                    .format(
                                            properties.getProperty("clazz.field"),
                                            fieldClassName,
                                            fieldNameByMethod
                                    ),
                            resultClass));
            constructorBuilder.append("this.").append(fieldNameByMethod).append(" = new ").append(fieldClassName)
                    .append("(new ").append(FieldName.class.getName()).append("(\"").append(getNameByMethod(m)).append("\"));");
        }
        CtMethod ctMethod = CtNewMethod.make(getCtClassMethodString(m), resultClass);
        resultClass.addMethod(ctMethod);
    }

    private static void handleWrapperSpecialMethods(CtClass resultClass) throws NoSuchMethodException, CannotCompileException {

        Method method = IObjectWrapper.class.getMethod("extractWrapped");
        String methodBody = "return " + properties.getProperty("field.iobject.name") + ";";
        String methodStr = MessageFormat
                .format(
                        properties.getProperty("method.wrapper"),
                        method.getReturnType().getName(),
                        method.getName(),
                        method.getParameterCount() > 0 ? getParamsString(method) : "",
                        methodBody);
        CtMethod ctMethod = CtNewMethod.make(methodStr, resultClass);
        resultClass.addMethod(ctMethod);

        method = IObjectWrapper.class.getMethod("init", IObject.class);
        methodBody = "this." + properties.getProperty("field.iobject.name") +
                " = " + method.getParameters()[0].getName() + ";";
        methodStr = MessageFormat
                .format(
                        properties.getProperty("method.wrapper"),
                        method.getReturnType().getName(),
                        method.getName(),
                        method.getParameterCount() > 0 ? getParamsString(method) : "",
                        methodBody);
        ctMethod = CtNewMethod.make(methodStr, resultClass);
        resultClass.addMethod(ctMethod);
    }

    private static String getCtClassMethodString(Method method) {
        Class<?> returnType = method.getReturnType();
        String returnTypeName = returnType.getName();
        boolean hasParams = method.getParameterCount() > 0;
        return MessageFormat
                .format(
                        properties.getProperty("method.wrapper"),
                        returnTypeName,
                        method.getName(),
                        hasParams ? getParamsString(method) : "",
                        getCtClassMethodBody(method)
                );
    }

    private static String getCtClassMethodBody(Method method) {
        Class<?> returnType = method.getReturnType();

        String fieldNameIObject = properties.getProperty("field.iobject.name");
        String fieldNameByMethod = getFieldNameByMethod(method);
        if (method.getName().matches(properties.getProperty("regexp.setter"))) {
            //TODO: refactor this if-else. There are two types of setter: for Field and listField
            if (method.getParameterTypes()[0].equals(Iterable.class)) {
                return MessageFormat
                        .format(
                                properties.getProperty("method.body.setter.list"),
                                fieldNameByMethod,
                                fieldNameIObject,
                                CollectionUtils.class.getName(),
                                getPrimitiveToNewObjectString(method.getParameters()[0].getType())
                        );
            } else {
                return MessageFormat
                        .format(
                                properties.getProperty("method.body.setter"),
                                fieldNameByMethod,
                                fieldNameIObject,
                                getPrimitiveToNewObjectString(method.getParameters()[0].getType())
                        );
            }
        } else if (method.getName().matches(properties.getProperty("regexp.getter"))) {
            return MessageFormat
                    .format(
                            properties.getProperty("method.body.getter"),
                            returnType.getName(),
                            getPrimitiveFromObjectPrefix(returnType) + fieldNameByMethod,
                            fieldNameIObject,
                            primitiveToClassString(returnType) + ".class",
                            method.getParameters().length == 0 ? "" : ".get(arg0)",
                            getPrimitiveFromObjectPostfix(returnType)
                    );
        } else if (method.getName().matches(properties.getProperty("regexp.counter"))) {
            return MessageFormat
                    .format(
                            properties.getProperty("method.body.counter"),
                            fieldNameByMethod,
                            fieldNameIObject,
                            "Object.class",
                            ".size()"
                    );
        }
        return "";
    }

    private static String getFieldNameByMethod(Method method) {
        String name = getNameByMethod(method);
        return "_" + name;
    }

    private static String getNameByMethod(Method method) {
        String methodName = method.getName();
        String result = methodName.replaceAll(
                properties.getProperty("regexp.method.to.field.replace"),
                properties.getProperty("regexp.method.to.field.replacement")
        );
        return result.length() > 1 ? result.substring(0,1).toLowerCase() + result.substring(1)
                : result.toLowerCase();
    }

    private static Boolean isListField(Method m) {
        return (m.getName().matches(properties.getProperty("regexp.getter")) && (m.getParameterCount() == 1))
                || m.getName().matches(properties.getProperty("regexp.counter"))
                || (m.getName().matches(properties.getProperty("regexp.setter")) && (m.getParameterTypes()[0].equals(Iterable.class)));
    }

    private static String getParamsString(Method m) {
        StringBuilder sb = new StringBuilder();
        for (Parameter p : m.getParameters()) {
            sb.append(p.getType().getName()).append(" ").append(p.getName()).append(" ");
        }
        return sb.toString();
    }

    private static String getPrimitiveFromObjectPrefix(Class<?> clz) {
        String name = primitiveToClassString(clz);
        if (!clz.getName().equals(name)) {
            return "((" + name + ") ";
        }
        return "";
    }

    private static String getPrimitiveFromObjectPostfix(Class<?> clz) {
        return mapPrimitivePostfixes.getOrDefault(clz.getName(), "");
    }

    private static String primitiveToClassString(Class<?> clz) {
        return mapPrimitiveToClass.getOrDefault(clz.getName(), clz.getName());
    }

    //$0 == this, $1 == first argument of method
    private static String getPrimitiveToNewObjectString(Class<?> clz) {
        if (clz.isPrimitive()) {
            return "new " + primitiveToClassString(clz) + "($1)";
        }
        return "$1";
    }
}


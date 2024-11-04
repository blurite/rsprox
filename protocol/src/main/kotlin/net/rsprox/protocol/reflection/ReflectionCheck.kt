package net.rsprox.protocol.reflection

public sealed interface ReflectionCheck {
    /**
     * Get field value is a reflection check which will aim to call the
     * [java.lang.reflect.Field.getInt] function on the respective field.
     * The value is submitted back in the reply, if a value was obtained.
     * @property className the full class name in which the field exists.
     * @property fieldName the name of the field in that class to look up.
     */
    public class GetFieldValue(
        public val className: String,
        public val fieldName: String,
    ) : ReflectionCheck {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GetFieldValue

            if (className != other.className) return false
            if (fieldName != other.fieldName) return false

            return true
        }

        override fun hashCode(): Int {
            var result = className.hashCode()
            result = 31 * result + fieldName.hashCode()
            return result
        }

        override fun toString(): String {
            return "GetFieldValue(" +
                "className='$className', " +
                "fieldName='$fieldName'" +
                ")"
        }
    }

    /**
     * Set field value aims to try to assign the provided int [value] to
     * a field in the class.
     * @property className the full class name in which the field exists.
     * @property fieldName the name of the field in that class to look up.
     * @property value the value to try to assign to the field.
     */
    public class SetFieldValue(
        public val className: String,
        public val fieldName: String,
        public val value: Int,
    ) : ReflectionCheck {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as SetFieldValue

            if (className != other.className) return false
            if (fieldName != other.fieldName) return false
            if (value != other.value) return false

            return true
        }

        override fun hashCode(): Int {
            var result = className.hashCode()
            result = 31 * result + fieldName.hashCode()
            result = 31 * result + value
            return result
        }

        override fun toString(): String {
            return "SetFieldValue(" +
                "className='$className', " +
                "fieldName='$fieldName', " +
                "value=$value" +
                ")"
        }
    }

    /**
     * Get field modifiers aims to try to look up a given field's modifiers,
     * if possible.
     * @property className the full class name in which the field exists.
     * @property fieldName the name of the field in that class to look up.
     */
    public class GetFieldModifiers(
        public val className: String,
        public val fieldName: String,
    ) : ReflectionCheck {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GetFieldModifiers

            if (className != other.className) return false
            if (fieldName != other.fieldName) return false

            return true
        }

        override fun hashCode(): Int {
            var result = className.hashCode()
            result = 31 * result + fieldName.hashCode()
            return result
        }

        override fun toString(): String {
            return "GetFieldModifiers(" +
                "className='$className', " +
                "fieldName='$fieldName'" +
                ")"
        }
    }

    /**
     * Invoke method check aims to try to invoke a function in a class
     * with the provided parameters. The [parameterValues] are turned
     * into an object using [java.io.ObjectInputStream.readObject] function.
     * @property className the full name of the class in which the function lies.
     * @property methodName the name of the function to invoke.
     * @property parameterClasses the types of the parameters that the function takes.
     * @property parameterValues the values to pass into the function,
     * represented as a serialized byte array.
     * @property returnClass the full name of the return type class
     */
    public class InvokeMethod(
        public val className: String,
        public val methodName: String,
        public val parameterClasses: List<String>,
        public val parameterValues: List<ByteArray>,
        public val returnClass: String,
    ) : ReflectionCheck {
        init {
            require(parameterClasses.size == parameterValues.size) {
                "Parameter classes and values must have an equal length: " +
                    "${parameterClasses.size}, ${parameterValues.size}"
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as InvokeMethod

            if (className != other.className) return false
            if (methodName != other.methodName) return false
            if (parameterClasses != other.parameterClasses) return false
            if (parameterValues != other.parameterValues) return false
            if (returnClass != other.returnClass) return false

            return true
        }

        override fun hashCode(): Int {
            var result = className.hashCode()
            result = 31 * result + methodName.hashCode()
            result = 31 * result + parameterClasses.hashCode()
            result = 31 * result + parameterValues.hashCode()
            result = 31 * result + returnClass.hashCode()
            return result
        }

        override fun toString(): String {
            return "InvokeMethod(" +
                "className='$className', " +
                "methodName='$methodName', " +
                "parameterClasses=$parameterClasses, " +
                "parameterValues=$parameterValues, " +
                "returnClass=$returnClass" +
                ")"
        }
    }

    /**
     * Get method modifiers will aim to try and look up a method's modifiers.
     * @property className the full name of the class in which the function lies.
     * @property methodName the name of the function to invoke.
     * @property parameterClasses the types of the parameters that the function takes.
     * @property returnClass the full name of the return type class
     */
    public class GetMethodModifiers(
        public val className: String,
        public val methodName: String,
        public val parameterClasses: List<String>,
        public val returnClass: String,
    ) : ReflectionCheck {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GetMethodModifiers

            if (className != other.className) return false
            if (methodName != other.methodName) return false
            if (parameterClasses != other.parameterClasses) return false
            if (returnClass != other.returnClass) return false

            return true
        }

        override fun hashCode(): Int {
            var result = className.hashCode()
            result = 31 * result + methodName.hashCode()
            result = 31 * result + parameterClasses.hashCode()
            result = 31 * result + returnClass.hashCode()
            return result
        }

        override fun toString(): String {
            return "GetMethodModifiers(" +
                "className='$className', " +
                "methodName='$methodName', " +
                "parameterClasses=$parameterClasses, " +
                "returnClass=$returnClass" +
                ")"
        }
    }
}

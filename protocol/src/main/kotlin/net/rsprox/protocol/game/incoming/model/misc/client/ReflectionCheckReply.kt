package net.rsprox.protocol.game.incoming.model.misc.client

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory
import net.rsprox.protocol.reflection.ReflectionCheck

/**
 * A reflection check reply is sent by the client whenever a server requests
 * a reflection checker to be performed.
 * @property id the original request id sent by the server.
 * @property result the results from executing the reflection check
 */
public class ReflectionCheckReply(
    public val id: Int,
    public val result: List<ReflectionCheckResult<*>>,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.CLIENT_EVENT

    override fun toString(): String {
        return "ReflectionCheckReply(" +
            "id=$id, " +
            "result=$result" +
            ")"
    }

    public sealed interface ReflectionCheckResult<T : ReflectionCheck> {
        public val check: T
    }

    /**
     * Any error result will be in its own class, as there will not be any
     * return values included in this lot.
     * @property check the reflection check requested by the server
     * @property throwable the throwable class that the client received during either construction or execution.
     */
    public class ErrorResult<T : ReflectionCheck, E : Class<out Throwable>>(
        override val check: T,
        public val throwable: ThrowableResultType<E>,
    ) : ReflectionCheckResult<T> {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ErrorResult<*, *>

            if (check != other.check) return false
            if (throwable != other.throwable) return false

            return true
        }

        override fun hashCode(): Int {
            var result = check.hashCode()
            result = 31 * result + throwable.hashCode()
            return result
        }

        override fun toString(): String =
            "ErrorResult(" +
                "check=$check, " +
                "throwable=$throwable" +
                ")"

        /**
         * The throwable result types notify the user whether the throwable was caught during the
         * construction of the reflection check where it looks up each class/field, or during
         * the execution, where it looks up or assigns new values to properties. As the exceptions
         * overlap, we need to distinguish the two types with a different wrapper.
         * @property throwableClass the class that was thrown.
         */
        public sealed interface ThrowableResultType<E : Class<out Throwable>> {
            public val throwableClass: E

            /**
             * A construction throwable is a throwable that was caught during the construction
             * of a reflection check, e.g. when looking up the classes or fields on which the operations
             * would be performed.
             */
            public class ConstructionThrowable<E : Class<out Throwable>>(
                override val throwableClass: E,
            ) : ThrowableResultType<E> {
                override fun equals(other: Any?): Boolean {
                    if (this === other) return true
                    if (other !is ConstructionThrowable<*>) return false

                    if (throwableClass != other.throwableClass) return false

                    return true
                }

                override fun hashCode(): Int {
                    return throwableClass.hashCode()
                }

                override fun toString(): String {
                    return "ConstructionThrowable(" +
                        "throwableClass=$throwableClass" +
                        ")"
                }
            }

            /**
             * An execution throwable is a throwable that was caught during the execution of a specific
             * operation that was requested, e.g. GetFieldModifiers or SetFieldValue.
             */
            public class ExecutionThrowable<E : Class<out Throwable>>(
                override val throwableClass: E,
            ) : ThrowableResultType<E> {
                override fun equals(other: Any?): Boolean {
                    if (this === other) return true
                    if (other !is ExecutionThrowable<*>) return false

                    if (throwableClass != other.throwableClass) return false

                    return true
                }

                override fun hashCode(): Int {
                    return throwableClass.hashCode()
                }

                override fun toString(): String {
                    return "ExecutionThrowable(" +
                        "throwableClass=$throwableClass" +
                        ")"
                }
            }
        }
    }

    /**
     * Get field value result provides a successful result for retrieving a
     * value of a field in the client.
     * @property check the reflection check requested by the server
     * @property value the value that the client received after invoking reflection
     */
    public class GetFieldValueResult(
        override val check: ReflectionCheck.GetFieldValue,
        public val value: Int,
    ) : ReflectionCheckResult<ReflectionCheck.GetFieldValue> {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GetFieldValueResult

            if (check != other.check) return false
            if (value != other.value) return false

            return true
        }

        override fun hashCode(): Int {
            var result = check.hashCode()
            result = 31 * result + value
            return result
        }

        override fun toString(): String {
            return "GetFieldValueResult(" +
                "check=$check, " +
                "value=$value" +
                ")"
        }
    }

    /**
     * Set field value results will only ever be successful if a value was
     * successfully assigned, in which case nothing gets returned.
     * @property check the reflection check requested by the server
     */
    public class SetFieldValueResult(
        override val check: ReflectionCheck.SetFieldValue,
    ) : ReflectionCheckResult<ReflectionCheck.SetFieldValue> {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as SetFieldValueResult

            return check == other.check
        }

        override fun hashCode(): Int {
            return check.hashCode()
        }

        override fun toString(): String {
            return "SetFieldValueResult(check=$check)"
        }
    }

    /**
     * Get field modifiers result will attempt to look up the modifiers
     * of a field.
     * @property check the reflection check requested by the server
     * @property modifiers the bitpacked modifier values as assigned by the JVM
     */
    public class GetFieldModifiersResult(
        override val check: ReflectionCheck.GetFieldModifiers,
        public val modifiers: Int,
    ) : ReflectionCheckResult<ReflectionCheck.GetFieldModifiers> {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GetFieldModifiersResult

            if (check != other.check) return false
            if (modifiers != other.modifiers) return false

            return true
        }

        override fun hashCode(): Int {
            var result = check.hashCode()
            result = 31 * result + modifiers
            return result
        }

        override fun toString(): String {
            return "GetFieldModifiersResult(" +
                "check=$check, " +
                "modifiers=$modifiers" +
                ")"
        }
    }

    /**
     * Invoke method result is sent when a method invocation was successfully
     * performed with the provided arguments and return type.
     * @property check the reflection check requested by the server
     * @property result the result of invoking the method
     */
    public class InvokeMethodResult<T : MethodInvocationReturnValue>(
        override val check: ReflectionCheck.InvokeMethod,
        public val result: T,
    ) : ReflectionCheckResult<ReflectionCheck.InvokeMethod> {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as InvokeMethodResult<*>

            if (check != other.check) return false
            if (result != other.result) return false

            return true
        }

        override fun hashCode(): Int {
            var result1 = check.hashCode()
            result1 = 31 * result1 + result.hashCode()
            return result1
        }

        override fun toString(): String {
            return "InvokeMethodResult(" +
                "check=$check, " +
                "result=$result" +
                ")"
        }
    }

    /**
     * Get method modifiers will attempt to look up the modifiers of a method
     * using reflection.
     * @property check the reflection check requested by the server
     * @property modifiers the bitpacked modifier values as assigned by the JVM
     */
    public class GetMethodModifiersResult(
        override val check: ReflectionCheck.GetMethodModifiers,
        public val modifiers: Int,
    ) : ReflectionCheckResult<ReflectionCheck.GetMethodModifiers> {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GetMethodModifiersResult

            if (check != other.check) return false
            if (modifiers != other.modifiers) return false

            return true
        }

        override fun hashCode(): Int {
            var result = check.hashCode()
            result = 31 * result + modifiers
            return result
        }

        override fun toString(): String {
            return "GetMethodModifiersResult(" +
                "check=$check, " +
                "modifiers=$modifiers" +
                ")"
        }
    }

    public sealed interface MethodInvocationReturnValue

    /**
     * A null return value is sent if a method invocation returned a null value.
     */
    public data object NullReturnValue : MethodInvocationReturnValue

    /**
     * A number return value is sent if a method returns any [Number] type,
     * in which case the client will call [java.lang.Number.longValue]
     * to retrieve the long representation of the value.
     * @property longValue the long representation of the numeric value.
     */
    public class NumberReturnValue(
        public val longValue: Long,
    ) : MethodInvocationReturnValue {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as NumberReturnValue

            return longValue == other.longValue
        }

        override fun hashCode(): Int {
            return longValue.hashCode()
        }

        override fun toString(): String {
            return "NumberReturnValue(longValue=$longValue)"
        }
    }

    /**
     * A string return value is provided if a method invocation results
     * in a string value.
     * @property stringValue the string value returned by the method.
     */
    public class StringReturnValue(
        public val stringValue: String,
    ) : MethodInvocationReturnValue {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as StringReturnValue

            return stringValue == other.stringValue
        }

        override fun hashCode(): Int {
            return stringValue.hashCode()
        }

        override fun toString(): String {
            return "StringReturnValue(stringValue='$stringValue')"
        }
    }

    /**
     * An unknown return value is provided when a method returns a value,
     * but that value is not a null, a number of a string - essentially
     * the 'else' case if all else falls through.
     */
    public data object UnknownReturnValue : MethodInvocationReturnValue
}

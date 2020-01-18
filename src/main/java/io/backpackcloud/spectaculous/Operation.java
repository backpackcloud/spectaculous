package io.backpackcloud.spectaculous;

import java.util.function.Function;

/**
 * Defines an operation with the target of a Spec.
 *
 * @param <T> the type of the target object
 * @param <R> the type of the result
 */
@FunctionalInterface
public interface Operation<T, R> {

  /**
   * Execute the operation and returns the result.
   *
   * @param object the target object
   * @return the result of the operation.
   * @throws Throwable if anything unexpected happens
   */
  R execute(T object) throws Throwable;

  /**
   * Wraps a Function into an Operation.
   *
   * @param function the function to wrap
   * @return an Operation that calls the given Function
   */
  static <T, R> Operation<T, R> of(Function<T, R> function) {
    return function::apply;
  }

}

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Marcelo Guimarães <ataxexe@backpackcloud.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.backpackcloud.spectaculous;

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

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

package io.backpackcloud.spectaculous;

import java.util.function.Supplier;

/**
 * Initial class that offers a start point for defining specs.
 */
public final class Spec {

  private final String scenario;

  private Spec(String scenario) {
    this.scenario = scenario;
  }

  /**
   * Starts a statement that targets the given object.
   *
   * @param object the object target of this spec statements
   * @return a new spec component for defining the statements
   */
  public <T> Spectacle<T> given(T object) {
    return new Spectaculous<>(scenario, () -> object);
  }

  /**
   * Starts a statement that targets the object supplied by the given supplier.
   * <p>
   * Every statement that makes use of the target will ask the supplier for the object.
   *
   * @param supplier the supplier for the target object of this spec statements
   * @return a new spec component for defining the statements
   */
  public <T> Spectacle<T> given(Supplier<? extends T> supplier) {
    return new Spectaculous<>(scenario, supplier);
  }

  /**
   * Starts a new spec describing it with the given scenario.
   *
   * @param scenario the given scenario that describes this spec.
   * @return a new Spec
   */
  public static Spec describe(String scenario) {
    return new Spec(scenario);
  }

  /**
   * Starts a new spec describing it with the given class.
   *
   * @param type the class that is the target of this spec.
   * @return a new Spec
   */
  public static Spec describe(Class type) {
    return describe(type.getName());
  }

}
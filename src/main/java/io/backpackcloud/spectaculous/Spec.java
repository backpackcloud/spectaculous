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

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Initial class that offers a start point for defining specs.
 */
public final class Spec<T> implements Spectacle<T> {

  private final Supplier<? extends T> supplier;
  private final String reason;
  private final String scenario;

  Spec(String scenario) {
    this(scenario, () -> {throw new SpectacularException("No object given");}, "");
  }

  private Spec(String scenario, Supplier<? extends T> supplier, String reason) {
    this.scenario = scenario;
    this.supplier = supplier;
    this.reason = reason;
  }

  private <E> Spectacle<T> test(E target, Predicate<? super E> predicate) {
    if (!predicate.test(target)) {
      throwSpecException();
    }
    return this;
  }

  private <E> E throwSpecException() {
    return throwSpecException(null);
  }

  private <E> E throwSpecException(Throwable cause) {
    String message = reason.isEmpty() ? scenario : String.format("%s: %s", scenario, reason);
    throw new SpectacularException(message, cause);
  }

  @Override
  public OutcomeDefinition<T> then(TargetedAction<? super T> action) {
    return new OutcomeDefinition<T>() {
      @Override
      public Spectacle<T> willThrow(Class<? extends Throwable> throwable) {
        try {
          action.run(supplier.get());
          throwSpecException();
        } catch (Throwable e) {
          test(e.getClass(), throwable::isAssignableFrom);
        }
        return Spec.this;
      }

      @Override
      public Spectacle<T> willFail() {
        try {
          action.run(supplier.get());
        } catch (Throwable e) {
          return Spec.this;
        }
        return throwSpecException();
      }

      @Override
      public Spectacle<T> willSucceed() {
        try {
          action.run(supplier.get());
        } catch (Throwable e) {
          throwSpecException();
        }
        return Spec.this;
      }
    };
  }

  @Override
  public OutcomeDefinition<T> then(Action action) {
    return new OutcomeDefinition<T>() {
      @Override
      public Spectacle<T> willThrow(Class<? extends Throwable> throwable) {
        try {
          action.run();
          throwSpecException();
        } catch (Throwable e) {
          test(e.getClass(), throwable::isAssignableFrom);
        }
        return Spec.this;
      }

      @Override
      public Spectacle<T> willFail() {
        try {
          action.run();
        } catch (Throwable e) {
          return Spec.this;
        }
        return throwSpecException();
      }

      @Override
      public Spectacle<T> willSucceed() {
        try {
          action.run();
        } catch (Throwable e) {
          throwSpecException();
        }
        return Spec.this;
      }
    };
  }

  @Override
  public Spectacle<T> given(Supplier<T> supplier) {
    return new Spec<>(scenario, supplier, reason);
  }

  @Override
  public Spectacle<T> because(String newReason) {
    return new Spec<>(scenario, supplier, newReason);
  }

  @Override
  public StatementActionDefinition<T> expect(Class<? extends Throwable> throwable) {
    return new StatementActionDefinition<T>() {
      @Override
      public Spectacle<T> when(TargetedAction<? super T> action) {
        try {
          action.run(supplier.get());
          throwSpecException();
        } catch (Throwable e) {
          test(e.getClass(), throwable::isAssignableFrom);
        }
        return Spec.this;
      }

      @Override
      public Spectacle<T> when(Action action) {
        try {
          action.run();
          throwSpecException();
        } catch (Throwable e) {
          test(e.getClass(), throwable::isAssignableFrom);
        }
        return Spec.this;
      }
    };
  }

  @Override
  public <R> StatementOperationDefinition<T, R> expect(Predicate<? super R> predicate) {
    return operation -> {
      try {
        return test(operation.execute(supplier.get()), predicate);
      } catch (Throwable throwable) {
        return throwSpecException(throwable);
      }
    };
  }

  @Override
  public Spectacle<T> waitFor(Action action) {
    try {
      action.run();
    } catch (Throwable throwable) {
      throw new SpectacularException(throwable);
    }
    return this;
  }

  @Override
  public Spectacle<T> waitFor(TargetedAction<? super T> action) {
    try {
      action.run(supplier.get());
    } catch (Throwable throwable) {
      throw new SpectacularException(throwable);
    }
    return this;
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
  public static <T> Spec<T> describe(Class<T> type) {
    return describe(type.getName());
  }

}
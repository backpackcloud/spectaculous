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
 * The default implementation of a Spectacle.
 */
class Spectaculous<T> implements Spectacle<T> {

  private final Supplier<? extends T> supplier;
  private final String reason;
  private final String scenario;

  Spectaculous(String scenario, Supplier<? extends T> supplier) {
    this(scenario, supplier, "");
  }

  private Spectaculous(String scenario, Supplier<? extends T> supplier, String reason) {
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

  private void throwSpecException() {
    throwSpecException(null);
  }

  private <E> E throwSpecException(Throwable cause) {
    String message = reason.isEmpty() ? scenario : String.format("%s: %s", scenario, reason);
    throw new SpectacularException(message, cause);
  }

  @Override
  public Spectacle<T> then(TargetedAction<? super T> action) {
    try {
      action.run(supplier.get());
    } catch (Throwable throwable) {
      throwSpecException(throwable);
    }
    return this;
  }

  @Override
  public Spectacle<T> then(Action action) {
    try {
      action.run();
    } catch (Throwable throwable) {
      throwSpecException(throwable);
    }
    return this;
  }

  @Override
  public Spectacle<T> given(Supplier<T> supplier) {
    return new Spectaculous<>(scenario, supplier, reason);
  }

  @Override
  public Spectacle<T> because(String newReason) {
    return new Spectaculous<>(scenario, supplier, newReason);
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
        return Spectaculous.this;
      }

      @Override
      public Spectacle<T> when(Action action) {
        try {
          action.run();
          throwSpecException();
        } catch (Throwable e) {
          test(e.getClass(), throwable::isAssignableFrom);
        }
        return Spectaculous.this;
      }
    };
  }

  @Override
  public <R> StatementFunctionDefinition<T, R> expect(Predicate<? super R> predicate) {
    return operation -> {
      try {
        return test(operation.execute(supplier.get()), predicate);
      } catch (Throwable throwable) {
        return throwSpecException(throwable);
      }
    };
  }

}

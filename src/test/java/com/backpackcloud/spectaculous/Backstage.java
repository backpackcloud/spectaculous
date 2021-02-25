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

import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class Backstage {

  Object value;
  Object result;
  Supplier supplier;
  Predicate predicate;
  Operation operation;
  Action action;
  TargetedAction targetedAction;

  @BeforeEach
  void init() throws Throwable {
    value = new Object();
    result = new Object();
    supplier = mock(Supplier.class);
    predicate = mock(Predicate.class);
    operation = mock(Operation.class);
    action = mock(Action.class);
    targetedAction = mock(TargetedAction.class);

    when(supplier.get()).thenReturn(value);
    when(operation.execute(value)).thenReturn(result);
    when(predicate.test(result)).thenReturn(true);
  }

  @Test
  public void testPredicateExpect() throws Throwable {
    Spec.describe("test")
        .given(supplier)
        .expect(predicate).from(operation);

    verify(supplier).get();
    verify(predicate).test(result);
    verify(operation).execute(value);
  }

  @Test
  public void testMatcherExpect() throws Throwable {
    Matcher matcher = mock(Matcher.class);
    when(matcher.matches(result)).thenReturn(true);

    Spec.describe("test")
        .given(supplier)
        .expect(matcher).from(operation);

    verify(supplier).get();
    verify(matcher).matches(result);
    verify(operation).execute(value);
  }

  @Test
  public void testSupplierExpect() throws Throwable {
    Supplier expected = mock(Supplier.class);

    when(expected.get()).thenReturn(result);

    Spec.describe("test")
        .given(supplier)
        .expect(expected).from(operation);

    verify(supplier).get();
    verify(expected).get();
    verify(operation).execute(value);
  }

  @Test
  public void testValueExpect() throws Throwable {
    Spec.describe("test")
        .given(supplier)
        .expect(result).from(operation)
        .given(value)
        .expect(result).from(operation);

    assertThrows(SpectacularException.class, () -> {
      Spec.describe("test")
          .given(value)
          .expect(value).from(operation);
    });

    verify(supplier).get();
    verify(operation, times(3)).execute(value);
  }

  @Test
  public void testExceptionExpect() throws Throwable {
    Action action = mock(Action.class);
    TargetedAction targetedAction = mock(TargetedAction.class);

    doThrow(new NullPointerException()).when(targetedAction).run(value);
    doThrow(new NullPointerException()).when(action).run();

    Spec.describe("test")
        .given(supplier)
        .expect(NullPointerException.class).when(action)
        .expect(NullPointerException.class).when(targetedAction);

    assertThrows(SpectacularException.class, () -> {
      Spec.describe("test")
          .given(supplier)
          .expect(IllegalAccessException.class).when(targetedAction);
    });

    assertThrows(SpectacularException.class, () -> {
      Spec.describe("test")
          .given(supplier)
          .expect(IllegalAccessException.class).when(action);
    });

    assertThrows(SpectacularException.class, () -> {
      Spec.describe("test")
          .given(supplier)
          .expect(IllegalAccessException.class).when(() -> {});
    });

    assertThrows(SpectacularException.class, () -> {
      Spec.describe("test")
          .given(value)
          .expect(IllegalAccessException.class).when(o -> {});
    });

    verify(supplier, times(2)).get();
    verify(action, times(2)).run();
    verify(targetedAction, times(2)).run(value);
  }

  @Test
  public void testThenStatement() throws Throwable {
    Spec.describe("test")
        .given(value)
        .then(action).willSucceed()
        .then(targetedAction).willSucceed()
        .given(supplier)
        .then(throwException()).willFail()
        .then(throwException()).willThrow(Exception.class)
        .then(o -> {
          throw new Exception();
        }).willFail()
        .then(o -> {
          throw new Exception();
        }).willThrow(Exception.class);

    assertThrows(SpectacularException.class, () -> {
      Spec.describe("test")
          .given(value)
          .because("It's going to fail")
          .then(throwException()).willSucceed();
    });

    assertThrows(SpectacularException.class, () -> {
      Spec.describe("test")
          .given(value)
          .because("It's going to fail")
          .then(o -> {
            throw new Exception();
          }).willSucceed();
    });

    verify(action).run();
    verify(targetedAction).run(value);
    verify(supplier, times(2)).get();
  }

  @Test
  public void testWrappers() throws Throwable {
    Object result = new Object();
    Object arg = new Object();
    Function function = mock(Function.class);
    Consumer consumer = mock(Consumer.class);
    Runnable runnable = mock(Runnable.class);

    when(function.apply(any())).thenReturn(result);

    Action.of(runnable).run();
    assertSame(result, Operation.of(function).execute(arg));
    TargetedAction.of(consumer).run(arg);

    verify(function).apply(arg);
    verify(runnable).run();
    verify(consumer).accept(arg);
  }

  @Test
  public void testMessages() throws Exception {
    try {
      Spec.describe("Something")
          .given(value)
          .then(throwException()).willSucceed();
      throw new Exception();
    } catch (SpectacularException e) {
      assertEquals("Something", e.getMessage());
    }

    try {
      Spec.describe("Something")
          .given(value)
          .because("is wrong")
          .then(throwException()).willSucceed();
      throw new Exception();
    } catch (SpectacularException e) {
      assertEquals("Something: is wrong", e.getMessage());
    }

    try {
      Spec.describe("Something")
          .given(value)
          .because("is wrong")
          .then(() -> {}).willSucceed()
          .because("is really wrong")
          .then(throwException()).willSucceed();
      throw new Exception();
    } catch (SpectacularException e) {
      assertEquals("Something: is really wrong", e.getMessage());
    }
  }

  @Test
  public void testWaitFor() {

  }

  private Action throwException() {
    return () -> {throw new Exception();};
  }

}

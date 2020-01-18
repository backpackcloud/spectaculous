# Spectaculous

Spectaculous is a framework that helps you defining specifications with the help of a fluent interface. The idea behind is to put some functional interfaces introduced in Java 8 together to provide a beautiful way to define specifications in pure Java code. Its main use is for testing but you can use for any purpose that that requires writing a specification.

## How To Build

Just make sure you have [Maven][] and run the command `maven package`. To install **spectaculous** on your local maven repository, use the command `maven install`. Spectaculous is also in Maven Central. Just use the following `groupId` and `artifactId` to declare the dependency:

- **groupId:** io.backpackcloud
- **artifactId:** spectaculous

## Defining Specifications

To define a new specification, use the `Spec` class. It defines an entry point to the fluent interface:

~~~java
Account myAccount = new Account(500);

Spec.describe("Money transfer")
  .given(new Account(500))
  .expect(500).from(Account::balance)
  
  .then(account -> account.transfer(100).to(myAccount))

  .expect(400).from(Account::balance)
  
  .because("Accounts should have enough funds to make a transfer")
  .expect(InsufficientBalanceException.class).when(account -> account.transfer(1000).to(myAccount))
~~~

~~~java
Spec.describe("A trader is alerted of status")
  .given(newStock("STK").withThresholdOf(15.0))

  .then(tradeAt(5.0))
  
  .expect("OFF").from(alert())

  .then(tradeAt(16.0))
  .expect("ON").from(alert());
~~~

~~~java
Spec.describe(Address.class)
    .given(Address.fromString("bar"))
    .because("The default channel should be assigned if not provided")
    .expect("default").from(Address::channel)
    .expect("bar").from(Address::id)

    .given(Address.fromString("foo:bar"))
    .because("The pattern channel:id should be used to parse the address")
    .expect("foo").from(Address::channel)
    .expect("bar").from(Address::id)

    .because("Addresses from string should have at least an ID")
    .expect(UnbelievableException.class).when(() -> Address.fromString(""))
    .expect(UnbelievableException.class).when(() -> Address.fromString(null));
~~~

[maven]: <https://maven.apache.org>

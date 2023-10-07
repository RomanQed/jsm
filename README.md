# jsm [![maven-central](https://img.shields.io/maven-central/v/com.github.romanqed/jsm?color=blue)](https://repo1.maven.org/maven2/com/github/romanqed/jsm/)

A lightweight library that allows you to create fast finite state machine according to a given scheme.

## Getting Started

To install it, you will need:

* java 11+
* Maven/Gradle

### Features

* Convenient and universal creation of a finite state machine model
* JIT compilation of the transition function according to the scheme of a finite automaton

## Installing

### Gradle dependency

```Groovy
dependencies {
    implementation group: 'com.github.romanqed', name: 'jsm', version: 'LATEST'
}
```

### Maven dependency

```
<dependency>
    <groupId>com.github.romanqed</groupId>
    <artifactId>jsm</artifactId>
    <version>LATEST</version>
</dependency>
```

## Example

```Java
package com.github.romanqed.jsm;

import com.github.romanqed.jsm.bytecode.BytecodeMachineFactory;
import com.github.romanqed.jsm.model.MachineModelBuilder;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        var builder = MachineModelBuilder.create(String.class, Character.class);
        var model = builder
                .setInitState("Init")
                .setExitState("Error")
                .addState("Hello")
                .addState("World")
                .addTransition("Init", "Hello", 'h')
                .addTransition("Hello", "World", 'w')
                .build();
        var factory = new BytecodeMachineFactory();
        var machine = factory.create(model);
        var tokens = List.of('h', 'w');
        System.out.println(machine.run(tokens));
    }
}
```

## Built With

* [Gradle](https://gradle.org) - Dependency management
* [ASM](https://asm.ow2.io) - Generation of transition function
* [jeflect](https://github.com/RomanQed/jeflect) - Class definers and various bytecode tricks

## Authors

* **[RomanQed](https://github.com/RomanQed)** - *com.github.romanqed.jsm.Main work*

See also the list of [contributors](https://github.com/RomanQed/jsm/contributors)
who participated in this project.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details

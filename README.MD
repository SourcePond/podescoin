# Quick Overview
PodesCoin (**PO**st **DES**erialization **CO**mponent **IN**jection) is an OSGi component which aims to inject [Blueprint](https://www.ibm.com/developerworks/library/os-osgiblueprint/) components into objects being de-serialized. Usually, you would need some static method or field if you needed access to external services during de-serialiation of an object. This is not necessary anymore; to specify injection-points in your serializable class, you can just use JSR-330 standard annotations [javax.inject.Inject](http://docs.oracle.com/javaee/7/api/javax/inject/Inject.html) and [javax.inject.Named](http://docs.oracle.com/javaee/7/api/javax/inject/Named.html).

It allows you to work with injectable components in conjunction with your serializable classes without having a tight coupling between them. To achieve this, PodesCoin installs an [OSGi Weaving Hook](https://osgi.org/javadoc/r4v43/core/org/osgi/framework/hooks/weaving/WeavingHook.html) which creates (or enhances an existing) readObject method for injecting components from a Blueprint container into the object, see [JavaDoc of Serializable](http://docs.oracle.com/javase/8/docs/api/java/io/Serializable.html) for further information about readObject.

## Dependencies
Bundle PodesCoin is available from Maven Central:

```
<dependency>
    <groupId>ch.sourcepond.utils</groupId>
    <artifactId>podescoin-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

There is also a Karaf feature available:

```
<dependency>
    <groupId>ch.sourcepond.utils</groupId>
    <artifactId>podescoin-feature</artifactId>
    <version>1.0.0</version>
</dependency>
```

Usually you want to test your serializable classes. Add following dependency to your test scope to get testing support for PodesCoin:

```
<dependency>
    <groupId>ch.sourcepond.utils</groupId>
    <artifactId>podescoin-testing</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage
As mentioned, the usage of PodesCoin requires an OSGi Blueprint extender available. If the bundle which contains your serializable class is not Blueprint enabled, the injection mechanism will *not* work for this bundle. Components are always looked up in the Blueprint container which manages the bundle of your serializable class. 

To weave a serializable class with the PodesCoin injector, you need to add annotation *ch.sourcepond.utils.podescoin.Recipient* on your class as shown in the example below.

```
import ch.sourcepond.utils.podescoin.Recipient;
import java.io.Serializable;

@Recipient
public class Foo implements Serializable {

	private static final long serialVersionUID = 1L;
	
}
```
Note: *@Recipient* is not inherited, so if want a whole class hierarchy to be enhanced with PodesCoin, you need to add this annotation on *every* class.

You have two possibilities to inject Blueprint components into your serializable class: field injection and/or an injector method. You can use both mechanisms at the same time in the same class.

### Field injection
Fields will always be injected **before** a custom readObject and/or the injector method is being executed. 

```
import ch.sourcepond.foo.BarService;
import ch.sourcepond.utils.podescoin.Recipient;
import java.io.Serializable;
import javax.inject.Inject;

@Recipient
public class Foo implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private transient BarService bar;
}
```

The example above assumes that exactly one service of type "ch.sourcepond.foo.BarService" is available in the bundles Blueprint container. If not, the deserialization process will be cancelled with an exception of type "ch.sourcepond.utils.podescoin.AmbiguousComponentException". You can use "javax.inject.Named" to bind a field to a uniquely named component as shown in the example below:

**Note:** A field will only be considered by PodesCoin to be injectable when it's declared **transient** and **non-final**.

```
import ch.sourcepond.foo.BarService;
import ch.sourcepond.utils.podescoin.Recipient;
import java.io.Serializable;
import javax.inject.Inject;
import javax.inject.Named;

@Recipient
public class Foo implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	@Named("myComponent")
	private transient BarService bar;
}
```

In the example above, we bind component "myComponent" to the field. If no component with the name specified is registered with the Blueprint container, the deserialization process will be cancelled with an exception of type "ch.sourcepond.utils.podescoin.NoSuchComponentException".

### Injector method

An injector method can have an arbitrary name, an arbitrary visibility, and, an arbitrary number of arguments. If you need the ObjectInputStream passed to readObject, then you must declare an ObjectInputStream parameter as first argument of your injector method. If you also use field injection at the same time, field injection will be performed **before** your injector method is actually being executed.

Another important fact is, that an injector method is *not* considered to be *virtual* `(INVOKEVIRTUAL)`. Suppose a class **B** which extends class **A**. **A** declares injector method **inject** which is overwritten by class **B**. If PodesCoin calls it on an instance of **B**, but, in the de-serialization context of its super-type **A**, only the instance method on **A** will be executed `(INVOKESPECIAL)`. The instance method on **B** will be *ignored*. This behavior corresponds to the common rules of *readObject* which must be private, and, thus, is called as instance method. See [Java Byte Code Listings](https://en.wikipedia.org/wiki/Java_bytecode_instruction_listings) for further information about INVOKEVIRTUAL and INVOKESPECIAL. Furthermore, do *not* declare a super call in your injection method like super.inject`(...)` in case of class **B**. This will result in PodesCoin failing to enhance the class.  


```
import ch.sourcepond.foo.BarService;
import ch.sourcepond.foo.FoobarService;
import ch.sourcepond.utils.podescoin.Recipient;
import java.io.Serializable;
import javax.inject.Inject;

@Recipient
public class Foo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Inject
	public void inject(BarService b, FoobarService d) {
		// Do awesome stuff here 
	}
}
```

The example above assumes that exactly one instance of each service with type "ch.sourcepond.foo.BarService" and "ch.sourcepond.foo.FoobarService" is available in the bundles Blueprint container. If not, the deserialization process will be cancelled with an exception of type "ch.sourcepond.utils.podescoin.AmbiguousComponentException". You can use "javax.inject.Named" to bind a parameter to a uniquely named component as shown in the example below:

```
import ch.sourcepond.foo.BarService;
import ch.sourcepond.foo.FoobarService;
import ch.sourcepond.utils.podescoin.Recipient;
import java.io.Serializable;
import javax.inject.Inject;
import javax.inject.Named;

@Recipient
public class Foo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Inject
	public void inject(@Named("bar") BarService b, @Named("foobar") FoobarService d) {
		// Do awesome stuff here 
	}
}
```

In the example above, we bind component "bar" to the first parameter and component "foobar" to the second one. If any component with the name specified could not be found in the Blueprint container, the deserialization process will be cancelled with an exception of type "ch.sourcepond.utils.podescoin.NoSuchComponentException".

```
import ch.sourcepond.foo.BarService;
import ch.sourcepond.foo.FoobarService;
import ch.sourcepond.utils.podescoin.Recipient;
import java.io.ObjectInputStream;
import java.io.Serializable;
import javax.inject.Inject;

@Recipient
public class Foo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Inject
	public void inject(ObjectInputStream in, BarService b, FoobarService d) {
		// Do awesome stuff here 
	}
}
```

The code above is almost the same as in the first example, but, with a crucial difference: the first parameter is the ObjectInputStream taken as argument by the readObject method. If you need the ObjectInputStream, for instance for extracting parameter values which are necessary for calling methods on your injected components, then you can do so by declaring an ObjectInputStream parameter as first argument of your injection method. Of course, the same applies if the component parameters are named:

```
import ch.sourcepond.foo.BarService;
import ch.sourcepond.foo.FoobarService;
import ch.sourcepond.utils.podescoin.Recipient;
import java.io.ObjectInputStream;
import java.io.Serializable;
import javax.inject.Inject;
import javax.inject.Named;

@Recipient
public class Foo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Inject
	public void inject(ObjectInputStream in, @Named("bar") BarService b, @Named("foobar") FoobarService d) {
		// Do awesome stuff here 
	}
}
```

## Initialization order
As mentioned in the introduction section, PodesCoin enhances an existing readObject method (or generates it if not existing). Injectable fields will be initialized *before* the body of readObject is executed. This insures, that your services are available when your de-serialization logic is running. The same applies, if you declared an injector method; it will be executed *before** the body of readObject. If you declared both, field injection *and* an injector method, then firstly the field injection will be applied before the injector method is executed.

The generated code does **not** call ObjectInputStream:defaultReadObject() on the input stream passed to readObject in any case. Calling this method is out of scope and lies in the responsibility of the client code.

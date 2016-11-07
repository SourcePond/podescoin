# Quick Overview
PodesCoin (**PO**st **DES**erialization **CO**mponent **IN**jection) is an OSGi component which aims to inject [Blueprint](https://www.ibm.com/developerworks/library/os-osgiblueprint/) components into objects being de-serialized. To specify injections-points in your serializable class, JSR-330 standard annotations [javax.inject.Inject](http://docs.oracle.com/javaee/7/api/javax/inject/Inject.html) and [javax.inject.Named](http://docs.oracle.com/javaee/7/api/javax/inject/Named.html) are used.

It allows to work with external services without tight coupling which makes your code better testable. To achieve this, PodesCoin installs an [OSGi Weaving Hook](https://osgi.org/javadoc/r4v43/core/org/osgi/framework/hooks/weaving/WeavingHook.html) which creates (or enhances an existing) readObject method for injecting components from a Blueprint container into the object, See [JavaDoc of Serializable](http://docs.oracle.com/javase/8/docs/api/java/io/Serializable.html) for further information about readObject.

## Installation
Bundle PodesCoin is available from Maven Central:

```
<dependency>
    <groupId>ch.sourcepond.utils</groupId>
    <artifactId>podescoin-impl</artifactId>
    <version>0.1.0</version>
</dependency>
```

There is also a Karaf feature available:

```
<dependency>
    <groupId>ch.sourcepond.utils</groupId>
    <artifactId>podescoin-feature</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Usage
As mentioned, the usage of PodesCoin requires an OSGi Blueprint extender available. If the bundle which contains your serializable class is not Blueprint enabled, the injection mechanism will *not* work for this bundle. Components are always looked up in the Blueprint container which manages the bundle of your serializable class. You have two possibilities to inject Blueprint components into your serializable class: field injection and/or an injector method. Even if not recommended, you can use both mechanisms at the same time in the same class.

### Field injection

```
import ch.sourcepond.foo.BarService;
import javax.inject.Inject;

public class Foo implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	public transient BarService bar;
}
```

The example above assumes that exactly one service of type "ch.sourcepond.foo.BarService" is available in the bundles Blueprint container. If not, the deserialization process will be cancelled with an exception of type "ch.sourcepond.utils.podescoin.AmbiguousComponentException". You can use "javax.inject.Named" to bind a field to a uniquely named component as shown in the example below:

**Note:** A field will only be considered by PodesCoin to be injectable when it's declared **transient** and **non-final**.

```
import ch.sourcepond.foo.BarService;
import javax.inject.Inject;
import javax.inject.Named;

public class Foo implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	@Named("myComponent")
	public transient BarService bar;
}
```

In the example above, we bind component "myComponent" to the field. If no component with the name specified is registered with the Blueprint container, the deserialization process will be cancelled with an exception of type "ch.sourcepond.utils.podescoin.NoSuchComponentException".

### Injector method

```

```

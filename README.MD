# Quick Overview
PodesCoin (**PO**st **DES**erialization **CO**mponent **IN**jection) is an OSGi component which aims to inject Blueprint services and components into objects being de-serialized. To specify injections-points in your serializable class, JSR-330 standard annotations [javax.inject.Inject](http://docs.oracle.com/javaee/7/api/javax/inject/Inject.html) and [javax.inject.Named](http://docs.oracle.com/javaee/7/api/javax/inject/Named.html) are used.

It allows to work with external services without tight coupling which makes your code better testable. To achieve this, PodesCoin installs an [OSGi Weaving Hook](https://osgi.org/javadoc/r4v43/core/org/osgi/framework/hooks/weaving/WeavingHook.html) which create a new or enhance an existing readObject method for injecting components from the Blueprint registry into the object, See [JavaDoc of Serializable](http://docs.oracle.com/javase/8/docs/api/java/io/Serializable.html) for further information about readObject.

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
As mentioned, the usage of PodesCoin requires an OSGi Blueprint container available. You have two possibilities to inject Blueprint components into your serializable class: field injection and/or an injector method. Even if not recommended, you can use both mechanisms at the same time in the same class.

### Field injection
```
import ch.sourcepond.foo.BarService;

public class Foo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Inject
	public transient BarService bar;
}
```
